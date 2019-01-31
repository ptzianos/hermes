import json
from datetime import datetime
from itertools import chain
from functools import partial
from typing import Any
from uuid import uuid4

from flask.sessions import SessionMixin
from sqlalchemy import Boolean, Column, DateTime, ForeignKey, Integer, String
from sqlalchemy.ext.declarative import declared_attr
from werkzeug.datastructures import CallbackDict

from hermes.config import (API_TOKEN_DURATION, EMAIL_VERIFICATION_TOKEN_DURATION,
                           PASSWORD_RESET_TOKEN_DURATION, SESSION_DURATION)
from hermes.db.config import Base


class EmailAddress(Base):
    __tablename__ = 'email_addresses'

    id = Column(Integer, primary_key=True)
    address = Column(String, unique=True)
    verified = Column(Boolean, default=False)
    owner = Column(Integer, ForeignKey('users.id'))


class User(Base):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    uuid = Column(String, unique=True, default=partial(lambda: str(uuid4().hex)))
    admin = Column(Boolean, default=False)
    name = Column(String)
    email = Column(String)
    fullname = Column(String)
    password = Column(String)
    public_key = Column(String, nullable=False)

    def __repr__(self) -> str:
        return ("<User(id='{}', name='{}', fullname='{}', public_key='{}')>"
                .format(self.id, self.name, self.fullname, self.public_key))

    def __str__(self) -> str:
        return "<User(id='{}', name='{}')>".format(self.id, self.name)


class BaseToken:
    id = Column(Integer, primary_key=True)
    token = Column(String, unique=True, nullable=False, default=partial(lambda: str(uuid4().hex)))
    expired = Column(Boolean, default=False)
    expiry = Column(DateTime, nullable=False)

    @declared_attr
    def owner(cls) -> Column:
        return Column(Integer, ForeignKey('users.id'))

    @property
    def is_expired(self) -> bool:
        """Return if session is expired

        If session is not expired but should be, then fix the instance accordingly.
        """
        if self.expired:
            return True
        if datetime.now() + SESSION_DURATION > self.expiry:
            self.expired = True
            return True
        return False

    def revoke(self) -> None:
        """Force expire a session, if it's not already expired"""
        if not self.expired:
            self.expired = True
            self.expiry = datetime.now()

    def refresh(self) -> None:
        if not self.expired:
            self.expiry = datetime.now() + self.duration


class SessionToken(BaseToken, Base):
    __tablename__ = 'session_tokens'

    duration = SESSION_DURATION

    class Meta:
        non_pickled_fields = ['id', 'owner', 'token', 'expired', 'expiry', 'admin_owner', 'failed_login_attempts',
                              'data', 'is_expired', 'is_sudo_session', 'is_anonymous']

    admin_owner = Column(ForeignKey(User.id))
    failed_login_attempts = Column(Integer, default=0)
    data = Column(String, default='')

    def __repr__(self) -> str:
        return ("<Session(owner='{}', token='{}', expiry='{}', expired='{}')>"
                .format(str(self.owner), self.token, self.expiry, self.expired))

    def __init__(self, *args, **kwargs) -> None:
        self.proxy = ProxySession(self)
        super().__init__(*args, **kwargs)

    @property
    def is_su_session(self) -> bool:
        return self.admin_owner is not None

    @property
    def is_anonymous(self) -> bool:
        return self.owner is None


class APIToken(BaseToken, Base):
    __tablename__ = 'api_tokens'

    duration = API_TOKEN_DURATION


class EmailVerificationToken(BaseToken, Base):
    __tablename__ = 'email_verification_tokens'

    duration = EMAIL_VERIFICATION_TOKEN_DURATION


class PasswordResetToken(BaseToken, Base):
    __tablename__ = 'password_reset_tokens'

    duration = PASSWORD_RESET_TOKEN_DURATION


class ProxySession(SessionMixin):
    """Acts as a proxy between the actual persistent object and the outer environment"""

    def __init__(self, session: SessionToken) -> None:
        def json_updater(updated_dict):
            self.persistent_session.data = json.dumps(updated_dict)

        self.new = session.id is None
        self.accessed = False
        self.modified = False
        self.data = CallbackDict(initial=json.loads(session.data) if session.data else {},
                                 on_update=json_updater)
        self.persistent_session = session
        self.db_session = None

    def __getitem__(self, item):
        self.accessed = True
        if item in SessionToken.Meta.non_pickled_fields:
            return getattr(self.persistent_session, item)
        return self.data[item]

    def __setitem__(self, key, value):
        self.modified = True
        if key in SessionToken.Meta.non_pickled_fields:
            setattr(self.persistent_session, key, value)
        else:
            self.data[key] = value

    def __delitem__(self, key):
        if key not in SessionToken.Meta.non_pickled_fields:
            self.data.__delitem__(key)

    def __getattr__(self, item: Any) -> Any:
        self.accessed = True
        if item in ['new', 'accessed', 'modified', 'data', 'persistent_session', 'db_session']:
            return self.__getattribute__(item)
        return getattr(self.persistent_session, item)

    def __setattr__(self, key, value):
        if key in ['new', 'accessed', 'modified', 'data', 'persistent_session', 'db_session']:
            return super().__setattr__(key, value)
        else:
            return setattr(self.persistent_session, key, value)

    def __iter__(self):
        return chain(self.data.__iter__(),
                     map(lambda key: getattr(self.persistent_session, key), SessionToken.Meta.non_pickled_fields))

    def __len__(self):
        return len(self.data) + len(SessionToken.Meta.non_pickled_fields)
