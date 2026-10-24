package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import com.example.data.tts.SystemVoiceInfo
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Article
import com.example.data.translation.TranslationResult
import com.example.data.tts.SpeechManager
import com.example.ui.components.AudioPlayerBar
import com.example.ui.theme.LightBackgroundOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    article: Article?,
    currentLineIndex: Int,
    isPlaying: Boolean,
    speechRate: Float,
    autoAdvance: Boolean,
    autoScroll: Boolean,
    translationResult: TranslationResult?,
    showDualLanguage: Boolean,
    readTranslatedVoice: Boolean,
    currentWordRange: Pair<Int, Int>? = null,
    onBack: () -> Unit,
    onReadLine: (Int) -> Unit,
    onPlayPause: () -> Unit,
    onNextLine: () -> Unit,
    onPrevLine: () -> Unit,
    onReplayLine: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onLanguageChange: (String) -> Unit,
    onToggleAutoAdvance: () -> Unit,
    onToggleAutoScroll: () -> Unit,
    onToggleDualLanguage: () -> Unit,
    onToggleReadTranslatedVoice: () -> Unit,
    onOpenTranslateScreen: () -> Unit,
    onOpenTopicVisualizer: (VisualTopic) -> Unit,
    onInspectWord: (word: String, lineText: String) -> Unit,
    onWordLongPress: (word: String, lineText: String) -> Unit,
    onAddNote: (lineIndex: Int, quote: String) -> Unit,
    onSummarise: () -> Unit,
    onOpenColorPicker: () -> Unit = {},
    selectedVoice: SystemVoiceInfo? = null,
    onOpenVoiceSelection: (() -> Unit)? = null,
    currentLightBgOption: LightBackgroundOption = LightBackgroundOption.CRISP_WHITE,
    modifier: Modifier = Modifier
) {
    if (article == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Select an article or book from the Library to begin reading.")
        }
        return
    }

    val lines = article.getLines()
    val totalLines = lines.size
    val rawProgress = if (totalLines > 0) ((currentLineIndex + 1).toFloat() / totalLines.toFloat()).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(targetValue = rawProgress, label = "reader_progress")
    val readingPercentage = if (totalLines > 0) ((currentLineIndex + 1) * 100 / totalLines).coerceIn(0, 100) else 0

    val listState = rememberLazyListState()

    // Smooth auto-scrolling to the currently spoken line
    LaunchedEffect(currentLineIndex, autoScroll) {
        if (autoScroll && currentLineIndex in lines.indices) {
            listState.animateScrollToItem(currentLineIndex.coerceAtLeast(0))
        }
    }

    // Determine visualizer topic based on article category
    val topicForArticle = when (article.category.lowercase()) {
        "history" -> VisualTopic.HISTORY
        "geography" -> VisualTopic.GEOGRAPHY
        "science" -> VisualTopic.SCIENCE
        "stories", "literature" -> VisualTopic.STORIES
        else -> VisualTopic.HISTORY
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${article.languageName} • By ${article.author} • $readingPercentage% read",
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("reader_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Library"
                        )
                    }
                },
                actions = {
                    // Summarise AI button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .clickable { onSummarise() }
                            .testTag("reader_summarise_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Summary",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Add Note button
                    IconButton(
                        onClick = {
                            val currentQuote = lines.getOrNull(currentLineIndex) ?: article.title
                            onAddNote(currentLineIndex, currentQuote)
                        },
                        modifier = Modifier.testTag("reader_add_note_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Add Note",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Change background light color button
                    IconButton(
                        onClick = onOpenColorPicker,
                        modifier = Modifier.testTag("reader_bg_color_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Change background light color",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            val activeText = if (readTranslatedVoice && translationResult != null) {
                translationResult.lineByLine.getOrNull(currentLineIndex)?.second ?: lines.getOrNull(currentLineIndex) ?: ""
            } else {
                lines.getOrNull(currentLineIndex) ?: ""
            }
            val activeLangCode = if (readTranslatedVoice && translationResult != null) {
                translationResult.targetLanguageCode
            } else {
                article.languageCode
            }

            AudioPlayerBar(
                isPlaying = isPlaying,
                currentLine = currentLineIndex,
                totalLines = lines.size,
                currentLineText = activeText,
                speechRate = speechRate,
                currentLanguageCode = activeLangCode,
                autoAdvance = autoAdvance,
                autoScroll = autoScroll,
                onPlayPause = onPlayPause,
                onNextLine = onNextLine,
                onPrevLine = onPrevLine,
                onReplayLine = onReplayLine,
                onSpeedChange = onSpeedChange,
                onLanguageChange = onLanguageChange,
                onToggleAutoAdvance = onToggleAutoAdvance,
                onToggleAutoScroll = onToggleAutoScroll,
                selectedVoice = selectedVoice,
                onOpenVoiceSelection = onOpenVoiceSelection
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Visual Reading Progress Bar & Percentage Indicator Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("reader_progress_card"),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (readingPercentage == 100) Icons.Default.CheckCircle else Icons.Default.AutoStories,
                                contentDescription = "Reading Progress",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Reading Progress",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Percentage indicator badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (readingPercentage == 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.testTag("reading_progress_percentage_badge")
                        ) {
                            Text(
                                text = "$readingPercentage%",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = if (readingPercentage == 100) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                    .testTag("reading_percentage_text")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Animated Visual Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .testTag("reader_visual_progress_bar"),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (totalLines > 0) "Line ${currentLineIndex + 1} of $totalLines" else "0 lines",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = when {
                                readingPercentage == 100 -> "Completed 🎉"
                                readingPercentage >= 75 -> "Almost finished"
                                readingPercentage >= 50 -> "Halfway through"
                                readingPercentage >= 25 -> "In progress"
                                else -> "Just started"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }

            // Interactive Action Pills Banner: Dual-Language Toggle, Voice Toggle, Background Color, Visualizer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Background Light Color Selector Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable { onOpenColorPicker() }
                        .testTag("reader_change_bg_color_pill")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(13.dp)
                                .clip(CircleShape)
                                .background(currentLightBgOption.previewColor)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Light: ${currentLightBgOption.title}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Dual Language Toggle Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (showDualLanguage) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onToggleDualLanguage() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (showDualLanguage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showDualLanguage) "Dual Text: ON (${translationResult?.targetLanguage ?: "Bengali"})" else "Dual Text: OFF",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = if (showDualLanguage) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Voice Selector Pill
                if (onOpenVoiceSelection != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        modifier = Modifier
                            .clickable { onOpenVoiceSelection() }
                            .testTag("reader_voice_pill_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Voice: ${selectedVoice?.name ?: "System"}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                // Voice Language Toggle
                if (translationResult != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (readTranslatedVoice) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { onToggleReadTranslatedVoice() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (readTranslatedVoice) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (readTranslatedVoice) "Voice: ${translationResult.targetLanguage}" else "Voice: Original",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                                color = if (readTranslatedVoice) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Open Translate Prompt Screen
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onOpenTranslateScreen() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("✨ Change Translation", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp))
                    }
                }

                // Topic Visualizer Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                    modifier = Modifier.clickable { onOpenTopicVisualizer(topicForArticle) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View ${article.category} Visualizer",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }
                }
            }

            // Quick interaction tip
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💡 Tap word for definition • Long-press word to save to Vocabulary Builder",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Line-by-line LazyColumn
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("reader_line_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(lines) { index, lineText ->
                    val isActive = index == currentLineIndex
                    val translatedSnippet = if (showDualLanguage && translationResult != null) {
                        translationResult.lineByLine.getOrNull(index)?.second
                    } else null

                    ReaderLineItem(
                        lineIndex = index,
                        lineText = lineText,
                        translatedText = translatedSnippet,
                        isActive = isActive,
                        isPlaying = isPlaying && isActive,
                        currentWordRange = if (isActive && isPlaying) currentWordRange else null,
                        onReadLine = { onReadLine(index) },
                        onWordClick = { word -> onInspectWord(word, lineText) },
                        onWordLongPress = { word -> onWordLongPress(word, lineText) },
                        onAddNote = { onAddNote(index, lineText) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderLineItem(
    lineIndex: Int,
    lineText: String,
    translatedText: String? = null,
    isActive: Boolean,
    isPlaying: Boolean,
    currentWordRange: Pair<Int, Int>? = null,
    onReadLine: () -> Unit,
    onWordClick: (String) -> Unit,
    onWordLongPress: (String) -> Unit,
    onAddNote: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isActive && isPlaying -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            isActive -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
            else -> Color.Transparent
        },
        label = "line_bg"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else -> Color.Transparent
        },
        label = "line_border"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .testTag("line_item_$lineIndex"),
        color = backgroundColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Line number or speech icon indicator
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onReadLine() },
                contentAlignment = Alignment.Center
            ) {
                if (isActive && isPlaying) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Currently reading",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (isActive) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Read this line",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Text(
                        text = "${lineIndex + 1}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Line words and dual-language translation
            Column(modifier = Modifier.weight(1f)) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReadLine() },
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val words = lineText.split(Regex("\\s+"))
                    var charOffset = 0
                    words.forEach { word ->
                        val wordStart = lineText.indexOf(word, charOffset)
                        val wordEnd = if (wordStart >= 0) wordStart + word.length else charOffset + word.length
                        if (wordStart >= 0) charOffset = wordEnd

                        val isCurrentSpokenWord = currentWordRange != null &&
                            ((wordStart in currentWordRange.first until currentWordRange.second) ||
                             (currentWordRange.first in wordStart until wordEnd))

                        val wordBg = if (isCurrentSpokenWord) MaterialTheme.colorScheme.primary.copy(alpha = 0.28f) else Color.Transparent

                        Text(
                            text = word,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 17.sp,
                                lineHeight = 26.sp,
                                fontWeight = if (isCurrentSpokenWord) FontWeight.ExtraBold else if (isActive) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            color = if (isCurrentSpokenWord) MaterialTheme.colorScheme.primary else if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(wordBg)
                                .combinedClickable(
                                    onClick = { onWordClick(word) },
                                    onLongClick = { onWordLongPress(word) }
                                )
                                .padding(horizontal = 2.dp, vertical = 1.dp)
                        )
                    }
                }

                // Translated dual text underneath original
                if (!translatedText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = translatedText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                        ),
                        modifier = Modifier.clickable { onReadLine() }
                    )
                }

                // Action pill when active
                if (isActive) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { onAddNote() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EditNote,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add Note",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
