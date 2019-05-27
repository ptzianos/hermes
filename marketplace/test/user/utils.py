from typing import Union

import requests
from Crypto.PublicKey import ECC, RSA
from flask import Flask
from flask.testing import FlaskClient


def register_user(flask_app: Flask, api_client: FlaskClient,
                  key: Union[ECC.EccKey, RSA.RsaKey],
                  passwd: str = '', name: str = '', email: str = '') -> 'hermes.user.models.User':
    if isinstance(key, ECC.EccKey):
        public_key = key.export_key(format='PEM')
    elif isinstance(key, RSA.RsaKey):
        public_key = key.export_key().decode()
    else:
        raise Exception('Key must be either RSA or ECDSA')
    resp = api_client.post('/api/v1/users/register', data={
        'email': email,
        'fullname': name,
        'password': passwd,
        'public_key': public_key,
        'public_key_type': 'ecdsa',
    })
    assert resp.status_code == requests.codes.ok
    with flask_app.app_context_and_db_session():
        from hermes.user.controllers import resolve_user
        new_user = resolve_user(resp.json['uuid'])
        assert new_user is not None
        return new_user
