package com.englishdoctor.vocabsolution.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.BuildConfig
import com.englishdoctor.vocabsolution.domain.Srs
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.EmptyState
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SectionTitle
import com.englishdoctor.vocabsolution.ui.components.TagPill
import com.englishdoctor.vocabsolution.ui.onboarding.GOALS
import com.englishdoctor.vocabsolution.ui.review.ItemRow
import com.englishdoctor.vocabsolution.ui.theme.BrandGold
import com.englishdoctor.vocabsolution.ui.theme.BrandNavy
import com.englishdoctor.vocabsolution.ui.theme.StageColors

private data class Badge(val emoji: String, val title: String, val earned: Boolean)

@Composable
fun ProfileScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val today = app.progress.today()
    val counts = p.stageCounts()
    val learned = p.cards.size
    val hooksDone = p.cards.keys.count { it.startsWith("hook:") }
    val pairsDone = p.cards.keys.count { it.startsWith("pair:") }
    val badges = listOf(
        Badge("🌱", "প্রথম শব্দ", learned >= 1),
        Badge("📚", "১০০ শব্দ", learned >= 100),
        Badge("🧠", "Memory Master", hooksDone >= 100),
        Badge("⚖️", "Confusion Killer", pairsDone >= 100),
        Badge("🔥", "৭ দিনের Streak", p.bestStreak >= 7),
        Badge("🏅", "৩০ দিনের Streak", p.bestStreak >= 30),
        Badge("🏛️", "BCS Ready", (8..10).any { (p.examBest[it] ?: 0) > 20 }),
        Badge("💎", "১০টি স্থায়ী শব্দ", counts[Srs.MASTERED] >= 10),
    )
    val goal = GOALS.firstOrNull { it.first == p.goal }?.second ?: ""

    ScreenScaffold("আমি", goal) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Box(Modifier.fillMaxWidth().background(BrandNavy, MaterialTheme.shapes.extraLarge).padding(20.dp)) {
                    Column {
                        Text(p.name.ifBlank { "শিক্ষার্থী" }, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                        Text("⭐ ${p.xp.bn()} XP · লেভেল ${(1 + p.xp / 500).bn()}", color = BrandGold, fontWeight = FontWeight.SemiBold)
                        Text("🔥 বর্তমান Streak ${p.currentStreak(today).bn()} দিন · সর্বোচ্চ ${p.bestStreak.bn()} দিন", color = Color.White)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Stat("শেখা শব্দ", learned.bn(), Modifier.weight(1f))
                    Stat("স্থায়ী", counts[Srs.MASTERED].bn(), Modifier.weight(1f))
                    Stat("পরীক্ষা", p.examHistory.size.bn(), Modifier.weight(1f))
                }
            }
            item { SectionTitle("১-৩-৭-৩০ অগ্রগতি") }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..Srs.MASTERED).forEach { s -> TagPill("${Srs.STAGE_NAMES[s]}: ${counts[s].bn()}", StageColors[s]) }
                }
            }
            item { SectionTitle("ব্যাজ") }
            item {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    badges.forEach { b ->
                        ContentCard(Modifier.fillMaxWidth(0.31f), accent = if (b.earned) BrandGold else null) {
                            Text(if (b.earned) b.emoji else "🔒", style = MaterialTheme.typography.headlineSmall)
                            Text(b.title, style = MaterialTheme.typography.labelMedium,
                                color = if (b.earned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                ContentCard {
                    MenuRow(Icons.Filled.Bookmark, "বুকমার্ক") { nav.navigate(Routes.BOOKMARKS) }
                    MenuRow(Icons.Filled.Settings, "সেটিংস") { nav.navigate(Routes.SETTINGS) }
                    MenuRow(Icons.Filled.Info, "অ্যাপ ও লেখক পরিচিতি") { nav.navigate(Routes.ABOUT) }
                }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier) {
    ContentCard(modifier) {
        Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(text) },
        leadingContent = { Icon(icon, null) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, null) },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    )
}

@Composable
fun BookmarksScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val items = p.bookmarks.mapNotNull { app.content.item(it) }
    ScreenScaffold("বুকমার্ক", "${items.size.bn()}টি শব্দ", onBack = { nav.popBackStack() }) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (items.isEmpty()) {
                item { EmptyState("🔖", "কোনো বুকমার্ক নেই", "Memory Hook বা Confused Words-এ 🔖 চাপলে এখানে জমা হবে।") }
            } else {
                item { Button({ nav.navigate(Routes.reviewSession("bookmarks")) }, Modifier.fillMaxWidth()) { Text("ফ্ল্যাশকার্ডে অনুশীলন করুন") } }
                items(items, key = { it.key }) { ItemRow(it) }
            }
        }
    }
}

