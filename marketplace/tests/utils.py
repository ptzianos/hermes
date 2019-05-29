import base64
from typing import Any, Dict, Optional

from flask import current_app, Response
from flask.testing import FlaskClient


def request_wrapper(method_name: str):
    def do_request(endpoint: str,
                   data: Optional[Dict[str, Any]] = None,
                   api_token: Optional[str] = None,
                   no_cookies: bool = False) -> Response:
        if api_token:
            encoded_token: str = (base64.encodebytes(api_token.encode())
                                  .decode('utf-8')
                                  .strip())
            header = {'Authorization': 'Bearer ' + encoded_token}
        else:
            header = {}
        test_client: FlaskClient = current_app.test_client()
        http_method = getattr(test_client, method_name)
        if data:
            resp: Response = http_method(endpoint, headers=header, data=data)
        else:
            resp: Response = http_method(endpoint, headers=header)
        if no_cookies:
            test_client.delete_cookie('localhost.local',
                                      current_app.session_cookie_name)
        return resp
    return do_request


get = request_wrapper('get')
put = request_wrapper('put')
post = request_wrapper('post')
patch = request_wrapper('patch')
delete = request_wrapper('delete')
