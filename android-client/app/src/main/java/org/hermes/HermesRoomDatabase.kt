package org.hermes

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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
    version = 2,
    exportSchema = true
)
@TypeConverters(SQLiteTypeConverter::class)
abstract class HermesRoomDatabase : RoomDatabase() {
    abstract fun adDao(): AdDao
    abstract fun eventDao(): EventDao
    abstract fun marketplaceDao(): MarketplaceDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        @JvmStatic
        private var INSTANCE: HermesRoomDatabase? = null

        // TODO: This needs to be injected using Dagger
        @JvmStatic
        fun getDatabase(applicationContext: Context): HermesRoomDatabase {
            if (INSTANCE == null) {
                synchronized(HermesRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(applicationContext,
                                                        HermesRoomDatabase::class.java,
                                                        "hermes_database")
                                       .fallbackToDestructiveMigration()
                                       .build()
                    }
                }
            }
            return INSTANCE as HermesRoomDatabase
        }
    }
}