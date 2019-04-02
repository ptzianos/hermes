package org.hermes.entities

import android.arch.persistence.room.*
import android.support.annotation.NonNull


@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Marketplace::class,
            parentColumns = ["uid"],
            childColumns = ["market_id"]
        )
    ],
    indices = [Index(value=["market_id"], name="user_market_index")]
)
data class User(
    @NonNull @ColumnInfo(name = "uuid") var uuid: String,
    @ColumnInfo(name = "name") var name: String? = null,
    @ColumnInfo(name = "full_name") var fullName: String? = null,
    @NonNull @ColumnInfo(name = "market_id") var marketId: Int
) {
    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int = -1
}
