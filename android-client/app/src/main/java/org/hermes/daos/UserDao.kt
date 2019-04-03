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

    @Query("SELECT * FROM users WHERE uid == :userId")
    fun findByIds(userId: Int): User

    @Query("SELECT * FROM users WHERE market_id == :marketId")
    fun findByMarket(marketId: Int): User

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}