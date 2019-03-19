import logging

from flask import Flask
from flask.logging import create_logger

import hermes.config
from hermes.signals import register_signals
from hermes.user.session import HermesSession

app = Flask(__name__)
app.config.from_object(hermes.config)
app.session_interface = HermesSession()
log = create_logger(app)
log.setLevel(logging.INFO)
register_signals(app)

from hermes.views import *
