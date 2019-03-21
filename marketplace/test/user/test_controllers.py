from typing import Callable, Iterator, Tuple

import pytest
from flask import Flask

from hermes.exceptions import UnknownToken, UnknownUser
from hermes.types import EmailAddressType, SessionTokenType, UserType


def test_resolve_user_helper(
        flask_app: Flask,
        user_generator: Iterator[Tuple[UserType, EmailAddressType]],
) -> None:
    def assert_user(user, other_user):
        assert user.name == other_user.name, 'Wrong user resolved'
        assert user.fullname == other_user.fullname, 'Wrong user resolved'
        assert user.uuid == other_user.uuid, 'Wrong user resolved'

    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import resolve_user

        # Setup user and email
        test_user, test_email = next(user_generator)
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
        user_generator: Iterator[UserType],
        user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import resolve_token

        test_user, _ = next(user_generator)
        test_session = user_session_factory(test_user)
        # Setup user and email
        g.db_session = flask_app.new_db_session_instance()
        g.db_session.add(test_user)
        g.db_session.add(test_session)
        g.db_session.commit()

        assert resolve_token(test_session).id == test_session.id, 'Wrong token resolved'
        assert resolve_token(test_session.token).id == test_session.id, 'Wrong token resolved'
        with pytest.raises(UnknownToken):
            resolve_token(1)


def test_generate_api_token(
        flask_app: Flask,
        user_generator: Iterator[Tuple[UserType, EmailAddressType]]):
    with flask_app.app_context():
        from flask import g
        from hermes.user.controllers import generate_api_token

        test_user, _ = next(user_generator)
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
