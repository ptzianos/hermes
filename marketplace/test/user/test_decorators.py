from datetime import datetime, timedelta
from typing import Callable, Iterator

import requests
from flask import Flask, make_response

from hermes.types import SessionTokenType, UserType
from hermes.user.decorators import admin_only, authenticated_only, unauthenticated_only


def test_authenticated_only_decorator_forbids_anonymous_session(flask_app: Flask) -> None:
    def test_func(*args, **kwargs):
        pass

    with flask_app.test_request_context():
        resp = authenticated_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, 'Decorator did not prevent anonymous user'


def test_authenticated_only_decorator_forbids_expired_session(
        flask_app: Flask,
        user_generator: Iterator[UserType],
        user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context() as req:
        from flask import session
        session.persistent_session = user_session_factory(next(user_generator))
        session.persistent_session.expiry = datetime.now() - timedelta(days=1)
        resp = authenticated_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, 'Decorator allowed expired authenticated session'


def test_authenticated_only_decorator_allows_authenticated_session(
    flask_app: Flask,
    user_generator: Iterator[UserType],
    user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context() as req:
        from flask import session
        session.persistent_session = user_session_factory(next(user_generator))
        resp = authenticated_only(test_func)()
        assert resp.status_code == requests.codes.ok, 'Decorator prevented authenticated user'


def test_unauthenticated_only_decorator_allows_anonymous_session(flask_app: Flask) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context():
        resp = unauthenticated_only(test_func)()
        assert resp.status_code == requests.codes.ok, 'Decorator did not allow anonymous user'


def test_unauthenticated_only_decorator_redirects_authenticated_session(
        flask_app: Flask,
        user_generator: Iterator[UserType],
        user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('', requests.codes.found)

    with flask_app.test_request_context() as req:
        from flask import session
        session.persistent_session = user_session_factory(next(user_generator))
        resp = unauthenticated_only(test_func)()
        assert resp.status_code == requests.codes.found, 'Decorator allowed authenticated user'


def test_admin_only_decorator_forbids_anonymous_session(flask_app: Flask) -> None:
    def test_func(*args, **kwargs):
        pass

    with flask_app.test_request_context():
        resp = admin_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, 'Decorator did not prevent anonymous user'


def test_admin_only_decorator_forbids_non_admin_session(
        flask_app: Flask,
        user_generator: Iterator[UserType],
        user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context() as req:
        from flask import session
        session.persistent_session = user_session_factory(next(user_generator))
        resp = admin_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, 'Decorator did not prevent authenticated user'


def test_admin_only_decorator_allows_admin_session(
        flask_app: Flask,
        admin_user_generator: Iterator[UserType],
        user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context() as req:
        from flask import session
        session.persistent_session = user_session_factory(next(admin_user_generator))
        resp = admin_only(test_func)()
        assert resp.status_code == requests.codes.ok, 'Decorator prevented admin user'
