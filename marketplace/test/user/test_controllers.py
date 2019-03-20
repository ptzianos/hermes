from typing import Iterator, Tuple

from flask import Flask

from hermes.types import EmailAddressType, UserType


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
