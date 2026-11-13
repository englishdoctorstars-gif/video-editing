package com.englishdoctor.vocabsolution.store

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.android.resources.Density
import com.englishdoctor.vocabsolution.R
import com.englishdoctor.vocabsolution.ui.theme.BrandGold
import com.englishdoctor.vocabsolution.ui.theme.BrandNavy
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.HindSiliguri
import com.englishdoctor.vocabsolution.ui.theme.Pos
import com.englishdoctor.vocabsolution.ui.theme.WrongRed
import org.junit.Rule
import org.junit.Test
import java.io.File
import kotlin.math.cos
import kotlin.math.sin

/*
 * The all-features ad for the English Doctor page: 1:1, 1080×1080 (540×540dp at xhdpi).
 * Backgrounds are opaque; every other layer is a transparent full-frame PNG animated by
 * tools/marketing/build_combo.py. Positions below are the final resting positions.
 */

private fun ts(size: TextUnit, color: Color = Color.White, weight: FontWeight = FontWeight.Bold, align: TextAlign = TextAlign.Center) =
    TextStyle(fontFamily = HindSiliguri, fontSize = size, color = color, fontWeight = weight, textAlign = align, lineHeight = size * 1.28f)

private val Night1 = Color(0xFF070B1E)
private val Night2 = Color(0xFF1B1F4B)
private val Card = Color(0xFF141A3C)
private val Teal = Color(0xFF26A69A)
private val Orange = Color(0xFFFF8F00)

@Composable private fun Full(content: @Composable BoxScope.() -> Unit) = Box(Modifier.fillMaxSize(), content = content)

@Composable
private fun Bg(top: Color, bottom: Color, glow1: Color, glow2: Color) = Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
    Box(Modifier.size(520.dp).offset((-170).dp, (-170).dp).background(Brush.radialGradient(listOf(glow1.copy(alpha = 0.33f), Color.Transparent))))
    Box(Modifier.size(560.dp).offset(230.dp, 220.dp).background(Brush.radialGradient(listOf(glow2.copy(alpha = 0.28f), Color.Transparent))))
    Canvas(Modifier.fillMaxSize()) {
        val step = 24.dp.toPx()
        var y = step / 2
        while (y < size.height) {
            var x = step / 2
            while (x < size.width) { drawCircle(Color.White.copy(alpha = 0.055f), 1.3f, Offset(x, y)); x += step }
            y += step
        }
    }
}

/** Places a child centred on (x, y) dp. */
@Composable
private fun At(x: Dp, y: Dp, content: @Composable () -> Unit) = Layout(content, Modifier.fillMaxSize()) { ms, c ->
    val p = ms.first().measure(c.copy(minWidth = 0, minHeight = 0))
    layout(c.maxWidth, c.maxHeight) { p.place(x.roundToPx() - p.width / 2, y.roundToPx() - p.height / 2) }
}

@Composable
private fun Pill(text: String, bg: Color = Color.White.copy(alpha = 0.12f), fg: Color = Color.White, size: TextUnit = 16.sp, modifier: Modifier = Modifier) =
    Text(text, style = ts(size, fg), modifier = modifier.background(bg, RoundedCornerShape(50)).border(1.dp, fg.copy(alpha = 0.3f), RoundedCornerShape(50))
        .padding(horizontal = 16.dp, vertical = 5.dp))

@Composable
private fun Glass(modifier: Modifier = Modifier, border: Color = Color.White.copy(alpha = 0.22f), content: @Composable () -> Unit) =
    Box(modifier.shadow(16.dp, RoundedCornerShape(20.dp)).background(Card.copy(alpha = 0.94f), RoundedCornerShape(20.dp))
        .border(1.5.dp, border, RoundedCornerShape(20.dp)).padding(horizontal = 18.dp, vertical = 12.dp)) { content() }

private fun screen(name: String) = listOf("../marketing/ad-screens", "marketing/ad-screens").map { File(it, "$name.png") }.first { it.exists() }
    .readBytes().let { BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap() }

@Composable
private fun Phone(name: String, width: Dp, modifier: Modifier = Modifier) =
    Box(modifier.width(width).height(width * 2.222f + 14.dp).shadow(26.dp, RoundedCornerShape(width / 6.5f))
        .background(Color(0xFF0B0B0F), RoundedCornerShape(width / 6.5f)).border(2.dp, Color(0xFF3A3F55), RoundedCornerShape(width / 6.5f)).padding(7.dp)) {
        Image(screen(name), null, Modifier.fillMaxSize().clip(RoundedCornerShape(width / 7.8f)), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter)
    }

