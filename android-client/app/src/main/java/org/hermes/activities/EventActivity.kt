package org.hermes.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log

import org.hermes.adapters.EventRecyclerViewAdapter
import org.hermes.entities.Event
import org.hermes.HermesRepository
import org.hermes.R
import org.hermes.viewmodels.EventViewModel
import java.util.*


class EventActivity : AppCompatActivity() {

    companion object {
        const val loggingTag = "EventLogActivity"
    }

    private val repository by lazy { HermesRepository.getInstance(application) }
    private var mEventViewModel: EventViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        // TODO: Change the id to something more suitable
        val recyclerView: RecyclerView = findViewById(R.id.fragment)
        val adapter = EventRecyclerViewAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get a new or existing ViewModel from the ViewModelProvider.
        mEventViewModel = ViewModelProviders
            .of(this)
            .get(EventViewModel::class.java)

        mEventViewModel?.getEvents()?.observe(this, Observer<List<Event>> {
                Log.i(loggingTag, "onChanged called")
                if (it == null) {
                    adapter.setEvents(Collections.emptyList())
                } else {
                    adapter.setEvents(it)
                }
        })
    }
}
