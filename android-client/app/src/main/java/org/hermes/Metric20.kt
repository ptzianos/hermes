package org.hermes

import java.lang.Exception
import org.threeten.bp.OffsetDateTime

/**
 * An implementation of the Metrics2.0 spec for the Hermes client
 * More info at: @see http://metrics20.org/spec/
 *
 * There are two tags that are mandatory:  unit and mtype.
 */
class Metric20(private val key: String,
               private val value: Any,
               private val timestamp: OffsetDateTime = OffsetDateTime.now()) {

    class NoData: Exception()
    class ForbiddenKey: Exception()
    class KeysNotPresent : Exception()

    enum class TagKey(val tagKey: String) {
        HOST("host"),               // physical or virtual machine
        HTTP_METHOD("http_method"), // the http method. like PUT, GET, etc.
        HTTP_CODE("http_code"),     // 200, 404, etc
        DEVICE("device"),           // block device, network device, …
        UNIT("unit"),               // the unit something is expressed in (b/s, MB, etc).
        WHAT("what"),               // the thing being measured, if the other tags don’t suffice. often same as metric key.
        TYPE("type"),               // further describe the metric. type is a very generic word, only use it if you really don’t know anything better.
        RESULT("result"),           // values: ok, fail, … (for http requests, http_code is probably more useful)
        STAT("stat"),               // to clarify the statistical view
        BIN_MAX("bin_max"),         // if your metrics are separated into bins by some numeric value, upper limit of a bin (like (statsd) histograms)
        DIRECTION("direction"),     // in/out (not ‘tx’ or ‘rx’, more consistent)
        MTYPE("mtype"),             // type of metric in terms of how the data should be interpreted.
        FILE("file"),               // file (that generated a metric)
        LINE("line"),               // line (that generated a metric)
        ENV("env")                  // environment
    }

    private val meta: HashMap<String, String> = HashMap()
    private val data: HashMap<String, String> = HashMap()

    init {
        meta["processed_by"] = "hermes"
    }

    fun setData(key: Metric20.TagKey, value: String): Metric20 {
        data[key.tagKey] = value
        return this
    }

    fun setMetadata(key: String, value: String): Metric20 {
        if (key == "processed_by")
            throw ForbiddenKey()
        this.meta[key] = value
        return this
    }

    fun toJsonString(): String {
        fun mapToJson(mapping: Map<String, String>): String = mapping
            .map { "\"${it.key}\": \"${it.value}\"" }
            .joinToString()

        if (data.isEmpty()) {
            throw NoData()
        } else if (!data.containsKey("unit") || !data.containsKey("mtype")) {
            throw KeysNotPresent()
        }
        return "{" + mapToJson(data) + ", \"meta\": {" + mapToJson(meta) + "}}"
    }

    /**
     * Carbon2.0 is a format that is compatible with with all Carbon-related daemons but it
     * is also easily convertible to Metrics 2.0 format.
     */
    fun toCarbon20String(): String {
        fun mapToJson(mapping: Map<String, String>, keyPrefix: String = ""): String = mapping
            .map { "$keyPrefix${it.key}=${it.value}" }
            .joinToString(separator = ";")

        if (data.isEmpty()) {
            throw NoData()
        } else if (!data.containsKey("unit") || !data.containsKey("mtype")) {
            throw KeysNotPresent()
        }
        return "$key;${mapToJson(data)};${mapToJson(meta, "meta.")} ${timestamp.toEpochSecond()} $value"
    }
}