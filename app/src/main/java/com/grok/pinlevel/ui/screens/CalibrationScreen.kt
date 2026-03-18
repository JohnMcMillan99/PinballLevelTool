package com.grok.pinlevel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.grok.pinlevel.ui.theme.DarkSurfaceVariant
import com.grok.pinlevel.ui.theme.PinBlue
import com.grok.pinlevel.ui.theme.PinGreen
import com.grok.pinlevel.viewmodel.LevelViewModel

@Composable
fun CalibrationScreen(viewModel: LevelViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Calibration",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White
        )

        Spacer(Modifier.height(24.dp))

        when (state.calibrationStep) {
            0 -> CalibrationIdle(
                pitchOffset = state.pitchOffset,
                rollOffset = state.rollOffset,
                onStart = { viewModel.startCalibration() }
            )
            1 -> CalibrationStepOne(
                pitch = state.pitch,
                roll = state.roll,
                onCapture = { viewModel.captureFlat() },
                onCancel = { viewModel.cancelCalibration() }
            )
            2 -> CalibrationStepTwo(
                pitch = state.pitch,
                roll = state.roll,
                onCapture = { viewModel.captureFlipped() },
                onCancel = { viewModel.cancelCalibration() }
            )
        }
    }
}

@Composable
private fun CalibrationIdle(
    pitchOffset: Double,
    rollOffset: Double,
    onStart: () -> Unit
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
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Calibration removes sensor bias using a flat-flip technique.\n\n" +
                        "You will need a known flat surface.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(Modifier.height(32.dp))

    Button(
        onClick = onStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinBlue),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Start Calibration", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CalibrationStepOne(
    pitch: Double,
    roll: Double,
    onCapture: () -> Unit,
    onCancel: () -> Unit
) {
    StepCard(
        step = "Step 1 of 2",
        instruction = "Place your phone face-up on a known flat surface.\n\nKeep it still and tap Capture.",
        pitch = pitch,
        roll = roll
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onCapture,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Capture Flat Reading", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(Modifier.height(12.dp))

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Cancel", color = Color.Gray)
    }
}

@Composable
private fun CalibrationStepTwo(
    pitch: Double,
    roll: Double,
    onCapture: () -> Unit,
    onCancel: () -> Unit
) {
    StepCard(
        step = "Step 2 of 2",
        instruction = "Now flip the phone 180\u00B0 (upside down) on the same surface.\n\nKeep it still and tap Capture.",
        pitch = pitch,
        roll = roll
    )

    Spacer(Modifier.height(24.dp))

    Button(
        onClick = onCapture,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PinGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text("Capture Flipped Reading", fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }

    Spacer(Modifier.height(12.dp))

    OutlinedButton(
        onClick = onCancel,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Cancel", color = Color.Gray)
    }
}

@Composable
private fun StepCard(
    step: String,
    instruction: String,
    pitch: Double,
    roll: Double
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
                text = step,
                style = MaterialTheme.typography.titleLarge,
                color = PinBlue
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = instruction,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Current: P ${String.format("%+.1f", pitch)}°  R ${String.format("%+.1f", roll)}°",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
