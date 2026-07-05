package com.example.domain.usecase

import com.example.domain.repository.IPinRepository
import com.example.data.model.PinPost
import kotlinx.coroutines.flow.Flow

class GetPostsUseCase(private val repository: IPinRepository) {
    val allPosts: Flow<List<PinPost>> = repository.allPosts
    val draftsCount: Flow<List<PinPost>> = repository.draftsCount
    val scheduledCount: Flow<List<PinPost>> = repository.scheduledCount

    suspend fun getById(id: Int): PinPost? {
        return repository.getPostById(id)
    }
    
    suspend fun getPendingScheduled(): List<PinPost> {
        return repository.getScheduledPostsPending()
    }
}
