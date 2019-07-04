package org.hermes.entities

import android.util.Log
import androidx.room.*
import androidx.annotation.NonNull
import org.hermes.Metric20
import org.hermes.utils.AtomicLiveBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@Entity(
    tableName = "sensors",
    indices = [
        Index(value = ["uuid"], unique = true),
        Index(value = ["data_id"], unique = true)
    ]
)
open class Sensor(@NonNull @ColumnInfo(name = "uuid") var uuid: String,
                  @NonNull @ColumnInfo(name = "data_id") var dataId: String,
                  @NonNull @ColumnInfo(name = "unit") var unit: String,
                  @NonNull @ColumnInfo(name = "mtype") var mtype: String,
                  @NonNull @ColumnInfo(name = "what") var what: String?,
                  @NonNull @ColumnInfo(name = "device") var device: String?,
                  @NonNull @ColumnInfo(name = "root_address") var rootAddress: String?,
                  @NonNull @ColumnInfo(name = "latest_address") var latestAddress: String?
) {

    companion object {
        const val SENSOR_BUFFER_SIZE = 100
    }

    // Unique ID of the row
    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int? = null

    @Ignore private val loggingTag = "Sensor"

    // Number of samples in the buffer
    @Ignore var counter = 0
        private set
    // Place in the buffer where the earliest sample is located
    @Ignore private var start = 0
    // Place in the buffer where the latest sample is located
    @Ignore private var end = -1
    // Lock used to synchronize access to the sensor buffer
    @Ignore private val lock = ReentrantLock()
    // Buffer used to hold samples
    @Ignore private var buffer = Array<Metric20?>(SENSOR_BUFFER_SIZE) { null }
    @Ignore var active: AtomicLiveBoolean = AtomicLiveBoolean(false)

    private fun clear() {
        start = 0
        end = -1
        counter = 0
    }

    fun returnSamples(metrics: Array<Metric20?>) = metrics.forEach { if (it != null) returnSample(it) }

    /**
     * Return old samples back to the buffer.
     * Contrary to the putSample method, this method will add the samples from the beginning
     * and backwards, so that when the data are flushed again, they will be in chronological
     * order.
     */
    fun returnSample(metric: Metric20) {
        fun dec(i: Int, mod: Int): Int = if (i == 0) mod - 1 else i - 1
        val newStart = dec(start, SENSOR_BUFFER_SIZE)
        if (newStart == end) return
        counter++
        start = newStart
        buffer[start] = metric
    }

    /**
     * Put a new sample in the buffer.
     * The buffer is a ring buffer so if the client exceeds the max
     * number of samples, the oldest sample will be overwritten.
     */
    fun putSample(metric: Metric20) = lock.withLock {
        val inc = fun(i: Int): Int = (i + 1) % SENSOR_BUFFER_SIZE
        end = inc(end)
        buffer[end] = metric
        start = if (end == start && counter > 0) inc(start) else start
        counter = if (counter < SENSOR_BUFFER_SIZE) counter + 1 else counter
        Log.d(loggingTag, "Putting new sample at pos: $end, counter: $counter")
    }

    /**
     * Return a buffer with all the samples in the correct order and clear the original buffer
     */
    fun flushData(): Array<Metric20?> = lock.withLock {
        val chunk = when {
            counter == 0 -> Array<Metric20?> (0) { null }
            start < end -> buffer.sliceArray(start .. end)
            else -> buffer.sliceArray(start until SENSOR_BUFFER_SIZE) + buffer.sliceArray(0 .. end)
        }
        clear()
        chunk
    }
}