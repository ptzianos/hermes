import random
from datetime import datetime, timedelta
from string import ascii_letters
from typing import Callable, Iterator, Tuple

import pytest
from Crypto.Hash import SHA3_512
from Crypto.PublicKey.RSA import RsaKey
from Crypto.Signature.pkcs1_15 import new as new_pkcs115
from flask import Flask

from hermes.exceptions import (AlreadyRegistered, AlreadyVerified,
                               ExpiredToken, ForbiddenAction,
                               UnknownToken, UnknownUser,
                               WrongParameters)
from hermes.types import EmailAddressType, SessionTokenType, UserType


def test_resolve_user_helper(
    flask_app: Flask,
    user: Iterator[Tuple[UserType, EmailAddressType]],
) -> None:
    def assert_user(user, other_user):
        assert user.name == other_user.name, 'Wrong user resolved'
        assert user.fullname == other_user.fullname, 'Wrong user resolved'
        assert user.uuid == other_user.uuid, 'Wrong user resolved'

    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import resolve_user

        # Setup user and email
        test_user, test_email = next(user)
        g.db_session = flask_app.new_db_session_instance()
        g.db_session.add(test_user)
        g.db_session.add(test_email)
        g.db_session.commit()

        assert_user(resolve_user(test_user), test_user)
        assert_user(resolve_user(test_user.name), test_user)
        assert_user(resolve_user(test_user.uuid), test_user)
        assert_user(resolve_user(test_email.address), test_user)
        with pytest.raises(Exception):
            resolve_user(1)
        assert resolve_user('bla') is None


