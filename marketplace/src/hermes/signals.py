from flask import Flask, g
from flask.wrappers import Response


def register_signals(app: Flask):

    @app.after_request
    def after_request(response: Response):
        """Automatically commit the User session object changes once a request is finished.

        It does not close the session object, because otherwise an exception will be thrown
        every time the fields of the PersistentSession object are accessed since it will no
        longer be part of an active session.
        """
        g.db_session.commit()
        return response
