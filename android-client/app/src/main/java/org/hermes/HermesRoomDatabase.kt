package org.hermes

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.hermes.daos.AdDao
import org.hermes.daos.EventDao
import org.hermes.daos.MarketplaceDao
import org.hermes.daos.UserDao
import org.hermes.entities.Ad
import org.hermes.entities.Event
import org.hermes.entities.Marketplace
import org.hermes.entities.User
import org.hermes.utils.SQLiteTypeConverter
import javax.inject.Singleton
import android.arch.persistence.room.Room
import android.content.Context

@Singleton
@Database(
    entities = [Ad::class, Event::class, Marketplace::class, User::class],
    version = 1
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
        fun getDatabase(context: Context): HermesRoomDatabase {
            if (INSTANCE == null) {
                synchronized(HermesRoomDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                HermesRoomDatabase::class.java, "hermes_database")
                                .build()
                    }
                }
            }
            return INSTANCE as HermesRoomDatabase
        }
    }
}