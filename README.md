# Introduction

This is the main repository of the Hermes Marketplace.

## Installation

### Docker

There is a Docker Compose configuration that you can use to deploy locally most components.

### Local development

In order to run the Marketplace API you need to have Python 3.6 installed in your system and poetry 
(https://poetry.eustace.io/). This Marketplace is built to run on Debian and Ubuntu systems, there is no guarantee that it will
operate normally on any other system. For a database, you can choose to use the SQLite that is preconfigured for the
development environment or you can setup a Postgres instance.

### Ansible

For production deployments there is an Ansible script available, which is under development that will deploy all
the components. The script will deploy the code and a Postgres database.
