ANSIBLE_POETRY=cd ansible && poetry

streamer-apk-lte-23:
	cd android-client && ./gradlew packageMax23Release && cp ./app/build/outputs/apk/max23/release/app-max23-release-unsigned.apk ../ansible/roles/hermes-node/files/hermes_client_lte_23.apk

streamer-apk-gte-24:
	cd android-client && ./gradlew packageMin24Release && cp ./app/build/outputs/apk/min24/release/app-min24-release-unsigned.apk ../ansible/roles/hermes-node/files/hermes_client_gte_24.apk

streamer-apks: streamer-apk-lte-23 streamer-apk-gte-24

poetry-docker:
	docker build -t poetry -f docker/Dockerfile.poetry .

ansible-setup:
	$(ANSIBLE_POETRY) install

ansible-api-deployment:
	$(ANSIBLE_POETRY) run ansible-playbook -i reclass complete-setup.yml --become

wallet-%:
	cd wallet && make $*

market-%:
	cd markett && make $*

