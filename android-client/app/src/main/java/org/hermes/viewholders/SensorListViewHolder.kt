package org.hermes.viewholders

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

import org.hermes.LedgerService
import org.hermes.R
import org.hermes.activities.SensorActivity


class SensorListViewHolder(val parent : View) :
    ViewHolder(parent), View.OnClickListener {

    private val sensorActiveImg: ImageView = parent.findViewById(R.id.sensorActive)
    private val sensorId: TextView = parent.findViewById(R.id.sensorUUID)
    private val sensorUUID: TextView = parent.findViewById(R.id.sensorID)
    private lateinit var mItem: LedgerService.Sensor
    private lateinit var context: Context

    fun bind(item: LedgerService.Sensor, context: Context) {
        if (item.active) {
            sensorActiveImg.setImageResource(R.drawable.green_circle)
        } else {
            sensorActiveImg.setImageResource(R.drawable.red_circle)
        }
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