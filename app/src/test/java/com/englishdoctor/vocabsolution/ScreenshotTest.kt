package com.englishdoctor.vocabsolution

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.PosChip
import com.englishdoctor.vocabsolution.ui.components.PosLegend
import com.englishdoctor.vocabsolution.ui.theme.VocabTheme
import org.junit.Rule
import org.junit.Test

/** Renders design-system pieces on the JVM to check Bengali shaping, colours and dark mode. */
class ScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Composable
    private fun Sample() {
        Surface {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Vocabsolution", style = MaterialTheme.typography.headlineMedium)
                Text("সবার জন্য একটি পূর্ণাঙ্গ শব্দভাণ্ডার সমাধান · মুখস্থ নয়, বুঝে শেখা", style = MaterialTheme.typography.titleMedium)
                PosLegend()
                ContentCard {
                    Text("Sagacious", style = MaterialTheme.typography.headlineSmall)
                    PosChip("adjective")
                    Text("বিচক্ষণ, প্রাজ্ঞ · ধ্বনি-সূত্র: সাগর-শেষ")
                    Text("ভোলার বৃদ্ধ মাঝি করিম চাচা সাগরের শুরু থেকে শেষ পর্যন্ত সব চেনেন। যুক্তাক্ষর: ক্ষ্ম, ন্ত্র, দ্ধ, স্থ।")
                }
            }
        }
    }

    @Test
    fun designSystemLight() = paparazzi.snapshot { VocabTheme(dark = false) { Sample() } }

    @Test
    fun designSystemDark() = paparazzi.snapshot { VocabTheme(dark = true) { Sample() } }
}
