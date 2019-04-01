package org.hermes.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity(
    tableName = "ads",
    foreignKeys = [
        ForeignKey(
                entity = Marketplace::class,
                parentColumns = arrayOf("uid"),
                childColumns = arrayOf("market_id")
        )
    ]
)
data class Ad(
    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int,
    @NonNull @ColumnInfo(name = "uuid") var uuid: String,
    @NonNull @ColumnInfo(name = "network") var network: String,
    @NonNull @ColumnInfo(name = "currency") var currency: String,
    @ColumnInfo(name = "lower_left_x") var lowerLeftX: Int,
    @ColumnInfo(name = "lower_left_y") var lowerLeftY: Int,
    @ColumnInfo(name = "width") var width: Int,
    @ColumnInfo(name = "height") var height: Int,
    @NonNull @ColumnInfo(name = "market_id") var marketId: Int
)