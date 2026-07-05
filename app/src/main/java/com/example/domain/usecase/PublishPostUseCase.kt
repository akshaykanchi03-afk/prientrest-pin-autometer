package com.example.domain.usecase

import com.example.domain.repository.IPinRepository
import com.example.data.model.PinPost

class PublishPostUseCase(private val repository: IPinRepository) {
    suspend operator fun invoke(post: PinPost): Result<String> {
        return repository.postPinNow(post)
    }
}
