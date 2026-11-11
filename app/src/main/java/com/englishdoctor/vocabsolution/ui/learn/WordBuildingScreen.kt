package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.data.MultiplyingWord
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.PosChip
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.components.TagPill
import com.englishdoctor.vocabsolution.ui.theme.Pos
import com.englishdoctor.vocabsolution.ui.theme.color

private val TABS = listOf("প্রত্যয়", "উপসর্গ", "শব্দ ভাঙো", "শব্দ গুণন", "POS কুইজ")

@Composable
fun WordBuildingScreen(nav: NavHostController) {
    val color = partColor(1)
    var tab by rememberSaveable { mutableIntStateOf(0) }
    ScreenScaffold(ModuleId.WORD_BUILDING.titleEn, "অধ্যায় ১ · ${ModuleId.WORD_BUILDING.titleBn}",
        onBack = { nav.popBackStack() }, onSearch = { nav.navigate(Routes.SEARCH) }, accent = color) { m ->
        Column(m.fillMaxSize()) {
            PrimaryScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                TABS.forEachIndexed { i, t -> Tab(tab == i, { tab = i }, text = { Text(t) }) }
            }
            when (tab) {
                0 -> SuffixList()
                1 -> PrefixList()
                2 -> AnalysisList()
                3 -> MultiplyingGrid(nav)
                else -> PosDetective()
            }
        }
    }
}

@Composable
private fun SuffixList() {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("শব্দের শেষাংশ দেখেই পদ চেনা যায়। তবে ব্যতিক্রম আছে: friendly, lovely বিশেষণ; building বিশেষ্য।",
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(app.content.suffixes, key = { it.id }) { s ->
            val key = "suffix:${s.id}"
            ContentCard(accent = Pos.of(s.posFormed).color()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s.affix, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    PosChip(s.posFormed)
                }
                Text("অর্থ: ${s.meaningBn}", style = MaterialTheme.typography.bodyLarge)
                ExampleWords(s.examples)
                LearnChip(key in p.cards) { app.progress.learn(key) }
            }
        }
    }
}

@Composable
private fun PrefixList() {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(app.content.prefixes, key = { it.id }) { s ->
            val key = "prefix:${s.id}"
            ContentCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s.affix, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    s.typeBn?.let { TagPill(it, partColor(1)) }
                }
                Text("অর্থ: ${s.meaningBn}", style = MaterialTheme.typography.bodyLarge)
                ExampleWords(s.examples)
                LearnChip(key in p.cards) { app.progress.learn(key) }
            }
        }
    }
}

@Composable
fun ExampleWords(words: List<String>) {
    val app = LocalApp.current
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        words.forEach { w ->
            Text("🔊 $w", color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { app.speaker.speak(w.replace("(s)", "")) }.padding(vertical = 4.dp))
        }
    }
}

/** Word Breaker: guess the parts first, then reveal prefix + root + suffix. */
@Composable
private fun AnalysisList() {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val revealed = remember { mutableStateOf(setOf<String>()) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("প্রথমে নিজে শব্দটি ভাঙার চেষ্টা করুন, তারপর \"ভেঙে দেখাও\" চাপুন।", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(app.content.analyses, key = { it.id }) { a ->
            val open = a.id in revealed.value
            ContentCard(accent = Pos.of(a.pos).color()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(a.word, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    SpeakButton({ app.speaker.speak(a.word) })
                }
                PosChip(a.pos)
                if (open) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                        a.parts.forEachIndexed { i, part ->
                            if (i > 0) Text("+", style = MaterialTheme.typography.titleMedium)
                            TagPill(part, partColor(if (i == 0) 2 else if (i == a.parts.lastIndex) 3 else 1))
                        }
                    }
                    Text("অর্থ: ${a.meaningBn}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    LearnChip("analysis:${a.id}" in p.cards) { app.progress.learn("analysis:${a.id}") }
                } else {
                    TextButton({ revealed.value = revealed.value + a.id }) { Text("ভেঙে দেখাও ✂️") }
                }
            }
        }
    }
}

/** 120 practice words: break them yourself, then open the matching root family (chapter 2). */
@Composable
private fun MultiplyingGrid(nav: NavHostController) {
    val app = LocalApp.current
    var selected by remember { mutableStateOf<MultiplyingWord?>(null) }
    Column {
        Text("প্রতিটি শব্দ একটি মূল শব্দ থেকে গঠিত। ভেঙে মূল, উপসর্গ ও প্রত্যয় খুঁজুন; তারপর শব্দ-পরিবার মিলিয়ে নিন।",
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
        LazyVerticalGrid(GridCells.Adaptive(160.dp), contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(app.content.multiplying, key = { it.id }) { w ->
                ContentCard(onClick = { selected = w }) {
                    Text(w.order.bn(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(w.word, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
    selected?.let { w ->
        val family = remember(w) { app.content.rootFor(w.word) }
        val entry = family?.let { f -> (f.rootPlusSuffix + f.prefixPlusRoot).first { it.word.equals(w.word, true) } }
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(w.word) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (entry != null) {
                        PosChip(entry.pos)
                        Text("অর্থ: ${entry.meaningBn}")
                        Text("মূল শব্দ: ${family.root}", fontWeight = FontWeight.SemiBold)
                    } else {
                        Text("এই শব্দটি নিজে ভেঙে দেখুন, তারপর সার্চ করে মূল শব্দ মিলিয়ে নিন।")
                    }
                }
            },
            confirmButton = {
                if (family != null) TextButton({ selected = null; nav.navigate(Routes.root(family.id)) }) { Text("পরিবার দেখুন →") }
            },
            dismissButton = {
                TextButton({ app.speaker.speak(w.word) }) { Text("🔊 শুনুন") }
            },
        )
    }
}

/** POS Detective: guess the part of speech from the suffix. */
@Composable
private fun PosDetective() {
    val app = LocalApp.current
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(round) {
        val posBn = listOf("noun", "verb", "adjective", "adverb").associateWith { Pos.of(it).let { p -> "${p.symbol} ${p.bn}" } }
        app.content.suffixes.filter { it.posFormed in posBn }.shuffled().take(10).map { s ->
            val answer = posBn.getValue(s.posFormed!!)
            val opts = posBn.values.toList()
            QuizQuestion("${s.affix} দিয়ে শেষ হলে শব্দটি সাধারণত কোন পদ?", opts, opts.indexOf(answer),
                "উদাহরণ: ${s.examples.joinToString(", ")} · অর্থ: ${s.meaningBn}", hint = "POS Detective 🕵️")
        }
    }
    QuickQuiz(questions, onRestart = { round++ })
}
