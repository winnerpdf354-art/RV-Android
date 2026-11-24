package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DateUtilsRu
import com.example.data.DefaultSyllabusData
import com.example.data.DraftSessionState
import com.example.data.LearningStatus
import com.example.data.StudyMode
import com.example.data.extractUrlsFromText
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.parseHexColor
import com.example.ui.theme.subjectColor
import kotlin.math.roundToInt

private enum class PickerStep { SUBJECT, CHAPTER, LECTURE }

@Composable
fun StudySessionScreen(
    draft: DraftSessionState,
    syllabus: Map<String, Map<String, List<String>>>,
    errorText: String?,
    onBack: () -> Unit,
    onSelectSubject: (String) -> Unit,
    onSelectChapter: (String) -> Unit,
    onSelectLecture: (String) -> Unit,
    onSetMode: (StudyMode) -> Unit,
    onSetStatus: (LearningStatus) -> Unit,
    onSetManualMinutes: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onTogglePauseResume: () -> Unit,
    onStopTimer: () -> Unit,
    onResumeFromReview: () -> Unit,
    onResetTimer: () -> Unit,
    onSaveSession: () -> Unit,
    onOpenNotesEditor: () -> Unit
) {
    val ru = LocalRuColors.current
    val haptics = LocalHapticFeedback.current
    var pickerStep by remember { mutableStateOf<PickerStep?>(null) }
    var showStatusInfoSheet by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    val chaptersForSubject = remember(syllabus, draft.subject) {
        syllabus[draft.subject]?.keys?.toList().orEmpty()
    }
    val lecturesForChapter = remember(syllabus, draft.subject, draft.chapter) {
        syllabus[draft.subject]?.get(draft.chapter).orEmpty()
    }

    Surface(
        color = ru.studyBg,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding()
        ) {
            // Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("study_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close study session")
                }
                Text(
                    text = "Study Session",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                // Mode Segmented Toggle (Revision / First Study)
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = ru.studyCard,
                    border = BorderStroke(1.dp, ru.cardBorder)
                ) {
                    Row(modifier = Modifier.padding(3.dp)) {
                        StudyMode.entries.forEach { m ->
                            val selected = draft.mode == m
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = if (selected) ru.blue.copy(alpha = 0.22f) else Color.Transparent,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(99.dp))
                                    .clickable(enabled = !draft.isTimerLocked) { onSetMode(m) }
                                    .testTag("study_mode_${m.name.lowercase()}")
                            ) {
                                Text(
                                    text = m.labelEn,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (selected) ru.blue else ru.mutedText.copy(alpha = if (draft.isTimerLocked) 0.5f else 1f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Scrollable Cockpit Body
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Card 1: Connected Thread Selector (Subject -> Chapter -> Lecture)
                ThreadSelectorCard(
                    subject = draft.subject,
                    chapter = draft.chapter,
                    lecture = draft.lecture,
                    locked = draft.isTimerLocked,
                    onPickSubject = { pickerStep = PickerStep.SUBJECT },
                    onPickChapter = { if (draft.subject.isNotBlank()) pickerStep = PickerStep.CHAPTER },
                    onPickLecture = { if (draft.chapter.isNotBlank()) pickerStep = PickerStep.LECTURE }
                )

                // Card 2: Fixed-Height Unified Manual Dial & Live Stopwatch
                TimeCockpitCard(
                    draft = draft,
                    onSetManualMinutes = {
                        onSetManualMinutes(it)
                    },
                    onStartTimer = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStartTimer()
                    },
                    onTogglePauseResume = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onTogglePauseResume()
                    },
                    onStopTimer = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onStopTimer()
                    },
                    onResetTimer = onResetTimer
                )

                // Card 3: Learning Status + Notes Telemetry Capsule
                StatusAndNotesCard(
                    draft = draft,
                    onSetStatus = onSetStatus,
                    onOpenStatusGuide = { showStatusInfoSheet = true },
                    onOpenNotes = onOpenNotesEditor
                )

                // Inline Error Banner
                if (!errorText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ru.red.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, ru.red.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorText,
                            color = ru.red,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }

                // Motivational Hindi Quote
                Text(
                    text = "\"गति से ज़्यादा ज़रूरी है, चलते रहना।\"",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = ru.mutedText,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                )
            }

            // Sticky Bottom Save / End / Review Dock
            StudyStickyFooter(
                draft = draft,
                onSave = onSaveSession,
                onEndTimer = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStopTimer()
                },
                onResumeFromReview = onResumeFromReview,
                onDismissReview = onResetTimer
            )
        }
    }

    // Searchable Picker Bottom Sheet for Subject / Chapter / Lecture
    pickerStep?.let { step ->
        val (title, options, currentVal) = when (step) {
            PickerStep.SUBJECT -> Triple("विषय चुनें (Subject)", syllabus.keys.toList(), draft.subject)
            PickerStep.CHAPTER -> Triple("अध्याय चुनें (Chapter)", chaptersForSubject, draft.chapter)
            PickerStep.LECTURE -> Triple("लेक्चर चुनें (Lecture)", lecturesForChapter, draft.lecture)
        }
        SearchableOptionSheet(
            title = title,
            options = options,
            selectedOption = currentVal,
            useSubjectColors = step == PickerStep.SUBJECT,
            onSelect = { chosen ->
                when (step) {
                    PickerStep.SUBJECT -> onSelectSubject(chosen)
                    PickerStep.CHAPTER -> onSelectChapter(chosen)
                    PickerStep.LECTURE -> onSelectLecture(chosen)
                }
                pickerStep = null
            },
            onDismiss = { pickerStep = null }
        )
    }

    if (showStatusInfoSheet) {
        SessionStatusGuideSheet(
            currentStatus = draft.status,
            onDismiss = { showStatusInfoSheet = false }
        )
    }
}

