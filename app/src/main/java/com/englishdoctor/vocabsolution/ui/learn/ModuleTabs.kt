package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold

/** Standard module screen: title bar in the part colour plus a scrollable tab row. */
@Composable
fun ModuleTabsScreen(nav: NavHostController, module: ModuleId, tabs: List<String>, initialTab: Int = 0, content: @Composable (Int) -> Unit) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    ScreenScaffold(module.titleEn, "অধ্যায় ${module.number.bn()} · ${module.titleBn}",
        onBack = { nav.popBackStack() }, onSearch = { nav.navigate(Routes.SEARCH) }, accent = partColor(module.part)) { m ->
        Column(m.fillMaxSize()) {
            PrimaryScrollableTabRow(selectedTabIndex = tab, edgePadding = 8.dp) {
                tabs.forEachIndexed { i, t -> Tab(tab == i, { tab = i }, text = { Text(t) }) }
            }
            content(tab)
        }
    }
}

/** Horizontal filter chips; null = all. */
@Composable
fun <T> ChipFilter(options: List<T>, selected: T?, label: (T) -> String, allLabel: String = "সব", onSelect: (T?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp),
        modifier = Modifier.padding(vertical = 8.dp)) {
        item { FilterChip(selected == null, { onSelect(null) }, label = { Text(allLabel) }) }
        items(options) { o -> FilterChip(selected == o, { onSelect(if (selected == o) null else o) }, label = { Text(label(o)) }) }
    }
}
