package org.hermes

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

import org.hermes.daos.AdDao
import org.hermes.daos.EventDao
import org.hermes.daos.MarketplaceDao
import org.hermes.daos.UserDao
import org.hermes.entities.Ad
import org.hermes.entities.Event
import org.hermes.entities.Marketplace
import org.hermes.entities.User
import org.hermes.utils.SQLiteTypeConverter


@Database(
    entities = [Ad::class, Event::class, Marketplace::class, User::class],
    version = 5,
    exportSchema = true
)
@TypeConverters(SQLiteTypeConverter::class)
abstract class HermesRoomDatabase : RoomDatabase() {
    abstract fun adDao(): AdDao
    abstract fun eventDao(): EventDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun userDao(): UserDao
}