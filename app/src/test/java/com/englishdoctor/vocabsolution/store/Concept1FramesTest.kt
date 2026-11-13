package com.englishdoctor.vocabsolution.store

import android.graphics.BitmapFactory
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
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
import com.englishdoctor.vocabsolution.ui.theme.BrandNavyLight
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.HindSiliguri
import org.junit.Rule
import org.junit.Test
import java.io.File

/*
 * Concept 1 "চায়ের দোকানের চ্যালেঞ্জ" as an illustrated 1:1 ad (1080×1080, 540dp at xhdpi).
 * Frames are joined into a video by tools/marketing/build_concept1.py.
 * Swap any scene frame for AI footage later; the timing in that script stays the same.
 */

private fun st(size: TextUnit, color: Color = BrandNavy, weight: FontWeight = FontWeight.Bold, align: TextAlign = TextAlign.Center) =
    TextStyle(fontFamily = HindSiliguri, fontSize = size, color = color, fontWeight = weight, textAlign = align, lineHeight = size * 1.3f)

private val SkyTop = Color(0xFFFFD08A)
private val SkyBottom = Color(0xFFF08A4B)
private val Wood = Color(0xFF8D5A3B)
private val WoodDark = Color(0xFF6B4229)
private val Awning = Color(0xFFC62828)
private val RafiColor = Color(0xFF1E88E5)
private val TanvirColor = Color(0xFF8E244D)

/** Roadside tea stall: warm evening sky, striped awning, wooden counter with tea. */
@Composable
private fun TeaStall(evening: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    val top = if (evening) Color(0xFF3B4A7A) else SkyTop
    val bottom = if (evening) Color(0xFFE07A4B) else SkyBottom
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(top, bottom)))) {
        // Awning
        Row(Modifier.fillMaxWidth().height(46.dp)) {
            repeat(9) { i -> Box(Modifier.weight(1f).fillMaxSize().background(if (i % 2 == 0) Awning else Color.White)) }
        }
        Row(Modifier.fillMaxWidth().offset(y = 38.dp)) {
            repeat(9) { i -> Box(Modifier.weight(1f).height(18.dp).clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                .background(if (i % 2 == 0) Awning else Color.White)) }
        }
        // Counter
        Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
            // Cups sit in the middle so they never cover the characters' name tags.
            Row(Modifier.fillMaxWidth().padding(horizontal = 185.dp), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom) {
                Text("☕", style = st(30.sp)); Text("🫖", style = st(40.sp)); Text("☕", style = st(30.sp)); Text("🍪", style = st(28.sp))
            }
            Box(Modifier.fillMaxWidth().height(10.dp).background(WoodDark))
            Box(Modifier.fillMaxWidth().height(70.dp).background(Wood))
        }
        content()
    }
}

@Composable
private fun Avatar(emoji: String, name: String, color: Color, modifier: Modifier = Modifier, size: Dp = 128.dp) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(size).clip(CircleShape).background(color.copy(alpha = 0.18f)).border(5.dp, color, CircleShape),
            contentAlignment = Alignment.Center) {
            Text(emoji, style = st((size.value * 0.55f).sp))
        }
        Text(name, style = st(18.sp, Color.White), modifier = Modifier.padding(top = 4.dp)
            .background(color, RoundedCornerShape(50)).padding(horizontal = 14.dp, vertical = 2.dp))
    }
}

@Composable
private fun Bubble(text: String, modifier: Modifier = Modifier, accent: Color = BrandNavy, big: Boolean = false) {
    Text(
        text, style = st(if (big) 28.sp else 24.sp, accent),
        modifier = modifier.widthIn(max = 330.dp).background(Color.White, RoundedCornerShape(26.dp))
            .border(3.dp, accent, RoundedCornerShape(26.dp)).padding(horizontal = 18.dp, vertical = 12.dp),
    )
}

@Composable
private fun TopCaption(text: String, modifier: Modifier = Modifier) {
    Text(text, style = st(24.sp, Color.White), modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp)
        .background(BrandNavy.copy(alpha = 0.88f), RoundedCornerShape(18.dp)).padding(vertical = 10.dp, horizontal = 12.dp))
}

