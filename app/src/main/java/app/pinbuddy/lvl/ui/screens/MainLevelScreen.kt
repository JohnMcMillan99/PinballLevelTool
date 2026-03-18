package app.pinbuddy.lvl.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.pinbuddy.lvl.model.MachineLevelConfig
import app.pinbuddy.lvl.ui.theme.DarkSurface
import app.pinbuddy.lvl.ui.theme.DarkSurfaceVariant
import app.pinbuddy.lvl.ui.theme.PinBlue
import app.pinbuddy.lvl.ui.theme.PinGreen
import app.pinbuddy.lvl.ui.theme.PinRed
import app.pinbuddy.lvl.ui.theme.PinYellow
import app.pinbuddy.lvl.data.LegHint
import app.pinbuddy.lvl.viewmodel.LevelState
import app.pinbuddy.lvl.viewmodel.LevelViewModel
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun MainLevelScreen(viewModel: LevelViewModel) {
    val state by viewModel.state.collectAsState()
    var showPitchConfig by remember { mutableStateOf(false) }
    var showRollConfig by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MachineHeader(state)
        Spacer(Modifier.height(8.dp))

        VoiceGuideToggle(
            isEnabled = state.isVoiceGuideEnabled,
            intervalSeconds = state.voiceGuideIntervalSeconds,
            onIntervalChange = { viewModel.setVoiceGuideIntervalSeconds(it) },
            currentHint = viewModel.currentVoiceHint,
            onToggle = { viewModel.toggleVoiceGuide() }
        )
        Spacer(Modifier.height(8.dp))

        DigitalReadout(
            state = state,
            onPitchTap = { showPitchConfig = true },
            onRollTap = { showRollConfig = true }
        )
        Spacer(Modifier.height(8.dp))

        LevelCanvas(
            pitch = state.pitch,
            roll = state.roll,
            targetAngle = state.targetAngle,
            targetRoll = state.targetRoll,
            pitchTolerance = state.pitchTolerance,
            rollTolerance = state.rollTolerance,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(Modifier.height(8.dp))

        LegAdjustmentHints(state)
        Spacer(Modifier.height(4.dp))
    }

    if (showPitchConfig) {
        ConfigDialog(
            title = "Configure Pitch",
            targetValue = state.targetAngle,
            targetRange = 3f..10f,
            targetLabel = "Target pitch",
            tolerance = state.pitchTolerance,
            onSave = { target, tol ->
                val machineId = state.currentMachine?.id
                if (machineId != null) {
                    val existing = state.machineLevelConfigs[machineId]
                        ?: MachineLevelConfig(targetPitch = state.targetAngle)
                    viewModel.updateMachineLevelConfig(
                        machineId,
                        existing.copy(targetPitch = target, pitchTolerance = tol)
                    )
                } else {
                    viewModel.setTargetAngle(target)
                }
                showPitchConfig = false
            },
            onDismiss = { showPitchConfig = false }
        )
    }

    if (showRollConfig) {
        ConfigDialog(
            title = "Configure Roll",
            targetValue = state.targetRoll,
            targetRange = -3f..3f,
            targetLabel = "Target roll",
            tolerance = state.rollTolerance,
            onSave = { target, tol ->
                val machineId = state.currentMachine?.id
                if (machineId != null) {
                    val existing = state.machineLevelConfigs[machineId]
                        ?: MachineLevelConfig(targetPitch = state.targetAngle)
                    viewModel.updateMachineLevelConfig(
                        machineId,
                        existing.copy(targetRoll = target, rollTolerance = tol)
                    )
                }
                showRollConfig = false
            },
            onDismiss = { showRollConfig = false }
        )
    }
}

