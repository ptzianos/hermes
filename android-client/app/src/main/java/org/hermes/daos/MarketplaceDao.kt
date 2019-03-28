package org.hermes.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.hermes.entities.Ad
import org.hermes.entities.Marketplace


@Dao
interface MarketplaceDao {

    @Query("SELECT * FROM marketplaces")
    fun getAll(): List<Marketplace>

    @Insert
    fun insertAll(vararg ads: Ad)

    @Delete
    fun delete(ad: Ad)
}