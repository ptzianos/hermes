from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base

from hermes.config import *

# TODO: Make this configurable from the environment or the command line
engine_connection = SQL_ALCHEMY_SQLITE

Engine = create_engine(engine_connection, echo=DEBUG_DB)
Base = declarative_base(bind=Engine)
