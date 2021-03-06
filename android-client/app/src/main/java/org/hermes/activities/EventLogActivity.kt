package org.hermes.activities

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.android.AndroidInjection
import javax.inject.Inject

import org.hermes.BaseActivity
import org.hermes.HermesClientApp
import org.hermes.R
import org.hermes.adapters.PagedEventViewAdapter
import org.hermes.entities.Event
import org.hermes.viewmodels.EventLogViewModel


class EventLogActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "EventLogActivity"

    lateinit var viewModel: EventLogViewModel

    @Inject
    lateinit var hermesApplication: HermesClientApp

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        viewModel = hermesApplication.daggerHermesComponent.getEventLogViewModel()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)

        viewModel.allEvents.observe(this, Observer<PagedList<Event>?> {
            (findViewById<RecyclerView>(R.id.eventRecyclerView).adapter as PagedEventViewAdapter).submitList(it)
        })
    }
}
