import base64
from typing import Optional, TYPE_CHECKING

from flask import current_app, Flask, g, Request, Response
from flask.sessions import SessionInterface
from sqlalchemy.orm.session import Session

if TYPE_CHECKING:
    from hermes.user.models import ProxySession


class HermesSession(SessionInterface):
    """Implements the Session Flask interface using the Session model."""

    db_session_initialized = False

    @staticmethod
    def _empty_session(db_session: Session) -> 'ProxySession':
        from hermes.user.models import SessionToken

        user_session = SessionToken()
        user_session.refresh()
        db_session.add(user_session)
        return user_session.proxy

    def open_session(
        self, app: Flask, request: Request, *args
    ) -> 'ProxySession':
        from hermes.user.models import APIToken, SessionToken

        # Create a db_session only if one has not been created yet
        if not getattr(g, 'db_session'):
            db_session = g.db_session = \
                current_app.new_db_session_instance()  # type: Session
            self.db_session_initialized = True
        else:
            db_session: Session = g.db_session

        session_token_str: Optional[str] = \
            request.cookies.get(app.session_cookie_name, None)
        authorization_str: Optional[str] = \
            request.headers.get('Authorization', None)

        if session_token_str:
            user_session: SessionToken = (db_session
                                          .query(SessionToken)
                                          .filter_by(token=session_token_str,
                                                     expired=False)
                                          .first())
            if user_session and user_session.is_expired:
                user_session.refresh()
                return user_session.proxy
        elif authorization_str:
            base64_token = ''
            if authorization_str.startswith('Basic'):
                base64_token = authorization_str.split('Basic ')[-1]
            elif authorization_str.startswith('Bearer'):
                base64_token = authorization_str.split('Bearer ')[-1]
            if not base64_token:
                return self._empty_session(db_session)
            token = (base64
                     .decodebytes(base64_token.encode('utf-8',
                                                      errors='ignore'))
                     .decode())
            api_token: APIToken = (db_session
                                   .query(APIToken)
                                   .filter_by(token=token, expired=False)
                                   .first())
            if not api_token or api_token.is_expired:
                return self._empty_session(db_session)
            return api_token.proxy

        return self._empty_session(db_session)

    def save_session(
        self, app: Flask, session: 'ProxySession', response: Response
    ) -> None:
        if not response:
            return
        if isinstance(session.persistent_session, SessionToken):
            # TODO: Work on fixing permanent sessions
            expires = self.get_expiration_time(app, session)
            response.set_cookie(app.session_cookie_name,
                                session['token'],
                                expires=expires,
                                httponly=self.get_cookie_httponly(app),
                                domain=self.get_cookie_domain(app),
                                path=self.get_cookie_path(app),
                                secure=self.get_cookie_secure(app))

        if self.db_session_initialized:
            g.db_session.close()
