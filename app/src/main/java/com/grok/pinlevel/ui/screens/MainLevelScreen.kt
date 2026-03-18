package com.grok.pinlevel.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grok.pinlevel.ui.theme.DarkSurface
import com.grok.pinlevel.ui.theme.DarkSurfaceVariant
import com.grok.pinlevel.ui.theme.PinBlue
import com.grok.pinlevel.ui.theme.PinGreen
import com.grok.pinlevel.ui.theme.PinRed
import com.grok.pinlevel.ui.theme.PinYellow
import com.grok.pinlevel.viewmodel.LevelState
import com.grok.pinlevel.viewmodel.LevelViewModel
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

@Composable
fun MainLevelScreen(viewModel: LevelViewModel) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MachineHeader(state)

        Spacer(Modifier.height(8.dp))

        DigitalReadout(state)

        Spacer(Modifier.height(12.dp))

        LevelCanvas(
            pitch = state.pitch,
            roll = state.roll,
            targetAngle = state.targetAngle,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .aspectRatio(1f)
        )

        Spacer(Modifier.height(8.dp))

        LegAdjustmentHints(state)

        Spacer(Modifier.height(4.dp))
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
            Text(
                text = "Target: ${String.format("%.1f", state.targetAngle)}°",
                style = MaterialTheme.typography.bodyLarge,
                color = PinBlue
            )
        }
    }
}

@Composable
private fun DigitalReadout(state: LevelState) {
    val pitchColor = angleColor(state.pitch, state.targetAngle)
    val rollColor = angleColor(state.roll, 0.0)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        ReadoutCard(label = "PITCH", value = state.pitch, color = pitchColor)
        ReadoutCard(label = "ROLL", value = state.roll, color = rollColor)
    }
}

@Composable
private fun ReadoutCard(label: String, value: Double, color: Color) {
    Card(
        modifier = Modifier.width(160.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = Color.Gray
            )
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
    modifier: Modifier = Modifier
) {
    val pitchError = pitch - targetAngle
    val rollError = roll

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val canvasRadius = min(w, h) / 2f * 0.92f

        val legInset = canvasRadius * 0.22f
        val legRadius = canvasRadius * 0.13f
        val goodZoneRadius = legRadius * 1.5f

        val maxAngle = 5.0

        val flPos = Offset(cx - canvasRadius + legInset, cy + canvasRadius - legInset)
        val frPos = Offset(cx + canvasRadius - legInset, cy + canvasRadius - legInset)
        val rlPos = Offset(cx - canvasRadius + legInset, cy - canvasRadius + legInset)
        val rrPos = Offset(cx + canvasRadius - legInset, cy - canvasRadius + legInset)

        val flErr = (-pitchError + rollError) / 2.0
        val frErr = (-pitchError - rollError) / 2.0
        val rlErr = (pitchError + rollError) / 2.0
        val rrErr = (pitchError - rollError) / 2.0

        drawCircle(
            color = DarkSurfaceVariant,
            radius = canvasRadius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFF3A3A3A),
            radius = canvasRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )

        drawLine(Color(0xFF333333), Offset(cx - canvasRadius, cy), Offset(cx + canvasRadius, cy), 1f)
        drawLine(Color(0xFF333333), Offset(cx, cy - canvasRadius), Offset(cx, cy + canvasRadius), 1f)

        val bubbleRadius = canvasRadius * 0.55f
        drawCircle(
            color = Color(0xFF444444),
            radius = bubbleRadius,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = Color(0xFF333333),
            radius = bubbleRadius * 0.5f,
            center = Offset(cx, cy),
            style = Stroke(width = 1f)
        )

        val bubbleX = cx - (rollError / maxAngle * bubbleRadius).toFloat()
            .coerceIn(-bubbleRadius, bubbleRadius)
        val bubbleY = cy - (pitchError / maxAngle * bubbleRadius).toFloat()
            .coerceIn(-bubbleRadius, bubbleRadius)
        val totalError = sqrt(pitchError * pitchError + rollError * rollError)
        val bubbleColor = when {
            totalError <= 0.2 -> PinGreen
            totalError <= 0.5 -> PinYellow
            else -> PinRed
        }
        drawCircle(
            color = bubbleColor.copy(alpha = 0.3f),
            radius = canvasRadius * 0.10f,
            center = Offset(bubbleX, bubbleY)
        )
        drawCircle(
            color = bubbleColor,
            radius = canvasRadius * 0.06f,
            center = Offset(bubbleX, bubbleY)
        )

        drawLeg(rlPos, legRadius, goodZoneRadius, rlErr, "RL")
        drawLeg(rrPos, legRadius, goodZoneRadius, rrErr, "RR")
        drawLeg(flPos, legRadius, goodZoneRadius, flErr, "FL")
        drawLeg(frPos, legRadius, goodZoneRadius, frErr, "FR")

        if (abs(pitchError) > 0.3 || abs(rollError) > 0.3) {
            drawAdjustmentArrow(cx, cy, pitchError, rollError, canvasRadius)
        }
    }
}

