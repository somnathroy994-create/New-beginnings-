package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.AppTab
import com.example.ui.ReadAloudViewModel
import com.example.ui.ScanAction
import com.example.ui.components.AddContentDialog
import com.example.ui.components.AddNoteDialog
import com.example.ui.components.LightColorPickerDialog
import com.example.ui.components.ScannerBottomSheet
import com.example.ui.components.SummaryBottomSheet
import com.example.ui.components.VoiceSelectionDialog
import com.example.ui.components.WordLookupDialog
import com.example.ui.screens.HabitNotesScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.TranslationScreen
import com.example.ui.screens.VisualizationsScreen
import com.example.ui.theme.LightBackgroundOption
import com.example.ui.theme.ReadAloudTheme

class MainActivity : ComponentActivity() {
    private val viewModel: ReadAloudViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            val lightBgOption by viewModel.lightBackgroundOption.collectAsState()
            ReadAloudTheme(lightBackgroundOption = lightBgOption) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ReadAloudViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val articles by viewModel.articles.collectAsState()
    val selectedArticle by viewModel.selectedArticle.collectAsState()
    val currentLineIndex by viewModel.currentLineIndex.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val allVocabulary by viewModel.allVocabulary.collectAsState()

    // Speech manager state
    val isPlaying by viewModel.speechManager.isPlaying.collectAsState()
    val currentWordRange by viewModel.speechManager.currentWordRange.collectAsState()
    val speechRate by viewModel.speechManager.speechRate.collectAsState()
    val autoAdvance by viewModel.autoAdvance.collectAsState()
    val autoScroll by viewModel.autoScroll.collectAsState()
    val currentVoice by viewModel.speechManager.currentVoice.collectAsState()
    val availableVoices by viewModel.speechManager.availableVoices.collectAsState()
    val voicePitch by viewModel.speechManager.pitch.collectAsState()

    // Translation & Visuals state
    val translationResult by viewModel.translationResult.collectAsState()
    val showDualLanguage by viewModel.showDualLanguage.collectAsState()
    val readTranslatedVoice by viewModel.readTranslatedVoice.collectAsState()
    val activeVisualTopic by viewModel.activeVisualTopic.collectAsState()

    // Dialog states
    val showAddArticleDialog by viewModel.showAddArticleDialog.collectAsState()
    val showAddNoteDialog by viewModel.showAddNoteDialog.collectAsState()
    val noteTargetLine by viewModel.noteTargetLine.collectAsState()
    val showWordLookupDialog by viewModel.showWordLookupDialog.collectAsState()
    val inspectedWord by viewModel.inspectedWord.collectAsState()
    val showSummarySheet by viewModel.showSummarySheet.collectAsState()
    val summaryResult by viewModel.summaryResult.collectAsState()
    val isSummarizing by viewModel.isSummarizing.collectAsState()
    val showColorPicker by viewModel.showColorPicker.collectAsState()
    val lightBackgroundOption by viewModel.lightBackgroundOption.collectAsState()
    val showVoiceSelectionDialog by viewModel.showVoiceSelectionDialog.collectAsState()

    // Scanner state
    val showScannerSheet by viewModel.showScannerSheet.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val extractedScanResult by viewModel.extractedScanResult.collectAsState()
    val scanError by viewModel.scanError.collectAsState()
    val isEnrichingWord by viewModel.isEnrichingWord.collectAsState()

    // Filter states
    val selectedLangFilter by viewModel.selectedLanguageFilter.collectAsState()
    val selectedCatFilter by viewModel.selectedCategoryFilter.collectAsState()

