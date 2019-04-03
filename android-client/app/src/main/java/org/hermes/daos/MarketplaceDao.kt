package org.hermes.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
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