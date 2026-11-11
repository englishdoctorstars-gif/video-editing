package com.englishdoctor.vocabsolution.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.exam.ExamHubScreen
import com.englishdoctor.vocabsolution.ui.home.HomeScreen
import com.englishdoctor.vocabsolution.ui.learn.LearnHubScreen
import com.englishdoctor.vocabsolution.ui.learn.ListenScreen
import com.englishdoctor.vocabsolution.ui.learn.MemoryHookPagerScreen
import com.englishdoctor.vocabsolution.ui.learn.PairDetailScreen
import com.englishdoctor.vocabsolution.ui.learn.PassageScreen
import com.englishdoctor.vocabsolution.ui.review.ReviewHubScreen
import com.englishdoctor.vocabsolution.ui.theme.BrandGold
import com.englishdoctor.vocabsolution.ui.theme.BrandNavy
import com.englishdoctor.vocabsolution.ui.theme.BrandNavyLight
import com.englishdoctor.vocabsolution.ui.theme.HindSiliguri
import com.englishdoctor.vocabsolution.ui.theme.VocabTheme
import org.junit.Rule
import org.junit.Test

private data class Shot(val name: String, val bn: String, val en: String, val dark: Boolean = false, val screen: @Composable () -> Unit)

private val SHOTS = listOf(
    Shot("1_home", "প্রতিদিনের লক্ষ্য, Streak ও আজকের শব্দ", "Daily goal, streak and Word of the Day") { HomeScreen(rememberNavController()) },
    Shot("2_chapters", "বইয়ের ১১টি অধ্যায়, ৪টি পর্ব — এক অ্যাপে", "All 11 chapters of the book in one app") { LearnHubScreen(rememberNavController()) },
    Shot("3_memory_hooks", "গল্পে গল্পে কঠিন শব্দ মনে রাখুন", "Remember hard words through Bengali stories") { MemoryHookPagerScreen(rememberNavController(), 2) },
    Shot("4_read_smart", "প্রসঙ্গের ভেতরে শব্দ শিখুন · IELTS Reading", "Learn words in context for IELTS Reading") { PassageScreen(rememberNavController(), "read_01_01") },
    Shot("5_confused_words", "কাছাকাছি শব্দের পার্থক্য এক নজরে", "Commonly confused words, side by side", dark = true) { PairDetailScreen(rememberNavController(), "pair_001") },
    Shot("6_listen_smart", "শুনে শুনে সঠিক বানান · IELTS Listening", "Listen, spell and beat spelling traps") { ListenScreen(rememberNavController()) },
    Shot("7_exam_ready", "চাকরির পরীক্ষার ২৫০টি MCQ, ২০ মিনিটের টাইমার", "250 job-exam MCQs with a 20-minute timer") { ExamHubScreen(rememberNavController()) },
    Shot("8_review", "১-৩-৭-৩০ পদ্ধতিতে স্থায়ী শব্দভান্ডার", "The 1-3-7-30 method makes words stick") { ReviewHubScreen(rememberNavController()) },
)

/** Phone screenshots, 1080×1920 (9:16), each with a caption above the real screen. */
class StoreScreenshotsTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1920, xdpi = 440, ydpi = 440, density = Density.XXHIGH),
        useDeviceResolution = true,
    )

    private fun render(shot: Shot, english: Boolean) {
        val app = storeApp(paparazzi.context)
        paparazzi.snapshot(name = (if (english) "en_" else "bn_") + shot.name) {
            CompositionLocalProvider(LocalApp provides app) {
                Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BrandNavy, BrandNavyLight)))) {
                    Text(
                        if (english) shot.en else shot.bn,
                        style = TextStyle(fontFamily = HindSiliguri, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color.White,
                            textAlign = TextAlign.Center, lineHeight = 32.sp),
                        modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 6.dp),
                    )
                    Text("Vocabsolution · by English Doctor",
                        style = TextStyle(fontFamily = HindSiliguri, fontSize = 13.sp, color = BrandGold, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                    Box(Modifier.padding(horizontal = 18.dp).clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)), contentAlignment = Alignment.TopCenter) {
                        VocabTheme(dark = shot.dark) { Surface { shot.screen() } }
                    }
                }
            }
        }
    }

    @Test fun bengali() = SHOTS.forEach { render(it, english = false) }
    @Test fun english() = SHOTS.forEach { render(it, english = true) }
}
