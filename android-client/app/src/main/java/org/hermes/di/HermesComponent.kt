package org.hermes.di

import android.app.Application
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.BindsInstance
import dagger.Component
import org.hermes.*
import javax.inject.Singleton

import org.hermes.ledgers.IOTAConnector
import org.hermes.viewmodels.EventLogViewModel
import org.hermes.viewmodels.LoginViewModel
import org.hermes.viewmodels.SensorListViewModel


@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AndroidSupportInjectionModule::class,
    HermesModule::class,
    IOTAModule::class,
    ActivityBindingModule::class,
    FragmentBindingModule::class,
    ServiceBindingModule::class
])
interface HermesComponent : AndroidInjector<HermesClientApp> {

    fun inject(application: Application)

    fun inject(repository: MetadataRepository)

    fun inject(repository: CryptoRepository)

    fun inject(repository: MarketRepository)

    fun inject(connector: IOTAConnector)

    fun inject(observer: HermesLifeCycleObserver)

    fun getRoomInstance(): HermesRoomDatabase

    fun getMarketRepository(): MarketRepository

    fun getMetadataRepository(): MetadataRepository

    fun getCryptoRepository(): CryptoRepository

    fun getLoginViewModel(): LoginViewModel

    fun getEventLogViewModel(): EventLogViewModel

    fun getSensorListViewModel(): SensorListViewModel

    fun getLifeCycleObserver(): HermesLifeCycleObserver

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun app(hermesClientApp: HermesClientApp): Builder

        fun build(): HermesComponent
    }
}