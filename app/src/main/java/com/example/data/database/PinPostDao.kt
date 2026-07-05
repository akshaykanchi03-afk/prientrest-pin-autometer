package com.example.data.database

import androidx.room.*
import com.example.data.model.PinPost
import kotlinx.coroutines.flow.Flow

@Dao
interface PinPostDao {
    @Query("SELECT * FROM pin_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<PinPost>>

    @Query("SELECT * FROM pin_posts WHERE status = :status ORDER BY createdAt DESC")
    fun getPostsByStatus(status: String): Flow<List<PinPost>>

    @Query("SELECT * FROM pin_posts WHERE id = :id")
    suspend fun getPostById(id: Int): PinPost?

    @Query("SELECT * FROM pin_posts WHERE status = 'SCHEDULED' AND scheduledTime <= :currentTime")
    suspend fun getScheduledPostsBefore(currentTime: Long): List<PinPost>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PinPost): Long

    @Delete
    suspend fun deletePost(post: PinPost)

    @Query("DELETE FROM pin_posts WHERE id = :id")
    suspend fun deletePostById(id: Int)
}
