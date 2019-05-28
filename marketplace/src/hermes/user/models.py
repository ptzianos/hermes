import json
from datetime import datetime
from itertools import chain
from functools import partial
from typing import Any
from uuid import uuid4

from flask import current_app
from flask.sessions import SessionMixin
from sqlalchemy import (Boolean, Column, DateTime, ForeignKey,
                        Integer, String)
from sqlalchemy.ext.declarative import declared_attr
from sqlalchemy.orm import relationship
from sqlalchemy_utils.types.choice import ChoiceType
from werkzeug.datastructures import CallbackDict

from hermes.config import (API_TOKEN_DURATION,
                           EMAIL_VERIFICATION_TOKEN_DURATION,
                           PASSWORD_RESET_TOKEN_DURATION,
                           PUBLIC_KEY_VERIFICATION_REQUEST_DURATION,
                           SESSION_DURATION)


class User(current_app.Base):
    __tablename__ = 'users'

    id = Column(Integer, primary_key=True)
    uuid = Column(String, unique=True,
                  default=partial(lambda: str(uuid4().hex)))
    admin = Column(Boolean, default=False)
    name = Column(String)
    fullname = Column(String)
    password = Column(String)
    created_on = Column(DateTime, nullable=False, default=datetime.now)
    last_modified_on = Column(DateTime, onupdate=datetime.now)

    def __repr__(self) -> str:
        return ("<User(id='{}', uuid='{}', name='{}', fullname='{}', "
                "public_key='{}')>"
                .format(self.id, self.uuid, self.name, self.fullname,
                        "__public_key__"))

    def __str__(self) -> str:
        return "<User(id='{}', name='{}')>".format(self.uuid, self.name)


class EmailAddress(current_app.Base):
    __tablename__ = 'email_addresses'

    id = Column(Integer, primary_key=True)
    uuid = Column(String, unique=True,
                  default=partial(lambda: str(uuid4().hex)))
    address = Column(String, unique=True)
    verified = Column(Boolean, default=False)
    owner_id = Column(Integer, ForeignKey('users.id'))
    owner = relationship(User, primaryjoin=owner_id == User.id)
    verified_on = Column(DateTime)
    created_on = Column(DateTime, nullable=False, default=datetime.now)
    last_modified_on = Column(DateTime, onupdate=datetime.now)


class PublicKey(current_app.Base):
    __tablename__ = 'public_keys'

    PUBLIC_KEY_TYPES = [
        ('ecdsa', 'ECDSA'),
        ('rsa', 'RSA')
    ]

    id = Column(Integer, primary_key=True)
    uuid = Column(String, unique=True, nullable=False,
                  default=partial(lambda: str(uuid4().hex)))
    value = Column(String, unique=True, nullable=False)
    type = Column(ChoiceType(PUBLIC_KEY_TYPES), nullable=False)
    owner_id = Column(Integer, ForeignKey('users.id'))
    owner = relationship(User, primaryjoin=owner_id == User.id)
    # TODO: Add the hash of the public key
    created_on = Column(DateTime, nullable=False, default=datetime.now)


class BaseToken:
    id = Column(Integer, primary_key=True)
    token = Column(String, unique=True, nullable=False,
                   default=partial(lambda: str(uuid4().hex)))
    created_on = Column(DateTime, nullable=False, default=datetime.now)
    expired = Column(Boolean, default=False)
    expiry = Column(DateTime, nullable=False)

    @declared_attr
    def owner_id(cls) -> Column:
        return Column(Integer, ForeignKey('users.id'))

    @declared_attr
    def owner(cls) -> relationship:
        return relationship(User, primaryjoin=cls.owner_id == User.id)

    @property
    def is_expired(self) -> bool:
        """Return if session is expired

        If session is not expired but should be, then fix the instance accordingly.
        """
        if self.expired:
            return True
        if datetime.now() > self.expiry:
            self.expired = True
            return True
        return False

    def revoke(self) -> None:
        """Force expire a session, if it's not already expired"""
        if not self.expired:
            self.expired = True
            self.expiry = datetime.now()

    def refresh(self) -> 'BaseToken':
        if not self.expired:
            self.expiry = datetime.now() + self.duration
        return self