/** Chapter heading used by every feature scene: "০১ · শব্দ ব্যবচ্ছেদ" + a one-line promise. */
@Composable
private fun Heading(no: String, kicker: String, title: String) = Full {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Pill("$no · $kicker", bg = BrandGold, fg = BrandNavy, size = 16.sp)
        Spacer(Modifier.height(8.dp))
        Text(title, style = ts(32.sp))
    }
}

@Composable
private fun Footer(text: String) = Full {
    Text(text, style = ts(18.sp, Color.White.copy(alpha = 0.9f), FontWeight.SemiBold), modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 26.dp)
        .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(50)).padding(horizontal = 18.dp, vertical = 6.dp))
}

// ---- 0 · hook ---------------------------------------------------------------------------------

@Composable private fun HookLine1() = Full {
    Text("২০ বার লিখে মুখস্থ…", style = ts(40.sp), modifier = Modifier.align(Alignment.TopCenter).padding(top = 120.dp))
}
@Composable private fun HookLine2() = Full {
    Text("পরের দিন সব ভুলে যান? 😩", style = ts(36.sp, BrandGold), modifier = Modifier.align(Alignment.TopCenter).padding(top = 200.dp))
}
@Composable private fun HookAudience() = Full {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 300.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("বিসিএস · ব্যাংক · IELTS · HSC · অফিস —", style = ts(20.sp, Color.White.copy(alpha = 0.85f), FontWeight.SemiBold))
        Text("সমস্যা সবার এক", style = ts(26.sp))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("🏛️", "🏦", "🎓", "📚", "💼").forEach {
                Box(Modifier.size(54.dp).background(Color.White.copy(alpha = 0.1f), CircleShape).border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center) { Text(it, style = ts(26.sp)) }
            }
        }
    }
}

// ---- 1 · turn ---------------------------------------------------------------------------------

@Composable private fun TurnLine() = Full {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 64.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("সমাধান:", style = ts(24.sp, Color.White.copy(alpha = 0.85f)))
        Text("মুখস্থ নয়, বুঝে শেখা", style = ts(40.sp, BrandGold))
    }
}
@Composable private fun TurnLogo() = Full {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 200.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(150.dp).shadow(30.dp, CircleShape).background(BrandNavy, CircleShape).border(3.dp, BrandGold, CircleShape), contentAlignment = Alignment.Center) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(150.dp))
        }
        Spacer(Modifier.height(10.dp))
        Text("Vocabsolution", style = ts(40.sp))
        Text("by English Doctor", style = ts(20.sp, BrandGold))
    }
}

// ---- 2 · dissection -------------------------------------------------------------------------

@Composable private fun DissWord() = Full {
    Text("Dehumanization", style = ts(50.sp), modifier = Modifier.align(Alignment.TopCenter).padding(top = 150.dp))
}

