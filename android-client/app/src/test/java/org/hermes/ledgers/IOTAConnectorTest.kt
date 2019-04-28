package org.hermes.ledgers

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hermes.HermesRoomDatabase
import org.hermes.Metric20
import org.hermes.iota.Seed
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
internal class IOTAConnectorTest {

    // TODO: Use Dagger2 to fix this ugly thing
    val db = lazy {
        Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            HermesRoomDatabase::class.java).build()
    }

    @Test
    fun sendData() {
        // TODO: Fix this test to assert result and not use the network
//        val iotaConnector = IOTAConnector(protocol = "https",
//            uri = "nodes.devnet.thetangle.org", port = "443", seed = Seed.new(),
//            db = db.value
//        )
//        iotaConnector.sendData(
//            Metric20("host.device", 1.0)
//                .setData(Metric20.TagKey.MTYPE, "something")
//                .setData(Metric20.TagKey.UNIT, "b/s"),
//            asyncConfirmation = true, blockUntilConfirmation = false
//        )
    }
}