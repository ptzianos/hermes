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


class UnknownProtocol(Exception):
    msg = 'unknown_messaging_protocol'


class UnknownLocation(Exception):
    msg = 'unknown_location_of_static_sensor'


class WrongRate(Exception):
    msg = 'wrong_rate'


class WrongLocationParameters(Exception):
    msg = 'wrong_location_params'


class UnknownAd(Exception):
    msg = 'unknown_ad'


class NoStartOfStream(Exception):
    msg = 'no_start_of_stream'


class AlreadyInactiveAd(Exception):
    msg = 'ad_already_inactive'
