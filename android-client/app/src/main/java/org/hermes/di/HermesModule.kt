package org.hermes.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import java.security.KeyStore
import javax.inject.Named
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

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

    @Provides @Singleton fun providesKeyStore(): KeyStore {
        return KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
        }
    }

    @Provides fun marketServiceApi(): Retrofit.Builder {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(
                OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build())
    }
}