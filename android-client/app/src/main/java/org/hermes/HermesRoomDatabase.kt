package org.hermes

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import org.hermes.daos.*
import org.hermes.entities.*
import org.hermes.utils.SQLiteTypeConverter


@Database(
    entities = [Ad::class, Event::class, Marketplace::class, Sensor::class, User::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(SQLiteTypeConverter::class)
abstract class HermesRoomDatabase : RoomDatabase() {
    abstract fun adDao(): AdDao
    abstract fun eventDao(): EventDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun userDao(): UserDao
    abstract fun sensorDao(): SensorDao
}