@Composable
fun SettingsScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val st = p.settings
    var name by remember { mutableStateOf(p.name) }
    var confirmReset by remember { mutableStateOf(false) }
    ScreenScaffold("সেটিংস", onBack = { nav.popBackStack() }) { m ->
        Column(m.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionTitle("প্রোফাইল")
            OutlinedTextField(name, { name = it; app.progress.updateProfile(it, p.goal, p.dailyGoal) }, label = { Text("নাম") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("লক্ষ্য")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GOALS.forEach { (id, title, _) -> FilterChip(p.goal == id, { app.progress.updateProfile(p.name, id, p.dailyGoal) }, label = { Text(title) }) }
            }
            Text("দৈনিক লক্ষ্য")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5, 10, 20, 30).forEach { n -> FilterChip(p.dailyGoal == n, { app.progress.updateProfile(p.name, p.goal, n) }, label = { Text("${n.bn()}টি") }) }
            }
            SectionTitle("দেখার ধরন")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("system" to "সিস্টেম", "light" to "লাইট ☀️", "dark" to "ডার্ক 🌙").forEach { (k, label) ->
                    FilterChip(st.theme == k, { app.progress.updateSettings { it.copy(theme = k) } }, label = { Text(label) })
                }
            }
            Text("লেখার আকার: ${(st.fontScale * 100).toInt().bn()}%")
            Slider(st.fontScale, { v -> app.progress.updateSettings { it.copy(fontScale = v) } }, valueRange = 0.85f..1.4f, steps = 10)
            SectionTitle("উচ্চারণ")
            Text("কথার গতি: ${(st.ttsRate * 100).toInt().bn()}%")
            Slider(st.ttsRate, { v -> app.progress.updateSettings { it.copy(ttsRate = v) } }, valueRange = 0.5f..1.3f, steps = 7)
            OutlinedButton({ app.speaker.speak("Vocabsolution makes vocabulary simple.") }) { Text("🔊 পরীক্ষা করুন") }
            SectionTitle("রিমাইন্ডার")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("দৈনিক রিমাইন্ডার", Modifier.weight(1f))
                Switch(st.reminderEnabled, { v -> app.progress.updateSettings { it.copy(reminderEnabled = v) } })
            }
            if (st.reminderEnabled) {
                Text("সময়: ${hourBn(st.reminderHour)}")
                Slider(st.reminderHour.toFloat(), { v -> app.progress.updateSettings { it.copy(reminderHour = v.toInt()) } },
                    valueRange = 6f..23f, steps = 16)
            }
            SectionTitle("ডেটা")
            OutlinedButton({ confirmReset = true }, Modifier.fillMaxWidth()) { Text("সব অগ্রগতি মুছে ফেলুন") }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("নিশ্চিত?") },
            text = { Text("শেখা শব্দ, রিভিউ, বুকমার্ক ও পরীক্ষার ফলাফল মুছে যাবে। এটি ফেরানো যাবে না।") },
            confirmButton = { Button({ app.progress.resetAll(); confirmReset = false }) { Text("মুছে ফেলুন") } },
            dismissButton = { TextButton({ confirmReset = false }) { Text("বাতিল") } },
        )
    }
}

private fun hourBn(h: Int): String {
    val period = when (h) { in 5..11 -> "সকাল"; in 12..15 -> "দুপুর"; in 16..17 -> "বিকেল"; in 18..19 -> "সন্ধ্যা"; else -> "রাত" }
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "$period ${h12.bn()}টা"
}

@Composable
fun AboutScreen(nav: NavHostController) {
    val ctx = LocalContext.current
    fun open(uri: String) = runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri))) }
    ScreenScaffold("অ্যাপ পরিচিতি", onBack = { nav.popBackStack() }) { m ->
        Column(m.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.fillMaxWidth().background(BrandNavy, MaterialTheme.shapes.extraLarge).padding(24.dp)) {
                Column {
                    Text("Vocabsolution", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Text("by English Doctor", color = BrandGold, fontWeight = FontWeight.Bold)
                    Text("সবার জন্য একটি পূর্ণাঙ্গ শব্দভাণ্ডার সমাধান", color = Color.White)
                    Text("সংস্করণ ${BuildConfig.VERSION_NAME}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
            ContentCard {
                Text("Developed & Authored by", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Text("Shahjalal Sultanate (English Doctor)", style = MaterialTheme.typography.titleLarge)
                Text("শাহজালাল সালতানাত (ইংলিশ ডক্টর)", style = MaterialTheme.typography.titleMedium)
                Text(
                    "ঢাকা বিশ্ববিদ্যালয় থেকে ইংরেজিতে স্নাতকোত্তর; English Doctor-এর প্রতিষ্ঠাতা। English Coach, Senior Faculty, " +
                        "Corporate Trainer, Teacher Trainer ও Head of R&D, এবং একজন Gemini Certified Educator। Cambridge ও Myanmar " +
                        "Curriculum-এ শিক্ষকতা এবং BRAC Education Program-এ Teacher Training-এর অভিজ্ঞতা রয়েছে।",
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            ContentCard {
                Text("English Doctor-এর লক্ষ্য", style = MaterialTheme.typography.titleMedium)
                Text("• ইংরেজি বলার ভয় দূর করা\n• মুখস্থ নয়, বাস্তব জীবনে ইংরেজির ব্যবহার শেখানো")
            }
            ContentCard {
                Text("যোগাযোগ", style = MaterialTheme.typography.titleMedium)
                TextButton({ open("tel:01682888114") }) { Text("📞 ০১৬৮২ ৮৮৮ ১১৪") }
                TextButton({ open("mailto:englishdoctorbd@gmail.com") }) { Text("✉️ englishdoctorbd@gmail.com") }
                Text("Facebook, LinkedIn, TikTok, Instagram ও YouTube-এ শাহজালাল সালতানাত (ইংলিশ ডক্টর)",
                    style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "© English Doctor. সর্বস্বত্ব সংরক্ষিত। লেখকের লিখিত অনুমতি ছাড়া এই অ্যাপ বা বইয়ের কোনো অংশ কপি, পুনর্মুদ্রণ বা বিতরণ করা যাবে না।\n" +
                    "ফন্ট: Hind Siliguri (SIL Open Font License)।",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