/** Two friends at the counter with optional speech bubbles. */
@Composable
private fun TwoFriends(caption: String?, rafi: String?, tanvir: String?, rafiFace: String, tanvirFace: String,
                       evening: Boolean = false, rafiBig: Boolean = false, rafiAccent: Color = RafiColor) = TeaStall(evening) {
    if (caption != null) TopCaption(caption, Modifier.align(Alignment.TopCenter).padding(top = 72.dp))
    Avatar(rafiFace, "রাফি", RafiColor, Modifier.align(Alignment.BottomStart).padding(start = 36.dp, bottom = 92.dp))
    Avatar(tanvirFace, "তানভীর", TanvirColor, Modifier.align(Alignment.BottomEnd).padding(end = 36.dp, bottom = 92.dp))
    if (tanvir != null) Bubble(tanvir, Modifier.align(Alignment.CenterEnd).padding(end = 24.dp, bottom = 20.dp), TanvirColor)
    if (rafi != null) Bubble(rafi, Modifier.align(Alignment.CenterStart).padding(start = 24.dp, bottom = 20.dp), rafiAccent, rafiBig)
}

private fun screen(name: String) = listOf("../marketing/ad-screens", "marketing/ad-screens").map { File(it, "$name.png") }.first { it.exists() }
    .readBytes().let { BitmapFactory.decodeByteArray(it, 0, it.size).asImageBitmap() }

@Composable
private fun Phone(name: String, width: Dp, modifier: Modifier = Modifier) {
    Box(modifier.width(width).height(width * 2.222f + 12.dp).clip(RoundedCornerShape(width / 7)).background(Color.Black).padding(6.dp)) {
        Image(screen(name), null, Modifier.fillMaxSize().clip(RoundedCornerShape(width / 8.5f)), contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter)
    }
}

@Composable
private fun NavyFrame(content: @Composable BoxScope.() -> Unit) =
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BrandNavy, BrandNavyLight))), content = content)

@Composable
private fun PhoneFrame(screenName: String, caption: String) = NavyFrame {
    Row(Modifier.fillMaxSize().padding(28.dp), verticalAlignment = Alignment.CenterVertically) {
        Phone(screenName, 205.dp)
        Spacer(Modifier.width(24.dp))
        Text(caption, style = st(30.sp, Color.White, align = TextAlign.Start), modifier = Modifier.weight(1f))
    }
}

/** The Sabbatical story: tired clerk, files on the boss's desk. */
@Composable
private fun OfficeStory() = Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB))))) {
    TopCaption("রহিম সাহেব — টানা ১০ বছর অফিস…", Modifier.align(Alignment.TopCenter).padding(top = 28.dp))
    Row(Modifier.align(Alignment.Center).padding(top = 30.dp), verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("😮‍💨", style = st(96.sp))
            Text("রহিম সাহেব", style = st(18.sp, Color.White), modifier = Modifier.background(Color(0xFF455A64), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 2.dp))
        }
        Text("📁📁📁", style = st(46.sp), modifier = Modifier.padding(bottom = 30.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("😄", style = st(80.sp))
            Text("বস", style = st(18.sp, Color.White), modifier = Modifier.background(Color(0xFF455A64), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 2.dp))
        }
    }
    Bubble("\"স্যার, কাল থেকে সব বাদ!\"", Modifier.align(Alignment.BottomCenter).padding(bottom = 46.dp), Color(0xFF455A64), big = true)
}

@Composable
private fun Formula() = NavyFrame {
    Column(Modifier.align(Alignment.Center).padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("সব  বাদ  …  কাল", style = st(46.sp, BrandGold))
        Text("⬇", style = st(34.sp, Color.White))
        Text("Sab · ba · ti · cal", style = st(46.sp, Color.White))
        Text("= দীর্ঘ ছুটি ✓", style = st(38.sp, Color.White), modifier = Modifier.background(CorrectGreen, RoundedCornerShape(20.dp))
            .padding(horizontal = 26.dp, vertical = 8.dp))
        Text("মুখস্থ নয়, গল্প মনে রাখুন", style = st(22.sp, Color.White.copy(alpha = 0.85f), FontWeight.SemiBold))
    }
}

