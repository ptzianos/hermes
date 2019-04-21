package org.hermes.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil

import org.hermes.R
import org.hermes.entities.Event
import org.hermes.fragments.EventLogFragment.OnListFragmentInteractionListener
import org.hermes.viewholders.EventLogViewHolder

/**
 * [RecyclerView.Adapter] that can display a [Event] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class PagedEventViewAdapter: PagedListAdapter<Event, EventLogViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Event>() {
            override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem.uid == newItem.uid

            override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean = oldItem.equals(newItem)
        }
    }

    val loggingTag = "PagedEventViewAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventLogViewHolder {
        return EventLogViewHolder(LayoutInflater.from(parent.context)
                                             .inflate(R.layout.event_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: EventLogViewHolder, position: Int) {
        Log.d(loggingTag, "onBindViewHolder called")
        val item = getItem(position) ?: return
        holder.bind(item)
    }
}
