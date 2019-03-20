import logging

from flask import Flask
from flask.logging import create_logger

import hermes.config
from hermes.db import init_db
from hermes.signals import register_signals
from hermes.user.session import HermesSession
from hermes.views import register_views_to_app

app = Flask(__name__)
app.config.from_object(hermes.config)
init_db()
app.session_interface = HermesSession()
log = create_logger(app)
log.setLevel(logging.INFO)
register_signals(app)
register_views_to_app(app)

