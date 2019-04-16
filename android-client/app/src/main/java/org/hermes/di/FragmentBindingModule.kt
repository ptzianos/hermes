package org.hermes.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.hermes.fragments.EventFragment


@Module
abstract class FragmentBindingModule {
    @ContributesAndroidInjector(modules = [ EventFragment.DaggerModule::class ])
    abstract fun eventFragment(): EventFragment
}
