MARKET_POETRY=cd marketplace && poetry
ANSIBLE_POETRY=cd ansible && poetry

market-install:
	pip3 install -r marketplace/requirementst.txt

market-docker:
	docker build -t hermes-api -f docker/Dockerfile.market ./marketplace

poetry-docker:
	docker build -t poetry -f docker/Dockerfile.poetry .

debug-market:
	$(MARKET_POETRY) run src/hermes/cli --host=127.0.0.1 --port=8000 run --dev

market-tests:
	$(MARKET_POETRY) run pytest -s

market-test-cov:
	$(MARKET_POETRY) run pytest --cov=hermes tests/

market-lint:
	printf "\n===============================\nRunning pylint...\n===============================\n\n"
	pylint marketplace/src/hermes
	printf "\n\n\n===============================\nRunning flake8...\n===============================\n"
	flake8 marketplace/src/hermes

market-requirements:
	$(MARKET_POETRY) run pip freeze | grep -v hermes.git > requirements.txt

ansible-setup:
	$(ANSIBLE_POETRY) install

ansible-api-deployment:
	$(ANSIBLE_POETRY) run ansible-playbook -i reclass complete-setup.yml --become

