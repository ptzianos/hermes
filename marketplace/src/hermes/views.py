from typing import Any, Callable, Tuple, Union

from flask import (Flask, make_response, redirect, request,
                   Response, session, url_for)
# from flask.logging import create_logger

from hermes.exceptions import *
from hermes.user.controllers import (authenticate_user,
                                     generate_api_token,
                                     generate_public_key_verification_request,
                                     list_emails,
                                     register_user,
                                     resolve_user,
                                     revoke_token,
                                     su,
                                     user_details,
                                     verify_email,)
from hermes.user.decorators import (admin_only, authenticated_only,
                                    unauthenticated_only)
from hermes.utils import make_json_response


# log = getLogger(__name__)
ViewResponse = Union[Response, Tuple[str, int], str]

view_registry = []


def view_decorator_factory(http_method: str):
    if http_method not in ['GET', 'POST', 'PUT', 'PATCH', 'DELETE']:
        raise Exception('Invalid http method')

    def rule_decorator(rule: str):
        def view_decorator(
                func: Callable[[Any], Response]
        ) -> Callable[[Any], Response]:
            view_registry.append((http_method, func, rule))
            return func
        return view_decorator

    return rule_decorator


get, post, put, patch, delete = (view_decorator_factory('GET'),
                                 view_decorator_factory('POST'),
                                 view_decorator_factory('PUT'),
                                 view_decorator_factory('PATCH'),
                                 view_decorator_factory('DELETE'))


@get('/')
def index() -> ViewResponse:
    if session.is_anonymous:
        return redirect(url_for(login.__name__))
    return 'Welcome to the Hermes Marketplace'


@post('/api/v1/users/register')
def register() -> ViewResponse:
    try:
        user, email_token, public_key_verification = (
            register_user(email=request.form.get('email'),
                          password=request.form.get('password'),
                          name=request.form.get('name'),
                          fullname=request.form.get('fullname'),
                          public_key=request.form.get('public_key'))
        )
        return make_json_response(200, **{
            "uuid": user.uuid,
            "name": user.name,
            "fullname": user.fullname,
            "email_id": email_token.email.uuid if email_token else '',
            "public_key_id": public_key_verification.public_key.uuid,
            "public_key_verification_token": public_key_verification.token,
            "public_key_verification_message": public_key_verification.original_message
        })
    except AlreadyRegistered:
        return make_response('already_registered', 400)


@post('/api/v1/users/deregister')
@unauthenticated_only
def deregister() -> ViewResponse:
    return make_response(501)


@post('/api/v1/users/login')
@unauthenticated_only
def login() -> ViewResponse:
    try:
        user = authenticate_user(request.form['username'],
                                 request.form['password'])
        session.owner = user
        session.refresh()
        return redirect(url_for(index))
    except (UnknownUser, WrongParameters):
        return make_response('wrong credentials', 400)


@get('/api/v1/users/<string:user_id>/emails')
@authenticated_only
def get_emails(user_id: str):
    if user_id != session.owner.id and not session.owner.is_admin:
        return make_response(403)
    return make_response(200, list_emails(user_id))


@get('/api/v1/users/<string:user_id>/emails/<string:email_id>/verify')
def verify_email_view(user_id: str, email_id: str):
    """Verify the email.

    Contrary to normal situations this is a GET request instead of a POST
    and this is so that the user can clink on a link in the email and that
    link will be opened using the browser.
    """
    try:
        verify_email(user_id, email_id, request.form.get('token'))
        return make_response(200)
    except UnknownUser:
        return make_response(403)
    except UnknownEmail:
        return make_response(404)
    except UnknownToken:
        return make_response(400)


@get('/api/v1/users/<string:user_id>/keys/<string:key_id>/message')
def get_key_verification_message(user_id: str, key_id: str):
    try:
        resolve_user(user_id)
        generate_public_key_verification_request(key_id)
        return make_response(200)
    except UnknownUser:
        return make_response(403)
    except UnknownEmail:
        return make_response(404)
    except UnknownToken:
        return make_response(400)


@delete('/api/v1/users/<string:user_id>/emails/<string:email_id>')
@authenticated_only
def delete_email_view(user_id: str, email_id: str):
    if user_id != session.owner.id and not session.owner.is_admin:
        return make_response(403)
    try:
        # TODO: implement this
        delete_email(email_id)
        return make_response(200)
    except UnknownEmail:
        return make_response(404)


@get('/api/v1/users/<string:user_id>/keys/')
@authenticated_only
def list_public_keys():
    """Returns a list of the user's public keys"""
    return 'Not implemented yet'


@post('/api/v1/users/logout')
@authenticated_only
def logout() -> ViewResponse:
    session.revoke()
    return make_response(200)


@post('/api/v1/users/<string:user_id>/su')
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


@get('/api/v1/users/')
@authenticated_only
@admin_only
def list_users() -> ViewResponse:
    return 'Hello World!'


@get('/api/v1/users/me')
@authenticated_only
def me() -> ViewResponse:
    return make_json_response(**user_details(requesting_user=session['owner'],
                                             user=session['owner']))


@get('/api/v1/users/<string:user_id>')
@authenticated_only
def get_user_details(user_id: str) -> ViewResponse:
    try:
        return make_json_response(**user_details(
            requesting_user=session['owner'],
            user=user_id)
        )
    except ForbiddenAction:
        return make_response(403)
    except UnknownUser:
        return make_response(404)


@patch('/api/v1/users/<string:user_id>')
@authenticated_only
def patch_user(user_id: str) -> ViewResponse:
    return make_response(501)


@post('/api/v1/users/<string:user_id>/tokens/')
def create_token(user_id: str) -> ViewResponse:
    if session.is_anonymous:
        try:
            user = authenticate_user(
                request.form.get('username'),
                request.form.get('password'),
                request.form.get('proof_of_ownership_request'),
                request.form.get('proof_of_ownership'),
            )
            session.owner = user
            session.refresh()
        except (UnknownUser, WrongParameters):
            return make_response('forbidden', 403)

    if user_id != session.user.uuid and not session.user.admin:
        return make_response('forbidden', 403)

    token = generate_api_token(session['owner'])
    return make_json_response(id=token.id,
                              token=token.token,
                              expiration_date=token.expiry)


@delete('/api/v1/users/<string:user_id>/token/<string:token_id>')
@authenticated_only
def revoke_token_view(user_id: str, token_id: str) -> ViewResponse:
    if user_id != session.user.uuid and not session.user.admin:
        return make_response('forbidden', 403)
    try:
        revoke_token(session['owner'], token_id)
        return make_response(200)
    except UnknownToken:
        return make_response(404)
    except ForbiddenAction:
        return make_response(403)


@get('/api/v1/ads/')
def list_ads() -> ViewResponse:
    return 'Hello World!'


@post('/api/v1/ads/')
@authenticated_only
def create_ad() -> ViewResponse:
    return 'Hello World!'


@get('/api/v1/ads/<string:ad_id>')
def get_ad(ad_id: str) -> ViewResponse:
    return 'Hello World!'


@patch('/api/v1/ads/<string:ad_id>')
@authenticated_only
def modify_ad(ad_id: str) -> ViewResponse:
    return 'Hello World!'


@delete('/api/v1/ads/<string:ad_id>')
@authenticated_only
def delete_ad(ad_id: str) -> ViewResponse:
    return 'Hello World!'


def register_views_to_app(flask_app: Flask) -> None:
    for http_method, func, rule in view_registry:
        # log.debug('Registering view to flask app: {} {}'.format(http_method, rule))
        flask_app.add_url_rule(rule, func.__name__, func, methods=[http_method])
