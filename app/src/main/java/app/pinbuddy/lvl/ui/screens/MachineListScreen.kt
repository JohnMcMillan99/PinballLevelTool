package app.pinbuddy.lvl.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import app.pinbuddy.lvl.R
import app.pinbuddy.lvl.model.MachineProfile
import app.pinbuddy.lvl.model.getPlayfieldDrawableId
import app.pinbuddy.lvl.model.Preset
import app.pinbuddy.lvl.ui.theme.DarkSurface
import app.pinbuddy.lvl.ui.theme.DarkSurfaceVariant
import app.pinbuddy.lvl.ui.theme.PinBlue
import app.pinbuddy.lvl.ui.theme.PinGreen
import app.pinbuddy.lvl.viewmodel.LevelViewModel

@Composable
fun MachineListScreen(
    viewModel: LevelViewModel,
    onMachineTap: (MachineProfile) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var editingMachine by remember { mutableStateOf<MachineProfile?>(null) }
    var editAngleText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        item {
            Text(
                text = "Presets",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Preset.entries.forEach { preset ->
                    Button(
                        onClick = { viewModel.setTargetAngle(preset.angle) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.targetAngle == preset.angle && preset != Preset.CUSTOM)
                                PinBlue else DarkSurfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = preset.label,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "${preset.angle}°",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color(0xFF333333)
            )
            Text(
                text = "Machines",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(state.machines, key = { it.id }) { machine ->
            val isSelected = state.currentMachine?.id == machine.id
            val isEditing = editingMachine?.id == machine.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onMachineTap(machine) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) DarkSurface else DarkSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (isSelected) BorderStroke(2.dp, PinGreen) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(getPlayfieldDrawableId(machine.id)),
                        contentDescription = "${machine.name} playfield",
                        modifier = Modifier
                            .size(72.dp, 48.dp)
                            .padding(end = 12.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = machine.name,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isSelected) PinGreen else Color.White
                        )
                        if (isSelected) {
                            Text(
                                text = "Currently selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PinGreen.copy(alpha = 0.7f)
                            )
                        }
                    }

                    if (isEditing) {
                        val focusManager = LocalFocusManager.current
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = editAngleText,
                                onValueChange = { editAngleText = it },
                                modifier = Modifier.width(80.dp),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(onDone = {
                                    editAngleText.toDoubleOrNull()?.let { angle ->
                                        viewModel.updateMachineAngle(machine.id, angle)
                                    }
                                    editingMachine = null
                                    focusManager.clearFocus()
                                }),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = PinBlue,
                                    cursorColor = PinBlue
                                )
                            )
                            Spacer(Modifier.width(4.dp))
                            Text("°", color = Color.White)
                        }
                    } else {
                        TextButton(onClick = {
                            editingMachine = machine
                            editAngleText = String.format("%.1f", machine.targetAngle)
                        }) {
                            Text(
                                text = "${String.format("%.1f", machine.targetAngle)}°",
                                style = MaterialTheme.typography.headlineMedium,
                                color = PinBlue,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}
