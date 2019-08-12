#!/usr/bin/env python3
import csv
import statistics
from collections import Counter
from datetime import datetime
from itertools import chain


data_samples = list()
sample_db = dict()
metadata_db = dict()


latex_colors = [
    'Apricot',
    'Aquamarine',
    'Blue',
    'BlueGreen',
    'BlueViolet',
    'Brown',
    'CadetBlue',
    'Cerulean',
    'CornflowerBlue',
    'Cyan',
    'Dandelion',
    'DarkOrchid',
    'Emerald',
    'ForestGreen',
    'Fuchsia',
    'Goldenrod',
    'Gray',
    'Green',
    'GreenYellow',
    'JungleGreen',
    'Lavender',
    'LimeGreen',
    'Magenta',
    'Mahogany',
    'Maroon',
    'Melon',
    'MidnightBlue',
    'Mulberry',
    'NavyBlue',
    'OliveGreen',
    'Orange',
    'OrangeRed',
    'Orchid',
    'Peach',
    'Periwinkle',
    'PineGreen',
    'Plum',
    'ProcessBlue',
    'Purple',
    'RawSienna',
    'Red',
    'RedOrange',
    'RedViolet',
    'Rhodamine',
    'RoyalBlue',
    'RoyalPurple',
    'RubineRed',
    'Salmon',
    'SeaGreen',
    'Sepia',
    'SkyBlue',
    'SpringGreen',
    'Tan',
    'TealBlue',
    'Thistle',
    'Turquoise',
    'Violet',
    'VioletRed',
    'White',
    'WildStrawberry',
    'Yellow',
    'YellowGreen',
    'YellowOrange',
]

categories = [color.lower() for color in latex_colors]
category_definition = [(color.lower() + '={color=' + color + ', draw=' + color + '}') for color in latex_colors]


def epoch_to_datetime(ts):
    try:
        return datetime.utcfromtimestamp(int(ts))
    except ValueError:
        return datetime.utcfromtimestamp(int(ts)/1000)


def datetime_to_string(dt):
    return dt.strftime('%Y-%m-%d %H:%M:%S')


def append(d, key, value):
    _list = d.get(key, list())
    _list.append(value)
    return _list


def parse_csv(csv_file_name: str = 'iota-data.csv'):
    counter = 0
    with open(csv_file_name) as csv_file:
        csv_reader = csv.reader(csv_file)
        for row in csv_reader:
            if counter != 0:
                data_samples.append(row)
                user_id = row[0].split('.')[0]
                sample_db[user_id] = append(sample_db, user_id, {
                    'collection': epoch_to_datetime(row[5]),
                    'broadcast': epoch_to_datetime(row[6]),
                    'attachment': epoch_to_datetime(row[7].replace('[', '').replace(']', '').split(',')[0]),
                    'address': row[2],
                })
            counter += 1

    for key, value in sample_db.items():
        sample_db[key] = sorted(value, key=lambda d: d['collection'])


def extract_metadata():
    index = 0
    for key, samples in sample_db.items():
        counter = 0
        for sample in samples:
            address = sample['address']
            metadata_db[key] = append(metadata_db, key, {
                'index': index,
                'broadcast_latency': (sample['broadcast'] - sample['collection']).seconds,
                'attachment_latency': (sample['attachment'] - sample['collection']).seconds,
                'address': address,
            })
            index += 1
            counter += 1
            if counter == 10:
                break


def extract_latencies(latency_type):
    sample_index = -1
    category_index = -1
    address = ''
    temp_broadcast_latencies = list()
    indices = set()
    for key in metadata_db.keys():
        bundle_index = -1
        index = -1
        category_index += 1
        for sample in metadata_db[key]:
            bundle_index += 0 if sample['address'] == address else 1
            sample_index = 0 if sample['address'] != address else (sample_index + 1)
            address = sample['address']
            index += 1
            indices.add((bundle_index, sample_index))
            temp_broadcast_latencies.append((
                (bundle_index, sample_index),
                '{}\t{}\t{}\t{}\t{}'.format(
                    sample[latency_type], categories[category_index], key,
                    sample['address'][:10], sample['index']
                )
            ))

    indices = sorted(indices)
    latencies = list()
    for broadcast_sample in temp_broadcast_latencies:
        composite_index, line = broadcast_sample
        latencies.append('{}\t{}\t{}'.format(indices.index(composite_index), line, composite_index))

    return indices, latencies


