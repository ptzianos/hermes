import argparse
import logging
import os
import signal
from asyncio import create_task, get_event_loop, Task
from typing import Dict

import toml
from cerberus import Validator

from carbon.ledger.connectors import Stream, get_connector
from carbon.ledger.protocols import get_protocol_parser


validator = Validator({
    'streams': {
        'type': 'list',
        'schema': {
            'type': 'dict', 'schema': {
                'root_address': {'type': 'string'},
                'network': {'type': 'string'},
                'protocol': {'type': 'string'}
            }
        }
    },
    'ads': {
        'type': 'list',
        'schema': {
            'type': 'dict', 'schema': {
                'uuid': {'type': 'string'}
            }
        }
    },
})


def signal_handler() -> None:
    """Kills the event loop."""
    get_event_loop().stop()


async def follow_stream(stream: Stream,
                        log: logging.Logger,
                        stop_at_the_end: bool = False) -> None:
    """Follow a stream."""
    stream_iterator = iter(stream)
    while True:
        try:
            log.debug(f'Fetching next packet for stream with '
                      f'root address {stream.root_address}')
            packet = next(stream_iterator)
            log.debug(f'Fetched packet for stream with root address '
                      f'{stream.root_address}: {packet}')
        except StopIteration:
            if stop_at_the_end:
                return
        except Exception as e:
            log.error(f'There was an error while trying to fetch the next '
                      f'packet: {repr(e)}')


async def explore_stream_backwards(stream: Stream) -> None:
    """Explore a stream backwards from the root address."""
    pass


async def schedule_streams(config_file: str) -> None:
    event_loop = get_event_loop()
    coroutine_registry = dict()  # type: Dict[str, Task]
    log = logging.getLogger('asyncio')
    config = toml.load(open(config_file, 'r'))
    if not validator.validate(config):
        log.error(f'Invalid configuration file: {repr(validator.errors)}')
    for stream_config in config['streams']:
        LedgerConnector = get_connector(stream_config['ledger'])
        ProtocolParser = get_protocol_parser(stream_config['protocol'])
        stream = Stream(
            ledger_connector=LedgerConnector(protocol=ProtocolParser),
            root_address=stream_config['root_address'], logger=log)
        log.info(f'Scheduling coroutines for IOTA stream with root address'
                 f' {stream_config["root_address"]}')
        following_task = follow_stream(stream)
        backwards_task = explore_stream_backwards(stream)


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description='Explore Carbon streams from ledgers.')
    parser.add_argument('config-file', type=str, default='config.example.toml')
    parser.add_argument('no-follow', type=bool, action='store_true',
                        help="Follow a stream but don't wait for it to continue")
    parser.add_argument('stream-window-size', type=int,
                        help='Size of the buffer used to store data samples '
                             'of a stream')
    parser.add_argument('logging-level', type=str, default='INFO',
                        help='Logging level of the application')
    parser.add_argument('show-data', type=str, help='')

    args = parser.parse_args()

    if not os.path.exists(args.config_file):
        print('Config file does not exist')
        exit(1)

    # Configure logging
    logging.getLogger('asyncio').setLevel(args.logging_level.upper())

    # Initialize event loop
    event_loop = get_event_loop()
    event_loop.add_signal_handler(signal.SIGKILL, signal_handler)
    event_loop.add_signal_handler(signal.SIGTERM, signal_handler)

    if args.no_follow:
        event_loop.run_until_complete(schedule_streams(args.config_file))
    else:
        event_loop.run_forever()
