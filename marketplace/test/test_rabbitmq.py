from unittest.mock import MagicMock, patch


@patch('requests.get')
def test_list_rabbitmq_users(http_get: MagicMock,
                             rabbitmq_user_response: MagicMock):
    http_get.return_value = rabbitmq_user_response
    from hermes.rabbtimq.api import list_users
    res = map(lambda user: user['name'], list_users())
    http_get.assert_called_with('http://localhost:15672/api/users',
                                auth=('user', 'password'))
    assert "user" in res
    assert "bla" not in res


@patch('requests.get')
def test_list_rabbitmq_vhosts(http_get: MagicMock,
                              rabbitmq_vhost_response: MagicMock):
    http_get.return_value = rabbitmq_vhost_response
    from hermes.rabbtimq.api import list_vhosts
    res = list_vhosts()
    http_get.assert_called_with('http://localhost:15672/api/vhosts',
                                auth=('user', 'password'))
    assert '/' in res


@patch('requests.get')
def test_ping_rabbitmq(http_get: MagicMock,
                       rabbitmq_overview_response: MagicMock):
    http_get.return_value = rabbitmq_overview_response
    from hermes.rabbtimq.api import ping
    assert ping()

