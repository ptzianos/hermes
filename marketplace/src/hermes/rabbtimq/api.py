from typing import Dict, List

import requests

from hermes.config import RABBITMQ_HTTP_API, RABBITMQ_PASSWORD, RABBITMQ_USER


def list_vhosts() -> List[str]:
    """Retrieves a list of all the VHosts available in RabbitMQ."""
    res = requests.get(RABBITMQ_HTTP_API(endpoint='vhosts'),
                       auth=(RABBITMQ_USER, RABBITMQ_PASSWORD))
    if res.status_code != requests.codes.ok:
        return []
    else:
        return list(map(lambda vhost: vhost['name'], res.json()))


def add_vhost(name: str) -> bool:
    """Adds a new VHost to RabbitMQ.

    If the VHost already exists then it won't throw an error, it will still
    return True.
    """
    endpoint = f"vhosts/{name}"
    res = requests.put(RABBITMQ_HTTP_API(endpoint=endpoint),
                       auth=(RABBITMQ_USER, RABBITMQ_PASSWORD))
    return res.status_code in [requests.codes.created,
                               requests.codes.no_content]


def list_users() -> List[Dict[str, str]]:
    """Retrieve a list of all the available users.

    The list that is returned contains the name of the user and the tags of the
    user as a string of a comma-separated list.
    """
    res = requests.get(RABBITMQ_HTTP_API(endpoint='users'),
                       auth=(RABBITMQ_USER, RABBITMQ_PASSWORD))
    if res.status_code != requests.codes.ok:
        return []
    else:
        return list(map(lambda user: {'name': user['name'],
                                      'tags': user['tags']},
                        res.json()))


def add_user(name: str, password: str, tags: List[str]) -> bool:
    """Adds a new user to RabbitMQ.

    If the user already exists then it won't throw an error, it will still
    return True.
    """
    endpoint = f"users/{name}"
    res = requests.put(RABBITMQ_HTTP_API(endpoint=endpoint),
                       auth=(RABBITMQ_USER, RABBITMQ_PASSWORD),
                       data={"password": password, "tags": ",".join(tags)})
    return res.status_code in [requests.codes.created,
                               requests.codes.no_content]


def ping() -> bool:
    """Returns True or False if RabbitMQ is reachable."""
    res = requests.get(RABBITMQ_HTTP_API(endpoint='overview'),
                       auth=(RABBITMQ_USER, RABBITMQ_PASSWORD))
    return res.status_code == requests.codes.ok
