from functools import partial
from typing import Callable

from flask import Flask

app = Flask(__name__)


def flask_methods(app: Flask):
    def method_decorator(http_method: str) -> Callable:
        return partial(app.route, methods=[http_method])
    return (method_decorator('GET'),
            method_decorator('PUT'),
            method_decorator('POST'),
            method_decorator('PATCH'),
            method_decorator('DELETE'))
