from flask import Flask, session
from flask.wrappers import Response


def register_signals(app: Flask):

    @app.after_request
    def after_request(response: Response):
        """Automatically commit the User session object changes once a request is finished"""
        session.db_session.commit()
        return response
