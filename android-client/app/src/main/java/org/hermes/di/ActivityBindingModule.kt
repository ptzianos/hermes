package org.hermes.di

import dagger.android.ContributesAndroidInjector
import dagger.Module
import org.hermes.activities.EventActivity

import org.hermes.activities.EventLogActivity
import org.hermes.activities.LoginActivity
import org.hermes.activities.SetupActivity


@Module
abstract class ActivityBindingModule {

    @ContributesAndroidInjector(modules = [LoginActivity.DaggerModule::class])
    abstract fun loginActivity(): LoginActivity

    @ContributesAndroidInjector(modules = [SetupActivity.DaggerModule::class])
    abstract fun setupActivity(): SetupActivity

    @ContributesAndroidInjector(modules = [EventLogActivity.DaggerModule::class])
    abstract fun eventLogActivity(): EventLogActivity

    @ContributesAndroidInjector(modules = [EventActivity.DaggerModule::class])
    abstract fun eventActivity(): EventActivity

}