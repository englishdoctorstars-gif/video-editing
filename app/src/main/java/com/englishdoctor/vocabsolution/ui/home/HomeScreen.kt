package com.englishdoctor.vocabsolution.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.IpaText
import com.englishdoctor.vocabsolution.ui.components.PosChip
import com.englishdoctor.vocabsolution.ui.components.ProgressRing
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.learn.partColor
import com.englishdoctor.vocabsolution.ui.openModule
import com.englishdoctor.vocabsolution.ui.review.newItemsFor
import com.englishdoctor.vocabsolution.ui.theme.BrandGold
import com.englishdoctor.vocabsolution.ui.theme.BrandNavy
import com.englishdoctor.vocabsolution.ui.theme.BrandNavyLight
import java.time.LocalTime

@Composable
fun HomeScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val today = app.progress.today()
    val due = p.dueKeys(today).size
    val done = p.todayDone(today)
    val streak = p.currentStreak(today)
    val wod = app.content.wordOfTheDay(today)
    val greeting = when (LocalTime.now().hour) {
        in 5..11 -> "শুভ সকাল"
        in 12..16 -> "শুভ দুপুর"
        in 17..19 -> "শুভ সন্ধ্যা"
        else -> "শুভ রাত্রি"
    }
    val newAvailable = newItemsFor(app, p.goal, p.cards.keys, 1).isNotEmpty()

    LazyColumn(
        Modifier.fillMaxWidth().statusBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("$greeting${if (p.name.isNotBlank()) ", ${p.name}" else ""}!", style = MaterialTheme.typography.headlineSmall)
                    Text("আজও একটু একটু করে এগিয়ে যাই।", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton({ nav.navigate(Routes.SEARCH) }) { Icon(Icons.Filled.Search, "খুঁজুন") }
            }
        }
        item {
            // Daily goal + streak hero card
            Box(
                Modifier.fillMaxWidth().clip(MaterialTheme.shapes.extraLarge)
                    .background(Brush.linearGradient(listOf(BrandNavy, BrandNavyLight))).padding(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(done.toFloat() / p.dailyGoal, BrandGold, size = 84.dp, stroke = 8.dp) {
                        Text("${done.coerceAtMost(p.dailyGoal).bn()}/${p.dailyGoal.bn()}", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.width(18.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("আজকের লক্ষ্য", color = Color.White.copy(alpha = 0.8f))
                        Text(if (done >= p.dailyGoal) "লক্ষ্য পূরণ! চমৎকার 🎉" else "আরও ${(p.dailyGoal - done).bn()}টি বাকি",
                            color = Color.White, style = MaterialTheme.typography.titleLarge)
                        Text("🔥 ${streak.bn()} দিনের Streak · ⭐ ${p.xp.bn()} XP", color = BrandGold, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ContentCard(Modifier.weight(1f), accent = MaterialTheme.colorScheme.tertiary, onClick = { nav.navigate(Routes.reviewSession("due")) }) {
                    Text("🔁 রিভিউ", style = MaterialTheme.typography.titleMedium)
                    Text(if (due > 0) "${due.bn()}টি শব্দ অপেক্ষায়" else "আজ কোনো রিভিউ নেই",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                ContentCard(Modifier.weight(1f), accent = MaterialTheme.colorScheme.secondary,
                    onClick = { if (newAvailable) nav.navigate(Routes.reviewSession("new")) }) {
                    Text("✨ নতুন পাঠ", style = MaterialTheme.typography.titleMedium)
                    Text(if (newAvailable) "${p.dailyGoal.bn()}টি নতুন শব্দ" else "সব শব্দ শেখা হয়েছে!",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            ContentCard(onClick = { nav.navigate(Routes.hook(wod.order - 1)) }) {
                Text("আজকের শব্দ · Word of the Day", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(wod.word, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                    SpeakButton({ app.speaker.speak(wod.word) })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PosChip(wod.pos)
                    IpaText(wod.ipa)
                }
                Text(wod.meaningBn, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
                Text("ধ্বনি-সূত্র: ${wod.soundHookBn}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text("গল্পটি পড়ুন", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        item { Text("দ্রুত শুরু করুন", style = MaterialTheme.typography.titleMedium) }
        item {
            val quick = when (p.goal) {
                "job" -> listOf(ModuleId.EXAM, ModuleId.EQUIVALENTS, ModuleId.CONFUSED, ModuleId.WORD_BUILDING)
                "ielts" -> listOf(ModuleId.READ, ModuleId.PARAPHRASE, ModuleId.LISTEN, ModuleId.WRITE)
                "foundation" -> listOf(ModuleId.WORD_BUILDING, ModuleId.ROOT_POWER, ModuleId.MEMORY_HOOKS, ModuleId.EQUIVALENTS)
                else -> listOf(ModuleId.MEMORY_HOOKS, ModuleId.COLLOCATIONS, ModuleId.CONFUSED, ModuleId.EQUIVALENTS)
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                quick.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        row.forEach { m ->
                            ContentCard(Modifier.weight(1f), accent = partColor(m.part), onClick = { nav.openModule(m) }) {
                                Text("অধ্যায় ${m.number.bn()}", style = MaterialTheme.typography.labelSmall, color = partColor(m.part))
                                Text(m.titleEn, style = MaterialTheme.typography.titleSmall, maxLines = 2, minLines = 2)
                                Text(m.titleBn, style = MaterialTheme.typography.bodySmall, maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        item {
            Button({ nav.navigate(Routes.LEARN) }, Modifier.fillMaxWidth()) { Text("সব অধ্যায় দেখুন") }
        }
    }
}
