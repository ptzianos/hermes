package org.hermes.sensors

import org.hermes.LedgerService

interface ISensor {

    fun beginScrappingData(ledgerService: LedgerService)

}