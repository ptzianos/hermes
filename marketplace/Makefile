MARKET_POETRY=poetry

.PHONY=install docker debug tests test-cov lint requirement

install:
	pip3 install -r marketplace/requirementst.txt

docker:
	docker build -t hermes-api -f docker/Dockerfile.market ./marketplace

debug:
	$(MARKET_POETRY) run src/hermes/cli --host=127.0.0.1 --port=8000 run --dev

tests:
	$(MARKET_POETRY) run pytest -s

test-cov:
	$(MARKET_POETRY) run pytest --cov=hermes tests/

lint:
	printf "\n===============================\nRunning pylint...\n===============================\n\n"
	pylint marketplace/src/hermes
	printf "\n\n\n===============================\nRunning flake8...\n===============================\n"
	flake8 marketplace/src/hermes

requirements:
	$(MARKET_POETRY) run pip freeze | grep -v hermes.git > requirements.txt

