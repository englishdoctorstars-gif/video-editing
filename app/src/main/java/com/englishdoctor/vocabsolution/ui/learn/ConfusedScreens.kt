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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ConfusedPair
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.data.PairSide
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.BookmarkButton
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.IpaText
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.PosChip
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.ShareButton
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.share.toShareCard
import com.englishdoctor.vocabsolution.ui.components.TagPill
import com.englishdoctor.vocabsolution.ui.theme.HomophoneAmber

private val SIDE_A = Color(0xFF1E88E5)
private val SIDE_B = Color(0xFFD81B60)

@Composable
fun ConfusedListScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var homophonesOnly by rememberSaveable { mutableStateOf(false) }
    ScreenScaffold(ModuleId.CONFUSED.titleBn, "অধ্যায় ৪ · ${ModuleId.CONFUSED.titleEn}",
        onBack = { nav.popBackStack() }, onSearch = { nav.navigate(Routes.SEARCH) }, accent = partColor(2)) { m ->
        Column(m.fillMaxSize()) {
            PrimaryTabRow(selectedTabIndex = tab) {
                Tab(tab == 0, { tab = 0 }, text = { Text("১০০ জোড়া শব্দ") })
                Tab(tab == 1, { tab = 1 }, text = { Text("কোনটি বসবে? 🎯") })
            }
            if (tab == 1) {
                var round by remember { mutableIntStateOf(0) }
                val questions = remember(round) { fillTheBlankQuestions(app.content.pairs) }
                QuickQuiz(questions, onRestart = { round++ })
                return@Column
            }
            val pairs = app.content.pairs.filter { !homophonesOnly || it.isHomophone }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(homophonesOnly, { homophonesOnly = !homophonesOnly },
                        label = { Text("শুধু একই উচ্চারণ (homophone)") })
                }
                items(pairs, key = { it.id }) { pair ->
                    ContentCard(onClick = { nav.navigate(Routes.pair(pair.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${pair.order.bn()}.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                            Text(pair.a.word, color = SIDE_A, style = MaterialTheme.typography.titleMedium)
                            Text("  vs  ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(pair.b.word, color = SIDE_B, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            if ("pair:${pair.id}" in p.cards) Text("✓", style = MaterialTheme.typography.titleMedium)
                        }
                        if (pair.isHomophone) TagPill("একই উচ্চারণ", HomophoneAmber)
                    }
                }
            }
        }
    }
}

/** Blank the word out of each pair's example sentences and ask which of the two fits. */
fun fillTheBlankQuestions(pairs: List<ConfusedPair>, count: Int = 10): List<QuizQuestion> =
    pairs.shuffled().mapNotNull { pair ->
        val side = listOf(pair.a, pair.b).filter { it.exampleEn.contains(it.word, ignoreCase = true) }.randomOrNull() ?: return@mapNotNull null
        val blanked = side.exampleEn.replace(Regex("\\b${Regex.escape(side.word)}\\w*", RegexOption.IGNORE_CASE), "_____")
        if (!blanked.contains("_____")) return@mapNotNull null
        val opts = listOf(pair.a.word, pair.b.word)
        QuizQuestion(blanked, opts, opts.indexOf(side.word), "${pair.differenceBn}\n${side.exampleBn}", hint = "${pair.a.word} না ${pair.b.word}?")
    }.take(count)

@Composable
fun PairDetailScreen(nav: NavHostController, id: String) {
    val app = LocalApp.current
    val pair = remember(id) { app.content.pair(id) }
    val p by app.progress.state.collectAsStateWithLifecycle()
    val key = "pair:${pair.id}"
    ScreenScaffold("${pair.a.word} vs ${pair.b.word}", "পার্থক্য কী? · ${pair.order.bn()}", onBack = { nav.popBackStack() },
        accent = partColor(2), actions = {
            ShareButton { pair.toShareCard() }
            BookmarkButton(key in p.bookmarks) { app.progress.toggleBookmark(key) }
        }) { m ->
        Column(m.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (pair.isHomophone) TagPill("একই উচ্চারণ · Homophone", HomophoneAmber)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SideCard(pair.a, SIDE_A, Modifier.weight(1f)) { app.speaker.speak(pair.a.word) }
                SideCard(pair.b, SIDE_B, Modifier.weight(1f)) { app.speaker.speak(pair.b.word) }
            }
            ContentCard(accent = partColor(2)) {
                Text("পার্থক্য", style = MaterialTheme.typography.labelLarge, color = partColor(2))
                Text(pair.differenceBn, style = MaterialTheme.typography.bodyLarge)
                if (pair.mnemonic.isNotBlank()) {
                    Text("💡 মনে রাখুন: ${pair.mnemonic}", style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 8.dp))
                }
            }
            listOf(pair.a to SIDE_A, pair.b to SIDE_B).forEach { (side, color) ->
                ContentCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${side.word} ›", color = color, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                        SpeakButton({ app.speaker.speak(side.exampleEn) })
                    }
                    Text(side.exampleEn, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text(side.exampleBn, style = MaterialTheme.typography.bodyMedium)
                }
            }
            LearnChip(key in p.cards) { app.progress.learn(key) }
            Button({ nav.navigate(Routes.pair(app.content.pairs[(pair.order) % app.content.pairs.size].id)) { popUpTo("pair/{id}") { inclusive = true } } },
                Modifier.fillMaxWidth()) { Text("পরের জোড়া →") }
        }
    }
}

@Composable
private fun SideCard(side: PairSide, color: Color, modifier: Modifier, onSpeak: () -> Unit) {
    ContentCard(modifier, accent = color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(side.word, style = MaterialTheme.typography.titleLarge, color = color, modifier = Modifier.weight(1f))
            SpeakButton(onSpeak)
        }
        PosChip(side.pos, showBn = false)
        IpaText(side.ipa)
        Text(side.bnPronunciation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(side.meaningBn, style = MaterialTheme.typography.titleSmall, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
    }
}
