from hermes.methods import flask_methods

from flask import Flask

app = Flask(__name__)
# app.session_interface = MySessionInterface()
get, post, put, patch, delete = flask_methods(app)


@get('/')
def index():
    return 'Hello World!'


@post('/api/v1/user/register')
def register():
    return 'Hello World!'


@post('/api/v1/user/<string:user_id>/deregister')
def deregister(user_id: str):
    return 'Hello World!'


@post('/api/v1/user/login')
def login():
    return 'Hello World!'


@post('/api/v1/user/logout')
def logout():
    return 'Hello World!'


@post('/api/v1/user/<string:user_id>/sudo')
def sudo(user_id: str):
    return 'Hello World!'


@get('/api/v1/user/')
def list_users():
    return 'Hello World!'


@get('/api/v1/user/me')
def me():
    return 'Hello World!'


@get('/api/v1/user/<string:user_id>')
def user_details(user_id: str):
    return 'Hello World!'


@patch('/api/v1/user/<string:user_id>')
def patch_user(user_id: str):
    return 'Hello World!'


@post('/api/v1/user/<string:user_id>/token')
def token(user_id: str):
    return 'Hello World!'


@delete('/api/v1/user/<string:user_id>/token')
def token(user_id: str):
    return 'Hello World!'


@get('/api/v1/ad/')
def list_ads():
    return 'Hello World!'


@post('/api/v1/ad/')
def create_ad():
    return 'Hello World!'


@get('/api/v1/ad/<string:ad_id>')
def get_ad(ad_id: str):
    return 'Hello World!'


@patch('/api/v1/ad/<string:ad_id>')
def modify_ad(ad_id: str):
    return 'Hello World!'


@delete('/api/v1/ad/<string:ad_id>')
def delete_ad(ad_id: str):
    return 'Hello World!'


if __name__ == '__main__':
    app.run()
