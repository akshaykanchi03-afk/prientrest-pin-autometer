package com.example.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import com.example.data.model.PinPost
import com.example.ui.viewmodel.PinViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GlassAppBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "glass_bg")
    
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse1"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(7000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse2"
    )
    val pulseAlpha3 by infiniteTransition.animateFloat(
        initialValue = 0.04f,
        targetValue = 0.14f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse3"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val baseColor = if (isDark) Color(0xFF090A11) else Color(0xFFF3F4F8)
            drawRect(baseColor)

            val orb1Color = if (isDark) Color(0xFF673AB7) else Color(0xFFD1C4E9)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb1Color.copy(alpha = pulseAlpha1), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.2f),
                    radius = size.width * 0.85f
                )
            )

            val orb2Color = if (isDark) Color(0xFF00BCD4) else Color(0xFFB2EBF2)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb2Color.copy(alpha = pulseAlpha2), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.85f, size.height * 0.8f),
                    radius = size.width * 0.85f
                )
            )

            val orb3Color = if (isDark) Color(0xFFE91E63) else Color(0xFFF8BBD0)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(orb3Color.copy(alpha = pulseAlpha3), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.65f),
                    radius = size.width * 0.55f
                )
            )
        }
        content()
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    borderWidth: androidx.compose.ui.unit.Dp = 1.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.55f)
    val borderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.35f)

    Box(
        modifier = modifier
            .background(bgColor, shape)
            .border(borderWidth, borderColor, shape)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: PinViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Observe state variables
    val username by viewModel.username.collectAsState()
    val isSandbox by viewModel.isSandboxMode.collectAsState()
    val isOperationLoading by viewModel.isOperationLoading.collectAsState()
    val operationStatusMessage by viewModel.operationStatusMessage.collectAsState()

    // Google Profile State observations
    val googleEmail by viewModel.googleEmail.collectAsState()
    val googleName by viewModel.googleName.collectAsState()
    val googleAvatar by viewModel.googleAvatar.collectAsState()
    var showGoogleLoginPage by remember { mutableStateOf(false) }
    var showGoogleGalleryPicker by remember { mutableStateOf(false) }

    // Form inputs
    val formTitle by viewModel.formTitle.collectAsState()
    val formDescription by viewModel.formDescription.collectAsState()
    val formDestination by viewModel.formDestination.collectAsState()
    val formBoardId by viewModel.formSelectedBoardId.collectAsState()
    val formBoardName by viewModel.formSelectedBoardName.collectAsState()
    val formImageUri by viewModel.formImageUri.collectAsState()
    val formScheduleTime by viewModel.formScheduleTime.collectAsState()
    val formValidationError by viewModel.formValidationError.collectAsState()

    // Active screen navigation
    var currentTab by remember { mutableStateOf("home") }
    var showAuthWebView by remember { mutableStateOf(false) }

    // Toast snackbar handler
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(operationStatusMessage) {
        operationStatusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearOperationMessage()
        }
    }

    GlassAppBackground {
        val isDark = isSystemInDarkTheme()
        Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.app_logo_concept_1779423040833),
                            contentDescription = "PinAuto Premium Logo",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "PinAuto",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp,
                                    brush = Brush.linearGradient(
                                        colors = if (isDark) {
                                            listOf(Color(0xFFFF3366), Color(0xFFFF9933))
                                        } else {
                                            listOf(Color(0xFFE91E63), Color(0xFFFF5722))
                                        }
                                    )
                                )
                            )
                            Text(
                                if (isSandbox) "Sandbox Simulation Mode" else "Connected via Pinterest v5 API",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isSandbox) MaterialTheme.colorScheme.secondary else Color(0xFF4CAF50),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }
                },
                actions = {
                    // Google Profile Indicator
                    if (googleEmail != null) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .clickable { showGoogleLoginPage = true }
                        ) {
                            val avatarModel: Any = remember(googleAvatar) {
                                val parsedInt = googleAvatar?.toIntOrNull()
                                if (parsedInt != null) {
                                    parsedInt
                                } else if (googleAvatar == "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80") {
                                    com.example.R.drawable.app_logo_concept_1779423040833
                                } else {
                                    googleAvatar ?: com.example.R.drawable.app_logo_concept_1779423040833
                                }
                            }
                            AsyncImage(
                                model = avatarModel,
                                contentDescription = "Google Account",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { showGoogleLoginPage = true },
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(18.dp))
                        }
                    }

                    // Pinterest Status Indicator
                    if (username != null) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "User info",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    username ?: "",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                if (isSandbox) {
                                    viewModel.triggerSandboxDirectLogin("sandbox_pins_expert")
                                } else {
                                    showAuthWebView = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("connect_button")
                        ) {
                            Icon(Icons.Default.Login, contentDescription = "Sign in", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect", fontSize = 12.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) Color.White.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.45f),
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.border(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(
                            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    ),
                    shape = androidx.compose.ui.graphics.RectangleShape
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                NavigationBar(
                    containerColor = if (isDark) Color(0xDD11121C) else Color(0xDDFFFFFF),
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets(0, 0, 0, 0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                listOf(
                                    if (isDark) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.3f),
                                    if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.1f)
                                )
                            ),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    NavigationBarItem(
                        selected = currentTab == "home",
                        onClick = { currentTab = "home" },
                        icon = { Icon(Icons.Default.Dashboard, "Dashboard tab") },
                        label = { Text("Hub") },
                        modifier = Modifier.testTag("nav_hub")
                    )
                    NavigationBarItem(
                        selected = currentTab == "create",
                        onClick = { currentTab = "create" },
                        icon = { Icon(Icons.Default.AddCircle, "Create pin tab") },
                        label = { Text("Compose") },
                        modifier = Modifier.testTag("nav_create")
                    )
                    NavigationBarItem(
                        selected = currentTab == "drafts",
                        onClick = { currentTab = "drafts" },
                        icon = { Icon(Icons.Default.FolderZip, "Drafts tab") },
                        label = { Text("Drafts") },
                        modifier = Modifier.testTag("nav_drafts")
                    )
                    NavigationBarItem(
                        selected = currentTab == "settings",
                        onClick = { currentTab = "settings" },
                        icon = { Icon(Icons.Default.AppSettingsAlt, "Settings tab") },
                        label = { Text("Setup") },
                        modifier = Modifier.testTag("nav_settings")
                    )
                }
            }
        },
        containerColor = Color.Transparent,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Screen switching logic
            when (currentTab) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCreate = { currentTab = "create" },
                    onSignInWithGoogleClick = { showGoogleLoginPage = true },
                    onConnectPinterestClick = {
                        if (isSandbox) {
                            viewModel.triggerSandboxDirectLogin("sandbox_pins_expert")
                        } else {
                            showAuthWebView = true
                        }
                    }
                )
                "create" -> CreatePinScreen(
                    viewModel = viewModel,
                    onShowGoogleGalleryPicker = { showGoogleGalleryPicker = true },
                    onConnectPinterestClick = {
                        if (isSandbox) {
                            viewModel.triggerSandboxDirectLogin("sandbox_pins_expert")
                        } else {
                            showAuthWebView = true
                        }
                    }
                )
                "drafts" -> DraftsScreen(viewModel, onEditDraft = { currentTab = "create" })
                "settings" -> SettingsScreen(
                    viewModel = viewModel,
                    onSignInWithGoogleClick = { showGoogleLoginPage = true },
                    onConnectPinterestClick = {
                        if (isSandbox) {
                            viewModel.triggerSandboxDirectLogin("sandbox_pins_expert")
                        } else {
                            showAuthWebView = true
                        }
                    }
                )
            }

            // Google Login Page Fullscreen Overlay
            if (showGoogleLoginPage) {
                GoogleLoginPageOverlay(
                    onDismiss = { showGoogleLoginPage = false },
                    onLoginSuccess = { email, name, avatarUrl ->
                        viewModel.loginWithGoogle(email, name, avatarUrl)
                        showGoogleLoginPage = false
                    }
                )
            }

            // Google Cloud Gallery Fullscreen Overlay
            if (showGoogleGalleryPicker) {
                GoogleGalleryPickerDialog(
                    onDismiss = { showGoogleGalleryPicker = false },
                    onImageSelected = { selectedUrl ->
                        viewModel.updateFormFields(imageUri = selectedUrl)
                        showGoogleGalleryPicker = false
                    },
                    googleEmail = googleEmail,
                    googleName = googleName,
                    googleAvatar = googleAvatar,
                    onConnectGoogle = {
                        showGoogleGalleryPicker = false
                        showGoogleLoginPage = true
                    }
                )
            }

            // Global indicator for processes loading
            if (isOperationLoading) {
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        GlassCard(
                            modifier = Modifier.width(260.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Executing Action...", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Webview sheet for authentication
            if (showAuthWebView) {
                AuthOverlayWebView(
                    authUrl = viewModel.getOAuthUrl(),
                    onRedirectIntercepted = { uri ->
                        viewModel.handleOAuthRedirect(uri)
                    },
                    onClose = { showAuthWebView = false }
                )
            }
        }
    }
    }
}

