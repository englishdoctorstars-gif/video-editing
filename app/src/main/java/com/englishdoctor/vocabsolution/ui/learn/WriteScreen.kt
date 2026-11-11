package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.data.WritingSection
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.LabelValue
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.SectionTitle
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.WrongRed

@Composable
fun WriteScreen(nav: NavHostController) {
    ModuleTabsScreen(nav, ModuleId.WRITE,
        listOf("Task 1", "গ্রাফ কুইজ 📈", "Task 2", "Linking Words", "সাধারণ → একাডেমিক", "Upgrade It 🎯", "প্রচলিত ভুল")) { tab ->
        when (tab) {
            0 -> Sections("task1")
            1 -> GraphQuiz()
            2 -> Sections("task2", withTopicVocab = true)
            3 -> Sections("linking")
            4 -> Sections("academic")
            5 -> UpgradeQuiz()
            else -> Sections("mistakes")
        }
    }
}

@Composable
private fun Sections(group: String, withTopicVocab: Boolean = false) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val sections = remember(group) { app.content.writing.sections.filter { it.group == group } }
    val color = partColor(3)
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (group == "task1") item {
            Text("দুটি গঠন পাল্টে পাল্টে লিখুন: Sales rose sharply in 2020. (verb + adverb) অথবা There was a sharp rise in sales in 2020. (adjective + noun)",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        sections.forEach { s ->
            item { SectionTitle("${s.titleBn} · ${s.titleEn}", color = color) }
            items(s.rows.indices.toList(), key = { "${s.id}-$it" }) { i -> RowCard(s, s.rows[i], i, p.cards.keys) }
        }
        if (withTopicVocab) {
            item { SectionTitle("বিষয়ভিত্তিক শব্দজুটি · Topic Vocabulary", color = color) }
            app.content.writing.topicVocab.forEach { c ->
                item {
                    ContentCard {
                        Text("${c.categoryBn} · ${c.categoryEn}", style = MaterialTheme.typography.titleMedium, color = color)
                        c.items.forEachIndexed { i, t ->
                            val key = "topic:${c.categoryEn}:$i"
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f).padding(vertical = 2.dp)) {
                                    Text(t.en, style = MaterialTheme.typography.titleSmall)
                                    Text(t.bn, style = MaterialTheme.typography.bodyMedium)
                                }
                                SpeakButton({ app.speaker.speak(t.en) })
                                if (key !in p.cards) androidx.compose.material3.TextButton({ app.progress.learn(key) }) { Text("+ রিভিউ") }
                                else Text("✓", color = CorrectGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowCard(s: WritingSection, row: List<String>, index: Int, learned: Set<String>) {
    val app = LocalApp.current
    ContentCard {
        when (s.group) {
            "mistakes" -> {
                Text("✗ ${row.getOrElse(0) { "" }}", color = WrongRed, style = MaterialTheme.typography.titleMedium)
                Text("✓ ${row.getOrElse(1) { "" }}", color = CorrectGreen, style = MaterialTheme.typography.titleMedium)
                Text(row.getOrElse(2) { "" })
            }
            "academic" -> {
                val key = "acad:${s.id}:$index"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(row[0], style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("→ ${row.getOrElse(1) { "" }}", style = MaterialTheme.typography.titleMedium, color = partColor(3), fontWeight = FontWeight.SemiBold)
                        Text(row.getOrElse(2) { "" })
                    }
                    SpeakButton({ app.speaker.speak(row.getOrElse(1) { "" }) })
                }
                LearnChip(key in learned) { app.progress.learn(key) }
            }
            else -> {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(row[0], style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    if (s.group != "task1" || s.columns.size != 4) SpeakButton({ app.speaker.speak(row[0].replace("…", "")) })
                }
                row.drop(1).forEachIndexed { i, cell -> LabelValue(s.columns.getOrElse(i + 1) { "" }, cell) }
            }
        }
    }
}

/** Upgrade It: pick the academic alternative for an everyday word. */
@Composable
private fun UpgradeQuiz() {
    val app = LocalApp.current
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(round) {
        val rows = app.content.writing.sections.filter { it.group == "academic" }.flatMap { it.rows }
        rows.shuffled().take(10).map { r ->
            makeQuestion("‘${r[0]}’-এর একাডেমিক বিকল্প কোনটি?", r[1], rows, { it[1] }, "${r[0]} → ${r[1]} (${r.getOrElse(2) { "" }})",
                hint = "Upgrade It · Band বাড়ান")
        }
    }
    QuickQuiz(questions, onRestart = { round++ })
}

private data class Trend(val answer: String, val points: List<Float>)

private val TRENDS = listOf(
    Trend("rose sharply", listOf(0.1f, 0.15f, 0.2f, 0.6f, 0.9f)),
    Trend("increased gradually", listOf(0.2f, 0.3f, 0.4f, 0.5f, 0.6f)),
    Trend("fell dramatically", listOf(0.9f, 0.85f, 0.8f, 0.3f, 0.1f)),
    Trend("declined slightly", listOf(0.7f, 0.68f, 0.65f, 0.62f, 0.6f)),
    Trend("remained stable", listOf(0.5f, 0.51f, 0.5f, 0.49f, 0.5f)),
    Trend("fluctuated", listOf(0.4f, 0.7f, 0.35f, 0.75f, 0.45f)),
    Trend("peaked and then fell", listOf(0.2f, 0.5f, 0.9f, 0.5f, 0.3f)),
    Trend("bottomed out and recovered", listOf(0.8f, 0.4f, 0.1f, 0.45f, 0.75f)),
    Trend("levelled off after rising", listOf(0.1f, 0.4f, 0.7f, 0.72f, 0.71f)),
)

/** Graph Describer: a tiny line chart; choose the phrase that describes it. */
@Composable
private fun GraphQuiz() {
    var round by remember { mutableIntStateOf(0) }
    val order = remember(round) { TRENDS.shuffled() }
    val questions = remember(round) {
        order.map { t -> makeQuestion("লেখচিত্রটি বর্ণনা করুন: Sales ___ between 2010 and 2020.", t.answer, TRENDS, { it.answer }, hint = "Task 1 · Graph Describer") }
    }
    var current by remember(round) { mutableIntStateOf(0) }
    Column {
        TrendChart(order.getOrNull(current)?.points ?: emptyList())
        QuickQuiz(questions, onRestart = { round++ }, onAnswered = { }, modifier = Modifier, onIndexChange = { current = it })
    }
}

@Composable
private fun TrendChart(points: List<Float>) {
    if (points.isEmpty()) return
    val line = partColor(3)
    val grid = MaterialTheme.colorScheme.outlineVariant
    Canvas(Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 24.dp, vertical = 12.dp)) {
        val w = size.width
        val h = size.height
        for (i in 0..4) drawLine(grid, Offset(0f, h * i / 4), Offset(w, h * i / 4), 1f)
        drawLine(grid, Offset(0f, 0f), Offset(0f, h), 2f)
        val path = Path()
        points.forEachIndexed { i, v ->
            val x = w * i / (points.size - 1)
            val y = h * (1 - v)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, line, style = Stroke(6f, cap = StrokeCap.Round))
        points.forEachIndexed { i, v -> drawCircle(line, 7f, Offset(w * i / (points.size - 1), h * (1 - v))) }
    }
}
