package app.pinbuddy.lvl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import app.pinbuddy.lvl.ui.navigation.PinLevelApp
import app.pinbuddy.lvl.ui.theme.PinLevelTheme
import app.pinbuddy.lvl.viewmodel.LevelViewModel

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
