package org.hermes.daos

import androidx.room.*
import org.hermes.entities.Sensor

@Dao
interface SensorDao {
    @Query("SELECT * FROM sensors")
    fun getAll(): List<Sensor>

    @Query("SELECT * FROM sensors where uuid = :uuid")
    fun getByUUID(uuid: String): Sensor?

    @Query("SELECT * FROM sensors where data_id = :tag")
    fun getByTag(tag: String): Sensor?

    @Insert
    fun insertAll(vararg sensors: Sensor)

    @Delete
    fun delete(sensor: Sensor)

    @Query("UPDATE sensors SET root_address = :rootAddress WHERE uuid = :uuid")
    fun updateRootAddress(uuid: String, rootAddress: String)

    @Query("UPDATE sensors SET latest_address = :latestAddress WHERE uuid = :uuid")
    fun updateLatestAddress(uuid: String, latestAddress: String)

    @Query("UPDATE sensors SET latest_address_index = :latestAddressIndex WHERE uuid = :uuid")
    fun updateLatestAddressIndex(uuid: String, latestAddressIndex: Int)
}