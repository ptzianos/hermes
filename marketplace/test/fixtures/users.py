import random
from typing import Callable, Iterator

import pytest
from flask import Flask

from hermes.types import SessionTokenType, UserType
from test.fixtures.seeds import *


def infinite_user_generator(
    user_model_factory: Callable[[], UserType],
    admin_user: bool = False
) -> Iterator[UserType]:
    while True:
        first_name = random.choice(first)
        last_name = random.choice(last)
        email_domain = random.choice(email)
        test_user = user_model_factory()
        test_user.name = test_user.fullname = f"{first_name} {last_name}"
        test_user.email = f"{first_name}.{last_name}@{email_domain}"
        test_user.admin = admin_user
        yield test_user


@pytest.fixture
def user_generator(flask_app: Flask) -> Iterator[UserType]:
    with flask_app.app_context():
        from hermes.user.models import User
        return infinite_user_generator(user_model_factory=User, admin_user=False)


@pytest.fixture
def admin_user_generator(flask_app: Flask) -> Iterator[UserType]:
    with flask_app.app_context():
        from hermes.user.models import User
        return infinite_user_generator(user_model_factory=User, admin_user=True)


@pytest.fixture
def user_session_factory(flask_app: Flask) -> Callable[[UserType], SessionTokenType]:
    with flask_app.app_context():
        from hermes.user.models import ProxySession, SessionToken, User

        def session_factory(user: User) -> ProxySession:
            persistent_session = SessionToken()
            persistent_session.owner = user
            persistent_session.refresh()
            return persistent_session

        return session_factory


@pytest.fixture
def random_email() -> str:
    first_name = random.choice(first)
    last_name = random.choice(last)
    email_domain = random.choice(email)
    return f"{first_name}.{last_name}@{email_domain}"
