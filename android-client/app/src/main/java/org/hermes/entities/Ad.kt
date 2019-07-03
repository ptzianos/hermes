package org.hermes.entities

import androidx.room.*
import androidx.annotation.NonNull

@Entity(
    tableName = "ads",
    foreignKeys = [
        ForeignKey(
                entity = User::class,
                parentColumns = ["uid"],
                childColumns = ["user_id"]
        )
    ]
)
data class Ad(
    // UUID assigned to this ad by the market
    @NonNull @ColumnInfo(name = "uuid") var uuid: String,
    // foreign key to the market that is connected to this
    @NonNull @ColumnInfo(name = "user_id") var userId: Int,
    // block-chain used to store the data
    @NonNull @ColumnInfo(name = "network") var network: String,
    @NonNull @ColumnInfo(name = "currency") var currency: String
) {
    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int? = null
}