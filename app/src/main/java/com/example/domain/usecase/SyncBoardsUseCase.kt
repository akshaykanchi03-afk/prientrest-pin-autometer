package com.example.domain.usecase

import com.example.domain.repository.IPinRepository
import com.example.data.api.PinterestBoard

class SyncBoardsUseCase(private val repository: IPinRepository) {
    suspend operator fun invoke(): Result<List<PinterestBoard>> {
        return repository.fetchRealBoards()
    }
}
