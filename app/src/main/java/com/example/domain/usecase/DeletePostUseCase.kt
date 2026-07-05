package com.example.domain.usecase

import com.example.domain.repository.IPinRepository

class DeletePostUseCase(private val repository: IPinRepository) {
    suspend operator fun invoke(id: Int) {
        repository.deletePostById(id)
    }
}
