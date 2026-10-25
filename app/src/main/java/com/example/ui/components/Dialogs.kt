package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ChatViewModel
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

@Composable
fun NewChatDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    var customName by remember { mutableStateOf("") }
    var initialMessage by remember { mutableStateOf("Hi! Glad to connect on NovaChat 🚀") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurfaceElevated,
        title = {
            Text(
                text = "New Encrypted Chat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Select an existing contact or enter a new name:",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )

                // Quick contacts row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allUsers.filter { !it.isCurrentUser }.take(3).forEach { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .clickable {
                                    viewModel.createNewChat(user.name, false, initialMessage)
                                    onDismiss()
                                }
                                .padding(8.dp)
                        ) {
                            AvatarView(name = user.name, avatarUrl = user.avatarUrl, size = 38.dp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.name.split(" ").firstOrNull() ?: user.name,
                                fontSize = 11.sp,
                                color = TextPrimaryDark,
                                maxLines = 1
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Contact or Topic Name", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_name_input")
                )

                OutlinedTextField(
                    value = initialMessage,
                    onValueChange = { initialMessage = it },
                    label = { Text("First Message", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customName.isNotBlank()) {
                        viewModel.createNewChat(customName.trim(), false, initialMessage)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                enabled = customName.isNotBlank(),
                modifier = Modifier.testTag("confirm_create_chat_btn")
            ) {
                Text("Start Chat", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
fun NewGroupDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    var groupTopic by remember { mutableStateOf("Android & Quantum Architecture") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurfaceElevated,
        title = {
            Text(
                text = "Create Encrypted Group",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "End-to-end encrypted group with post-quantum security tokens.",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )

                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Subject", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaViolet,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = groupTopic,
                    onValueChange = { groupTopic = it },
                    label = { Text("Description / Topic", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaViolet,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (groupName.isNotBlank()) {
                        viewModel.createNewChat(
                            title = groupName.trim(),
                            isGroup = true,
                            initialMessage = "Group created: $groupTopic. Welcome members! 🚀"
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaViolet),
                enabled = groupName.isNotBlank()
            ) {
                Text("Create Group", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
fun CreateStoryDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    var captionText by remember { mutableStateOf("") }
    var selectedGradientIndex by remember { mutableIntStateOf(0) }

    val gradientOptions = listOf(
        listOf(Color(0xFF0F2027), Color(0xFF203A43), Color(0xFF2C5364)),
        listOf(Color(0xFF2E0854), Color(0xFF5B146F), Color(0xFF861657)),
        listOf(Color(0xFF0B3024), Color(0xFF135A3D), Color(0xFF1B8A5A)),
        listOf(Color(0xFF1F1C2C), Color(0xFF928DAB))
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurfaceElevated,
        title = {
            Text(
                text = "New Status Update",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Live preview canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Brush.verticalGradient(gradientOptions[selectedGradientIndex]))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = captionText.ifBlank { "Type what's on your mind..." },
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                // Color palette row
                Text("Choose Canvas Background", fontSize = 12.sp, color = TextSecondaryDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    gradientOptions.forEachIndexed { idx, gradient ->
                        val isSelected = selectedGradientIndex == idx
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Brush.verticalGradient(gradient))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) NovaCyan else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedGradientIndex = idx },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, null, tint = NovaCyan, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    placeholder = { Text("Type your status update...", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (captionText.isNotBlank()) {
                        viewModel.postStatus(captionText.trim(), selectedGradientIndex)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                enabled = captionText.isNotBlank()
            ) {
                Text("Post Status", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
fun SecurityFingerprintDialog(
    onDismiss: () -> Unit
) {
    var isVerified by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurfaceElevated,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Security, null, tint = NovaMint, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Verify Safety Numbers",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "To verify that your communication is end-to-end encrypted with Post-Quantum Double Ratchet, compare these numbers with your contact's device:",
                    fontSize = 12.sp,
                    color = TextSecondaryDark,
                    textAlign = TextAlign.Center
                )

                // Simulated QR Graphic
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "QR Code",
                        tint = Color.Black,
                        modifier = Modifier.size(100.dp)
                    )
                }

                // 60-digit fingerprint
                Text(
                    text = "48102  93120  44801  88301\n29401  77102  33491  80211\n49102  39102  44910  29381",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = NovaCyan,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                if (isVerified) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = NovaMint, modifier = Modifier.size(18.dp))
                        Text("Safety numbers verified!", color = NovaMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isVerified = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaMint)
            ) {
                Text(if (isVerified) "Done" else "Mark Verified", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close", color = TextSecondaryDark)
            }
        }
    )
}

@Composable
fun ProfileEditDialog(
    viewModel: ChatViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var statusPhrase by remember { mutableStateOf(currentUser?.statusPhrase ?: "Available") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurfaceElevated,
        title = {
            Text(
                text = "Edit Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("About / Bio", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = statusPhrase,
                    onValueChange = { statusPhrase = it },
                    label = { Text("Status Phrase", color = TextMutedDark) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NovaCyan,
                        unfocusedBorderColor = ObsidianBorder,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        viewModel.updateProfile(name.trim(), bio.trim(), statusPhrase.trim())
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NovaCyan)
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}
