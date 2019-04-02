package org.hermes.activities

import android.os.Bundle
import android.app.Activity

import org.hermes.R

class EventActivity : Activity() {

    companion object {
        const val loggingTag = "EventLogActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event_list)
    }
}
