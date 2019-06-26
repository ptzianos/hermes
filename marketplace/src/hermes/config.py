from datetime import timedelta
from functools import partial

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
RECREATE_DATABASE = False

PUBLIC_KEY_VERIFICATION_TEXT = """
By signing this with your private key you verify that you are the sole rightful owner of the key pair whose
public key has a SHA3-512 digest that starts with {digest}.

Expires on: {expiration_date}
"""

RABBITMQ_USER = "user"
RABBITMQ_PASSWORD = "password"
RABBITMQ_CELERY_VHOST = "celery"
RABBITMQ_AD_VHOST = "vhost"
RABBITMQ_HOST = "localhost"
RABBITMQ_AMQP_PORT = 5672
RABBITMQ_HTTP_PORT = 15672
RABBITMQ_HTTP_ADDR = f"http://{RABBITMQ_HOST}:{RABBITMQ_HTTP_PORT}"
RABBITMQ_HTTP_API = partial("{addr}/api/{endpoint}".format, addr=RABBITMQ_HTTP_ADDR)
