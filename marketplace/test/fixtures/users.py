import random
from datetime import datetime
from typing import Callable, Iterator, Tuple

import pytest
from Crypto.PublicKey.RSA import RsaKey, generate as rsa_generate
from Crypto.PublicKey.ECC import EccKey, generate as ecc_generate
from flask import Flask

from hermes.types import EmailAddressType, SessionTokenType, UserType
from test.fixtures.seeds import *


def infinite_user_generator(
    user_model_factory: Callable[[], UserType],
    email_model_factory: Callable[[], EmailAddressType],
    admin_user: bool = False
) -> Iterator[Tuple[UserType, EmailAddressType]]:
    while True:
        first_name = random.choice(first)
        last_name = random.choice(last)
        email_domain = random.choice(email)
        test_user = user_model_factory()
        test_user.name = test_user.fullname = f"{first_name} {last_name}"
        test_user.admin = admin_user

        email_instance = email_model_factory()
        email_instance.address = f"{first_name}.{last_name}@{email_domain}"
        email_instance.verified = True
        email_instance.owner = test_user
        email_instance.verified_on = datetime.now()

        yield test_user, email_instance


@pytest.fixture
def user(flask_app: Flask) -> Iterator[Tuple[UserType, EmailAddressType]]:
    with flask_app.app_context():
        from hermes.user.models import EmailAddress, User
        return infinite_user_generator(user_model_factory=User,
                                       email_model_factory=EmailAddress,
                                       admin_user=False)


@pytest.fixture
def admin_user(flask_app: Flask) -> Iterator[Tuple[UserType, EmailAddressType]]:
    with flask_app.app_context():
        from hermes.user.models import EmailAddress, User
        return infinite_user_generator(user_model_factory=User,
                                       email_model_factory=EmailAddress,
                                       admin_user=True)


@pytest.fixture
def user_session_factory(flask_app: Flask) -> Callable[[UserType], SessionTokenType]:
    with flask_app.app_context():
        from hermes.user.models import ProxySession, SessionToken, User

        def session_factory(user: User) -> ProxySession:
            persistent_session = SessionToken()
            persistent_session.owner = user
            persistent_session.expired = False
            persistent_session.created_on = datetime.now()
            persistent_session.refresh()
            return persistent_session

        return session_factory


@pytest.fixture
def random_email() -> Iterator[str]:
    def email_generator() -> Iterator[str]:
        while True:
            first_name = random.choice(first)
            last_name = random.choice(last)
            email_domain = random.choice(email)
            yield f"{first_name}.{last_name}@{email_domain}"
    return email_generator()


@pytest.fixture
def rsa_key_pair() -> Iterator[RsaKey]:
    while True:
        yield rsa_generate(1024)


@pytest.fixture
def ecdsa_key_pair() -> Iterator[EccKey]:
    while True:
        yield ecc_generate('secp256r1')
