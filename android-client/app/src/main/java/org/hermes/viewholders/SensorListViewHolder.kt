package org.hermes.viewholders

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import org.hermes.R
import org.hermes.activities.SensorActivity
import org.hermes.entities.Sensor


class SensorListViewHolder(val parent : View) :
    ViewHolder(parent), View.OnClickListener {

    private val sensorActiveImg: ImageView = parent.findViewById(R.id.sensorActive)
    private val sensorId: TextView = parent.findViewById(R.id.sensorUUID)
    private val sensorUUID: TextView = parent.findViewById(R.id.sensorID)
    private lateinit var mItem: Sensor
    private lateinit var context: Context

    fun bind(item: Sensor, context: Context) {
        sensorActiveImg.setImageResource(when(item.active.get()) {
            true -> R.drawable.green_circle
            false -> R.drawable.red_circle
        })
        sensorId.text = item.uuid
        sensorUUID.text = item.dataId
        mItem = item
        this.context = context
        this.parent.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        context.startActivity(Intent(context, SensorActivity::class.java).apply {
            putExtra("sensorId", mItem.dataId)
        })
    }
}