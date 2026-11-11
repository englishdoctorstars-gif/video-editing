package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.MemoryHook
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.BookmarkButton
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.FlipCard
import com.englishdoctor.vocabsolution.ui.components.IpaText
import com.englishdoctor.vocabsolution.ui.components.LabelValue
import com.englishdoctor.vocabsolution.ui.components.LearnChip
import com.englishdoctor.vocabsolution.ui.components.PosChip
import com.englishdoctor.vocabsolution.ui.components.PosMark
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SpeakButton
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen

@Composable
fun MemoryHooksListScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    ScreenScaffold(ModuleId.MEMORY_HOOKS.titleEn, "অধ্যায় ৩ · ${ModuleId.MEMORY_HOOKS.titleBn}",
        onBack = { nav.popBackStack() }, onSearch = { nav.navigate(Routes.SEARCH) }, accent = partColor(1)) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Text("শব্দের উচ্চারণের সাথে মেলে এমন বাংলা ধ্বনি দিয়ে ছোট গল্প। গল্পটি একবার পড়ে মূল সূত্রটি মনে রাখুন, শব্দটি আপনা থেকেই মনে পড়বে।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(app.content.hooks, key = { it.id }) { h ->
                ContentCard(onClick = { nav.navigate(Routes.hook(h.order - 1)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${h.order.bn()}.", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 8.dp))
                        PosMark(h.pos)
                        Column(Modifier.weight(1f)) {
                            Text(h.word, style = MaterialTheme.typography.titleMedium)
                            Text(h.meaningBn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if ("hook:${h.id}" in p.cards) Text("✓", color = CorrectGreen, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryHookPagerScreen(nav: NavHostController, start: Int) {
    val app = LocalApp.current
    val hooks = app.content.hooks
    val pager = rememberPagerState(initialPage = start.coerceIn(0, hooks.lastIndex)) { hooks.size }
    ScreenScaffold("Memory Hooks", "${(pager.currentPage + 1).bn()} / ${hooks.size.bn()} · বাম-ডানে সোয়াইপ করুন",
        onBack = { nav.popBackStack() }, accent = partColor(1)) { m ->
        HorizontalPager(pager, m.fillMaxSize(), key = { hooks[it].id }) { page -> StoryCard(hooks[page]) }
    }
}

@Composable
private fun StoryCard(h: MemoryHook) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    var flipped by rememberSaveable(h.id) { mutableStateOf(false) }
    val key = "hook:${h.id}"
    var note by remember(h.id) { mutableStateOf(p.notes[key] ?: "") }
    val accent = partColor(1)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FlipCard(
            flipped = flipped,
            onFlip = { flipped = !flipped },
            front = {
                // Front: the word only, so the learner tries to recall the meaning first.
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(h.word, style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                    SpeakButton({ app.speaker.speak(h.word) })
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PosChip(h.pos)
                    IpaText(h.ipa)
                }
                Text(h.bnPronunciation, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(24.dp))
                Text("অর্থ মনে আছে? কার্ডে ট্যাপ করে গল্পটি দেখুন 👆", color = MaterialTheme.colorScheme.primary)
            },
            back = {
                Text(h.word, style = MaterialTheme.typography.headlineSmall, color = accent)
                Text(h.meaningBn, style = MaterialTheme.typography.titleLarge)
                Text(h.meaningEn, style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                LabelValue("ধ্বনি-সূত্র", "${h.soundSplit}  →  ${h.soundHookBn}", valueColor = accent)
                LabelValue("গল্প", h.storyBn)
                LabelValue("মূল সূত্র 🔑", h.keyCueBn, valueColor = MaterialTheme.colorScheme.secondary)
                Text("আবার ট্যাপ করলে শব্দে ফিরবেন", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
        )
        ContentCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("উদাহরণ", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                SpeakButton({ app.speaker.speak(h.exampleEn) })
            }
            Text(h.exampleEn, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(h.exampleBn, style = MaterialTheme.typography.bodyMedium)
            if (h.synonyms.isNotEmpty()) {
                Text("Synonyms: ${h.synonyms.joinToString(", ")}", color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(top = 6.dp))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            LearnChip(key in p.cards) { app.progress.learn(key) }
            Spacer(Modifier.weight(1f))
            BookmarkButton(key in p.bookmarks) { app.progress.toggleBookmark(key) }
        }
        OutlinedTextField(
            value = note,
            onValueChange = { note = it; app.progress.setNote(key, it) },
            label = { Text("নিজের Memory Hook লিখুন (ঐচ্ছিক)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
        )
    }
}
