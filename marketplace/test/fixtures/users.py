import pytest


@pytest.fixture(scope="")
def setup_db():
    pass


@pytest.fixture
def test_public_key():
    pass


@pytest.fixture
def test_email():
    pass


@pytest.fixture
def test_user(test_email):
    pass
