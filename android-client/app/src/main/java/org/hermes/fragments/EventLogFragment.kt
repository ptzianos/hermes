package org.hermes.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

import org.hermes.HermesClientApp
import org.hermes.R
import org.hermes.adapters.PagedEventViewAdapter
import org.hermes.entities.Event
import org.hermes.viewmodels.EventLogViewModel


class EventLogFragment @Inject constructor() : Fragment() {

    @Module
    abstract class DaggerModule

    private var columnCount = 1

    @Inject
    lateinit var application: HermesClientApp

    @Inject
    lateinit var eventLogViewModel: EventLogViewModel

    private lateinit var mView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { columnCount = 1 }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mView = (inflater.inflate(R.layout.event_list_fragment, container, false) as RecyclerView)
            .let {
                it.layoutManager = LinearLayoutManager(context)
                it.adapter = PagedEventViewAdapter(context as Context)
                it
            }
        return mView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        eventLogViewModel.allEvents.observe(this, Observer<PagedList<Event>?> {
            (mView.findViewById<RecyclerView>(R.id.eventRecyclerView).adapter as PagedEventViewAdapter).submitList(it)
        })
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