private fun DrawScope.drawLeg(
    center: Offset,
    radius: Float,
    goodZoneRadius: Float,
    heightError: Double,
    label: String
) {
    val absErr = abs(heightError)
    val color = when {
        absErr <= 0.2 -> PinGreen
        absErr <= 0.5 -> PinYellow
        else -> PinRed
    }

    drawCircle(
        color = PinBlue.copy(alpha = 0.25f),
        radius = goodZoneRadius,
        center = center
    )
    drawCircle(
        color = PinBlue.copy(alpha = 0.6f),
        radius = goodZoneRadius,
        center = center,
        style = Stroke(width = 2f)
    )

    drawCircle(color = color.copy(alpha = 0.4f), radius = radius, center = center)
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = 3f))

    drawContext.canvas.nativeCanvas.drawText(
        label,
        center.x,
        center.y + 5f,
        android.graphics.Paint().apply {
            this.color = android.graphics.Color.WHITE
            textSize = radius * 0.75f
            textAlign = android.graphics.Paint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
    )

    if (absErr > 0.3) {
        val arrowDir = if (heightError > 0) 1f else -1f
        val arrowStart = Offset(center.x, center.y - radius * arrowDir * 1.6f)
        val arrowEnd = Offset(center.x, center.y - radius * arrowDir * 2.4f)
        drawLine(color, arrowStart, arrowEnd, strokeWidth = 3f)
        val headSize = radius * 0.4f
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
    canvasRadius: Float
) {
    val arrowLen = canvasRadius * 0.15f
    val dx = -(rollError / 5.0).toFloat().coerceIn(-1f, 1f) * arrowLen
    val dy = -(pitchError / 5.0).toFloat().coerceIn(-1f, 1f) * arrowLen
    val startX = cx
    val startY = cy
    drawLine(
        Color.White.copy(alpha = 0.5f),
        Offset(startX, startY),
        Offset(startX + dx, startY + dy),
        strokeWidth = 3f
    )
}

@Composable
private fun LegAdjustmentHints(state: LevelState) {
    val pitchError = state.pitch - state.targetAngle
    val rollError = state.roll

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
                LegHint("RL", rlErr)
                LegHint("RR", rrErr)
            }
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegHint("FL", flErr)
                LegHint("FR", frErr)
            }
        }
    }
}

@Composable
private fun LegHint(label: String, heightError: Double) {
    val absErr = abs(heightError)
    val color = when {
        absErr <= 0.2 -> PinGreen
        absErr <= 0.5 -> PinYellow
        else -> PinRed
    }
    val hint = when {
        absErr <= 0.2 -> "OK"
        heightError > 0 -> "Lower"
        else -> "Raise"
    }
    val arrow = when {
        absErr <= 0.2 -> "\u2713"
        heightError > 0 -> "\u2193"
        else -> "\u2191"
    }

    Box(modifier = Modifier.size(width = 90.dp, height = 36.dp)) {
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

private fun angleColor(actual: Double, target: Double): Color {
    val diff = abs(actual - target)
    return when {
        diff <= 0.2 -> PinGreen
        diff <= 0.5 -> PinYellow
        else -> PinRed
    }
}
