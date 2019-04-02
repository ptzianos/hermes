package org.hermes.viewmodels

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData

import org.hermes.HermesRepository
import org.hermes.entities.Event


class EventViewModel(application: Application): AndroidViewModel(application) {

    private val hermesRepository = HermesRepository(application)

    fun getEvents(): LiveData<List<Event>> {
        return hermesRepository.getEvents()
    }

    fun insertEvents(vararg events: Event) {
        hermesRepository.insertEvent(*events)
    }
}
