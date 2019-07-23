package org.hermes.sensors

import android.util.Log
import androidx.room.Ignore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

import org.hermes.BACKGROUND
import org.hermes.LedgerService
import org.hermes.entities.Sensor
import org.hermes.repositories.SensorRepository


@Singleton
class StressTestingSensor @Inject constructor(
    @Ignore private val sensorRepository: SensorRepository
): ISensor, Sensor(
    uuid = "00000000-0000-0000-0000-000000000002",
    dataId = "android.stress",
    unit = "string",
    mtype = "zig_zagging_stress_test",
    what = "",
    device = "",
    latestAddress = "",
    rootAddress = ""
) {

    companion object {
        const val MIN_BYTES = 1000
        const val MAX_BYTES = 10000
    }

    @Ignore
    val loggingTag = "StressTestingSensor"

    override fun beginScrappingData(ledgerService: LedgerService) {
        Log.i(loggingTag, "Starting generating data with uuid $uuid")
        CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
            var lastNumberOfBytes = MIN_BYTES
            var increaseBytes = true
            while (true) {
                val bytes = if (increaseBytes) {
                    if (lastNumberOfBytes < MAX_BYTES) lastNumberOfBytes + 1000
                    else {
                        increaseBytes = false
                        lastNumberOfBytes - 1000
                    }
                } else {
                    if (lastNumberOfBytes > MIN_BYTES) lastNumberOfBytes - 1000
                    else {
                        increaseBytes = true
                        lastNumberOfBytes + 1000
                    }
                }
                val msg = 0.until(bytes).joinToString { "a" }
                ledgerService.iHermesService?.sendDataString(uuid, msg,
                    null, null, null, null, null, null, -1, null)
                lastNumberOfBytes = bytes
                delay(10 * 1000)
            }
        }
    }

    init { sensorRepository.add(this) }
}