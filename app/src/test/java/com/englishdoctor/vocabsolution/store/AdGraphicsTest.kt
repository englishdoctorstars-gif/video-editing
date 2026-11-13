package com.englishdoctor.vocabsolution.store

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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

/** Sales details used on every ad graphic. Keep in sync with docs/ad-concepts.md. */
object Sales {
    const val PRICE = "১০০ টাকা"
    const val NUMBER = "01682888114"
    const val PAGE = "English Doctor"
}

private fun t(size: TextUnit, color: Color = Color.White, weight: FontWeight = FontWeight.Bold, align: TextAlign = TextAlign.Center) =
    TextStyle(fontFamily = HindSiliguri, fontSize = size, color = color, fontWeight = weight, textAlign = align, lineHeight = size * 1.35f)

@Composable
private fun NavyBackground(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(BrandNavy, BrandNavyLight))), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
private fun Brand(compact: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.size(if (compact) 54.dp else 92.dp))
        Text("Vocabsolution", style = t(if (compact) 23.sp else 32.sp))
        Text("by English Doctor", style = t(16.sp, BrandGold))
    }
}

@Composable
private fun InfoRow(icon: String, label: String, value: String, compact: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp)).padding(horizontal = 16.dp, vertical = if (compact) 4.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, style = t(24.sp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, style = t(if (compact) 11.sp else 13.sp, Color.White.copy(alpha = 0.8f), FontWeight.SemiBold, TextAlign.Start))
            Text(value, style = t(if (compact) 17.sp else 19.sp, Color.White, FontWeight.Bold, TextAlign.Start))
        }
    }
}

/** The closing frame of every ad: price, WhatsApp, bKash/Nagad and the Facebook page. */
@Composable
fun EndCard(compact: Boolean) = NavyBackground {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 26.dp, vertical = if (compact) 14.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 10.dp, Alignment.CenterVertically),
    ) {
        Brand(compact)
        Text("মুখস্থের অবসান!", style = t(if (compact) 20.sp else 24.sp))
        Text("পুরো অ্যাপ মাত্র ${Sales.PRICE}", style = t(if (compact) 19.sp else 22.sp, BrandNavy),
            modifier = Modifier.background(BrandGold, RoundedCornerShape(50)).padding(horizontal = 22.dp, vertical = 6.dp))
        InfoRow("💬", "WhatsApp-এ মেসেজ দিন", Sales.NUMBER, compact)
        InfoRow("💳", "bKash / Nagad", Sales.NUMBER, compact)
        InfoRow("👍", "অথবা Facebook পেজে মেসেজ দিন", Sales.PAGE, compact)
        Text("ইন্টারনেট ছাড়াই চলে · কোনো বিজ্ঞাপন নেই", style = t(if (compact) 12.sp else 14.sp, Color.White.copy(alpha = 0.85f), FontWeight.SemiBold))
    }
}

/** Three steps to buy, for ads, posts and WhatsApp replies. */
@Composable
fun HowToBuyCard(compact: Boolean) = NavyBackground {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = if (compact) 12.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 12.dp, Alignment.CenterVertically),
    ) {
        Brand(compact = true)
        Text("কীভাবে কিনবেন?", style = t(if (compact) 22.sp else 26.sp, BrandGold))
        listOf(
            "১" to "WhatsApp ${Sales.NUMBER} অথবা Facebook পেজ \"${Sales.PAGE}\"-এ মেসেজ দিন",
            "২" to "bKash বা Nagad থেকে ${Sales.NUMBER} নম্বরে ${Sales.PRICE} Send Money করুন",
            "৩" to "Transaction ID (TrxID) মেসেজে পাঠান — অ্যাপ ও ব্যবহারের নিয়ম পেয়ে যাবেন",
        ).forEach { (n, s) ->
            Row(
                Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).padding(if (compact) 9.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.size(38.dp).background(BrandGold, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
                    Text(n, style = t(20.sp, BrandNavy))
                }
                Spacer(Modifier.width(14.dp))
                Text(s, style = t(if (compact) 14.sp else 16.sp, BrandNavy, FontWeight.SemiBold, TextAlign.Start))
            }
        }
        Text("মাত্র ${Sales.PRICE} · Android ফোনে চলে", style = t(16.sp, Color.White, FontWeight.SemiBold))
    }
}

// ---- Concept 2 "৩ সেকেন্ডের শব্দ চ্যালেঞ্জ" frames ------------------------------------------------

