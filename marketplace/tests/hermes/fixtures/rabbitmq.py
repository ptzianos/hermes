import pytest

from tests.hermes.utils import mock_response


sample_overview_response = {
    "management_version": "3.7.13",
    "rates_mode": "basic",
    "rabbitmq_version": "3.7.13",
    "cluster_name": "rabbit@a6c452ffdd98",
    "erlang_version": "21.3.2",
    "listeners": [
        {
            "node": "rabbit@a6c452ffdd98",
            "protocol": "amqp",
            "ip_address": "::",
            "port": 5672,
            "socket_opts":
                {
                    "backlog": 128,
                    "nodelay": True,
                    "linger": [True, 0],
                    "exit_on_close": False
                }
        },
        {
            "node": "rabbit@a6c452ffdd98",
            "protocol": "clustering",
            "ip_address": "::",
            "port": 25672,
            "socket_opts": []
        },
        {
            "node": "rabbit@a6c452ffdd98",
            "protocol": "http",
            "ip_address": "::",
            "port": 15672,
            "socket_opts":
                {
                    "port": 15672,
                    "ssl": False
                }
        }
    ],
    "contexts": [
        {
            "ssl_opts": [],
            "node": "rabbit@a6c452ffdd98",
            "description": "RabbitMQ Management",
            "path": "/",
            "port":"15672",
            "ssl":"false"
        }
    ]
}

sample_user_response = [
    {
        "name": "user",
        "password_hash": "rtWEQt5EYsIdRLyMqFKXQpshTQCw1NPwbIjvCsTiYr2Tygk3",
        "hashing_algorithm": "rabbit_password_hashing_sha256",
        "tags": "administrator"
    }
]

sample_vhost_response = [
    {
        "cluster_state": {"rabbit@a6c452ffdd98": "running"},
        "name": "/",
        "tracing": False,
    },
    {
        "cluster_state": {"rabbit@a6c452ffdd98": "running"},
        "name": "bla",
        "tracing": False,
    }
]


@pytest.fixture
def rabbitmq_overview_response():
    return mock_response(200, sample_overview_response)


@pytest.fixture
def rabbitmq_user_response():
    return mock_response(200, sample_user_response)


@pytest.fixture
def rabbitmq_vhost_response():
    return mock_response(200, sample_vhost_response)
