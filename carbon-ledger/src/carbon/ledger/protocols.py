from abc import ABC
from datetime import datetime
from enum import Enum
from logging import Logger
from typing import List, Type, TYPE_CHECKING

if TYPE_CHECKING:
    from carbon.ledger.data import Packet
    from carbon.ledger.ledgers import Block


class Protocol(str, Enum):
    PLAINTEXT = 'PLAINTEXT'


def epoch_to_datetime(ts):
    try:
        return datetime.utcfromtimestamp(int(ts))
    except ValueError:
        return datetime.utcfromtimestamp(int(ts)/1000)


def datetime_to_string(dt):
    return dt.strftime('%Y-%m-%d %H:%M:%S')


class ProtocolParser(ABC):
    class InvalidData(Exception):
        pass

    @staticmethod
    def parse_headers(address: str, raw_data: str, log: Logger) -> 'Block':
        raise NotImplemented()

    @staticmethod
    def parse_data(block: 'Block', log: Logger) -> List['Packet']:
        raise NotImplemented()


class HermesPlaintextParser(ProtocolParser):
    @staticmethod
    def parse_headers(address: str, raw_data: str, log: Logger) -> 'Block':
        from carbon.ledger.connectors import Block
        fields = (raw_data
                  .replace('next_address:', '')
                  .replace('previous_address:', '')
                  .split('::'))  # type: List[str]
        log.debug(f'Header of block with address {address} '
                  f'is : {"::".join(fields[:3])}')
        if len(fields) < 4:
            raise ProtocolParser.InvalidData()
        return Block(address=address,
                     next_link=fields[1],
                     previous_link=fields[2],
                     data={'samples': fields[3:]},
                     metadata={'digest': fields[0]})

    @staticmethod
    def parse_data(block: 'Block', log: Logger) -> None:
        from carbon.ledger.data import Packet
        for sample in block.data['samples']:
            tags, timestamp, data = sample.split(' ')
            tags = tags.split(';')
            block.samples.append(Packet(sample, tags[0], tags[1:],
                                        epoch_to_datetime(timestamp),
                                        data, block))


def get_protocol_parser(protocol_id: str) -> Type[ProtocolParser]:
    protocol = Protocol[protocol_id.upper()]
    if protocol == Protocol.PLAINTEXT:
        return HermesPlaintextParser
