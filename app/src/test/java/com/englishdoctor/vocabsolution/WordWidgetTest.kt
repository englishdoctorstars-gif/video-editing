package com.englishdoctor.vocabsolution

import androidx.glance.appwidget.testing.unit.runGlanceAppWidgetUnitTest
import androidx.glance.testing.unit.hasText
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.englishdoctor.vocabsolution.data.MemoryHook
import com.englishdoctor.vocabsolution.widget.WidgetBody
import org.junit.Test

class WordWidgetTest {
    private val hook = MemoryHook(
        id = "hook_003", order = 3, word = "Sagacious", ipa = "/səˈɡeɪʃəs/", bnPronunciation = "স্যাগেইশাস",
        pos = "adjective", meaningBn = "বিচক্ষণ, প্রাজ্ঞ", meaningEn = "wise", soundSplit = "Saga-cious",
        soundHookBn = "সাগর-শেষ", storyBn = "", keyCueBn = "", exampleEn = "", exampleBn = "", synonyms = emptyList(),
    )

    @Test
    fun showsWordMeaningAndHook() = runGlanceAppWidgetUnitTest {
        setAppWidgetSize(DpSize(250.dp, 110.dp))
        provideComposable { WidgetBody(hook, onClick = null) }
        onNode(hasText("আজকের শব্দ")).assertExists()
        onNode(hasText("Sagacious")).assertExists()
        onNode(hasText("বিচক্ষণ, প্রাজ্ঞ")).assertExists()
        onNode(hasText("ধ্বনি-সূত্র: সাগর-শেষ")).assertExists()
    }
}
