import traceback
from time import strftime

from flask import current_app, Flask, g, request
from flask.wrappers import Response


def register_signals(app: Flask):

    @app.after_request
    def after_request(response: Response):
        """Commit the User session object changes once a request is finished.

        It does not close the session object, because otherwise an exception
        will be thrown every time the fields of the PersistentSession object
        are accessed since it will no longer be part of an active session.
        """
        timestamp = strftime('[%Y-%b-%d %H:%M]')
        current_app.log.info('%s %s %s %s %s %s', timestamp,
                             request.remote_addr, request.method,
                             request.scheme, request.full_path,
                             response.status)

        g.db_session.commit()
        return response

    @app.errorhandler(Exception)
    def exceptions(e):
        tb = traceback.format_exc()
        timestamp = strftime('[%Y-%b-%d %H:%M]')
        current_app.log.error('%s %s %s %s %s 5xx INTERNAL SERVER ERROR\n%s',
                              timestamp, request.remote_addr, request.method,
                              request.scheme, request.full_path, tb)
        return e.status_code
