# from test.fixtures.flask import *
from test.fixtures.users import *

import logging
import os
import tempfile

import pytest
from flask import Flask
from flask.logging import create_logger

from hermes import config
from hermes.db import init_db
from hermes.signals import register_signals


@pytest.fixture(scope="session")
def flask_app() -> Flask:
    """Bootstraps a flask application and initializes all the necessary infrastructure for it to work.

    It registers all the signals and views of the application and sets up the Session mechanism.
    """
    hermes_test_app = Flask(__name__)
    hermes_test_app.config['TESTING'] = True
    db_fd, db_path = tempfile.mkstemp()
    hermes_test_app.config['DATABASE_FILE_PATH'] = db_path
    hermes_test_app.config['DATABASE'] = config.SQL_ALCHEMY_SQLITE_ABS_PATH_TEMPLATE.format(abs_file_path=db_path)

    # The database needs to be initialized before the session object
    # because the session object will need the models of the app
    # which in turn require an initialized database.
    with hermes_test_app.app_context():
        init_db()

        from hermes.user.session import HermesSession
        hermes_test_app.session_interface = HermesSession()

        register_signals(hermes_test_app)

        hermes_test_app.Base.metadata.create_all()

        from hermes.views import register_views_to_app
        register_views_to_app(hermes_test_app)

    log = create_logger(hermes_test_app)
    log.setLevel(logging.DEBUG)

    yield hermes_test_app

    os.close(db_fd)
    os.unlink(hermes_test_app.config['DATABASE_FILE_PATH'])