    // Back handling: If on secondary tab or reader, pressing back returns to Library
    BackHandler(enabled = currentTab != AppTab.LIBRARY) {
        viewModel.selectTab(AppTab.LIBRARY)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                // 1. Library Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.LIBRARY,
                    onClick = { viewModel.selectTab(AppTab.LIBRARY) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.LIBRARY) Icons.Default.AutoStories else Icons.Outlined.AutoStories,
                            contentDescription = "Library"
                        )
                    },
                    label = { Text("Library") },
                    modifier = Modifier.testTag("nav_library")
                )

                // 2. Reader Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.READER,
                    onClick = {
                        if (selectedArticle != null) {
                            viewModel.selectTab(AppTab.READER)
                        } else if (articles.isNotEmpty()) {
                            viewModel.openArticle(articles.first())
                        }
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (isPlaying) {
                                    Badge { Text("▶") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.READER) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                                contentDescription = "Reader"
                            )
                        }
                    },
                    label = { Text("Reader") },
                    modifier = Modifier.testTag("nav_reader")
                )

                // 3. Translate Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.TRANSLATE,
                    onClick = { viewModel.selectTab(AppTab.TRANSLATE) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.TRANSLATE) Icons.Default.Translate else Icons.Outlined.Translate,
                            contentDescription = "Translate"
                        )
                    },
                    label = { Text("Translate") },
                    modifier = Modifier.testTag("nav_translate")
                )

                // 4. Visuals Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.VISUALS,
                    onClick = { viewModel.selectTab(AppTab.VISUALS) },
                    icon = {
                        Icon(
                            imageVector = if (currentTab == AppTab.VISUALS) Icons.Default.Explore else Icons.Outlined.Explore,
                            contentDescription = "Visuals"
                        )
                    },
                    label = { Text("Visuals") },
                    modifier = Modifier.testTag("nav_visuals")
                )

                // 5. Habit & Notes Tab
                NavigationBarItem(
                    selected = currentTab == AppTab.HABIT_NOTES,
                    onClick = { viewModel.selectTab(AppTab.HABIT_NOTES) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (allNotes.isNotEmpty()) {
                                    Badge { Text("${allNotes.size}") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (currentTab == AppTab.HABIT_NOTES) Icons.Default.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                                contentDescription = "Habit & Notes"
                            )
                        }
                    },
                    label = { Text("Habit") },
                    modifier = Modifier.testTag("nav_habit")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.LIBRARY -> {
                    LibraryScreen(
                        articles = articles,
                        selectedLanguageFilter = selectedLangFilter,
                        selectedCategoryFilter = selectedCatFilter,
                        onSelectLanguageFilter = { viewModel.setLanguageFilter(it) },
                        onSelectCategoryFilter = { viewModel.setCategoryFilter(it) },
                        onOpenArticle = { viewModel.openArticle(it) },
                        onTranslateArticle = {
                            viewModel.openArticle(it)
                            viewModel.selectTab(AppTab.TRANSLATE)
                        },
                        onSummariseArticle = {
                            viewModel.openArticle(it)
                            viewModel.generateSummaryForCurrent()
                        },
                        onOpenVisualizer = { topic ->
                            viewModel.selectVisualTopic(topic)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onDeleteArticle = { viewModel.deleteArticle(it) },
                        onOpenAddArticle = { viewModel.openAddArticleDialog() },
                        onOpenScanner = { viewModel.openScannerSheet() },
                        onOpenColorPicker = { viewModel.openColorPicker() },
                        currentLightBgOption = lightBackgroundOption
                    )
                }

                AppTab.READER -> {
                    ReaderScreen(
                        article = selectedArticle,
                        currentLineIndex = currentLineIndex,
                        isPlaying = isPlaying,
                        speechRate = speechRate,
                        autoAdvance = autoAdvance,
                        autoScroll = autoScroll,
                        translationResult = translationResult,
                        showDualLanguage = showDualLanguage,
                        readTranslatedVoice = readTranslatedVoice,
                        currentWordRange = currentWordRange,
                        onBack = { viewModel.selectTab(AppTab.LIBRARY) },
                        onReadLine = { viewModel.readLine(it) },
                        onPlayPause = { viewModel.playOrPause() },
                        onNextLine = { viewModel.nextLine() },
                        onPrevLine = { viewModel.previousLine() },
                        onReplayLine = { viewModel.readLine(currentLineIndex) },
                        onSpeedChange = { viewModel.setSpeechSpeed(it) },
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onToggleAutoAdvance = { viewModel.toggleAutoAdvance() },
                        onToggleAutoScroll = { viewModel.toggleAutoScroll() },
                        onToggleDualLanguage = { viewModel.toggleDualLanguage() },
                        onToggleReadTranslatedVoice = { viewModel.toggleReadTranslatedVoice() },
                        onOpenTranslateScreen = { viewModel.selectTab(AppTab.TRANSLATE) },
                        onOpenTopicVisualizer = { topic -> viewModel.selectVisualTopic(topic) },
                        onInspectWord = { word, lineText -> viewModel.inspectWord(word, lineText) },
                        onWordLongPress = { word, lineText -> viewModel.quickSaveWordToVocabulary(word, lineText) },
                        onAddNote = { lineIdx, quote -> viewModel.openAddNoteDialog(lineIdx, quote) },
                        onSummarise = { viewModel.generateSummaryForCurrent() },
                        onOpenColorPicker = { viewModel.openColorPicker() },
                        selectedVoice = currentVoice,
                        onOpenVoiceSelection = { viewModel.openVoiceSelectionDialog() },
                        currentLightBgOption = lightBackgroundOption
                    )
                }

                AppTab.TRANSLATE -> {
                    TranslationScreen(
                        currentArticle = selectedArticle ?: articles.firstOrNull(),
                        onPronounceText = { text, langCode ->
                            viewModel.speechManager.setLanguageByCode(langCode)
                            viewModel.speechManager.speak(text, "translate_tts")
                        },
                        onSaveTranslationAsNote = { orig, trans ->
                            viewModel.addNote("Translation:\n$trans", "Translation", "#8B5CF6")
                        },
                        onOpenInReader = { title, content, langCode ->
                            viewModel.addNewArticle(title, "Translation", "Translator", "General", langCode, langCode, content)
                        }
                    )
                }

                AppTab.VISUALS -> {
                    VisualizationsScreen(
                        initialTopic = activeVisualTopic,
                        onOpenArticleForTopic = { topic ->
                            val matched = articles.find { it.category.equals(topic, ignoreCase = true) }
                            if (matched != null) viewModel.openArticle(matched)
                        }
                    )
                }

                AppTab.HABIT_NOTES -> {
                    HabitNotesScreen(
                        notes = allNotes,
                        vocabularyList = allVocabulary,
                        onOpenAddNote = { viewModel.openAddNoteDialog() },
                        onDeleteNote = { viewModel.deleteNote(it) },
                        onPronounce = { word, lang -> viewModel.pronounceWord(word, lang) },
                        onUpdateMastery = { id, status -> viewModel.updateWordMastery(id, status) },
                        onDeleteWord = { viewModel.deleteWord(it) }
                    )
                }
            }

            // Global Dialogs & Bottom Sheets
            if (showAddArticleDialog) {
                AddContentDialog(
                    onDismiss = { viewModel.closeAddArticleDialog() },
                    onOpenScanner = { viewModel.openScannerSheet() },
                    onAddArticle = { title, author, source, category, langCode, langName, content ->
                        viewModel.addNewArticle(title, author, source, category, langCode, langName, content)
                    }
                )
            }

            if (showAddNoteDialog) {
                AddNoteDialog(
                    targetLine = noteTargetLine,
                    articleTitle = selectedArticle?.title,
                    onDismiss = { viewModel.closeAddNoteDialog() },
                    onSaveNote = { text, tag, colorHex ->
                        viewModel.addNote(text, tag, colorHex)
                    }
                )
            }

            if (showWordLookupDialog && inspectedWord != null) {
                val isSavedInVocab = allVocabulary.any { it.word.equals(inspectedWord?.word, ignoreCase = true) }
                WordLookupDialog(
                    word = inspectedWord,
                    isSaved = isSavedInVocab,
                    isEnriching = isEnrichingWord,
                    onEnrichWithAi = { viewModel.enrichInspectedWordWithAi() },
                    onDismiss = { viewModel.closeWordLookupDialog() },
                    onPronounce = { viewModel.pronounceWord(it, inspectedWord?.languageCode) },
                    onSaveToVocabulary = { viewModel.saveInspectedWord() }
                )
            }

            if (showScannerSheet) {
                ScannerBottomSheet(
                    isScanning = isScanning,
                    extractedResult = extractedScanResult,
                    errorMessage = scanError,
                    onDismiss = { viewModel.closeScannerSheet() },
                    onScanBitmap = { bitmap -> viewModel.scanPageBitmap(bitmap) },
                    onUseSampleScan = { sample -> viewModel.useSampleScan(sample) },
                    onReadAloudNow = { scanResult -> viewModel.processExtractedScanAndOpen(scanResult, ScanAction.READ_ALOUD) },
                    onTranslateScan = { scanResult -> viewModel.processExtractedScanAndOpen(scanResult, ScanAction.TRANSLATE) },
                    onSummariseScan = { scanResult -> viewModel.processExtractedScanAndOpen(scanResult, ScanAction.SUMMARISE) },
                    onOpenInReader = { scanResult -> viewModel.processExtractedScanAndOpen(scanResult, ScanAction.OPEN) }
                )
            }

            if (showSummarySheet) {
                val summaryConfig by viewModel.summaryConfig.collectAsState()
                val summaryError by viewModel.summaryError.collectAsState()
                val isReadingSummary by viewModel.isReadingSummary.collectAsState()
                SummaryBottomSheet(
                    summary = summaryResult,
                    isLoading = isSummarizing,
                    errorMessage = summaryError,
                    config = summaryConfig,
                    isReadingSummary = isReadingSummary,
                    onConfigChange = { viewModel.updateSummaryConfig(it) },
                    onRegenerate = { viewModel.generateSummaryForCurrent() },
                    onRetryGemini = { viewModel.generateSummaryForCurrent() },
                    onUseLocalSummary = { viewModel.generateLocalSummaryOnly() },
                    onToggleReadSummary = {
                        if (isReadingSummary) viewModel.stopReadingSummary() else viewModel.readSummaryAloud()
                    },
                    onDismiss = { viewModel.closeSummarySheet() },
                    onSaveAsNote = { viewModel.saveSummaryAsNote() }
                )
            }

            if (showColorPicker) {
                LightColorPickerDialog(
                    currentOption = lightBackgroundOption,
                    onSelectOption = { viewModel.setLightBackgroundOption(it) },
                    onDismiss = { viewModel.closeColorPicker() }
                )
            }

            if (showVoiceSelectionDialog) {
                VoiceSelectionDialog(
                    availableVoices = availableVoices,
                    selectedVoice = currentVoice,
                    currentPitch = voicePitch,
                    currentSpeed = speechRate,
                    currentArticleLanguageCode = selectedArticle?.languageCode,
                    isPlayingPreview = isPlaying,
                    onSelectVoice = { viewModel.selectVoice(it) },
                    onPreviewVoice = { viewModel.previewVoice(it) },
                    onStopPreview = { viewModel.stopVoicePreview() },
                    onPitchChange = { viewModel.setVoicePitch(it) },
                    onSpeedChange = { viewModel.setSpeechSpeed(it) },
                    onDismiss = { viewModel.closeVoiceSelectionDialog() }
                )
            }
        }
    }
}
