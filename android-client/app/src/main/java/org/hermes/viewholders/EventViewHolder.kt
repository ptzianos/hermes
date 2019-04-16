package org.hermes.viewholders

import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.threeten.bp.format.DateTimeFormatter

import org.hermes.R
import org.hermes.entities.Event


class EventViewHolder(parent : View) : ViewHolder(parent) {

    private val eventIdWidget: TextView = parent.findViewById(R.id.eventId)
    private val eventResource: TextView = parent.findViewById(R.id.eventResource)
    private val eventAction: TextView = parent.findViewById(R.id.eventAction)
    private val eventResourceId: EditText = parent.findViewById(R.id.eventResourceId)
    private val eventDate: EditText = parent.findViewById(R.id.eventDate)
    private val eventExtraInfo: EditText = parent.findViewById(R.id.eventExtraInfo)
    private val eventArrow: ImageButton = parent.findViewById(R.id.eventArrow)

    fun bind(item: Event) {
        eventIdWidget.text = item.uid.toString()
        eventResource.text = item.resource
        eventAction.text = item.action

        eventResourceId.setText((item.resourceId ?: -1).toString())

        eventDate.setText(item.createdOn.format(DateTimeFormatter.RFC_1123_DATE_TIME))
        eventExtraInfo.setText(item.extraInfo)
    }

}