from base64 import decode
from typing import Callable

from flask.sessions import NullSession
from flask.ctx import *


def authenticated(func: Callable) -> Callable:
    """A decorator used to prevent access to endpoints for unauthenticated users.

    If a user is authenticated the user object that corresponds to the user is
    added to the request object. Expected headers in the request conform to the
    `Basic` and `Bearer` schemes as they are defined
    [here](https://tools.ietf.org/html/rfc2617#section-2) and
    [here](https://tools.ietf.org/html/rfc6750#section-2)
    """
    def authentication_middleware(request):
        if request.logged_in:
            return func(request)
        authorization_str = request.data.get('Authorization')  # type: str
        if not authorization_str:
            if authorization_str.startswith('Basic'):
                pass
            elif authorization_str.startswith('Bearer'):
                pass
            else:
                raise Exception  # 403
        raise Exception  # 401
    return authentication_middleware
