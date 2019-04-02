package org.hermes.adapters

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView


import org.hermes.R
import org.hermes.entities.Event
import org.hermes.fragments.EventFragment
import org.hermes.fragments.EventFragment.OnListFragmentInteractionListener
import org.hermes.utils.SQLiteTypeConverter

/**
 * [RecyclerView.Adapter] that can display a [Event] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class EventRecyclerViewAdapter(
        private val mValues: List<Event>,
        private val mListener: EventFragment.OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder>() {


    companion object {
        const val loggingTag = "EventRecyclerViewAdapt"
    }

//    private val mOnClickListener: View.OnClickListener

    init {
//        mOnClickListener = View.OnClickListener { v ->
//            val item = v.tag as Event
//            // Notify the active callbacks interface (the activity, if the fragment is attached to
//            // one) that an item has been selected.
//            // Maybe show the full info here?
//            mListener?.onListFragmentInteraction(item)
//        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.event_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d(loggingTag, "onBindViewHolder called")
        val item = mValues[position]

        holder.eventIdWidget.setText(item.uid)
        holder.eventResource.text = item.resource
        holder.eventAction.text = item.action
        if (item.resourceId != null) holder.eventResourceId.setText(item.resourceId as Int)
        holder.eventDate.setText(SQLiteTypeConverter.fromOffsetDateTime(item.createdOn))
        holder.eventExtraInfo.setText(item.extraInfo)

//        with(holder.mView) {
//            tag = item
//            setOnClickListener(mOnClickListener)
//        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(mView: View): RecyclerView.ViewHolder(mView) {

        val wrapper: RelativeLayout = mView.findViewById(R.id.eventListItemLayout)
        val eventIdWidget: EditText = mView.findViewById(R.id.eventId)
        val eventResource: TextView = mView.findViewById(R.id.eventResource)
        val eventAction: TextView = mView.findViewById(R.id.eventAction)
        val eventResourceId: EditText = mView.findViewById(R.id.eventResourceId)
        val eventDate: EditText = mView.findViewById(R.id.eventDate)
        val eventExtraInfo: EditText = mView.findViewById(R.id.eventExtraInfo)
    }
}