@Composable
private fun ChallengeWord(count: String) = NavyBackground {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("শব্দ চ্যালেঞ্জ 🧠", style = t(26.sp, BrandGold))
        Text("SAGACIOUS", style = t(54.sp))
        Text("মানে কী?", style = t(30.sp))
        Spacer(Modifier.height(24.dp))
        Box(Modifier.size(150.dp).border(8.dp, BrandGold, RoundedCornerShape(50)), contentAlignment = Alignment.Center) {
            Text(count, style = t(72.sp, BrandGold))
        }
    }
}

@Composable
private fun ChallengeOptions(reveal: Boolean) = NavyBackground {
    Column(Modifier.padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("SAGACIOUS", style = t(40.sp))
        listOf("ক) নিষ্ঠুর", "খ) বিচক্ষণ", "গ) দুর্বল", "ঘ) ধনী").forEachIndexed { i, o ->
            val right = reveal && i == 1
            Text(
                o + if (right) "  ✓" else "",
                style = t(26.sp, if (right) Color.White else BrandNavy, align = TextAlign.Start),
                modifier = Modifier.fillMaxWidth()
                    .background(if (right) CorrectGreen else if (reveal) Color.White.copy(alpha = 0.55f) else Color.White, RoundedCornerShape(18.dp))
                    .padding(horizontal = 22.dp, vertical = 14.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(if (reveal) "সঠিক উত্তর: খ) বিচক্ষণ" else "কমেন্টে উত্তর দিন 👇", style = t(26.sp, BrandGold))
    }
}

/** Stand-in for the AI shot of the Bhola boatman; replace with the generated clip in the final edit. */
@Composable
private fun ChallengeStory() = NavyBackground {
    Column(Modifier.padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("⛵🌊", style = t(80.sp))
        Text("মনে রাখার গল্প", style = t(22.sp, BrandGold))
        Text("ভোলার বুড়ো মাঝি সাগরের শুরু থেকে শেষ সব চেনেন — ঝড়ের আগেই বুঝে যান।", style = t(28.sp))
        Text("সাগর-শেষ = Sa-ga-cious", style = t(30.sp, BrandGold),
            modifier = Modifier.background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp)).padding(14.dp))
    }
}

@Composable
private fun ChallengePhone() = NavyBackground {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("সাগর-শেষ = বিচক্ষণ ✓", style = t(26.sp, BrandGold))
        val file = listOf("../marketing/ad-screens", "marketing/ad-screens").map { File(it, "05_hook_sagacious_story.png") }.first { it.exists() }
        val bytes = file.readBytes()
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        Box(Modifier.width(218.dp).height(480.dp).clip(RoundedCornerShape(30.dp)).background(Color.Black).padding(8.dp)) {
            Image(bmp.asImageBitmap(), null, Modifier.fillMaxSize().clip(RoundedCornerShape(24.dp)), contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter)
        }
        Text("এমন ১০০টি গল্প Vocabsolution-এ", style = t(21.sp))
    }
}

class AdGraphicsVerticalTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1920, xdpi = 440, ydpi = 440, density = Density.XXHIGH),
        useDeviceResolution = true,
    )

    @Test fun endCard() = paparazzi.snapshot(name = "endcard_9x16") { EndCard(compact = false) }
    @Test fun howToBuy() = paparazzi.snapshot(name = "how_to_buy_9x16") { HowToBuyCard(compact = false) }
    @Test fun c2Count3() = paparazzi.snapshot(name = "c2_01_count3") { ChallengeWord("৩") }
    @Test fun c2Count2() = paparazzi.snapshot(name = "c2_02_count2") { ChallengeWord("২") }
    @Test fun c2Count1() = paparazzi.snapshot(name = "c2_03_count1") { ChallengeWord("১") }
    @Test fun c2Options() = paparazzi.snapshot(name = "c2_04_options") { ChallengeOptions(reveal = false) }
    @Test fun c2Story() = paparazzi.snapshot(name = "c2_05_story") { ChallengeStory() }
    @Test fun c2Phone() = paparazzi.snapshot(name = "c2_06_phone") { ChallengePhone() }
    @Test fun c2Answer() = paparazzi.snapshot(name = "c2_07_answer") { ChallengeOptions(reveal = true) }
}

class AdGraphicsFeedTest {
    @get:Rule
    val paparazzi = Paparazzi(
        deviceConfig = DeviceConfig.PIXEL_5.copy(screenWidth = 1080, screenHeight = 1350, xdpi = 440, ydpi = 440, density = Density.XXHIGH),
        useDeviceResolution = true,
    )

    @Test fun endCard() = paparazzi.snapshot(name = "endcard_4x5") { EndCard(compact = true) }
    @Test fun howToBuy() = paparazzi.snapshot(name = "how_to_buy_4x5") { HowToBuyCard(compact = true) }
}
