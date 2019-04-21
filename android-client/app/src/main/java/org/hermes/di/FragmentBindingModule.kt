package org.hermes.di

import dagger.Module
import dagger.android.ContributesAndroidInjector

import org.hermes.fragments.EventLogFragment


@Module
abstract class FragmentBindingModule {
    @ContributesAndroidInjector(modules = [ EventLogFragment.DaggerModule::class ])
    abstract fun eventLogFragment(): EventLogFragment
}
