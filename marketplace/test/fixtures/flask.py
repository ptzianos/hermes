import logging
import os
import tempfile

import pytest
from flask import Flask
from flask.logging import create_logger
from flask.testing import FlaskClient

from hermes.db import init_db
from hermes.signals import register_signals


@pytest.fixture(scope='session')
def flask_app():
    hermes_test_app = Flask(__name__)
    hermes_test_app.config['TESTING'] = True
    db_fd, hermes_test_app.config['DATABASE'] = tempfile.mkstemp()

    # The database needs to be initialized before the session object
    # because the session object will need the models of the app
    # which in turn require an initialized database.
    with hermes_test_app.app_context():
        init_db()

    from hermes.user.session import HermesSession
    hermes_test_app.session_interface = HermesSession()

    log = create_logger(hermes_test_app)
    log.setLevel(logging.DEBUG)
    register_signals(hermes_test_app)

    hermes_test_app.Base.metadata.create_all()

    yield hermes_test_app.test_client()

    os.close(db_fd)
    os.unlink(hermes_test_app.config['DATABASE'])


@pytest.fixture(scope='session')
def api_client(flask_app: Flask) -> FlaskClient:
    return flask_app.test_client()
