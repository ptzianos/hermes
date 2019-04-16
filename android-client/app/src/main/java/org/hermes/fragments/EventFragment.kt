package org.hermes.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.android.support.AndroidSupportInjection
import org.hermes.HermesClientApp

import org.hermes.adapters.PagedEventViewAdapter
import org.hermes.entities.Event
import org.hermes.R
import org.hermes.viewmodels.EventViewModel
import javax.inject.Inject


class EventFragment : Fragment() {

    @Module
    abstract class DaggerModule

    private var columnCount = 1

    @Inject
    lateinit var application: HermesClientApp

    lateinit var viewModel: EventViewModel

    private var listener: EventFragment.OnListFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { columnCount = 1 }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return (inflater.inflate(R.layout.event_list_fragment, container, false) as RecyclerView)
            .let {
                it.layoutManager = LinearLayoutManager(context)
                it.adapter = PagedEventViewAdapter()
                it
            }
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = application.daggerHermesComponent.getEventViewModel()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
//        if (context is EventFragment.OnListFragmentInteractionListener) {
//            listener = context
//        } else {
//            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
//        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnListFragmentInteractionListener {
        fun onListFragmentInteraction(item: Event?)
    }

}
