package org.hermes.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import org.hermes.entities.Ad

@Dao
interface AdDao {
    @Query("SELECT * FROM ads")
    fun getAll(): List<Ad>

    @Query("SELECT * FROM ads WHERE uid = :adId")
    fun findById(adId: Int): Ad

    @Query("SELECT * FROM ads WHERE user_id = :userId")
    fun findByUserId(userId: Int): Ad?

    @Insert
    fun insertAll(vararg ads: Ad)

    @Delete
    fun delete(ad: Ad)
}