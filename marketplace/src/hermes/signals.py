from flask import Flask, g
from flask.wrappers import Response

from hermes.db import init_db


def register_signals(app: Flask):
    @app.before_request
    def before_request(*args, **kwargs):
        init_db()

    @app.after_request
    def after_request(response: Response):
        """Automatically commit the User session object changes once a request is finished"""
        g.db_session.commit()
        # Once the commit is finished then cleanup the session object itself
        g.db_session.remove()
        return response
