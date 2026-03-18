package com.grok.pinlevel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.grok.pinlevel.ui.navigation.PinLevelApp
import com.grok.pinlevel.ui.theme.PinLevelTheme
import com.grok.pinlevel.viewmodel.LevelViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: LevelViewModel = viewModel()
            PinLevelTheme {
                PinLevelApp(viewModel)
            }
        }
    }
}
