package org.hermes.bip32

enum class BTCNet(val prefix: String) {
    PUBLIC_MAIN_NET("0488b21e"),
    PRIVATE_MAIN_NET("0488ade4"),
    PUBLIC_TEST_NET("043587cf"),
    PRIVATE_TEST_NET("04358394")
}
