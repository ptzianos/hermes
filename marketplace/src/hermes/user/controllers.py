from datetime import datetime
from itertools import chain
from logging import getLogger
from typing import Dict, Iterable, List, Optional, Tuple, Union

from Crypto.PublicKey.RSA import import_key as import_rsa_key
from Crypto.PublicKey.ECC import import_key as import_ecdsa_key
from Crypto.Signature.pkcs1_15 import PKCS115_SigScheme
from Crypto.Hash import SHA3_512
from flask import g
from sqlalchemy import or_

from hermes.config import (PUBLIC_KEY_VERIFICATION_TEXT,
                           PUBLIC_KEY_VERIFICATION_REQUEST_DURATION)
from hermes.exceptions import (AlreadyRegistered, AlreadyVerified,
                               ExpiredToken, ForbiddenAction, NoSuchUser,
                               NoPassword, UnknownToken, UnknownUser,
                               UnsupportedPublicKeyType, UserHasNoPassword,
                               WeakPassword, WrongParameters, )
from hermes.user.models import (APIToken, BaseToken, EmailAddress,
                                EmailVerificationToken, PasswordResetToken,
                                PublicKey, PublicKeyVerificationRequest,
                                SessionToken, User, )


UserLikeObj = Optional[Union[str, User]]
TokenLikeObj = Optional[Union[str, APIToken, SessionToken]]

log = getLogger(__name__)


def resolve_user(some_user: UserLikeObj) -> Optional[User]:
    """Checks if the argument is a string or a User object and resolves it.

    Args:
        some_user (UserLikeObj): The User-like object which could be the user
            UUID, the User object itself, the user email or the user's public
            key.

    Returns:
        User: the User object that is resolved.

    """
    if some_user is None:
        return None
    if type(some_user) not in [str, User]:
        raise Exception('not a user')
    if isinstance(some_user, User):
        return some_user
    return ((g.db_session
            .query(User)
            .filter(or_(User.name == some_user,
                        User.fullname == some_user,
                        User.uuid == some_user))
            .first()) or
            (g.db_session
            .query(User)
            .join(EmailAddress)
            .filter_by(address=some_user)
            .first()) or
            (g.db_session
            .query(User)
            .join(PublicKey)
            .filter_by(value=some_user)
            .first()))


def resolve_token(some_token: TokenLikeObj) -> Optional[BaseToken]:
    """Checks if the toke is a string and fetches it from the database.

    Args:
        some_token (TokenLikeObj): A BaseToken-like object.

    Returns:
        Optional[BaseToken]: The token that is resolved.

    """
    if some_token is None:
        return None
    if type(some_token) not in [str, APIToken, SessionToken]:
        raise UnknownToken()
    if isinstance(some_token, BaseToken):
        return some_token
    return (g.db_session
            .query(APIToken)
            .filter_by(token=some_token)
            .first()
            or
            g.db_session
            .query(SessionToken)
            .filter_by(token=some_token)
            .first())


def check_password_strength(password: str):
    """Checks whether or not the password is strong enough

    Args:
        password (str): the password to check

    Raises:
        WeakPassword: when the password is too weak

    Todo:
        - Implement this

    """
    if not password:
        raise NoPassword()
    if len(password) < 4:
        raise WeakPassword()
    return password


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


def hash_value(some_str: str) -> str:
    """Hash a random string.

    Should not be used for hashing passwords, but just for getting digests.

    Args:
        some_str (str): a string to be hashed

    Returns:
        str: the string in hashed form

    """
    return (SHA3_512.new()
            .update(some_str.encode())
            .digest()
            .hex())


def check_ecdsa_public_key(key: str) -> bool:
    """Parses a public ECDSA key and returns whether it's valid or not."""
    try:
        import_ecdsa_key(key)
        return True
    except (ValueError, IndexError, TypeError):
        return False


