package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Update
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ChatViewModel
import com.example.ui.MainTab
import com.example.ui.components.AvatarView
import com.example.ui.components.CreateStoryDialog
import com.example.ui.components.NewChatDialog
import com.example.ui.components.NewGroupDialog
import com.example.ui.components.ProfileEditDialog
import com.example.ui.components.SecurityFingerprintDialog
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun MainScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val conversations by viewModel.activeConversations.collectAsState()
    val statuses by viewModel.statuses.collectAsState()
    val isUrdu by viewModel.isUrduRtl.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()

    val totalUnread = conversations.sumOf { it.unreadCount }
    val hasUnreadStatuses = statuses.any { !it.isViewed }

    val layoutDirection = if (isUrdu) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = ObsidianBackground,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ObsidianSurface)
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Brand Logo & Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.navigateToTab(MainTab.CHATS) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(NovaCyan, NovaViolet, NovaCyan)
                                        )
                                    )
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(ObsidianBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "N",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp,
                                    color = NovaCyan
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = if (isUrdu) "نووا چیٹ" else "NovaChat",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimaryDark,
                                letterSpacing = 0.5.sp
                            )
                        }

                        // Right actions (Search, Quick Profile / Switcher)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    isSearchExpanded = !isSearchExpanded
                                    if (!isSearchExpanded) viewModel.setSearchQuery("")
                                },
                                modifier = Modifier.testTag("main_search_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (isSearchExpanded) NovaCyan else TextSecondaryDark
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            // Current User Quick Avatar
                            AvatarView(
                                name = currentUser?.name ?: "Alex",
                                avatarUrl = currentUser?.avatarUrl ?: "",
                                size = 32.dp,
                                modifier = Modifier
                                    .clickable { viewModel.navigateToTab(MainTab.SETTINGS) }
                            )
                        }
                    }

                    // Expandable search bar
                    AnimatedVisibility(visible = isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search messages, contacts, groups...", fontSize = 13.sp, color = TextMutedDark) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ObsidianSurfaceElevated,
                                unfocusedContainerColor = ObsidianSurfaceElevated,
                                focusedBorderColor = NovaCyan,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .testTag("main_search_input")
                        )
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = ObsidianSurface,
                    contentColor = TextPrimaryDark,
                    tonalElevation = 8.dp
                ) {
                    // Chats
                    NavigationBarItem(
                        selected = currentTab == MainTab.CHATS,
                        onClick = { viewModel.navigateToTab(MainTab.CHATS) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalUnread > 0) {
                                        Badge(
                                            containerColor = NovaCyan,
                                            contentColor = Color.Black
                                        ) {
                                            Text(totalUnread.toString(), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainTab.CHATS) Icons.Filled.Chat else Icons.Outlined.Chat,
                                    contentDescription = "Chats"
                                )
                            }
                        },
                        label = { Text(if (isUrdu) "چیٹس" else "Chats", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NovaCyan,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_chats")
                    )

                    // Calls
                    NavigationBarItem(
                        selected = currentTab == MainTab.CALLS,
                        onClick = { viewModel.navigateToTab(MainTab.CALLS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.CALLS) Icons.Filled.Call else Icons.Outlined.Call,
                                contentDescription = "Calls"
                            )
                        },
                        label = { Text(if (isUrdu) "کالز" else "Calls", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NovaCyan,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_calls")
                    )

                    // Communities
                    NavigationBarItem(
                        selected = currentTab == MainTab.COMMUNITIES,
                        onClick = { viewModel.navigateToTab(MainTab.COMMUNITIES) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.COMMUNITIES) Icons.Filled.Groups else Icons.Outlined.Groups,
                                contentDescription = "Communities"
                            )
                        },
                        label = { Text(if (isUrdu) "کمیونٹیز" else "Communities", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NovaCyan,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_communities")
                    )

                    // Updates
                    NavigationBarItem(
                        selected = currentTab == MainTab.UPDATES,
                        onClick = { viewModel.navigateToTab(MainTab.UPDATES) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (hasUnreadStatuses) {
                                        Badge(
                                            containerColor = NovaViolet
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (currentTab == MainTab.UPDATES) Icons.Filled.Update else Icons.Outlined.Update,
                                    contentDescription = "Updates"
                                )
                            }
                        },
                        label = { Text(if (isUrdu) "اپڈیٹس" else "Updates", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NovaCyan,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_updates")
                    )

                    // Settings
                    NavigationBarItem(
                        selected = currentTab == MainTab.SETTINGS,
                        onClick = { viewModel.navigateToTab(MainTab.SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Settings"
                            )
                        },
                        label = { Text(if (isUrdu) "ترتیبات" else "Settings", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = NovaCyan,
                            indicatorColor = NovaCyan,
                            unselectedIconColor = TextSecondaryDark,
                            unselectedTextColor = TextMutedDark
                        ),
                        modifier = Modifier.testTag("nav_tab_settings")
                    )
                }
            },
            floatingActionButton = {
                when (currentTab) {
                    MainTab.CHATS -> {
                        FloatingActionButton(
                            onClick = { viewModel.showNewChatDialog.value = true },
                            containerColor = NovaCyan,
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier.testTag("fab_new_chat")
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = "New Chat")
                        }
                    }
                    MainTab.CALLS -> {
                        FloatingActionButton(
                            onClick = {
                                // Start call with first contact
                                val firstContact = conversations.firstOrNull()
                                if (firstContact != null) {
                                    viewModel.startCall(firstContact.id, firstContact.title, firstContact.avatarUrl, false)
                                }
                            },
                            containerColor = NovaCyan,
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier.testTag("fab_new_call")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "New Call")
                        }
                    }
                    MainTab.COMMUNITIES -> {
                        FloatingActionButton(
                            onClick = { viewModel.showNewGroupDialog.value = true },
                            containerColor = NovaViolet,
                            contentColor = Color.White,
                            shape = CircleShape,
                            modifier = Modifier.testTag("fab_new_community")
                        ) {
                            Icon(Icons.Default.Groups, contentDescription = "New Group")
                        }
                    }
                    MainTab.UPDATES -> {
                        FloatingActionButton(
                            onClick = { viewModel.showCreateStoryDialog.value = true },
                            containerColor = NovaCyan,
                            contentColor = Color.Black,
                            shape = CircleShape,
                            modifier = Modifier.testTag("fab_new_status")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "New Status")
                        }
                    }
                    MainTab.SETTINGS -> {}
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentTab) {
                    MainTab.CHATS -> ChatsTab(viewModel = viewModel)
                    MainTab.CALLS -> CallsTab(viewModel = viewModel)
                    MainTab.COMMUNITIES -> CommunitiesTab(viewModel = viewModel)
                    MainTab.UPDATES -> UpdatesTab(viewModel = viewModel)
                    MainTab.SETTINGS -> SettingsTab(viewModel = viewModel)
                }
            }
        }
    }

    // Interactive Dialogs
    val showNewChat by viewModel.showNewChatDialog.collectAsState()
    if (showNewChat) {
        NewChatDialog(viewModel = viewModel, onDismiss = { viewModel.showNewChatDialog.value = false })
    }

    val showNewGroup by viewModel.showNewGroupDialog.collectAsState()
    if (showNewGroup) {
        NewGroupDialog(viewModel = viewModel, onDismiss = { viewModel.showNewGroupDialog.value = false })
    }

    val showCreateStory by viewModel.showCreateStoryDialog.collectAsState()
    if (showCreateStory) {
        CreateStoryDialog(viewModel = viewModel, onDismiss = { viewModel.showCreateStoryDialog.value = false })
    }

    val showSecurityInfo by viewModel.showSecurityInfoDialog.collectAsState()
    if (showSecurityInfo) {
        SecurityFingerprintDialog(onDismiss = { viewModel.showSecurityInfoDialog.value = false })
    }

    val showProfileEdit by viewModel.showProfileEditDialog.collectAsState()
    if (showProfileEdit) {
        ProfileEditDialog(viewModel = viewModel, onDismiss = { viewModel.showProfileEditDialog.value = false })
    }
}
