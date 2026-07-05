package com.example.domain.repository

import com.example.data.api.PinterestBoard
import com.example.data.model.PinPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IPinRepository {
    val accessToken: StateFlow<String?>
    val username: StateFlow<String?>
    val isSandboxMode: StateFlow<Boolean>
    val googleEmail: StateFlow<String?>
    val googleName: StateFlow<String?>
    val googleAvatar: StateFlow<String?>
    val boards: StateFlow<List<PinterestBoard>>
    
    val amazonAssociateTag: StateFlow<String?>
    val amazonStoreName: StateFlow<String?>
    
    val allPosts: Flow<List<PinPost>>
    val draftsCount: Flow<List<PinPost>>
    val scheduledCount: Flow<List<PinPost>>

    fun setSandboxMode(enabled: Boolean)
    fun getOAuthAuthorizationUrl(): String
    suspend fun handleOAuthRedirectCode(code: String): Result<Boolean>
    suspend fun fetchRealBoards(): Result<List<PinterestBoard>>
    suspend fun postPinNow(post: PinPost): Result<String>
    
    suspend fun getPostById(id: Int): PinPost?
    suspend fun savePinPost(post: PinPost): Long
    suspend fun deletePostById(id: Int)
    suspend fun getScheduledPostsPending(): List<PinPost>
    
    fun logout()
    fun loginWithGoogle(email: String, name: String, avatarUrl: String)
    fun logoutGoogle()
    fun simulateSandboxLogin(customUsername: String = "sandbox_creator_demo")
    
    fun saveAmazonStoreConfig(associateTag: String, storeName: String)
    fun disconnectAmazonStore()
}
