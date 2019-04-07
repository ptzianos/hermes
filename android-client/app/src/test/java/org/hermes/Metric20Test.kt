package org.hermes

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.threeten.bp.OffsetDateTime

class Metric20Test {
    @Test
    fun `check valid metric to metrics 20`() {
        assertEquals(
            "{\"mtype\": \"b/s\", \"unit\": \"20\", \"meta\": {\"processed_by\": \"hermes\"}}",
            Metric20("host.some.device", "1.0")
                .setData(Metric20.TagKey.MTYPE, "b/s")
                .setData(Metric20.TagKey.UNIT, "20")
                .toJsonString()
        )
    }

    @Test
    fun `check valid metric to carbon 20`() {
        val timestamp = OffsetDateTime.now()
        val expectedString = "host.some.device;mtype=b/s;unit=20;meta.processed_by=hermes ${timestamp.toEpochSecond()} 1.0"
        assertEquals(
            expectedString,
            Metric20("host.some.device", "1.0", timestamp)
                .setData(Metric20.TagKey.MTYPE, "b/s")
                .setData(Metric20.TagKey.UNIT, "20")
                .toCarbon20String()
        )
    }

    // TODO: Add more tests
}