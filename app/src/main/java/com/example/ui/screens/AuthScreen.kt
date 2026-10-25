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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.ChatViewModel
import com.example.ui.components.AvatarView
import com.example.ui.theme.NovaCyan
import com.example.ui.theme.NovaMint
import com.example.ui.theme.NovaRose
import com.example.ui.theme.NovaViolet
import com.example.ui.theme.ObsidianBackground
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianSurfaceElevated
import com.example.ui.theme.TextMutedDark
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

enum class AuthMethod {
    OPTIONS_MENU,
    PHONE_NUMBER,
    EMAIL_PASSWORD
}

@Composable
fun AuthScreen(
    viewModel: ChatViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Tab: 0 = Sign Up, 1 = Log In
    var selectedAuthTab by remember { mutableIntStateOf(0) }
    var currentMethod by remember { mutableStateOf(AuthMethod.OPTIONS_MENU) }

    // Phone state
    var phoneStage by remember { mutableIntStateOf(0) } // 0 = Enter phone, 1 = OTP
    var phoneNumber by remember { mutableStateOf("+1 (555) 019-2834") }
    var countryCode by remember { mutableStateOf("+1") }
    var otpCode by remember { mutableStateOf("") }

    // Email/Pass state
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Profile details state (Stage 2)
    var isProfileSetupActive by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Alex Vance") }
    var userHandle by remember { mutableStateOf("@alex_vance") }
    var userBio by remember { mutableStateOf("Building the future with NovaChat ✨") }

    // Modals
    var showGoogleAccountDialog by remember { mutableStateOf(false) }
    var showFacebookAuthDialog by remember { mutableStateOf(false) }
    var authErrorText by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Back button when inside sub-flow
                if (currentMethod != AuthMethod.OPTIONS_MENU && !isProfileSetupActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                currentMethod = AuthMethod.OPTIONS_MENU
                                phoneStage = 0
                            },
                            modifier = Modifier.testTag("auth_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimaryDark
                            )
                        }
                        Text(
                            text = "Back to sign up options",
                            color = TextSecondaryDark,
                            fontSize = 13.sp
                        )
                    }
                }

                // Hero Banner
                Image(
                    painter = painterResource(id = R.drawable.novachat_hero_banner),
                    contentDescription = "NovaChat Global Network",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, NovaCyan.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Branding Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.sweepGradient(listOf(NovaCyan, NovaViolet, NovaCyan))
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
                        text = "NovaChat",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimaryDark,
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "End-to-End Encrypted Private Communications",
                    fontSize = 12.sp,
                    color = NovaCyan,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Error Notice if any
                authErrorText?.let { error ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(NovaRose.copy(alpha = 0.15f))
                            .padding(12.dp)
                    ) {
                        Text(text = error, color = NovaRose, fontSize = 12.sp)
                    }
                }

                if (isProfileSetupActive) {
                    // Profile Setup Step
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Complete Your Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Set up your public persona and cryptographic identity",
                            fontSize = 12.sp,
                            color = TextSecondaryDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        AvatarView(name = userName, size = 68.dp)

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("Display Name", color = TextMutedDark) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NovaCyan,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("setup_name_input")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = userHandle,
                            onValueChange = { userHandle = it },
                            label = { Text("Unique Handle (@username)", color = TextMutedDark) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NovaCyan,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = userBio,
                            onValueChange = { userBio = it },
                            label = { Text("About / Status", color = TextMutedDark) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NovaCyan,
                                unfocusedBorderColor = ObsidianBorder,
                                focusedTextColor = TextPrimaryDark,
                                unfocusedTextColor = TextPrimaryDark
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                viewModel.updateProfile(userName, userBio, "Available")
                                onComplete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("finish_setup_btn")
                        ) {
                            Text(
                                text = "Enter NovaChat",
                                color = Color.Black,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    // Sign Up vs Log In Tabs
                    TabRow(
                        selectedTabIndex = selectedAuthTab,
                        containerColor = ObsidianSurfaceElevated,
                        contentColor = NovaCyan,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedAuthTab]),
                                color = NovaCyan
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedAuthTab == 0,
                            onClick = { selectedAuthTab = 0 },
                            text = {
                                Text(
                                    text = "Sign Up",
                                    fontWeight = if (selectedAuthTab == 0) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = if (selectedAuthTab == 0) NovaCyan else TextSecondaryDark
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_signup")
                        )
                        Tab(
                            selected = selectedAuthTab == 1,
                            onClick = { selectedAuthTab = 1 },
                            text = {
                                Text(
                                    text = "Log In",
                                    fontWeight = if (selectedAuthTab == 1) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = if (selectedAuthTab == 1) NovaCyan else TextSecondaryDark
                                )
                            },
                            modifier = Modifier.testTag("auth_tab_login")
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    when (currentMethod) {
                        AuthMethod.OPTIONS_MENU -> {
                            // Primary Sign Up Options: Google (Gmail), Facebook, and Phone Number
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val actionPrefix = if (selectedAuthTab == 0) "Sign up" else "Continue"

                                // 1. Continue with Gmail / Google
                                Button(
                                    onClick = {
                                        showGoogleAccountDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color(0xFF1F1F1F)
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("auth_google_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_google_logo),
                                            contentDescription = "Google Logo",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "$actionPrefix with Google (Gmail)",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1F1F1F)
                                        )
                                    }
                                }

                                // 2. Continue with Facebook
                                Button(
                                    onClick = {
                                        showFacebookAuthDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1877F2),
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("auth_facebook_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_facebook_logo),
                                            contentDescription = "Facebook Logo",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "$actionPrefix with Facebook",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }

                                // 3. Continue with Phone Number
                                OutlinedButton(
                                    onClick = {
                                        currentMethod = AuthMethod.PHONE_NUMBER
                                        phoneStage = 0
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = ObsidianSurfaceElevated,
                                        contentColor = TextPrimaryDark
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        brush = Brush.horizontalGradient(listOf(NovaCyan, NovaViolet))
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("auth_phone_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = "Phone",
                                            tint = NovaCyan,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "$actionPrefix with Phone Number",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimaryDark
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Divider "or with email"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = ObsidianBorder)
                                    Text(
                                        text = " or with email ",
                                        fontSize = 11.sp,
                                        color = TextMutedDark,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    HorizontalDivider(modifier = Modifier.weight(1f), color = ObsidianBorder)
                                }

                                // Email / Password Option button
                                Button(
                                    onClick = {
                                        currentMethod = AuthMethod.EMAIL_PASSWORD
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E2638),
                                        contentColor = TextSecondaryDark
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(46.dp)
                                        .testTag("auth_email_option_btn")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, null, tint = NovaViolet, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$actionPrefix with Email & Password",
                                            fontSize = 13.sp,
                                            color = TextPrimaryDark
                                        )
                                    }
                                }
                            }
                        }

                        AuthMethod.PHONE_NUMBER -> {
                            // Phone & OTP Flow
                            if (phoneStage == 0) {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        text = "Enter your phone number. We will send an SMS with a 6-digit verification code.",
                                        fontSize = 13.sp,
                                        color = TextSecondaryDark,
                                        lineHeight = 18.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Country code picker pill
                                        Box(
                                            modifier = Modifier
                                                .height(56.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(ObsidianSurfaceElevated)
                                                .border(1.dp, ObsidianBorder, RoundedCornerShape(14.dp))
                                                .padding(horizontal = 14.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = countryCode,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = NovaCyan
                                            )
                                        }

                                        OutlinedTextField(
                                            value = phoneNumber,
                                            onValueChange = { phoneNumber = it },
                                            label = { Text("Phone Number", color = TextMutedDark) },
                                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = NovaCyan) },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = NovaCyan,
                                                unfocusedBorderColor = ObsidianBorder,
                                                focusedTextColor = TextPrimaryDark,
                                                unfocusedTextColor = TextPrimaryDark
                                            ),
                                            shape = RoundedCornerShape(14.dp),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("phone_number_input")
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            phoneStage = 1
                                            otpCode = "774921" // Development auto-fill code
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("send_phone_code_btn")
                                    ) {
                                        Text(
                                            text = "Send 6-Digit Code",
                                            color = Color.Black,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else {
                                // OTP Code Screen
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Text(
                                        text = "Verification code sent to $phoneNumber",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimaryDark
                                    )

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(NovaMint.copy(alpha = 0.15f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Sandbox Mock OTP: 774921",
                                            fontSize = 12.sp,
                                            color = NovaMint,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = { otpCode = it },
                                        placeholder = { Text("6-digit code", color = TextMutedDark) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NovaMint,
                                            unfocusedBorderColor = ObsidianBorder,
                                            focusedTextColor = TextPrimaryDark,
                                            unfocusedTextColor = TextPrimaryDark
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            textAlign = TextAlign.Center,
                                            fontSize = 22.sp,
                                            letterSpacing = 8.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("phone_otp_input")
                                    )

                                    Button(
                                        onClick = {
                                            isProfileSetupActive = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = NovaMint),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .testTag("confirm_phone_otp_btn")
                                    ) {
                                        Text(
                                            text = "Verify Phone & Continue",
                                            color = Color.White,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { phoneStage = 0 },
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Change Number", color = TextSecondaryDark)
                                    }
                                }
                            }
                        }

                        AuthMethod.EMAIL_PASSWORD -> {
                            // Email & Password Form
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedTextField(
                                    value = emailInput,
                                    onValueChange = { emailInput = it },
                                    label = { Text("Email Address", color = TextMutedDark) },
                                    leadingIcon = { Icon(Icons.Default.Email, null, tint = NovaCyan) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NovaCyan,
                                        unfocusedBorderColor = ObsidianBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("email_auth_input")
                                )

                                OutlinedTextField(
                                    value = passwordInput,
                                    onValueChange = { passwordInput = it },
                                    label = { Text("Password", color = TextMutedDark) },
                                    leadingIcon = { Icon(Icons.Default.Lock, null, tint = NovaViolet) },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password visibility",
                                                tint = TextSecondaryDark
                                            )
                                        }
                                    },
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NovaViolet,
                                        unfocusedBorderColor = ObsidianBorder,
                                        focusedTextColor = TextPrimaryDark,
                                        unfocusedTextColor = TextPrimaryDark
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("password_auth_input")
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Button(
                                    onClick = {
                                        if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                            isProcessing = true
                                            if (selectedAuthTab == 0) {
                                                viewModel.signUpWithEmail(emailInput, passwordInput, emailInput.substringBefore("@")) { result ->
                                                    isProcessing = false
                                                    if (result.isSuccess) {
                                                        userName = emailInput.substringBefore("@")
                                                        isProfileSetupActive = true
                                                    } else {
                                                        authErrorText = result.exceptionOrNull()?.localizedMessage ?: "Sign up failed"
                                                    }
                                                }
                                            } else {
                                                viewModel.signInWithEmail(emailInput, passwordInput) { result ->
                                                    isProcessing = false
                                                    if (result.isSuccess) {
                                                        onComplete()
                                                    } else {
                                                        authErrorText = result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = NovaCyan),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("email_submit_btn")
                                ) {
                                    if (isProcessing) {
                                        CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                                    } else {
                                        Text(
                                            text = if (selectedAuthTab == 0) "Create Account" else "Log In",
                                            color = Color.Black,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom trust badge
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.VerifiedUser, null, tint = NovaMint, modifier = Modifier.size(15.dp))
                    Text(
                        text = "Encrypted Identity • Zero Metadata Logging",
                        fontSize = 11.sp,
                        color = TextMutedDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Google / Gmail Account Selector Dialog
    if (showGoogleAccountDialog) {
        val googleAccounts = listOf(
            Triple("Irfan Jameel", "irfanjameel373@gmail.com", "I"),
            Triple("Alex Vance", "alex.vance@gmail.com", "A"),
            Triple("Nova Developer", "dev@novachat.network", "N")
        )

        AlertDialog(
            onDismissRequest = { showGoogleAccountDialog = false },
            containerColor = ObsidianSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = "Google",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Choose a Google account to continue to NovaChat:",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    googleAccounts.forEach { (name, email, initial) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.25f))
                                .clickable {
                                    showGoogleAccountDialog = false
                                    userName = name
                                    userHandle = "@" + email.substringBefore("@")
                                    isProfileSetupActive = true
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(NovaCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initial,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimaryDark
                                )
                                Text(
                                    text = email,
                                    fontSize = 12.sp,
                                    color = TextMutedDark
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showGoogleAccountDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }

    // Facebook Auth Dialog
    if (showFacebookAuthDialog) {
        AlertDialog(
            onDismissRequest = { showFacebookAuthDialog = false },
            containerColor = ObsidianSurfaceElevated,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_facebook_logo),
                        contentDescription = "Facebook",
                        tint = Color(0xFF1877F2),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Connect with Facebook",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimaryDark
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "NovaChat will receive your name, profile picture, and email address from your Facebook account.",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1877F2).copy(alpha = 0.15f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF1877F2), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Encrypted token exchange", color = Color(0xFF1877F2), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFacebookAuthDialog = false
                        userName = "Facebook User"
                        userHandle = "@fb_user"
                        isProfileSetupActive = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Continue with Facebook", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showFacebookAuthDialog = false }) {
                    Text("Cancel", color = TextSecondaryDark)
                }
            }
        )
    }
}
