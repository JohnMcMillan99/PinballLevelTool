package app.pinbuddy.lvl.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pinbuddy.lvl.ui.theme.DarkSurfaceVariant
import app.pinbuddy.lvl.ui.theme.PinBlue
import app.pinbuddy.lvl.ui.theme.PinGreen
import app.pinbuddy.lvl.viewmodel.LevelViewModel

@Composable
fun CalibrationScreen(viewModel: LevelViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Calibration",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(Modifier.height(16.dp))

        GlassOffsetCard(
            enabled = state.glassOffsetEnabled,
            degrees = state.glassOffsetDegrees,
            onEnabledChange = { viewModel.setGlassOffsetEnabled(it) },
            onDegreesChange = { viewModel.setGlassOffsetDegrees(it) }
        )

        Spacer(Modifier.height(16.dp))

        CalibrationIdle(
            pitchOffset = state.pitchOffset,
            rollOffset = state.rollOffset,
            pitch = state.pitch,
            roll = state.roll,
            onCalibrate = { viewModel.calibrateFlat() }
        )
    }
}

@Composable
private fun GlassOffsetCard(
    enabled: Boolean,
    degrees: Double,
    onEnabledChange: (Boolean) -> Unit,
    onDegreesChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Measuring on glass",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "The glass sits ~8.5° steeper than the playfield. Enable to show playfield pitch when your phone rests on the glass.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PinGreen,
                        checkedTrackColor = PinGreen.copy(alpha = 0.5f)
                    )
                )
            }
            if (enabled) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Offset: ${String.format("%.1f", degrees)}°",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.width(80.dp)
                    )
                    Slider(
                        value = degrees.toFloat(),
                        onValueChange = { onDegreesChange(it.toDouble()) },
                        valueRange = 5f..12f,
                        steps = 13,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = PinGreen,
                            activeTrackColor = PinGreen
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CalibrationIdle(
    pitchOffset: Double,
    rollOffset: Double,
    pitch: Double,
    roll: Double,
    onCalibrate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Current Offset",
                style = MaterialTheme.typography.titleLarge,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Pitch: ${String.format("%+.2f", pitchOffset)}°",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Text(
                text = "Roll: ${String.format("%+.2f", rollOffset)}°",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Lay your phone on a flat surface with the screen facing up (phone on its back). Keep it still, then press Calibrate. This sets the current orientation as zero.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Current reading: P ${String.format("%+.1f", pitch)}°  R ${String.format("%+.1f", roll)}°",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }

    Spacer(Modifier.height(32.dp))

    Button(
        onClick = onCalibrate,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinBlue),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Calibrate", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
