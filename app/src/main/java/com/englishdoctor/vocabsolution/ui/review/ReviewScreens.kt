package com.englishdoctor.vocabsolution.ui.review

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.AppContainer
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.data.StudyItem
import com.englishdoctor.vocabsolution.domain.Srs
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.EmptyState
import com.englishdoctor.vocabsolution.ui.components.FlipCard
import com.englishdoctor.vocabsolution.ui.components.PosChip
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SectionTitle
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.components.TagPill
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.HomophoneAmber
import com.englishdoctor.vocabsolution.ui.theme.StageColors
import com.englishdoctor.vocabsolution.ui.theme.WrongRed

/** Which modules feed "নতুন পাঠ" first, per learner goal. Root words come last (there are 2,694). */
private fun moduleOrder(goal: String) = when (goal) {
    "job" -> listOf(ModuleId.EQUIVALENTS, ModuleId.CONFUSED, ModuleId.MEMORY_HOOKS, ModuleId.WORD_BUILDING, ModuleId.ROOT_POWER)
    "ielts" -> listOf(ModuleId.MEMORY_HOOKS, ModuleId.CONFUSED, ModuleId.WORD_BUILDING, ModuleId.EQUIVALENTS, ModuleId.ROOT_POWER)
    "foundation" -> listOf(ModuleId.WORD_BUILDING, ModuleId.MEMORY_HOOKS, ModuleId.ROOT_POWER, ModuleId.CONFUSED, ModuleId.EQUIVALENTS)
    else -> listOf(ModuleId.MEMORY_HOOKS, ModuleId.CONFUSED, ModuleId.EQUIVALENTS, ModuleId.WORD_BUILDING, ModuleId.ROOT_POWER)
}

/** New cards for today's lesson, interleaving modules so a lesson mixes chapters. */
fun newItemsFor(app: AppContainer, goal: String, learned: Set<String>, count: Int): List<StudyItem> {
    val byModule = app.content.studyItems.filter { it.key !in learned }.groupBy { it.module }
    val queues = moduleOrder(goal).mapNotNull { byModule[it]?.toMutableList() }.filter { it.isNotEmpty() }
    val out = mutableListOf<StudyItem>()
    var i = 0
    while (out.size < count && queues.any { it.isNotEmpty() }) {
        // Pattern: two cards from the goal's main module, then one from the others in turn.
        val preferred = if (queues.size == 1 || i % 3 != 2) 0 else 1 + (i / 3) % (queues.size - 1)
        val src = queues[preferred].takeIf { it.isNotEmpty() } ?: queues.first { it.isNotEmpty() }
        out += src.removeAt(0)
        i++
    }
    return out
}

@Composable
fun ReviewHubScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val today = app.progress.today()
    val due = p.dueKeys(today).size
    val counts = p.stageCounts()
    val struggling = p.cards.filter { it.value.wrong > 0 && it.value.stage < Srs.MASTERED }.keys.mapNotNull { app.content.item(it) }

    ScreenScaffold("রিভিশন", "১-৩-৭-৩০ দিনের পুনরাবৃত্তি", onSearch = { nav.navigate(Routes.SEARCH) }) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                ContentCard(accent = MaterialTheme.colorScheme.tertiary) {
                    Text(if (due > 0) "আজ ${due.bn()}টি শব্দ রিভিউ করুন" else "আজকের রিভিউ শেষ ✓", style = MaterialTheme.typography.titleLarge)
                    Text("ঠিক সময়ে রিভিউ করলে শব্দ স্থায়ী স্মৃতিতে চলে যায়।", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button({ nav.navigate(Routes.reviewSession("due")) }, Modifier.fillMaxWidth(), enabled = due > 0) { Text("রিভিউ শুরু করুন") }
                    OutlinedButton({ nav.navigate(Routes.reviewSession("new")) }, Modifier.fillMaxWidth()) {
                        Text("নতুন ${p.dailyGoal.bn()}টি শব্দ শিখুন")
                    }
                }
            }
            item { SectionTitle("আপনার শব্দভান্ডার") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..Srs.MASTERED).forEach { s ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(14.dp).clip(CircleShape).background(StageColors[s]))
                            Text(Srs.STAGE_NAMES[s], Modifier.weight(1f).padding(start = 10.dp))
                            Text("${counts[s].bn()}টি", style = MaterialTheme.typography.titleSmall, color = StageColors[s])
                        }
                    }
                }
            }
            item { OutlinedButton({ nav.navigate(Routes.BOOKMARKS) }, Modifier.fillMaxWidth()) { Text("🔖 বুকমার্ক করা শব্দ (${p.bookmarks.size.bn()})") } }
            if (struggling.isNotEmpty()) {
                item { SectionTitle("যেগুলো বারবার ভুল হচ্ছে", color = WrongRed) }
                items(struggling.take(30), key = { it.key }) { item -> ItemRow(item) }
            }
        }
    }
}

