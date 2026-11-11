package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.data.READ_SUBJECT_BN
import com.englishdoctor.vocabsolution.data.ReadingPassage
import com.englishdoctor.vocabsolution.data.VocabEntry
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.IpaText
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.PosChip
import com.englishdoctor.vocabsolution.ui.components.PosLegend
import com.englishdoctor.vocabsolution.ui.components.PosMark
import com.englishdoctor.vocabsolution.ui.components.ProgressRing
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.theme.LocalDarkTheme
import com.englishdoctor.vocabsolution.ui.theme.Pos

private fun learnedIn(p: ReadingPassage, cards: Set<String>) = p.vocab.count { "readv:${p.id}:${it.word}" in cards }

@Composable
fun ReadSubjectsScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val color = partColor(3)
    val bySubject = remember { app.content.passages.groupBy { it.subjectNo } }
    ScreenScaffold(ModuleId.READ.titleEn, "অধ্যায় ৮ · ${ModuleId.READ.titleBn}", onBack = { nav.popBackStack() },
        onSearch = { nav.navigate(Routes.SEARCH) }, accent = color) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("IELTS Reading-এর ১০টি জনপ্রিয় বিষয়ে ১৫০টি ছোট বাংলা প্যাসেজ, প্রতিটিতে ১০টি একাডেমিক শব্দ প্রসঙ্গের ভেতরে। আগে প্রসঙ্গ থেকে অর্থ আন্দাজ করুন, তারপর মিলিয়ে নিন।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(bySubject.keys.sorted()) { s ->
                val list = bySubject.getValue(s)
                val learned = list.sumOf { learnedIn(it, p.cards.keys) }
                val total = list.sumOf { it.vocab.size }
                ContentCard(accent = color, onClick = { nav.navigate(Routes.readSubject(s)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(learned.toFloat() / total, color) { Text(s.bn(), color = color) }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(list.first().subject, style = MaterialTheme.typography.titleMedium, color = color)
                            Text(READ_SUBJECT_BN[s] ?: "", style = MaterialTheme.typography.bodyMedium)
                            Text("${list.size.bn()}টি প্যাসেজ · ${total.bn()}টি শব্দ · শেখা ${learned.bn()}টি",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReadTopicsScreen(nav: NavHostController, subject: Int) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val list = remember(subject) { app.content.passages.filter { it.subjectNo == subject }.sortedBy { it.topicNo } }
    ScreenScaffold(list.first().subject, READ_SUBJECT_BN[subject], onBack = { nav.popBackStack() }, accent = partColor(3)) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { ps ->
                val learned = learnedIn(ps, p.cards.keys)
                ContentCard(onClick = { nav.navigate(Routes.passage(ps.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${ps.topicNo.bn()}.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ps.title, style = MaterialTheme.typography.titleMedium)
                            Text(ps.vocab.take(4).joinToString(" · ") { it.word } + " …", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${learned.bn()}/১০", color = partColor(3), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

/** Maps an English token in the passage ("Escalate", "Excavation") to its Vocab Box entry. */
private fun matchVocab(token: String, vocab: List<VocabEntry>): VocabEntry? {
    val t = token.lowercase()
    vocab.firstOrNull { it.word.lowercase() == t }?.let { return it }
    return vocab.firstOrNull { v ->
        val w = v.word.lowercase()
        val stem = w.take(maxOf(4, w.length - 2))
        t.startsWith(stem) || w.startsWith(t.take(maxOf(4, t.length - 2)))
    }
}

@Composable
fun PassageScreen(nav: NavHostController, id: String) {
    val app = LocalApp.current
    val passage = remember(id) { app.content.passage(id) }
    val p by app.progress.state.collectAsStateWithLifecycle()
    var guessMode by rememberSaveable { mutableStateOf(true) }
    var selected by remember { mutableStateOf<VocabEntry?>(null) }
    var quiz by rememberSaveable { mutableStateOf(false) }
    val dark = LocalDarkTheme.current

    val body: AnnotatedString = remember(passage, dark) {
        buildAnnotatedString {
            var last = 0
            Regex("[A-Za-z][A-Za-z\\-]+").findAll(passage.bodyBn).forEach { m ->
                append(passage.bodyBn.substring(last, m.range.first))
                val v = matchVocab(m.value, passage.vocab)
                if (v != null) {
                    val c = Pos.of(v.pos).let { if (dark) it.dark else it.light }
                    withLink(LinkAnnotation.Clickable(v.word, TextLinkStyles(SpanStyle(color = c, fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline))) { selected = v }) { append(m.value) }
                } else append(m.value)
                last = m.range.last + 1
            }
            append(passage.bodyBn.substring(last))
        }
    }

    ScreenScaffold(passage.title, "${passage.subject} · টপিক ${passage.topicNo.bn()}", onBack = { nav.popBackStack() }, accent = partColor(3)) { m ->
        if (quiz) {
            Column(m.fillMaxSize()) {
                TextButton({ quiz = false }) { Text("← প্যাসেজে ফিরুন") }
                var round by remember { mutableIntStateOf(0) }
                val questions = remember(round) {
                    passage.vocab.shuffled().map { v ->
                        makeQuestion("‘${v.word}’ — প্যাসেজের প্রসঙ্গে অর্থ কী?", v.meaningBn, passage.vocab, { it.meaningBn },
                            "${v.word} ${v.ipa} = ${v.meaningBn}", hint = "Context Quiz")
                    }
                }
                QuickQuiz(questions, onRestart = { round++ }, onAnswered = { })
            }
            return@ScreenScaffold
        }
        Column(m.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(guessMode, { guessMode = true }, label = { Text("আন্দাজ মোড 🤔") })
                FilterChip(!guessMode, { guessMode = false }, label = { Text("অর্থসহ") })
            }
            Text(if (guessMode) "রঙিন শব্দগুলোর অর্থ প্রসঙ্গ থেকে আন্দাজ করুন; ট্যাপ করলে অর্থ দেখাবে।" else "প্রতিটি শব্দের অর্থ নিচের Vocab Box-এ।",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ContentCard { Text(body, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.15f)) }
            PosLegend()
            Text("Vocab Box", style = MaterialTheme.typography.titleMedium, color = partColor(3))
            passage.vocab.forEach { v ->
                val key = "readv:${passage.id}:${v.word}"
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PosMark(v.pos)
                    Column(Modifier.weight(1f)) {
                        Text(v.word, style = MaterialTheme.typography.titleSmall)
                        IpaText(v.ipa)
                        Text(if (guessMode && key !in p.cards) "• • •" else v.meaningBn,
                            color = if (guessMode && key !in p.cards) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
                    }
                    SpeakButton({ app.speaker.speak(v.word) })
                }
            }
            Button({ quiz = true }, Modifier.fillMaxWidth()) { Text("Context Quiz শুরু করুন (১০টি প্রশ্ন)") }
            OutlinedButton({ passage.vocab.forEach { app.progress.learn("readv:${passage.id}:${it.word}") } }, Modifier.fillMaxWidth()) {
                Text("সব ১০টি শব্দ রিভিউতে যোগ করুন")
            }
        }
    }
    selected?.let { v ->
        val key = "readv:${passage.id}:${v.word}"
        AlertDialog(
            onDismissRequest = { selected = null },
            title = { Text(v.word) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PosChip(v.pos)
                    IpaText(v.ipa)
                    Text(v.meaningBn, style = MaterialTheme.typography.titleLarge)
                    LearnChip(key in p.cards) { app.progress.learn(key) }
                }
            },
            confirmButton = { TextButton({ selected = null }) { Text("ঠিক আছে") } },
            dismissButton = { TextButton({ app.speaker.speak(v.word) }) { Text("🔊 শুনুন") } },
        )
    }
}
