package org.hermes.di

import dagger.Module
import dagger.android.ContributesAndroidInjector

import org.hermes.fragments.EventLogFragment
import org.hermes.fragments.SensorListFragment


@Module
abstract class FragmentBindingModule {
    @ContributesAndroidInjector(modules = [ EventLogFragment.DaggerModule::class ])
    abstract fun eventLogFragment(): EventLogFragment

    @ContributesAndroidInjector(modules = [ SensorListFragment.DaggerModule::class ])
    abstract fun sensorListFragment(): SensorListFragment
}
