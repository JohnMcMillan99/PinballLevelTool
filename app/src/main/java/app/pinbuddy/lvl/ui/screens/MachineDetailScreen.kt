package app.pinbuddy.lvl.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.pinbuddy.lvl.model.getPlayfieldDrawableId
import app.pinbuddy.lvl.model.MachineProfile
import app.pinbuddy.lvl.model.getMachineGuideOrEmpty
import app.pinbuddy.lvl.ui.theme.DarkSurfaceVariant
import app.pinbuddy.lvl.ui.theme.PinBlue
import app.pinbuddy.lvl.ui.theme.PinGreen
import app.pinbuddy.lvl.viewmodel.LevelViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineDetailScreen(
    machine: MachineProfile,
    viewModel: LevelViewModel,
    onBack: () -> Unit,
    onLevelThisMachine: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val guide = getMachineGuideOrEmpty(machine.id)
    var skillShotsExpanded by remember { mutableStateOf(true) }
    var multiballExpanded by remember { mutableStateOf(false) }
    var tipsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TopAppBar(
            title = { Text(machine.name) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
            ) {
                Image(
                    painter = painterResource(getPlayfieldDrawableId(machine.id)),
                    contentDescription = "${machine.name} playfield",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target angle: ${String.format("%.1f", machine.targetAngle)}°",
                    style = MaterialTheme.typography.titleLarge,
                    color = PinBlue
                )
                Button(
                    onClick = {
                        viewModel.selectMachine(machine)
                        onLevelThisMachine()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PinGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Level This Machine", fontWeight = FontWeight.Bold)
                }
            }

            if (!guide.isEmpty) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "How to Play",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))

                GuideSection(
                    title = "Skill Shots",
                    items = guide.skillShots,
                    expanded = skillShotsExpanded,
                    onToggle = { skillShotsExpanded = !skillShotsExpanded }
                )

                GuideSection(
                    title = "Multiball Triggers",
                    items = guide.multiballTriggers,
                    expanded = multiballExpanded,
                    onToggle = { multiballExpanded = !multiballExpanded }
                )

                GuideSection(
                    title = "Tips",
                    items = guide.tips,
                    expanded = tipsExpanded,
                    onToggle = { tipsExpanded = !tipsExpanded }
                )
            } else {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Strategy guide coming soon.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun GuideSection(
    title: String,
    items: List<String>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onToggle),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PinBlue,
                fontWeight = FontWeight.Bold
            )
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    items.forEach { item ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                color = PinGreen,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = item,
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