@Composable
private fun ThreadSelectorCard(
    subject: String,
    chapter: String,
    lecture: String,
    locked: Boolean,
    onPickSubject: () -> Unit,
    onPickChapter: () -> Unit,
    onPickLecture: () -> Unit
) {
    val ru = LocalRuColors.current
    val accent = if (subject.isNotBlank()) subjectColor(subject) else ru.emerald

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ru.studyCard,
        border = BorderStroke(1.dp, if (subject.isNotBlank()) accent.copy(alpha = 0.35f) else ru.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            ThreadStepRow(
                stepLabel = "SUBJECT",
                value = subject,
                placeholder = "विषय चुनें...",
                isDone = subject.isNotBlank(),
                isNext = subject.isBlank(),
                isEnabled = !locked,
                showConnectorBelow = true,
                accent = accent,
                onClick = onPickSubject,
                testTag = "study_pick_subject"
            )
            ThreadStepRow(
                stepLabel = "CHAPTER",
                value = chapter,
                placeholder = if (subject.isBlank()) "पहले विषय चुनें" else "अध्याय चुनें...",
                isDone = chapter.isNotBlank(),
                isNext = subject.isNotBlank() && chapter.isBlank(),
                isEnabled = !locked && subject.isNotBlank(),
                showConnectorBelow = true,
                accent = accent,
                onClick = onPickChapter,
                testTag = "study_pick_chapter"
            )
            ThreadStepRow(
                stepLabel = "LECTURE",
                value = lecture,
                placeholder = if (chapter.isBlank()) "पहले अध्याय चुनें" else "लेक्चर चुनें...",
                isDone = lecture.isNotBlank(),
                isNext = chapter.isNotBlank() && lecture.isBlank(),
                isEnabled = !locked && chapter.isNotBlank(),
                showConnectorBelow = false,
                accent = accent,
                onClick = onPickLecture,
                testTag = "study_pick_lecture"
            )
        }
    }
}

