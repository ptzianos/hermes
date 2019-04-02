package org.hermes

import android.app.Application
import android.arch.lifecycle.LiveData
import kotlinx.coroutines.*
import org.hermes.daos.EventDao
import org.hermes.entities.Event
import org.hermes.iota.Seed

import kotlin.coroutines.EmptyCoroutineContext


class HermesRepository(application: Application) {

    val db: HermesRoomDatabase = HermesRoomDatabase.getDatabase(application)
    val eventDao: EventDao = db.eventDao()
    var seed: Seed? = null

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: HermesRepository? = null

        fun getInstance(application: Application) =
            instance ?: synchronized(this) {
                instance ?: HermesRepository(application).also { instance = it }
            }
    }

    fun getEvents(): LiveData<List<Event>> {
        return eventDao.getAll()
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
