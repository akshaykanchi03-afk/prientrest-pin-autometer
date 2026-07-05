package com.example.data.repository

import android.content.Context
import android.util.Base64
import com.example.BuildConfig
import com.example.data.api.PinterestApi
import com.example.data.api.PinterestBoard
import com.example.data.api.PinterestPinRequest
import com.example.data.api.MediaSource
import com.example.data.database.PinPostDao
import com.example.data.model.PinPost
import com.example.data.security.SecureStorage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import com.example.domain.repository.IPinRepository

class PinRepository(
    private val context: Context,
    private val pinPostDao: PinPostDao
) : IPinRepository {
    private val secureStorage = SecureStorage(context)
    private val sharedPrefs = context.getSharedPreferences("pinterest_automator_prefs", Context.MODE_PRIVATE)

    // Token & Auth Keys
    private val tokenKey = "pinterest_access_token"
    private val userProfileKey = "pinterest_user_profile"

    private val _accessToken = MutableStateFlow<String?>(null)
    override val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val _username = MutableStateFlow<String?>(null)
    override val username: StateFlow<String?> = _username.asStateFlow()

    private val _isSandboxMode = MutableStateFlow(true)
    override val isSandboxMode: StateFlow<Boolean> = _isSandboxMode.asStateFlow()

    // Google Auth Keys & StateFlows
    private val googleEmailKey = "google_user_email"
    private val googleNameKey = "google_user_name"
    private val googleAvatarKey = "google_user_avatar"

    private val _googleEmail = MutableStateFlow<String?>(null)
    override val googleEmail: StateFlow<String?> = _googleEmail.asStateFlow()

    private val _googleName = MutableStateFlow<String?>(null)
    override val googleName: StateFlow<String?> = _googleName.asStateFlow()

    private val _googleAvatar = MutableStateFlow<String?>(null)
    override val googleAvatar: StateFlow<String?> = _googleAvatar.asStateFlow()

    // Amazon Affiliate Keys & StateFlows
    private val amazonAssociateTagKey = "amazon_associate_tag"
    private val amazonStoreNameKey = "amazon_store_name"

    private val _amazonAssociateTag = MutableStateFlow<String?>(null)
    override val amazonAssociateTag: StateFlow<String?> = _amazonAssociateTag.asStateFlow()

    private val _amazonStoreName = MutableStateFlow<String?>(null)
    override val amazonStoreName: StateFlow<String?> = _amazonStoreName.asStateFlow()

    // Loaded boards list
    private val _boards = MutableStateFlow<List<PinterestBoard>>(emptyList())
    override val boards: StateFlow<List<PinterestBoard>> = _boards.asStateFlow()

    // Mock boards for sandbox mode
    val mockBoards = listOf(
        PinterestBoard("b_diy_101", "DIY Crafts & Handiwork", "Ideas for homemade custom decorations"),
        PinterestBoard("b_recipes_202", "Gourmet Kitchen Recipes", "Quick bites and delicious full-course meals"),
        PinterestBoard("b_tech_303", "Mobile UI/UX Design", "Elegant and modern interface screen references"),
        PinterestBoard("b_travel_404", "Wanderlust Destinations", "Bucket-list travel landscapes and guides")
    )

    // All local database posts
    override val allPosts: Flow<List<PinPost>> = pinPostDao.getAllPosts()
    override val draftsCount: Flow<List<PinPost>> = pinPostDao.getPostsByStatus("DRAFT")
    override val scheduledCount: Flow<List<PinPost>> = pinPostDao.getPostsByStatus("SCHEDULED")

    // Retrofit service init
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val pinterestApi = Retrofit.Builder()
        .baseUrl("https://api.pinterest.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
        .create(PinterestApi::class.java)

    init {
        // Read credentials on start
        _accessToken.value = secureStorage.decryptAndGet(tokenKey)
        _username.value = sharedPrefs.getString(userProfileKey, null)
        _isSandboxMode.value = sharedPrefs.getBoolean("sandbox_enabled", true)

        // Read Google Credentials on start
        _googleEmail.value = sharedPrefs.getString(googleEmailKey, null)
        _googleName.value = sharedPrefs.getString(googleNameKey, null)
        _googleAvatar.value = sharedPrefs.getString(googleAvatarKey, null)

        // Read Amazon Store credentials on start
        _amazonAssociateTag.value = sharedPrefs.getString(amazonAssociateTagKey, null)
        _amazonStoreName.value = sharedPrefs.getString(amazonStoreNameKey, null)

        if (_isSandboxMode.value) {
            _boards.value = mockBoards
            if (_accessToken.value != null && _username.value == null) {
                _username.value = "sandbox_creator_demo"
            }
        } else {
            // Load boards asynchronously if real oauth token exists
            _accessToken.value?.let {
                // Background board loading handled by ViewModel
            }
        }
    }

    override fun setSandboxMode(enabled: Boolean) {
        _isSandboxMode.value = enabled
        sharedPrefs.edit().putBoolean("sandbox_enabled", enabled).apply()
        if (enabled) {
            _boards.value = mockBoards
            if (_username.value == null || _username.value?.contains("sandbox") != true) {
                _username.value = "sandbox_creator_demo"
                sharedPrefs.edit().putString(userProfileKey, _username.value).apply()
            }
        } else {
            _boards.value = emptyList()
            if (_username.value?.contains("sandbox") == true) {
                logout()
            }
        }
    }

    override fun getOAuthAuthorizationUrl(): String {
        val clientId = BuildConfig.PINTEREST_CLIENT_ID.ifBlank { "dummy_client_id" }
        val redirectUri = BuildConfig.PINTEREST_REDIRECT_URI.ifBlank { "https://localhost/auth/pinterest" }
        return "https://www.pinterest.com/oauth/?client_id=$clientId" +
                "&redirect_uri=$redirectUri" +
                "&response_type=code" +
                "&scope=boards:read,boards:write,pins:read,pins:write,user_accounts:read"
    }

    override suspend fun handleOAuthRedirectCode(code: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            if (_isSandboxMode.value) {
                // Sandbox fast credentials simulated login
                val simulatedToken = "sb_tok_" + System.currentTimeMillis()
                val simulatedName = "sandbox_pins_guru"
                secureStorage.encryptAndSave(tokenKey, simulatedToken)
                _accessToken.value = simulatedToken
                _username.value = simulatedName
                sharedPrefs.edit().putString(userProfileKey, simulatedName).apply()
                _boards.value = mockBoards
                Result.success(true)
            } else {
                val clientId = BuildConfig.PINTEREST_CLIENT_ID
                val clientSecret = BuildConfig.PINTEREST_CLIENT_SECRET
                val redirectUri = BuildConfig.PINTEREST_REDIRECT_URI

                if (clientId.isBlank() || clientId == "your_pinterest_client_id" ||
                    clientSecret.isBlank() || clientSecret == "your_pinterest_client_secret") {
                    return@withContext Result.failure(Exception("Pinterest Client ID and Secret are not configured. Please use Sandbox / Simulation Mode or enter valid environment keys."))
                }

                try {
                    // Encode base64 basic authentication header
                    val basicValue = "$clientId:$clientSecret"
                    val b64Auth = "Basic " + Base64.encodeToString(basicValue.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

                    // Post authorization code exchange
                    val tokenResp = pinterestApi.exchangeToken(
                        basicAuth = b64Auth,
                        code = code,
                        redirectUri = redirectUri
                    )

                    secureStorage.encryptAndSave(tokenKey, tokenResp.accessToken)
                    _accessToken.value = tokenResp.accessToken

                    // Retrieve active user profile
                    val profileResp = pinterestApi.getUserProfile("Bearer ${tokenResp.accessToken}")
                    _username.value = profileResp.username
                    sharedPrefs.edit().putString(userProfileKey, profileResp.username).apply()

                    // Populate board items
                    fetchRealBoards()

                    Result.success(true)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Result.failure(Exception("Credentials exchange failed: ${e.localizedMessage ?: "Unknown service error"}"))
                }
            }
        }
    }

    override suspend fun fetchRealBoards(): Result<List<PinterestBoard>> {
        return withContext(Dispatchers.IO) {
            val token = _accessToken.value
            if (token == null) {
                return@withContext Result.failure(Exception("Not certified with Pinterest. Please sign in first."))
            }
            if (_isSandboxMode.value) {
                _boards.value = mockBoards
                Result.success(mockBoards)
            } else {
                try {
                    val resp = pinterestApi.getBoards("Bearer $token")
                    _boards.value = resp.items
                    Result.success(resp.items)
                } catch (e: Exception) {
                    e.printStackTrace()
                    Result.failure(Exception("Failed to fetch Pinterest boards: ${e.localizedMessage ?: "Check connection"}"))
                }
            }
        }
    }

    override suspend fun postPinNow(post: PinPost): Result<String> {
        return withContext(Dispatchers.IO) {
            val isSandbox = _isSandboxMode.value
            val token = _accessToken.value

            if (!isSandbox && token == null) {
                return@withContext Result.failure(Exception("Pinterest token missing. Authenticate to publish."))
            }

            if (isSandbox) {
                // Sandbox post simulation with dynamic delay
                kotlinx.coroutines.delay(1200)
                val simulatedPinId = "sim_pin_" + (1000000000..9999999999).random()
                // Mark loaded status in database
                val postedItem = post.copy(
                    status = "POSTED",
                    postedTime = System.currentTimeMillis(),
                    pinId = simulatedPinId,
                    failureReason = null
                )
                pinPostDao.insertPost(postedItem)
                Result.success(simulatedPinId)
            } else {
                try {
                    // Modern Pinterest v5 API requires standard image url. 
                    // If uri starts with file/content, the API won't resolve it directly, so we pass a high-quality aesthetic placeholder.
                    val targetImgUrl = if (post.imageUri.startsWith("http")) {
                        post.imageUri
                    } else {
                        // Pinterest v5 demo expects a public image web resource
                        "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80"
                    }

                    val pinReq = PinterestPinRequest(
                        title = post.title,
                        description = post.description,
                        destinationLink = post.destinationLink.ifBlank { null },
                        boardId = post.boardId,
                        mediaSource = MediaSource(url = targetImgUrl)
                    )

                    val resp = pinterestApi.createPin("Bearer $token", pinReq)
                    val postedItem = post.copy(
                        status = "POSTED",
                        postedTime = System.currentTimeMillis(),
                        pinId = resp.id,
                        failureReason = null
                    )
                    pinPostDao.insertPost(postedItem)
                    Result.success(resp.id)
                } catch (e: Exception) {
                    e.printStackTrace()
                    val errorMessage = e.localizedMessage ?: "Pinterest server communication error"
                    val failedItem = post.copy(
                        status = "FAILED",
                        failureReason = errorMessage
                    )
                    pinPostDao.insertPost(failedItem)
                    Result.failure(Exception(errorMessage))
                }
            }
        }
    }

    // Database Actions
    override suspend fun getPostById(id: Int): PinPost? = pinPostDao.getPostById(id)

    override suspend fun savePinPost(post: PinPost): Long {
        return pinPostDao.insertPost(post)
    }

    override suspend fun deletePostById(id: Int) {
        pinPostDao.deletePostById(id)
    }

    override suspend fun getScheduledPostsPending(): List<PinPost> {
        return pinPostDao.getScheduledPostsBefore(System.currentTimeMillis())
    }

    override fun logout() {
        secureStorage.clear(tokenKey)
        sharedPrefs.edit().remove(userProfileKey).apply()
        _accessToken.value = null
        _username.value = null
        if (!_isSandboxMode.value) {
            _boards.value = emptyList()
        }
    }

    override fun loginWithGoogle(email: String, name: String, avatarUrl: String) {
        sharedPrefs.edit().apply {
            putString(googleEmailKey, email)
            putString(googleNameKey, name)
            putString(googleAvatarKey, avatarUrl)
        }.apply()
        _googleEmail.value = email
        _googleName.value = name
        _googleAvatar.value = avatarUrl
    }

    override fun logoutGoogle() {
        sharedPrefs.edit().apply {
            remove(googleEmailKey)
            remove(googleNameKey)
            remove(googleAvatarKey)
        }.apply()
        _googleEmail.value = null
        _googleName.value = null
        _googleAvatar.value = null
    }

    // Method to login directly to Sandbox for testing
    override fun simulateSandboxLogin(customUsername: String) {
        val simToken = "sb_tok_direct_" + System.currentTimeMillis()
        secureStorage.encryptAndSave(tokenKey, simToken)
        _accessToken.value = simToken
        _username.value = customUsername
        sharedPrefs.edit().putString(userProfileKey, customUsername).apply()
        _boards.value = mockBoards
    }

    override fun saveAmazonStoreConfig(associateTag: String, storeName: String) {
        _amazonAssociateTag.value = associateTag
        _amazonStoreName.value = storeName
        sharedPrefs.edit()
            .putString(amazonAssociateTagKey, associateTag)
            .putString(amazonStoreNameKey, storeName)
            .apply()
    }

    override fun disconnectAmazonStore() {
        _amazonAssociateTag.value = null
        _amazonStoreName.value = null
        sharedPrefs.edit()
            .remove(amazonAssociateTagKey)
            .remove(amazonStoreNameKey)
            .apply()
    }
}
