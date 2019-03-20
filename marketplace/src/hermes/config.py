from datetime import timedelta

SESSION_DURATION = timedelta(days=1)
API_TOKEN_DURATION = timedelta(days=30)
PASSWORD_RESET_TOKEN_DURATION = timedelta(days=1)
EMAIL_VERIFICATION_TOKEN_DURATION = timedelta(days=3)
PUBLIC_KEY_VERIFICATION_REQUEST_DURATION = timedelta(days=1)

SQL_ALCHEMY_SQLITE_TEMPLATE = 'sqlite://{file_path}'
SQL_ALCHEMY_SQLITE_ABS_PATH_TEMPLATE = 'sqlite:///{abs_file_path}'
SQL_ALCHEMY_POSTGRESQL_TEMPLATE = 'postgresql://{username}:{password}@{db_host}:{db_port}/{db_name}'
SQL_ALCHEMY_POSTGRESQL_PSYCOPG_TEMPLATE = 'postgresql+psycopg2://{username}:{password}@{db_host}:{db_port}/{db_name}'

SALT = '2sTOkalOpHhHuNmod5GrGgijXkmkUsJz'

DATABASE = SQL_ALCHEMY_SQLITE_TEMPLATE.format(file_path='dev.db')

DEBUG_DB = False

PUBLIC_KEY_VERIFICATION_TEXT = """
By signing this text with your private key you verify that you are the sole rightful owner of the key pair whose
public key has a SHA3-512 digest equal to {digest}.
This makes you legally responsible for all actions performed on the platform and liable for any malicious
activities that might occur.
If you have not yourself requested this key to be verified, please do not sign this document.

Expires on: {expiration_date}
"""
