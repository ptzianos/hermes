from abc import ABC
from enum import Enum
from logging import Logger
from typing import Any, Dict, Iterable, Iterator, List, Optional, Tuple, Type

from carbon.ledger.data import Packet
from carbon.ledger.protocols import ProtocolParser


class Network(str, Enum):
    IOTA = 'IOTA'


class Block:
    """Represents a block posted on a ledger that contains one or more data packets."""

    def __init__(self, address: str, next_link: str, previous_link: str, data: Dict[str, Any],
                 metadata: Dict[str, Any]) -> None:
        self.data = data
        self.address = address
        self.next_link = next_link
        self.previous_link = previous_link
        self.metadata = metadata
        self.samples = list()


class LedgerConnector(ABC):
    def __init__(self, protocol: ProtocolParser):
        self._protocol = protocol

    def fetch(self, address: str) -> Tuple[Block, List[Packet]]:
        raise NotImplemented()


class IOTAConnector(LedgerConnector):
    class InvalidAddress(Exception):
        pass

    class NoDataFetched(Exception):
        pass

    def __init__(self, node_address: str = 'https://nodes.thetangle.org', *args, **kwargs):
        super().__init__(*args,  **kwargs)
        from iota.api import Iota
        self._iota_api = Iota(node_address)

    def fetch(self, address: str) -> Block:
        """Fetches a block from IOTA with an address.

        Since IOTA does not support exactly blocks, what is fetched is all the transactions
        of an address which are then sorted based on their timestamps and then concatenated
        in one block.

        TODO: Add digest checks.
        """
        if not address:
            raise IOTAConnector.InvalidAddress()
        from iota.commands.extended.utils import find_transaction_objects
        transactions = sorted(find_transaction_objects(self._iota_api.adapter, addresses=[address]),
                              key=lambda t: t.timestamp)
        if not transactions:
            raise IOTAConnector.NoDataFetched()

        block = self._protocol.parse_headers(
            address=address, raw_data=''.join(map(lambda t: t.signature_message_fragment, transactions)))
        return block


class Stream(Iterable):
    """Stream of blocks that has been posted to the storage backend.

    The length of the stream is the currently known length, It could change in the future.
    The iterator of the stream provides only a partial view of it based on the latest data
    fetched by the ledger. It is not thread-safe and it should be used with caution.
    """

    class LazyIterator:
        def __init__(self, stream: 'Stream', reverse_order: bool = False) -> None:
            self._stream = stream
            self._next_address = stream.latest_address if reverse_order else stream.root_address
            self._reversed = reverse_order

        def __next__(self) -> Block:
            if not self._next_address:
                raise StopIteration()

            # Return the block immediately if it's already fetched
            block = self._stream._address_index[self._next_address]
            if not block:
                # Try to fetch the block from the ledger
                self._stream._fetch(self._next_address, not self._reversed)

            # Check again if block has been fetched
            block = self._stream._address_index[self._next_address]
            if not block:
                raise StopIteration()

            self._next_address = block.previous_link if self._reversed else block.next_link
            return block

        def __iter__(self):
            return self

    class LazyDataIterator:
        def __init__(self, stream: 'Stream', reverse_order: bool = False) -> None:
            self._iter = reversed(stream) if reverse_order else iter(stream)
            self._reversed = reverse_order
            self._data_iter = None  # type: Optional[Iterator[Packet]]

        def __next__(self) -> Packet:
            for block in self._iter:
                if self._reversed:
                    for packet in reversed(block.data):
                        yield packet
                else:
                    for packet in block.data:
                        yield packet

        def __iter__(self) -> Iterator[Packet]:
            return self

    def __init__(self, ledger_connector: LedgerConnector, root_address: str, logger: Logger):
        self._connector = ledger_connector
        self.root_address = root_address
        self.latest_address = root_address
        self._address_index = dict(())  # type: Dict[str, Block]
        self._logger = logger

    def _fetch(self, address: str, latest: bool = False) -> None:
        """Fetch a block from the ledger and update the state of the stream."""
        try:
            # Fetch block from the ledger
            block, data_samples = self._connector.fetch(address)
        except Exception as e:
            self._logger.error(
                msg=f'Could not fetch block with address {address}. {repr(e)}')
            return
        self._address_index[address] = block
        # Add block to the registry
        self._logger.info(f'Fetched block with address {address}')

    def __iter__(self) -> Iterator[Block]:
        return Stream.LazyIterator(self)

    def __reversed__(self):
        return Stream.LazyIterator(self, reverse_order=True)

    def __len__(self) -> int:
        return len(self._address_index)

    def __getitem__(self, item: int):
        # TODO: Fix me
        return self._blocks[item]

    @property
    def data(self) -> Iterator[Packet]:
        return Stream.LazyDataIterator(self, reverse_order=False)


def get_connector(ledger: str) -> Type[LedgerConnector]:
    network = Network[ledger.upper()]
    if network == Network.IOTA:
        return IOTAConnector
