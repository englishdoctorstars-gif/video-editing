package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.WrongRed

@Composable
fun CollocationsScreen(nav: NavHostController, initialTab: Int = 0) {
    ModuleTabsScreen(nav, ModuleId.COLLOCATIONS, initialTab = initialTab, tabs = listOf("Power Verbs", "দৈনন্দিন জীবন", "প্রচলিত ভুল", "Verb Wheel 🎯", "Fix-it ✓")) { tab ->
        when (tab) {
            0 -> CollocationList("power_verbs")
            1 -> CollocationList("daily_life")
            2 -> MistakeList()
            3 -> VerbWheelQuiz()
            else -> FixItQuiz()
        }
    }
}

@Composable
private fun CollocationList(section: String) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val all = remember(section) { app.content.collocations.filter { it.section == section } }
    val groups = remember(section) { all.map { it.groupEn }.distinct() }
    var group by rememberSaveable(section) { mutableStateOf<String?>(null) }
    val color = partColor(2)
    Column {
        ChipFilter(groups, group, { g -> all.first { it.groupEn == g }.let { c -> c.groupBn.ifBlank { c.groupEn } } }) { group = it }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(all.filter { group == null || it.groupEn == group }, key = { it.id }) { c ->
                val key = "coll:${c.id}"
                ContentCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${c.number.bn()}.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                        Text(c.phrase, style = MaterialTheme.typography.titleLarge, color = color, modifier = Modifier.weight(1f))
                        SpeakButton({ app.speaker.speak(c.phrase) })
                    }
                    Text(c.meaningBn, style = MaterialTheme.typography.titleSmall)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.exampleEn, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                            Text(c.exampleBn, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        SpeakButton({ app.speaker.speak(c.exampleEn) })
                    }
                    LearnChip(key in p.cards) { app.progress.learn(key) }
                }
            }
        }
    }
}


@Composable
private fun MistakeList() {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            Text("বাংলা থেকে সরাসরি অনুবাদ করলে যে ভুলগুলো সবচেয়ে বেশি হয়:", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(app.content.collocationMistakes, key = { it.id }) { m ->
            val key = "mistake:${m.id}"
            ContentCard {
                Text("✗ ${m.wrong}", color = WrongRed, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("✓ ${m.right}", color = CorrectGreen, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    SpeakButton({ app.speaker.speak(m.right) })
                }
                Text(m.meaningBn)
                LearnChip(key in p.cards) { app.progress.learn(key) }
            }
        }
    }
}

/** Verb Wheel: which power verb completes the collocation? */
@Composable
private fun VerbWheelQuiz() {
    val app = LocalApp.current
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(round) {
        val power = app.content.collocations.filter { it.section == "power_verbs" }
        val verbs = power.map { it.groupEn.lowercase() }.distinct()
        power.shuffled().take(10).mapNotNull { c ->
            val verb = c.groupEn.lowercase()
            val words = c.phrase.split(" ")
            if (!words.first().equals(verb, true)) return@mapNotNull null
            val blank = "_____ " + words.drop(1).joinToString(" ")
            makeQuestion(blank, verb, verbs, { it }, "${c.phrase} = ${c.meaningBn}\n${c.exampleEn}", hint = "কোন ক্রিয়া বসবে?")
        }
    }
    QuickQuiz(questions, onRestart = { round++ })
}

/** Fix-it: pick the natural English from a wrong/right pair. */
@Composable
private fun FixItQuiz() {
    val app = LocalApp.current
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(round) {
        app.content.collocationMistakes.shuffled().take(10).map { m ->
            val opts = listOf(m.wrong, m.right).shuffled()
            QuizQuestion("“${m.meaningBn}” — ইংরেজিতে কোনটি সঠিক?", opts, opts.indexOf(m.right), "✗ ${m.wrong} → ✓ ${m.right}", hint = "Fix-it ✓")
        }
    }
    QuickQuiz(questions, onRestart = { round++ })
}
