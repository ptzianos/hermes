from flask import Flask

app = Flask(__name__)


@app.route('/')
def index():
    return 'Hello World!'


@app.route('/api/v1/user/register')
def register():
    return 'Hello World!'


@app.route('/api/v1/user/deregister')
def deregister():
    return 'Hello World!'


@app.route('/api/v1/user/login')
def login():
    return 'Hello World!'


@app.route('/api/v1/user/logout')
def logout():
    return 'Hello World!'


@app.route('/api/v1/user/?')
def user():
    return 'Hello World!'


@app.route('/api/v1/user/?/token')
def token():
    return 'Hello World!'


@app.route('/api/v1/')
def hello_world():
    return 'Hello World!'


if __name__ == '__main__':
    app.run()
