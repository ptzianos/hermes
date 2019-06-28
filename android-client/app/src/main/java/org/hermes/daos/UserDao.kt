package org.hermes.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import org.hermes.entities.User


@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): List<User>

    @Query("SELECT * FROM users WHERE uid = :userId")
    fun findById(userId: Int): User

    @Query("SELECT * FROM users WHERE domain = :domain")
    fun findByMarket(domain: String): User?

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)

    @Query("DELETE FROM users WHERE domain = :domain")
    fun deleteByDomain(domain: String)

    @Query("UPDATE users SET token = :token WHERE market_uuid = :uuid")
    fun updateToken(uuid: String, token: String)
}