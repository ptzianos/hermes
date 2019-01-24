install-market:
	pip3 install -r marketplace/requirementst.txt

market-docker:
	docker build -t hermes-api -f marketplace/Dockerfile ./marketplace

market-tests:
	nosetests tests

poetry-docker:
	docker build -t poetry -f docker/Dockerfile.poetry .

debug-market:
	cd marketplace && poetry run python3 src/hermes/server.py --host=127.0.0.1 --port=8000 --dev --run-migrations
