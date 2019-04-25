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
import javax.inject.Inject

import org.hermes.HermesClientApp
import org.hermes.R
import org.hermes.adapters.SensorListViewAdapter
import org.hermes.viewmodels.SensorListViewModel


class SensorListFragment : Fragment() {

    @Module
    abstract class DaggerModule

    private var columnCount = 1

    @Inject
    lateinit var application: HermesClientApp

    lateinit var viewModel: SensorListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { columnCount = 1 }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return (inflater.inflate(R.layout.event_list_fragment, container, false) as RecyclerView)
            .let {
                it.layoutManager = LinearLayoutManager(context)
                it.adapter = SensorListViewAdapter(context as Context)
                it
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = application.daggerHermesComponent.getSensorListViewModel()
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }
}
