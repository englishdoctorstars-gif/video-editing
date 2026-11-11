package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.PosChip
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.WrongRed

@Composable
fun EquivalentsScreen(nav: NavHostController) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    ScreenScaffold("Word Equivalents", "অধ্যায় ৫ · ${ModuleId.EQUIVALENTS.titleBn}",
        onBack = { nav.popBackStack() }, onSearch = { nav.navigate(Routes.SEARCH) }, accent = partColor(2)) { m ->
        Column(m.fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text("সমার্থক-বিপরীত") })
                Tab(tab == 1, { tab = 1 }, text = { Text("এক কথায়") })
                Tab(tab == 2, { tab = 2 }, text = { Text("কুইজ 🎯") })
            }
            when (tab) {
                0 -> SynAntList()
                1 -> OneWordList()
                else -> EquivalentsQuiz()
            }
        }
    }
}

@Composable
private fun SynAntList() {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    var q by rememberSaveable { mutableStateOf("") }
    val list = app.content.synAnts.filter { q.isBlank() || it.word.contains(q.trim(), true) || it.meaningBn.contains(q.trim()) }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item { OutlinedTextField(q, { q = it }, label = { Text("শব্দ খুঁজুন") }, singleLine = true, modifier = Modifier.fillMaxWidth()) }
        items(list, key = { it.id }) { s ->
            val key = "synant:${s.id}"
            ContentCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(s.word, style = MaterialTheme.typography.titleLarge)
                    PosChip(s.pos, showBn = false)
                    Row(Modifier.weight(1f)) {}
                    SpeakButton({ app.speaker.speak(s.word) })
                }
                Text(s.meaningBn, style = MaterialTheme.typography.titleSmall)
                Text("≈ সমার্থক: ${s.synonyms.joinToString(", ")}", color = CorrectGreen)
                Text("↔ বিপরীত: ${s.antonyms.joinToString(", ")}", color = WrongRed)
                LearnChip(key in p.cards) { app.progress.learn(key) }
            }
        }
    }
}

@Composable
private fun OneWordList() {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val cats = remember { app.content.oneWords.map { it.categoryBn }.distinct() }
    var cat by rememberSaveable { mutableStateOf<String?>(null) }
    val list = app.content.oneWords.filter { cat == null || it.categoryBn == cat }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { FilterChip(cat == null, { cat = null }, label = { Text("সব") }) }
                items(cats) { c -> FilterChip(cat == c, { cat = if (cat == c) null else c }, label = { Text(c) }) }
            }
        }
        items(list, key = { it.id }) { o ->
            val key = "oneword:${o.id}"
            ContentCard {
                Text(o.descriptionEn, style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(o.answer, style = MaterialTheme.typography.titleLarge, color = partColor(2), modifier = Modifier.weight(1f))
                    SpeakButton({ app.speaker.speak(o.answer) })
                }
                Text(o.meaningBn, style = MaterialTheme.typography.titleSmall)
                LearnChip(key in p.cards) { app.progress.learn(key) }
            }
        }
    }
}

@Composable
private fun EquivalentsQuiz() {
    val app = LocalApp.current
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(round) {
        val syn = app.content.synAnts
        val one = app.content.oneWords
        val list = mutableListOf<QuizQuestion>()
        syn.shuffled().take(4).forEach { s ->
            list += makeQuestion("'${s.word}'-এর সমার্থক শব্দ কোনটি?", s.synonyms.first(), syn.flatMap { it.antonyms + it.synonyms }
                .filter { it !in s.synonyms }, { it }, "${s.word} = ${s.meaningBn}", hint = "Synonym · সমার্থক")
        }
        syn.shuffled().take(3).forEach { s ->
            list += makeQuestion("'${s.word}'-এর বিপরীত শব্দ কোনটি?", s.antonyms.first(), syn.flatMap { it.synonyms }
                .filter { it !in s.antonyms }, { it }, "${s.word} = ${s.meaningBn}", hint = "Antonym · বিপরীত")
        }
        one.shuffled().take(3).forEach { o ->
            list += makeQuestion(o.descriptionEn, o.answer, one.filter { it.categoryBn == o.categoryBn }, { it.answer },
                "${o.answer} = ${o.meaningBn}", hint = "One-word Substitution · এক কথায় প্রকাশ")
        }
        list.shuffled()
    }
    QuickQuiz(questions, onRestart = { round++ })
}
