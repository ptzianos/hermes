# Hermes Marketplace API Server

## Dependencies and Installation

This project is built for Python 3.7 and meant to run primarily on Linux
servers. Any other system and configuration will not be guaranteed to be
supported. The build tool is Poetry and for running an instance locally
you need to have Docker and Docker Compose installed.

## Production Instance

For deployments in production there are Ansible roles and playbooks in the
`./ansible` directory that can be used to deploy it.

### Dependencies

- Ansible
- Python >= 2.7
- A server with 1 CPU and > 2GB RAM

## Development Instance

In order to spin up a development instance you can use the
`./marketplace/docker-compose.yml` configuration. This will build and run a
container with the API Server and a container with a PostgreSQL instance.


## API Documentation

TODO!

If you want to explore the API of the marketplace there is an OpenAPI doc
available.