package org.hermes.entities

import android.arch.persistence.room.*
import android.support.annotation.NonNull

@Entity(
    tableName = "ads",
    foreignKeys = [
        ForeignKey(
                entity = Marketplace::class,
                parentColumns = ["uid"],
                childColumns = ["market_id"]
        )
    ],
    indices = [Index(value=["market_id"], name="ad_market_index")]
)
data class Ad(
    @NonNull @ColumnInfo(name = "uuid") var uuid: String,
    @NonNull @ColumnInfo(name = "network") var network: String,
    @NonNull @ColumnInfo(name = "currency") var currency: String,
    @ColumnInfo(name = "lower_left_x") var lowerLeftX: Int? = null,
    @ColumnInfo(name = "lower_left_y") var lowerLeftY: Int? = null,
    @ColumnInfo(name = "width") var width: Int? = null,
    @ColumnInfo(name = "height") var height: Int? = null,
    @NonNull @ColumnInfo(name = "market_id") var marketId: Int
) {
    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int? = null
}