package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import dagger.Module
import dagger.android.AndroidInjection
import java.util.regex.Pattern
import javax.inject.Inject

import org.hermes.BaseActivity
import org.hermes.HermesRepository
import org.hermes.R
import org.hermes.entities.Event
import org.threeten.bp.format.DateTimeFormatter


class EventActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "EventActivity"

    @Inject
    lateinit var repository: HermesRepository

    private lateinit var eventIdWidget: TextView
    private lateinit var eventResource: TextView
    private lateinit var eventAction: TextView
    private lateinit var eventResourceId: EditText
    private lateinit var eventDate: EditText
    private lateinit var eventExtraInfo: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        eventIdWidget = findViewById(R.id.eventId)
        eventResource = findViewById(R.id.eventResource)
        eventAction = findViewById(R.id.eventAction)
        eventResourceId = findViewById(R.id.eventResourceId)
        eventDate = findViewById(R.id.eventDate)
        eventExtraInfo = findViewById(R.id.eventExtraInfo)

        val extras = intent.extras
        if (extras != null) {
            val eventId = extras.getInt("eventId", -1)
            if (eventId == -1) {
                Toast.makeText(this, "No event provided", Toast.LENGTH_LONG)
                    .show()
                startActivity(Intent(this, EventLogActivity::class.java))
            } else {
                repository.fetchEvent(eventId) { bind(it) }
            }
        }
    }

    private fun bind(event: Event) {
        eventIdWidget.text = event.uid.toString()
        eventResource.text = event.resource
        eventResourceId.setText((event.resourceId ?: -1).toString())
        eventAction.text = event.action
        eventDate.setText(event.createdOn.format(DateTimeFormatter.RFC_1123_DATE_TIME))
        eventExtraInfo.setText(processText(event))
    }

    private fun processText(event: Event): String {
        if (event.resource == "iota" && event.action == "broadcast") {
            val transactionBroadcastMsgStrRegex = "([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12})\\s--\\s.*([9A-Z]{81})"
            val transactionAndBundleMsgStrRegex =
                "([0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12})\\s--\\s.*([9A-Z]{81})\\s.*([9A-Z]{81})"
            val transactionBroadcastMsgRegex = Regex(transactionBroadcastMsgStrRegex)
            val transactionAndBundleMsgRegex = Regex(transactionAndBundleMsgStrRegex)
            val isTransactionBroadcastMessage = event.extraInfo?.matches(transactionBroadcastMsgRegex) ?: false
            val isBundleConfirmationMessage = event.extraInfo?.matches(transactionAndBundleMsgRegex) ?: false
            if (isTransactionBroadcastMessage) {
                val pattern = Pattern.compile(transactionBroadcastMsgStrRegex)
                val match = pattern.matcher(event.extraInfo ?: "")
                if (match.find())
                    return "Sensor UUID: ${match.group(1)}\n" +
                            "https://thetangle.org/address/${match.group(3)}"
            } else if (isBundleConfirmationMessage) {
                val pattern = Pattern.compile(transactionBroadcastMsgStrRegex)
                val match = pattern.matcher(event.extraInfo ?: "")
                if (match.find())
                    return "Sensor UUID: ${match.group(1)}" +
                            "\nhttps://thetangle.org/bundle/${match.group(3)}" +
                            "\nhttps://thetangle.org/address/${match.group(4)}"
            }
        }
        return event.extraInfo ?: ""
    }
}