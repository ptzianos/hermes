from functools import partial
from typing import Callable, Tuple, Union

from flask import (Flask, jsonify, make_response, redirect, request,
                   Response, session, url_for)
from flask.logging import create_logger

import hermes.server
from hermes.exceptions import *
from hermes.user.controllers import (authenticate_user, deregister_user, exit_su,
                                     generate_api_token, register_user, resolve_user,
                                     revoke_token, su, user_details)
from hermes.user.decorators import admin_only, authenticated_only, unauthenticated_only


log = create_logger(hermes.server.app)
ViewResponse = Union[Response, Tuple[str, int], str]


def http_methods(flask_app: Flask):
    """Decorator factory for outputting which API endpoints have been registered."""
    def method_decorator(http_method: str) -> Callable:
        def anon(rule, route, methods):
            log.info('Registering endpoint: {} {}'.format(methods, rule))
            return route(rule=rule, methods=methods)
        return partial(anon, route=flask_app.route, methods=[http_method])
    return (method_decorator('GET'), method_decorator('PUT'), method_decorator('POST'),
            method_decorator('PATCH'), method_decorator('DELETE'))


get, post, put, patch, delete = http_methods(hermes.server.app)


@get('/')
def index() -> ViewResponse:
    import ipdb
    ipdb.set_trace()
    if session.is_anonymous:
        return redirect(url_for(login))
    return 'Welcome to the Hermes Marketplace'


@post('/api/v1/user/register')
def register() -> ViewResponse:
    try:
        user = register_user(email=request.form.get('email'),
                             password=request.form.get('password'),
                             name=request.form.get('password'),
                             fullname=request.form.get('password'),
                             public_key=request.form.get('password'))
        return jsonify(uuid=user.uuid)
    except AlreadyRegistered:
        return make_response('already_registered', 400)


@post('/api/v1/user/deregister')
@unauthenticated_only
def deregister() -> ViewResponse:
    import ipdb
    ipdb.set_trace()
    return 'Hello World!'


@post('/api/v1/user/login')
@unauthenticated_only
def login() -> ViewResponse:
    if not session.is_anonymous:
        return redirect(url_for(index))
    try:
        authenticate_user(request.form['username'], request.form['password'])
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


@get('/api/v1/user')
@get('/api/v1/user/me')
@authenticated_only
def me() -> ViewResponse:
    return jsonify(**user_details(requesting_user=session['owner'], user=session['owner']))


@get('/api/v1/user/<string:user_id>')
@authenticated_only
def get_user_details(user_id: str) -> ViewResponse:
    try:
        return jsonify(**user_details(requesting_user=session['owner'], user=user_id))
    except ForbiddenAction:
        return make_response()


@patch('/api/v1/user/<string:user_id>')
@authenticated_only
def patch_user(user_id: str) -> ViewResponse:
    # TODO: implement this
    return 'Hello World!'


@post('/api/v1/user/token')
@authenticated_only
def token() -> ViewResponse:
    token = generate_api_token(session['owner'])
    return jsonify(token=token.token, expiration_date=token.expiry)


@delete('/api/v1/user/<string:user_id>/token')
@authenticated_only
def revoke_token(user_id: str) -> ViewResponse:
    return 'Hello World!'


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
