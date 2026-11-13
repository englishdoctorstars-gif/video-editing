package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.EmptyState
import com.englishdoctor.vocabsolution.ui.components.IpaText
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.PosMark
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.components.TagPill
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.HomophoneAmber
import com.englishdoctor.vocabsolution.ui.theme.Pos
import com.englishdoctor.vocabsolution.ui.theme.WrongRed
import com.englishdoctor.vocabsolution.ui.theme.color

@Composable
fun ListenScreen(nav: NavHostController, initialTab: Int = 0) {
    ModuleTabsScreen(nav, ModuleId.LISTEN, initialTab = initialTab, tabs =
        listOf("Section শব্দ", "Listen & Spell 🎧", "বানানের ফাঁদ", "মানচিত্র", "সংখ্যা ও তারিখ", "সংকেত-শব্দ")) { tab ->
        when (tab) {
            0 -> SectionWords()
            1 -> ListenAndSpell()
            2 -> SpellingTrapList()
            3 -> MapWords()
            4 -> NumberTips()
            else -> Signposts()
        }
    }
}

@Composable
private fun SectionWords() {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    var section by rememberSaveable { mutableStateOf<Int?>(null) }
    val words = app.content.listeningWords
    Column {
        ChipFilter((1..4).toList(), section, { "Section ${it.bn()}" }) { section = it }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            words.filter { section == null || it.section == section }.groupBy { it.section }.forEach { (s, list) ->
                item {
                    ContentCard(accent = partColor(3)) {
                        Text("Section ${s.bn()} · ${list.first().sectionBn}", style = MaterialTheme.typography.titleMedium, color = partColor(3))
                        Text(list.first().sectionDescBn, style = MaterialTheme.typography.bodySmall)
                    }
                }
                items(list, key = { it.id }) { w ->
                    val key = "listen:${w.id}"
                    ContentCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            PosMark(w.pos)
                            Column(Modifier.weight(1f)) {
                                Text(w.word, style = MaterialTheme.typography.titleMedium, color = Pos.of(w.pos).color())
                                IpaText(w.ipa)
                                Text(w.meaningBn)
                            }
                            SpeakButton({ app.speaker.speak(w.word) })
                        }
                        LearnChip(key in p.cards) { app.progress.learn(key) }
                    }
                }
            }
        }
    }
}

/** TTS says a word, the learner types it; wrong letters are marked red. */
@Composable
private fun ListenAndSpell() {
    val app = LocalApp.current
    var round by remember { mutableIntStateOf(0) }
    val queue = remember(round) {
        (app.content.spellingTraps.map { it.word to it.trapBn } +
            app.content.listeningWords.filter { " " !in it.word }.map { it.word to it.meaningBn })
            .shuffled().take(10)
    }
    var index by remember(round) { mutableIntStateOf(0) }
    var typed by remember(round) { mutableStateOf("") }
    var checked by remember(round) { mutableStateOf(false) }
    var score by remember(round) { mutableIntStateOf(0) }

    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (index >= queue.size) {
            EmptyState("🎧", "স্কোর: ${score.bn()} / ${queue.size.bn()}", "IELTS-এ বানান ভুল হলে উত্তর সঠিক ধরা হয় না, তাই নিয়মিত অনুশীলন করুন।")
            Button({ round++ }, Modifier.fillMaxWidth()) { Text("আবার খেলুন") }
            return@Column
        }
        val (word, hint) = queue[index]
        LaunchedEffect(index, round) { app.speaker.speak(word) }
        LinearProgressIndicator(progress = { index.toFloat() / queue.size }, Modifier.fillMaxWidth())
        Text("শব্দটি শুনুন এবং সঠিক বানানে লিখুন", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button({ app.speaker.speak(word) }) { Text("🔊 আবার শুনুন") }
            OutlinedButton({ app.speaker.speak(word, slow = true) }) { Text("🐢 ধীরে") }
        }
        OutlinedTextField(
            typed, { if (!checked) typed = it }, singleLine = true, label = { Text("বানান লিখুন") },
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None, autoCorrectEnabled = false, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
        if (!checked) {
            Button({ checked = true; if (typed.trim().equals(word, true)) score++ }, Modifier.fillMaxWidth(), enabled = typed.isNotBlank()) {
                Text("যাচাই করুন")
            }
        } else {
            val ok = typed.trim().equals(word, true)
            Text(if (ok) "✓ সঠিক বানান!" else "✗ সঠিক বানান: $word", color = if (ok) CorrectGreen else WrongRed,
                style = MaterialTheme.typography.titleLarge)
            if (!ok) {
                Text(buildAnnotatedString {
                    append("আপনার লেখা: ")
                    typed.trim().forEachIndexed { i, c ->
                        val good = i < word.length && word[i].equals(c, true)
                        withStyle(SpanStyle(color = if (good) CorrectGreen else WrongRed, fontWeight = FontWeight.Bold,
                            textDecoration = if (good) null else TextDecoration.Underline)) { append(c) }
                    }
                }, style = MaterialTheme.typography.titleMedium)
            }
            Text("সূত্র: $hint", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button({ index++; typed = ""; checked = false }, Modifier.fillMaxWidth()) { Text("পরের শব্দ") }
        }
    }
}

@Composable
private fun SpellingTrapList() {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(app.content.spellingTraps, key = { it.id }) { t ->
            val key = "spell:${t.id}"
            ContentCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(t.word, style = MaterialTheme.typography.titleLarge)
                        Text(t.meaningBn)
                    }
                    SpeakButton({ app.speaker.speak(t.word) })
                }
                TagPill("⚠️ ${t.trapBn}", HomophoneAmber)
                LearnChip(key in p.cards) { app.progress.learn(key) }
            }
        }
    }
}

@Composable
private fun MapWords() {
    val app = LocalApp.current
    Column {
        Text("মানচিত্রের প্রশ্নে দিক ও অবস্থান বোঝার ৩০টি শব্দ। ট্যাপ করলে উচ্চারণ শুনবেন।",
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(16.dp))
        LazyVerticalGrid(GridCells.Adaptive(150.dp), contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(app.content.mapWords, key = { it.id }) { m ->
                ContentCard(onClick = { app.speaker.speak(m.term) }) {
                    Text("🧭 ${m.term}", style = MaterialTheme.typography.titleSmall)
                    Text(m.meaningBn, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun NumberTips() {
    val tips = LocalApp.current.content.numberTips
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(tips, key = { it.id }) { t ->
            ContentCard(accent = partColor(3)) {
                Text(t.titleBn, style = MaterialTheme.typography.titleMedium, color = partColor(3))
                t.bodyBn.split("\n").forEach { line ->
                    Text(line, fontWeight = if ("→" in line) FontWeight.SemiBold else FontWeight.Normal,
                        color = if ("→" in line) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun Signposts() {
    val app = LocalApp.current
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { Text("লেকচারে এই শব্দগুলো শুনলে বুঝবেন বক্তা কোন দিকে যাচ্ছেন।", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(app.content.signposts, key = { it.id }) { s ->
            ContentCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(s.signpost, style = MaterialTheme.typography.titleMedium)
                        Text(s.meaningBn)
                    }
                    TagPill(s.functionBn, partColor(3))
                    SpeakButton({ app.speaker.speak(s.signpost) })
                }
            }
        }
    }
}
