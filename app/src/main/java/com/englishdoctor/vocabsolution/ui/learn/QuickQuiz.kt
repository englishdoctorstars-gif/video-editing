package com.englishdoctor.vocabsolution.ui.learn

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.englishdoctor.vocabsolution.data.OPTION_LABELS
import com.englishdoctor.vocabsolution.data.OPTION_KEYS
import com.englishdoctor.vocabsolution.notify.bn
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.WrongRed

data class QuizQuestion(
    val prompt: String,
    val options: List<String>,
    val correct: Int,
    val explanation: String = "",
    val hint: String = "",
)

/** Builds a 4-option question from a correct answer and a pool of distractors. */
fun <T> makeQuestion(prompt: String, answer: String, pool: List<T>, label: (T) -> String, explanation: String = "", hint: String = ""): QuizQuestion {
    val distractors = pool.map(label).filter { it.isNotBlank() && !it.equals(answer, true) }.distinct().shuffled().take(3)
    val opts = (distractors + answer).shuffled()
    return QuizQuestion(prompt, opts, opts.indexOf(answer), explanation, hint)
}

/** Practice quiz with instant feedback; shows a score summary at the end. */
@Composable
fun QuickQuiz(
    questions: List<QuizQuestion>,
    onRestart: () -> Unit,
    onAnswered: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
    onIndexChange: (Int) -> Unit = {},
) {
    var index by remember(questions) { mutableIntStateOf(0) }
    var chosen by remember(questions) { mutableStateOf<Int?>(null) }
    var score by remember(questions) { mutableIntStateOf(0) }

    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (questions.isEmpty()) {
            Text("প্রশ্ন তৈরি করা যায়নি।")
            return
        }
        if (index >= questions.size) {
            val pct = score * 100 / questions.size
            Text(if (pct >= 80) "দারুণ! 🎉" else if (pct >= 50) "ভালো হয়েছে 👍" else "চিন্তা নেই, ভুল থেকেই শেখা হয় 💪",
                style = MaterialTheme.typography.headlineSmall)
            Text("স্কোর: ${score.bn()} / ${questions.size.bn()}", style = MaterialTheme.typography.titleLarge)
            Button({ onRestart(); onIndexChange(0) }, Modifier.fillMaxWidth()) { Text("আবার খেলুন") }
            return
        }
        val q = questions[index]
        LinearProgressIndicator(progress = { index.toFloat() / questions.size }, modifier = Modifier.fillMaxWidth())
        Text("প্রশ্ন ${(index + 1).bn()} / ${questions.size.bn()}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (q.hint.isNotBlank()) Text(q.hint, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        Text(q.prompt, style = MaterialTheme.typography.titleLarge)
        q.options.forEachIndexed { i, opt ->
            val state = when {
                chosen == null -> null
                i == q.correct -> true
                i == chosen -> false
                else -> null
            }
            OptionCard("${OPTION_LABELS[OPTION_KEYS[i]]}) $opt", state) {
                if (chosen == null) {
                    chosen = i
                    val ok = i == q.correct
                    if (ok) score++
                    onAnswered(ok)
                }
            }
        }
        if (chosen != null) {
            val ok = chosen == q.correct
            Text(if (ok) "✓ সঠিক!" else "✗ সঠিক উত্তর: ${q.options[q.correct]}", color = if (ok) CorrectGreen else WrongRed,
                style = MaterialTheme.typography.titleMedium)
            if (q.explanation.isNotBlank()) Text(q.explanation, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button({ index++; chosen = null; onIndexChange(index) }, Modifier.fillMaxWidth()) {
                Text(if (index + 1 < questions.size) "পরের প্রশ্ন" else "ফলাফল দেখুন")
            }
        }
    }
}

/** An answer option; [state] true = correct (green), false = wrong (red), null = neutral. */
@Composable
fun OptionCard(text: String, state: Boolean?, selected: Boolean = false, onClick: () -> Unit) {
    val color = when (state) {
        true -> CorrectGreen
        false -> WrongRed
        null -> if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
    }
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(if (state != null || selected) 2.dp else 1.dp, color),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (state != null || selected) color.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surface,
        ),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (state == true) Text("✓", color = CorrectGreen, style = MaterialTheme.typography.titleLarge)
            if (state == false) Text("✗", color = WrongRed, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun RestartRow(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick, Modifier.fillMaxWidth()) { Text(text, textAlign = TextAlign.Center) }
}
