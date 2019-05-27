market-install:
	pip3 install -r marketplace/requirementst.txt

market-docker:
	docker build -t hermes-api -f docker/Dockerfile.market ./marketplace

poetry-docker:
	docker build -t poetry -f docker/Dockerfile.poetry .

debug-market:
	cd marketplace && poetry run src/hermes/cli --host=127.0.0.1 --port=8000 run --dev

market-tests:
	cd marketplace && poetry run pytest -s

market-test-cov:
	cd marketplace && poetry run pytest --cov=hermes tests/

market-lint:
	printf "\n===============================\nRunning pylint...\n===============================\n\n"
	pylint marketplace/src/hermes
	printf "\n\n\n===============================\nRunning flake8...\n===============================\n"
	flake8 marketplace/src/hermes

ansible-setup:
	cd ansible && poetry install

ansible-playbook:
	cd ansible && poetry run ansible-playbook -i reclass complete-setup.yml --become

