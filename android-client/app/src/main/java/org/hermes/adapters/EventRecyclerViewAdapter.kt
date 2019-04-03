package org.hermes.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import java.util.*


import org.hermes.R
import org.hermes.entities.Event
import org.hermes.fragments.EventFragment.OnListFragmentInteractionListener
import org.hermes.utils.SQLiteTypeConverter

/**
 * [RecyclerView.Adapter] that can display a [Event] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class EventRecyclerViewAdapter(context: Context): RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder>() {

    companion object {
        const val loggingTag = "EventRecyclerViewAdapt"
    }

    val mInflater = LayoutInflater.from(context)
    var mEvents: List<Event> = Collections.emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater
            .from(parent.context)
            .inflate(R.layout.event_list_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(loggingTag, "onBindViewHolder called")

        val item = mEvents.getOrNull(position) ?: return

        holder.eventIdWidget.setText(item.uid.toString())
        holder.eventResource.text = item.resource
        holder.eventAction.text = item.action

        holder.eventResourceId.setText((item.resourceId ?: -1).toString())

        holder.eventDate.setText(SQLiteTypeConverter.fromOffsetDateTime(item.createdOn))
        holder.eventExtraInfo.setText(item.extraInfo)
    }

    fun setEvents(events: List<Event>) {
        mEvents = events
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = mEvents.size

    inner class ViewHolder(mView: View): RecyclerView.ViewHolder(mView) {
        val eventIdWidget: EditText = mView.findViewById(R.id.eventId)
        val eventResource: TextView = mView.findViewById(R.id.eventResource)
        val eventAction: TextView = mView.findViewById(R.id.eventAction)
        val eventResourceId: EditText = mView.findViewById(R.id.eventResourceId)
        val eventDate: EditText = mView.findViewById(R.id.eventDate)
        val eventExtraInfo: EditText = mView.findViewById(R.id.eventExtraInfo)
    }
}
