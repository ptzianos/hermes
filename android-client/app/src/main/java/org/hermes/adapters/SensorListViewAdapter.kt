package org.hermes.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

import org.hermes.LedgerService
import org.hermes.R
import org.hermes.entities.Sensor
import org.hermes.viewholders.SensorListViewHolder

/**
 * [RecyclerView.Adapter] that can display a [LedgerService.Sensor].
 */
class SensorListViewAdapter(val context: Context):
    ListAdapter<Sensor, SensorListViewHolder>(diffCallback) {

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Sensor>() {
            override fun areItemsTheSame(oldItem: Sensor, newItem: Sensor): Boolean =
                oldItem.dataId == newItem.dataId

            override fun areContentsTheSame(oldItem: Sensor, newItem: Sensor): Boolean =
                oldItem.equals(newItem)
        }
    }

    val loggingTag = "PagedSensorListViewAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorListViewHolder {
        return SensorListViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.sensor_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: SensorListViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bind(item, context)
    }
}
