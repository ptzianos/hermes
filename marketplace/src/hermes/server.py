import argparse
import logging
import sys

from flask import Flask
from flask.logging import create_logger

import hermes.config
from hermes.signals import register_signals
from hermes.user.session import HermesSession

app = Flask(__name__)
app.config.from_object(hermes.config)
app.session_interface = HermesSession()
logger = create_logger(app)
log = create_logger(app)
log.setLevel(logging.INFO)
register_signals(app)


# Import all the endpoints
from hermes.views import *


def run_migrations(exit_immediately=False):
    from hermes.db.config import Base
    # TODO: Make this an automatic scan process along with a singleton registry class
    from hermes.user.models import SessionToken, User
    log.info('Running schema migrations')
    Base.metadata.create_all()
    if exit_immediately:
        sys.exit(status=1)


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--dev', action='store_true',
                        help='run the server in development mode')
    parser.add_argument('--run-migrations', action='store_true',
                        help='run database migrations when starting the server')
    parser.add_argument('--run-migrations-and-exitt', action='store_true',
                        help='run database migrations and exit immediately')
    parser.add_argument('--host', type=str, default=None)
    parser.add_argument('--port', type=int, default=None)
    parser.add_argument('--log-to-file', action='store_true', help='Use rotating file logger')

    args = parser.parse_args()

    if args.run_migrations:
        run_migrations()

    app.run(host=args.host, port=args.port, debug=args.dev)
