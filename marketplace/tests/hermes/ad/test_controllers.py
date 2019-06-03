from decimal import Decimal
from typing import Iterator

import pytest
from Crypto.PublicKey.ECC import EccKey

from hermes.exceptions import *
from tests.hermes.user.utils import register_user


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_create_ad_controller(
    ecdsa_key_pair: Iterator[EccKey]
):
    from hermes.ad.controllers import create_ad

    user, pk, _, _ = register_user(next(ecdsa_key_pair))
    new_ad = create_ad(user, data_type='type', data_unit='unit',
                       start_of_stream_address='0xaeda',
                       longitude=Decimal(1.0),
                       latitude=Decimal(2.0))
    assert new_ad.owner.id == user.id

    with pytest.raises(AttributeError):
        create_ad(None, data_type='type', data_unit='unit',
                  start_of_stream_address='0xaeda', latitude=Decimal(2.0),
                  longitude=Decimal(2.0))

    with pytest.raises(UnknownProtocol):
        create_ad(user, data_type='type', data_unit='unit', protocol='bla',
                  start_of_stream_address='0xaeda', latitude=Decimal(2.0),
                  longitude=Decimal(2.0))

    with pytest.raises(UnknownLocation):
        create_ad(user, data_type='type', data_unit='unit', mobile=False,
                  start_of_stream_address='0xaeda', longitude=None,
                  latitude=Decimal(2.0))

    with pytest.raises(WrongRate):
        create_ad(user, data_type='type', data_unit='unit',
                  start_of_stream_address='0xaeda', rate=Decimal(-1.0),
                  latitude=Decimal(2.0), longitude=Decimal(2.0))

    with pytest.raises(NoStartOfStream):
        create_ad(user, data_type='type', data_unit='unit',
                  latitude=Decimal(2.0), longitude=Decimal(2.0),
                  start_of_stream_address='')


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_ad_queries(
    ecdsa_key_pair: Iterator[EccKey]
):
    from hermes.ad.controllers import AdQuery, create_ad
    user, pk, _, _ = register_user(next(ecdsa_key_pair))
    create_ad(user, data_type='float', data_unit='m/s',
              start_of_stream_address='0x1', longitude=Decimal(0.0),
              latitude=Decimal(0.0))
    create_ad(user, data_type='float', data_unit='celsius',
              start_of_stream_address='0x2', longitude=Decimal(1.0),
              latitude=Decimal(2.0))
    create_ad(user, data_type='integer', data_unit='celsius',
              start_of_stream_address='0x3', longitude=Decimal(1.0),
              latitude=Decimal(1.0))
    create_ad(user, data_type='integer', data_unit='celsius',
              start_of_stream_address='0x4', longitude=Decimal(3.0),
              latitude=Decimal(0.0))
    create_ad(user, data_type='float', data_unit='m^2/s',
              start_of_stream_address='0x5', longitude=Decimal(10.0),
              latitude=Decimal(20.0))

    assert len(list(AdQuery().active().by_data_type('float').resolve())) == 3

    query = (AdQuery()
             .active()
             .by_location(latitude=Decimal(0.5), longitude=Decimal(0.5),
                          height=Decimal(2.0), width=Decimal(2.0))
             .resolve())
    assert len(list(query)) == 2
    assert any(map(lambda ad: ad.start_of_stream == '0x2', query))
    assert any(map(lambda ad: ad.start_of_stream == '0x3', query))

    query_2 = (AdQuery()
               .active()
               .by_data_type('integer')
               .by_location(latitude=Decimal(0.5), longitude=Decimal(0.5),
                            height=Decimal(2.0), width=Decimal(2.0))
               .resolve())
    assert len(list(query_2)) == 1
    assert any(map(lambda ad: ad.start_of_stream == '0x3', query_2))
    assert not any(map(lambda ad: ad.start_of_stream == '0x2', query_2))
