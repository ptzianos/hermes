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