@Composable private fun DissParts() = Full {
    Row(Modifier.align(Alignment.TopCenter).padding(top = 250.dp), verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(Triple("de", "উপসর্গ", Teal), Triple("human", "মূল", Color(0xFF42A5F5)), Triple("ize", "প্রত্যয়", Orange), Triple("ation", "প্রত্যয়", Color(0xFFAB47BC)))
            .forEachIndexed { i, (part, label, c) ->
                if (i > 0) Text("+", style = ts(30.sp, Color.White.copy(alpha = 0.7f)), modifier = Modifier.padding(top = 6.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(part, style = ts(28.sp), modifier = Modifier.shadow(10.dp, RoundedCornerShape(14.dp)).background(c, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp))
                    Text(label, style = ts(15.sp, c), modifier = Modifier.padding(top = 4.dp))
                }
            }
    }
}

@Composable private fun DissMeaning() = Full {
    Column(Modifier.align(Alignment.TopCenter).padding(top = 360.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("= অমানবিকীকরণ", style = ts(34.sp, BrandGold))
        Spacer(Modifier.height(6.dp))
        Pill("■ n · বিশেষ্য — শেষে -ation, তাই বিশেষ্য", bg = Pos.NOUN.light.copy(alpha = 0.25f), size = 15.sp)
    }
}

// ---- 3 · root power -------------------------------------------------------------------------

private val RingWords = listOf(
    "action" to "noun", "active" to "adjective", "react" to "verb", "actually" to "adverb",
    "activity" to "noun", "inactive" to "adjective", "activate" to "verb", "interact" to "verb",
)
private const val CX = 270f
private const val CY = 300f
private const val RING_R = 165f

private fun ringPos(i: Int): Pair<Float, Float> {
    val a = Math.toRadians(-90.0 + i * 45.0)
    return (CX + RING_R * 1.18f * cos(a)).toFloat() to (CY + RING_R * 0.78f * sin(a)).toFloat()
}

@Composable private fun RootLines() = Full {
    Canvas(Modifier.fillMaxSize()) {
        RingWords.indices.forEach { i ->
            val (x, y) = ringPos(i)
            drawLine(Color.White.copy(alpha = 0.28f), Offset(CX * density, CY * density), Offset(x * density, y * density), 2.dp.toPx())
        }
    }
}

@Composable private fun RootCenter() = Full {
    At(CX.dp, CY.dp) {
        Box(Modifier.size(104.dp).shadow(24.dp, CircleShape).background(BrandGold, CircleShape), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("ACT", style = ts(32.sp, BrandNavy))
                Text("মূল", style = ts(14.sp, BrandNavy))
            }
        }
    }
}

@Composable private fun RootRing(range: IntRange) = Full {
    range.forEach { i ->
        val (w, pos) = RingWords[i]
        val p = Pos.of(pos)
        val (x, y) = ringPos(i)
        At(x.dp, y.dp) {
            Text("${p.symbol} $w", style = ts(19.sp), modifier = Modifier.shadow(8.dp, RoundedCornerShape(50)).background(p.light, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 4.dp))
        }
    }
}

// ---- 4 · stories -----------------------------------------------------------------------------

@Composable private fun StoryPhone() = Full { Phone("03_hook_sabbatical_story", 168.dp, Modifier.align(Alignment.TopStart).padding(start = 40.dp, top = 112.dp)) }

@Composable private fun StoryStep(step: Int) = Full {
    val y = listOf(140, 240, 340)[step]
    val content = listOf(
        Triple("শব্দ", "Sabbatical", Color.White),
        Triple("ধ্বনি-সূত্র", "সব বাদ … কাল", BrandGold),
        Triple("অর্থ", "= দীর্ঘ ছুটি ✓", CorrectGreen),
    )[step]
    Glass(Modifier.offset(x = 236.dp, y = y.dp).width(270.dp), border = content.third.copy(alpha = 0.6f)) {
        Column {
            Text(content.first, style = ts(14.sp, Color.White.copy(alpha = 0.65f), FontWeight.SemiBold, TextAlign.Start))
            Text(content.second, style = ts(28.sp, content.third, align = TextAlign.Start))
        }
    }
}

@Composable private fun StoryQuote() = Full {
    Text("\"স্যার, কাল থেকে সব বাদ!\" — ছোট্ট গল্প, মনে থাকে চিরকাল", style = ts(16.sp, Color.White.copy(alpha = 0.85f), FontWeight.SemiBold),
        modifier = Modifier.offset(x = 236.dp, y = 440.dp).width(270.dp))
}

// ---- 5 · IELTS paraphrase --------------------------------------------------------------------

@Composable private fun ParaQuestion() = Full {
    Glass(Modifier.offset(x = 40.dp, y = 140.dp).width(460.dp)) {
        Column {
            Text("প্রশ্নে লেখা", style = ts(15.sp, Color.White.copy(alpha = 0.65f), FontWeight.SemiBold, TextAlign.Start))
            Text("“reduce the harm caused by climate change”", style = ts(24.sp, align = TextAlign.Start))
        }
    }
}
@Composable private fun ParaPassage() = Full {
    Glass(Modifier.offset(x = 40.dp, y = 300.dp).width(460.dp), border = BrandGold) {
        Column {
            Text("প্যাসেজে লেখা", style = ts(15.sp, BrandGold.copy(alpha = 0.85f), FontWeight.SemiBold, TextAlign.Start))
            Text("“mitigate climate change”", style = ts(28.sp, BrandGold, align = TextAlign.Start))
        }
    }
}
@Composable private fun ParaLink() = Full {
    At(270.dp, 268.dp) {
        Text("≈  একই অর্থ  ✓", style = ts(22.sp), modifier = Modifier.shadow(12.dp, RoundedCornerShape(50)).background(CorrectGreen, RoundedCornerShape(50))
            .padding(horizontal = 18.dp, vertical = 6.dp))
    }
}

// ---- 6 · listening & pronunciation -----------------------------------------------------------

@Composable private fun ListenPhone() = Full { Phone("12_listen_and_spell", 168.dp, Modifier.align(Alignment.TopEnd).padding(end = 40.dp, top = 112.dp)) }

@Composable private fun ListenCard(step: Int) = Full {
    val y = listOf(130, 250, 370)[step]
    Glass(Modifier.offset(x = 34.dp, y = y.dp).width(285.dp)) {
        when (step) {
            0 -> Column {
                Text("🔊 ব্রিটিশ উচ্চারণ", style = ts(19.sp, BrandGold, align = TextAlign.Start))
                Text("accommodation", style = ts(26.sp, align = TextAlign.Start))
                Text("/əˌkɒməˈdeɪʃn/", style = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 18.sp, color = Color.White.copy(alpha = 0.8f)))
            }
            1 -> Column {
                Text("🎧 শুনে বানান লিখুন", style = ts(19.sp, BrandGold, align = TextAlign.Start))
                Text("৫৯টি বানানের ফাঁদ চিনে নিন", style = ts(17.sp, align = TextAlign.Start))
            }
            else -> Column {
                Text("🗣️ শুনুন, তারপর বলুন", style = ts(19.sp, BrandGold, align = TextAlign.Start))
                Text("সঠিক উচ্চারণে আত্মবিশ্বাসে বলুন", style = ts(17.sp, align = TextAlign.Start))
            }
        }
    }
}

