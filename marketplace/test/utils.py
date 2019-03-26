from unittest.mock import MagicMock, Mock


def mock_response(status_code: int, json_response):
    res = MagicMock()
    res.status_code = status_code
    res.json = Mock(return_value=json_response)

    return res
