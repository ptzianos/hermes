from flask import Flask, g
from flask.wrappers import Response


def register_signals(app: Flask):

    @app.after_request
    def after_request(response: Response):
        """Automatically commit the User session object changes once a request is finished"""
        g.db_session.commit()
        return response