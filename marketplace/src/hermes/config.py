from datetime import timedelta

SESSION_DURATION = timedelta(days=1)
API_TOKEN_DURATION = timedelta(days=30)
PASSWORD_RESET_TOKEN_DURATION = timedelta(days=1)
EMAIL_VERIFICATION_TOKEN_DURATION = timedelta(days=3)

SQL_ALCHEMY_SQLITE_TEMPLATE = 'sqlite://{file_path}'
SQL_ALCHEMY_SQLITE_ABS_PATH_TEMPLATE = 'sqlite:///{abs_file_path}'
SQL_ALCHEMY_POSTGRESQL_TEMPLATE = 'postgresql://{username}:{password}@{db_host}:{db_port}/{db_name}'
SQL_ALCHEMY_POSTGRESQL_PSYCOPG_TEMPLATE = 'postgresql+psycopg2://{username}:{password}@{db_host}:{db_port}/{db_name}'

SALT = '2sTOkalOpHhHuNmod5GrGgijXkmkUsJz'

DATABASE = SQL_ALCHEMY_SQLITE_TEMPLATE.format(file_path='dev.db')

DEBUG_DB = False
