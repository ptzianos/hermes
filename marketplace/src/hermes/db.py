from flask import current_app
from sqlalchemy import create_engine
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import scoped_session, sessionmaker


def init_db():
    current_app.log('Creating engine with uri {database}'
                    .format(database=current_app.config['DATABASE']))
    current_app.Engine = create_engine(current_app.config['DATABASE'],
                                       echo=current_app.config['DEBUG'])
    current_app.Base = declarative_base(bind=current_app.Engine)
    current_app.new_db_session_instance = scoped_session(
        sessionmaker(bind=current_app.Engine))

    # Import all the models so that the database can be initialized.
    # noqa: F401
    from hermes.ad.models import Ad
    from hermes.user.models import (APIToken, AuthenticationToken, BaseToken,
                                    EmailAddress, EmailVerificationToken,
                                    PasswordResetToken, PublicKey,
                                    PublicKeyVerificationRequest, SessionToken,
                                    User)

    if current_app.config.get('RECREATE_DATABASE'):
        current_app.Base.metadata.create_all(current_app.Engine)