@Composable
private fun ConfigDialog(
    title: String,
    targetValue: Double,
    targetRange: ClosedFloatingPointRange<Float>,
    targetLabel: String,
    tolerance: Double,
    onSave: (target: Double, tolerance: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var editTarget by remember { mutableFloatStateOf(targetValue.toFloat()) }
    var editTolerance by remember { mutableFloatStateOf(tolerance.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurfaceVariant,
        title = { Text(title, color = Color.White) },
        text = {
            Column {
                Text(
                    "$targetLabel: ${String.format("%.1f", editTarget)}°",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = editTarget,
                    onValueChange = { editTarget = it },
                    valueRange = targetRange,
                    colors = SliderDefaults.colors(
                        thumbColor = PinBlue,
                        activeTrackColor = PinBlue
                    )
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Tolerance: \u00b1${String.format("%.1f", editTolerance)}°",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge
                )
                Slider(
                    value = editTolerance,
                    onValueChange = { editTolerance = it },
                    valueRange = 0.2f..2.0f,
                    colors = SliderDefaults.colors(
                        thumbColor = PinGreen,
                        activeTrackColor = PinGreen
                    )
                )
                Text(
                    "Green when within \u00b1${String.format("%.1f", editTolerance)}°",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(editTarget.toDouble(), editTolerance.toDouble()) }) {
                Text("Save", color = PinGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

@Composable
private fun VoiceGuideToggle(
    isEnabled: Boolean,
    intervalSeconds: Int,
    onIntervalChange: (Int) -> Unit,
    currentHint: LegHint?,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Voice Guide",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    if (isEnabled && currentHint != null) {
                        Text(
                            text = currentHint.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = PinGreen
                        )
                    } else if (isEnabled) {
                        Text(
                            text = "Level achieved!",
                            style = MaterialTheme.typography.bodySmall,
                            color = PinGreen
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Every ${intervalSeconds}s",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Slider(
                        value = intervalSeconds.toFloat(),
                        onValueChange = { onIntervalChange(it.toInt().coerceIn(5, 60)) },
                        valueRange = 5f..60f,
                        steps = 10,
                        modifier = Modifier.width(100.dp),
                        colors = SliderDefaults.colors(
                            thumbColor = PinGreen,
                            activeTrackColor = PinGreen
                        )
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PinGreen,
                            checkedTrackColor = PinGreen.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun MachineHeader(state: LevelState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.currentMachine?.name ?: "No Machine Selected",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Pitch: ${String.format("%.1f", state.targetAngle)}° \u00b1${String.format("%.1f", state.pitchTolerance)}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = PinBlue
                )
                Text(
                    text = "Roll: ${String.format("%.1f", state.targetRoll)}° \u00b1${String.format("%.1f", state.rollTolerance)}°",
                    style = MaterialTheme.typography.bodySmall,
                    color = PinBlue
                )
            }
        }
    }
}

@Composable
private fun DigitalReadout(
    state: LevelState,
    onPitchTap: () -> Unit,
    onRollTap: () -> Unit
) {
    val pitchColor = angleColor(state.pitch, state.targetAngle, state.pitchTolerance)
    val rollColor = angleColor(state.roll, state.targetRoll, state.rollTolerance)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ReadoutCard(
            label = "PITCH",
            value = state.pitch,
            color = pitchColor,
            onClick = onPitchTap
        )
        ReadoutCard(
            label = "ROLL",
            value = state.roll,
            color = rollColor,
            onClick = onRollTap
        )
    }
}

@Composable
private fun ReadoutCard(label: String, value: Double, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
                Text(
                    text = " \u270E",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray.copy(alpha = 0.5f)
                )
            }
            Text(
                text = "${String.format("%+.1f", value)}°",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun LevelCanvas(
    pitch: Double,
    roll: Double,
    targetAngle: Double,
    targetRoll: Double,
    pitchTolerance: Double,
    rollTolerance: Double,
    modifier: Modifier = Modifier
) {
    val pitchError = pitch - targetAngle
    val rollError = roll - targetRoll
    val avgTolerance = (pitchTolerance + rollTolerance) / 2.0

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val rectAspect = 0.58f
        val maxH = h * 0.94f
        val maxW = w * 0.94f
        val rectH = min(maxH, maxW / rectAspect)
        val rectW = rectH * rectAspect
        val rectLeft = (w - rectW) / 2f
        val rectTop = (h - rectH) / 2f
        val corner = CornerRadius(16f, 16f)

        drawRoundRect(
            color = DarkSurfaceVariant,
            topLeft = Offset(rectLeft, rectTop),
            size = Size(rectW, rectH),
            cornerRadius = corner
        )
        drawRoundRect(
            color = Color(0xFF3A3A3A),
            topLeft = Offset(rectLeft, rectTop),
            size = Size(rectW, rectH),
            cornerRadius = corner,
            style = Stroke(width = 2f)
        )

        drawLine(Color(0xFF333333), Offset(rectLeft, cy), Offset(rectLeft + rectW, cy), 1f)
        drawLine(Color(0xFF333333), Offset(cx, rectTop), Offset(cx, rectTop + rectH), 1f)

        val flErr = (-pitchError + rollError) / 2.0
        val frErr = (-pitchError - rollError) / 2.0
        val rlErr = (pitchError + rollError) / 2.0
        val rrErr = (pitchError - rollError) / 2.0

        val legInset = min(rectW, rectH) * 0.14f
        val legRadius = min(rectW, rectH) * 0.09f

        val rlPos = Offset(rectLeft + legInset, rectTop + legInset)
        val rrPos = Offset(rectLeft + rectW - legInset, rectTop + legInset)
        val flPos = Offset(rectLeft + legInset, rectTop + rectH - legInset)
        val frPos = Offset(rectLeft + rectW - legInset, rectTop + rectH - legInset)

        val maxAngle = 5.0
        val bubbleAreaW = rectW * 0.45f
        val bubbleAreaH = rectH * 0.35f
        val bubbleX = cx - (rollError / maxAngle * bubbleAreaW).toFloat()
            .coerceIn(-bubbleAreaW, bubbleAreaW)
        val bubbleY = cy - (pitchError / maxAngle * bubbleAreaH).toFloat()
            .coerceIn(-bubbleAreaH, bubbleAreaH)
        val totalError = sqrt(pitchError * pitchError + rollError * rollError)
        val bubbleColor = when {
            totalError <= avgTolerance -> PinGreen
            totalError <= avgTolerance * 2 -> PinYellow
            else -> PinRed
        }

        drawCircle(
            color = Color(0xFF444444),
            radius = min(bubbleAreaW, bubbleAreaH) * 0.8f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )
        drawCircle(
            color = bubbleColor.copy(alpha = 0.25f),
            radius = legRadius * 1.3f,
            center = Offset(bubbleX, bubbleY)
        )
        drawCircle(
            color = bubbleColor,
            radius = legRadius * 0.7f,
            center = Offset(bubbleX, bubbleY)
        )

        drawLeg(rlPos, legRadius, rlErr, avgTolerance, "RL")
        drawLeg(rrPos, legRadius, rrErr, avgTolerance, "RR")
        drawLeg(flPos, legRadius, flErr, avgTolerance, "FL")
        drawLeg(frPos, legRadius, frErr, avgTolerance, "FR")

        if (abs(pitchError) > pitchTolerance || abs(rollError) > rollTolerance) {
            drawAdjustmentArrow(cx, cy, pitchError, rollError, min(rectW, rectH) * 0.4f)
        }

        drawContext.canvas.nativeCanvas.drawText(
            "REAR",
            cx,
            rectTop + legInset * 0.3f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.argb(80, 255, 255, 255)
                textSize = legRadius * 0.6f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
        )
        drawContext.canvas.nativeCanvas.drawText(
            "FRONT",
            cx,
            rectTop + rectH - legInset * 0.1f,
            android.graphics.Paint().apply {
                color = android.graphics.Color.argb(80, 255, 255, 255)
                textSize = legRadius * 0.6f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
        )
    }
}

private fun DrawScope.drawLeg(
    center: Offset,
    radius: Float,
    heightError: Double,
    tolerance: Double,
    label: String
) {
    val absErr = abs(heightError)
    val color = when {
        absErr <= tolerance -> PinGreen
        absErr <= tolerance * 2 -> PinYellow
        else -> PinRed
    }

    drawCircle(color = color.copy(alpha = 0.35f), radius = radius, center = center)
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 3f))

    val labelText = label
    val dirChar = when {
        absErr <= tolerance -> "\u2713"
        heightError > 0 -> "\u2193"
        else -> "\u2191"
    }

    drawContext.canvas.nativeCanvas.drawText(
        labelText,
        center.x,
        center.y - radius * 0.1f,
        android.graphics.Paint().apply {
            this.color = android.graphics.Color.WHITE
            textSize = radius * 0.7f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
    )
    drawContext.canvas.nativeCanvas.drawText(
        dirChar,
        center.x,
        center.y + radius * 0.6f,
        android.graphics.Paint().apply {
            this.color = when {
                absErr <= tolerance -> android.graphics.Color.argb(255, 76, 175, 80)
                absErr <= tolerance * 2 -> android.graphics.Color.argb(255, 255, 193, 7)
                else -> android.graphics.Color.argb(255, 244, 67, 54)
            }
            textSize = radius * 0.7f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    )

    if (absErr > tolerance) {
        val arrowDir = if (heightError > 0) 1f else -1f
        val arrowStart = Offset(center.x, center.y - radius * arrowDir * 1.4f)
        val arrowEnd = Offset(center.x, center.y - radius * arrowDir * 2.0f)
        drawLine(color, arrowStart, arrowEnd, strokeWidth = 2.5f)
        val headSize = radius * 0.35f
        val headPath = Path().apply {
            moveTo(arrowEnd.x, arrowEnd.y)
            lineTo(arrowEnd.x - headSize, arrowEnd.y + headSize * arrowDir)
            lineTo(arrowEnd.x + headSize, arrowEnd.y + headSize * arrowDir)
            close()
        }
        drawPath(headPath, color)
    }
}

private fun DrawScope.drawAdjustmentArrow(
    cx: Float,
    cy: Float,
    pitchError: Double,
    rollError: Double,
    scale: Float
) {
    val arrowLen = scale * 0.35f
    val dx = -(rollError / 5.0).toFloat().coerceIn(-1f, 1f) * arrowLen
    val dy = -(pitchError / 5.0).toFloat().coerceIn(-1f, 1f) * arrowLen
    drawLine(
        Color.White.copy(alpha = 0.4f),
        Offset(cx, cy),
        Offset(cx + dx, cy + dy),
        strokeWidth = 2.5f
    )
}

@Composable
private fun LegAdjustmentHints(state: LevelState) {
    val pitchError = state.pitch - state.targetAngle
    val rollError = state.roll - state.targetRoll
    val avgTol = (state.pitchTolerance + state.rollTolerance) / 2.0

    val flErr = (-pitchError + rollError) / 2.0
    val frErr = (-pitchError - rollError) / 2.0
    val rlErr = (pitchError + rollError) / 2.0
    val rrErr = (pitchError - rollError) / 2.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegHintCell("RL", rlErr, avgTol)
                LegHintCell("RR", rrErr, avgTol)
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegHintCell("FL", flErr, avgTol)
                LegHintCell("FR", frErr, avgTol)
            }
        }
    }
}

@Composable
private fun LegHintCell(label: String, heightError: Double, tolerance: Double) {
    val absErr = abs(heightError)
    val color = when {
        absErr <= tolerance -> PinGreen
        absErr <= tolerance * 2 -> PinYellow
        else -> PinRed
    }
    val hint = when {
        absErr <= tolerance -> "OK"
        heightError > 0 -> "Lower"
        else -> "Raise"
    }
    val arrow = when {
        absErr <= tolerance -> "\u2713"
        heightError > 0 -> "\u2193"
        else -> "\u2191"
    }

    Box(modifier = Modifier.width(90.dp).height(36.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = "$arrow $hint",
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun angleColor(actual: Double, target: Double, tolerance: Double): Color {
    val diff = abs(actual - target)
    return when {
        diff <= tolerance -> PinGreen
        diff <= tolerance * 2 -> PinYellow
        else -> PinRed
    }
}
