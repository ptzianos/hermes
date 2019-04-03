package org.hermes

import android.app.Application
import android.arch.lifecycle.LiveData
import android.util.Log
import kotlinx.coroutines.*

import org.hermes.entities.Event
import org.hermes.iota.Seed
import java.lang.StringBuilder

import kotlin.coroutines.EmptyCoroutineContext
import android.arch.persistence.room.Room




class HermesRepository(application: Application) {

    private val loggingTag = "HermesRepository"

    private val db = Room.databaseBuilder(application, HermesRoomDatabase::class.java, "my-room-database")
                         .fallbackToDestructiveMigration()
                         .build()
    private var seed: Seed? = null

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: HermesRepository? = null

        fun getInstance(application: Application) =
            instance ?: synchronized(this) {
                instance ?: HermesRepository(application).also { instance = it }
            }
    }

    fun getEvents(): LiveData<List<Event>> {
        return db.eventDao().getAllLive()
    }

    /**
     * Insert some events in a background thread.
     *
     * Doing an insert in the UI thread would throw an exception.
     */
    fun insertEvent(vararg events: Event) {
        CoroutineScope(Dispatchers.IO).launch(EmptyCoroutineContext, CoroutineStart.DEFAULT) {
            db.eventDao().insertAll(*events)
        }
    }

    fun handoverSeed(seed: Seed) {
        this.seed = seed
    }
}
