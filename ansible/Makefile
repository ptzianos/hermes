ANSIBLE_POETRY=poetry
EXTRA_ARGS=

ansible-setup:
	$(ANSIBLE_POETRY) install

deployment:
	$(ANSIBLE_POETRY) run ansible-playbook -i reclass complete-setup.yml --become $(EXTRA_ARGS)

api-deployment: EXTRA_ARGS="--tags=api"
api-deployment: deployment

rabbitmq-deployment: EXTRA_ARGS="--tags=rabbitmq"
rabbitmq-deployment: deployment

common-deployment: EXTRA_ARGS="--tags=common"
common-deployment: deployment