@Composable
private fun ThreadStepRow(
    stepLabel: String,
    value: String,
    placeholder: String,
    isDone: Boolean,
    isNext: Boolean,
    isEnabled: Boolean,
    showConnectorBelow: Boolean,
    accent: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val ru = LocalRuColors.current
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isEnabled) { onClick() }
            .padding(vertical = 4.dp, horizontal = 4.dp)
            .testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(26.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> ru.emerald
                            isNext -> accent.copy(alpha = 0.2f)
                            else -> ru.subtleFill
                        }
                    )
                    .border(
                        width = if (isNext) 1.5.dp else 1.dp,
                        color = when {
                            isDone -> ru.emerald
                            isNext -> accent
                            else -> ru.cardBorder
                        },
                        shape = CircleShape
                    )
            ) {
                when {
                    isDone -> Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                    isNext -> Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
                }
            }
            if (showConnectorBelow) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(26.dp)
                        .background(if (isDone) ru.emerald.copy(alpha = 0.65f) else ru.cardBorder)
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stepLabel,
                style = MaterialTheme.typography.labelSmall,
                color = ru.mutedText.copy(alpha = if (isEnabled) 1f else 0.45f)
            )
            Text(
                text = value.ifBlank { placeholder },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (value.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal,
                color = when {
                    value.isNotBlank() -> MaterialTheme.colorScheme.onSurface
                    isEnabled -> ru.mutedText
                    else -> ru.mutedText.copy(alpha = 0.45f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TimeCockpitCard(
    draft: DraftSessionState,
    onSetManualMinutes: (Int) -> Unit,
    onStartTimer: () -> Unit,
    onTogglePauseResume: () -> Unit,
    onStopTimer: () -> Unit,
    onResetTimer: () -> Unit
) {
    val ru = LocalRuColors.current
    val showTimer = draft.isTimerMode || draft.isRunning || draft.elapsedSeconds > 0L

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ru.studyCard,
        border = BorderStroke(
            1.dp,
            when {
                draft.isRunning -> ru.emerald.copy(alpha = 0.45f)
                showTimer -> ru.amber.copy(alpha = 0.40f)
                else -> ru.cardBorder
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(212.dp)
    ) {
        if (!showTimer) {
            ManualDialContent(
                minutes = draft.manualMinutes,
                onSetMinutes = onSetManualMinutes,
                onStartTimer = onStartTimer
            )
        } else {
            LiveStopwatchContent(
                draft = draft,
                onTogglePauseResume = onTogglePauseResume,
                onStopTimer = onStopTimer,
                onResetTimer = onResetTimer
            )
        }
    }
}

@Composable
private fun ManualDialContent(
    minutes: Int,
    onSetMinutes: (Int) -> Unit,
    onStartTimer: () -> Unit
) {
    val ru = LocalRuColors.current
    val haptics = LocalHapticFeedback.current
    var textValue by remember(minutes) { mutableStateOf(minutes.toString()) }
    var dragRemainder by remember { mutableFloatStateOf(0f) }

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Row: - [ 25 min ] +  and Play Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = CircleShape,
                    color = ru.subtleFill,
                    border = BorderStroke(1.dp, ru.cardBorder),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onSetMinutes((minutes - 1).coerceAtLeast(1)) }
                        .testTag("manual_minus_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease 1 minute")
                    }
                }

                Row(verticalAlignment = Alignment.Bottom) {
                    BasicTextField(
                        value = textValue,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(4)
                            textValue = digits
                            digits.toIntOrNull()?.let { v ->
                                if (v in 1..1440) onSetMinutes(v)
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            fontFamily = JetBrainsMono,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(ru.blue),
                        modifier = Modifier
                            .width(86.dp)
                            .testTag("manual_minutes_input")
                    )
                    Text(
                        text = "min",
                        style = MaterialTheme.typography.labelMedium,
                        color = ru.mutedText,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ru.subtleFill,
                    border = BorderStroke(1.dp, ru.cardBorder),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { onSetMinutes((minutes + 1).coerceAtMost(1440)) }
                        .testTag("manual_plus_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Increase 1 minute")
                    }
                }
            }

            // Circular Play button -> switches to Live Stopwatch
            Surface(
                shape = CircleShape,
                color = ru.emerald,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .clickable { onStartTimer() }
                    .testTag("study_start_timer_btn")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start live timer",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }

        // Tactile Horizontal Scrubbable Ruler Dial (5m - 180m)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(ru.subtleFill)
                .pointerInput(minutes) {
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { _, dragAmount ->
                            dragRemainder -= dragAmount
                            val stepPx = 10f
                            val deltaMins = (dragRemainder / stepPx).toInt()
                            if (deltaMins != 0) {
                                dragRemainder -= deltaMins * stepPx
                                val next = (minutes + deltaMins).coerceIn(5, 180)
                                if (next != minutes) {
                                    if (next % 5 == 0) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    onSetMinutes(next)
                                }
                            }
                        }
                    )
                }
                .testTag("tactile_ruler_dial")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val spacing = 10f
                val centerVal = minutes.coerceIn(5, 180)
                for (v in (centerVal - 22)..(centerVal + 22)) {
                    if (v !in 5..180) continue
                    val x = centerX + (v - centerVal) * spacing
                    if (x < 8f || x > size.width - 8f) continue
                    val isMajor = v % 15 == 0
                    val isMid = v % 5 == 0
                    val h = when {
                        isMajor -> size.height * 0.62f
                        isMid -> size.height * 0.42f
                        else -> size.height * 0.24f
                    }
                    val col = when {
                        isMajor -> ru.blue.copy(alpha = 0.85f)
                        isMid -> ru.mutedText.copy(alpha = 0.65f)
                        else -> ru.mutedText.copy(alpha = 0.30f)
                    }
                    drawLine(
                        color = col,
                        start = Offset(x, size.height - 6f),
                        end = Offset(x, size.height - 6f - h),
                        strokeWidth = if (isMajor) 2.6f else 1.5f,
                        cap = StrokeCap.Round
                    )
                }
                // Center indicator needle
                drawLine(
                    color = ru.emerald,
                    start = Offset(centerX, 4f),
                    end = Offset(centerX, size.height - 4f),
                    strokeWidth = 3.2f,
                    cap = StrokeCap.Round
                )
            }
        }

        // Quick Preset Pills (15m, 25m, 45m, 60m)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            listOf(15, 25, 45, 60).forEach { preset ->
                val active = minutes == preset
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (active) ru.blue.copy(alpha = 0.22f) else ru.subtleFill,
                    border = BorderStroke(1.dp, if (active) ru.blue else ru.cardBorder),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onSetMinutes(preset) }
                        .testTag("preset_${preset}m")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 7.dp)) {
                        Text(
                            text = "${preset}m",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (active) ru.blue else ru.mutedText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LiveStopwatchContent(
    draft: DraftSessionState,
    onTogglePauseResume: () -> Unit,
    onStopTimer: () -> Unit,
    onResetTimer: () -> Unit
) {
    val ru = LocalRuColors.current
    val statusColor = if (draft.isRunning) ru.emerald else ru.amber
    val litTicks = ((draft.elapsedSeconds % 3600L) / 60L).toInt().coerceIn(0, 60)

    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = if (draft.isRunning) "Focusing" else "Paused",
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = DateUtilsRu.formatDurationClock(draft.elapsedSeconds),
                    fontFamily = JetBrainsMono,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 3 Circular Transport Buttons: Pause/Resume, Stop, Reset
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                    shape = CircleShape,
                    color = statusColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, statusColor),
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable { onTogglePauseResume() }
                        .testTag("timer_pause_resume_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (draft.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (draft.isRunning) "Pause" else "Resume",
                            tint = statusColor
                        )
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = ru.red.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, ru.red.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable { onStopTimer() }
                        .testTag("timer_stop_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Stop, contentDescription = "End session", tint = ru.red)
                    }
                }
                Surface(
                    shape = CircleShape,
                    color = ru.subtleFill,
                    border = BorderStroke(1.dp, ru.cardBorder),
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .clickable { onResetTimer() }
                        .testTag("timer_reset_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset timer", tint = ru.mutedText)
                    }
                }
            }
        }

        // 60-Tick Linear Chronograph
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
        ) {
            val totalTicks = 60
            val step = size.width / totalTicks
            for (i in 0 until totalTicks) {
                val x = i * step + step / 2f
                val isLit = i < litTicks || (draft.elapsedSeconds > 0 && i == 0)
                val isMajor = i % 5 == 0
                val h = if (isMajor) size.height * 0.78f else size.height * 0.45f
                drawLine(
                    color = if (isLit) statusColor else ru.mutedText.copy(alpha = 0.25f),
                    start = Offset(x, size.height),
                    end = Offset(x, size.height - h),
                    strokeWidth = if (isMajor) 2.5f else 1.5f,
                    cap = StrokeCap.Round
                )
            }
        }

        Text(
            text = if (draft.isRunning) "लाइव सेशन चल रहा है · बैकग्राउंड में भी चलता रहेगा" else "रुका हुआ है · जारी रखें या End दबाएँ",
            style = MaterialTheme.typography.labelSmall,
            color = ru.mutedText
        )
    }
}

