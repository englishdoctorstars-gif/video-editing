package com.englishdoctor.vocabsolution.ui.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.englishdoctor.vocabsolution.data.MCQ_TYPE_BN
import com.englishdoctor.vocabsolution.data.Mcq
import com.englishdoctor.vocabsolution.data.OPTION_KEYS
import com.englishdoctor.vocabsolution.data.OPTION_LABELS
import com.englishdoctor.vocabsolution.domain.ExamRules
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.LocalApp
import com.englishdoctor.vocabsolution.ui.Routes
import com.englishdoctor.vocabsolution.ui.components.ContentCard
import com.englishdoctor.vocabsolution.ui.components.ProgressRing
import com.englishdoctor.vocabsolution.ui.components.ScreenScaffold
import com.englishdoctor.vocabsolution.ui.components.SectionTitle
import com.englishdoctor.vocabsolution.ui.learn.OptionCard
import com.englishdoctor.vocabsolution.ui.learn.partColor
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.WrongRed
import kotlinx.coroutines.delay

@Composable
fun ExamHubScreen(nav: NavHostController) {
    val app = LocalApp.current
    val p by app.progress.state.collectAsStateWithLifecycle()
    val chooseSet = remember { mutableStateOf<Int?>(null) }
    val color = partColor(4)

    ScreenScaffold("Exam Ready", "চাকরির পরীক্ষার ১০টি MCQ সেট", accent = color) { m ->
        LazyColumn(m, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Text("প্রতিটি সেট ২০ মিনিটে শেষ করুন। ২৫-এর মধ্যে ২০টির বেশি সঠিক হলে পরের স্তর খুলে যাবে। ভুল উত্তরগুলো স্বয়ংক্রিয়ভাবে রিভিশনে যোগ হবে।",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            ExamRules.LEVEL_SETS.forEach { (level, sets) ->
                val unlocked = ExamRules.isLevelUnlocked(level, p.examBest)
                item {
                    SectionTitle("${ExamRules.LEVEL_BN[level]} · সেট ${sets.first.bn()}–${sets.last.bn()}", color = color)
                    Text(ExamRules.LEVEL_HINT.getValue(level), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                sets.forEach { set ->
                    item {
                        val best = p.examBest[set]
                        ContentCard(accent = if (unlocked) color else null, onClick = { if (unlocked) chooseSet.value = set }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                ProgressRing((best ?: 0) / 25f, if ((best ?: 0) > ExamRules.PASS_ABOVE) CorrectGreen else color, size = 44.dp) {
                                    if (unlocked) Text(set.bn(), color = color, style = MaterialTheme.typography.titleSmall)
                                    else Icon(Icons.Filled.Lock, "বন্ধ", Modifier.size(18.dp))
                                }
                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                    Text("MCQ সেট ${set.bn()}", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        when {
                                            !unlocked -> "আগের স্তরে ২০-এর বেশি পেলে খুলবে"
                                            best == null -> "২৫টি প্রশ্ন · ২০ মিনিট"
                                            else -> "সেরা স্কোর: ${best.bn()}/২৫"
                                        },
                                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            if (p.examHistory.isNotEmpty()) {
                item { SectionTitle("সাম্প্রতিক ফলাফল") }
                item {
                    p.examHistory.takeLast(5).reversed().forEach { a ->
                        Text("সেট ${a.set.bn()} · ${a.score.bn()}/২৫", modifier = Modifier.padding(vertical = 2.dp))
                    }
                }
            }
        }
    }
    chooseSet.value?.let { set ->
        AlertDialog(
            onDismissRequest = { chooseSet.value = null },
            title = { Text("MCQ সেট ${set.bn()}") },
            text = { Text("পরীক্ষা মোড: ২০ মিনিটের টাইমার, শেষে ফলাফল।\nঅনুশীলন মোড: প্রতিটি উত্তরের পরই সঠিক উত্তর দেখুন।") },
            confirmButton = { Button({ chooseSet.value = null; nav.navigate(Routes.examSession(set, "exam")) }) { Text("পরীক্ষা ⏱️") } },
            dismissButton = { TextButton({ chooseSet.value = null; nav.navigate(Routes.examSession(set, "practice")) }) { Text("অনুশীলন") } },
        )
    }
}

class ExamSession : ViewModel() {
    var started = false
    val answers = mutableStateMapOf<Int, String>()
    val flagged = mutableStateMapOf<Int, Boolean>()
    var current by mutableIntStateOf(0)
    var secondsLeft by mutableIntStateOf(ExamRules.MINUTES * 60)
    var submitted by mutableStateOf(false)
}

@Composable
fun ExamSessionScreen(nav: NavHostController, set: Int, mode: String) {
    val app = LocalApp.current
    val questions = remember(set) { app.content.mcqSet(set) }
    val s: ExamSession = viewModel(key = "exam-$set-$mode")
    val practice = mode == "practice"
    var confirmSubmit by remember { mutableStateOf(false) }

    fun submit() {
        if (s.submitted) return
        s.submitted = true
        val score = questions.count { s.answers[it.number] == it.answer }
        val byType = questions.groupBy { it.type }.mapValues { (_, qs) -> qs.count { s.answers[it.number] == it.answer } }
        val wrong = questions.filter { s.answers[it.number] != it.answer }.map { "mcq:${it.id}" }
        // Practice runs do not count towards unlocking levels; their mistakes still go to review.
        if (practice) wrong.forEach(app.progress::learn) else app.progress.recordExam(set, score, byType, wrong)
    }

    if (!practice) {
        LaunchedEffect(s.submitted) {
            while (!s.submitted && s.secondsLeft > 0) {
                delay(1000)
                s.secondsLeft--
            }
            if (s.secondsLeft <= 0) submit()
        }
    }
    val timer = "%s:%s".format((s.secondsLeft / 60).bn().padStart(2, '০'), (s.secondsLeft % 60).bn().padStart(2, '০'))
    ScreenScaffold(
        "MCQ সেট ${set.bn()}",
        if (s.submitted) "ফলাফল" else if (practice) "অনুশীলন মোড" else "⏱️ $timer বাকি",
        onBack = { nav.popBackStack() },
        accent = partColor(4),
    ) { m ->
        if (s.submitted && !practice) {
            ExamResult(questions, s, m) { nav.popBackStack() }
            return@ScreenScaffold
        }
        val q = questions[s.current]
        Column(m.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LinearProgressIndicator(progress = { s.answers.size / questions.size.toFloat() }, Modifier.fillMaxWidth())
            // Question navigator
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                questions.forEachIndexed { i, qq ->
                    val answered = s.answers.containsKey(qq.number)
                    val label = (i + 1).bn() + if (s.flagged[qq.number] == true) "⚑" else ""
                    if (i == s.current) Button({ s.current = i }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(label) }
                    else if (answered) FilledTonalButton({ s.current = i }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(label) }
                    else OutlinedButton({ s.current = i }, contentPadding = PaddingValues(horizontal = 8.dp)) { Text(label) }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${q.number.bn()}. ${MCQ_TYPE_BN[q.type] ?: ""}", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                if (!practice) IconButton({ s.flagged[q.number] = s.flagged[q.number] != true }) {
                    Icon(if (s.flagged[q.number] == true) Icons.Filled.Flag else Icons.Outlined.Flag, "পরে দেখব")
                }
            }
            Text(q.question, style = MaterialTheme.typography.titleLarge)
            val chosen = s.answers[q.number]
            val reveal = practice && chosen != null
            OPTION_KEYS.forEach { k ->
                val text = q.options[k] ?: return@forEach
                val state = if (!reveal) null else if (k == q.answer) true else if (k == chosen) false else null
                OptionCard("${OPTION_LABELS[k]}) $text", state, selected = !practice && chosen == k) {
                    if (practice && chosen != null) return@OptionCard
                    s.answers[q.number] = k
                }
            }
            if (reveal) {
                val ok = chosen == q.answer
                Text(if (ok) "✓ সঠিক!" else "✗ সঠিক উত্তর: ${OPTION_LABELS[q.answer]}) ${q.options[q.answer]}",
                    color = if (ok) CorrectGreen else WrongRed, style = MaterialTheme.typography.titleMedium)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton({ s.current-- }, Modifier.weight(1f), enabled = s.current > 0) { Text("← আগের") }
                if (s.current < questions.lastIndex) Button({ s.current++ }, Modifier.weight(1f)) { Text("পরের →") }
                else Button({ if (practice) { submit(); nav.popBackStack() } else confirmSubmit = true }, Modifier.weight(1f)) {
                    Text(if (practice) "শেষ করুন" else "জমা দিন")
                }
            }
            if (!practice && s.current < questions.lastIndex) {
                TextButton({ confirmSubmit = true }, Modifier.fillMaxWidth()) { Text("এখনই জমা দিন") }
            }
        }
    }
    if (confirmSubmit) {
        val left = questions.size - s.answers.size
        AlertDialog(
            onDismissRequest = { confirmSubmit = false },
            title = { Text("জমা দেবেন?") },
            text = { Text(if (left > 0) "${left.bn()}টি প্রশ্নের উত্তর দেওয়া হয়নি।" else "সব প্রশ্নের উত্তর দেওয়া হয়েছে।") },
            confirmButton = { Button({ confirmSubmit = false; submit() }) { Text("জমা দিন") } },
            dismissButton = { TextButton({ confirmSubmit = false }) { Text("ফিরে যান") } },
        )
    }
}

@Composable
private fun ExamResult(questions: List<Mcq>, s: ExamSession, modifier: Modifier, onDone: () -> Unit) {
    val score = questions.count { s.answers[it.number] == it.answer }
    val passed = score > ExamRules.PASS_ABOVE
    LazyColumn(modifier, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            ContentCard(accent = if (passed) CorrectGreen else partColor(4)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProgressRing(score / 25f, if (passed) CorrectGreen else partColor(4), size = 80.dp, stroke = 8.dp) {
                        Text("${score.bn()}/২৫", style = MaterialTheme.typography.titleMedium)
                    }
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(if (passed) "অভিনন্দন! 🎉" else "আরেকটু চেষ্টা 💪", style = MaterialTheme.typography.headlineSmall)
                        Text(if (passed) "পরের স্তর খুলে গেছে।" else "পরের স্তর খুলতে ২০-এর বেশি লাগবে।")
                    }
                }
            }
        }
        item { SectionTitle("ধরনভিত্তিক বিশ্লেষণ") }
        item {
            questions.groupBy { it.type }.forEach { (type, qs) ->
                val ok = qs.count { s.answers[it.number] == it.answer }
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text(MCQ_TYPE_BN[type] ?: type, Modifier.weight(1f))
                    Text("${ok.bn()}/${qs.size.bn()}", color = if (ok == qs.size) CorrectGreen else if (ok * 2 < qs.size) WrongRed else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
        item { SectionTitle("উত্তরপত্র") }
        questions.forEach { q ->
            item(key = q.id) {
                val chosen = s.answers[q.number]
                val ok = chosen == q.answer
                ContentCard {
                    Text("${q.number.bn()}. ${q.question}", style = MaterialTheme.typography.titleSmall)
                    Text("সঠিক: ${OPTION_LABELS[q.answer]}) ${q.options[q.answer]}", color = CorrectGreen)
                    if (!ok) Text(if (chosen == null) "উত্তর দেওয়া হয়নি" else "আপনার উত্তর: ${OPTION_LABELS[chosen]}) ${q.options[chosen]}", color = WrongRed)
                }
            }
        }
        item { Spacer(Modifier.height(8.dp)); Button(onDone, Modifier.fillMaxWidth()) { Text("ঠিক আছে") } }
    }
}
