from functools import wraps
from typing import Any, Callable

from flask import make_response, redirect, Response, session, url_for


def authenticated_only(func) -> Callable[[Any], Response]:
    @wraps(func)
    def decorator(*args, **kwargs) -> Response:
        if session.is_anonymous:
            return make_response('', 403)
        return func(*args, **kwargs)
    return decorator


def unauthenticated_only(func) -> Callable[[Any], Response]:
    @wraps(func)
    def decorator(*args, **kwargs) -> Response:
        if not session.is_anonymous:
            from hermes.views import index
            return redirect(url_for(index.__name__))
        return func(*args, **kwargs)
    return decorator


def admin_only(func) -> Callable[[Any], Response]:
    @wraps(func)
    def decorator(*args, **kwargs) -> Response:
        if session.is_anonymous or not session.owner.admin:
            return make_response('', 403)
        return func(*args, **kwargs)
    return decorator
