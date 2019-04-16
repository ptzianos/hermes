package org.hermes.di

import android.app.Application
import android.content.SharedPreferences
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

import org.hermes.HermesClientApp
import org.hermes.HermesRepository
import org.hermes.HermesRoomDatabase
import org.hermes.viewmodels.EventViewModel
import org.hermes.viewmodels.LoginViewModel


@Singleton
@Component(modules = [
    AndroidInjectionModule::class,
    AndroidSupportInjectionModule::class,
    HermesModule::class,
    ActivityBindingModule::class
])
interface HermesComponent : AndroidInjector<HermesClientApp> {

    fun inject(application: Application)

    fun inject(repository: HermesRepository)

    fun providesRoomInstance(): HermesRoomDatabase

    fun providesRepository(): HermesRepository

    fun providesSharedPrefs(): SharedPreferences

    fun providesLoginViewModel(): LoginViewModel

    fun providesEventViewModel(): EventViewModel

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        @BindsInstance
        fun app(hermesClientApp: HermesClientApp): Builder

        fun build(): HermesComponent
    }
}