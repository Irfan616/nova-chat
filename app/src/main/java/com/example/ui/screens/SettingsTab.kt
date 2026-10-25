package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import com.example.ui.components.AvatarView
import com.example.ui.components.WallpaperGradients
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun SettingsTab(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val wallpaperIndex by viewModel.wallpaperIndex.collectAsState()
    val accentIndex by viewModel.accentColorIndex.collectAsState()
    val isUrdu by viewModel.isUrduRtl.collectAsState()
    val readReceipts by viewModel.readReceipts.collectAsState()
    val cacheNotice by viewModel.cacheClearedNotice.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarView(
                        name = currentUser?.name ?: "Alex",
                        avatarUrl = currentUser?.avatarUrl ?: "",
                        size = 64.dp,
                        isOnline = true
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser?.name ?: "Alex Vance",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Text(
                            text = currentUser?.handle ?: "@alex_vance",
                            fontSize = 13.sp,
                            color = NovaCyan
                        )
                        Text(
                            text = currentUser?.phone ?: "+1 (555) 019-2834",
                            fontSize = 12.sp,
                            color = TextMutedDark
                        )
                    }

                    IconButton(
                        onClick = { viewModel.showProfileEditDialog.value = true },
                        modifier = Modifier.testTag("edit_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = NovaCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = currentUser?.bio ?: "Building the future with NovaChat ✨",
                    fontSize = 13.sp,
                    color = TextSecondaryDark,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, null, tint = NovaMint, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Post-Quantum Key ID", fontSize = 11.sp, color = TextMutedDark)
                    }
                    Text("7A3F-98DE-2B01", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NovaMint)
                }
            }
        }

        // Account Switcher Section (Demo / Multi-User testing)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwitchAccount, null, tint = NovaViolet)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Account Switcher",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Text(
                        text = "Instant Peer Demo",
                        fontSize = 11.sp,
                        color = NovaViolet
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Switch active identity to test real-time chat from other personas:",
                    fontSize = 12.sp,
                    color = TextSecondaryDark
                )
                Spacer(modifier = Modifier.height(10.dp))

                allUsers.forEach { user ->
                    val isCurrent = user.id == currentUser?.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrent) NovaViolet.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.switchUserAccount(user.id) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AvatarView(name = user.name, avatarUrl = user.avatarUrl, size = 36.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(user.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = TextPrimaryDark)
                            Text(user.handle, fontSize = 11.sp, color = TextMutedDark)
                        }
                        if (isCurrent) {
                            Icon(Icons.Default.CheckCircle, null, tint = NovaViolet, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        // Appearance & Customization Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Palette, null, tint = NovaCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Appearance & Theme",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                // Dark Theme Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DarkMode, null, tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Obsidian Dark Mode", fontSize = 14.sp, color = TextPrimaryDark)
                    }
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { viewModel.toggleDarkTheme() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NovaCyan, checkedTrackColor = NovaCyan.copy(alpha = 0.4f))
                    )
                }

                // Chat Wallpaper Picker
                Text("Chat Wallpaper", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextSecondaryDark)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(WallpaperGradients) { idx, gradient ->
                        val isSelected = wallpaperIndex == idx
                        Box(
                            modifier = Modifier
                                .size(56.dp, 80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Brush.verticalGradient(gradient))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) NovaCyan else ObsidianBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { viewModel.setWallpaperIndex(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = NovaCyan, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Privacy & Security Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, null, tint = NovaMint)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy & Encryption",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }

                // Verify Encryption fingerprint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.showSecurityInfoDialog.value = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode, null, tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Verify Encryption Fingerprint", fontSize = 14.sp, color = TextPrimaryDark)
                            Text("Compare 60-digit safety numbers", fontSize = 11.sp, color = TextMutedDark)
                        }
                    }
                    Icon(Icons.Default.Lock, null, tint = NovaMint, modifier = Modifier.size(16.dp))
                }

                // Read Receipts Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DoneAll, null, tint = TextSecondaryDark, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Read Receipts (Blue Ticks)", fontSize = 14.sp, color = TextPrimaryDark)
                            Text("Show when messages are read", fontSize = 11.sp, color = TextMutedDark)
                        }
                    }
                    Switch(
                        checked = readReceipts,
                        onCheckedChange = { viewModel.toggleReadReceipts() },
                        colors = SwitchDefaults.colors(checkedThumbColor = NovaMint, checkedTrackColor = NovaMint.copy(alpha = 0.4f))
                    )
                }
            }
        }

        // Storage & Cache Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CleaningServices, null, tint = NovaCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Storage & Cache",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Text("38.4 MB Cached", fontSize = 12.sp, color = TextMutedDark)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.clearCache() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2638)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("clear_cache_btn")
                ) {
                    Text("Clear Temporary Cache", color = NovaCyan, fontSize = 13.sp)
                }

                AnimatedVisibility(visible = cacheNotice) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = NovaMint, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cache cleaned successfully! 0 B temporary files.", color = NovaMint, fontSize = 12.sp)
                    }
                }
            }
        }

        // Language & Localization (English & Urdu RTL)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianSurfaceElevated)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, null, tint = NovaViolet)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Language / زبان",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }
                    Text(
                        text = if (isUrdu) "اردو (Urdu RTL)" else "English",
                        fontSize = 13.sp,
                        color = NovaViolet
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.toggleUrduLanguage() },
                    colors = ButtonDefaults.buttonColors(containerColor = NovaViolet.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isUrdu) "Switch to English" else "اردو زبان فعال کریں (Toggle Urdu)",
                        color = NovaViolet,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
