# Hermes Wallet Library

A pure Kotlin implementation of [BIP32](https://github.com/bitcoin/bips/blob/master/bip-0032.mediawiki),
[BIP39](https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki),
[BIP44](https://github.com/bitcoin/bips/blob/master/bip-0044.mediawiki),
[SLIP44](https://github.com/satoshilabs/slips/blob/master/slip-0044.md) and a balanced ternary system.

## Architecture

The codebase of the wallet is split into the following modules:
- collections: a small library of data structures
- extensions: a small library of Kotlin standard object extensions
- ternary: a library that implements a balanced ternary system
- bip32: implementation of the BIP32 specification
- bip39: implementation of the BIP39 specification
- bip44: implementation of the BIP44 specification

The BIP44 wallet implementation is an extension of the BIP32 wallet. The BIP32 wallet is compliant with the specification
and all the test vectors.

The BIP39 implementation is also compliant with the specification and passes all the tests using the official test
vectors.

You can use any of the modules independently.

## Development, Automation and Code Quality

All modules are written in Kotlin, Gradle is used for building, testing and packaging,
[Ktlint](https://ktlint.github.io/) is used for linting and Makefile is used to automate most of the tasks.

For most development related chores, there are appropriate targets in the Makefile such as build, jar and list.
In most cases, Makefile targets are used to wrap and simplify Gradle tasks. The pattern behind the tasks is:
`<module_name>.<task>`

** Contributions are more than welcome! **

### Linting

In some places, the code is allowed to deviate in minor ways from the KtLint standard. The following are examples show
the acceptable deviations:

```kotlin
... = when {
    positive -> BigInteger(1, this)
    else ->     BigInteger(this)
}
```

In this case a few extra spaces are added, to allow for the reader to easily parse the `when` command.

```kotlin
= when {
    this.size < digits -> if (padStart) this.something()
                          else          this.somethingElse()
}
```

Again, some extra space is allowed to allow the reader to parse more easily the combination of a `when` and an `if`.
These two rules are: `indent` and `no-multi-spaces`.
All other rules are adhered to.
