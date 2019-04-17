package org.hermes.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

import org.hermes.HermesRoomDatabase
import org.hermes.R


@Module
class HermesModule {

    @Provides @Singleton fun providesDatabase(application: Application) =
        Room.databaseBuilder(application, HermesRoomDatabase::class.java, "hermes_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Named("auth") @Singleton fun providesAuthPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(
            application.getString(R.string.auth_preference_key),
            Context.MODE_PRIVATE)
    }

    @Provides @Named("iota") @Singleton fun providesIOTAPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(
            application.getString(R.string.iota_preference_key),
            Context.MODE_PRIVATE)
    }
}