from typing import Iterator

import pytest
import requests
from Crypto.Hash import SHA3_512
from Crypto.PublicKey.ECC import EccKey, import_key as import_ecdsa_key
from Crypto.PublicKey.RSA import RsaKey
from Crypto.Signature.DSS import new as new_dss_sig_scheme

from tests.hermes.user.utils import register_user, register_user_and_get_token
from tests.utils import delete, get, post


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_register_view(random_email: Iterator[str],
                       ecdsa_key_pair: Iterator[EccKey],
                       rsa_key_pair: Iterator[RsaKey]) -> None:
    from hermes.user.controllers import resolve_user

    # Register a new user
    resp = post('/api/v1/users/register', data={
        'email': next(random_email),
        'public_key': next(ecdsa_key_pair).export_key(format='PEM'),
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.ok

    # Fetch user from the database with two ways
    user = resolve_user(resp.json['uuid'])
    user2 = resolve_user(resp.json['email'])

    assert user is not None
    assert user2 is not None
    assert user.uuid == user2.uuid

    # Trying to register a user with a duplicate email should fail
    resp = post('/api/v1/users/register', data={
        'email': resp.json['email'],
        'public_key': next(ecdsa_key_pair).export_key(format='PEM'),
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.bad_request

    # Register a user with just an RSA key should work
    resp = post('/api/v1/users/register', data={
        'public_key': next(rsa_key_pair).export_key().decode(),
        'public_key_type': 'rsa',
    })
    assert resp.status_code == requests.codes.ok
    assert resolve_user(resp.json['uuid']) is not None

    # Trying to register a user with the wrong type of key should fail
    resp = post('/api/v1/users/register', data={
        'public_key': next(rsa_key_pair).export_key().decode(),
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.bad_request


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_generate_api_token_view(
    ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user, pk, _, verification_request = register_user(next(ecdsa_key_pair))

    # Sign the message of the verification request
    msg_hash = (SHA3_512.new()
                .update(verification_request.original_message.encode()))
    sig_scheme = new_dss_sig_scheme(import_ecdsa_key(pk.value),
                                    mode='fips-186-3')
    signature = sig_scheme.sign(msg_hash)
    token_endpoint = ('/api/v1/users/{user_uuid}/tokens/'
                      .format(user_uuid=user.uuid))

    # Request a new api token with wrong signed message
    resp = post(token_endpoint, data={
        'proof_of_ownership_token': verification_request.token,
        'proof_of_ownership': 'fffffff',
    }, no_cookies=True)
    assert resp.status_code == requests.codes.bad_request

    # Request a new api token with the correct signed message
    verification_data = {
        'proof_of_ownership_token': verification_request.token,
        'proof_of_ownership': signature.hex(),
    }
    resp = post(token_endpoint, data=verification_data, no_cookies=True)
    assert resp.status_code == requests.codes.ok
    assert resp.json.get('token') is not None

    api_token = resp.json.get('token')

    # Ensure the same verification request can not be used twice
    resp = post(token_endpoint, data=verification_data, no_cookies=True)
    assert resp.status_code == requests.codes.bad_request

    # Ensure that the api token is working
    resp = get('/api/v1/users/me', api_token=api_token, no_cookies=True)
    assert resp.status_code == requests.codes.ok
    assert resp.json['uuid'] == user.uuid


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_cant_generate_token_twice_with_same_verification_request(
    ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user, pk, _, verification_request = register_user(next(ecdsa_key_pair))

    msg_hash = (SHA3_512.new()
                .update(verification_request.original_message.encode()))
    sig_scheme = new_dss_sig_scheme(import_ecdsa_key(pk.value),
                                    mode='fips-186-3')
    signature = sig_scheme.sign(msg_hash)
    token_endpoint = ('/api/v1/users/{user_uuid}/tokens/'
                      .format(user_uuid=user.uuid))
    post_data = {
        'proof_of_ownership_token': verification_request.token,
        'proof_of_ownership': signature.hex(),
    }
    # Get a new api token
    resp = post(token_endpoint, data=post_data, no_cookies=True)
    assert resp.status_code == requests.codes.ok
    assert resp.json.get('token') is not None

    # Trying to get a new api token with same message should fail
    resp = post(token_endpoint, data=post_data, no_cookies=True)
    assert resp.status_code == requests.codes.bad_request


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_new_verification_request_expires_old_one(
    ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user, pk, _, verification_request = register_user(next(ecdsa_key_pair))

    # Get a new verification request
    msg_endpoint = ('/api/v1/users/{user_uuid}/keys/{key_id}/message'
                    .format(user_uuid=user.uuid, key_id=pk.uuid))
    verification_request_resp = get(msg_endpoint, no_cookies=True)
    assert verification_request_resp.status_code == requests.codes.ok
    assert verification_request_resp.json.get('public_key_verification_token') is not None
    assert verification_request_resp.json.get('public_key_verification_message') is not None
    assert verification_request.is_expired

    # Old verification request should be useless now
    msg_hash = (SHA3_512.new()
                .update(verification_request.original_message.encode()))
    sig_scheme = new_dss_sig_scheme(import_ecdsa_key(pk.value),
                                    mode='fips-186-3')
    signature = sig_scheme.sign(msg_hash)
    token_endpoint = ('/api/v1/users/{user_uuid}/tokens/'
                      .format(user_uuid=user.uuid))
    resp = post(token_endpoint, data={
        'proof_of_ownership_token': verification_request.token,
        'proof_of_ownership': signature.hex(),
    }, no_cookies=True)
    assert resp.status_code == requests.codes.bad_request

    # New verification request must be usable now
    msg_hash = (SHA3_512.new()
                .update(verification_request_resp.json
                        .get('public_key_verification_message')
                        .encode()))
    sig_scheme = new_dss_sig_scheme(import_ecdsa_key(pk.value),
                                    mode='fips-186-3')
    signature = sig_scheme.sign(msg_hash)
    token_endpoint = ('/api/v1/users/{user_uuid}/tokens/'
                      .format(user_uuid=user.uuid))

    # Request a new api token with latest verification request
    resp = post(token_endpoint, data={
        'proof_of_ownership_token':
            verification_request_resp.json.get('public_key_verification_token'),
        'proof_of_ownership': signature.hex(),
    }, no_cookies=True)
    assert resp.status_code == requests.codes.ok
    assert resp.json['token'] is not None


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_user_details_endpoint(
    ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user1, pk1, _, api_token1 = \
        register_user_and_get_token(next(ecdsa_key_pair))
    user2, pk2, _, api_token2 = \
        register_user_and_get_token(next(ecdsa_key_pair))
    admin_user, admin_pk, _, admin_api_token = \
        register_user_and_get_token(next(ecdsa_key_pair), admin=True)

    # Get user's details through the me endpoint
    resp = get('/api/v1/users/{user_id}'.format(user_id=user1.uuid),
               api_token=api_token1.token, no_cookies=True)
    assert resp.status_code == requests.codes.ok

    # Fail getting other user's details
    resp = get('/api/v1/users/{user_id}'.format(user_id=user2.uuid),
               api_token=api_token1.token, no_cookies=True)
    assert resp.status_code == requests.codes.forbidden

    # Get user's details through the users endpoint
    resp = get('/api/v1/users/{user_id}'.format(user_id=user1.uuid),
               api_token=admin_api_token.token, no_cookies=True)
    assert resp.status_code == requests.codes.ok

    # Fail listing all users
    resp = get('/api/v1/users/'.format(user_id=user1.uuid),
               api_token=api_token1.token, no_cookies=True)
    assert resp.status_code == requests.codes.forbidden

    # Get list of all users when admin
    resp = get('/api/v1/users/'.format(user_id=user1.uuid),
               api_token=admin_api_token.token, no_cookies=True)
    assert resp.status_code == requests.codes.ok

    # Get a 404 when requesting non-existent user
    resp = get('/api/v1/users/asdasd', api_token=admin_api_token.token,
               no_cookies=True)
    assert resp.status_code == requests.codes.not_found


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_list_and_revoke_token_endpoints(
    ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user, pk, _, api_token = \
        register_user_and_get_token(next(ecdsa_key_pair))

    # Get a new token
    msg_endpoint = ('/api/v1/users/{user_uuid}/keys/{key_id}/message'
                    .format(user_uuid=user.uuid, key_id=pk.uuid))
    verification_request_resp = get(msg_endpoint, no_cookies=True)
    msg_hash = (SHA3_512.new()
                .update(verification_request_resp.json
                        .get('public_key_verification_message')
                        .encode()))
    sig_scheme = new_dss_sig_scheme(import_ecdsa_key(pk.value),
                                    mode='fips-186-3')
    signature = sig_scheme.sign(msg_hash)
    token_endpoint = ('/api/v1/users/{user_uuid}/tokens/'
                      .format(user_uuid=user.uuid))
    resp = post(token_endpoint, data={
        'proof_of_ownership_token':
            verification_request_resp.json.get('public_key_verification_token'),
        'proof_of_ownership': signature.hex(),
    }, no_cookies=True)
    assert resp.status_code == requests.codes.ok
    assert resp.json['token'] is not None
    api_token2 = resp.json.get('token')

    # List all the tokens without an api token
    resp = get(token_endpoint, no_cookies=True)
    assert resp.status_code == requests.codes.forbidden

    # List all the tokens of another user should fail
    resp = get('/api/v1/users/blaa/tokens/', no_cookies=True)
    assert resp.status_code == requests.codes.forbidden

    # Create name of token
    api_token_name = SHA3_512.new(data=api_token.token.encode()).digest().hex()[:10]

    # List all the tokens
    resp = get(token_endpoint, no_cookies=True, api_token=api_token.token)
    assert resp.status_code == requests.codes.ok
    assert len(resp.json['tokens']) == 2
    assert len(list(filter(lambda t: t['name'] == api_token_name,
                           resp.json['tokens']))) == 1

    # Try to get user's details with new token to ensure it's working
    resp = get('/api/v1/users/me', api_token=api_token.token, no_cookies=True)
    assert resp.status_code == requests.codes.ok

    # Revoke api token without authentication should fail
    resp = delete('/api/v1/users/{user_id}/tokens/{token_name}'
                  .format(user_id=user.uuid, token_name=api_token_name)
                  , no_cookies=True)
    assert resp.status_code == requests.codes.forbidden

    # Revoke api token
    resp = delete('/api/v1/users/{user_id}/tokens/{token_name}'
                  .format(user_id=user.uuid, token_name=api_token_name)
                  , no_cookies=True, api_token=api_token.token)
    assert resp.status_code == requests.codes.ok

    # Try to revoke again token should fail
    resp = delete('/api/v1/users/{user_id}/tokens/{token_name}'
                  .format(user_id=user.uuid, token_name=api_token_name),
                  api_token=api_token.token, no_cookies=True)
    assert resp.status_code == requests.codes.forbidden

    # Try to revoke again token should fail
    resp = delete('/api/v1/users/{user_id}/tokens/{token_name}'
                  .format(user_id=user.uuid, token_name=api_token_name),
                  api_token=api_token2, no_cookies=True)
    assert resp.status_code == requests.codes.bad_request

    # Fail if you try to get user's details with revoked token
    resp = get('/api/v1/users/me', api_token=api_token.token, no_cookies=True)
    assert resp.status_code == requests.codes.forbidden