@Composable
fun ItemRow(item: StudyItem, onClick: (() -> Unit)? = null) {
    val app = LocalApp.current
    ContentCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium)
                Text(item.answer, style = MaterialTheme.typography.bodyMedium)
                Text("${item.module.number.bn()}. ${item.module.titleEn}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (item.pos != null) PosChip(item.pos, showBn = false)
            SpeakButton({ app.speaker.speak(item.speak) })
        }
    }
}

/** mode: "due" (SRS review), "new" (today's lesson) or "bookmarks" (free practice). */
@Composable
fun ReviewSessionScreen(nav: NavHostController, mode: String) {
    val app = LocalApp.current
    val queue = remember(mode) {
        val p = app.progress.state.value
        when (mode) {
            "new" -> newItemsFor(app, p.goal, p.cards.keys, p.dailyGoal)
            "bookmarks" -> p.bookmarks.mapNotNull { app.content.item(it) }.shuffled()
            else -> p.dueKeys(app.progress.today()).mapNotNull { app.content.item(it) }.shuffled()
        }
    }
    var index by remember { mutableIntStateOf(0) }
    var flipped by remember { mutableStateOf(false) }
    var remembered by remember { mutableIntStateOf(0) }
    val title = when (mode) { "new" -> "নতুন পাঠ"; "bookmarks" -> "বুকমার্ক অনুশীলন"; else -> "রিভিউ" }

    ScreenScaffold(title, if (queue.isEmpty()) null else "${(index + 1).coerceAtMost(queue.size).bn()} / ${queue.size.bn()}",
        onBack = { nav.popBackStack() }) { m ->
        Column(m.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            if (queue.isEmpty()) {
                EmptyState("🎉", "এখন কিছু নেই", if (mode == "due") "আজকের সব রিভিউ শেষ। নতুন পাঠ শুরু করতে পারেন।" else "এখানে এখনো কোনো শব্দ নেই।")
                return@Column
            }
            if (index >= queue.size) {
                EmptyState("🏆", "সেশন শেষ!", "${queue.size.bn()}টি কার্ডের মধ্যে ${remembered.bn()}টি মনে ছিল। আগামীকাল আবার দেখা হবে।")
                Button({ nav.popBackStack() }, Modifier.fillMaxWidth()) { Text("ফিরে যান") }
                return@Column
            }
            LinearProgressIndicator(progress = { index.toFloat() / queue.size }, Modifier.fillMaxWidth())
            val item = queue[index]
            val state = app.progress.state.value.cards[item.key]
            if (state != null) {
                TagPill(Srs.STAGE_NAMES[state.stage], StageColors[state.stage])
            } else TagPill("নতুন শব্দ", HomophoneAmber)
            FlipCard(flipped, { flipped = !flipped }, front = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                    SpeakButton({ app.speaker.speak(item.speak) })
                }
                if (item.pos != null) PosChip(item.pos)
                Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Text("অর্থ মনে করার চেষ্টা করুন, তারপর ট্যাপ করুন 👆", color = MaterialTheme.colorScheme.primary)
            }, back = {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(item.answer, style = MaterialTheme.typography.headlineSmall)
                if (item.detail.isNotBlank()) Text(item.detail, style = MaterialTheme.typography.bodyLarge)
            })
            // Day-7 stage of the book's method: use the word in your own sentence.
            if (flipped && state?.stage == 3) {
                var sentence by remember(item.key) { mutableStateOf("") }
                OutlinedTextField(sentence, { sentence = it }, label = { Text("৭ম দিন: শব্দটি দিয়ে নিজের একটি বাক্য লিখুন") },
                    modifier = Modifier.fillMaxWidth())
            }
            if (flipped) {
                fun next() { index++; flipped = false }
                if (mode == "new" || state == null) {
                    Button({ app.progress.learn(item.key); remembered++; next() }, Modifier.fillMaxWidth()) { Text("শিখেছি ✓ রিভিউতে যোগ করুন") }
                    OutlinedButton({ next() }, Modifier.fillMaxWidth()) { Text("এড়িয়ে যান") }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GradeButton("ভুলে গেছি", WrongRed, Modifier.weight(1f)) { app.progress.review(item.key, Srs.Grade.FORGOT); next() }
                        GradeButton("একটু কষ্ট", HomophoneAmber, Modifier.weight(1f)) { app.progress.review(item.key, Srs.Grade.HARD); remembered++; next() }
                        GradeButton("মনে ছিল", CorrectGreen, Modifier.weight(1f)) { app.progress.review(item.key, Srs.Grade.REMEMBERED); remembered++; next() }
                    }
                }
            } else {
                Button({ flipped = true }, Modifier.fillMaxWidth()) { Text("উত্তর দেখুন") }
            }
        }
    }
}

@Composable
private fun GradeButton(text: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(onClick, modifier, colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)) {
        Text(text, textAlign = TextAlign.Center)
    }
}
