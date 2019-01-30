from datetime import timedelta

SESSION_DURATION = timedelta(days=1)
API_TOKEN_DURATION = timedelta(days=30)
PASSWORD_RESET_TOKEN_DURATION = timedelta(days=1)
EMAIL_VERIFICATION_TOKEN_DURATION = timedelta(days=3)

SQL_ALCHEMY_SQLITE = 'sqlite:///dev.db'
SQL_ALCHEMY_POSTGRESQL = 'postgresql://{username}:{password}@{db_host}:{db_port}/{db_name}'
SQL_ALCHEMY_POSTGRESQL_PSYCOPG = 'postgresql+psycopg2://{username}:{password}@{db_host}:{db_port}/{db_name}'

SALT = '2sTOkalOpHhHuNmod5GrGgijXkmkUsJz'

DEBUG_DB = True
