from datetime import datetime, timedelta
from typing import Callable, Iterator

import pytest
import requests
from flask import Flask, make_response

from hermes.types import SessionTokenType, UserType
from hermes.user.decorators import (admin_only, authenticated_only,
                                    unauthenticated_only)


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_authenticated_only_decorator_forbids_anonymous_session(
    flask_app: Flask
) -> None:
    def test_func(*args, **kwargs):
        pass

    with flask_app.test_request_context():
        resp = authenticated_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, \
            'Decorator did not prevent anonymous user'


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_authenticated_only_decorator_forbids_expired_session(
    flask_app: Flask,
    user: Iterator[UserType],
    user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context():
        from flask import session
        user, _ = next(user)
        session.persistent_session = user_session_factory(user)
        session.persistent_session.expiry = datetime.now() - timedelta(days=1)
        resp = authenticated_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, \
            'Decorator allowed expired authenticated session'


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_authenticated_only_decorator_allows_authenticated_session(
    flask_app: Flask,
    user: Iterator[UserType],
    user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context():
        from flask import session
        user, _ = next(user)
        session.persistent_session = user_session_factory(user)
        resp = authenticated_only(test_func)()
        assert resp.status_code == requests.codes.ok, \
            'Decorator prevented authenticated user'


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_unauthenticated_only_decorator_allows_anonymous_session(
    flask_app: Flask
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context():
        resp = unauthenticated_only(test_func)()
        assert resp.status_code == requests.codes.ok, \
            'Decorator did not allow anonymous user'


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_unauthenticated_only_decorator_redirects_authenticated_session(
    flask_app: Flask,
    user: Iterator[UserType],
    user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('', requests.codes.found)

    with flask_app.test_request_context():
        from flask import session
        user, _ = next(user)
        session.persistent_session = user_session_factory(user)
        resp = unauthenticated_only(test_func)()
        assert resp.status_code == requests.codes.found, \
            'Decorator allowed authenticated user'


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_admin_only_decorator_forbids_anonymous_session(
    flask_app: Flask
) -> None:
    def test_func(*args, **kwargs):
        pass

    with flask_app.test_request_context():
        resp = admin_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, \
            'Decorator did not prevent anonymous user'


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_admin_only_decorator_forbids_non_admin_session(
    flask_app: Flask,
    user: Iterator[UserType],
    user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context():
        from flask import session
        user, _ = next(user)
        session.persistent_session = user_session_factory(user)
        resp = admin_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, \
            'Decorator did not prevent authenticated user'


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_admin_only_decorator_allows_admin_session(
    flask_app: Flask,
    admin_user: Iterator[UserType],
    user_session_factory: Callable[[UserType], SessionTokenType]
) -> None:
    def test_func(*args, **kwargs):
        return make_response('ok', 200)

    with flask_app.test_request_context():
        from flask import session
        user, _ = next(admin_user)
        session.persistent_session = user_session_factory(user)
        resp = admin_only(test_func)()
        assert resp.status_code == requests.codes.ok, \
            'Decorator prevented admin user'
