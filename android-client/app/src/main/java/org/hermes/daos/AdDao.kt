package org.hermes.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.hermes.entities.Ad

@Dao
interface AdDao {
    @Query("SELECT * FROM ads")
    fun getAll(): List<Ad>

    @Query("SELECT * FROM ads WHERE uid == :userId")
    fun findByIds(userId: Int): Ad

    @Query("SELECT * FROM ads WHERE market_id == :marketId")
    fun findByMarket(marketId: Int): Ad

    @Insert
    fun insertAll(vararg ads: Ad)

    @Delete
    fun delete(ad: Ad)
}