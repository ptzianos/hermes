import requests
from flask import Flask

from hermes.user.decorators import authenticated_only


def test_authenticated_only_decorator_forbids_anonymous_session(flask_app: Flask) -> None:
    def test_func(*args, **kwargs):
        pass

    with flask_app.test_request_context():
        resp = authenticated_only(test_func)()
        assert resp.status_code == requests.codes.forbidden, "Decorator did not prevent anonymous user"
