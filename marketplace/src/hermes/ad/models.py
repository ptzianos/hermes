from datetime import datetime
from functools import partial
from uuid import uuid4

from flask import current_app
from sqlalchemy import (Boolean, Column, DateTime, Float,
                        ForeignKey, Integer, Numeric, String)
from sqlalchemy.orm import relationship
from sqlalchemy_utils.types.choice import ChoiceType

from hermes.user.models import User


class Ad(current_app.Base):
    __tablename__ = 'ads'

    PROTOCOL = [
        ('plaintext', 'PLAINTEXT'),
        ('public_bundle', 'PUBLIC_BUNDLE'),
        ('private_bundle', 'PRIVATE_BUNDLE')
    ]

    CURRENCIES = [
        ('miota', 'MIOTA'),
    ]

    id = Column(Integer, primary_key=True)
    uuid = Column(String, unique=True,
                  default=partial(lambda: uuid4().hex))
    owner_id = Column(Integer, ForeignKey('users.id'))
    owner = relationship(User, primaryjoin=owner_id == User.id)
    protocol = Column(ChoiceType(PROTOCOL), nullable=False)
    mobile = Column(Boolean)
    data_type = Column(String)
    data_unit = Column(String)
    location_x = Column(Numeric)
    location_y = Column(Numeric)
    created_on = Column(DateTime, nullable=False, default=datetime.now)
    last_modified_on = Column(DateTime, onupdate=datetime.now)
    last_pinged_on = Column(DateTime)
    rate = Column(Float, default=0.0)
    currency = Column(ChoiceType(CURRENCIES), default='miota')

    def __repr__(self) -> str:
        return ("<Ad(id='{}', owner='{}', data_uuid='{}', location=({}, {}),"
                " type={}, unit={}, mobile={}, protocol={})>"
                .format(self.uuid, self.owner.uuid, '', self.location_x,
                        self.location_y, self.data_type, self.data_unit,
                        self.mobile, self.protocol))

    def __str__(self) -> str:
        return ("<Ad(id='{}', owner='{}', data_uuid='{}', location=({}, {}),"
                " type={}, unit={}, mobile={}, protocol={})>"
                .format(self.uuid, self.owner.uuid, '', self.location_x,
                        self.location_y, self.data_type, self.data_unit,
                        self.mobile, self.protocol))
