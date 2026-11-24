package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LearningStatus
import com.example.data.NoteCard
import com.example.data.StudyMode
import com.example.data.ToastMessage
import com.example.data.ToastTone
import com.example.data.extractUrlsFromText
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.parseHexColor

@Composable
fun IslandToastHost(
    toast: ToastMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    AnimatedVisibility(
        visible = toast != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        toast?.let { msg ->
            val accent = when (msg.tone) {
                ToastTone.SUCCESS -> ru.emerald
                ToastTone.WARNING -> ru.amber
                ToastTone.ERROR -> ru.red
                ToastTone.INFO -> ru.blue
            }
            val icon = when (msg.tone) {
                ToastTone.SUCCESS -> Icons.Default.CheckCircle
                ToastTone.WARNING -> Icons.Default.Warning
                ToastTone.ERROR -> Icons.Default.ErrorOutline
                ToastTone.INFO -> Icons.Default.Info
            }
            Surface(
                shape = RoundedCornerShape(99.dp),
                color = if (ru.isDark) Color(0xF2121A2B) else Color(0xF50F172A),
                border = BorderStroke(1.dp, accent.copy(alpha = 0.45f)),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clickable { onDismiss() }
                    .testTag("island_toast")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = msg.message,
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (msg.undoLabel != null && msg.onUndo != null) {
                        Spacer(Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(99.dp),
                            color = accent.copy(alpha = 0.22f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(99.dp))
                                .clickable {
                                    msg.onUndo.invoke()
                                    onDismiss()
                                }
                                .testTag("toast_undo_button")
                        ) {
                            Text(
                                text = msg.undoLabel,
                                color = accent,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusPill(
    status: LearningStatus,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    val col = ru.statusColor(status)
    Surface(
        shape = RoundedCornerShape(99.dp),
        color = col.copy(alpha = if (ru.isDark) 0.16f else 0.12f),
        border = BorderStroke(1.dp, col.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Text(
            text = status.displayLabel,
            color = col,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun ModePill(
    mode: StudyMode,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    val col = if (mode == StudyMode.REVISION) ru.blue else ru.purple
    Surface(
        shape = RoundedCornerShape(99.dp),
        color = col.copy(alpha = if (ru.isDark) 0.15f else 0.10f),
        modifier = modifier
    ) {
        Text(
            text = mode.labelHi,
            color = col,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmLabel: String = "हटाएँ",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val ru = LocalRuColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyMedium, color = ru.mutedText) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) ru.red else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.testTag("confirm_dialog_ok")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("confirm_dialog_cancel")
            ) {
                Text("रद्द करें")
            }
        }
    )
}

@Composable
fun EmptyStateIllustration(
    isSearchEmpty: Boolean,
    title: String,
    subtitle: String,
    actionLabel: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 48.dp)
    ) {
        Canvas(modifier = Modifier.size(92.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(
                color = ru.blue.copy(alpha = 0.12f),
                radius = size.minDimension * 0.46f,
                center = Offset(cx, cy)
            )
            if (isSearchEmpty) {
                // Magnifying glass vector
                drawCircle(
                    color = ru.blue,
                    radius = size.minDimension * 0.22f,
                    center = Offset(cx - 6f, cy - 6f),
                    style = Stroke(width = 6f)
                )
                drawLine(
                    color = ru.amber,
                    start = Offset(cx + 12f, cy + 12f),
                    end = Offset(cx + 34f, cy + 34f),
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
            } else {
                // Rocket / Spark study launch icon
                val rocket = Path().apply {
                    moveTo(cx, cy - 32f)
                    cubicTo(cx + 18f, cy - 16f, cx + 16f, cy + 8f, cx + 10f, cy + 18f)
                    lineTo(cx - 10f, cy + 18f)
                    cubicTo(cx - 16f, cy + 8f, cx - 18f, cy - 16f, cx, cy - 32f)
                    close()
                }
                drawPath(rocket, color = ru.blue)
                drawCircle(Color.White, radius = 6.5f, center = Offset(cx, cy - 6f))
                drawLine(
                    color = ru.emerald,
                    start = Offset(cx - 6f, cy + 24f),
                    end = Offset(cx - 10f, cy + 36f),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = ru.amber,
                    start = Offset(cx, cy + 24f),
                    end = Offset(cx, cy + 40f),
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = ru.emerald,
                    start = Offset(cx + 6f, cy + 24f),
                    end = Offset(cx + 10f, cy + 36f),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = ru.mutedText
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAction,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("empty_state_cta")
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
fun RichNoteCardsViewer(
    cards: List<NoteCard>,
    highlightUrl: String? = null,
    onToggleChecklistLine: ((cardId: String, lineIndex: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        cards.forEach { card ->
            val accent = parseHexColor(card.colorHex, ru.blue)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ru.subtleFill,
                border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                        Text(
                            text = card.labelName.ifBlank { "सामान्य" },
                            style = MaterialTheme.typography.labelMedium,
                            color = accent,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (card.pinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = ru.amber,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    val lines = card.content.lines()
                    lines.forEachIndexed { idx, line ->
                        RichNoteLine(
                            line = line,
                            accent = accent,
                            onToggleChecklist = if (onToggleChecklistLine != null) {
                                { onToggleChecklistLine(card.id, idx) }
                            } else null
                        )
                    }
                    val urls = extractUrlsFromText(card.content).distinct()
                    if (urls.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            urls.forEach { url ->
                                val isHighlighted = highlightUrl != null && url.equals(highlightUrl, ignoreCase = true)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isHighlighted) ru.amber.copy(alpha = 0.22f)
                                            else ru.blue.copy(alpha = 0.10f)
                                        )
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 5.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                        contentDescription = "Open link",
                                        tint = if (isHighlighted) ru.amber else ru.blue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = url,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isHighlighted) ru.amber else ru.blue,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RichNoteLine(
    line: String,
    accent: Color,
    onToggleChecklist: (() -> Unit)?
) {
    val ru = LocalRuColors.current
    val trimmed = line.trimStart()
    when {
        trimmed == "---" || trimmed == "***" -> {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = ru.cardBorder
            )
        }
        trimmed.startsWith("## ") || trimmed.startsWith("# ") -> {
            Text(
                text = trimmed.removePrefix("## ").removePrefix("# "),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            )
        }
        trimmed.startsWith("[ ] ") || trimmed.startsWith("[x] ") || trimmed.startsWith("[X] ") -> {
            val checked = trimmed.startsWith("[x] ", ignoreCase = true)
            val content = trimmed.drop(4)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .then(if (onToggleChecklist != null) Modifier.clickable { onToggleChecklist() } else Modifier)
                    .padding(vertical = 2.dp)
            ) {
                Icon(
                    imageVector = if (checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                    contentDescription = if (checked) "Checked" else "Unchecked",
                    tint = if (checked) ru.emerald else ru.mutedText,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = parseInlineMarkdown(content, ru.amber),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (checked) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (checked) ru.mutedText else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        trimmed.startsWith("> ") -> {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .background(accent.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .border(BorderStroke(0.dp, Color.Transparent))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(18.dp)
                        .background(accent, RoundedCornerShape(2.dp))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = parseInlineMarkdown(trimmed.drop(2), ru.amber),
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                    color = ru.mutedText
                )
            }
        }
        trimmed.startsWith("- ") || trimmed.startsWith("• ") -> {
            Row(modifier = Modifier.padding(vertical = 1.dp)) {
                Text("•  ", color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    text = parseInlineMarkdown(trimmed.drop(2), ru.amber),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        else -> {
            if (line.isBlank()) {
                Spacer(Modifier.height(4.dp))
            } else {
                Text(
                    text = parseInlineMarkdown(line, ru.amber),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

private fun parseInlineMarkdown(input: String, highlightColor: Color) = buildAnnotatedString {
    var i = 0
    while (i < input.length) {
        when {
            input.startsWith("**", i) -> {
                val end = input.indexOf("**", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(input.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(input[i]); i++
                }
            }
            input.startsWith("==", i) -> {
                val end = input.indexOf("==", i + 2)
                if (end > i + 2) {
                    withStyle(
                        SpanStyle(
                            background = highlightColor.copy(alpha = 0.28f),
                            fontWeight = FontWeight.SemiBold
                        )
                    ) {
                        append(input.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(input[i]); i++
                }
            }
            input.startsWith("~~", i) -> {
                val end = input.indexOf("~~", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(input.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(input[i]); i++
                }
            }
            input.startsWith("__", i) -> {
                val end = input.indexOf("__", i + 2)
                if (end > i + 2) {
                    withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                        append(input.substring(i + 2, end))
                    }
                    i = end + 2
                } else {
                    append(input[i]); i++
                }
            }
            input[i] == '*' && (i + 1 < input.length && input[i + 1] != ' ') -> {
                val end = input.indexOf('*', i + 1)
                if (end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(input.substring(i + 1, end))
                    }
                    i = end + 1
                } else {
                    append(input[i]); i++
                }
            }
            else -> {
                append(input[i])
                i++
            }
        }
    }
}
