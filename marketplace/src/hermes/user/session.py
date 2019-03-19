import base64

from flask import current_app, Flask, g, make_response, Request, Response
from flask.sessions import SessionInterface

from hermes.user.models import APIToken, ProxySession, SessionToken


class HermesSession(SessionInterface):
    """Implements the Session Flask interface with SQLAlchemy Session model as a backend
    """
    def open_session(self, app: Flask, request: Request, *args) -> 'ProxySession':
        session_token = request.cookies.get(app.session_cookie_name) or None
        # Get the db_session object that has been created in the before request hook
        db_session = g.db_session = current_app.new_db_session_instance()
        if session_token:
            user_session = (db_session
                            .query(SessionToken)
                            .filter_by(token=session_token, expired=False)
                            .first())  # type: SessionToken
            if not user_session or user_session.is_expired:
                session_token = None
            else:
                user_session.refresh()
        authorization_str = request.headers.get('Authorization')  # type: str
        if authorization_str:
            base64_token = ''
            if authorization_str.startswith('Basic'):
                base64_token = authorization_str.split('Basic ')[-1]
            elif authorization_str.startswith('Bearer'):
                base64_token = authorization_str.split('Bearer ')[-1]
            if not base64_token:
                raise Exception  # 403
            token = base64.decodebytes(base64_token.encode('ascii', errors='ignore'))
            api_token = db_session.query(APIToken).filter_by(token=token, expired=False)
            if not api_token:
                return make_response()
            # TODO: Fix the session object. Probably create one but don't save it
            return api_token.owner

        if session_token is None:
            user_session = SessionToken()
            user_session.refresh()
            db_session.add(user_session)

        return user_session.proxy

    def save_session(self, app: Flask, session: 'ProxySession', response: Response) -> None:
        if not response:
            return
        # TODO: Work on fixing permanent sessions
        expires = self.get_expiration_time(app, session)
        response.set_cookie(app.session_cookie_name,
                            session['token'],
                            expires=expires,
                            httponly=self.get_cookie_httponly(app),
                            domain=self.get_cookie_domain(app),
                            path=self.get_cookie_path(app),
                            secure=self.get_cookie_secure(app))
