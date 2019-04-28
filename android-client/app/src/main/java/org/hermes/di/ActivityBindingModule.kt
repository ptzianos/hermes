package org.hermes.di

import dagger.android.ContributesAndroidInjector
import dagger.Module
import org.hermes.activities.*


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

    @ContributesAndroidInjector(modules = [SensorListActivity.DaggerModule::class])
    abstract fun sensorListActivity(): SensorListActivity

    @ContributesAndroidInjector(modules = [SensorActivity.DaggerModule::class])
    abstract fun sensorActivity(): SensorActivity

    @ContributesAndroidInjector(modules = [DrawerActivity.DaggerModule::class])
    abstract fun drawerActivity(): DrawerActivity
}