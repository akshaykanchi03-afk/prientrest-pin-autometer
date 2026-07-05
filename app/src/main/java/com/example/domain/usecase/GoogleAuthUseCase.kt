package com.example.domain.usecase

import com.example.domain.repository.IPinRepository

class GoogleAuthUseCase(private val repository: IPinRepository) {
    fun login(email: String, name: String, avatarUrl: String) {
        repository.loginWithGoogle(email, name, avatarUrl)
    }
    
    fun logout() {
        repository.logoutGoogle()
    }
}
