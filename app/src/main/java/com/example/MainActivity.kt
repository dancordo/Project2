package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.MainTerminalScreen
import com.example.ui.theme.FuturesEdgeTheme
import com.example.ui.theme.TerminalBg
import com.example.viewmodel.TerminalViewModel

class MainActivity : ComponentActivity() {

    private val terminalViewModel: TerminalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuturesEdgeTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TerminalBg
                ) {
                    MainTerminalScreen(viewModel = terminalViewModel)
                }
            }
        }
    }
}
