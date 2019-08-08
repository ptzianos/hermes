package org.hermes.sensors

import android.util.Log
import androidx.room.Ignore
import java.security.SecureRandom
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import org.hermes.BACKGROUND
import org.hermes.LedgerService
import org.hermes.entities.Sensor
import org.hermes.iota.IOTA
import org.hermes.repositories.SensorRepository

/**
 * A Sensor that will emit enough text to fill a number of transactions
 * that has been specified during the construction of the object.
 */
class StressTestingSensor @Inject constructor(
    @Ignore private val sensorRepository: SensorRepository,
    @Ignore private val iotaTransactionNum: Int
): ISensor, Sensor(
    uuid = "00000000-0000-0000-0000-00000000000%s"
        .format(iotaTransactionNum.toString().padStart(2, '0')),
    dataId = "android.stress.$iotaTransactionNum",
    unit = "string",
    mtype = "stress_test",
    what = "",
    device = "",
    latestAddress = "",
    rootAddress = ""
) {

    @Ignore
    val loggingTag = "StressTestingSensor"

    private val random = SecureRandom.getInstance("SHA1PRNG")

    private fun calculateHeaderSize(): Int {
        return 272 + // size of header until the beginning of the dataId tag with things such as the digest, previous/next address, etc.
               dataId.length +
               ";unit=$unit;mtype=$mtype;".length +
               35 // length of the last tag plus the unix epoch
    }

    override fun beginScrappingData(ledgerService: LedgerService) {
        Log.i(loggingTag, "Starting generating data with uuid $uuid")
        CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
            val lengthOfHeader = calculateHeaderSize()
            val textLength = iotaTransactionNum * IOTA.Transaction.SIGNATURE_MESSAGE_FRAGMENT - lengthOfHeader
            while (true) {
                val msg = 0.until(textLength).joinToString { (random.nextInt(74) + 48).toChar().toString() }
                ledgerService.iHermesService?.sendDataString(uuid, msg,
                    null, null, null, null, null, null, -1, null)
                delay(10 * 1000)
            }
        }
    }

    init {
        sensorRepository.eventBus.sendMessage(sensorRepository.eventBus.obtainMessage().apply {
            obj = Pair(SensorRepository.MessageType.ADD_SENSOR, this) })
    }
}