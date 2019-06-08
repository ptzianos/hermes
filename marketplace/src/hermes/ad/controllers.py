from datetime import datetime
from decimal import Decimal
from typing import Any, Dict, Iterable, Optional, TYPE_CHECKING

from flask import g

from hermes.exceptions import *
from hermes.user.controllers import resolve_user, UserLikeObj

if TYPE_CHECKING:
    from sqlalchemy.orm import Query

    from hermes.ad.models import Ad
    from hermes.user.models import User


def resolve_ad(ad_id: str) -> Optional['Ad']:
    return (g.db_session.query(Ad).filter(Ad.uuid == ad_id).first()
            if ad_id else None)


def create_ad(owner: 'User', data_type: str, data_unit: str,
              start_of_stream_address: str,
              longitude: Optional[Decimal], latitude: Optional[Decimal],
              protocol: str = 'plaintext', mobile: bool = True,
              rate: Decimal = 0.0, currency: str = 'miota') -> 'Ad':
    """Creates a new Ad instance."""
    from hermes.ad.models import Ad
    if protocol not in [p[0] for p in Ad.PROTOCOL]:
        raise UnknownProtocol()
    if not mobile and None in [longitude, latitude]:
        raise UnknownLocation()
    if rate < Decimal(0.0):
        raise WrongRate()
    if not start_of_stream_address:
        raise NoStartOfStream()
    ad = Ad(owner_id=owner.id, mobile=mobile, data_type=data_type,
            data_unit=data_unit, protocol=protocol, currency=currency,
            longitude=longitude, latitude=latitude, rate=rate,
            start_of_stream=start_of_stream_address)
    g.db_session.add(ad)
    g.db_session.commit()

    return ad


class AdQuery:
    def __init__(self) -> None:
        self.query: 'Query' = None

    def active(self, active: bool = True) -> 'AdQuery':
        if not self.query:
            self.query = g.db_session.query(Ad)
        self.query = self.query.filter(Ad.inactive != active)
        return self

    def inactive(self) -> 'AdQuery':
        if not self.query:
            self.query = g.db_session.query(Ad)
        self.query = self.query.filter(Ad.inactive == True)
        return self

    def by_location(self, latitude: Decimal, longitude: Decimal,
                    width: Decimal, height: Decimal) -> 'AdQuery':
        from hermes.ad.models import Ad

        if not self.query:
            self.active()
        if None in [latitude, longitude, width, height]:
            raise WrongLocationParameters()
        lower_left_x = latitude
        lower_left_y = longitude
        upper_right_x = lower_left_x + width
        upper_right_y = lower_left_y + height
        self.query = self.query.filter(Ad.latitude >= lower_left_y,
                                       Ad.latitude <= upper_right_y,
                                       Ad.longitude >= lower_left_x,
                                       Ad.longitude <= upper_right_x)
        return self

    def by_data_type(self, data_type: str) -> 'AdQuery':
        from hermes.ad.models import Ad

        if not self.query:
            self.active()
        self.query = self.query.filter(Ad.data_type == data_type)
        return self

    def resolve(self) -> Iterable['Ad']:
        if not self.query:
            self.active()
        return self.query

    def to_json(self) -> Iterable[Dict[str, Any]]:
        return list(map(ad_to_json, self.query))


def ad_to_json(ad: 'Ad') -> Dict[str, Any]:
    return {
        'uuid': ad.uuid,
        'owner_id': ad.owner_id,
        'inactive': ad.inactive,
        'protocol': ad.protocol.code,
        'currency': ad.currency.code,
        'mobile': ad.mobile,
        'data_type': ad.data_type,
        'data_unit': ad.data_unit,
        'rate': str(ad.rate),
        'latitude': str(ad.latitude) if ad.latitude else '',
        'longitude': str(ad.longitude) if ad.longitude else '',
        'start_of_stream': ad.start_of_stream,
        'created_on': ad.created_on,
        'last_modified_on': ad.last_modified_on,
        'last_pinged_on': ad.last_pinged_on,
    }


def ping_ad(ad_id: str, requesting_user: 'UserLikeObj'):
    from hermes.ad.models import Ad

    ad: Ad = g.db_session.query(Ad).filter_by(uuid=ad_id).first()
    if not ad:
        raise UnknownAd()
    user = resolve_user(requesting_user)
    if not user:
        raise UnknownUser()
    if user.uuid != ad.owner.uuid and not user.admin:
        raise ForbiddenAction()
    ad.last_pinged_on = datetime.utcnow()


def delete_ad(ad_id: str, requesting_user: 'UserLikeObj'):
    from hermes.ad.models import Ad

    ad: Ad = g.db_session.query(Ad).filter_by(uuid=ad_id).first()
    if not ad:
        raise UnknownAd()
    user = resolve_user(requesting_user)
    if not user:
        raise UnknownUser()
    if user.uuid != ad.owner.uuid and not user.admin:
        raise ForbiddenAction()
    if ad.inactive:
        raise AlreadyInactiveAd()
    ad.inactive = True
    ad.deactivation_data = datetime.utcnow()
