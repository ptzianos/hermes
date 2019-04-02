package org.hermes.sensors

// TODO: This needs to go more work and planning for the sensor subsystem
interface ISensor {
    fun getMetricName(): String

    fun getMetricContext(): String
}