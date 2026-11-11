package com.englishdoctor.vocabsolution.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.englishdoctor.vocabsolution.ui.theme.CorrectGreen
import com.englishdoctor.vocabsolution.ui.theme.IpaFamily
import com.englishdoctor.vocabsolution.ui.theme.Pos
import com.englishdoctor.vocabsolution.ui.theme.color

/** Coloured part-of-speech chip. Always shows the symbol too, so colour is never the only cue. */
@Composable
fun PosChip(pos: String?, modifier: Modifier = Modifier, showBn: Boolean = true) {
    val p = Pos.of(pos)
    val c = p.color()
    Surface(
        color = c.copy(alpha = 0.14f),
        contentColor = c,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, c.copy(alpha = 0.5f)),
        modifier = modifier.semantics { contentDescription = p.bn },
    ) {
        val label = buildString {
            append(p.symbol)
            if (p.abbr.isNotEmpty()) append(" ").append(p.abbr)
            if (showBn) append(" · ").append(p.bn)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp))
    }
}

/** The POS symbol alone, coloured — for dense lists. */
@Composable
fun PosMark(pos: String?) {
    val p = Pos.of(pos)
    Text(p.symbol, color = p.color(), style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.width(22.dp).semantics { contentDescription = p.bn })
}

@Composable
fun PosLegend(modifier: Modifier = Modifier) {
    FlowRow(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf("noun", "verb", "adjective", "adverb").forEach { PosChip(it) }
    }
}

@Composable
fun IpaText(ipa: String, modifier: Modifier = Modifier) {
    if (ipa.isBlank()) return
    Text(ipa, fontFamily = IpaFamily, style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = modifier)
}

@Composable
fun SpeakButton(onSpeak: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onSpeak, modifier = modifier) {
        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "উচ্চারণ শুনুন", tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun BookmarkButton(marked: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            if (marked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
            contentDescription = if (marked) "বুকমার্ক সরান" else "বুকমার্ক করুন",
            tint = if (marked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** "শিখেছি" — adds the card to the 1-3-7-30 review cycle. */
@Composable
fun LearnChip(learned: Boolean, onLearn: () -> Unit) {
    AssistChip(
        onClick = { if (!learned) onLearn() },
        label = { Text(if (learned) "রিভিউতে আছে" else "শিখেছি · রিভিউতে যোগ") },
        leadingIcon = {
            Icon(
                if (learned) Icons.Filled.CheckCircle else Icons.Outlined.AddCircleOutline,
                contentDescription = null,
                tint = if (learned) CorrectGreen else MaterialTheme.colorScheme.primary,
            )
        },
        colors = AssistChipDefaults.assistChipColors(),
    )
}

@Composable
fun ProgressRing(progress: Float, color: Color, size: Dp = 48.dp, stroke: Dp = 5.dp, content: @Composable () -> Unit = {}) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(600), label = "ring")
    val track = MaterialTheme.colorScheme.surfaceVariant
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val s = stroke.toPx()
            val arc = Size(this.size.width - s, this.size.height - s)
            drawArc(track, 0f, 360f, false, Offset(s / 2, s / 2), arc, style = Stroke(s))
            drawArc(color, -90f, 360f * animated, false, Offset(s / 2, s / 2), arc, style = Stroke(s, cap = StrokeCap.Round))
        }
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    accent: Color? = null,
    actions: @Composable () -> Unit = {},
    content: @Composable (Modifier) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleLarge)
                        if (subtitle != null) {
                            Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    if (onBack != null) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "ফিরে যান")
                    }
                },
                actions = {
                    actions()
                    if (onSearch != null) IconButton(onClick = onSearch) { Icon(Icons.Filled.Search, contentDescription = "খুঁজুন") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = accent?.copy(alpha = 0.10f) ?: MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { pad -> content(Modifier.padding(pad)) }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.primary) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = color, modifier = modifier.padding(top = 8.dp, bottom = 4.dp))
}

@Composable
fun ContentCard(modifier: Modifier = Modifier, accent: Color? = null, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    val border = accent?.let { BorderStroke(1.5.dp, it.copy(alpha = 0.45f)) }
    val m = modifier.fillMaxWidth()
    if (onClick != null) {
        Card(onClick = onClick, modifier = m, colors = colors, border = border) { Column(Modifier.padding(16.dp), content = content) }
    } else {
        Card(modifier = m, colors = colors, border = border) { Column(Modifier.padding(16.dp), content = content) }
    }
}

@Composable
fun LabelValue(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    if (value.isBlank()) return
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = valueColor)
    }
}

@Composable
fun TagPill(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = color,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.14f)).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/** A card that flips on tap: [front] first, [back] after. */
@Composable
fun FlipCard(flipped: Boolean, onFlip: () -> Unit, modifier: Modifier = Modifier, front: @Composable ColumnScope.() -> Unit, back: @Composable ColumnScope.() -> Unit) {
    val rotation by animateFloatAsState(if (flipped) 180f else 0f, tween(450), label = "flip")
    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .clickable { onFlip() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
        elevation = CardDefaults.cardElevation(3.dp),
    ) {
        Column(
            Modifier.padding(20.dp).fillMaxWidth().graphicsLayer { rotationY = if (rotation > 90f) 180f else 0f },
            horizontalAlignment = Alignment.Start,
        ) {
            if (rotation <= 90f) front() else back()
        }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, body: String, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ShareButton(card: () -> com.englishdoctor.vocabsolution.share.ShareCardData) {
    val context = androidx.compose.ui.platform.LocalContext.current
    IconButton(onClick = { com.englishdoctor.vocabsolution.share.ShareCardRenderer.share(context, card()) }) {
        Icon(androidx.compose.material.icons.Icons.Filled.Share, contentDescription = "ছবি হিসেবে শেয়ার করুন",
            tint = MaterialTheme.colorScheme.primary)
    }
}
