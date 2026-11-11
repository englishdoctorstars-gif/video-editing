package com.englishdoctor.vocabsolution

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.compose.rememberNavController
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.englishdoctor.vocabsolution.data.Progress
import com.englishdoctor.vocabsolution.domain.Srs
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.exam.ExamHubScreen
import com.englishdoctor.vocabsolution.ui.home.HomeScreen
import com.englishdoctor.vocabsolution.ui.learn.CollocationsScreen
import com.englishdoctor.vocabsolution.ui.learn.LearnHubScreen
import com.englishdoctor.vocabsolution.ui.learn.ListenScreen
import com.englishdoctor.vocabsolution.ui.learn.ParaphraseTopicScreen
import com.englishdoctor.vocabsolution.ui.learn.PassageScreen
import com.englishdoctor.vocabsolution.ui.learn.ReadSubjectsScreen
import com.englishdoctor.vocabsolution.ui.learn.WriteScreen
import com.englishdoctor.vocabsolution.ui.learn.MemoryHookPagerScreen
import com.englishdoctor.vocabsolution.ui.learn.PairDetailScreen
import com.englishdoctor.vocabsolution.ui.learn.RootDetailScreen
import com.englishdoctor.vocabsolution.ui.learn.WordBuildingScreen
import com.englishdoctor.vocabsolution.ui.profile.ProfileScreen
import com.englishdoctor.vocabsolution.ui.review.ReviewHubScreen
import com.englishdoctor.vocabsolution.ui.theme.VocabTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import com.englishdoctor.vocabsolution.share.ShareCardRenderer
import com.englishdoctor.vocabsolution.share.toShareCard
import com.englishdoctor.vocabsolution.ui.onboarding.PlacementScreen
import com.englishdoctor.vocabsolution.ui.onboarding.buildPlacement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

/** Full screens with realistic progress, rendered on the JVM (no emulator needed). */
class ScreensScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5.copy(screenHeight = 2600), maxPercentDifference = 0.1)

    private fun shot(dark: Boolean = false, content: @Composable () -> Unit) {
        val app = AppContainer(paparazzi.context)
        val today = LocalDate.now().toEpochDay()
        val cards = app.content.studyItems.take(60).associate { it.key to Srs.learn(today - 2) } +
            app.content.studyItems.drop(60).take(25).associate { it.key to Srs.learn(today - 40).copy(stage = Srs.MASTERED, due = Long.MAX_VALUE) }
        app.progress.preview(
            Progress(onboarded = true, name = "রাফি", goal = "job", dailyGoal = 10, cards = cards, xp = 1240, streak = 6,
                bestStreak = 9, lastActiveDay = today, todayDay = today, todayCount = 7, examBest = mapOf(1 to 22, 2 to 18)),
        )
        paparazzi.snapshot {
            CompositionLocalProvider(LocalApp provides app) {
                VocabTheme(dark = dark) { Surface { content() } }
            }
        }
    }

    @Test fun home() = shot { HomeScreen(rememberNavController()) }
    @Test fun homeDark() = shot(dark = true) { HomeScreen(rememberNavController()) }
    @Test fun learnHub() = shot { LearnHubScreen(rememberNavController()) }
    @Test fun wordBuilding() = shot { WordBuildingScreen(rememberNavController()) }
    @Test fun rootFamily() = shot { RootDetailScreen(rememberNavController(), "root_01") }
    @Test fun memoryHook() = shot { MemoryHookPagerScreen(rememberNavController(), 2) }
    @Test fun confusedPair() = shot(dark = true) { PairDetailScreen(rememberNavController(), "pair_001") }
    @Test fun reviewHub() = shot { ReviewHubScreen(rememberNavController()) }
    @Test fun examHub() = shot { ExamHubScreen(rememberNavController()) }
    @Test fun profile() = shot { ProfileScreen(rememberNavController()) }
    @Test fun collocations() = shot { CollocationsScreen(rememberNavController()) }
    @Test fun listen() = shot { ListenScreen(rememberNavController()) }
    @Test fun readSubjects() = shot { ReadSubjectsScreen(rememberNavController()) }
    @Test fun passage() = shot { PassageScreen(rememberNavController(), "read_01_01") }
    @Test fun passageDark() = shot(dark = true) { PassageScreen(rememberNavController(), "read_05_02") }
    @Test fun paraphraseTopic() = shot { ParaphraseTopicScreen(rememberNavController(), 1) }
    @Test fun write() = shot { WriteScreen(rememberNavController()) }
    @Test fun placement() = shot { PlacementScreen(rememberNavController()) }

    @Test
    fun placementQuestionsAreValid() {
        val app = AppContainer(paparazzi.context)
        repeat(30) {
            val qs = buildPlacement(app.content)
            assertEquals(10, qs.size)
            assertEquals(listOf(3, 3, 4), (1..3).map { b -> qs.count { it.band == b } })
            qs.forEach { assertTrue(it.q.prompt, it.q.correct in it.q.options.indices && it.q.options.size >= 2) }
        }
    }

    @Test
    fun shareCardHook() {
        val app = AppContainer(paparazzi.context)
        val bmp = ShareCardRenderer.render(paparazzi.context, app.content.hooks[2].toShareCard())
        paparazzi.snapshot { Image(bmp.asImageBitmap(), null, Modifier.fillMaxWidth()) }
    }

    @Test
    fun shareCardPair() {
        val app = AppContainer(paparazzi.context)
        val bmp = ShareCardRenderer.render(paparazzi.context, app.content.pairs[0].toShareCard())
        paparazzi.snapshot { Image(bmp.asImageBitmap(), null, Modifier.fillMaxWidth()) }
    }
}
