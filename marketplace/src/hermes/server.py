from .methods import 

from flask import Flask

app = Flask(__name__)
# app.session_interface = MySessionInterface()
get, post, put, patch, delete =


@app.route('/', methods=['GET'])
def index():
    return 'Hello World!'


@app.route('/api/v1/user/register', methods=['POST'])
def register():
    return 'Hello World!'


@app.route('/api/v1/user/<string:user_id>/deregister', methods=['POST'])
def deregister(user_id: str):
    return 'Hello World!'


@app.route('/api/v1/user/login', methods=['POST'])
def login():
    return 'Hello World!'


@app.route('/api/v1/user/logout', methods=['POST'])
def logout():
    return 'Hello World!'


@app.route('/api/v1/user/<string:user_id>', methods=['GET'])
def user(user_id: str):
    return 'Hello World!'


@app.route('/api/v1/user/<string:user_id>', methods=['PATCH'])
def user(user_id: str):
    return 'Hello World!'


@app.route('/api/v1/user/<string:user_id>/token', methods=['POST'])
def token(user_id: str):
    return 'Hello World!'


@app.route('/api/v1/user/<string:user_id>/token', methods=['DELETE'])
def token(user_id: str):
    return 'Hello World!'


@app.route('/api/v1/ad/', methods=['GET'])
def hello_world():
    return 'Hello World!'


@app.route('/api/v1/ad/', methods=['POST'])
def hello_world():
    return 'Hello World!'


@app.route('/api/v1/ad/', methods=['DELETE'])
def hello_world():
    return 'Hello World!'


@app.route('/api/v1/ad/{ad_id}', methods=['GET'])
def hello_world():
    return 'Hello World!'


if __name__ == '__main__':
    app.run()
