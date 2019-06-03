from decimal import Decimal
from typing import Iterator

import pytest
import requests
from Crypto.PublicKey.ECC import EccKey

from tests.hermes.user.utils import register_user
from tests.utils import get


@pytest.mark.usefixtures('sqlalchemy_test_session')
def test_create_ad_controller(
    ecdsa_key_pair: Iterator[EccKey]
):
    from hermes.ad.controllers import create_ad

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

    ad_endpoint = '/api/v1/ads/'

    resp = get(ad_endpoint)
    assert resp.status_code == requests.codes.ok
    assert len(resp.json['ads']) == 5

    resp = get(ad_endpoint + '?data_type=float')
    assert resp.status_code == requests.codes.ok
    assert len(resp.json['ads']) == 3

    resp = get(ad_endpoint + '?x=0.5&y=0.5&height=2.0&width=2.0')
    assert resp.status_code == requests.codes.ok
    assert len(resp.json['ads']) == 2
    assert any(map(lambda ad: ad['start_of_stream'] == '0x2', resp.json['ads']))
    assert any(map(lambda ad: ad['start_of_stream'] == '0x3', resp.json['ads']))

    resp = get(ad_endpoint + '?data_type=integer&x=0.5&y=0.5&height=2.0&width=2.0')
    assert resp.status_code == requests.codes.ok
    assert len(list(resp.json['ads'])) == 1
    assert any(map(lambda ad: ad['start_of_stream'] == '0x3', resp.json['ads']))
    assert not any(map(lambda ad: ad['start_of_stream'] == '0x2', resp.json['ads']))
