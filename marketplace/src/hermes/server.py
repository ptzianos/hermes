import logging
import os

from flask import Flask
from flask.logging import create_logger

import hermes.config
from hermes.db import init_db
from hermes.signals import register_signals
from hermes.user.session import HermesSession
from hermes.views import register_views_to_app

app = Flask(__name__)

# Default configuration
app.config.from_object(hermes.config)
# Configuration from the environment
if os.environ.get('HERMES_SETTINGS'):
    app.config.from_envvar('HERMES_SETTINGS')

app.session_interface = HermesSession()
log = create_logger(app)
log.setLevel(logging.INFO)
register_signals(app)
register_views_to_app(app)

with app.app_context():
    init_db()
