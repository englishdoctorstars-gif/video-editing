package com.englishdoctor.vocabsolution.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.englishdoctor.vocabsolution.MainActivity
import com.englishdoctor.vocabsolution.VocabApp
import com.englishdoctor.vocabsolution.data.MemoryHook
import com.englishdoctor.vocabsolution.ui.theme.Pos
import java.time.LocalDate

private val NAVY = Color(0xFF0D2C54)
private val GOLD = Color(0xFFF2A900)
private val WHITE = Color(0xFFFFFFFF)
private val SOFT = Color(0xCCFFFFFF)

const val EXTRA_OPEN_HOOK = "open_hook"

/** Home-screen "আজকের শব্দ": the same Word of the Day as the app's home screen. */
class WordWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val content = (context.applicationContext as VocabApp).container.content
        val hook = content.wordOfTheDay(LocalDate.now().toEpochDay())
        val open = Intent(context, MainActivity::class.java)
            .putExtra(EXTRA_OPEN_HOOK, hook.order - 1)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        provideContent { WidgetBody(hook, actionStartActivity(open)) }
    }

    companion object {
        /** Refresh all placed widgets (called on app start and by the daily worker). */
        suspend fun refresh(context: Context) = runCatching { WordWidget().updateAll(context) }
    }
}

@Composable
internal fun WidgetBody(h: MemoryHook, onClick: Action?) {
    val pos = Pos.of(h.pos)
    val base = GlanceModifier.fillMaxSize().background(NAVY).cornerRadius(20.dp).padding(14.dp)
    Column(
        if (onClick != null) base.clickable(onClick) else base,
        verticalAlignment = Alignment.Vertical.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text("আজকের শব্দ", style = TextStyle(color = ColorProvider(GOLD), fontSize = 12.sp, fontWeight = FontWeight.Bold))
            Text("  ·  Vocabsolution", style = TextStyle(color = ColorProvider(SOFT), fontSize = 12.sp))
        }
        Spacer(GlanceModifier.height(4.dp))
        Row(verticalAlignment = Alignment.Vertical.CenterVertically) {
            Text(h.word, style = TextStyle(color = ColorProvider(WHITE), fontSize = 24.sp, fontWeight = FontWeight.Bold))
            Text("  ${pos.symbol} ${pos.bn}", style = TextStyle(color = ColorProvider(pos.dark), fontSize = 13.sp, fontWeight = FontWeight.Bold))
        }
        Text(h.meaningBn, style = TextStyle(color = ColorProvider(WHITE), fontSize = 16.sp, fontWeight = FontWeight.Bold), maxLines = 2)
        Text("ধ্বনি-সূত্র: ${h.soundHookBn}", style = TextStyle(color = ColorProvider(SOFT), fontSize = 13.sp), maxLines = 1)
        Text("ট্যাপ করে গল্পটি পড়ুন →", style = TextStyle(color = ColorProvider(GOLD), fontSize = 12.sp))
    }
}

class WordWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WordWidget()
}