@Composable
private fun StatusAndNotesCard(
    draft: DraftSessionState,
    onSetStatus: (LearningStatus) -> Unit,
    onOpenStatusGuide: () -> Unit,
    onOpenNotes: () -> Unit
) {
    val ru = LocalRuColors.current
    val notes = draft.activeNotes
    val groupedByColor = remember(notes) {
        notes.groupBy { it.labelName to it.colorHex }
            .map { (key, list) -> Triple(key.first, key.second, list.size) }
    }
    val linkCount = remember(notes) {
        notes.sumOf { extractUrlsFromText(it.content).distinct().size }
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ru.studyCard,
        border = BorderStroke(1.dp, ru.cardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(14.dp)
        ) {
            // Learning Status Header + Info Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("LEARNING STATUS", style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .clickable { onOpenStatusGuide() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("status_info_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Session Status Guide",
                        tint = ru.blue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("गाइड", style = MaterialTheme.typography.labelSmall, color = ru.blue)
                }
            }

            // 3 Status Buttons: Clear / Practice / Relearn
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    Triple(LearningStatus.EASY, "Clear", "पक्का समझ आया"),
                    Triple(LearningStatus.MEDIUM, "Practice", "अभ्यास ज़रूरी है"),
                    Triple(LearningStatus.HARD, "Relearn", "दोबारा पढ़ना है")
                ).forEach { (st, title, sub) ->
                    val selected = draft.status == st
                    val col = ru.statusColor(st)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) col.copy(alpha = 0.20f) else ru.subtleFill,
                        border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) col else ru.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSetStatus(st) }
                            .testTag("status_btn_${st.name.lowercase()}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp)
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) col else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = sub,
                                style = MaterialTheme.typography.labelSmall,
                                color = ru.mutedText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = ru.cardBorder)

            // Notes Telemetry Capsule
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ru.subtleFill,
                border = BorderStroke(1.dp, ru.cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onOpenNotes() }
                    .testTag("notes_telemetry_capsule")
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        if (notes.isEmpty()) {
                            Text(
                                text = "सूत्र, ट्रिक या लिंक जोड़ें",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ru.mutedText
                            )
                            Icon(Icons.Default.Add, contentDescription = "Add notes", tint = ru.blue)
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                groupedByColor.take(3).forEach { (lbl, hex, cnt) ->
                                    val c = parseHexColor(hex, ru.blue)
                                    Surface(
                                        shape = RoundedCornerShape(99.dp),
                                        color = c.copy(alpha = 0.18f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Box(Modifier.size(7.dp).clip(CircleShape).background(c))
                                            Spacer(Modifier.width(5.dp))
                                            Text("$lbl $cnt", style = MaterialTheme.typography.labelSmall, color = c)
                                        }
                                    }
                                }
                                if (groupedByColor.size > 3) {
                                    Text(
                                        text = "+${groupedByColor.size - 3}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ru.mutedText
                                    )
                                }
                                if (linkCount > 0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = ru.blue, modifier = Modifier.size(13.dp))
                                        Text("$linkCount", style = MaterialTheme.typography.labelSmall, color = ru.blue)
                                    }
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = "Open notes editor",
                                tint = ru.blue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Multi-color proportional bottom bar when notes exist
                    if (groupedByColor.isNotEmpty()) {
                        val total = notes.size.coerceAtLeast(1)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        ) {
                            groupedByColor.forEach { (_, hex, cnt) ->
                                Box(
                                    modifier = Modifier
                                        .weight(cnt.toFloat() / total)
                                        .fillMaxHeight()
                                        .background(parseHexColor(hex, ru.blue))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudyStickyFooter(
    draft: DraftSessionState,
    onSave: () -> Unit,
    onEndTimer: () -> Unit,
    onResumeFromReview: () -> Unit,
    onDismissReview: () -> Unit
) {
    val ru = LocalRuColors.current
    Surface(
        color = ru.studyCard,
        border = BorderStroke(1.dp, ru.cardBorder),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                draft.isReviewingStopped -> {
                    val mins = ((draft.elapsedSeconds + 30L) / 60L).toInt().coerceAtLeast(1)
                    IconButton(onClick = onResumeFromReview) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume timer", tint = ru.emerald)
                    }
                    Column(modifier = Modifier.weight(1f).padding(horizontal = 6.dp)) {
                        Text("$mins Min · ${draft.status.displayLabel}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("सेव करने के लिए तैयार", style = MaterialTheme.typography.labelSmall, color = ru.emerald)
                    }
                    Button(
                        onClick = onSave,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ru.emerald),
                        modifier = Modifier.testTag("study_review_save_btn")
                    ) {
                        Text("Save", color = Color.White)
                    }
                    IconButton(onClick = onDismissReview) {
                        Icon(Icons.Default.Close, contentDescription = "Discard timer", tint = ru.mutedText)
                    }
                }
                draft.isRunning || draft.elapsedSeconds > 0L -> {
                    val dotCol = if (draft.isRunning) ru.emerald else ru.amber
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(dotCol))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${if (draft.isRunning) "Focusing" else "Paused"} · ${DateUtilsRu.formatDurationClock(draft.elapsedSeconds)} · ${draft.status.displayLabel}",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Button(
                        onClick = onEndTimer,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ru.red),
                        modifier = Modifier.testTag("study_end_timer_btn")
                    ) {
                        Text("End", color = Color.White)
                    }
                }
                else -> {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${draft.manualMinutes} min · ${draft.status.displayLabel}",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = draft.mode.labelHi,
                            style = MaterialTheme.typography.labelSmall,
                            color = ru.mutedText
                        )
                    }
                    Button(
                        onClick = onSave,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("study_save_btn")
                    ) {
                        Text("Save Session")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchableOptionSheet(
    title: String,
    options: List<String>,
    selectedOption: String,
    useSubjectColors: Boolean,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val ru = LocalRuColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val filtered = remember(options, query) {
        if (query.isBlank()) options else options.filter { it.contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.75f)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 10.dp))
            if (options.size > 6) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("खोजें...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filtered, key = { it }) { item ->
                    val active = item == selectedOption
                    val accent = if (useSubjectColors) subjectColor(item) else ru.blue
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) accent.copy(alpha = 0.18f) else ru.subtleFill,
                        border = BorderStroke(1.dp, if (active) accent else ru.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(item) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                if (useSubjectColors) {
                                    Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
                                    Spacer(Modifier.width(10.dp))
                                }
                                Text(
                                    text = item,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) accent else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (active) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SessionStatusGuideSheet(
    currentStatus: LearningStatus,
    onDismiss: () -> Unit
) {
    val ru = LocalRuColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var guideTab by remember { mutableIntStateOf(0) } // 0: Session Status (3), 1: Notes Levels (5)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .navigationBarsPadding()
                .padding(horizontal = 18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Session Status गाइड", style = MaterialTheme.typography.titleLarge)
                    Text("सड़क और ड्राइवर का फर्क", style = MaterialTheme.typography.labelMedium, color = ru.blue)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close guide")
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterPill(
                    label = "भाग 1: Session Status (3)",
                    selected = guideTab == 0,
                    onClick = { guideTab = 0 }
                )
                FilterPill(
                    label = "भाग 2: Notes के स्तर (5)",
                    selected = guideTab == 1,
                    onClick = { guideTab = 1 }
                )
            }

            Spacer(Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ru.blue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, ru.blue.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = DefaultSyllabusData.GUIDE_INTRO,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Text(
                    text = if (guideTab == 0) DefaultSyllabusData.STATUS_GUIDE_NOTE else DefaultSyllabusData.NOTE_LEVEL_GUIDE_NOTE,
                    style = MaterialTheme.typography.labelMedium,
                    color = ru.mutedText
                )

                val items = if (guideTab == 0) {
                    DefaultSyllabusData.sessionStatusGuides
                } else {
                    DefaultSyllabusData.noteLevelGuides.values.toList()
                }

                items.forEach { item ->
                    val isSelected = guideTab == 0 && item.key.equals(currentStatus.key, ignoreCase = true)
                    val col = when (item.key.lowercase()) {
                        "easy" -> ru.emerald
                        "medium" -> ru.amber
                        "hard" -> ru.red
                        "complex" -> ru.purple
                        else -> ru.blue
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) col.copy(alpha = 0.14f) else ru.subtleFill,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) col else ru.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${item.name} · ${item.subtitle}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = col,
                                    fontWeight = FontWeight.Bold
                                )
                                if (isSelected) {
                                    Text("✓ चुना हुआ", style = MaterialTheme.typography.labelSmall, color = col)
                                }
                            }
                            Text("यह क्या करता है? ${item.whatItDoes}", style = MaterialTheme.typography.bodySmall)
                            item.difference?.let { diff ->
                                Text(diff, style = MaterialTheme.typography.labelSmall, color = col)
                            }
                            Text("उदाहरण: ${item.example}", style = MaterialTheme.typography.bodySmall, color = ru.mutedText)
                            Text("क्यों ज़रूरी है? ${item.whyNeeded}", style = MaterialTheme.typography.bodySmall)
                            Text("अगर न हो तो: ${item.withoutIt}", style = MaterialTheme.typography.bodySmall, color = ru.mutedText)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
        }
    }
}
