from typing import Iterator

import requests
from Crypto.PublicKey.ECC import EccKey
from Crypto.PublicKey.RSA import RsaKey
from flask import Flask
from flask.testing import FlaskClient

from test.user.utils import register_user


def test_register_view(flask_app: Flask,
                       api_client: FlaskClient,
                       random_email: Iterator[str],
                       ecdsa_key_pair: Iterator[EccKey],
                       rsa_key_pair: Iterator[RsaKey]) -> None:

    # Register a standard user with an ECDSA key
    resp = api_client.post('/api/v1/users/register', data={
        'email': next(random_email),
        'public_key': next(ecdsa_key_pair).export_key(format='PEM'),
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.ok
    with flask_app.app_context_and_db_session():
        from hermes.user.controllers import resolve_user
        user = resolve_user(resp.json['uuid'])
        assert user is not None
        user2 = resolve_user(resp.json['email'])
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
    with flask_app.app_context_and_db_session():
        from hermes.user.controllers import resolve_user
        assert resolve_user(resp.json['uuid']) is not None

    # Try to register a user with the wrong type of key
    resp = api_client.post('/api/v1/users/register', data={
        'public_key': next(rsa_key_pair).export_key().decode(),
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.bad_request


def test_generate_key_verification_views(
        flask_app: Flask, api_client: FlaskClient,
        ecdsa_key_pair: Iterator[EccKey]
) -> None:
    user, pk, _ = register_user(flask_app, api_client, next(ecdsa_key_pair))
    resp = api_client.get('/api/v1/users/{user_uuid}/keys/{key_id}/message'.format(user_uuid=user.uuid, key_id=pk.uuid))
