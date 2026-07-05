package com.example.domain.usecase

import com.example.domain.repository.IPinRepository

class PinterestAuthUseCase(private val repository: IPinRepository) {
    fun getOAuthUrl(): String = repository.getOAuthAuthorizationUrl()
    
    suspend fun handleRedirectCode(code: String): Result<Boolean> {
        return repository.handleOAuthRedirectCode(code)
    }
    
    fun toggleSandbox(enabled: Boolean) {
        repository.setSandboxMode(enabled)
    }
    
    fun loginToSandbox(username: String) {
        repository.simulateSandboxLogin(username)
    }
    
    fun logout() {
        repository.logout()
    }
}
