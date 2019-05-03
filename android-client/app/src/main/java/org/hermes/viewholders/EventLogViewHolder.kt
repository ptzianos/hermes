package org.hermes.viewholders

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.threeten.bp.format.DateTimeFormatter

import org.hermes.R
import org.hermes.activities.EventActivity
import org.hermes.entities.Event


class EventLogViewHolder(val parent : View) : ViewHolder(parent), View.OnClickListener {

    private val eventIdWidget: TextView = parent.findViewById(R.id.eventId)
    private val eventResource: TextView = parent.findViewById(R.id.eventResource)
    private val eventAction: TextView = parent.findViewById(R.id.eventAction)
    private val eventResourceId: TextView = parent.findViewById(R.id.eventResourceId)
    private val eventDate: TextView = parent.findViewById(R.id.eventDate)
    private lateinit var context: Context
    private lateinit var item: Event

    fun bind(item: Event, context: Context) {
        eventIdWidget.text = item.uid.toString()
        eventResource.text = item.resource
        eventAction.text = item.action

        eventResourceId.text = (item.resourceId ?: -1).toString()

        eventDate.text = item.createdOn.format(DateTimeFormatter.RFC_1123_DATE_TIME)

        this.context = context
        this.item = item
        parent.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        context.startActivity(Intent(context, EventActivity::class.java).apply {
            putExtra("eventId", item.uid)
        })
    }
}