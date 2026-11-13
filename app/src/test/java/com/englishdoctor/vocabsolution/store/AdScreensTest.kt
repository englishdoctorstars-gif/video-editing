package com.englishdoctor.vocabsolution.store

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.navigation.compose.rememberNavController
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.englishdoctor.vocabsolution.share.ShareCardRenderer
import com.englishdoctor.vocabsolution.share.toShareCard
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.exam.ExamHubScreen
import com.englishdoctor.vocabsolution.ui.home.HomeScreen
import com.englishdoctor.vocabsolution.ui.learn.CollocationsScreen
import com.englishdoctor.vocabsolution.ui.learn.LearnHubScreen
import com.englishdoctor.vocabsolution.ui.learn.ListenScreen
import com.englishdoctor.vocabsolution.ui.learn.MemoryHookPagerScreen
import com.englishdoctor.vocabsolution.ui.learn.PairDetailScreen
import com.englishdoctor.vocabsolution.ui.learn.ParaphraseTopicScreen
import com.englishdoctor.vocabsolution.ui.learn.PassageScreen
import com.englishdoctor.vocabsolution.ui.learn.RootDetailScreen
import com.englishdoctor.vocabsolution.ui.learn.WordBuildingScreen
import com.englishdoctor.vocabsolution.ui.learn.WriteScreen
import com.englishdoctor.vocabsolution.ui.onboarding.PlacementScreen
import com.englishdoctor.vocabsolution.ui.profile.ProfileScreen
import com.englishdoctor.vocabsolution.ui.review.ReviewSessionScreen
import com.englishdoctor.vocabsolution.ui.theme.VocabTheme
import org.junit.Rule
import org.junit.Test

/**
 * Raw app screens (1080×2400, a common phone size) for compositing into AI-generated
 * ad footage. Copied into marketing/ad-screens/ by tools/release/collect_ad_screens.py.
 */
class AdScreensTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 2400, xdpi = 440, ydpi = 440, density = Density.XXHIGH),
        useDeviceResolution = true,
    )

    private fun shot(name: String, dark: Boolean = false, content: @Composable () -> Unit) {
        val app = storeApp(paparazzi.context)
        paparazzi.snapshot(name = name) {
            CompositionLocalProvider(LocalApp provides app) { VocabTheme(dark = dark) { Surface { content() } } }
        }
    }

    @Test fun s01Home() = shot("01_home") { HomeScreen(rememberNavController()) }
    @Test fun s02HookSabbaticalFront() = shot("02_hook_sabbatical_front") { MemoryHookPagerScreen(rememberNavController(), 0) }
    @Test fun s03HookSabbaticalStory() = shot("03_hook_sabbatical_story") { MemoryHookPagerScreen(rememberNavController(), 0, startFlipped = true) }
    @Test fun s04HookSagaciousFront() = shot("04_hook_sagacious_front") { MemoryHookPagerScreen(rememberNavController(), 2) }
    @Test fun s05HookSagaciousStory() = shot("05_hook_sagacious_story") { MemoryHookPagerScreen(rememberNavController(), 2, startFlipped = true) }
    @Test fun s06WordBreaker() = shot("06_word_breaker") { WordBuildingScreen(rememberNavController(), initialTab = 2, revealAll = true) }
    @Test fun s07RootFamily() = shot("07_root_family_act") { RootDetailScreen(rememberNavController(), "root_01") }
    @Test fun s08ConfusedPair() = shot("08_confused_affect_effect") { PairDetailScreen(rememberNavController(), "pair_001") }
    @Test fun s09Mistakes() = shot("09_common_mistakes") { CollocationsScreen(rememberNavController(), initialTab = 2) }
    @Test fun s10ReadSmart() = shot("10_read_smart_passage") { PassageScreen(rememberNavController(), "read_01_01") }
    @Test fun s11Paraphrase() = shot("11_paraphrase_bank") { ParaphraseTopicScreen(rememberNavController(), 1) }
    @Test fun s12ListenSpell() = shot("12_listen_and_spell") { ListenScreen(rememberNavController(), initialTab = 1) }
    @Test fun s13GraphQuiz() = shot("13_write_graph_quiz") { WriteScreen(rememberNavController(), initialTab = 1) }
    @Test fun s14ExamHub() = shot("14_exam_ready") { ExamHubScreen(rememberNavController()) }
    @Test fun s15Review() = shot("15_review_flashcard") { ReviewSessionScreen(rememberNavController(), "due") }
    @Test fun s16Placement() = shot("16_placement_test") { PlacementScreen(rememberNavController()) }
    @Test fun s17Chapters() = shot("17_all_chapters") { LearnHubScreen(rememberNavController()) }
    @Test fun s18Profile() = shot("18_progress_badges") { ProfileScreen(rememberNavController()) }
    @Test fun s19HomeDark() = shot("19_home_dark", dark = true) { HomeScreen(rememberNavController()) }

    @Test
    fun s20ShareCard() {
        val app = storeApp(paparazzi.context)
        val bmp = ShareCardRenderer.render(paparazzi.context, app.content.hooks[0].toShareCard())
        paparazzi.snapshot(name = "20_share_card_sabbatical") { Image(bmp.asImageBitmap(), null, Modifier.fillMaxWidth()) }
    }
}
