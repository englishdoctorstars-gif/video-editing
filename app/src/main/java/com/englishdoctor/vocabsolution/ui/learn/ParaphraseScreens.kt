package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.ProgressRing
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.components.TagPill

@Composable
fun ParaphraseTopicsScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val color = partColor(3)
    val topics = remember { app.content.paraphrases.groupBy { it.topicNo } }
    ScreenScaffold(ModuleId.PARAPHRASE.titleEn, "অধ্যায় ৯ · ${ModuleId.PARAPHRASE.titleBn}", onBack = { nav.popBackStack() },
        onSearch = { nav.navigate(Routes.SEARCH) }, accent = color) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("IELTS Reading-এ প্রশ্নের শব্দ আর প্যাসেজের শব্দ প্রায় কখনো হুবহু এক থাকে না। প্রতিটি collocation-এর সাথে দুটি paraphrase শিখে একই ধারণার ভিন্ন রূপ চিনুন।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(topics.keys.sorted()) { t ->
                val list = topics.getValue(t)
                val learned = list.count { "para:${it.id}" in p.cards }
                ContentCard(accent = color, onClick = { nav.navigate(Routes.paraTopic(t)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ProgressRing(learned.toFloat() / list.size, color) { Text(t.bn(), color = color) }
                        Column(Modifier.weight(1f).padding(start = 14.dp)) {
                            Text(list.first().topicEn, style = MaterialTheme.typography.titleMedium, color = color)
                            Text(list.first().topicBn)
                            Text("${list.size.bn()}টি collocation · শেখা ${learned.bn()}টি", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParaphraseTopicScreen(nav: NavHostController, topic: Int) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val list = remember(topic) { app.content.paraphrases.filter { it.topicNo == topic } }
    val structures = remember(topic) { list.map { it.structure }.distinct() }
    var structure by rememberSaveable { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val color = partColor(3)
    ScreenScaffold(list.first().topicEn, list.first().topicBn, onBack = { nav.popBackStack() }, accent = color) { m ->
        Column(m.fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text("${list.size.bn()}টি collocation") })
                Tab(tab == 1, { tab = 1 }, text = { Text("Paraphrase Match 🎯") })
            }
            if (tab == 1) {
                var round by remember { mutableIntStateOf(0) }
                val questions = remember(round) {
                    list.shuffled().take(10).map { c ->
                        makeQuestion("“${c.paraphrases.random()}”\n— কোন collocation-এর paraphrase?", c.collocation, list, { it.collocation },
                            "${c.collocation} = ${c.meaningBn}", hint = "IELTS Reading-এর মতো মিলিয়ে নিন")
                    }
                }
                QuickQuiz(questions, onRestart = { round++ })
                return@Column
            }
            ChipFilter(structures, structure, { it }) { structure = it }
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(query, { query = it }, label = { Text("খুঁজুন") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
                items(list.filter { (structure == null || it.structure == structure) &&
                    (query.isBlank() || it.collocation.contains(query.trim(), true) || it.meaningBn.contains(query.trim())) }, key = { it.id }) { c ->
                    val key = "para:${c.id}"
                    ContentCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${c.number.bn()}.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 6.dp))
                            Text(c.collocation, style = MaterialTheme.typography.titleMedium, color = color, modifier = Modifier.weight(1f))
                            SpeakButton({ app.speaker.speak(c.collocation) })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            TagPill(c.structure, color)
                            Text(c.meaningBn, style = MaterialTheme.typography.titleSmall)
                        }
                        c.paraphrases.forEach { Text("≈ $it", color = MaterialTheme.colorScheme.tertiary, modifier = Modifier.padding(top = 2.dp)) }
                        LearnChip(key in p.cards) { app.progress.learn(key) }
                    }
                }
            }
        }
    }
}
