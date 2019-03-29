package org.hermes.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull

@Entity(tableName = "marketplaces")
data class Marketplace(
    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int,
    @NonNull @ColumnInfo(name = "host") var host: String
)