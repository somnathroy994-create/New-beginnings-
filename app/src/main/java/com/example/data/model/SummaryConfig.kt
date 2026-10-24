package com.example.data.model

enum class SummaryLength(val label: String, val promptDesc: String) {
    SNAPSHOT("Snapshot", "2-3 concise, high-impact sentences capturing the core thesis"),
    BULLETS("Key Bullets", "4-5 actionable bullet points highlighting critical takeaways"),
    DEEP_DIVE("Deep Dive", "a thorough, analytical breakdown covering context, arguments, and conclusions"),
    CONCEPTS("Key Concepts", "explaining core terminology, definitions, and foundational concepts")
}

enum class SummaryTone(val label: String, val promptDesc: String) {
    BALANCED("Balanced", "clear, engaging, and objective"),
    ANALYTICAL("Analytical", "formal, scholarly, and structured"),
    SIMPLIFIED("Simple / ELI5", "plain language, accessible, free of jargon"),
    CREATIVE("Storyteller", "narrative-driven, vivid, and evocative")
}

enum class SummaryFocus(val label: String, val promptDesc: String) {
    OVERVIEW("All-Round", "a balanced overview of the entire material"),
    THEMES("Core Themes", "philosophical, thematic, and conceptual depth"),
    PLOT_FLOW("Narrative Flow", "chronological progression, turning points, and key events"),
    ACTIONABLE("Actionable Lessons", "practical wisdom, takeaways, and lessons learned")
}

data class SummaryConfig(
    val length: SummaryLength = SummaryLength.BULLETS,
    val tone: SummaryTone = SummaryTone.BALANCED,
    val focus: SummaryFocus = SummaryFocus.OVERVIEW,
    val targetLanguage: String = "Original"
) {
    companion object {
        val SUPPORTED_LANGUAGES = listOf(
            "Original",
            "English",
            "Spanish",
            "French",
            "German",
            "Hindi",
            "Japanese"
        )
    }
}