// ---- 7 · correct usage ------------------------------------------------------------------------

@Composable private fun UsageRow(step: Int) = Full {
    val (wrong, right, note) = listOf(
        Triple("do a mistake", "make a mistake", "শব্দজুটি"),
        Triple("revert back to me", "get back to me", "অফিসের ইংরেজি"),
        Triple("affect ↔ effect", "affect = ক্রিয়া · effect = ফল", "পার্থক্য কী?"),
    )[step]
    Glass(Modifier.offset(x = 40.dp, y = (130 + step * 112).dp).width(460.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("✗ $wrong", style = ts(19.sp, Color(0xFFFF8A80), FontWeight.SemiBold, TextAlign.Start))
                Text("✓ $right", style = ts(23.sp, Color(0xFF81C784), align = TextAlign.Start))
            }
            Pill(note, size = 13.sp)
        }
    }
}

// ---- 8 · exam + 1-3-7-30 ----------------------------------------------------------------------

@Composable private fun ExamPhone() = Full { Phone("14_exam_ready", 160.dp, Modifier.align(Alignment.TopStart).padding(start = 40.dp, top = 116.dp)) }

@Composable private fun ExamTimer() = Full {
    Glass(Modifier.offset(x = 230.dp, y = 126.dp).width(275.dp)) {
        Column {
            Text("⏱️ ২০ মিনিটের MCQ সেট", style = ts(20.sp, BrandGold, align = TextAlign.Start))
            Text("বিসিএস, ব্যাংক, এনটিআরসিএ ধাঁচে ২৫০টি প্রশ্ন", style = ts(17.sp, align = TextAlign.Start))
        }
    }
}

@Composable private fun ReviewDot(i: Int) = Full {
    val days = listOf("১", "৩", "৭", "৩০")
    val colors = listOf(Color(0xFF42A5F5), Color(0xFF26A69A), Color(0xFFFFA726), Color(0xFF7E57C2))
    At((262 + i * 66).dp, 310.dp) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(52.dp).shadow(10.dp, CircleShape).background(colors[i], CircleShape), contentAlignment = Alignment.Center) {
                Text(days[i], style = ts(22.sp))
            }
            Text("দিন", style = ts(14.sp, Color.White.copy(alpha = 0.8f)))
        }
    }
}

@Composable private fun ReviewText() = Full {
    Text("কবে কোন শব্দ রিভিউ করবেন, অ্যাপ নিজেই মনে করিয়ে দেয় — শব্দ যায় স্থায়ী স্মৃতিতে",
        style = ts(17.sp, Color.White.copy(alpha = 0.9f), FontWeight.SemiBold, TextAlign.Start), modifier = Modifier.offset(x = 232.dp, y = 372.dp).width(275.dp))
}

