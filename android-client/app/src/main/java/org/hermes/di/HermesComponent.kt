package org.hermes.di

import android.app.Application
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

import org.hermes.HermesClientApp
import org.hermes.HermesLifeCycleObserver
import org.hermes.HermesRepository
import org.hermes.HermesRoomDatabase
import org.hermes.ledgers.IOTAConnector
import org.hermes.viewmodels.EventViewModel
import org.hermes.viewmodels.LoginViewModel


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

    fun inject(repository: HermesRepository)

    fun inject(connector: IOTAConnector)

    fun inject(observer: HermesLifeCycleObserver)

    fun getRoomInstance(): HermesRoomDatabase

    fun getRepository(): HermesRepository

    fun getLoginViewModel(): LoginViewModel

    fun getEventViewModel(): EventViewModel

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