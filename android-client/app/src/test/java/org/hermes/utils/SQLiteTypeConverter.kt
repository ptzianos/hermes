package org.hermes.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.threeten.bp.*

class BalancedTrinaryTests {
    @Test
    fun `string to timestamp`() {
        val localDate = LocalDate.of(2019, Month.MARCH, 15)
        val localTime = LocalTime.MIDNIGHT
        val zoneOffset = ZoneOffset.UTC
        val localDateTime = LocalDateTime.of(localDate, localTime)
        assertEquals(
                SQLiteTypeConverter.toOffsetDateTime(""),
                OffsetDateTime.of(localDateTime, zoneOffset))
    }
}