// ---- 9 · everything in one app ----------------------------------------------------------------

private val Tiles = listOf(
    "✂️" to "শব্দ ব্যবচ্ছেদ", "🌳" to "Root Power", "📖" to "গল্পে শেখা", "≈" to "Paraphrase",
    "🎧" to "Listening", "🗣️" to "উচ্চারণ", "📝" to "২৫০ MCQ", "🔁" to "১-৩-৭-৩০",
)

@Composable private fun GridTitle() = Full {
    Text("এক অ্যাপে সব", style = ts(38.sp, BrandGold), modifier = Modifier.align(Alignment.TopCenter).padding(top = 34.dp))
}

@Composable private fun GridRow(row: Int) = Full {
    Row(Modifier.align(Alignment.TopCenter).padding(top = (110 + row * 150).dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Tiles.subList(row * 4, row * 4 + 4).forEach { (icon, label) ->
            Column(Modifier.width(112.dp).height(132.dp).shadow(10.dp, RoundedCornerShape(18.dp)).background(Card, RoundedCornerShape(18.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(18.dp)).padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(icon, style = ts(38.sp, BrandGold))
                Spacer(Modifier.height(6.dp))
                Text(label, style = ts(16.sp))
            }
        }
    }
}

@Composable private fun GridFooter() = Footer("বইয়ের ১১টি অধ্যায় · ইন্টারনেট ছাড়াই · কোনো বিজ্ঞাপন নেই")

// ---- 10 · end card ----------------------------------------------------------------------------

@Composable private fun EndCard() = Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BrandNavy, Color(0xFF1F4A80))))) {
    Row(Modifier.fillMaxSize().padding(28.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(0.95f), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(128.dp))
            Text("Vocabsolution", style = ts(30.sp))
            Text("by English Doctor", style = ts(17.sp, BrandGold))
            Spacer(Modifier.height(10.dp))
            Text("মুখস্থের অবসান!", style = ts(24.sp))
            Spacer(Modifier.height(8.dp))
            Text("শাহজালাল সালতানাত (ইংলিশ ডক্টর)", style = ts(14.sp, Color.White.copy(alpha = 0.85f), FontWeight.SemiBold))
            Text("-এর বই থেকে তৈরি", style = ts(13.sp, Color.White.copy(alpha = 0.7f), FontWeight.SemiBold))
        }
        Column(Modifier.weight(1.05f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("পুরো অ্যাপ মাত্র ${Sales.PRICE}", style = ts(23.sp, BrandNavy),
                modifier = Modifier.fillMaxWidth().background(BrandGold, RoundedCornerShape(50)).padding(vertical = 8.dp))
            listOf("💬" to ("WhatsApp-এ মেসেজ দিন" to Sales.NUMBER), "💳" to ("bKash / Nagad" to Sales.NUMBER), "👍" to ("অথবা Facebook পেজে" to Sales.PAGE)).forEach { (icon, lv) ->
                Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, style = ts(22.sp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(lv.first, style = ts(13.sp, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold, TextAlign.Start))
                        Text(lv.second, style = ts(20.sp, align = TextAlign.Start))
                    }
                }
            }
        }
    }
}

class ComboLayersTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1080, xdpi = 320, ydpi = 320, density = Density.XHIGH),
        theme = "android:Theme.Translucent.NoTitleBar",
        useDeviceResolution = true,
    )

    private fun shot(name: String, content: @Composable () -> Unit) = paparazzi.snapshot(name = name) { content() }

    @Test fun bgNight() = shot("bg_night") { Bg(Night1, Night2, Color(0xFF6C7BFF), Color(0xFF9C4DFF)) }
    @Test fun bgBlue() = shot("bg_blue") { Bg(Color(0xFF0D2C54), Color(0xFF1F4A80), Color(0xFF42A5F5), BrandGold) }
    @Test fun bgTeal() = shot("bg_teal") { Bg(Color(0xFF062A33), Color(0xFF0D4F5C), Color(0xFF26A69A), Color(0xFF42A5F5)) }
    @Test fun bgPlum() = shot("bg_plum") { Bg(Color(0xFF1E0F35), Color(0xFF3A1E5C), Color(0xFFAB47BC), BrandGold) }
    @Test fun endcard() = shot("endcard") { EndCard() }

    @Test fun h1() = shot("hook_1") { HookLine1() }
    @Test fun h2() = shot("hook_2") { HookLine2() }
    @Test fun h3() = shot("hook_3") { HookAudience() }
    @Test fun t1() = shot("turn_line") { TurnLine() }
    @Test fun t2() = shot("turn_logo") { TurnLogo() }

    @Test fun d0() = shot("d_head") { Heading("০১", "শব্দ ব্যবচ্ছেদ", "শব্দ ভাঙুন, অর্থ বুঝুন") }
    @Test fun d1() = shot("d_word") { DissWord() }
    @Test fun d2() = shot("d_parts") { DissParts() }
    @Test fun d3() = shot("d_meaning") { DissMeaning() }

    @Test fun r0() = shot("r_head") { Heading("০২", "Root Power", "এক মূল, পুরো পরিবার") }
    @Test fun r1() = shot("r_lines") { RootLines() }
    @Test fun r2() = shot("r_center") { RootCenter() }
    @Test fun r3() = shot("r_ring1") { RootRing(0..3) }
    @Test fun r4() = shot("r_ring2") { RootRing(4..7) }
    @Test fun r5() = shot("r_footer") { Footer("৯৬টি শব্দ-পরিবার · ২,৬৯৪টি শব্দ · রঙে চেনা যায় পদ") }

    @Test fun s0() = shot("s_head") { Heading("০৩", "Memory Hooks", "গল্পে গল্পে মনে রাখুন") }
    @Test fun s1() = shot("s_phone") { StoryPhone() }
    @Test fun s2() = shot("s_step1") { StoryStep(0) }
    @Test fun s3() = shot("s_step2") { StoryStep(1) }
    @Test fun s4() = shot("s_step3") { StoryStep(2) }
    @Test fun s5() = shot("s_quote") { StoryQuote() }

    @Test fun p0() = shot("p_head") { Heading("০৪", "IELTS Paraphrase", "একই কথা, ভিন্ন রূপ") }
    @Test fun p1() = shot("p_question") { ParaQuestion() }
    @Test fun p2() = shot("p_passage") { ParaPassage() }
    @Test fun p3() = shot("p_link") { ParaLink() }
    @Test fun p4() = shot("p_footer") { Footer("১,১৫২টি paraphrase · ১৫০টি Reading প্যাসেজ · Writing-এর ভাষা") }

    @Test fun l0() = shot("l_head") { Heading("০৫", "Listening ও উচ্চারণ", "শুনুন, লিখুন, বলুন") }
    @Test fun l1() = shot("l_phone") { ListenPhone() }
    @Test fun l2() = shot("l_card1") { ListenCard(0) }
    @Test fun l3() = shot("l_card2") { ListenCard(1) }
    @Test fun l4() = shot("l_card3") { ListenCard(2) }

    @Test fun u0() = shot("u_head") { Heading("০৬", "সঠিক প্রয়োগ", "ভুল ইংরেজি আর নয়") }
    @Test fun u1() = shot("u_row1") { UsageRow(0) }
    @Test fun u2() = shot("u_row2") { UsageRow(1) }
    @Test fun u3() = shot("u_row3") { UsageRow(2) }
    @Test fun u4() = shot("u_footer") { Footer("২০০টি শব্দজুটি · ১০০ জোড়া পার্থক্য · প্রচলিত ভুলের সংশোধন") }

    @Test fun e0() = shot("e_head") { Heading("০৭", "পরীক্ষা ও রিভিউ", "অনুশীলন আর স্থায়ী মনে রাখা") }
    @Test fun e1() = shot("e_phone") { ExamPhone() }
    @Test fun e2() = shot("e_timer") { ExamTimer() }
    @Test fun e3() = shot("e_dot1") { ReviewDot(0) }
    @Test fun e4() = shot("e_dot2") { ReviewDot(1) }
    @Test fun e5() = shot("e_dot3") { ReviewDot(2) }
    @Test fun e6() = shot("e_dot4") { ReviewDot(3) }
    @Test fun e7() = shot("e_text") { ReviewText() }

    @Test fun g0() = shot("g_title") { GridTitle() }
    @Test fun g1() = shot("g_row1") { GridRow(0) }
    @Test fun g2() = shot("g_row2") { GridRow(1) }
    @Test fun g3() = shot("g_footer") { GridFooter() }
}
