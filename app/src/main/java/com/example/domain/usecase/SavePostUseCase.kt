package com.example.domain.usecase

import com.example.domain.repository.IPinRepository
import com.example.data.model.PinPost

class SavePostUseCase(private val repository: IPinRepository) {
    suspend operator fun invoke(post: PinPost): Long {
        return repository.savePinPost(post)
    }
}
