package org.hermes.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

import org.hermes.HermesRoomDatabase
import org.hermes.R


@Module
class HermesModule {

    @Provides @Singleton fun providesDatabase(application: Application) =
        Room.databaseBuilder(application, HermesRoomDatabase::class.java, "hermes_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton fun providesSharedPreferences(application: Application): SharedPreferences {
        return application.getSharedPreferences(
            application.getString(R.string.auth_preference_key),
            Context.MODE_PRIVATE)
    }
}