@Composable
private fun Montage() = NavyFrame {
    Column(Modifier.fillMaxSize().padding(vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Vocabsolution-এ আরও যা আছে", style = st(26.sp, BrandGold))
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            listOf("06_word_breaker" to "শব্দ ভাঙা", "14_exam_ready" to "২৫০ MCQ", "15_review_flashcard" to "১-৩-৭-৩০ রিভিউ").forEach { (s, l) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Phone(s, 150.dp)
                    Text(l, style = st(19.sp, Color.White), modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("১০০ গল্প · ১১ অধ্যায় · ইন্টারনেট ছাড়াই", style = st(21.sp, Color.White, FontWeight.SemiBold))
    }
}

@Composable
private fun NextDay() = TeaStall(evening = true) {
    Text("পরের দিন…", style = st(54.sp, Color.White), modifier = Modifier.align(Alignment.Center))
}

@Composable
private fun EndCardSquare() = NavyFrame {
    Row(Modifier.fillMaxSize().padding(26.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(0.9f), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(130.dp))
            Text("Vocabsolution", style = st(30.sp, Color.White))
            Text("by English Doctor", style = st(17.sp, BrandGold))
            Spacer(Modifier.height(10.dp))
            Text("মুখস্থের অবসান!", style = st(24.sp, Color.White))
        }
        Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("পুরো অ্যাপ মাত্র ${Sales.PRICE}", style = st(23.sp, BrandNavy),
                modifier = Modifier.fillMaxWidth().background(BrandGold, RoundedCornerShape(50)).padding(vertical = 8.dp))
            listOf("💬" to ("WhatsApp-এ মেসেজ দিন" to Sales.NUMBER), "💳" to ("bKash / Nagad" to Sales.NUMBER),
                "👍" to ("অথবা Facebook পেজে" to Sales.PAGE)).forEach { (icon, lv) ->
                Row(Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp)).padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(icon, style = st(22.sp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(lv.first, style = st(13.sp, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold, TextAlign.Start))
                        Text(lv.second, style = st(20.sp, Color.White, align = TextAlign.Start))
                    }
                }
            }
        }
    }
}

class Concept1FramesTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1080, xdpi = 320, ydpi = 320, density = Density.XHIGH),
        useDeviceResolution = true,
    )

    @Test fun f01() = paparazzi.snapshot(name = "c1_01_hook") {
        TwoFriends("Sabbatical মানে কী? 🤔", rafi = null, tanvir = "দোস্ত, Sabbatical মানে কী?", rafiFace = "🤨", tanvirFace = "😏")
    }
    @Test fun f02() = paparazzi.snapshot(name = "c1_02_forgot") {
        TwoFriends("কাল মুখস্থ, আজ ভুলে গেছি?", rafi = "কাল রাতেই মুখস্থ করলাম…\nএখন মনে নাই! 😩", tanvir = null, rafiFace = "😩", tanvirFace = "😏")
    }
    @Test fun f03() = paparazzi.snapshot(name = "c1_03_tip") {
        TwoFriends(null, rafi = null, tanvir = "মুখস্থ না, গল্প মনে রাখ! 📱", rafiFace = "🤔", tanvirFace = "😎")
    }
    @Test fun f04() = paparazzi.snapshot(name = "c1_04_card") { PhoneFrame("02_hook_sabbatical_front", "Memory Hooks:\nপ্রথমে শুধু শব্দ…") }
    @Test fun f05() = paparazzi.snapshot(name = "c1_05_story") { PhoneFrame("03_hook_sabbatical_story", "কার্ড উল্টালেই\nমজার গল্প ✨") }
    @Test fun f06() = paparazzi.snapshot(name = "c1_06_office") { OfficeStory() }
    @Test fun f07() = paparazzi.snapshot(name = "c1_07_formula") { Formula() }
    @Test fun f08() = paparazzi.snapshot(name = "c1_08_montage") { Montage() }
    @Test fun f09() = paparazzi.snapshot(name = "c1_09_nextday") { NextDay() }
    @Test fun f10() = paparazzi.snapshot(name = "c1_10_question") {
        TwoFriends(null, rafi = "বল তো, Sagacious মানে কী?", tanvir = null, rafiFace = "😏", tanvirFace = "🤔", evening = true)
    }
    @Test fun f11() = paparazzi.snapshot(name = "c1_11_answer") {
        TwoFriends("রাফি এখন নিজেই প্রশ্ন করে! 😎", rafi = "সাগর-শেষ —\nবিচক্ষণ! ✓", tanvir = "👏 বাহ্!", rafiFace = "😎", tanvirFace = "🤩",
            evening = true, rafiBig = true, rafiAccent = CorrectGreen)
    }
    @Test fun f12() = paparazzi.snapshot(name = "c1_12_endcard") { EndCardSquare() }
}
