from decimal import Decimal, InvalidOperation
from typing import Any, Callable, Tuple, TYPE_CHECKING, Union

import requests
from flask import (Flask, make_response, redirect, request,
                   Response, session, url_for)

from hermes.ad.controllers import (AdQuery, ad_to_json, create_ad, delete_ad,
                                   resolve_ad)
from hermes.exceptions import *
from hermes.user.controllers import (authenticate_user,
                                     deregister_user,
                                     generate_api_token,
                                     generate_public_key_verification_request,
                                     list_emails,
                                     list_keys,
                                     list_tokens,
                                     register_user,
                                     resolve_user,
                                     revoke_token,
                                     su,
                                     user_details,
                                     verify_email,)
from hermes.user.decorators import (admin_only, authenticated_only,
                                    owner_or_admin, unauthenticated_only)
from hermes.utils import make_json_response


if TYPE_CHECKING:
    from hermes.user.models import AuthenticationToken
    session: AuthenticationToken


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
                          public_key_type=request.form.get('public_key_type'),
                          public_key=request.form.get('public_key'))
        )
        return make_json_response(requests.codes.ok, **{
            "uuid": user.uuid,
            "name": user.name,
            "fullname": user.fullname,
            "email_id": email_token.email.uuid if email_token else '',
            "email": email_token.email.address if email_token else '',
            "public_key_id": public_key_verification.public_key.uuid,
            "public_key_verification_token": public_key_verification.token,
            "public_key_verification_message":
                public_key_verification.original_message
        })
    except AlreadyRegistered:
        return make_response('already_registered', requests.codes.bad_request)
    except WrongParameters:
        return make_response('', requests.codes.bad_request)


@post('/api/v1/users/<string:user_id>/deregister')
@authenticated_only
@owner_or_admin
def deregister(user_id: str) -> ViewResponse:
    deregister_user(user_id)
    return make_response(200)


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
        return make_response('', requests.codes.bad_request)


@post('/api/v1/users/logout')
@authenticated_only
def logout() -> ViewResponse:
    session.revoke()
    return make_response('', requests.codes.ok)


@get('/api/v1/users/<string:user_id>/emails')
@authenticated_only
@owner_or_admin
def get_emails(user_id: str):
    return make_response('', requests.codes.ok, list_emails(user_id))


@delete('/api/v1/users/<string:user_id>/emails/<string:email_id>')
@authenticated_only
@owner_or_admin
# TODO: Implement me
def delete_email_view(user_id: str, email_id: str):
    # try:
    #     # TODO: implement this
    #     delete_email(email_id)
    #     return make_response('', requests.codes.ok)
    # except UnknownEmail:
    #     return make_response('', requests.codes.not_found)
    return make_response('', requests.codes.not_implemented)


@get('/api/v1/users/<string:user_id>/emails/<string:email_id>/verify')
def verify_email_view(user_id: str, email_id: str):
    """Verify the email.

    Contrary to normal situations this is a GET request instead of a POST
    and this is so that the user can clink on a link in the email and that
    link will be opened using the browser.
    """
    try:
        verify_email(user_id, email_id, request.form.get('token'))
        return make_response('', requests.codes.ok)
    except UnknownUser:
        return make_response('', requests.codes.forbidden)
    except UnknownEmail:
        return make_response('', requests.codes.not_found)
    except UnknownToken:
        return make_response('', requests.codes.bad_request)


@get('/api/v1/users/<string:user_id>/keys/')
@authenticated_only
@owner_or_admin
def list_public_keys(user_id: str) -> ViewResponse:
    """Returns a list of the user's public keys"""
    if not session.owner.admin and not user_id == session.owner.uuid:
        return make_response('', requests.codes.forbidden)
    return make_json_response(requests.codes.ok, keys=list_keys(user_id))


@post('/api/v1/users/<string:user_id>/su')
@authenticated_only
@admin_only
def post_su(user_id: str) -> ViewResponse:
    try:
        su(user=session['owner'], user_to_su=user_id)
        return make_response('', requests.codes.ok)
    except WrongParameters:
        return make_response('', requests.codes.bad_request)
    except ForbiddenAction:
        return make_response('', requests.codes.forbidden)


@get('/api/v1/users/')
@authenticated_only
@admin_only
def list_users() -> ViewResponse:
    return 'Hello World!'