def test_resolve_token_helper(
    flask_app: Flask,
    user: Iterator[UserType],
    user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import resolve_token

        test_user, _ = next(user)
        test_session = user_session_factory(test_user)
        # Setup user and email
        g.db_session = flask_app.new_db_session_instance()
        g.db_session.add(test_user)
        g.db_session.add(test_session)
        g.db_session.commit()

        assert resolve_token(test_session).id == test_session.id, \
            'Wrong token resolved'
        assert resolve_token(test_session.token).id == test_session.id, \
            'Wrong token resolved'
        with pytest.raises(UnknownToken):
            resolve_token(1)


def test_generate_api_token(
    flask_app: Flask,
    user: Iterator[Tuple[UserType, EmailAddressType]]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import generate_api_token

        test_user, _ = next(user)
        # Setup user and email
        g.db_session = flask_app.new_db_session_instance()
        g.db_session.add(test_user)
        g.db_session.commit()

        new_token = generate_api_token(test_user)
        assert new_token.token, 'Wrong token created'
        assert not new_token.expired, 'Wrong token created'
        assert not new_token.is_expired, 'Wrong token created'
        with pytest.raises(UnknownUser):
            generate_api_token('1')


def test_revoke_token(
    flask_app: Flask,
    user: Iterator[Tuple[UserType, EmailAddressType]]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import generate_api_token, revoke_token

        test_user, _ = next(user)
        # Setup user and email
        g.db_session = flask_app.new_db_session_instance()
        g.db_session.add(test_user)
        g.db_session.commit()

        new_token = generate_api_token(test_user)
        revoke_token(test_user, new_token)

        assert new_token.expired, 'Wrong token created'
        assert new_token.is_expired, 'Wrong token created'

        with pytest.raises(UnknownUser):
            revoke_token('1', new_token)
        with pytest.raises(UnknownToken):
            revoke_token(test_user, '1')


def test_user_details(
    flask_app: Flask,
    user: Iterator[Tuple[UserType, EmailAddressType]],
    admin_user: Iterator[Tuple[UserType, EmailAddressType]]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import user_details

        test_user_1, _ = next(user)
        test_user_2, _ = next(user)
        admin_user, _ = next(admin_user)

        g.db_session = flask_app.new_db_session_instance()
        g.db_session.add(test_user_1)
        g.db_session.add(test_user_2)
        g.db_session.add(admin_user)
        g.db_session.commit()

        with pytest.raises(UnknownUser):
            user_details(test_user_1, '1')
        with pytest.raises(UnknownUser):
            user_details('1', test_user_1)
        with pytest.raises(ForbiddenAction):
            user_details(test_user_2, test_user_1)

        details = user_details(test_user_1, test_user_1)

        assert details['name'] == test_user_1.name, 'Wrong details'
        assert details['fullname'] == test_user_1.fullname, 'Wrong details'
        assert details['uuid'] == test_user_1.uuid, 'Wrong details'
        assert details['admin'] == str(test_user_1.admin), 'Wrong details'

        details = user_details(admin_user, test_user_1)

        assert details['name'] == test_user_1.name, 'Wrong details'
        assert details['fullname'] == test_user_1.fullname, 'Wrong details'
        assert details['uuid'] == test_user_1.uuid, 'Wrong details'
        assert details['admin'] == str(test_user_1.admin), 'Wrong details'


def test_list_keys(
    flask_app: Flask,
    user: Iterator[Tuple[UserType, EmailAddressType]]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import list_keys
        from hermes.user.models import PublicKey, User

        def get_public_key(user: User) -> PublicKey:
            pk = PublicKey(
                value=''.join([random.choice(ascii_letters) for _ in range(20)]),
                owner=user,
                type='rsa',
            )
            g.db_session.add(pk)
            g.db_session.commit()
            return pk

        test_user_1, _ = next(user)
        test_user_2, _ = next(user)
        # Setup user and email
        g.db_session = flask_app.new_db_session_instance()
        g.db_session.add(test_user_1)
        g.db_session.add(test_user_2)
        g.db_session.commit()

        new_pk_1_1 = get_public_key(test_user_1)
        new_pk_1_2 = get_public_key(test_user_1)
        new_pk_2_1 = get_public_key(test_user_2)

        pks_1 = list_keys(test_user_1)
        pks_2 = list_keys(test_user_2)

        assert len(pks_1) == 2, 'Wrong number of public keys returned'
        assert len(pks_2) == 1, 'Wrong number of public keys returned'
        assert new_pk_1_1.uuid in list(map(lambda d: d['uuid'], pks_1)), \
            'Wrong pk uuid returned'
        assert new_pk_1_2.uuid in list(map(lambda d: d['uuid'], pks_1)), \
            'Wrong pk uuid returned'
        assert new_pk_2_1.uuid in list(map(lambda d: d['uuid'], pks_2)), \
            'Wrong pk uuid returned'

        with pytest.raises(UnknownUser):
            list_keys('1')


def test_user_registration(
    flask_app: Flask,
    rsa_key_pair: Iterator[RsaKey],
    random_email: Iterator[str]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import register_user

        email = next(random_email)
        email_2 = next(random_email)
        rsa_key = next(rsa_key_pair)
        rsa_key_2 = next(rsa_key_pair)
        rsa_key_3 = next(rsa_key_pair)

        g.db_session = flask_app.new_db_session_instance()

        new_user, _, pk_verification_request = \
            register_user(public_key=rsa_key.export_key().decode(),
                          public_key_type='rsa')

        assert pk_verification_request.owner.id == new_user.id, \
            'Wrong public key verification request'
        assert pk_verification_request.public_key.value == rsa_key.export_key().decode(), \
            'Wrong public key in the database'

        with pytest.raises(AlreadyRegistered):
            register_user(public_key=rsa_key.export_key().decode(),
                          public_key_type='rsa')

        with pytest.raises(WrongParameters):
            register_user(public_key='dgf', public_key_type='rsa')

        with pytest.raises(WrongParameters):
            register_user()

        new_user_2, email_verification, pk_verification_request_2 = \
            register_user(email=email,
                          public_key=rsa_key_2.export_key().decode(),
                          public_key_type='rsa')

        assert email_verification.owner.id == new_user_2.id, \
            'Wrong user created'
        assert pk_verification_request_2.owner.id == new_user_2.id, \
            'Wrong user created'
        assert new_user_2.name == email, 'Wrong name on user'

        with pytest.raises(AlreadyRegistered):
            register_user(email=email_2,
                          public_key=rsa_key_2.export_key().decode(),
                          public_key_type='rsa')

        with pytest.raises(AlreadyRegistered):
            register_user(email=email,
                          public_key=rsa_key_3.export_key().decode(),
                          public_key_type='rsa')


def test_email_verification(
    flask_app: Flask,
    rsa_key_pair: Iterator[RsaKey],
    random_email: Iterator[str]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import register_user, verify_email

        email = next(random_email)
        rsa_key = next(rsa_key_pair)

        g.db_session = flask_app.new_db_session_instance()

        new_user, email_verification, pk_verification_request = \
            register_user(email=email,
                          public_key=rsa_key.export_key().decode(),
                          public_key_type='rsa')

        with pytest.raises(UnknownUser):
            verify_email('bla', email_verification.email.uuid,
                         email_verification.token)

        with pytest.raises(UnknownToken):
            verify_email(new_user, email_verification.email.uuid, 'bla')

        verify_email(new_user, email_verification.email.uuid,
                     email_verification.token)

        with pytest.raises(AlreadyVerified):
            verify_email(new_user, email_verification.email.uuid,
                         email_verification.token)


def test_public_key_verification(
    flask_app: Flask,
    rsa_key_pair: Iterator[RsaKey],
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import register_user, verify_public_key

        rsa_key = next(rsa_key_pair)  # type: RsaKey

        g.db_session = flask_app.new_db_session_instance()

        new_user, _, pk_verification_request = \
            register_user(public_key=rsa_key.export_key().decode(),
                          public_key_type='rsa')

        sig_scheme = new_pkcs115(rsa_key)
        msg_hash = (SHA3_512
                    .new()
                    .update(pk_verification_request.original_message.encode()))
        hexed_sig = sig_scheme.sign(msg_hash).hex()

        with pytest.raises(UnknownToken):
            verify_public_key('bla', hexed_sig)

        with pytest.raises(ValueError):
            verify_public_key(pk_verification_request.token, 'aaaaaaaaaaa')

        pk_verification_request.expiry = datetime.now() - timedelta(hours=1)
        with pytest.raises(ExpiredToken):
            verify_public_key(pk_verification_request.token, hexed_sig)

        pk_verification_request.expiry = datetime.now() + timedelta(hours=1)
        pk_verification_request.expired = False
        verify_public_key(pk_verification_request.token, hexed_sig)


def test_user_authentication(
    flask_app: Flask,
    rsa_key_pair: Iterator[RsaKey],
    random_email: Iterator[str]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import (authenticate_user, register_user,
                                             verify_public_key)

        email = next(random_email)
        rsa_key = next(rsa_key_pair)
        password = 'blaaa'

        g.db_session = flask_app.new_db_session_instance()

        new_user, email_verification_token, pk_verification_request = \
            register_user(email=email, password=password,
                          public_key=rsa_key.export_key().decode(),
                          public_key_type='rsa')

        sig_scheme = new_pkcs115(rsa_key)
        msg_hash = (SHA3_512
                    .new()
                    .update(pk_verification_request.original_message.encode()))
        hexed_sig = sig_scheme.sign(msg_hash).hex()

        email_verification_token.email.verified = True
        email_verification_token.revoke()

        authenticate_user(email_or_username=email, password_plaintext=password)
        authenticate_user(proof_of_ownership=hexed_sig,
                          proof_of_ownership_request=pk_verification_request.token)

        with pytest.raises(UnknownUser):
            authenticate_user(email_or_username='b;a', password_plaintext=password)

        with pytest.raises(WrongParameters):
            authenticate_user(email_or_username=email, password_plaintext='_')

        with pytest.raises(WrongParameters):
            authenticate_user()

        with pytest.raises(ExpiredToken):
            authenticate_user(proof_of_ownership=hexed_sig,
                              proof_of_ownership_request=pk_verification_request.token)
