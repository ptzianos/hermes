package org.hermes.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.hermes.service.LedgerService

@Module
abstract class ServiceBindingModule {

    @ContributesAndroidInjector(modules = [LedgerService.DaggerModule::class])
    abstract fun ledgerService(): LedgerService

}
