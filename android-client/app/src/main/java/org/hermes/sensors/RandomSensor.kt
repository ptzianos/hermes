package org.hermes.sensors

import android.util.Log
import androidx.room.Ignore
import java.util.*
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
class RandomSensor @Inject constructor(
    @Ignore private val sensorRepository: SensorRepository
): ISensor, Sensor(
    uuid = "00000000-0000-0000-0000-000000000001",
    dataId = "android.random",
    unit = "int",
    mtype = "random_source",
    what = "",
    device = "",
    latestAddress = "",
    rootAddress = "",
    latestAddressIndex = 1000
) {

    @Ignore val loggingTag = "RandomSensor"

    override fun beginScrappingData(ledgerService: LedgerService) {
        Log.i(loggingTag, "Starting generating data with uuid $uuid")
        CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
            while (true) {
                ledgerService.iHermesService?.sendDataDouble(uuid, (Random().nextInt() % 30).toDouble(),
                    null, null, null, null, null, null, -1, null)
                delay(5 * 1000)
            }
        }
    }

    init { sensorRepository.add(this) }
}