def get_statistics():
    transaction_hash_lists = list()
    for sample_list in sample_db.values():
        transaction_hash_lists.append(list(map(lambda s: s['address'], sample_list)))
    sample_counter = Counter(Counter(chain(*transaction_hash_lists)).values())
    broadcast_latency_lists = list()
    for sample_list in metadata_db.values():
        broadcast_latency_lists.append(list(map(lambda s: s['broadcast_latency'], sample_list)))
    max_broadcast_latency = max(chain.from_iterable(broadcast_latency_lists))
    min_broadcast_latency = min(chain.from_iterable(broadcast_latency_lists))
    avg_broadcast_latency = statistics.mean(chain.from_iterable(broadcast_latency_lists))
    stdev_broadcast_latency = statistics.stdev(chain.from_iterable(broadcast_latency_lists))
    attachment_latency_lists = list()
    for sample_list in metadata_db.values():
        attachment_latency_lists.append(list(map(lambda s: s['attachment_latency'], sample_list)))
    max_attachment_latency = max(chain.from_iterable(attachment_latency_lists))
    min_attachment_latency = min(chain.from_iterable(attachment_latency_lists))
    avg_attachment_latency = statistics.mean(chain.from_iterable(attachment_latency_lists))
    stdev_attachment_latency = statistics.stdev(chain.from_iterable(attachment_latency_lists))

    return (sample_counter, max_broadcast_latency, min_broadcast_latency,
            avg_broadcast_latency, stdev_broadcast_latency,
            max_attachment_latency, min_attachment_latency,
            avg_attachment_latency, stdev_attachment_latency)


if __name__ == '__main__':
    parse_csv()
    extract_metadata()
    broadcast_x_axis_indices, broadcast_latencies = extract_latencies('broadcast_latency')
    attachment_x_axis_indices, attachment_latencies = extract_latencies('attachment_latency')

    print('---------------------------------------------------')
    print('Color categories')
    print('---------------------------------------------------')
    print(',\n'.join(category_definition))
    # print('---------------------------------------------------')
    # print('Sample Metadata')
    # print('---------------------------------------------------')
    # pprint(metadata_db)
    print('---------------------------------------------------')
    print('Broadcast X Axis Indices')
    print('---------------------------------------------------')
    print(','.join(map(lambda t: '({},{})'.format(t[0], t[1]), broadcast_x_axis_indices)))
    print('---------------------------------------------------')
    print('Attachment X Axis Indices')
    print('---------------------------------------------------')
    print(','.join(map(lambda t: '({},{})'.format(t[0], t[1]), attachment_x_axis_indices)))
    print('---------------------------------------------------')
    print('Broadcast latencies')
    print('---------------------------------------------------')
    print('\n'.join(broadcast_latencies))
    print('---------------------------------------------------')
    print('Attachment latencies')
    print('---------------------------------------------------')
    print('\n'.join(attachment_latencies))
    print('---------------------------------------------------')
    print('Number of users processed')
    print('---------------------------------------------------')
    print(len(sample_db.keys()))
    print('---------------------------------------------------')
    print('Sample number distribution')
    print('---------------------------------------------------')
    (sample_counter, max_broadcast_latency, min_broadcast_latency, avg_broadcast_latency, stdev_broadcast_latency,
     max_attachment_latency, min_attachment_latency, avg_attachment_latency, stdev_attachment_latency) = get_statistics()
    print(f'Sample distribution: {sample_counter}')
    print(f'Max broadcast latency: {max_broadcast_latency}')
    print(f'Min broadcast latency: {min_broadcast_latency}')
    print(f'Avg broadcast latency: {avg_broadcast_latency}')
    print(f'Stdev broadcast latency: {stdev_broadcast_latency}')
    print(f'Max attachment latency: {max_attachment_latency}')
    print(f'Min attachment latency: {min_attachment_latency}')
    print(f'Avg attachment latency: {avg_attachment_latency}')
    print(f'Stdev attachment latency: {stdev_attachment_latency}')
