install-market:
	pip3 install -r marketplace/requirementst.txt

market-docker:
	docker build -t hermes-api -f marketplace/Dockerfile ./marketplace

market-tests:
	nosetests tests

poetry-docker:
	docker build -t poetry -f docker/Dockerfile.poetry .
