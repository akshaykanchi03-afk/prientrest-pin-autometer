package com.example.ui.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.PinPost
import com.example.data.repository.PinRepository
import com.example.data.api.PinterestBoard
import com.example.domain.repository.IPinRepository
import com.example.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class PinViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: IPinRepository = PinRepository(
        application.applicationContext,
        AppDatabase.getDatabase(application.applicationContext).pinPostDao()
    ),
    private val syncBoardsUseCase: SyncBoardsUseCase = SyncBoardsUseCase(repository),
    private val pinterestAuthUseCase: PinterestAuthUseCase = PinterestAuthUseCase(repository),
    private val googleAuthUseCase: GoogleAuthUseCase = GoogleAuthUseCase(repository),
    private val getPostsUseCase: GetPostsUseCase = GetPostsUseCase(repository),
    private val savePostUseCase: SavePostUseCase = SavePostUseCase(repository),
    private val publishPostUseCase: PublishPostUseCase = PublishPostUseCase(repository),
    private val deletePostUseCase: DeletePostUseCase = DeletePostUseCase(repository)
) : AndroidViewModel(application) {

    private val context = application.applicationContext

    // Authenticated Profile States
    val accessToken: StateFlow<String?> = repository.accessToken
    val username: StateFlow<String?> = repository.username
    val isSandboxMode: StateFlow<Boolean> = repository.isSandboxMode
    val boards: StateFlow<List<PinterestBoard>> = repository.boards

    // Google Profile States
    val googleEmail: StateFlow<String?> = repository.googleEmail
    val googleName: StateFlow<String?> = repository.googleName
    val googleAvatar: StateFlow<String?> = repository.googleAvatar

    // Amazon Store States
    val amazonAssociateTag: StateFlow<String?> = repository.amazonAssociateTag
    val amazonStoreName: StateFlow<String?> = repository.amazonStoreName

    // Dynamic Database Streams
    val allPosts: StateFlow<List<PinPost>> = getPostsUseCase.allPosts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val draftsList: StateFlow<List<PinPost>> = getPostsUseCase.draftsCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scheduledList: StateFlow<List<PinPost>> = getPostsUseCase.scheduledCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Loading & Verification Feedback states
    private val _isOperationLoading = MutableStateFlow(false)
    val isOperationLoading: StateFlow<Boolean> = _isOperationLoading.asStateFlow()

    private val _operationStatusMessage = MutableStateFlow<String?>(null)
    val operationStatusMessage: StateFlow<String?> = _operationStatusMessage.asStateFlow()

    // Create Pin Form inputs
    private val _formTitle = MutableStateFlow("")
    val formTitle = _formTitle.asStateFlow()

    private val _formDescription = MutableStateFlow("")
    val formDescription = _formDescription.asStateFlow()

    private val _formDestination = MutableStateFlow("")
    val formDestination = _formDestination.asStateFlow()

    private val _formSelectedBoardId = MutableStateFlow("")
    val formSelectedBoardId = _formSelectedBoardId.asStateFlow()

    private val _formSelectedBoardName = MutableStateFlow("")
    val formSelectedBoardName = _formSelectedBoardName.asStateFlow()

    private val _formImageUri = MutableStateFlow("")
    val formImageUri = _formImageUri.asStateFlow()

    private val _formScheduleTime = MutableStateFlow<Long?>(null)
    val formScheduleTime = _formScheduleTime.asStateFlow()

    // Validation Status helper
    private val _formValidationError = MutableStateFlow<String?>(null)
    val formValidationError = _formValidationError.asStateFlow()

    init {
        // Run initial synchronization if logged in
        syncBoards()
    }

    fun syncBoards() {
        viewModelScope.launch {
            syncBoardsUseCase()
        }
    }

    fun toggleSandbox(enabled: Boolean) {
        pinterestAuthUseCase.toggleSandbox(enabled)
        _operationStatusMessage.value = "Sandbox Mode set to $enabled"
        syncBoards()
    }

    fun getOAuthUrl(): String = pinterestAuthUseCase.getOAuthUrl()

    fun handleOAuthRedirect(uri: String): Boolean {
        if (uri.contains("code=")) {
            val code = Uri.parse(uri).getQueryParameter("code")
            if (code != null) {
                _isOperationLoading.value = true
                _operationStatusMessage.value = "Exchanging validation credentials..."
                viewModelScope.launch {
                    val res = pinterestAuthUseCase.handleRedirectCode(code)
                    _isOperationLoading.value = false
                    if (res.isSuccess) {
                        _operationStatusMessage.value = "Successfully linked Pinterest Profile!"
                    } else {
                        _operationStatusMessage.value = "Auth failed: " + res.exceptionOrNull()?.localizedMessage
                    }
                }
                return true // Intercepted
            }
        }
        return false
    }

    fun triggerSandboxDirectLogin(username: String) {
        pinterestAuthUseCase.loginToSandbox(username)
        _operationStatusMessage.value = "Direct logged in to sandbox successfully."
    }

    fun logout() {
        pinterestAuthUseCase.logout()
        _operationStatusMessage.value = "Cleared Pinterest connection details safely."
    }

    fun loginWithGoogle(email: String, name: String, avatarUrl: String) {
        googleAuthUseCase.login(email, name, avatarUrl)
        _operationStatusMessage.value = "Welcome back, $name! Logged in with Google."
    }

    fun logoutGoogle() {
        googleAuthUseCase.logout()
        _operationStatusMessage.value = "Successfully signed out of Google account."
    }

    // Set Form inputs
    fun updateFormFields(
        title: String? = null,
        description: String? = null,
        destination: String? = null,
        boardId: String? = null,
        boardName: String? = null,
        imageUri: String? = null,
        scheduleTime: Long? = null
    ) {
        title?.let { _formTitle.value = it }
        description?.let { _formDescription.value = it }
        destination?.let { _formDestination.value = it }
        boardId?.let { _formSelectedBoardId.value = it }
        boardName?.let { _formSelectedBoardName.value = it }
        imageUri?.let { _formImageUri.value = it }
        _formScheduleTime.value = scheduleTime
        _formValidationError.value = null
    }

    fun clearFormFields() {
        _formTitle.value = ""
        _formDescription.value = ""
        _formDestination.value = ""
        _formSelectedBoardId.value = ""
        _formSelectedBoardName.value = ""
        _formImageUri.value = ""
        _formScheduleTime.value = null
        _formValidationError.value = null
    }

    fun clearOperationMessage() {
        _operationStatusMessage.value = null
    }

    // Save as local Draft
    fun saveDraft(): Boolean {
        if (_formTitle.value.isBlank()) {
            _formValidationError.value = "Title is required even for draft."
            return false
        }
        viewModelScope.launch {
            val post = PinPost(
                title = _formTitle.value,
                description = _formDescription.value,
                destinationLink = _formDestination.value,
                boardId = _formSelectedBoardId.value,
                boardName = _formSelectedBoardName.value,
                imageUri = _formImageUri.value,
                scheduledTime = null,
                status = "DRAFT"
            )
            savePostUseCase(post)
            clearFormFields()
            _operationStatusMessage.value = "Draft saved successfully!"
        }
        return true
    }

    // Load custom draft into active form fields to edit
    fun loadDraftIntoForm(post: PinPost) {
        updateFormFields(
            title = post.title,
            description = post.description,
            destination = post.destinationLink,
            boardId = post.boardId,
            boardName = post.boardName,
            imageUri = post.imageUri,
            scheduleTime = post.scheduledTime
        )
    }

    // Delete post
    fun deletePost(postId: Int) {
        viewModelScope.launch {
            cancelAlarm(postId)
            deletePostUseCase(postId)
            _operationStatusMessage.value = "Item removed successfully."
        }
    }

    // Post Immediately or Schedule future
    fun submitPinPost(onSuccess: () -> Unit) {
        // Validate
        if (_formTitle.value.isBlank()) {
            _formValidationError.value = "Pin title is required."
            return
        }
        if (_formSelectedBoardId.value.isBlank()) {
            _formValidationError.value = "Please select a target Pinterest board."
            return
        }
        if (_formImageUri.value.isBlank()) {
            _formValidationError.value = "Please choose an image for your Pin."
            return
        }

        val hasSchedule = _formScheduleTime.value != null
        _isOperationLoading.value = true

        viewModelScope.launch {
            val scheduledTime = _formScheduleTime.value
            
            if (hasSchedule && scheduledTime != null) {
                // Future Schedule Mode
                val post = PinPost(
                    title = _formTitle.value,
                    description = _formDescription.value,
                    destinationLink = _formDestination.value,
                    boardId = _formSelectedBoardId.value,
                    boardName = _formSelectedBoardName.value,
                    imageUri = _formImageUri.value,
                    scheduledTime = scheduledTime,
                    status = "SCHEDULED"
                )
                val newId = savePostUseCase(post)
                val finalPost = post.copy(id = newId.toInt())
                
                // Set system context alarm
                scheduleAlarm(finalPost)

                _isOperationLoading.value = false
                clearFormFields()
                _operationStatusMessage.value = "Post scheduled successfully!"
                onSuccess()
            } else {
                // Post Now Mode
                val post = PinPost(
                    title = _formTitle.value,
                    description = _formDescription.value,
                    destinationLink = _formDestination.value,
                    boardId = _formSelectedBoardId.value,
                    boardName = _formSelectedBoardName.value,
                    imageUri = _formImageUri.value,
                    status = "DRAFT" // Temporarily draft
                )
                val newId = savePostUseCase(post)
                val freshPost = post.copy(id = newId.toInt())

                _operationStatusMessage.value = "Publishing to Pinterest..."
                val result = publishPostUseCase(freshPost)
                _isOperationLoading.value = false

                if (result.isSuccess) {
                    _operationStatusMessage.value = "Successfully posted Pin to Pinterest!"
                    clearFormFields()
                    onSuccess()
                } else {
                    _operationStatusMessage.value = "Publishing failed: " + (result.exceptionOrNull()?.localizedMessage ?: "Try again.")
                }
            }
        }
    }

    // Set precise AlarmManager
    private fun scheduleAlarm(post: PinPost) {
        val scheduledTime = post.scheduledTime ?: return
        if (scheduledTime <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, com.example.data.receiver.PinScheduleReceiver::class.java).apply {
            putExtra("PIN_POST_ID", post.id)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            post.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        scheduledTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelAlarm(postId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, com.example.data.receiver.PinScheduleReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            postId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    fun saveAmazonStore(associateTag: String, storeName: String) {
        repository.saveAmazonStoreConfig(associateTag, storeName)
        _operationStatusMessage.value = "Amazon Store ($storeName) successfully connected!"
    }

    fun disconnectAmazonStore() {
        repository.disconnectAmazonStore()
        _operationStatusMessage.value = "Amazon Store disconnected safely."
    }

    fun importAmazonProduct(url: String): Boolean {
        if (url.isBlank()) {
            _formValidationError.value = "Amazon product URL cannot be empty."
            return false
        }
        if (!url.contains("amazon.", ignoreCase = true)) {
            _formValidationError.value = "Please enter a valid Amazon product URL."
            return false
        }

        try {
            // Smart extraction
            val asinRegex = """/(dp|gp/product|d)/(B[A-Z0-9]{9})""".toRegex()
            val asinMatch = asinRegex.find(url) ?: """\b(B[A-Z0-9]{9})\b""".toRegex().find(url)
            val asin = asinMatch?.groupValues?.get(2) ?: asinMatch?.groupValues?.get(1) ?: "B0PRODUCT"

            val titleRegex = """amazon\.[a-z\.]+/([^/]+)/(dp|gp/product)/""".toRegex(RegexOption.IGNORE_CASE)
            val titleMatch = titleRegex.find(url)
            var extractedTitle = ""
            if (titleMatch != null) {
                extractedTitle = titleMatch.groupValues[1]
                    .replace("-", " ")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { word ->
                        word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    }
            } else {
                extractedTitle = "Premium Amazon Find"
            }

            if (extractedTitle.length > 50) {
                extractedTitle = extractedTitle.take(47) + "..."
            }

            val associateTag = amazonAssociateTag.value ?: "pinautomator-20"
            val monetizedUrl = "https://www.amazon.com/dp/$asin?tag=$associateTag"

            // Choose image preset based on title keywords
            val lowerTitle = extractedTitle.lowercase()
            val imageToUse = when {
                lowerTitle.contains("desk") || lowerTitle.contains("mat") || lowerTitle.contains("office") || lowerTitle.contains("workspace") || lowerTitle.contains("keyboard") || lowerTitle.contains("mouse") -> 
                    "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=800&q=80"
                lowerTitle.contains("art") || lowerTitle.contains("decor") || lowerTitle.contains("paint") || lowerTitle.contains("aesthetic") -> 
                    "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80"
                lowerTitle.contains("desert") || lowerTitle.contains("sweet") || lowerTitle.contains("food") || lowerTitle.contains("cake") || lowerTitle.contains("kitchen") || lowerTitle.contains("recipe") -> 
                    "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80"
                else -> "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=800&q=80"
            }

            val pTitle = "🔥 Must-Have Amazon Find: $extractedTitle!"
            val pDesc = "Looking for the perfect aesthetic upgrade? Check out this highly-rated $extractedTitle! \n\n" +
                    "✨ Why we love this find:\n" +
                    "• High-quality design with premium durability\n" +
                    "• Elegant, minimalist style that elevates any setup\n" +
                    "• Fast shipping and thousands of 5-star reviews on Amazon!\n\n" +
                    "Click to shop this deal instantly on Amazon!\n\n" +
                    "#amazonfinds #aestheticfinds #creativepicks #pinterestshop"

            // Update form
            updateFormFields(
                title = pTitle,
                description = pDesc,
                destination = monetizedUrl,
                imageUri = imageToUse
            )

            _operationStatusMessage.value = "Successfully imported and monetized Amazon product details!"
            return true
        } catch (e: java.lang.Exception) {
            _formValidationError.value = "Error parsing URL: ${e.localizedMessage}"
            return false
        }
    }
}
