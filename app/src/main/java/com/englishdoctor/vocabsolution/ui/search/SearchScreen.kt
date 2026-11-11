package com.englishdoctor.vocabsolution.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.data.StudyItem
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.EmptyState
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.review.ItemRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun SearchScreen(nav: NavHostController) {
    val app = LocalApp.current
    var query by rememberSaveable { mutableStateOf("") }
    var results by remember { mutableStateOf<List<StudyItem>>(emptyList()) }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }
    LaunchedEffect(query) {
        delay(150) // debounce typing
        results = withContext(Dispatchers.Default) { app.content.search(query) }
    }
    ScreenScaffold("খুঁজুন", "ইংরেজি বা বাংলায় লিখুন", onBack = { nav.popBackStack() }) { m ->
        Column(m.fillMaxSize()) {
            OutlinedTextField(query, { query = it }, singleLine = true, placeholder = { Text("যেমন: mitigate বা প্রশমিত") },
                modifier = Modifier.fillMaxWidth().padding(16.dp).focusRequester(focus))
            if (query.trim().length >= 2 && results.isEmpty()) {
                EmptyState("🔍", "কিছু পাওয়া যায়নি", "অন্য বানানে বা বাংলা অর্থ দিয়ে খুঁজে দেখুন।")
            }
            if (results.isNotEmpty()) {
                Text("${results.size.bn()}টি ফলাফল", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
            }
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(results, key = { it.key }) { item -> ItemRow(item) { openItem(nav, item, app.content.hooks.map { it.id }) } }
            }
        }
    }
}

/** Jump from a search hit to where it lives in the book. */
private fun openItem(nav: NavHostController, item: StudyItem, hookIds: List<String>) {
    val parts = item.key.split(":")
    when (parts[0]) {
        "hook" -> nav.navigate(Routes.hook(hookIds.indexOf(parts[1]).coerceAtLeast(0)))
        "pair" -> nav.navigate(Routes.pair(parts[1]))
        "root" -> nav.navigate(Routes.root(parts[1]))
        "mcq" -> nav.navigate(Routes.EXAM)
        else -> nav.navigate(Routes.module(item.module.takeIf { it != ModuleId.EXAM } ?: ModuleId.WORD_BUILDING))
    }
}
