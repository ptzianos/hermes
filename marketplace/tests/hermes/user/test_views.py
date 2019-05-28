import base64
from typing import Iterator

import pytest
import requests
from Crypto.Hash import SHA3_512
from Crypto.PublicKey.ECC import EccKey, import_key as import_ecdsa_key
from Crypto.PublicKey.RSA import RsaKey
from Crypto.Signature.DSS import new as new_dss_sig_scheme
from flask.testing import FlaskClient

from tests.hermes.user.utils import register_user


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_register_view(api_client: FlaskClient,
                       random_email: Iterator[str],
                       ecdsa_key_pair: Iterator[EccKey],
                       rsa_key_pair: Iterator[RsaKey]) -> None:
    from hermes.user.controllers import resolve_user

    # Register a standard user with an ECDSA key
    resp = api_client.post('/api/v1/users/register', data={
        'email': next(random_email),
        'public_key': next(ecdsa_key_pair).export_key(format='PEM'),
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.ok

    user = resolve_user(resp.json['uuid'])
    user2 = resolve_user(resp.json['email'])

    assert user is not None
    assert user2 is not None
    assert user.uuid == user2.uuid

    # Try to register a user with a duplicate email
    resp = api_client.post('/api/v1/users/register', data={
        'email': resp.json['email'],
        'public_key': next(ecdsa_key_pair).export_key(format='PEM'),
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.bad_request

    # Register a user with just an RSA key
    resp = api_client.post('/api/v1/users/register', data={
        'public_key': next(rsa_key_pair).export_key().decode(),
        'public_key_type': 'rsa',
    })
    assert resp.status_code == requests.codes.ok
    assert resolve_user(resp.json['uuid']) is not None

    # Try to register a user with the wrong type of key
    resp = api_client.post('/api/v1/users/register', data={
        'public_key': next(rsa_key_pair).export_key().decode(),
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.bad_request


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_generate_api_token_view(
    flask_app,
    api_client: FlaskClient,
    ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user, pk, _, verification_request = register_user(api_client,
                                                      next(ecdsa_key_pair))

    msg_hash = (SHA3_512.new()
                .update(verification_request.original_message.encode()))
    sig_scheme = new_dss_sig_scheme(import_ecdsa_key(pk.value),
                                    mode='fips-186-3')
    signature = sig_scheme.sign(msg_hash)
    token_endpoint = ('/api/v1/users/{user_uuid}/tokens/'
                      .format(user_uuid=user.uuid))
    api_client.delete_cookie('localhost.local', flask_app.session_cookie_name)
    resp = api_client.post(token_endpoint, data={
        'proof_of_ownership_request': verification_request.token,
        'proof_of_ownership': signature.hex(),
    })

    assert resp.status_code == requests.codes.ok
    assert resp.json.get('token') is not None

    api_token = resp.json.get('token')
    api_client.delete_cookie('localhost.local', flask_app.session_cookie_name)
    resp = api_client.get(
        '/api/v1/users/me',
        headers={
            'Authorization': 'Bearer ' + base64.encodebytes(api_token.encode())
                                               .decode('utf-8')
                                               .strip()
        })

    assert resp.status_code == requests.codes.ok
    assert resp.json['uuid'] == user.uuid


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_cant_generate_token_twice_with_same_verification_request(
    flask_app,
    api_client: FlaskClient,
    ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user, pk, _, verification_request = register_user(api_client,
                                                      next(ecdsa_key_pair))

    msg_hash = (SHA3_512.new()
                .update(verification_request.original_message.encode()))
    sig_scheme = new_dss_sig_scheme(import_ecdsa_key(pk.value),
                                    mode='fips-186-3')
    signature = sig_scheme.sign(msg_hash)
    token_endpoint = ('/api/v1/users/{user_uuid}/tokens/'
                      .format(user_uuid=user.uuid))
    post_data = {
        'proof_of_ownership_request': verification_request.token,
        'proof_of_ownership': signature.hex(),
    }
    api_client.delete_cookie('localhost.local', flask_app.session_cookie_name)
    resp = api_client.post(token_endpoint, data=post_data)

    assert resp.status_code == requests.codes.ok
    assert resp.json.get('token') is not None

    api_client.delete_cookie('localhost.local', flask_app.session_cookie_name)
    resp = api_client.post(token_endpoint, data=post_data)

    assert resp.status_code == requests.codes.forbidden


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_new_verification_request_expires_old_one(
    flask_app,
    api_client: FlaskClient,
    ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user, pk, _, verification_request = register_user(api_client,
                                                      next(ecdsa_key_pair))
    msg_endpoint = ('/api/v1/users/{user_uuid}/keys/{key_id}/message'
                    .format(user_uuid=user.uuid, key_id=pk.uuid))
    api_client.delete_cookie('localhost.local', flask_app.session_cookie_name)
    verification_request_resp = api_client.get(msg_endpoint)

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
    api_client.delete_cookie('localhost.local', flask_app.session_cookie_name)
    resp = api_client.post(token_endpoint, data={
        'proof_of_ownership_request': verification_request.token,
        'proof_of_ownership': signature.hex(),
    })
    assert resp.status_code == requests.codes.forbidden

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
    api_client.delete_cookie('localhost.local', flask_app.session_cookie_name)
    resp = api_client.post(token_endpoint, data={
        'proof_of_ownership_request':
            verification_request_resp.json.get('public_key_verification_token'),
        'proof_of_ownership': signature.hex(),
    })

    assert resp.status_code == requests.codes.ok
    assert resp.json['token'] is not None
