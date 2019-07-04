package org.hermes.entities

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.lang.StringBuilder
import org.threeten.bp.OffsetDateTime

import org.hermes.utils.SQLiteTypeConverter

@Entity(tableName = "events")
class Event(
    @ColumnInfo(name = "action") var action: String,
    @ColumnInfo(name = "resource") var resource: String,
    @ColumnInfo(name = "resource_id") var resourceId: Int? = null,
    @ColumnInfo(name = "extra_info") var extraInfo: String?,
    @NonNull @ColumnInfo(name = "created_on") val createdOn: OffsetDateTime = OffsetDateTime.now()
) {

    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int? = null

    override fun toString(): String {
        return StringBuilder()
                .append(SQLiteTypeConverter.fromOffsetDateTime(createdOn))
                .append(" : ")
                .append(action)
                .append(" : ")
                .append(resource)
                .append("::")
                .append(resourceId ?: -1)
                .append(" : ")
                .append(extraInfo ?: "")
                .toString()
    }
    
    override fun equals(other: Any?): Boolean {
        if (other != null && other is Event)
            return this.action == other.action && this.createdOn == other.createdOn &&
                    this.resource == other.resource && this.resourceId == other.resourceId &&
                    this.uid == other.uid
        return false
    }

    override fun hashCode(): Int {
        var result = action.hashCode()
        result = 31 * result + resource.hashCode()
        result = 31 * result + (resourceId ?: 0)
        result = 31 * result + (extraInfo?.hashCode() ?: 0)
        result = 31 * result + createdOn.hashCode()
        result = 31 * result + (uid ?: 0)
        return result
    }
}