def check_rsa_public_key(key: str) -> bool:
    """Parses a public RSA key and returns whether it's valid or not."""
    try:
        import_rsa_key(key)
        return True
    except (ValueError, IndexError, TypeError):
        return False


def register_user(
    email: Optional[str] = '',
    password: Optional[str] = '',
    name: Optional[str] = '',
    fullname: Optional[str] = '',
    public_key: str = '',
    public_key_type: str = 'ecdsa',
    admin: bool = False
) -> Tuple[User, Optional[EmailVerificationToken], PublicKeyVerificationRequest]:
    """Creates a new user object.

    If there is already a User with the same email, name, fullname or public
    key an exception will be thrown.

    Args:
        email (Optional[str]): the email of the user.
        password (Optional[str]): the plaintext password of the user.
        name (Optional[str]): the username.
        fullname (Optional[str]): the fullname of the User.
        public_key (str): the public .
        public_key_type (str): the public .
        admin (bool): specifies whether or not the new user will be an admin.

    Raises:
        AlreadyRegistered: a user with the same name or email already exists.
        UnsupportedPublicKeyType: when the key algorithm is unknown or unsupported.
        WrongParameters: when a parameter is missing.

    Returns:
        User: a new instance of `hermes.user.models.User`.

    """
    if not public_key:
        raise WrongParameters()
    name = name or email or hash_value(public_key)
    fullname = fullname or name
    if (public_key_type == 'ecdsa'
            and not check_ecdsa_public_key(public_key)):
        raise WrongParameters()
    elif (public_key_type == 'rsa'
            and not check_rsa_public_key(public_key)):
        raise WrongParameters()
    elif public_key_type not in ['ecdsa', 'rsa']:
        raise UnsupportedPublicKeyType()
    if (resolve_user(email) or resolve_user(name) or resolve_user(fullname)
            or resolve_user(public_key)):
        raise AlreadyRegistered()

    # We need to either save all the objects together (User, PublicKey and maybe
    # email) or we rollback all of them
    g.db_session.begin_nested()

    user = User(name=name, fullname=fullname, admin=admin)
    if password:
        check_password_strength(password)
        user.password = hash_password(password)
    g.db_session.add(user)

    public_key_model = PublicKey(owner=user, value=public_key,
                                 type=public_key_type)
    public_key_verification_token = generate_public_key_verification_request(
        public_key_model)
    user.public_key = public_key_model
    g.db_session.add(public_key_model)

    if email:
        email_model = EmailAddress(owner=user, address=email)
        g.db_session.add(email_model)
        email_verification_token = generate_email_verification_token(email_model)
    else:
        email_verification_token = None

    try:
        g.db_session.commit()
    except Exception as e:
        log.error(repr(e))
        raise WrongParameters()

    return user, email_verification_token, public_key_verification_token


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
    for token in chain(g.db_session.query(APIToken).filter_by(owner=user,
                                                              expired=False),
                       g.db_session.query(SessionToken).filter_by(owner=user,
                                                                  expired=False)):
        token.revoke()


def authenticate_user(
    email_or_username: Optional[str],
    password_plaintext: Optional[str]
) -> User:
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


