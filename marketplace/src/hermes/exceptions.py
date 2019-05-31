class NoSuchUser(Exception):
    msg = 'unknown_user'


class ExpiredToken(Exception):
    msg = 'expired_token'


class ForbiddenAction(Exception):
    msg = 'action_not_permitted'


class UnknownUser(Exception):
    msg = 'unknown_user'


class WrongParameters(Exception):
    msg = 'wrong_parameters'


class AlreadyRegistered(Exception):
    msg = 'already_registered'


class AlreadyVerified(Exception):
    msg = 'already_verified'


class AlreadyRevoked(Exception):
    msg = 'already_revoked'


class UnknownToken(Exception):
    msg = 'unknown_token'


class UnknownPublicKey(Exception):
    msg = 'unknown_public_key'


class UserHasNoPassword(Exception):
    msg = 'no_password'


class WeakPassword(Exception):
    msg = 'password_not_strong_enough'


class NoPassword(Exception):
    msg = 'no_password'


class UnsupportedPublicKeyType(Exception):
    msg = 'unsupported_public_key_type'


class UnknownEmail(Exception):
    msg = 'unknown_email'
