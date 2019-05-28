from typing import Optional, TYPE_CHECKING, Tuple, Union

import requests
from Crypto.PublicKey import ECC, RSA
from flask.testing import FlaskClient

if TYPE_CHECKING:
    from hermes.user.models import (EmailAddress, PublicKey,
                                    PublicKeyVerificationRequest,
                                    User)


def register_user(
    api_client: FlaskClient,
    key: Union[ECC.EccKey, RSA.RsaKey],
    passwd: str = '', name: str = '', email: str = ''
) -> Tuple['User', 'PublicKey', Optional['EmailAddress'], 'PublicKeyVerificationRequest']:
    """Registers a new user.

    Returns the user and the verification token message and id and the email
    object if an email was provided.
    """
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

    from flask import g
    from hermes.user.controllers import resolve_user
    from hermes.user.models import (EmailAddress, PublicKey,
                                    PublicKeyVerificationRequest)

    new_user = resolve_user(resp.json['uuid'])
    assert new_user is not None
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
