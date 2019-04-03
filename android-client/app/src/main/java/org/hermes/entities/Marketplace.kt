package org.hermes.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.NonNull

@Entity(tableName = "marketplaces")
data class Marketplace(
    @NonNull @ColumnInfo(name = "host") var host: String
) {
    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int? = null
}