from functools import wraps
from typing import Any, Callable

import requests
from flask import make_response, redirect, Response, session, url_for


def authenticated_only(func) -> Callable[[Any], Response]:
    """A decorator for views that allows only authenticated users.

    Unauthenticated users will get a 403 response.
    """
    @wraps(func)
    def decorator(*args, **kwargs) -> Response:
        if session.is_anonymous:
            return make_response('', requests.codes.forbidden)
        if session.is_expired:
            session.revoke()
            return make_response('', requests.codes.forbidden)
        return func(*args, **kwargs)
    return decorator


def unauthenticated_only(func) -> Callable[[Any], Response]:
    """A decorator that allows only unauthenticated users to access a view.

    Authenticated users will be redirected to the index page.
    """
    @wraps(func)
    def decorator(*args, **kwargs) -> Response:
        if not session.is_anonymous:
            from hermes.views import index
            return redirect(url_for(index.__name__))
        return func(*args, **kwargs)
    return decorator


def admin_only(func) -> Callable[[Any], Response]:
    """A decorator that allows only admin users to access a view.

    All other users will get a 403 response.
    """
    @wraps(func)
    def decorator(*args, **kwargs) -> Response:
        if session.is_anonymous or not session.owner.admin:
            return make_response('', 403)
        return func(*args, **kwargs)
    return decorator
