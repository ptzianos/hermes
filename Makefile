install-market:
	pip3 install -r marketplace/requirementst.txt

market-docker:
	docker build -t hermes-api -f marketplace/Dockerfile ./marketplace

poetry-docker:
	docker build -t poetry -f docker/Dockerfile.poetry .

debug-market:
	cd marketplace && poetry run src/hermes/cli --host=127.0.0.1 --port=8000 run --dev

test-market:
	cd marketplace && poetry run pytest -s

test-market:
	cd marketplace && poetry run pytest -s

test-cov-market:
	cd marketplace && poetry run pytest --cov=hermes test/

lint-market:
	printf "\n===============================\nRunning pylint...\n===============================\n\n"
	pylint marketplace/src/hermes
	printf "\n\n\n===============================\nRunning flake8...\n===============================\n"
	flake8 marketplace/src/hermes
