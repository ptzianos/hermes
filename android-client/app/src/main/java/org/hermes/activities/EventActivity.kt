package org.hermes.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView

import org.hermes.adapters.EventRecyclerViewAdapter
import org.hermes.HermesRepository
import org.hermes.R
import org.hermes.entities.Event
import org.hermes.viewmodels.EventViewModel


class EventActivity : AppCompatActivity() {

    companion object {
        const val loggingTag = "EventLogActivity"
    }

    private val repository by lazy { HermesRepository.getInstance(application) }
    private val mEventViewModel by lazy { ViewModelProviders.of(this).get(EventViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        // TODO: Change the id to something more suitable
        val recyclerView: RecyclerView = findViewById(R.id.fragment)
        val adapter = EventRecyclerViewAdapter()

        recyclerView.adapter = adapter

        // Subscribe the adapter to the ViewModel, so the items in the adapter are refreshed
        // when the list changes
        mEventViewModel.allEvents.observe(this, Observer<PagedList<Event>?> {
            Log.i(loggingTag,"Observed change of data")
            adapter.submitList(it)
//            adapter.notifyDataSetChanged()
        })
    }
}
