package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Article
import com.example.data.translation.TranslationResult
import com.example.data.translation.TranslatorEngine

@Composable
fun TranslationScreen(
    currentArticle: Article?,
    onPronounceText: (String, String) -> Unit,
    onSaveTranslationAsNote: (String, String) -> Unit,
    onOpenInReader: ((String, String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var sourceText by remember(currentArticle) {
        mutableStateOf(currentArticle?.content ?: "Physicists around the globe are celebrating an extraordinary breakthrough in magnetic confinement fusion. Yesterday morning, the Stellar Energy Laboratory sustained a burning plasma reaction at over one hundred million degrees Celsius for eight continuous minutes. This historic milestone brings humankind significantly closer to an inexhaustible source of clean, zero-carbon electricity.")
    }
    var promptQuery by remember { mutableStateOf("Translate to Bengali") }
    var translationResult by remember {
        mutableStateOf<TranslationResult?>(
            TranslatorEngine.translateArticleContent(
                Article(
                    id = "temp_preview",
                    title = currentArticle?.title ?: "Sample Article",
                    content = sourceText,
                    languageCode = "en",
                    languageName = "English"
                ),
                targetLangCode = "bn",
                promptInstruction = "Translate to Bengali"
            )
        )
    }

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("translation_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Prompt Translation",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Translate like Hindi, Bengali, Tamil, Chinese by prompt",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }

        // Quick Prompt Suggestions Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Pair("বাংলা Bengali", "Translate to Bengali"),
                Pair("हिन्दी Hindi", "Translate to Hindi"),
                Pair("தமிழ் Tamil", "Translate to Tamil"),
                Pair("中文 Chinese", "Translate to Chinese"),
                Pair("English", "Translate to English"),
                Pair("Español", "Translate to Spanish")
            ).forEach { (label, prompt) ->
                val isSelected = promptQuery.equals(prompt, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clickable {
                            promptQuery = prompt
                            val (targetCode, _) = TranslatorEngine.resolveTargetLanguage(prompt)
                            translationResult = TranslatorEngine.translateArticleContent(
                                Article(
                                    id = "temp_art",
                                    title = "Translation",
                                    content = sourceText,
                                    languageCode = currentArticle?.languageCode ?: "en"
                                ),
                                targetLangCode = targetCode,
                                promptInstruction = prompt
                            )
                        }
                        .testTag("quick_prompt_$label")
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Prompt Input Field with Submit Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = promptQuery,
                onValueChange = { promptQuery = it },
                label = { Text("Translation Prompt") },
                placeholder = { Text("e.g. Translate to Hindi, Bengali, Tamil, Chinese...") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("translation_prompt_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val (targetCode, _) = TranslatorEngine.resolveTargetLanguage(promptQuery)
                    translationResult = TranslatorEngine.translateArticleContent(
                        Article(
                            id = "temp_art",
                            title = "Translation",
                            content = sourceText,
                            languageCode = currentArticle?.languageCode ?: "en"
                        ),
                        targetLangCode = targetCode,
                        promptInstruction = promptQuery
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
                modifier = Modifier.testTag("submit_translate_prompt_btn")
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = "Translate")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Translation Result Area
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val result = translationResult
            if (result != null) {
                // Header badge of languages
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${result.sourceLanguage} ➔ ${result.targetLanguage}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Pronounce entire translated text
                                FilledIconButton(
                                    onClick = { onPronounceText(result.translatedText, result.targetLanguageCode) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Read translated aloud",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                // Copy
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(result.translatedText))
                                        Toast.makeText(context, "Translation copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                // Save Note
                                IconButton(
                                    onClick = {
                                        onSaveTranslationAsNote(result.originalText.take(80), result.translatedText)
                                        Toast.makeText(context, "Saved translation to Notes", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = "Save Note", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Line-by-line Comparative Cards
                items(result.lineByLine) { (origLine, transLine) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Original sentence
                            Text(
                                text = origLine,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Divider with language indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = result.targetLanguage,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                FilledIconButton(
                                    onClick = { onPronounceText(transLine, result.targetLanguageCode) },
                                    modifier = Modifier.size(28.dp),
                                    shape = CircleShape
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = "Read sentence aloud",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Translated sentence
                            Text(
                                text = transLine,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    lineHeight = 24.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
