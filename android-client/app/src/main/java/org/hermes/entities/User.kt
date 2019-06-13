package org.hermes.entities

import androidx.room.*
import androidx.annotation.NonNull


@Entity(tableName = "users")
data class User(
    @NonNull @ColumnInfo(name = "uuid") var uuid: String,
    @NonNull @ColumnInfo(name = "name") var name: String,
    @NonNull @ColumnInfo(name = "domain") var domain: String,
    @NonNull @ColumnInfo(name = "token") var token: String
) {
    @NonNull @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "uid") var uid: Int? = null
}