@get('/api/v1/users/me')
@authenticated_only
def me() -> ViewResponse:
    return make_json_response(requests.codes.ok,
                              **user_details(requesting_user=session['owner'],
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
        return make_response('', requests.codes.forbidden)
    except UnknownUser:
        return make_response('', requests.codes.not_found)


@patch('/api/v1/users/<string:user_id>')
@authenticated_only
@owner_or_admin
# TODO: Implement me
def patch_user(user_id: str) -> ViewResponse:
    return make_response('', requests.codes.not_implemented)


@get('/api/v1/users/<string:user_id>/keys/<string:key_id>/message')
def get_key_verification_message(user_id: str, key_id: str):
    try:
        resolve_user(user_id)
        public_key_verification_request = \
            generate_public_key_verification_request(key_id)
        return make_json_response(
            status_code=requests.codes.ok, **{
                "public_key_verification_token":
                    public_key_verification_request.token,
                "public_key_verification_message":
                    public_key_verification_request.original_message,
            })
    except UnknownUser:
        return make_response('', requests.codes.forbidden)
    except UnknownEmail:
        return make_response('', requests.codes.not_found)
    except (AlreadyVerified, UnknownPublicKey):
        return make_response('', requests.codes.bad_request)


@get('/api/v1/users/<string:user_id>/tokens/')
@authenticated_only
@owner_or_admin
def list_tokens_view(user_id: str) -> ViewResponse:
    return make_json_response(requests.codes.ok, tokens=list_tokens(user_id))


@post('/api/v1/users/<string:user_id>/tokens/')
def create_token(user_id: str) -> ViewResponse:
    if session.is_anonymous:
        try:
            user = authenticate_user(
                request.form.get('username'),
                request.form.get('password'),
                request.form.get('proof_of_ownership_token'),
                request.form.get('proof_of_ownership'),
            )
            session.owner = user
            session.refresh()
        except (UnknownUser, WrongParameters):
            return make_response('', requests.codes.forbidden)
        except (ExpiredToken, ValueError):
            return make_response('', requests.codes.bad_request)

    if user_id != session.owner.uuid and not session.owner.admin:
        return make_response('', requests.codes.forbidden)

    token = generate_api_token(session['owner'])
    return make_json_response(token=token.token, expiration_date=token.expiry)


@delete('/api/v1/users/<string:user_id>/tokens/<string:token_name>')
@authenticated_only
@owner_or_admin
def revoke_token_view(user_id: str, token_name: str) -> ViewResponse:
    try:
        revoke_token(session['owner'], token_name)
        return make_response('', requests.codes.ok)
    except UnknownToken:
        return make_response('', requests.codes.not_found)
    except ForbiddenAction:
        return make_response('', requests.codes.forbidden)
    except AlreadyRevoked:
        return make_response('', requests.codes.bad_request)


@put('/api/v1/ads/')
@post('/api/v1/ads/')
@authenticated_only
def create_ad_view() -> ViewResponse:
    # TODO: Eventually allow non-mobile sensors, non-zero rates and
    # other currencies
    try:
        new_ad = create_ad(
            owner=session.owner,
            data_type=request.args.get('data_type'),
            data_unit=request.args.get('data_unit'),
            start_of_stream_address=request.args.get('start_of_stream_address'),
            longitude=Decimal(request.args.get('longitude')),
            latitude=Decimal(request.args.get('latitude')),
            mobile=True, rate=Decimal(0.0), currency='miota')
    except (AttributeError, InvalidOperation, NoStartOfStream, UnknownProtocol,
            UnknownLocation, WrongRate):
        return make_response('', requests.codes.bad_request)
    return make_json_response(requests.codes.ok, uuid=new_ad.uuid)


@get('/api/v1/ads/')
def list_ads() -> ViewResponse:
    """View function for listing available ads.

    Query parameters are:
    * x, y, width,height: for defining an area of interest
    * data_type: type of data in the stream
    """
    query = AdQuery().active(active=not request.args.get('inactive'))
    if any(map(lambda param: request.args.get(param) is not None,
               ['x', 'y', 'width', 'height'])):
        try:
            query.by_location(latitude=Decimal(request.args.get('x')),
                              longitude=Decimal(request.args.get('y')),
                              width=Decimal(request.args.get('width')),
                              height=Decimal(request.args.get('height')))
        except (InvalidOperation, TypeError, WrongLocationParameters):
            make_response('', requests.codes.bad_request)
    if request.args.get('data_type'):
        query.by_data_type(request.args.get('data_type'))
    return make_json_response(status_code=200, ads=query.to_json())


@get('/api/v1/ads/<string:ad_id>')
def get_ad(ad_id: str) -> ViewResponse:
    ad = resolve_ad(ad_id)
    return (make_json_response(200, **ad_to_json(ad))
            if ad else make_response('', requests.codes.not_found))


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
        flask_app.add_url_rule(rule, func.__name__, func,
                               methods=[http_method])