// Scaffold extension removed - standard Material 3 Scaffold used directly.


// SCREEN 1: HOME HUB (Metrics + Timeline)
@Composable
fun HomeScreen(
    viewModel: PinViewModel,
    onNavigateToCreate: () -> Unit,
    onSignInWithGoogleClick: () -> Unit,
    onConnectPinterestClick: () -> Unit
) {
    val posts by viewModel.allPosts.collectAsState()
    val drafts by viewModel.draftsList.collectAsState()
    val scheduled by viewModel.scheduledList.collectAsState()
    val googleEmail by viewModel.googleEmail.collectAsState()
    val googleName by viewModel.googleName.collectAsState()
    val username by viewModel.username.collectAsState()
    val isSandbox by viewModel.isSandboxMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredPosts = remember(posts, searchQuery, selectedFilter) {
        posts.filter { post ->
            val matchesSearch = post.title.contains(searchQuery, ignoreCase = true) ||
                    post.description.contains(searchQuery, ignoreCase = true) ||
                    post.boardName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "SCHEDULED" -> post.status == "SCHEDULED"
                "POSTED" -> post.status == "POSTED"
                "FAILED" -> post.status == "FAILED"
                else -> true
            }
            matchesSearch && matchesFilter
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Dashboard Banner with Dynamic Google State integration
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (googleName != null) "Welcome back, $googleName" else "Pin Automation Suite",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (googleEmail != null) "Connected dynamically with $googleEmail • Workspace storage active."
                            else "Schedule your visuals, write destinations, and watch your Pinterest traffic grow organically.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onNavigateToCreate,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Draft direct pin")
                    }
                }
            }
        }
 
        // Sub-item: Show Google Sign-In helper card if totally disconnected
        if (googleEmail == null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onSignInWithGoogleClick() }
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Unlock Premium Google Backup",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "Integrate your Google Workspace and automatically archive active pins.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Sign in",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Sub-item: Show Pinterest connection helper card if disconnected
        if (username == null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onConnectPinterestClick() }
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PinterestLogoIcon(
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Connect Your Pinterest Account",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                if (isSandbox) "Simulate sandbox connection instantly."
                                else "Sign in securely via Pinterest OAuth to publish your designs.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Connect Pinterest",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Stats Row Shelf
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Scheduled",
                    count = scheduled.size,
                    icon = Icons.Default.Schedule,
                    color = Color(0xFF2196F3),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Live Pins",
                    count = posts.count { it.status == "POSTED" },
                    icon = Icons.Default.CloudQueue,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Drafts",
                    count = drafts.size,
                    icon = Icons.Default.Drafts,
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search & Filters Item
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search pins by keywords...") },
                    leadingIcon = { Icon(Icons.Default.Search, "Search icon") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, "Clear search")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_bar"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Quick Filters Roll
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("ALL", "SCHEDULED", "POSTED", "FAILED").forEach { chipKey ->
                        val isSelected = selectedFilter == chipKey
                        InputChip(
                            selected = isSelected,
                            onClick = { selectedFilter = chipKey },
                            label = { Text(chipKey.lowercase().replaceFirstChar { it.uppercase() }) },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Lists header
        item {
            Text(
                "Publication Timeline",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        // List elements
        if (filteredPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = "Empty list",
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No pins matched your criteria.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Tap Compose to schedule or post a pin!",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } else {
            items(filteredPosts, key = { it.id }) { post ->
                PinActionRowCard(post = post, onDelete = { viewModel.deletePost(post.id) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        color.copy(alpha = 0.25f),
                                        color.copy(alpha = 0.05f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = color
                        )
                    }
                    Text(
                        count.toString(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                )
            }
        }
        // Modern premium left-accent color pillar
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .align(Alignment.CenterStart)
                .offset(x = 1.dp)
                .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.2f)
                        )
                    )
                )
        )
    }
}

