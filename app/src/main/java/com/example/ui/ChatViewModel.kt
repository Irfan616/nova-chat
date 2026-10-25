package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthService
import com.example.auth.AuthState
import com.example.auth.AuthUser
import com.example.data.ActiveCallInfo
import com.example.data.NovaChatRepository
import com.example.data.entity.CallEntity
import com.example.data.entity.CommunitySpaceEntity
import com.example.data.entity.ConversationEntity
import com.example.data.entity.MessageEntity
import com.example.data.entity.StoryEntity
import com.example.data.entity.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    CHATS, CALLS, COMMUNITIES, UPDATES, SETTINGS
}

enum class ChatFilter {
    ALL, UNREAD, GROUPS, CHANNELS
}

sealed class AppScreen {
    data class Main(val tab: MainTab = MainTab.CHATS) : AppScreen()
    data class Conversation(val conversationId: String) : AppScreen()
    object ActiveCall : AppScreen()
    data class StoryViewer(val statusId: String) : AppScreen()
    object Auth : AppScreen()
}

class ChatViewModel(
    private val repository: NovaChatRepository,
    val authService: AuthService? = null
) : ViewModel() {

    // Firebase Auth State
    val authState: StateFlow<AuthState>? = authService?.authState

    // Screen navigation stack
    private val _screen = MutableStateFlow<AppScreen>(AppScreen.Main(MainTab.CHATS))
    val screen: StateFlow<AppScreen> = _screen.asStateFlow()

    // Main bottom bar tab
    private val _currentTab = MutableStateFlow(MainTab.CHATS)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Chat filter
    private val _chatFilter = MutableStateFlow(ChatFilter.ALL)
    val chatFilter: StateFlow<ChatFilter> = _chatFilter.asStateFlow()

    // Global and in-chat search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Settings
    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _wallpaperIndex = MutableStateFlow(0)
    val wallpaperIndex: StateFlow<Int> = _wallpaperIndex.asStateFlow()

    private val _accentColorIndex = MutableStateFlow(0) // 0=Cyan, 1=Violet, 2=Mint, 3=Rose
    val accentColorIndex: StateFlow<Int> = _accentColorIndex.asStateFlow()

    private val _isUrduRtl = MutableStateFlow(false)
    val isUrduRtl: StateFlow<Boolean> = _isUrduRtl.asStateFlow()

    private val _readReceipts = MutableStateFlow(true)
    val readReceipts: StateFlow<Boolean> = _readReceipts.asStateFlow()

    private val _cacheClearedNotice = MutableStateFlow(false)
    val cacheClearedNotice: StateFlow<Boolean> = _cacheClearedNotice.asStateFlow()

    // Dialog flags
    val showNewChatDialog = MutableStateFlow(false)
    val showNewGroupDialog = MutableStateFlow(false)
    val showCreateStoryDialog = MutableStateFlow(false)
    val showSecurityInfoDialog = MutableStateFlow(false)
    val showProfileEditDialog = MutableStateFlow(false)
    val showAccountSwitcherDialog = MutableStateFlow(false)

    // Data from repository
    val activeConversations: StateFlow<List<ConversationEntity>> = repository.activeConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedConversations: StateFlow<List<ConversationEntity>> = repository.archivedConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<UserEntity?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allUsers: StateFlow<List<UserEntity>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calls: StateFlow<List<CallEntity>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val statuses: StateFlow<List<StoryEntity>> = repository.allStatuses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communities: StateFlow<List<CommunitySpaceEntity>> = repository.allCommunities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val typingStatus: StateFlow<Map<String, String?>> = repository.typingStatus
    val activeCall: StateFlow<ActiveCallInfo?> = repository.activeCall
    val playingVoiceId: StateFlow<String?> = repository.playingVoiceId
    val voiceProgress: StateFlow<Map<String, Float>> = repository.voiceProgress

    // Navigation functions
    fun navigateToTab(tab: MainTab) {
        _currentTab.value = tab
        _screen.value = AppScreen.Main(tab)
    }

    fun openConversation(conversationId: String) {
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
        _screen.value = AppScreen.Conversation(conversationId)
    }

    fun navigateBack() {
        when (_screen.value) {
            is AppScreen.Conversation, is AppScreen.ActiveCall, is AppScreen.StoryViewer, is AppScreen.Auth -> {
                _screen.value = AppScreen.Main(_currentTab.value)
            }
            is AppScreen.Main -> {}
        }
    }

    fun setChatFilter(filter: ChatFilter) {
        _chatFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setWallpaperIndex(index: Int) {
        _wallpaperIndex.value = index
    }

    fun setAccentColorIndex(index: Int) {
        _accentColorIndex.value = index
    }

    fun toggleUrduLanguage() {
        _isUrduRtl.value = !_isUrduRtl.value
    }

    fun toggleReadReceipts() {
        _readReceipts.value = !_readReceipts.value
    }

    fun clearCache() {
        _cacheClearedNotice.value = true
        viewModelScope.launch {
            kotlinx.coroutines.delay(2000)
            _cacheClearedNotice.value = false
        }
    }

    // Messaging
    fun getMessagesForConversation(convId: String): StateFlow<List<MessageEntity>> {
        return repository.getMessages(convId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun getConversationDetails(convId: String): StateFlow<ConversationEntity?> {
        return repository.getConversation(convId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        type: String = "TEXT",
        mediaUrl: String? = null,
        mediaDurationMs: Long = 0L,
        mediaSizeText: String? = null,
        replyToId: String? = null,
        replyToSnippet: String? = null,
        replyToSender: String? = null
    ) {
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = conversationId,
                text = text,
                type = type,
                mediaUrl = mediaUrl,
                mediaDurationMs = mediaDurationMs,
                mediaSizeText = mediaSizeText,
                replyToId = replyToId,
                replyToSnippet = replyToSnippet,
                replyToSender = replyToSender
            )
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.addReaction(messageId, emoji)
        }
    }

    fun togglePinMessage(messageId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePinMessage(messageId, isPinned)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun editMessage(messageId: String, newText: String) {
        viewModelScope.launch {
            repository.editMessage(messageId, newText)
        }
    }

    fun togglePinConversation(conversationId: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePin(conversationId, isPinned)
        }
    }

    fun toggleMuteConversation(conversationId: String, isMuted: Boolean) {
        viewModelScope.launch {
            repository.toggleMute(conversationId, isMuted)
        }
    }

    fun toggleArchiveConversation(conversationId: String, isArchived: Boolean) {
        viewModelScope.launch {
            repository.toggleArchive(conversationId, isArchived)
        }
    }

    fun setDisappearingTimer(conversationId: String, seconds: Long) {
        viewModelScope.launch {
            repository.setDisappearingTimer(conversationId, seconds)
        }
    }

    fun togglePlayVoice(messageId: String, durationMs: Long) {
        repository.togglePlayVoice(messageId, durationMs)
    }

    // Calls
    fun startCall(peerId: String, peerName: String, peerAvatar: String, isVideo: Boolean) {
        repository.startCall(peerId, peerName, peerAvatar, isVideo)
        _screen.value = AppScreen.ActiveCall
    }

    fun toggleCallMute() = repository.toggleCallMute()
    fun toggleCallSpeaker() = repository.toggleCallSpeaker()
    fun toggleCallVideo() = repository.toggleCallVideo()
    fun flipCamera() = repository.flipCamera()
    fun endCall() {
        repository.endCall()
        navigateBack()
    }

    // Status / Stories
    fun postStatus(caption: String, bgGradientIndex: Int) {
        viewModelScope.launch {
            repository.postStatus(caption, bgGradientIndex)
        }
    }

    fun openStoryViewer(statusId: String) {
        viewModelScope.launch {
            repository.markStatusViewed(statusId)
        }
        _screen.value = AppScreen.StoryViewer(statusId)
    }

    // Account Switcher
    fun switchUserAccount(targetUserId: String) {
        viewModelScope.launch {
            repository.switchUserAccount(targetUserId)
        }
    }

    fun updateProfile(name: String, bio: String, statusPhrase: String) {
        viewModelScope.launch {
            repository.updateProfile(name, bio, statusPhrase)
        }
    }

    fun createNewChat(title: String, isGroup: Boolean, initialMessage: String) {
        viewModelScope.launch {
            val convId = repository.createNewConversation(title, isGroup, initialMessage)
            openConversation(convId)
        }
    }

    // Firebase Authentication flows
    fun signInWithEmail(email: String, pass: String, onResult: (Result<AuthUser>) -> Unit = {}) {
        viewModelScope.launch {
            val result = authService?.signInWithEmail(email, pass)
                ?: Result.failure(IllegalStateException("AuthService not initialized"))
            onResult(result)
        }
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String?, onResult: (Result<AuthUser>) -> Unit = {}) {
        viewModelScope.launch {
            val result = authService?.signUpWithEmail(email, pass, displayName)
                ?: Result.failure(IllegalStateException("AuthService not initialized"))
            onResult(result)
        }
    }

    fun signOutFromFirebase() {
        viewModelScope.launch {
            authService?.signOut()
        }
    }
}

class ChatViewModelFactory(
    private val repository: NovaChatRepository,
    private val authService: AuthService? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatViewModel(repository, authService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
