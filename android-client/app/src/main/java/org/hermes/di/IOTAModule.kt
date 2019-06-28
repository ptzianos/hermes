package org.hermes.di

import dagger.Module
import dagger.Provides
import org.iota.jota.IotaAPI

@Module
class IOTAModule {
    @Provides fun iotaAPI(): IotaAPI {
        return IotaAPI.Builder()
            .protocol("https")
            .host("nodes.thetangle.org")
            .port(443)
            .timeout(20)
            .build()
    }
}