from itertools import chain
from typing import Dict, Optional, Union

from flask import g
from sqlalchemy import or_

from hermes.exceptions import AlreadyRegistered, ForbiddenAction, UnknownUser, WrongParameters
from hermes.user.models import APIToken, BaseToken, EmailAddress, SessionToken, User


UserLikeObj = Optional[Union[str, User]]
TokenLikeObj = Optional[Union[str, APIToken, SessionToken]]


def resolve_user(some_user: UserLikeObj) -> Optional[User]:
    """Checks if the argument is a string or a User object and resolves it.

    Args:
        some_user (UserLikeObj): The User-like object which could be the user
            UUID or the User object itself.

    Returns:
        User: the User object that is resolved.

    """
    if some_user is None:
        return None
    if type(some_user) not in [str, User]:
        raise Exception('not a user')
    if isinstance(some_user, User):
        return some_user
    return (g.db_session
            .query(User)
            .filter(or_(User.name == some_user, User.uuid == some_user))
            .first()) or \
           (g.db_session
            .query(EmailAddress)
            .filter_by(email=some_user)
            .first())


def resolve_token(some_token: TokenLikeObj) -> Optional[BaseToken]:
    """Checks if the toke is a string and fetches it from the database.

    Args:
        some_token (TokenLikeObj): A BaseToken-like object.

    Returns:
        Optional[BaseToken]: The token that is resolved.

    """
    if some_token is None:
        return None
    if isinstance(some_token, str):
        return (g.db_session
                .query(APIToken)
                .filter_by(token=some_token)
                .first()
                or
                g.db_session
                .query(SessionToken)
                .filter_by(token=some_token)
                .first())
    return some_token


def hash_password(passwd: str) -> str:
    """Hash the password.

    Args:
        passwd (str): a string with the plaintext password.

    Returns:
        str: the password in hashed form.

    Todo:
        - Implement this

    """
    return passwd


def register_user(email: Optional[str] = '',
                  password: Optional[str] = '',
                  name: Optional[str] = '',
                  fullname: Optional[str] = '',
                  public_key: Optional[str] = '',
                  admin: bool = False) -> User:
    """Creates a new user object.

    If there is already a User with the same email, name, fullname or public key
    an exception will be thrown.

    Args:
        email (Optional[str]): the email of the user.
        password (Optional[str]): the plaintext password of the user.
        name (Optional[str]): the username.
        fullname (Optional[str]): the fullname of the User.
        public_key (Optional[str]): the public .
        admin (bool): specifies whether or not the new user will be an
            administrator.

    Returns:
        User: a new instance of `hermes.user.models.User`.

    Todo:
        - add more checks for existence of duplicate user.
        - implement public key only registration.
        - change user email to be instance of EmailAddress model.
        - add verification for emails and public keys.

    """
    if not email or not name or not password:
        raise WrongParameters()
    # check if the public key is valid
    db_session = g.db_session()
    if (db_session
            .query(User)
            .filter(or_(User.email == email, User.name == name))
            .first()):
        raise AlreadyRegistered()
    user = User(email=email,
                name=name,
                password=hash_password(password),
                public_key=public_key,
                fullname=fullname,
                admin=admin)
    db_session.add(user)
    db_session.commit()
    return user


def deregister_user(user_uuid: str) -> None:
    """Deletes a User from the database.

    Objects that are also deleted are:
    ?

    Objects that are modified are:
    * APITokens -> they are revoked.
    * Sessions -> they are revoked.

    Args:
        user_uuid (str): The UUID of the user to be de-registered.

    """
    user = g.db_session.query(User).filter_by(uuid=user_uuid).first()
    if not user:
        raise UnknownUser()
    for token in chain(g.db_session.query(APIToken).filter_by(owner=user, expired=False),
                       g.db_session.query(SessionToken).filter_by(owner=user, expired=False)):
        token.revoke()


def authenticate_user(email_or_username: Optional[str], password_plaintext: Optional[str]) -> User:
    """Authenticates a user based on their credentials.

    Args:
        email_or_username (Optional[str]): name of the user to authenticate.
        password_plaintext (Optional[str]): password in plaintext form.

    Returns:
        User: the ``hermes.user.models.User`` that was authenticated.

    Raises:
        UnknownUser: raised if user doesn't exist or password doesn't match.

    """
    if not email_or_username or not password_plaintext:
        raise WrongParameters()
    user = (g.db_session
            .query(User)
            .filter(or_(name=email_or_username, email=email_or_username))
            .first())
    if not user:
        raise UnknownUser()
    if not user.password == hash_password(password_plaintext):
        raise WrongParameters()
    return user


def user_details(requesting_user: UserLikeObj, user: UserLikeObj) -> Dict[str, str]:
    """Returns the details of a user.

    Args:
        requesting_user (UserLikeObj): the user who makes the request.
        user (UserLikeObj): the user whose details will be returned.

    Returns:
        Dict[str, str]: a dictionary with the details of the user

    Raises:
        ForbiddenAction: if the requesting user is not the one whose details
            are requested and he is also not an administrator.

    """
    requesting_user = resolve_user(requesting_user)
    user = resolve_user(user)
    if not user or not requesting_user:
        raise WrongParameters()
    if user.uuid != requesting_user.uuid and not requesting_user.admin:
        raise ForbiddenAction()
    return {
        'name': user.name,
        'fullname': user.fullname,
        'uuid': user.uuid,
        'admin': str(user.admin),
    }


def generate_api_token(user: UserLikeObj) -> APIToken:
    """Generates an API token for a user.

    Args:
        user (UserLikeObj): a UserLikeObj instance that will be resolved.

    Returns:
        APIToken: A new APIToken for the User.

    """
    user = resolve_user(user)
    api_token = APIToken(owner=user)
    g.db_session.add(api_token)
    return api_token


def revoke_token(user: UserLikeObj, token: TokenLikeObj) -> None:
    """Revoke a token if it's not already expired.

    Args:
        user (UserLikeObj): A UserLikeObj instance that will be resolved.
        token (TokenLikeObj): A TokenLikeObj instance that will be resolved.

    Raises:
        ForbiddenAction: when the user is neither the owner of the token and nor an
            administrator.

    """
    if user.id != token.owner.id and not user.admin:
        raise ForbiddenAction()
    token.revoke()


def su(user: UserLikeObj, user_to_su: UserLikeObj, session: Optional[SessionToken]) -> None:
    """Modifies the session to impersonate a user.

    This function is only available to administrator users who wish to impersonate
    non-administrators.

    Args:
        user (UserLikeObj): A UserLikeObj that will be resolved.
        user_to_su: A UserLikeObj that will be resolved.
        session: The session to be modified.

    Raises:
        ForbiddenAction: if the session is already an impersonating session or is expired.

    """
    user = resolve_user(user)
    user_to_su = resolve_user(user_to_su)
    if None in [user, user_to_su, session]:
        raise WrongParameters()
    if not user.admin or user_to_su.admin:
        raise ForbiddenAction()
    if session.is_su_session:
        raise ForbiddenAction()
    if session.is_expired:
        raise ForbiddenAction()
    session.admin_owner = session.owner
    session.owner = user_to_su
    session.refresh()


def exit_su(session: SessionToken) -> None:
    """Resets the session to its original non-impersonating state.

    Args:
        session (SessionToken): the session to be modified.

    Raises:
        ForbiddenAction: if the session is not an impersonating session or is None.

    """
    if session is None or not session.is_su_session:
        raise ForbiddenAction()
    session.owner = session.admin_owner
    session.admin_owner = None
    session.refresh()
