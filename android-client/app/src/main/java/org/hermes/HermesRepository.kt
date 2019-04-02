package org.hermes

import android.app.Application
import android.arch.lifecycle.LiveData
import dagger.Reusable
import kotlinx.coroutines.*
import org.hermes.daos.EventDao
import org.hermes.entities.Event
import kotlin.coroutines.EmptyCoroutineContext


// @Singleton
// Make this a singleton and an interface so that it can be replaced during tests?
// or just make it a singleton and inject the sources of datas?
@Reusable
class HermesRepository(application: Application) {

    val db: HermesRoomDatabase = HermesRoomDatabase.getDatabase(application)
    val eventDao: EventDao = db.eventDao()

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
}
