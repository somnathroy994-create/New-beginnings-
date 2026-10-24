package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Warm Editorial Reader Palette
val DeepSlate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Slate700 = Color(0xFF334155)
val Slate600 = Color(0xFF475569)
val Slate400 = Color(0xFF94A3B8)
val Slate200 = Color(0xFFE2E8F0)
val Slate100 = Color(0xFFF1F5F9)
val Slate50 = Color(0xFFF8FAFC)

// Warm Parchment & Reading Paper Backgrounds
val ParchmentLight = Color(0xFFF8FAFC)
val ParchmentSurface = Color(0xFFFFFFFF)
val ParchmentCard = Color(0xFFF1F5F9)

// Brand Accents
val AmberGold = Color(0xFFD97706)
val AmberLight = Color(0xFFFDE68A)
val CyanAccent = Color(0xFF0284C7)
val EmeraldGreen = Color(0xFF10B981)
val RoseRed = Color(0xFFF43F5E)
val VioletPurple = Color(0xFF8B5CF6)

// Active reading highlight colors
val HighlightSpeechDark = Color(0x3338BDF8)
val HighlightSpeechLight = Color(0x33F59E0B)
val HighlightWord = Color(0x55FBBF24)

/**
 * Curated light background color options for comfortable reading.
 * Allows users to customize the light reading theme to their visual preference.
 */
enum class LightBackgroundOption(
    val id: String,
    val title: String,
    val description: String,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val surfaceVariantColor: Color,
    val onBackgroundColor: Color,
    val previewColor: Color
) {
    CRISP_WHITE(
        id = "crisp_white",
        title = "Crisp Clean",
        description = "Modern pure slate contrast",
        backgroundColor = Color(0xFFF8FAFC),
        surfaceColor = Color(0xFFFFFFFF),
        surfaceVariantColor = Color(0xFFF1F5F9),
        onBackgroundColor = Color(0xFF0F172A),
        previewColor = Color(0xFFF8FAFC)
    ),
    WARM_CREAM(
        id = "warm_cream",
        title = "Warm Cream",
        description = "Soft warm paper for cozy reading",
        backgroundColor = Color(0xFFFBF8F2),
        surfaceColor = Color(0xFFFFFDF9),
        surfaceVariantColor = Color(0xFFF2ECE1),
        onBackgroundColor = Color(0xFF292524),
        previewColor = Color(0xFFFBF8F2)
    ),
    CALMING_MINT(
        id = "calming_mint",
        title = "Calming Mint",
        description = "Gentle green to ease eye strain",
        backgroundColor = Color(0xFFF0F7F4),
        surfaceColor = Color(0xFFF9FDFB),
        surfaceVariantColor = Color(0xFFE1EFE7),
        onBackgroundColor = Color(0xFF132A1C),
        previewColor = Color(0xFFE8F5E9)
    ),
    PEACH_SUNSET(
        id = "peach_sunset",
        title = "Soft Peach",
        description = "Warm gentle glow for relaxing",
        backgroundColor = Color(0xFFFFF6F2),
        surfaceColor = Color(0xFFFFFBF9),
        surfaceVariantColor = Color(0xFFFCE7E0),
        onBackgroundColor = Color(0xFF331D18),
        previewColor = Color(0xFFFFECE5)
    ),
    VINTAGE_SEPIA(
        id = "vintage_sepia",
        title = "Classic Sepia",
        description = "Timeless antique novel aesthetic",
        backgroundColor = Color(0xFFF5EDE0),
        surfaceColor = Color(0xFFFAF4E9),
        surfaceVariantColor = Color(0xFFEADECA),
        onBackgroundColor = Color(0xFF342617),
        previewColor = Color(0xFFF5EDE0)
    ),
    NORDIC_MIST(
        id = "nordic_mist",
        title = "Nordic Mist",
        description = "Cool soothing blue-gray tone",
        backgroundColor = Color(0xFFF0F4F8),
        surfaceColor = Color(0xFFF8FAFD),
        surfaceVariantColor = Color(0xFFDFE7F0),
        onBackgroundColor = Color(0xFF1E293B),
        previewColor = Color(0xFFE3EDF7)
    );

    companion object {
        fun fromId(id: String?): LightBackgroundOption {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CRISP_WHITE
        }
    }
}
