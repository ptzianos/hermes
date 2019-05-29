from typing import Optional, TYPE_CHECKING, Tuple, Union

import requests
from Crypto.Hash import SHA3_512
from Crypto.PublicKey.ECC import EccKey, import_key as import_ecdsa_key
from Crypto.PublicKey.RSA import RsaKey
from Crypto.Signature.DSS import new as new_dss_sig_scheme
from flask.testing import FlaskClient

from tests.utils import post

if TYPE_CHECKING:
    from hermes.user.models import (APIToken, EmailAddress, PublicKey,
                                    PublicKeyVerificationRequest,
                                    User)


def register_user(
    key: Union[EccKey, RsaKey],
    passwd: str = '', name: str = '', email: str = '', admin: bool = False
) -> Tuple['User', 'PublicKey', Optional['EmailAddress'], 'PublicKeyVerificationRequest']:
    """Registers a new user.

    Returns the user, the verification token request model and the email
    object if an email was provided.
    """
    if isinstance(key, EccKey):
        public_key = key.export_key(format='PEM')
    elif isinstance(key, RsaKey):
        public_key = key.export_key().decode()
    else:
        raise Exception('Key must be either RSA or ECDSA')
    resp = post('/api/v1/users/register', data={
        'email': email,
        'fullname': name,
        'password': passwd,
        'public_key': public_key,
        'public_key_type': 'ecdsa',
    }, no_cookies=True)
    assert resp.status_code == requests.codes.ok

    from flask import g
    from hermes.user.controllers import resolve_user
    from hermes.user.models import (EmailAddress, PublicKey,
                                    PublicKeyVerificationRequest)

    new_user = resolve_user(resp.json['uuid'])
    assert new_user is not None
    if admin:
        new_user.admin = True

    pk = g.db_session.query(PublicKey).filter_by(owner_id=new_user.id).first()
    email: Optional[EmailAddress] = (g.db_session
                                     .query(EmailAddress)
                                     .filter_by(owner_id=new_user.id)
                                     .first()
                                     if email else None)

    verification_request: PublicKeyVerificationRequest = (
        g.db_session
         .query(PublicKeyVerificationRequest)
         .filter_by(token=resp.json['public_key_verification_token'])
         .first()
    )
    return new_user, pk, email, verification_request


def register_user_and_get_token(
    key: Union[EccKey, RsaKey],
    passwd: str = '', name: str = '', email: str = '', admin: bool = False
) -> Tuple['User', 'PublicKey', Optional['EmailAddress'], 'APIToken']:
    """Registers a new user.

    Returns the user, the verification token request model and the email
    object if an email was provided.
    """
    user, pk, email, verification_request = register_user(key, passwd, name,
                                                          email, admin)

    msg_hash = (SHA3_512.new()
                .update(verification_request.original_message.encode()))
    sig_scheme = new_dss_sig_scheme(import_ecdsa_key(pk.value),
                                    mode='fips-186-3')
    signature = sig_scheme.sign(msg_hash)
    token_endpoint = ('/api/v1/users/{user_uuid}/tokens/'
                      .format(user_uuid=user.uuid))
    resp = post(token_endpoint, data={
        'proof_of_ownership_request': verification_request.token,
        'proof_of_ownership': signature.hex(),
    }, no_cookies=True)
    assert resp.status_code == requests.codes.ok
    assert resp.json['token'] is not None

    from flask import g
    from hermes.user.models import APIToken
    api_token: APIToken = (g.db_session
                           .query(APIToken)
                           .filter_by(token=resp.json['token'])
                           .first())

    return user, pk, email, api_token