def user_details(
    requesting_user: UserLikeObj,
    user: UserLikeObj
) -> Dict[str, str]:
    """Returns the details of a user.

    Args:
        requesting_user (UserLikeObj): the user who makes the request.
        user (UserLikeObj): the user whose details will be returned.

    Returns:
        Dict[str, str]: a dictionary with the details of the user

    Raises:
        ForbiddenAction: if the requesting user is not the one whose details
            are requested and he is also not an administrator.
        UnknownUser: if any of the two user can not be found

    """
    requesting_user = resolve_user(requesting_user)
    user = resolve_user(user)
    if not user or not requesting_user:
        raise UnknownUser()
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

    Raises:
        UnknownUser: when no user is

    Returns:
        APIToken: A new APIToken for the User.

    """
    user = resolve_user(user)
    if not user:
        raise UnknownUser()
    api_token = APIToken(owner=user).refresh()
    g.db_session.add(api_token)
    g.db_session.commit()
    return api_token


def revoke_token(user: UserLikeObj, token: TokenLikeObj) -> None:
    """Revoke a token if it's not already expired.

    Args:
        user (UserLikeObj): A UserLikeObj instance that will be resolved.
        token (TokenLikeObj): A TokenLikeObj instance that will be resolved.

    Raises:
        ForbiddenAction: when the user is neither the owner of the token nor an
            administrator.

    """
    user = resolve_user(user)
    if not user:
        raise UnknownUser()
    token = resolve_token(token)
    if not token:
        raise UnknownToken()
    if user.id != token.owner.id and not user.admin:
        raise ForbiddenAction()
    token.revoke()


def su(
    user: UserLikeObj,
    user_to_su: UserLikeObj,
    session: Optional[SessionToken]
) -> None:
    """Modifies the session to impersonate a user.

    This function is only available to administrator users who wish to
    impersonate non-administrators.

    Args:
        user (UserLikeObj): A UserLikeObj that will be resolved.
        user_to_su: A UserLikeObj that will be resolved.
        session: The session to be modified.

    Raises:
        ForbiddenAction: if the session is already an impersonating session
            or is expired.

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
        ForbiddenAction: if the session is not an impersonating session or
            is None.

    """
    if session is None or not session.is_su_session:
        raise ForbiddenAction()
    session.owner = session.admin_owner
    session.admin_owner = None
    session.refresh()


def list_keys(user: UserLikeObj) -> List[Dict[str, str]]:
    """List all the public keys of the user.

    The list returned will be a list of dictionaries with the uuid, a flag
    to show whether or not its verified and the date when it was added to
    the platform.

    Args:
        user (UserLikeObj): a user model, email, name or UUID.

    Raises:
        UnknownUser: when user matching any of the aforementioned criteria
            cannot be located.

    """
    user = resolve_user(user)
    if not user:
        raise UnknownUser()

    def to_json(public_key: PublicKey):
        return {
            "uuid": public_key.uuid,
            "verified": public_key.verified,
            "added_on": public_key.created_on,
        }
    return list(map(to_json,
                    (g.db_session
                     .query(PublicKey)
                     .join(User)
                     .filter_by(id=user.id))))


def generate_email_verification_token(
    email: EmailAddress
) -> EmailVerificationToken:
    """Creates a new token for verifying an email.

    If the email is already verified an exception will be thrown. If there are
    tokens in the database that need to be expired, this method will do that.
    If there are valid tokens available all except one will be revoked and no
    new tokens will be created.

    Args:
        email (EmailAddress): the email model to be verified.

    Raises:
        AlreadyVerified: when the email has already been verified.

    """
    if email.verified:
        raise AlreadyVerified()
    existing_email_tokens: Iterable[EmailVerificationToken] = (
        g.db_session
         .query(EmailVerificationToken)
         .filter_by(email_id=email.id, expired=False)
    )
    valid_tokens = []
    for token in existing_email_tokens:
        if token.is_expired:
            token.revoke()
        else:
            valid_tokens.append(token)
    for token in valid_tokens[1:]:
        token.revoke()
    if valid_tokens:
        return valid_tokens[0]
    email_verification_token = EmailVerificationToken(
        email=email,
        owner=email.owner
    )
    email_verification_token.refresh()
    g.db_session.add(email_verification_token)
    return email_verification_token


def generate_password_reset_token(user: User) -> PasswordResetToken:
    """Creates a new password reset token changing a password.

    If there is already a password reset token, it is revoked and a new
    one is generated. A user with no password set can not get a password
    reset token.

    Args:
        user (User): the user model for which to generate the reset token.

    Raises:
        NoSuchUser: when an invalid user is given.
        UserHasNoPassword:

    Returns:
        PasswordResetToken: the password reset token model.

    """
    user = resolve_user(user)
    if not user:
        raise NoSuchUser()
    if user.password is None:
        raise UserHasNoPassword()
    existing_reset_tokens: Iterable[EmailVerificationToken] = (
        g.db_session
         .query(PasswordResetToken)
         .join(User)
         .filter_by(id=user.id, expired=False)
    )
    for token in existing_reset_tokens:
        token.revoke()
    password_reset_token = PasswordResetToken(owner=user).refresh()
    g.db_session.add(password_reset_token)
    return password_reset_token


def generate_public_key_verification_request(
    public_key: PublicKey
) -> PublicKeyVerificationRequest:
    """Creates a new request for verifying a public key.

    If there is already a non-revoked public key request it will be reused
    to prevent an attacker from generating a lot of slightly different
    messages and trying to attack the system.

    Args:
        public_key (PublicKey): the public key model to be verified

    Raises:
        AlreadyVerified: when the public key has already been verified.
        UnknownToken: can not find a request with such a token

    Returns:
        PublicKeyVerificationRequest

    """
    if public_key.verified:
        raise AlreadyVerified()
    existing_public_key_verification_tokens: Iterable[PublicKeyVerificationRequest] = (
        g.db_session
         .query(PublicKeyVerificationRequest)
         .filter_by(public_key_id=public_key.id, expired=False)
    )
    for token in existing_public_key_verification_tokens[1:]:
        token.revoke()

    message = PUBLIC_KEY_VERIFICATION_TEXT.format(
        digest=public_key.uuid,
        expiration_date=(datetime.now() +
                         PUBLIC_KEY_VERIFICATION_REQUEST_DURATION)
    )
    public_key_verification_token = PublicKeyVerificationRequest(
        public_key=public_key,
        owner=public_key.owner,
        original_message=message,
        message_hash=hash_value(message),
    )
    public_key_verification_token.refresh()
    g.db_session.add(public_key_verification_token)
    return public_key_verification_token


def verify_email(user: User, email_verification_token: str) -> None:
    user = resolve_user(user)
    if not user:
        raise UnknownUser()

    verification_token_model = (g.db_session
                                .query(EmailVerificationToken)
                                .filter_by(token=email_verification_token))
    if not verification_token_model:
        raise UnknownToken()


def reset_password(user: UserLikeObj,
                   new_password: Optional[str],
                   password_reset_token: PasswordResetToken) -> None:
    """Gets a password reset token and the changes the user's password.

    Raises:
        UnknownUser
        UnknownToken
        ExpiredToken
        AlreadyVerified

    """
    user = resolve_user(user)
    if not user:
        raise UnknownUser()
    if not new_password or not password_reset_token:
        raise WrongParameters()
    password_reset_model: Optional[PasswordResetToken] = (
        g.db_session
         .query(PasswordResetToken)
         .filter_by(token=password_reset_token)
         .first()
    )
    if not password_reset_model:
        raise UnknownToken()


def verify_public_key(
        user: User,
        public_key_verification_token: PublicKeyVerificationRequest,
        proof_of_verification: str
) -> None:
    """Validates a response to a public key verification request token.

    Raises:
        UnknownUser
        UnknownToken
        ExpiredToken
        AlreadyVerified

    """
    user = resolve_user(user)
    if not user:
        raise UnknownUser()
    request: Optional[PublicKeyVerificationRequest] = (
        g.db_session
         .query(PublicKeyVerificationRequest)
         .filter_by(token=public_key_verification_token)
         .first()
    )
    if not request or request.owner.id != user.id:
        raise UnknownToken()
    if request.public_key.verified:
        raise AlreadyVerified()
    if request.is_expired:
        raise ExpiredToken()
    if proof_of_verification != request.expected:
        raise WrongParameters()
    request.public_key.verified = True
