FROM poetry:latest

COPY . /hermes
WORKDIR /hermes

ENV FLASK_APP=server.py
ENV FLASK_ENV=development

RUN poetry install

ENTRYPOINT poetry run "flask run --host 0.0.0.0 --port 8000"
