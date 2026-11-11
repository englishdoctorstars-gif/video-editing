package com.englishdoctor.vocabsolution.ui.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.PosLegend
import com.englishdoctor.vocabsolution.ui.theme.BrandGold
import com.englishdoctor.vocabsolution.ui.theme.BrandNavy
import com.englishdoctor.vocabsolution.ui.theme.StageColors
import kotlinx.coroutines.launch

val GOALS = listOf(
    Triple("job", "🏛️ চাকরির প্রস্তুতি", "BCS, ব্যাংক, NTRCA, প্রাথমিক শিক্ষক নিয়োগ"),
    Triple("ielts", "🎓 IELTS", "Listening, Reading, Writing-এর শব্দভান্ডার"),
    Triple("foundation", "🧱 ভিত মজবুত", "শব্দ গঠন, Root ও দৈনন্দিন ইংরেজি"),
    Triple("general", "💬 আত্মবিশ্বাসী ইংরেজি", "পড়া, লেখা ও বলায় সঠিক শব্দ"),
)

@Composable
fun OnboardingScreen() {
    val app = LocalApp.current
    val pager = rememberPagerState { 4 }
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("general") }
    var daily by remember { mutableIntStateOf(10) }
    val notifyPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    var placement by remember { mutableStateOf(false) }

    if (placement) {
        // Last onboarding step: the optional placement test.
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
            PlacementTest(
                onFinished = { level, score ->
                    app.progress.recordPlacement(level, score)
                    app.progress.completeOnboarding(name, goal, daily)
                },
                onSkip = { app.progress.completeOnboarding(name, goal, daily) },
            )
        }
        return
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        HorizontalPager(pager, Modifier.weight(1f)) { page ->
            Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                when (page) {
                    0 -> Intro(
                        "Vocabsolution",
                        "সবার জন্য একটি পূর্ণাঙ্গ শব্দভাণ্ডার সমাধান",
                        "মুখস্থ নয়, বুঝে শেখা। শব্দকে ভেঙে তার গঠন বুঝুন, এক মূল থেকে পুরো শব্দ-পরিবার চিনুন, আর গল্পে গল্পে কঠিন শব্দ মনে রাখুন।",
                    ) { Text("by English Doctor", color = BrandGold, fontWeight = FontWeight.Bold) }
                    1 -> Intro(
                        "১-৩-৭-৩০",
                        "দিনের পুনরাবৃত্তি পদ্ধতি",
                        "১ম দিন শিখুন, ৩য় দিন যাচাই করুন, ৭ম দিন বাক্যে ব্যবহার করুন, ৩০তম দিনে ঝালিয়ে নিন। অ্যাপ নিজেই মনে করিয়ে দেবে কোন শব্দ কবে দেখতে হবে।",
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("১", "৩", "৭", "৩০").forEachIndexed { i, d ->
                                Box(Modifier.size(52.dp).clip(CircleShape).background(StageColors[i + 1]), contentAlignment = Alignment.Center) {
                                    Text(d, color = Color.White, style = MaterialTheme.typography.titleLarge)
                                }
                            }
                        }
                    }
                    2 -> Intro(
                        "রঙ ও সংকেত",
                        "এক নজরে পদ চিনুন",
                        "বইয়ের মতোই প্রতিটি শব্দের রঙ ও চিহ্ন তার পদ নির্দেশ করে। রঙের পাশাপাশি চিহ্নও থাকে, তাই সবাই সহজে চিনতে পারবেন।",
                    ) { PosLegend() }
                    else -> {
                        Text("আপনার লক্ষ্য কী?", style = MaterialTheme.typography.headlineSmall)
                        OutlinedTextField(name, { name = it }, label = { Text("আপনার নাম (ঐচ্ছিক)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        GOALS.forEach { (id, title, hint) ->
                            val selected = goal == id
                            ContentCard(accent = if (selected) MaterialTheme.colorScheme.primary else null, onClick = { goal = id }) {
                                Text(title, style = MaterialTheme.typography.titleMedium,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("প্রতিদিন কয়টি শব্দ?", style = MaterialTheme.typography.titleMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5 to "৫ মিনিট", 10 to "১০ মিনিট", 20 to "২০ মিনিট", 30 to "৩০ মিনিট").forEach { (n, label) ->
                                FilterChip(daily == n, { daily = n }, label = { Text("${n.bn()}টি\n$label", textAlign = TextAlign.Center) })
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i ->
                    Box(Modifier.size(if (pager.currentPage == i) 12.dp else 8.dp).clip(CircleShape)
                        .background(if (pager.currentPage == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant))
                }
            }
            Spacer(Modifier.weight(1f))
            if (pager.currentPage < 3) {
                TextButton({ scope.launch { pager.animateScrollToPage(3) } }) { Text("এড়িয়ে যান") }
                Button({ scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) { Text("পরবর্তী") }
            } else {
                Button({
                    if (Build.VERSION.SDK_INT >= 33) notifyPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    placement = true
                }) { Text("পরবর্তী") }
            }
        }
    }
}

@Composable
private fun Intro(big: String, title: String, body: String, extra: @Composable () -> Unit) {
    Spacer(Modifier.height(32.dp))
    Box(Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge).background(BrandNavy).padding(28.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(big, style = MaterialTheme.typography.displaySmall, color = Color.White)
            Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White.copy(alpha = 0.9f))
        }
    }
    Text(body, style = MaterialTheme.typography.bodyLarge)
    extra()
}
