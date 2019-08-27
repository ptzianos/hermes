from datetime import datetime
from typing import Any, List, Optional, TYPE_CHECKING

if TYPE_CHECKING:
    from carbon.ledger.ledgers import Block


class Packet:
    """Represents a data sample.

    The encoding of the data sample is done using the Carbon 2.0 protocol.
    TODO: Add Carbon2.0 stuff here
    """

    class InvalidPacket(Exception):
        pass

    def __init__(self, raw: Optional[str] = '', tag: Optional[str] = '', other_tags: Optional[List[str]] = '',
                 timestamp: Optional[datetime] = '', data: Optional[Any] = None, block: Optional[Block] = None,
                 previous_packet: Optional['Packet'] = None, next_packet: Optional['Packet'] = None) -> None:
        self._tag = tag
        self._tags = other_tags
        self._raw = ''
        self._timestamp = timestamp
        self._data = data
        self._raw = raw
        self._block = block
        self._previous = previous_packet
        self._next = next_packet
        if not self._raw and (not self._tag or not self._timestamp or not self._data):
            raise Packet.InvalidPacket()

    def _parse_raw(self) -> None:
        if not self._raw:
            raise Packet.InvalidPacket()
        _tags, self._timestamp, self._data = self._raw.split(' ')
        split_tags = _tags.split(',')
        self._tag = split_tags[0]
        self._tags = split_tags[1:]

    def _to_raw(self) -> None:
        self._raw = f'${self._tag};{self._tags} ${self._timestamp.strftime("%Y-%m-%d %H:%M:%S")} ${self._data}'

    @property
    def tag(self) -> str:
        if not self._tag:
            self._parse_raw()
        return self._tag

    @property
    def tags(self) -> List[str]:
        if not self._tags:
            self._parse_raw()
        return self._tags

    @staticmethod
    def from_raw(raw: str) -> 'Packet':
        return Packet(raw)

    def __str__(self) -> str:
        if not self._raw:
            self._to_raw()
        return self._raw

    def __repr__(self) -> str:
        return self.__str__()

    def __metrics_20__(self) -> str:
        raise NotImplemented()
