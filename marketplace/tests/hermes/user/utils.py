from typing import Optional, TYPE_CHECKING, Tuple, Union

import requests
from Crypto.Hash import SHA3_512
from Crypto.PublicKey.ECC import EccKey, import_key as import_ecdsa_key
from Crypto.PublicKey.RSA import RsaKey
from Crypto.Signature.DSS import new as new_dss_sig_scheme
from flask.testing import FlaskClient

if TYPE_CHECKING:
    from hermes.user.models import (APIToken, EmailAddress, PublicKey,
                                    PublicKeyVerificationRequest,
                                    User)


def register_user(
    api_client: FlaskClient,
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
    resp = api_client.post('/api/v1/users/register', data={
        'email': email,
        'fullname': name,
        'password': passwd,
        'public_key': public_key,
        'public_key_type': 'ecdsa',
    })
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
