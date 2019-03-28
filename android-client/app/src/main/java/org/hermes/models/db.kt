package org.hermes.models

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase

@Database(entities = arrayOf(User::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

//val db = Room.databaseBuilder(
//        applicationContext,
//        AppDatabase::class.java, "database-name"
//).build()
