from flask import jsonify, Response


def make_json_response(status_code: int = 200, **kwargs) -> Response:
    response = jsonify(**kwargs)  # Response
    response.status_code = status_code
    return response
