from typing import Any, Callable, Tuple, Union

from flask import (Flask, make_response, redirect, request,
                   Response, session, url_for)
from flask.logging import create_logger

from hermes.server import app as hermes_app
from hermes.exceptions import *
from hermes.user.controllers import (authenticate_user, deregister_user, exit_su,
                                     generate_api_token, register_user, resolve_user,
                                     revoke_token, su, user_details)
from hermes.user.decorators import admin_only, authenticated_only, unauthenticated_only
from hermes.utils import make_json_response


log = create_logger(hermes_app)
ViewResponse = Union[Response, Tuple[str, int], str]


def http_methods(flask_app: Flask):
    """Decorator factory for outputting which API endpoints have been registered."""
    def decorator_factory(http_method: str):
        def rule_factory(rule: str):
            def method_decorator(func: Callable[[Any], Response]) -> Callable[[Any], Response]:
                log.debug('Registering endpoint: {} {}'.format(http_method, rule))
                flask_app.add_url_rule(rule, func.__name__, func, methods=[http_method])
                return func
            return method_decorator
        return rule_factory
    return (decorator_factory('GET'), decorator_factory('PUT'),
            decorator_factory('POST'), decorator_factory('PATCH'),
            decorator_factory('DELETE'))


get, post, put, patch, delete = http_methods(hermes_app)


@get('/')
def index() -> ViewResponse:
    import ipdb
    ipdb.set_trace()
    if session.is_anonymous:
        return redirect(url_for(login.__name__))
    return 'Welcome to the Hermes Marketplace'


@post('/api/v1/user/register')
def register() -> ViewResponse:
    try:
        user = register_user(email=request.form.get('email'),
                             password=request.form.get('password'),
                             name=request.form.get('password'),
                             fullname=request.form.get('password'),
                             public_key=request.form.get('password'))
        return make_json_response(uuid=user.uuid)
    except AlreadyRegistered:
        return make_response('already_registered', 400)


@post('/api/v1/user/deregister')
@unauthenticated_only
def deregister() -> ViewResponse:
    return 'Hello World!'


@post('/api/v1/user/login')
@unauthenticated_only
def login() -> ViewResponse:
    try:
        user = authenticate_user(request.form['username'], request.form['password'])
        session.owner = user
        session.refresh()
        return redirect(url_for(index))
    except (UnknownUser, WrongParameters):
        return make_response('wrong credentials', 400)


@post('/api/v1/user/logout')
@authenticated_only
def logout() -> ViewResponse:
    session.revoke()
    return make_response(200)


@post('/api/v1/user/su/<string:user_id>')
@authenticated_only
@admin_only
def post_su(user_id: str) -> ViewResponse:
    try:
        su(user=session['owner'], user_to_su=user_id, session=session)
        return make_response(200)
    except WrongParameters:
        return make_response(400)
    except ForbiddenAction:
        return make_response(403)


@get('/api/v1/user/')
@authenticated_only
@admin_only
def list_users() -> ViewResponse:
    return 'Hello World!'


@get('/api/v1/user/me')
@authenticated_only
def me() -> ViewResponse:
    return make_json_response(**user_details(requesting_user=session['owner'],
                                             user=session['owner']))


@get('/api/v1/user/<string:user_id>')
@authenticated_only
def get_user_details(user_id: str) -> ViewResponse:
    try:
        return make_json_response(**user_details(requesting_user=session['owner'],
                                                 user=user_id))
    except ForbiddenAction:
        return make_response(403)


@patch('/api/v1/user/<string:user_id>')
@authenticated_only
def patch_user(user_id: str) -> ViewResponse:
    # TODO: implement this
    return 'Hello World!'


@post('/api/v1/user/token')
@authenticated_only
def create_token() -> ViewResponse:
    token = generate_api_token(session['owner'])
    return make_json_response(token=token.token,
                              expiration_date=token.expiry)


@delete('/api/v1/user/token/<string:token_id>')
@authenticated_only
def revoke_token(token_id: str) -> ViewResponse:
    revoke_token(session['owner'], token_id)
    return make_response(200)


@get('/api/v1/ad/')
def list_ads() -> ViewResponse:
    return 'Hello World!'


@post('/api/v1/ad/')
@authenticated_only
def create_ad() -> ViewResponse:
    return 'Hello World!'


@get('/api/v1/ad/<string:ad_id>')
def get_ad(ad_id: str) -> ViewResponse:
    return 'Hello World!'


@patch('/api/v1/ad/<string:ad_id>')
@authenticated_only
def modify_ad(ad_id: str) -> ViewResponse:
    return 'Hello World!'


@delete('/api/v1/ad/<string:ad_id>')
@authenticated_only
def delete_ad(ad_id: str) -> ViewResponse:
    return 'Hello World!'
