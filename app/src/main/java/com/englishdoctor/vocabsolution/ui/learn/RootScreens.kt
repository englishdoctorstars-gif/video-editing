package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.data.RootWord
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.PosLegend
import com.englishdoctor.vocabsolution.ui.components.PosMark
import com.englishdoctor.vocabsolution.ui.components.ProgressRing
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SectionTitle
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.Pos
import com.englishdoctor.vocabsolution.ui.theme.color

@Composable
fun RootListScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    val color = partColor(1)
    val families = app.content.roots.filter { query.isBlank() || it.root.contains(query.trim(), true) }
    ScreenScaffold(ModuleId.ROOT_POWER.titleEn, "অধ্যায় ২ · ${ModuleId.ROOT_POWER.titleBn}",
        onBack = { nav.popBackStack() }, onSearch = { nav.navigate(Routes.SEARCH) }, accent = color) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("একটি মূল শব্দ জানলেই তার পুরো পরিবার চেনা যায়: আগে মূল + প্রত্যয়, তারপর উপসর্গ + মূল।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { PosLegend() }
            item {
                OutlinedTextField(query, { query = it }, label = { Text("মূল শব্দ খুঁজুন (যেমন ACT)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
            }
            items(families, key = { it.id }) { f ->
                val learned = (f.rootPlusSuffix + f.prefixPlusRoot).count { "root:${f.id}:${it.word}" in p.cards }
                ContentCard(onClick = { nav.navigate(Routes.root(f.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(f.root, style = MaterialTheme.typography.titleLarge, color = color)
                            Text("${f.size.bn()}টি শব্দ · শেখা ${learned.bn()}টি", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ProgressRing(learned.toFloat() / f.size, color, size = 40.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun RootDetailScreen(nav: NavHostController, id: String) {
    val app = LocalApp.current
    val f = remember(id) { app.content.root(id) }
    val p by app.progress.state.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    val color = partColor(1)
    fun keep(w: RootWord) = filter == null || w.pos == filter

    ScreenScaffold(f.root, "শব্দ-পরিবার · ${f.size.bn()}টি শব্দ", onBack = { nav.popBackStack() }, accent = color) { m ->
        LazyColumn(m.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FilterChip(filter == null, { filter = null }, label = { Text("সব") }) }
                    items(listOf("noun", "verb", "adjective", "adverb")) { pos ->
                        val ps = Pos.of(pos)
                        FilterChip(filter == pos, { filter = if (filter == pos) null else pos },
                            label = { Text("${ps.symbol} ${ps.bn}", color = ps.color()) })
                    }
                }
            }
            item {
                val new = (f.rootPlusSuffix + f.prefixPlusRoot).map { "root:${f.id}:${it.word}" }.filter { it !in p.cards }
                if (new.isNotEmpty()) {
                    Button({ new.take(10).forEach(app.progress::learn) }, Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text("পরের ${minOf(10, new.size).bn()}টি শব্দ রিভিউতে যোগ করুন")
                    }
                }
            }
            listOf("মূল + প্রত্যয়" to f.rootPlusSuffix, "উপসর্গ + মূল" to f.prefixPlusRoot).forEach { (title, words) ->
                val shown = words.filter(::keep)
                if (shown.isNotEmpty()) {
                    item { SectionTitle("$title · ${shown.size.bn()}টি", color = color) }
                    items(shown, key = { title + it.word }) { w ->
                        val key = "root:${f.id}:${w.word}"
                        RootWordRow(w, learned = key in p.cards, onSpeak = { app.speaker.speak(w.word) }, onLearn = { app.progress.learn(key) })
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun RootWordRow(w: RootWord, learned: Boolean, onSpeak: () -> Unit, onLearn: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onSpeak).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        PosMark(w.pos)
        Column(Modifier.weight(1f)) {
            Text(w.word, style = MaterialTheme.typography.titleSmall, color = Pos.of(w.pos).color())
            Text(w.meaningBn, style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onLearn, enabled = !learned) {
            if (learned) Icon(Icons.Filled.CheckCircle, "রিভিউতে আছে", tint = CorrectGreen)
            else Icon(Icons.Outlined.AddCircleOutline, "রিভিউতে যোগ করুন", tint = MaterialTheme.colorScheme.primary)
        }
    }
}
