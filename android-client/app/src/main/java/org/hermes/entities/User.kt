package org.hermes.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull


@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Marketplace::class,
            parentColumns = arrayOf("uid"),
            childColumns = arrayOf("market_id")
        )
    ]
)
data class User(
    @PrimaryKey @ColumnInfo(name = "uid") var uid: Int,
    @NonNull @ColumnInfo(name = "uuid") var uuid: String,
    @ColumnInfo(name = "name") var name: String?,
    @ColumnInfo(name = "full_name") var fullName: String?,
    @NonNull @ColumnInfo(name = "market_id") var marketId: Int
)
