package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

enum class VisualTopic {
    HISTORY,
    GEOGRAPHY,
    SCIENCE,
    STORIES
}

@Composable
fun VisualizationsScreen(
    initialTopic: VisualTopic = VisualTopic.HISTORY,
    onOpenArticleForTopic: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedTopic by remember { mutableStateOf(initialTopic) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .testTag("visualizations_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Topic Visualizer",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Interactive diagrams for History, Geography, Science & Stories",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Topic Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VisualTopicItem(
                    label = "History Timeline",
                    icon = Icons.Default.HistoryEdu,
                    isSelected = selectedTopic == VisualTopic.HISTORY,
                    onClick = { selectedTopic = VisualTopic.HISTORY }
                )
                VisualTopicItem(
                    label = "Geography & Climate",
                    icon = Icons.Default.Public,
                    isSelected = selectedTopic == VisualTopic.GEOGRAPHY,
                    onClick = { selectedTopic = VisualTopic.GEOGRAPHY }
                )
                VisualTopicItem(
                    label = "Solar System & Science",
                    icon = Icons.Default.Science,
                    isSelected = selectedTopic == VisualTopic.SCIENCE,
                    onClick = { selectedTopic = VisualTopic.SCIENCE }
                )
                VisualTopicItem(
                    label = "Story Narrative Arc",
                    icon = Icons.Default.AutoStories,
                    isSelected = selectedTopic == VisualTopic.STORIES,
                    onClick = { selectedTopic = VisualTopic.STORIES }
                )
            }
        }

        // Active Topic Visualizer Content
        when (selectedTopic) {
            VisualTopic.HISTORY -> HistoryTimelineVisualizer()
            VisualTopic.GEOGRAPHY -> GeographyHimalayasVisualizer()
            VisualTopic.SCIENCE -> ScienceSolarSystemVisualizer()
            VisualTopic.STORIES -> StoryNarrativeArcVisualizer()
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun VisualTopicItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clickable { onClick() }
            .testTag("visual_tab_$label")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// -------------------------------------------------------------
// 1. HISTORY TIMELINE VISUALIZER
// -------------------------------------------------------------
data class HistoryMilestone(
    val era: String,
    val period: String,
    val title: String,
    val description: String,
    val keyArtifacts: String,
    val color: Color
)

@Composable
fun HistoryTimelineVisualizer() {
    val milestones = listOf(
        HistoryMilestone(
            era = "2500 BCE",
            period = "Bronze Age",
            title = "Indus Valley Civilization (Harappa & Mohenjo-Daro)",
            description = "Pioneered brick-paved urban layout, dockyards in Lothal, advanced standardized weights, and peaceful trade with Mesopotamia.",
            keyArtifacts = "Steatite Seals, Dancing Girl of Mohenjo-Daro, Great Bath",
            color = Color(0xFFF59E0B)
        ),
        HistoryMilestone(
            era = "322 BCE",
            period = "Classical Era",
            title = "Mauryan Empire & Ashokan Edicts",
            description = "First pan-Indian empire unified under Chandragupta Maurya and philosopher Chanakya; Emperor Ashoka pioneered non-violence and rock edicts.",
            keyArtifacts = "Ashoka Lion Capital, Sanchi Stupa, Rock Inscriptions",
            color = Color(0xFF38BDF8)
        ),
        HistoryMilestone(
            era = "320 CE",
            period = "Golden Age",
            title = "Gupta Empire & Ancient Science",
            description = "Golden age of mathematics (Aryabhata's zero and pi), Sanskrit literature (Kalidasa), and the great university of Nalanda.",
            keyArtifacts = "Aryabhatiya Astronomy, Nalanda Mahavihara, Ajanta Caves",
            color = Color(0xFF10B981)
        ),
        HistoryMilestone(
            era = "1000 CE",
            period = "Maritime Age",
            title = "Chola Dynasty & Maritime Silk Route",
            description = "Dominant maritime empire across Southeast Asia, celebrated for Dravidian temple architecture and bronze sculpture mastery.",
            keyArtifacts = "Brihadisvara Temple, Nataraja Bronzes, Naval Expeditions",
            color = Color(0xFF8B5CF6)
        ),
        HistoryMilestone(
            era = "1947 CE",
            period = "Modern Era",
            title = "Indian Renaissance & Independence",
            description = "Literary resurgence led by Rabindranath Tagore, Swami Vivekananda, and freedom struggle yielding modern democratic constitution.",
            keyArtifacts = "Gitanjali (Nobel Prize 1913), Constitution of India",
            color = Color(0xFFF43F5E)
        )
    )

    var activeIndex by remember { mutableIntStateOf(0) }
    val active = milestones[activeIndex]

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "HISTORICAL TIMELINE SCRUBBER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Timeline Canvas Graphic
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    val width = size.width
                    val y = size.height / 2
                    // Background axis line
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(20f, y),
                        end = Offset(width - 20f, y),
                        strokeWidth = 4f
                    )
                    // Milestone points
                    val step = (width - 40f) / (milestones.size - 1)
                    milestones.forEachIndexed { i, m ->
                        val x = 20f + i * step
                        val isSel = i == activeIndex
                        drawCircle(
                            color = if (isSel) m.color else m.color.copy(alpha = 0.5f),
                            radius = if (isSel) 14f else 8f,
                            center = Offset(x, y)
                        )
                        if (isSel) {
                            drawCircle(
                                color = Color.White,
                                radius = 6f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }

                // Interactive slider to scrub timeline
                Slider(
                    value = activeIndex.toFloat(),
                    onValueChange = { activeIndex = it.toInt() },
                    valueRange = 0f..(milestones.size - 1).toFloat(),
                    steps = milestones.size - 2,
                    colors = SliderDefaults.colors(
                        thumbColor = active.color,
                        activeTrackColor = active.color
                    )
                )

                // Timeline era labels row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    milestones.forEachIndexed { idx, m ->
                        Text(
                            text = m.era,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = if (idx == activeIndex) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (idx == activeIndex) active.color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { activeIndex = idx }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Era Detail Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = active.color.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, active.color.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = active.color
                    ) {
                        Text(
                            text = active.era,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = active.period,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = active.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = active.description,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏛️", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "KEY DISCOVERIES & ARTIFACTS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = active.color
                                )
                            )
                            Text(
                                text = active.keyArtifacts,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. GEOGRAPHY & CLIMATE VISUALIZER
// -------------------------------------------------------------
@Composable
fun GeographyHimalayasVisualizer() {
    var elevationMeters by remember { mutableFloatStateOf(4500f) }

    val zoneInfo = when {
        elevationMeters > 7000f -> Triple("Death Zone / Permanent Ice", "-25°C to -40°C", "Glacial moraines, sub-zero winds, Everest (8848m)")
        elevationMeters > 4000f -> Triple("Alpine Tundra & Glaciers", "-5°C to 5°C", "Yak pastures, rhododendron shrubs, Source of Ganga & Indus")
        elevationMeters > 2000f -> Triple("Temperate Coniferous Forest", "10°C to 18°C", "Pine, deodar, oak forests, Hill stations (Darjeeling, Shimla)")
        elevationMeters > 500f -> Triple("Subtropical Terai Foothills", "20°C to 30°C", "Sal trees, elephant corridors, high monsoon rainfall")
        else -> Triple("Fertile Ganga-Brahmaputra Plains", "25°C to 38°C", "Alluvial soil, agriculture, Sundarbans mangrove delta")
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "HIMALAYAN TOPOGRAPHY & MONSOON PROFILE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mountain Canvas Elevation Cross Section
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Sky gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF0284C7), Color(0xFFE0F2FE))
                        )
                    )

                    // Mountain silhouette path
                    val mountainPath = Path().apply {
                        moveTo(0f, h * 0.9f)
                        lineTo(w * 0.2f, h * 0.75f) // Terai
                        lineTo(w * 0.45f, h * 0.5f)  // Middle hills
                        lineTo(w * 0.75f, h * 0.15f) // High peak (Everest)
                        lineTo(w * 0.85f, h * 0.35f)
                        lineTo(w, h * 0.45f) // Tibetan plateau
                        lineTo(w, h)
                        lineTo(0f, h)
                        close()
                    }
                    drawPath(
                        path = mountainPath,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFF8FAFC), Color(0xFF64748B), Color(0xFF1E293B))
                        )
                    )

                    // Current elevation marker horizontal line
                    val markerY = h - (elevationMeters / 9000f) * (h * 0.85f)
                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = Offset(0f, markerY),
                        end = Offset(w, markerY),
                        strokeWidth = 3f
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Elevation Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Elevation:",
                        style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Text(
                        text = "${elevationMeters.toInt()} meters",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Slider(
                    value = elevationMeters,
                    onValueChange = { elevationMeters = it },
                    valueRange = 0f..8848f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0m (Delta)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    Text("4000m (Alpine)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                    Text("8848m (Everest)", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Climate Zone Information Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "ECOLOGICAL ZONE AT ${elevationMeters.toInt()}m",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = zoneInfo.first,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🌡️ Temperature Range: ", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    Text(zoneInfo.second, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text("🌿 Features: ", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold))
                    Text(zoneInfo.third, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. SCIENCE: SOLAR SYSTEM & PLANETARY ORBITS
// -------------------------------------------------------------
data class PlanetInfo(
    val name: String,
    val distanceAU: Float,
    val diameterKm: String,
    val orbitalDays: String,
    val color: Color
)

@Composable
fun ScienceSolarSystemVisualizer() {
    val planets = listOf(
        PlanetInfo("Mercury", 0.39f, "4,879 km", "88 days", Color(0xFF94A3B8)),
        PlanetInfo("Venus", 0.72f, "12,104 km", "225 days", Color(0xFFFBBF24)),
        PlanetInfo("Earth", 1.0f, "12,742 km", "365.25 days", Color(0xFF38BDF8)),
        PlanetInfo("Mars", 1.52f, "6,779 km", "687 days", Color(0xFFEF4444)),
        PlanetInfo("Jupiter", 5.2f, "139,820 km", "12 years", Color(0xFFD97706)),
        PlanetInfo("Saturn", 9.58f, "116,460 km", "29.5 years", Color(0xFFFDE68A))
    )

    var selectedPlanet by remember { mutableStateOf(planets[2]) } // Earth default

    val infiniteTransition = rememberInfiniteTransition(label = "orbit_animation")
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Space dark
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "INTERACTIVE SOLAR SYSTEM MODEL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Planetary Orbits Canvas
                Canvas(
                    modifier = Modifier
                        .size(240.dp)
                ) {
                    val center = Offset(size.width / 2, size.height / 2)

                    // Draw Sun
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFFFEF08A), Color(0xFFF59E0B)),
                            center = center,
                            radius = 24f
                        ),
                        radius = 20f,
                        center = center
                    )

                    // Draw orbits and planets
                    val maxRadius = size.width / 2 - 16f
                    planets.forEachIndexed { i, planet ->
                        val orbitRadius = 32f + (i * (maxRadius - 32f) / (planets.size - 1))
                        // Orbit line
                        drawCircle(
                            color = Color.White.copy(alpha = 0.2f),
                            radius = orbitRadius,
                            center = center,
                            style = Stroke(width = 1.5f)
                        )

                        // Calculate moving planet position
                        val speedMultiplier = (planets.size - i) * 0.8f
                        val angleRad = Math.toRadians(((orbitAngle * speedMultiplier) + (i * 60)).toDouble())
                        val px = center.x + orbitRadius * cos(angleRad).toFloat()
                        val py = center.y + orbitRadius * sin(angleRad).toFloat()

                        val isCurrent = planet.name == selectedPlanet.name
                        drawCircle(
                            color = planet.color,
                            radius = if (isCurrent) 9f else 5f,
                            center = Offset(px, py)
                        )
                        if (isCurrent) {
                            drawCircle(
                                color = Color.White,
                                radius = 13f,
                                center = Offset(px, py),
                                style = Stroke(width = 2f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Planet Selector Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    planets.forEach { planet ->
                        val isSelected = planet.name == selectedPlanet.name
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) planet.color else Color(0xFF1E293B),
                            modifier = Modifier.clickable { selectedPlanet = planet }
                        ) {
                            Text(
                                text = planet.name,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Planet Details
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, selectedPlanet.color.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(selectedPlanet.color)
                    )
                    Text(
                        text = "${selectedPlanet.name.uppercase()} DATA",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = selectedPlanet.color
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${selectedPlanet.distanceAU} AU",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Distance from Sun", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedPlanet.diameterKm,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Diameter", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selectedPlanet.orbitalDays,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text("Orbital Period", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. STORIES: NARRATIVE ARC VISUALIZER
// -------------------------------------------------------------
data class ArcStage(
    val stage: String,
    val storyBeat: String,
    val exampleKabuliwala: String
)

@Composable
fun StoryNarrativeArcVisualizer() {
    val stages = listOf(
        ArcStage(
            stage = "1. Exposition",
            storyBeat = "Introduce characters, world, and baseline normal.",
            exampleKabuliwala = "Five-year-old Mini chatters endlessly in Kolkata; Rahmat the Afghan dry-fruit merchant wanders the streets."
        ),
        ArcStage(
            stage = "2. Inciting Incident",
            storyBeat = "An event sparks the journey and forms the bond.",
            exampleKabuliwala = "Mini overcomes fear of the giant turbaned merchant and they bond over shared laughter and pistachios."
        ),
        ArcStage(
            stage = "3. Rising Action",
            storyBeat = "Complications, emotional investment, and conflict grow.",
            exampleKabuliwala = "Rahmat visits regularly as a father figure, but a violent altercation over debt leads to his sudden arrest and imprisonment."
        ),
        ArcStage(
            stage = "4. Climax",
            storyBeat = "The highest point of tension and emotional revelation.",
            exampleKabuliwala = "Years later, on Mini's wedding day, Rahmat arrives after release from jail, carrying a paper with his own daughter's ink handprint."
        ),
        ArcStage(
            stage = "5. Resolution",
            storyBeat = "Themes consolidate, leaving a profound emotional impact.",
            exampleKabuliwala = "The narrator realizes all fathers across nations share the same love, and gives Rahmat money to return home to his daughter."
        )
    )

    var currentStageIndex by remember { mutableIntStateOf(0) }
    val currentStage = stages[currentStageIndex]

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "FREYTAG'S NARRATIVE PYRAMID",
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Narrative Arc Curve Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // Draw Pyramid Path
                    val arcPath = Path().apply {
                        moveTo(20f, h * 0.85f)
                        lineTo(w * 0.25f, h * 0.7f)
                        lineTo(w * 0.5f, h * 0.2f) // Climax
                        lineTo(w * 0.75f, h * 0.5f)
                        lineTo(w - 20f, h * 0.85f)
                    }
                    drawPath(
                        path = arcPath,
                        color = Color(0xFFF59E0B),
                        style = Stroke(width = 4f)
                    )

                    // Draw 5 stage nodes
                    val points = listOf(
                        Offset(20f, h * 0.85f),
                        Offset(w * 0.25f, h * 0.7f),
                        Offset(w * 0.5f, h * 0.2f),
                        Offset(w * 0.75f, h * 0.5f),
                        Offset(w - 20f, h * 0.85f)
                    )

                    points.forEachIndexed { i, pt ->
                        val isSel = i == currentStageIndex
                        drawCircle(
                            color = if (isSel) Color(0xFF38BDF8) else Color(0xFFF59E0B),
                            radius = if (isSel) 12f else 6f,
                            center = pt
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stage Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    stages.forEachIndexed { idx, st ->
                        val isSel = idx == currentStageIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { currentStageIndex = idx }
                        ) {
                            Text(
                                text = st.stage,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal),
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Narrative Beat Description Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = currentStage.stage.uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentStage.storyBeat,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "STORY APPLIED: RABINDRANATH TAGORE'S KABULIWALA",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentStage.exampleKabuliwala,
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
