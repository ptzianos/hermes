package org.hermes

enum class State(val str: String) {
    UNINITIALIZED("uninitialized"),
    SEEDED("seeded"),
    REGISTERED("registered"),
    FIRST_TOKEN_ACQUIRED("token_acquired"),
}
