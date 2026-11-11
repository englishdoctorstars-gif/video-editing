package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.ModuleId
import com.englishdoctor.vocabsolution.data.PART_TITLES
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.ProgressRing
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SectionTitle
import com.englishdoctor.vocabsolution.ui.openModule
import com.englishdoctor.vocabsolution.ui.theme.BookPart
import com.englishdoctor.vocabsolution.ui.theme.tint

@Composable
@ReadOnlyComposable
fun partColor(part: Int): Color = BookPart.entries[part - 1].tint()

val MODULE_BLURB = mapOf(
    ModuleId.WORD_BUILDING to "৪৮ প্রত্যয় · ৪৮ উপসর্গ · ২০ বিশ্লেষণ · ১২০ অনুশীলন",
    ModuleId.ROOT_POWER to "৯৬টি শব্দ-পরিবার · ২,৬৯৪টি শব্দ",
    ModuleId.MEMORY_HOOKS to "১০০টি কঠিন শব্দ, গল্প ও ধ্বনি-সূত্রে",
    ModuleId.CONFUSED to "১০০ জোড়া কাছাকাছি শব্দের পার্থক্য",
    ModuleId.EQUIVALENTS to "১৪৪ সমার্থক-বিপরীত · ১৩০ এক কথায় প্রকাশ",
    ModuleId.COLLOCATIONS to "২০০ শব্দজুটি ও ২৫টি প্রচলিত ভুল",
    ModuleId.LISTEN to "IELTS Listening-এর শব্দ, বানানের ফাঁদ",
    ModuleId.READ to "১৫০টি প্যাসেজ · ১,৫০০ একাডেমিক শব্দ",
    ModuleId.PARAPHRASE to "১,১৫২টি collocation ও paraphrase",
    ModuleId.WRITE to "Task 1 ও Task 2-এর উচ্চ Band-এর ভাষা",
    ModuleId.EXAM to "১০টি সেট · ২৫০টি MCQ · ৩টি স্তর",
)

@Composable
fun LearnHubScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val totals = remember { app.content.studyItems.groupingBy { it.module }.eachCount() }
    val learned = remember(p.cards) { p.cards.keys.mapNotNull { app.content.item(it)?.module }.groupingBy { it }.eachCount() }

    ScreenScaffold("শিখুন", "৪টি পর্ব · ১১টি অধ্যায়", onSearch = { nav.navigate(Routes.SEARCH) }) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ModuleId.entries.groupBy { it.part }.forEach { (part, modules) ->
                item { SectionTitle(PART_TITLES.getValue(part), color = partColor(part)) }
                modules.forEach { mod ->
                    item {
                        val color = partColor(mod.part)
                        ContentCard(accent = color, onClick = { if (mod.available) nav.openModule(mod) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val total = totals[mod] ?: 0
                                val done = learned[mod] ?: 0
                                if (mod.available) {
                                    ProgressRing(if (mod == ModuleId.EXAM) (p.examBest.size / 10f) else if (total == 0) 0f else done.toFloat() / total, color) {
                                        Text(mod.number.bn(), style = MaterialTheme.typography.titleMedium, color = color)
                                    }
                                } else {
                                    ProgressRing(0f, color) { Icon(Icons.Filled.Lock, "শীঘ্রই আসছে", tint = color) }
                                }
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(mod.titleEn, style = MaterialTheme.typography.titleMedium, color = color)
                                    Text(mod.titleBn, style = MaterialTheme.typography.bodyMedium)
                                    Text(if (mod.available) MODULE_BLURB.getValue(mod) else "শীঘ্রই আসছে · ${MODULE_BLURB.getValue(mod)}",
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
