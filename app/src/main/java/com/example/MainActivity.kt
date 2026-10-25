package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.AppScreen
import com.example.ui.ChatViewModel
import com.example.ui.ChatViewModelFactory
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.StoryViewerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NovaChatApplication
        val viewModelFactory = ChatViewModelFactory(app.repository, app.authService)
        val viewModel: ChatViewModel by viewModels { viewModelFactory }

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val currentScreen by viewModel.screen.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Back handler for navigation
                    BackHandler(enabled = currentScreen !is AppScreen.Main) {
                        viewModel.navigateBack()
                    }

                    when (val screen = currentScreen) {
                        is AppScreen.Main -> {
                            MainScreen(viewModel = viewModel)
                        }
                        is AppScreen.Conversation -> {
                            ConversationScreen(
                                conversationId = screen.conversationId,
                                viewModel = viewModel
                            )
                        }
                        is AppScreen.ActiveCall -> {
                            ActiveCallScreen(viewModel = viewModel)
                        }
                        is AppScreen.StoryViewer -> {
                            StoryViewerScreen(
                                statusId = screen.statusId,
                                viewModel = viewModel
                            )
                        }
                        is AppScreen.Auth -> {
                            AuthScreen(
                                viewModel = viewModel,
                                onComplete = { viewModel.navigateBack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
