#!/usr/bin/env python3
import statistics
from collections import Counter
from datetime import datetime
from string import ascii_lowercase
from typing import Dict, Iterable, List, Optional, Tuple

from iota import Transaction
from iota.api import Iota
from iota.commands.extended.utils import find_transaction_objects


stream_root_addresses = [
    'OCEXMTLLCLOFZXYBDHPBYJIYYM9XTUALRVHGOEU9UUDDJFABFYEOWOUYUVNMCAEYFVIFVAQREUKKWYFBXYSKFQ9USZ',
]

explored_txs = list()
explored_addresses = set()
iota_api = Iota('https://nodes.thetangle.org')
transaction_index = dict()  # type: Dict[str, Transaction]
transaction_samples = {'': ['']}  # type: Dict[str, List[str]]


def epoch_to_datetime(ts):
    try:
        return datetime.utcfromtimestamp(int(ts))
    except ValueError:
        return datetime.utcfromtimestamp(int(ts)/1000)


def datetime_to_string(dt):
    return dt.strftime('%Y-%m-%d %H:%M:%S')


def process_transaction_bundle_data(transactions: Iterable[Transaction]) -> Tuple[str, str, Iterable[Transaction]]:
    next_address = ''
    previous_address = ''
    previous_tx_hash = ''
    for transaction in transactions:
        tx_hash = str(transaction.hash)
        tx_data = transaction.signature_message_fragment.as_string()
        if transaction.current_index == 0:
            fields = (tx_data
                      .replace('next_address:', '')
                      .replace('previous_address:', '')
                      .split('::'))  # type: List[str]
            next_address = fields[1]
            previous_address = fields[2]
            transaction_samples[tx_hash] = fields[3:]  # type: List[str]
        else:
            if tx_data.startswith('::'):
                transaction_samples[tx_hash] += tx_data[2:].split('::')
            elif tx_data.startswith(':') and transaction_samples[previous_tx_hash][-1].endswith(':'):
                _monitoring_data = tx_data[1:].split('::')
                transaction_samples[previous_tx_hash] = (transaction_samples[previous_tx_hash][:-1] +
                                                         [transaction_samples[previous_tx_hash][-1][:-1]])
                transaction_samples[tx_hash] = (transaction_samples[previous_tx_hash][:-1] +
                                                [_monitoring_data[0][1:]] + _monitoring_data[1:])
            else:
                _monitoring_data = tx_data.split('::')
                if len(transaction_samples[previous_tx_hash]) == 0:
                    transaction_samples[tx_hash] = _monitoring_data
                elif len(_monitoring_data) == 1:
                    transaction_samples[tx_hash] = [
                        transaction_samples[previous_tx_hash][-1] + _monitoring_data[0]]
                    transaction_samples[previous_tx_hash] = transaction_samples[previous_tx_hash][:-1]
                else:
                    transaction_samples[tx_hash] = [
                        transaction_samples[previous_tx_hash][-1] + _monitoring_data[0]] + _monitoring_data[1:]
                    transaction_samples[previous_tx_hash] = transaction_samples[previous_tx_hash][:-1]

        previous_tx_hash = tx_hash

    return next_address, previous_address, transactions


def fetch_data_from_address(address: Optional[str]) -> None:
    if not address:
        return
    if address in explored_addresses:
        return
    transactions = sorted(find_transaction_objects(iota_api.adapter, addresses=[address]),
                          key=lambda t: t.current_index)
    print('Retrieving data from address {}'.format(address))
    if not transactions:
        print('No data retrieved from address {}'.format(address))
        return

    next_address, previous_address, tx_hashes = process_transaction_bundle_data(transactions)
    explored_addresses.add(address)
    fetch_data_from_address(previous_address)
    explored_txs.append(tx_hashes)
    fetch_data_from_address(next_address)


if __name__ == '__main__':
    fetch_data_from_address(stream_root_addresses[-1])
    lines = list()
    broadcast_latencies = list()
    attachment_latencies = list()
    bundle_index = 0
    broadcast_latency_list = list()
    attachment_latency_list = list()
    x_labels = list()
    sample_counter = 0
    for lt in explored_txs:
        for transaction in lt:
            for sample_index, sample in enumerate(transaction_samples[str(transaction.hash)]):
                sample_ts = epoch_to_datetime(sample.split(' ')[1])
                line = '({},{},{}) {}'.format(bundle_index, transaction.current_index, sample_index, sample, )
                lines.append(line)
                x_labels.append('({},{})'.format(bundle_index, sample_index))
                line = '{}\t{}\t{}'.format(sample_counter,
                                           (epoch_to_datetime(transaction.timestamp) - sample_ts).seconds,
                                           ascii_lowercase[bundle_index])
                broadcast_latencies.append(line)
                broadcast_latency_list.append((epoch_to_datetime(transaction.timestamp) - sample_ts).seconds)
                line = '{}\t{}\t{}'.format(sample_counter,
                                           (epoch_to_datetime(transaction.attachment_timestamp) - sample_ts).seconds,
                                           ascii_lowercase[bundle_index])
                attachment_latencies.append(line)
                attachment_latency_list.append((epoch_to_datetime(transaction.attachment_timestamp) - sample_ts).seconds)
                sample_counter += 1

        bundle_index += 1

    latency_distribution = Counter(map(lambda t: t[1] - t[0], zip(broadcast_latency_list, attachment_latency_list)))
    latency_distribution_count = sum(latency_distribution.values())
    latency_percentages = [(key, (value/latency_distribution_count)*100) for key, value in latency_distribution.items()]

    del transaction_samples['']
    samples_per_bundle = Counter([len(value) for value in transaction_samples.values()]).items()

    print('\n'.join(lines))
    print('---------------------------------------------------')
    print('---------------------------------------------------')
    print('Broadcast latencies')
    print('---------------------------------------------------')
    print('---------------------------------------------------')
    print('\n'.join(broadcast_latencies))
    print('---------------------------------------------------')
    print('---------------------------------------------------')
    print('Average broadcast latency is: {}'.format(statistics.mean(broadcast_latency_list)))
    print('Standard deviation of broadcast latency is: {}'.format(statistics.stdev(broadcast_latency_list)))
    print('---------------------------------------------------')
    print('---------------------------------------------------')
    print('Attachment latencies')
    print('---------------------------------------------------')
    print('---------------------------------------------------')
    print('\n'.join(attachment_latencies))
    print('---------------------------------------------------')
    print('---------------------------------------------------')
    print('Average attachment latency is: {}'.format(statistics.mean(attachment_latency_list)))
    print('Standard deviation of attachment latency is: {}'.format(statistics.stdev(attachment_latency_list)))
    print('---------------------------------------------------')
    print('---------------------------------------------------')
    print('Latency delays are:\n{}'.format(','.join(map(str, latency_percentages))))
    print('---------------------------------------------------')
    print('---------------------------------------------------')
    print('X Axis Labels:\n{}'.format(','.join(x_labels)))
    print('X Ticks:\n{}'.format(','.join(map(str, range(len(x_labels))))))
    print('Samples per bundle: {}'.format(' '.join(map(str, samples_per_bundle))))
    print('Min broadcast latency is: {}'.format(min(broadcast_latency_list)))
    print('Max broadcast latency is: {}'.format(max(broadcast_latency_list)))
    print('Min attachment latency is: {}'.format(min(attachment_latency_list)))
    print('Max attachment latency is: {}'.format(max(attachment_latency_list)))
