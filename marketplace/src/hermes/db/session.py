from sqlalchemy.orm import sessionmaker

from hermes.db.config import Engine

new_db_session_instance = sessionmaker(bind=Engine)
