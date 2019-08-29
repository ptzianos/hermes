from abc import ABC
from datetime import datetime
from enum import Enum
from typing import List, Type, TYPE_CHECKING

if TYPE_CHECKING:
    from carbon.ledger.data import Packet
    from carbon.ledger.ledgers import Block


class Protocol(str, Enum):
    PLAINTEXT = 'plaintext'


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
    def parse_headers(address: str, raw_data: str) -> Block:
        raise NotImplemented()

    @staticmethod
    def parse_data(block: Block) -> List[Packet]:
        raise NotImplemented()


class HermesPlaintextParser(ProtocolParser):
    @staticmethod
    def parse_headers(address: str, raw_data: str) -> Block:
        fields = (raw_data
                  .replace('next_address:', '')
                  .replace('previous_address:', '')
                  .split('::'))  # type: List[str]
        if len(fields) < 4:
            raise ProtocolParser.InvalidData()
        return Block(address=address, next_link=fields[1], previous_link=fields[2],
                     data={'samples': fields[3:]}, metadata={'digest': fields[0]})

    @staticmethod
    def parse_data(block: Block) -> None:
        for sample in block.data['samples']:
            tags, timestamp, data = sample.split(' ')
            tags = tags.split(';')
            block.samples.append(Packet(sample, tags[0], tags[1:], epoch_to_datetime(timestamp), data, block))


def get_protocol_parser(protocol: str) -> Type[ProtocolParser]:
    protocol = Protocol[protocol.lower()]
    if protocol == Protocol.PLAINTEXT:
        return HermesPlaintextParser
