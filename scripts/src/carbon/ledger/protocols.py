from abc import ABC
from typing import List, TYPE_CHECKING

if TYPE_CHECKING:
    from carbon.ledger.ledgers import Block


class ProtocolParser(ABC):
    class InvalidData(Exception):
        pass

    @staticmethod
    def parse(address: str, raw_data: str) -> Block:
        raise NotImplemented()


class HermesPlaintextParser(ProtocolParser):
    @staticmethod
    def parse(address: str, raw_data: str) -> Block:
        fields = (raw_data
                  .replace('next_address:', '')
                  .replace('previous_address:', '')
                  .split('::'))  # type: List[str]
        if len(fields) < 4:
            raise ProtocolParser.InvalidData()
        return Block(address=address, next_link=fields[1], previous_link=fields[2],
                     data={'transactions': fields[3:]}, metadata={'digest': fields[0]})