class AuthenticationToken(BaseToken):

    class Meta:
        non_pickled_fields = ['id', 'owner', 'token', 'expired', 'expiry',
                              'admin_owner', 'failed_login_attempts',
                              'created_on', 'data', 'is_expired',
                              'is_sudo_session', 'is_anonymous']

    data = Column(String, default='')

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._proxy = None

    @property
    def proxy(self) -> 'ProxySession':
        if not getattr(self, '_proxy', None):
            self._proxy = ProxySession(self)
        return self._proxy

    @property
    def is_anonymous(self) -> bool:
        return self.owner is None


class SessionToken(AuthenticationToken, current_app.Base):
    __tablename__ = 'session_tokens'

    duration = SESSION_DURATION

    admin_owner = Column(ForeignKey(User.id))
    failed_login_attempts = Column(Integer, default=0)

    def __repr__(self) -> str:
        return ("<Session(owner='{}', token='{}', expiry='{}', expired='{}')>"
                .format(str(self.owner), self.token, self.expiry, self.expired))

    @property
    def is_su_session(self) -> bool:
        return self.admin_owner is not None


class APIToken(AuthenticationToken, current_app.Base):
    __tablename__ = 'api_tokens'

    duration = API_TOKEN_DURATION


class EmailVerificationToken(BaseToken, current_app.Base):
    __tablename__ = 'email_verification_tokens'

    duration = EMAIL_VERIFICATION_TOKEN_DURATION

    email_id = Column(Integer, ForeignKey('email_addresses.id'))
    email = relationship(EmailAddress,
                         primaryjoin=email_id == EmailAddress.id)


class PasswordResetToken(BaseToken, current_app.Base):
    __tablename__ = 'password_reset_tokens'

    duration = PASSWORD_RESET_TOKEN_DURATION

    # This field is set to True when the reset token is used to
    # change a password
    used = Column(Boolean, default=False)

    @property
    def is_expired(self) -> bool:
        """A token is expired if the expiration date has passed or it has been used."""
        return super().is_expired or self.used


class PublicKeyVerificationRequest(BaseToken, current_app.Base):
    __tablename__ = 'public_key_verification_requests'

    duration = PUBLIC_KEY_VERIFICATION_REQUEST_DURATION

    public_key_id = Column(Integer, ForeignKey('public_keys.id'))
    public_key = relationship(PublicKey,
                              primaryjoin=public_key_id == PublicKey.id)
    original_message = Column(String, nullable=False)


class ProxySession(SessionMixin):
    """Acts as a proxy between the actual persistent object and the outer environment"""

    def __init__(self, session: AuthenticationToken) -> None:
        def json_updater(updated_dict):
            self.persistent_session.data = json.dumps(updated_dict)

        self.new = session.id is None
        self.accessed = False
        self.modified = False
        self.data = CallbackDict(initial=json.loads(session.data) if session.data else {},
                                 on_update=json_updater)
        # Persistent session is the SessionToken model instance
        self.persistent_session = session

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
        if item in ['new', 'accessed', 'modified', 'data', 'persistent_session']:
            return self.__getattribute__(item)
        return getattr(self.persistent_session, item)

    def __setattr__(self, key, value):
        if key in ['new', 'accessed', 'modified', 'data', 'persistent_session']:
            return super().__setattr__(key, value)
        else:
            return setattr(self.persistent_session, key, value)

    def __iter__(self):
        return chain(self.data.__iter__(),
                     map(lambda key: getattr(self.persistent_session, key),
                         SessionToken.Meta.non_pickled_fields))

    def __len__(self):
        return len(self.data) + len(SessionToken.Meta.non_pickled_fields)
