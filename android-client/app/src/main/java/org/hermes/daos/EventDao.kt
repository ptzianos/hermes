package org.hermes.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.hermes.entities.Event
import org.threeten.bp.OffsetDateTime

@Dao
interface EventDao {
    @Query("SELECT * FROM events ORDER BY datetime(created_on)")
    fun getAll(): List<Event>

    @Query("SELECT * FROM events WHERE uid == :eventId")
    fun findById(eventId: Int): Event

    @Query("SELECT * FROM events WHERE datetime(created_on) >= :createdAfter")
    fun findAllAfterDate(createdAfter: OffsetDateTime)

    @Query("SELECT * FROM events WHERE datetime(created_on) BETWEEN datetime(:createdAfter) AND datetime(:createdBefore) ORDER BY datetime(created_on)")
    fun findAllBetween(createdAfter: OffsetDateTime, createdBefore: OffsetDateTime)

    @Query("SELECT * FROM events WHERE (datetime(created_on) BETWEEN datetime(:createdAfter) AND datetime(:createdBefore)) AND uid > :latestUId ORDER BY datetime(created_on) LIMIT :limit")
    fun findAllBetweenDatesPaged(createdAfter: OffsetDateTime, createdBefore: OffsetDateTime, latestUId: Int = -1, limit: Int)

    @Query("SELECT * FROM events WHERE resource = :resource")
    fun findAllForResource(resource: String)

    @Query("SELECT * FROM events WHERE resource = :resource AND uid > :latestUId ORDER BY datetime(created_on) LIMIT :limit")
    fun findAllForResourcePaged(resource: String, latestUId: Int = -1, limit: Int)

    @Query("SELECT * FROM events WHERE (datetime(created_on) BETWEEN datetime(:createdAfter) AND datetime(:createdBefore)) AND resource = :resource ORDER BY datetime(created_on)")
    fun findAllBetweenDatesForResource(resource: String,
                                       createdAfter: OffsetDateTime,
                                       createdBefore: OffsetDateTime)

    @Query("SELECT * FROM events WHERE (datetime(created_on) BETWEEN datetime(:createdAfter) AND datetime(:createdBefore)) AND resource = :resource AND uid > :latestUId ORDER BY datetime(created_on) LIMIT :limit")
    fun findAllBetweenDatesForResourcePaged(resource: String,
                                            createdAfter: OffsetDateTime,
                                            createdBefore: OffsetDateTime,
                                            latestUId: Int = -1,
                                            limit: Int)

    @Insert
    fun insertAll(vararg events: Event)

    @Delete
    fun delete(event: Event)
}