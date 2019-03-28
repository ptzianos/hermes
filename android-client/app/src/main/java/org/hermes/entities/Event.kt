package org.hermes.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "events")
data class Event(
    @NonNull @PrimaryKey @ColumnInfo(name = "uid") var uid: Int,
    @ColumnInfo(name = "action") var action: String,
    @ColumnInfo(name = "resource") var resource: String,
    @ColumnInfo(name = "resource_id") var resourceId: Int?,
    @ColumnInfo(name = "extra_info") var extraInfo: String?,
    @NonNull @ColumnInfo(name = "created_on") val createdOn: OffsetDateTime? = null
)