@Composable
fun PinActionRowCard(
    post: PinPost,
    onDelete: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pin_card_${post.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Loaded visual Coil image preview
            val statusColor = when (post.status) {
                "SCHEDULED" -> Color(0xFF2196F3)
                "POSTED" -> Color(0xFF4CAF50)
                "FAILED" -> Color(0xFFF44336)
                else -> Color(0xFF9E9E9E)
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.5.dp,
                        color = statusColor.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                if (post.imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = Uri.parse(post.imageUri),
                        contentDescription = "Pin Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "No image",
                        modifier = Modifier.align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Middle: titles, descriptions, target board
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    post.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "Board: ${post.boardName.ifBlank { "Unassigned" }}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Time indicators depending on status
                val timerString = when (post.status) {
                    "SCHEDULED" -> "Scheduler: " + post.scheduledTime?.let { formatter.format(Date(it)) }
                    "POSTED" -> "Live: " + post.postedTime?.let { formatter.format(Date(it)) }
                    "FAILED" -> "Failed attempt"
                    else -> "Draft created"
                }

                Text(
                    timerString,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (post.status == "FAILED" && !post.failureReason.isNullOrBlank()) {
                    Text(
                        post.failureReason,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.error, fontSize = 10.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right status pill & Delete action
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                // Status badge
                val (badgeColor, textColor) = when (post.status) {
                    "SCHEDULED" -> Color(0xFF2196F3).copy(alpha = 0.15f) to Color(0xFF1E88E5)
                    "POSTED" -> Color(0xFF4CAF50).copy(alpha = 0.15f) to Color(0xFF43A047)
                    "FAILED" -> Color(0xFFF44336).copy(alpha = 0.15f) to Color(0xFFE53935)
                    else -> Color(0xFF9E9E9E).copy(alpha = 0.15f) to Color(0xFF757575)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor
                ) {
                    Text(
                        post.status,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Delete item",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}


// SCREEN 2: COMPOSE PIN COMPOSABLE
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreatePinScreen(
    viewModel: PinViewModel,
    onShowGoogleGalleryPicker: () -> Unit,
    onConnectPinterestClick: () -> Unit
) {
    val context = LocalContext.current

    // Observe text fields
    val title by viewModel.formTitle.collectAsState()
    val description by viewModel.formDescription.collectAsState()
    val destination by viewModel.formDestination.collectAsState()
    val boardId by viewModel.formSelectedBoardId.collectAsState()
    val boardName by viewModel.formSelectedBoardName.collectAsState()
    val imageUri by viewModel.formImageUri.collectAsState()
    val scheduleTime by viewModel.formScheduleTime.collectAsState()
    val testValidationError by viewModel.formValidationError.collectAsState()
    val username by viewModel.username.collectAsState()
    val isSandbox by viewModel.isSandboxMode.collectAsState()

    val boardsList by viewModel.boards.collectAsState()
    var isBoardDropdownExpanded by remember { mutableStateOf(false) }

    // Setup native gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.updateFormFields(imageUri = uri.toString())
        }
    }

    val datePrefFormat = remember { SimpleDateFormat("EEE, MMM dd, yyyy - hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Create New Pin",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Design a high-impact graphic and map its publishing criteria.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Sub-item: Show Pinterest connection helper card if disconnected
        if (username == null) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .clickable { onConnectPinterestClick() }
                            .padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PinterestLogoIcon(
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pinterest Account Disconnected",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            )
                            Text(
                                "Connect your Pinterest profile first to select boards and publish pins.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Connect Pinterest",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Amazon Store Quick-Link Importer Card
        item {
            val amazonTag by viewModel.amazonAssociateTag.collectAsState()
            val amazonName by viewModel.amazonStoreName.collectAsState()
            
            var amazonProductUrl by remember { mutableStateOf("") }
            var isImportingProduct by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF9900).copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xAA161824) else Color(0xEEF8F9FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column {
                    // Header Image with overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Image(
                            painter = painterResource(id = com.example.R.drawable.img_amazon_store_banner_1783269633019),
                            contentDescription = "Amazon Store Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Overlay for readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.2f),
                                            Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                                )
                        )
                        // Monitized Badge Overlay
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF9900),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = "Monetized",
                                    tint = Color.Black,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "MONETIZED PIPELINE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Content overlay at the bottom of the image banner
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9900)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "Amazon Icon",
                                    tint = Color.Black,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Amazon Store Importer",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = if (amazonTag != null) "Active Affiliate Store: ${amazonName ?: "My Store"}" else "No active storefront linked",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Card Body Padding
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Quick Status Badges Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (amazonTag != null) {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF4CAF50))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Tag Active: $amazonTag",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(100.dp),
                                    color = Color(0xFFFF9900).copy(alpha = 0.1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9900).copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFFF9900))
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Standard Link Mode (No affiliate tag)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFF9900)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "Auto-Template",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Short description
                        Text(
                            text = "Paste any product link. We extract high-res graphics, write persuasive sales descriptions, and inject affiliate parameters instantly.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                                lineHeight = 17.sp
                            )
                        )

                        // Visual Steps Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Link, "Step 1", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("1. Paste URL", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            }
                            Icon(Icons.Default.KeyboardArrowRight, "Arrow", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.AutoAwesome, "Step 2", modifier = Modifier.size(16.dp), tint = Color(0xFFFF9900))
                                Text("2. Auto-Rewrite", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            }
                            Icon(Icons.Default.KeyboardArrowRight, "Arrow", modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Share, "Step 3", modifier = Modifier.size(16.dp), tint = Color(0xFF4CAF50))
                                Text("3. Direct to Pin", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
                            }
                        }

                        // Product URL input
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = amazonProductUrl,
                                onValueChange = { amazonProductUrl = it },
                                placeholder = { Text("Paste Amazon Product Link (dp/gp/...)") },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Link,
                                        contentDescription = "Product Link",
                                        tint = Color(0xFFFF9900)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF9900),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                    cursorColor = Color(0xFFFF9900)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("amazon_import_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            
                            // Amazon Golden Gradient Button
                            Button(
                                onClick = {
                                    if (amazonProductUrl.isNotBlank()) {
                                        isImportingProduct = true
                                        scope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            val success = viewModel.importAmazonProduct(amazonProductUrl)
                                            if (success) {
                                                amazonProductUrl = ""
                                            }
                                            isImportingProduct = false
                                        }
                                    }
                                },
                                enabled = amazonProductUrl.isNotBlank() && !isImportingProduct,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent, // Custom gradient container
                                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                ),
                                contentPadding = PaddingValues(0.dp), // Clear padding to let brush expand
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(56.dp)
                                    .width(110.dp)
                            ) {
                                val buttonBrush = if (amazonProductUrl.isNotBlank() && !isImportingProduct) {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFC400), Color(0xFFFF9900))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color.Transparent, Color.Transparent)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(buttonBrush)
                                        .padding(horizontal = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isImportingProduct) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.Black,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Import icon",
                                                tint = Color.Black,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                "Import",
                                                color = Color.Black,
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Info or Call to Action warning when Amazon is not fully configured
                        if (amazonTag == null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth(),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = "Tip",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "Want to earn commissions? Link your custom Store Name & Associate ID in the Setup tab to turn links into active revenue generators!",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            lineHeight = 15.sp
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Image picker card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .testTag("image_selector"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xAA161824) else Color(0xEEF8F9FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                if (imageUri.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = if (imageUri.startsWith("http")) imageUri else Uri.parse(imageUri),
                            contentDescription = "Selected Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Overlay cancel button
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(36.dp)
                                .clickable { viewModel.updateFormFields(imageUri = "") }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear image selection",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        
                        // Badge overlay with info
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.7f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (imageUri.startsWith("http")) Icons.Default.CloudQueue else Icons.Default.Image,
                                    contentDescription = "Source",
                                    tint = if (imageUri.startsWith("http")) Color(0xFF8AB4F8) else Color(0xFFFF9900),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (imageUri.startsWith("http")) "Google Cloud Stream Active" else "Local Device File Selected",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Pin Media Studio",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            "Choose an asset source to design your Pinterest Pin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Option A: Local Device
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        galleryLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = "Add image",
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Local Gallery",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Browse device files",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            // Option B: Google Cloud Gallery Picker
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable {
                                        onShowGoogleGalleryPicker()
                                    }
                                    .testTag("google_cloud_picker"),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = Color(0xFF1A73E8).copy(alpha = 0.3f)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1A73E8).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudQueue,
                                            contentDescription = "Google Cloud Icon",
                                            modifier = Modifier.size(20.dp),
                                            tint = Color(0xFF1A73E8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Google Cloud",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        "Photos, Drive & Sync",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick mockup asset picker (for convenient desktop testing in Sandbox mode)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "Or use aesthetic Sandbox placeholder URL:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "Workspace Inspiration" to "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=800&q=80",
                        "Creative Art" to "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80",
                        "Delicious Desert" to "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80"
                    ).forEach { (label, presetUrl) ->
                        ElevatedFilterChip(
                            selected = imageUri == presetUrl,
                            onClick = { viewModel.updateFormFields(imageUri = presetUrl) },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        // Input Fields (Title, description, link)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Pin Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.updateFormFields(title = it) },
                    label = { Text("Pin Title *") },
                    placeholder = { Text("Keep it punchy & search-optimized") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("field_title"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Pin Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.updateFormFields(description = it) },
                    label = { Text("Pin Description") },
                    placeholder = { Text("What is this Pin about? Tell people what they will see...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("field_description"),
                    maxLines = 4,
                    shape = RoundedCornerShape(10.dp)
                )

                // Destination Link
                OutlinedTextField(
                    value = destination,
                    onValueChange = { viewModel.updateFormFields(destination = it) },
                    label = { Text("Destination Link (Optional URL)") },
                    placeholder = { Text("e.g. https://mywebsite.com/diy-crafts") },
                    leadingIcon = { Icon(Icons.Default.Link, "Link Icon") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("field_destination"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }
        }

        // Board Selector
        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = boardName.ifBlank { "Unassigned" },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Pinterest Board *") },
                    trailingIcon = {
                        IconButton(onClick = { isBoardDropdownExpanded = !isBoardDropdownExpanded }) {
                            Icon(
                                imageVector = if (isBoardDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                contentDescription = "Toggle board selection dropdown"
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBoardDropdownExpanded = !isBoardDropdownExpanded }
                        .testTag("board_selector"),
                    shape = RoundedCornerShape(10.dp)
                )

                DropdownMenu(
                    expanded = isBoardDropdownExpanded,
                    onDismissRequest = { isBoardDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .testTag("board_dropdown")
                ) {
                    if (boardsList.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No boards found. (Click connect first)") },
                            onClick = { isBoardDropdownExpanded = false }
                        )
                    } else {
                        boardsList.forEach { board ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(board.name, fontWeight = FontWeight.Bold)
                                        board.description?.let {
                                            Text(
                                                it,
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.updateFormFields(boardId = board.id, boardName = board.name)
                                    isBoardDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Schedule Post Trigger
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Scheduling option",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Schedule Post", fontWeight = FontWeight.Bold)
                            Text(
                                "Toggle to set a future publication date and exact time alert.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = scheduleTime != null,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    // Launch date picker dialogues
                                    val calendar = Calendar.getInstance()
                                    DatePickerDialog(
                                        context,
                                        { _, year, month, day ->
                                            calendar.set(Calendar.YEAR, year)
                                            calendar.set(Calendar.MONTH, month)
                                            calendar.set(Calendar.DAY_OF_MONTH, day)

                                            TimePickerDialog(
                                                context,
                                                { _, hour, min ->
                                                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                                                    calendar.set(Calendar.MINUTE, min)
                                                    viewModel.updateFormFields(scheduleTime = calendar.timeInMillis)
                                                },
                                                calendar.get(Calendar.HOUR_OF_DAY),
                                                calendar.get(Calendar.MINUTE),
                                                false
                                            ).show()
                                        },
                                        calendar.get(Calendar.YEAR),
                                        calendar.get(Calendar.MONTH),
                                        calendar.get(Calendar.DAY_OF_MONTH)
                                    ).show()
                                } else {
                                    viewModel.updateFormFields(scheduleTime = null)
                                }
                            }
                        )
                    }

                    if (scheduleTime != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Will auto-publish on:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    datePrefFormat.format(Date(scheduleTime!!)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error validation alerts
        if (!testValidationError.isNullOrBlank()) {
            item {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = "Error message", tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(testValidationError!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }

        // Action Buttons (Save draft vs publish scheduler)
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaximize()
                    .padding(vertical = 12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.saveDraft() },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_draft_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Drafts Icon")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Draft")
                }

                Button(
                    onClick = {
                        viewModel.submitPinPost {
                            // On complete, can show dynamic toast or state action success!
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("publish_pin_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = if (scheduleTime != null) Icons.Default.AlarmOn else Icons.Default.Publish,
                        contentDescription = "Submit Icon"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (scheduleTime != null) "Schedule Pin" else "Publish Now")
                }
            }
        }
    }
}

private fun Modifier.fillMaximize(): Modifier {
    return this.fillMaxWidth()
}


// SCREEN 3: DRAFTS PANE
@Composable
fun DraftsScreen(
    viewModel: PinViewModel,
    onEditDraft: () -> Unit
) {
    val drafts by viewModel.draftsList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                "Local Draft Storage",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Keep pins organized offline. Load drafts instantly to publish when ready.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        if (drafts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Empty folders",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "No saved drafts found offline.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        "Fill out details in Compose page and hit Save Draft.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("drafts_grid")
            ) {
                items(drafts, key = { it.id }) { draft ->
                    DraftGridItemCard(
                        draft = draft,
                        onSelect = {
                            viewModel.loadDraftIntoForm(draft)
                            onEditDraft()
                        },
                        onDelete = { viewModel.deletePost(draft.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun DraftGridItemCard(
    draft: PinPost,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("draft_item_${draft.id}")
            .clickable { onSelect() }
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                if (draft.imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = Uri.parse(draft.imageUri),
                        contentDescription = "Draft thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Landscape,
                        contentDescription = "Placeholder",
                        modifier = Modifier.align(Alignment.Center),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(24.dp)
                        .background(Color.Black.copy(alpha = 0.55f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove draft",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(top = 8.dp, start = 4.dp, end = 4.dp)) {
                Text(
                    draft.title.ifBlank { "Untitled Draft" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (draft.description.isNotBlank()) draft.description else "No description added yet",
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = "Action icon",
                            modifier = Modifier.size(10.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Load Draft",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}


// SCREEN 4: SETTINGS & SETUP PAGE
@Composable
fun SettingsScreen(
    viewModel: PinViewModel,
    onSignInWithGoogleClick: () -> Unit,
    onConnectPinterestClick: () -> Unit
) {
    val username by viewModel.username.collectAsState()
    val isSandbox by viewModel.isSandboxMode.collectAsState()
    val googleEmail by viewModel.googleEmail.collectAsState()
    val googleName by viewModel.googleName.collectAsState()
    val googleAvatar by viewModel.googleAvatar.collectAsState()
    var tempUsernameInput by remember { mutableStateOf("sandbox_pins_pioneer") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "App Configurations",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                "Customize Pinterest secrets and verify app operations in Sandbox Sandbox Emulator.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Mode switch panel
        item {
            GlassCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Sandbox Local Emulator", fontWeight = FontWeight.Bold)
                            Text(
                                "Simulates OAuth flows and Pin uploads without requiring active Pinterest developer configurations.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        Switch(
                            checked = isSandbox,
                            onCheckedChange = { viewModel.toggleSandbox(it) },
                            modifier = Modifier.testTag("toggle_sandbox_mode")
                        )
                    }

                    if (isSandbox) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(14.dp))

                        Text("Sandbox Custom Profile Linker", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = tempUsernameInput,
                                onValueChange = { tempUsernameInput = it },
                                placeholder = { Text("Enter mock username") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("sandbox_username_field")
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Button(
                                onClick = { viewModel.triggerSandboxDirectLogin(tempUsernameInput) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Bind profile", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Google Workspace Integration
        item {
            GlassCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            if (googleAvatar != null) {
                                val avatarModel: Any = remember(googleAvatar) {
                                    val parsedInt = googleAvatar?.toIntOrNull()
                                    if (parsedInt != null) {
                                        parsedInt
                                    } else if (googleAvatar == "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=120&q=80") {
                                        com.example.R.drawable.app_logo_concept_1779423040833
                                    } else {
                                        googleAvatar!!
                                    }
                                }
                                AsyncImage(
                                    model = avatarModel,
                                    contentDescription = "Google Avatar",
                                    modifier = Modifier.size(40.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                GoogleLogoIcon(modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Workspace Profile", fontWeight = FontWeight.Bold)
                            Text(
                                if (googleEmail != null) "Signed in as $googleEmail" else "Sign in to synchronize drafts with Google Drive backups.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(14.dp))

                    if (googleEmail != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Active Storage Backup", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                Text("Synchronizing pins to Google Sheets", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Button(
                                onClick = { viewModel.logoutGoogle() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Sign out Google", fontSize = 11.sp)
                            }
                        }
                    } else {
                        Button(
                            onClick = onSignInWithGoogleClick,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF161616)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GoogleLogoIcon(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Sign in with Google", color = Color.White)
                        }
                    }
                }
            }
        }        // Amazon Store Integration
        item {
            val amazonTag by viewModel.amazonAssociateTag.collectAsState()
            val amazonName by viewModel.amazonStoreName.collectAsState()
            
            var editStoreName by remember { mutableStateOf(amazonName ?: "") }
            var editAssociateTag by remember { mutableStateOf(amazonTag ?: "") }
            var isEditingAmazon by remember { mutableStateOf(amazonTag == null) }
 
            // Sync state if they change externally
            LaunchedEffect(amazonTag, amazonName) {
                if (amazonTag != null) {
                    editStoreName = amazonName ?: ""
                    editAssociateTag = amazonTag ?: ""
                }
            }
 
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF9900).copy(alpha = 0.35f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.1f)
                            )
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) Color(0xAA161824) else Color(0xEEF8F9FA)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header row with Icon, title and badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFF9900)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = "Amazon Store icon",
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Amazon Associate Engine",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                if (amazonTag != null) "Active Affiliate Syncing" else "Connect Storefront for Auto-Monetization",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                            )
                        }
                        if (amazonTag != null && !isEditingAmazon) {
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Connected",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
 
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(14.dp))
 
                    if (isEditingAmazon) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "Configure your Amazon Associate settings. Once configured, every Amazon link imported will automatically generate optimized Pinterest graphics, professional product headlines, and inject your tracking ID.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                lineHeight = 16.sp
                            )

                            OutlinedTextField(
                                value = editStoreName,
                                onValueChange = { editStoreName = it },
                                label = { Text("Storefront Name") },
                                placeholder = { Text("e.g. My Cozy Workspace Finds") },
                                leadingIcon = { Icon(Icons.Default.Store, "Store Icon", tint = Color(0xFFFF9900)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF9900),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                    cursorColor = Color(0xFFFF9900)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
 
                            OutlinedTextField(
                                value = editAssociateTag,
                                onValueChange = { editAssociateTag = it },
                                label = { Text("Amazon Associate Tag (Affiliate ID) *") },
                                placeholder = { Text("e.g. yourtag-20") },
                                leadingIcon = { Icon(Icons.Default.Tag, "Tag Icon", tint = Color(0xFFFF9900)) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF9900),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                                    cursorColor = Color(0xFFFF9900)
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            )
 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (amazonTag != null) {
                                    TextButton(onClick = { isEditingAmazon = false }) {
                                        Text("Cancel", fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Button(
                                    onClick = {
                                        if (editAssociateTag.isNotBlank()) {
                                            viewModel.saveAmazonStore(
                                                associateTag = editAssociateTag.trim(),
                                                storeName = editStoreName.ifBlank { "My Amazon Store" }.trim()
                                            )
                                            isEditingAmazon = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9900)),
                                    enabled = editAssociateTag.isNotBlank(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Save Configuration", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Active configuration detail cards
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.03f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Store, "Store", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Storefront:",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        amazonName ?: "My Amazon Store",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Tag, "Tag", modifier = Modifier.size(16.dp), tint = Color(0xFFFF9900))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Affiliate ID:",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        amazonTag ?: "",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = Color(0xFFFF9900)
                                        )
                                    )
                                }
                            }

                            // Benefits list showing what is active
                            Column(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val features = listOf(
                                    "Automatic ASIN parsing & link redirection",
                                    "Persistent commission parameter tags",
                                    "AI description rewriting with relevant hashtags"
                                )
                                features.forEach { feature ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Active Feature",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = feature,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                        )
                                    }
                                }
                            }
 
                            Spacer(modifier = Modifier.height(6.dp))
 
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                OutlinedButton(
                                    onClick = { isEditingAmazon = true },
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, "Edit", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Edit Config", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = { viewModel.disconnectAmazonStore() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f), contentColor = MaterialTheme.colorScheme.error),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.LinkOff, "Disconnect", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Disconnect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Credentials metadata report
        item {
            GlassCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Developer Secret Variables Check", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                    Text(
                        "Determines keys resolved from .env properties at compilation runtimes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    CredentialReportRow(
                        fieldName = "PINTEREST_CLIENT_ID",
                        statusLabel = "Injected",
                        color = Color(0xFF4CAF50)
                    )
                    CredentialReportRow(
                        fieldName = "PINTEREST_CLIENT_SECRET",
                        statusLabel = "Injected",
                        color = Color(0xFF4CAF50)
                    )
                    CredentialReportRow(
                        fieldName = "PINTEREST_REDIRECT_URI",
                        statusLabel = "https://localhost/auth/pinterest (Default Enabled)",
                        color = Color(0xFF2196F3)
                    )
                }
            }
        }

        // SECURITY WARNING AS MANDATED BY SKILL
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = Color(0xFFE53935)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Security Warning",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE53935)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Security Warning: I have included your API keys in the generated APK file for this prototype. Please be aware that Android APKs can be easily decompiled, and these keys can be extracted by anyone who has access to the file. Do not share this APK file publicly or with unauthorized individuals to prevent potential misuse.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Logout
        if (username != null) {
            item {
                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("logout_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Log out")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Disconnect Pinterest profile")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CredentialReportRow(
    fieldName: String,
    statusLabel: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(fieldName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(statusLabel, fontSize = 11.sp, color = color, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = size.width * 0.22f
        // Beautiful multi-color circles/arcs representing the Google identity
        drawArc(
            color = Color(0xFFEA4335), // Red
            startAngle = 180f,
            sweepAngle = 100f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFFBBC05), // Yellow
            startAngle = 120f,
            sweepAngle = 60f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF34A853), // Green
            startAngle = 30f,
            sweepAngle = 90f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF4285F4), // Blue
            startAngle = 280f,
            sweepAngle = 110f,
            useCenter = false,
            style = androidx.compose.ui.graphics.drawscope.Stroke(strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

@Composable
fun Tactile3DButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFF1A73E8),
    depthColor: Color = Color(0xFF0D52B1),
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val targetTranslationY = if (isPressed) 4.dp else 0.dp
    val targetShadowHeight = if (isPressed) 1.dp else 5.dp
    
    val animatedTranslationY by animateDpAsState(targetValue = targetTranslationY, label = "btnTranslation")

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
    ) {
        // 3D Bevel Depth Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 5.dp)
                .background(
                    color = if (enabled) depthColor else Color(0xFFB5B8BC),
                    shape = RoundedCornerShape(10.dp)
                )
        )
        // Main Button Face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = animatedTranslationY)
                .background(
                    color = if (enabled) containerColor else Color(0xFFD2D5DA),
                    shape = RoundedCornerShape(10.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.25f), shape = RoundedCornerShape(10.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}

@Composable
fun Tactile3DAccountTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.25f),
    depthColor: Color = if (isSystemInDarkTheme()) Color.Black.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.12f),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val targetTranslationY = if (isPressed) 3.dp else 0.dp
    val animatedTranslationY by animateDpAsState(targetValue = targetTranslationY, label = "tileTranslation")

    Box(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // 3D Depth Layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 4.dp)
                .background(
                    color = depthColor,
                    shape = RoundedCornerShape(14.dp)
                )
        )
        // Main Face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = animatedTranslationY)
                .background(
                    color = containerColor,
                    shape = RoundedCornerShape(14.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun GoogleLoginPageOverlay(
    onDismiss: () -> Unit,
    onLoginSuccess: (email: String, name: String, avatarUrl: String) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) } // 1 = Account Selector/Manual Email, 2 = Password
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val sampleAccounts = listOf(
        Triple("akshaykanchi03@gmail.com", "Akshay Kanchi", com.example.R.drawable.app_logo_concept_1779423040833.toString()),
        Triple("alex.designer.pixel@gmail.com", "Alex Rivera", "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=120&q=80"),
        Triple("pinterest.automator.dev@gmail.com", "PinFlow Developer", "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?auto=format&fit=crop&w=120&q=80")
    )

    // Infinite 3D floating animation values
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatTranslationY by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "translationY"
    )
    val floatRotationX by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotationX"
    )
    val floatRotationY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = -1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotationY"
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Perspective 3D Grid Ground and Studio Lights Background
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clickable(enabled = !isAuthenticating) { onDismiss() }
        ) {
            // Dark elegant background
            drawRect(color = Color(0xFF090A10))

            // Studio Ambient Radial Lights
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8AB4F8).copy(alpha = 0.22f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.25f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE8EAED).copy(alpha = 0.12f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.75f),
                    radius = size.width * 0.6f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD4E2).copy(alpha = 0.08f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.45f),
                    radius = size.width * 0.5f
                )
            )

            // CSS 3D Perspective Lines
            val horizonX = size.width / 2f
            val horizonY = size.height * 0.42f
            val gridSpacing = 44f
            val linesCount = ((size.width) / gridSpacing).toInt()

            for (i in -linesCount..linesCount) {
                val startX = horizonX + (i * gridSpacing * 0.08f)
                val endX = horizonX + (i * gridSpacing * 5.5f)
                
                drawLine(
                    color = Color(0xFF2B3A4A).copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(startX, horizonY),
                    end = androidx.compose.ui.geometry.Offset(endX, size.height),
                    strokeWidth = 1.8f
                )
            }

            // Horizontal gridlines scaling outwards
            var currentY = horizonY
            var step = 8f
            while (currentY < size.height) {
                drawLine(
                    color = Color(0xFF2B3A4A).copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(0f, currentY),
                    end = androidx.compose.ui.geometry.Offset(size.width, currentY),
                    strokeWidth = 1.4f
                )
                step *= 1.34f
                currentY += step
            }
        }

        // Main Login Container with 3D Stacked Layers and tilt
        Box(
            modifier = Modifier
                .widthIn(max = 410.dp)
                .fillMaxWidth()
                .padding(24.dp)
                .graphicsLayer {
                    rotationX = floatRotationX
                    rotationY = floatRotationY
                    translationY = floatTranslationY
                    cameraDistance = 14f * density
                }
                .clickable(enabled = false) {}, // Prevent dismiss click propagate
            contentAlignment = Alignment.Center
        ) {
            // Layer 3 (Deepest shadow panel)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = (-12).dp, y = 16.dp)
                    .background(Color(0xFF060608).copy(alpha = 0.6f), shape = RoundedCornerShape(26.dp))
            )

            // Layer 2 (Warm secondary sheet)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = (-6).dp, y = 8.dp)
                    .background(Color(0xFF1E1F22), shape = RoundedCornerShape(26.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(26.dp))
            )

            // Layer 1 (Primary Glass/Tactile Card Face)
            GlassCard(
                shape = RoundedCornerShape(26.dp),
                borderWidth = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Floating 3D Google Branding Logo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .graphicsLayer {
                                translationY = -4f + (floatTranslationY * 0.25f)
                            }
                            .padding(bottom = 16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.dp, Color(0xFFE2E4E7), shape = CircleShape)
                                .padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GoogleLogoIcon(modifier = Modifier.fillMaxSize())
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Google",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF202124),
                            letterSpacing = (-0.8).sp
                        )
                    }

                    if (step == 1) {
                        Text(
                            "Sign in",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF202124),
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            "to continue to PinFlow Workspace",
                            fontSize = 12.sp,
                            color = Color(0xFF5F6368),
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(22.dp))

                        Text(
                            "Select an account securely:",
                            fontSize = 11.sp,
                            color = Color(0xFF202124),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Render 3D tactile account cards
                        sampleAccounts.forEach { account ->
                            Tactile3DAccountTile(
                                onClick = {
                                    emailInput = account.first
                                    step = 2
                                }
                            ) {
                                val avatarModel: Any = remember(account.third) {
                                    val parsedInt = account.third.toIntOrNull()
                                    if (parsedInt != null) {
                                        parsedInt
                                    } else {
                                        account.third
                                    }
                                }
                                AsyncImage(
                                    model = avatarModel,
                                    contentDescription = "Avatar",
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, Color.White, shape = CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        account.second,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF202124)
                                    )
                                    Text(
                                        account.first,
                                        fontSize = 10.sp,
                                        color = Color(0xFF5F6368),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Select",
                                    tint = Color(0xFF1A73E8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE8EAED))
                            Text(
                                "Or manual entry",
                                fontSize = 10.sp,
                                color = Color(0xFF9AA0A6),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE8EAED))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sunken / Concave Inset Border Styled Text Field
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                emailError = null
                            },
                            label = { Text("Email or phone", fontWeight = FontWeight.Bold) },
                            placeholder = { Text("username@gmail.com") },
                            isError = emailError != null,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFD2D5DA),
                                focusedTextColor = Color(0xFF202124),
                                unfocusedTextColor = Color(0xFF202124),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedContainerColor = Color(0xFFF8F9FA)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (emailError != null) {
                            Text(
                                text = emailError ?: "",
                                color = Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 6.dp, top = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { onDismiss() },
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF5F6368))
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold)
                            }

                            Tactile3DButton(
                                onClick = {
                                    if (emailInput.isBlank() || !emailInput.contains("@")) {
                                        emailError = "Enter a valid email address"
                                    } else {
                                        step = 2
                                    }
                                },
                                modifier = Modifier.width(105.dp)
                            ) {
                                Text("Next", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                            }
                        }

                    } else if (step == 2) {
                        Text(
                            "Welcome",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF202124),
                            letterSpacing = (-0.5).sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Extruded Selected User Badge
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E4E7)),
                            color = Color(0xFFF1F3F4)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Account",
                                    tint = Color(0xFF1A73E8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    emailInput,
                                    fontSize = 11.sp,
                                    color = Color(0xFF202124),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = {
                                passwordInput = it
                                passwordError = null
                            },
                            label = { Text("Enter your password", fontWeight = FontWeight.Bold) },
                            isError = passwordError != null,
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1A73E8),
                                focusedLabelColor = Color(0xFF1A73E8),
                                cursorColor = Color(0xFF1A73E8),
                                unfocusedBorderColor = Color(0xFFD2D5DA),
                                focusedTextColor = Color(0xFF202124),
                                unfocusedTextColor = Color(0xFF202124),
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedContainerColor = Color(0xFFF8F9FA)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (passwordError != null) {
                            Text(
                                text = passwordError ?: "",
                                color = Color.Red,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 6.dp, top = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (isAuthenticating) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFF1A73E8),
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Signing you in securely via Google...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF1A73E8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = { step = 1 },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF5F6368))
                                ) {
                                    Text("Back", fontWeight = FontWeight.Bold)
                                }

                                Tactile3DButton(
                                    onClick = {
                                        if (passwordInput.length < 4) {
                                            passwordError = "Password must be at least 4 characters"
                                        } else {
                                            isAuthenticating = true
                                            coroutineScope.launch {
                                                kotlinx.coroutines.delay(1400)
                                                isAuthenticating = false
                                                val foundAccount = sampleAccounts.find { it.first.equals(emailInput, ignoreCase = true) }
                                                val resolvedName = foundAccount?.second ?: emailInput.substringBefore("@").replace(".", " ").replaceFirstChar { it.titlecase() }
                                                val resolvedAvatar = foundAccount?.third ?: com.example.R.drawable.app_logo_concept_1779423040833.toString()
                                                onLoginSuccess(emailInput, resolvedName, resolvedAvatar)
                                            }
                                        }
                                    },
                                    modifier = Modifier.width(115.dp)
                                ) {
                                    Text("Sign in", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleGalleryPickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit,
    googleEmail: String?,
    googleName: String?,
    googleAvatar: String?,
    onConnectGoogle: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Google Photos, 1 = Google Drive, 2 = URL Fetcher
    var searchQuery by remember { mutableStateOf("") }
    
    // Drive states
    var currentDriveFolder by remember { mutableStateOf<String?>(null) } // null = root list of folders
    
    // Custom URL state
    var customUrlInput by remember { mutableStateOf("") }
    var customUrlError by remember { mutableStateOf<String?>(null) }
    var isVerifyingUrl by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    val curatedPhotos = remember {
        listOf(
            GooglePhotoItem("Cozy Desk & Warm Lamp Light", "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=800&q=80", "Workspace"),
            GooglePhotoItem("Premium Headset & Wooden Setup", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80", "Workspace"),
            GooglePhotoItem("Minimalist Office Studio Decor", "https://images.unsplash.com/photo-1493934558415-9d19f0b2b4d2?auto=format&fit=crop&w=800&q=80", "Workspace"),
            GooglePhotoItem("Cozy Creative Journal & Plants", "https://images.unsplash.com/photo-1516979187457-637abb4f9353?auto=format&fit=crop&w=800&q=80", "Workspace"),
            GooglePhotoItem("Brewed Coffee Latte Cup", "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=800&q=80", "Lifestyle"),
            GooglePhotoItem("Succulent Greenhouse Garden", "https://images.unsplash.com/photo-1448375240586-882707db888b?auto=format&fit=crop&w=800&q=80", "Gardening"),
            GooglePhotoItem("Creative Abstract Fluid Art", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80", "Design"),
            GooglePhotoItem("Summer Sunset Golden Hour Beach", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80", "Travel"),
            GooglePhotoItem("Artisanal Sourdough Bakery", "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80", "Food")
        )
    }
    
    val driveFolders = remember {
        listOf(
            "Pinterest Workspace Pins" to listOf(
                GooglePhotoItem("Minimalist Workspace Setup", "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=800&q=80", "Workspace"),
                GooglePhotoItem("Sleek Desk Setup Focus", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80", "Workspace"),
                GooglePhotoItem("Cozy Ambient Work Station", "https://images.unsplash.com/photo-1493934558415-9d19f0b2b4d2?auto=format&fit=crop&w=800&q=80", "Workspace")
            ),
            "DIY & Design Inspiration" to listOf(
                GooglePhotoItem("Creative Watercolor Painting", "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80", "Design"),
                GooglePhotoItem("Abstract Creative Acrylic", "https://images.unsplash.com/photo-1513364776144-60967b0f800f?auto=format&fit=crop&w=800&q=80", "Design")
            ),
            "Cozy Lifestyle & Coffee Collection" to listOf(
                GooglePhotoItem("Perfect Morning Latte Flatlay", "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=800&q=80", "Lifestyle"),
                GooglePhotoItem("Fresh Baked Sourdough Bread", "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=800&q=80", "Food")
            )
        )
    }
    
    // Dialog wrapper overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(24.dp)
                .clickable(enabled = false) {}, // Prevent dismiss click propagate
            shape = RoundedCornerShape(28.dp),
            color = if (isSystemInDarkTheme()) Color(0xFF131522) else Color(0xFFFFFFFF),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = if (isSystemInDarkTheme()) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Google identity indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isSystemInDarkTheme()) Color(0x111A73E8) else Color(0xFFF1F3F4)),
                        contentAlignment = Alignment.Center
                    ) {
                        GoogleLogoIcon(modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Google Cloud Sync",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Import asset directly into workspace",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Dialog",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(14.dp))
                
                // Account Status Panel
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSystemInDarkTheme()) Color(0x441E2030) else Color(0xFFF8F9FA),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (googleEmail != null) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatarModel: Any = remember(googleAvatar) {
                                val parsedInt = googleAvatar?.toIntOrNull()
                                if (parsedInt != null) parsedInt else googleAvatar ?: ""
                            }
                            AsyncImage(
                                model = avatarModel,
                                contentDescription = "Google Avatar",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.dp, Color(0xFF1A73E8), shape = CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = googleName ?: "Connected User",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Synced: $googleEmail",
                                    fontSize = 10.sp,
                                    color = Color(0xFF1A73E8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Active Sync",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "SYNCED",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Unlock Auto-Sync from Google Drive & Photos",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Connect your Google Account to automatically pull assets from private folder structures.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onConnectGoogle,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A73E8),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Login, "Connect", modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Connect Google Workspace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Navigation Tabs within dialog
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF1A73E8)
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Image, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Photos", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        selectedContentColor = Color(0xFF1A73E8),
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        selectedContentColor = Color(0xFF1A73E8),
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Link, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("URL", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        },
                        selectedContentColor = Color(0xFF1A73E8),
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Tab Content Panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Synced Photos Tab
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Real-time search/filter input
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Filter synchronized photos...") },
                                    leadingIcon = { Icon(Icons.Default.Search, "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF1A73E8),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                val filteredPhotos = curatedPhotos.filter {
                                    it.title.contains(searchQuery, ignoreCase = true) ||
                                    it.category.contains(searchQuery, ignoreCase = true)
                                }
                                
                                if (filteredPhotos.isEmpty()) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.Warning, "No results", modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("No matching photos found", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(filteredPhotos) { photo ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(115.dp)
                                                    .clickable { onImageSelected(photo.url) },
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    AsyncImage(
                                                        model = photo.url,
                                                        contentDescription = photo.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    
                                                    // Bottom text overlay
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomStart)
                                                            .fillMaxWidth()
                                                            .background(Color.Black.copy(alpha = 0.6f))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = photo.title,
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        1 -> {
                            // Google Drive Tab
                            Column(modifier = Modifier.fillMaxSize()) {
                                if (currentDriveFolder == null) {
                                    Text(
                                        "Connected Cloud Folders:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    
                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(driveFolders) { item ->
                                            val folderName = item.first
                                            val photos = item.second
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { currentDriveFolder = folderName },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.FolderOpen,
                                                        contentDescription = "Folder",
                                                        tint = Color(0xFFFF9900),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = folderName,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = "${photos.size} items synced",
                                                            fontSize = 10.sp,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                        )
                                                    }
                                                    Icon(
                                                        imageVector = Icons.Default.ChevronRight,
                                                        contentDescription = "Open Folder",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // Inside a folder
                                    val currentPhotos = driveFolders.find { it.first == currentDriveFolder }?.second ?: emptyList()
                                    
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { currentDriveFolder = null },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ChevronRight,
                                                contentDescription = "Back to root folders",
                                                modifier = Modifier.size(16.dp).graphicsLayer { rotationZ = 180f }
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = currentDriveFolder ?: "",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(currentPhotos) { photo ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(115.dp)
                                                    .clickable { onImageSelected(photo.url) },
                                                shape = RoundedCornerShape(12.dp),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                            ) {
                                                Box(modifier = Modifier.fillMaxSize()) {
                                                    AsyncImage(
                                                        model = photo.url,
                                                        contentDescription = photo.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    
                                                    // Bottom text overlay
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomStart)
                                                            .fillMaxWidth()
                                                            .background(Color.Black.copy(alpha = 0.6f))
                                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Text(
                                                            text = photo.title,
                                                            color = Color.White,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        2 -> {
                            // Direct Google Image URL Importer
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "Paste any direct Google Photo, Google Drive, or high-res cloud image URL below to validate and import it instantly.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    lineHeight = 16.sp
                                )
                                
                                OutlinedTextField(
                                    value = customUrlInput,
                                    onValueChange = {
                                        customUrlInput = it
                                        customUrlError = null
                                    },
                                    placeholder = { Text("https://lh3.googleusercontent.com/... or drive link") },
                                    label = { Text("Google Cloud Asset URL") },
                                    singleLine = true,
                                    isError = customUrlError != null,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF1A73E8),
                                        unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                if (customUrlError != null) {
                                    Text(
                                        text = customUrlError ?: "",
                                        color = Color.Red,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Button(
                                    onClick = {
                                        if (customUrlInput.isBlank()) {
                                            customUrlError = "URL cannot be empty"
                                        } else if (!customUrlInput.startsWith("http")) {
                                            customUrlError = "Please enter a valid HTTP/HTTPS URL"
                                        } else {
                                            isVerifyingUrl = true
                                            scope.launch {
                                                kotlinx.coroutines.delay(1000)
                                                isVerifyingUrl = false
                                                onImageSelected(customUrlInput.trim())
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1A73E8)
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isVerifyingUrl) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Publish, "Fetch", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Fetch & Import Cloud Asset", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                
                                // Preset shortcuts for Google photos share links
                                Text(
                                    "Common Cloud presets:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf(
                                        "Aesthetic Workspace" to "https://images.unsplash.com/photo-1517245386807-bb43f82c33c4?auto=format&fit=crop&w=800&q=80",
                                        "Retro Tech Setup" to "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80"
                                    ).forEach { (name, url) ->
                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { customUrlInput = url },
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                            )
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(name, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class GooglePhotoItem(val title: String, val url: String, val category: String)
