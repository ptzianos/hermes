package org.hermes.utils


import android.util.Log
import java.io.DataInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.security.Provider
import java.security.SecureRandomSpi
import java.security.Security


/**
 * Based on the Java implementation from
 * [BitcoinJ implementation](https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/crypto/LinuxSecureRandom.java)
 *
 *
 * A SecureRandom implementation that is able to override the standard JVM provided
 * implementation, and which simply serves random numbers by reading /dev/urandom. That is, it
 * delegates to the kernel on UNIX systems and is unusable on other platforms. Attempts to manually
 * set the seed are ignored. There is no difference between seed bytes and non-seed bytes, they are
 * all from the same source.
 */
class LinuxSecureRandom : SecureRandomSpi() {

    private val dis: DataInputStream = DataInputStream(urandom)

    /**
     * Registers the LinuxSecureRandom class to the Provider subsystem.
     */
    private class LinuxSecureRandomProvider :
        Provider("LinuxSecureRandom", 1.0,
            "A Linux specific random number provider that uses /dev/urandom") {
        init {
            put("SecureRandom.LinuxSecureRandom", LinuxSecureRandom::class.java.name)
        }
    }

    override fun engineSetSeed(bytes: ByteArray) {
        // Ignore.
    }

    override fun engineNextBytes(bytes: ByteArray) {
        try {
            dis.readFully(bytes) // This will block until all the bytes can be read.
        } catch (e: IOException) {
            throw RuntimeException(e) // Fatal error. Do not attempt to recover from this.
        }

    }

    override fun engineGenerateSeed(i: Int): ByteArray {
        val bits = ByteArray(i)
        engineNextBytes(bits)
        return bits
    }

    companion object {
        private val urandom: FileInputStream

        const val loggingTag = "LinuxSecureRandom"

        init {
            try {
                val file = File("/dev/urandom")
                // This stream is deliberately leaked.
                urandom = FileInputStream(file)
                if (urandom.read() == -1) {
                    throw RuntimeException("/dev/urandom not readable?")
                }
                // Now override the default SecureRandom implementation with this one.
                val position = Security.insertProviderAt(LinuxSecureRandomProvider(), 1)

                if (position != -1) {
                    Log.i(loggingTag, "Secure randomness will be read from $file only.")
                } else {
                    Log.i(loggingTag, "Randomness is already secure.")
                }
            } catch (e: FileNotFoundException) {
                // Should never happen.
                Log.e(loggingTag, "/dev/urandom does not appear to exist or is not openable")
                throw RuntimeException(e)
            } catch (e: IOException) {
                Log.e(loggingTag, "/dev/urandom does not appear to be readable")
                throw RuntimeException(e)
            }

        }
    }
}