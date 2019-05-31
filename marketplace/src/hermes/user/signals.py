from typing import TYPE_CHECKING
from uuid import uuid4

from Crypto.Hash import SHA3_512
from sqlalchemy import event

from hermes.user.models import APIToken


if TYPE_CHECKING:
    from sqlalchemy.engine.base import Connection
    from sqlalchemy.orm.mapper import Mapper


@event.listens_for(APIToken, 'before_insert')
def setup_api_token_name(mapper: 'Mapper', connection: 'Connection', target: APIToken):
    # This is done because under normal circumstances the token field has not
    # been assigned a value yet.
    target.token = uuid4().hex
    target.name = (SHA3_512.new(data=target.token.encode(encoding='utf-8',
                                                         errors='ignore'))
                   .digest()
                   .hex())[:10]
