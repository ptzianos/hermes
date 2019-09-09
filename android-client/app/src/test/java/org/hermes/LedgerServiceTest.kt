package org.hermes

import org.hermes.entities.Sensor
import org.junit.Test
import org.junit.jupiter.api.Assertions.*

internal class LedgerServiceTest {

    @Test
    fun testSensorBufferCounter() {
        val sensor = Sensor("1", "test", "m/s", "int", null, null, "", "", 1000)
        for (i in 1..2 * Sensor.SENSOR_BUFFER_SIZE) {
            sensor.putSample(Metric20("pkHash", i))
            assertEquals(if (i < Sensor.SENSOR_BUFFER_SIZE) i else Sensor.SENSOR_BUFFER_SIZE, sensor.counter)
        }
    }

    @Test
    fun testSensorBuffering() {
        val sensor = Sensor("1", "test", "m/s", "int", null, null, "", "", 1000)
        for (i in 1 .. 5)
            sensor.putSample(Metric20("pkHash", i))
        assertEquals(5, sensor.counter)
        var samples = sensor.flushData()
        assertEquals(5, samples.size)
        for (i in 0 until 5)
            assertEquals(i + 1, samples[i]?.value)

        assertEquals(0, sensor.flushData().size)

        for (i in 1 .. Sensor.SENSOR_BUFFER_SIZE + 5)
            sensor.putSample(Metric20("pkHash", i))
        samples = sensor.flushData()
        assertEquals(Sensor.SENSOR_BUFFER_SIZE, samples.size)
        for (i in 0 until Sensor.SENSOR_BUFFER_SIZE)
            assertEquals(i + 5 + 1, samples[i]?.value)

        for (i in 1 .. 5)
            sensor.putSample(Metric20("pkHash", i))
        for (i in 1 .. 3)
            sensor.putSample(Metric20("pkHash", -1 * i))
        samples = sensor.flushData()
        assertEquals(8, samples.size)
        for (i in 0 until 5)
            assertEquals(i + 1, samples[i]?.value)
        for (i in 1 .. 3)
            assertEquals(-1 * i, samples[4 + i]?.value)

        for (i in 1 until Sensor.SENSOR_BUFFER_SIZE)
            sensor.putSample(Metric20("pkHash", i))
        for (i in 1 .. 3)
            sensor.returnSample(Metric20("pkHash", -1 * i))
        samples = sensor.flushData()
        assertEquals(Sensor.SENSOR_BUFFER_SIZE, samples.size)
        assertEquals(-1, samples[0]?.value)
        for (i in 1 until Sensor.SENSOR_BUFFER_SIZE)
            assertEquals(i, samples[i]?.value)
    }
}