package org.hermes.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.android.AndroidInjection
import javax.inject.Inject

import org.hermes.adapters.EventRecyclerViewAdapter
import org.hermes.entities.Event
import org.hermes.HermesClientApp
import org.hermes.R
import org.hermes.viewmodels.EventViewModel


class EventActivity : AppCompatActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "EventLogActivity"

    lateinit var viewModel: EventViewModel

    @Inject
    lateinit var hermesApplication: HermesClientApp

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        viewModel = hermesApplication.daggerHermesComponent.getEventViewModel()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        // TODO: Change the id to something more suitable
        val recyclerView: RecyclerView = findViewById(R.id.fragment)
        val adapter = EventRecyclerViewAdapter()

        recyclerView.adapter = adapter

        viewModel.allEvents.observe(this, Observer<PagedList<Event>?> {
            Log.i(loggingTag,"Observed change of data")
            adapter.submitList(it)
        })
    }
}
