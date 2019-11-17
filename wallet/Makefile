GRADLE=./gradlew

.PHONY: tests
tests:
	$(GRADLE) test

%.tests:
	$(GRADLE) $*:test

%.jar:
	$(GRADLE) $*:jar

.PHONY: ternary.build
ternary.build: collections.jar
	mkdir -p ternary/libs
	cp collections/build/libs/collections-1.0.0.jar ternary/libs/
	$(GRADLE) ternary:build

.PHONY: extensions.build
extensions.build: collections.jar
	mkdir -p extensions/libs
	cp ternary/build/libs/ternary-1.0.0.jar extensions/libs/
	$(GRADLE) extensions:build

.PHONY: bip32.build
bip32.build: extensions.jar ternary.jar
	mkdir -p bip32/libs
	cp extensions/build/libs/extensions-1.0.0.jar bip32/libs/
	$(GRADLE) bip32:build

.PHONY: bip39.build
bip39.build: collections.jar extensions.jar bip32.jar
	mkdir -p bip39/libs
	cp collections/build/libs/collections-1.0.0.jar bip39/libs/
	cp extensions/build/libs/extensions-1.0.0jar bip39/libs/
	cp bip32/build/libs/bip32-1.0.0.jar bip39/libs/
	$(GRADLE) bip39:build

.PHONY: bip44.build
bip44.build: collections.jar extensions.jar ternary.jar bip32.jar
	mkdir -p bip44/libs
	cp collections/build/libs/collections-1.0.0.jar bip44/libs/
	cp extensions/build/libs/extensions-1.0.0.jar bip44/libs/
	cp ternary/build/libs/ternary-1.0.0.jar bip44/libs/
	cp bip32/build/libs/bip32-1.0.0.jar bip44/libs/
	$(GRADLE) bip44:build

%.build:
	$(GRADLE) $*:build

%.lint:
	$(GRADLE) $*:ktlintCheck

.PHONY: lint
lint:
	$(GRADLE) ktlintCheck

.PHONY: jars
jars: collections.jar extensions.jar ternary.jar bip32.jar bip39.jar bip44.jar

