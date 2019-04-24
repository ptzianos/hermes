package org.hermes.viewholders

import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import org.hermes.LedgerService

import org.hermes.R


class SensorListViewHolder(parent : View) : ViewHolder(parent) {

    private val sensorActiveImg: ImageView = parent.findViewById(R.id.sensorActive)
    private val sensorId: TextView = parent.findViewById(R.id.sensorId)
    private val sensorUUID: TextView = parent.findViewById(R.id.sensorUUID)

    fun bind(item: LedgerService.Sensor, context: Context) {
        if (item.active) {
            sensorActiveImg.setImageResource(R.drawable.green_circle)
        } else {
            sensorActiveImg.setImageResource(R.drawable.red_circle)
        }
        sensorId.text = item.uuid
        sensorUUID.text = item.dataId
    }
}