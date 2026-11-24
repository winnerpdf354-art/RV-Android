package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DateUtilsRu
import com.example.data.LearningStatus
import com.example.data.StudySession
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.StatusPill
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.RuCard
import com.example.ui.theme.subjectColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    sessions: List<StudySession>,
    dailyGoalMinutes: Int,
    habitWeekOffset: Int,
    selectedHabitDate: String?,
    onChangeHabitWeek: (Int) -> Unit,
    onJumpCurrentWeek: () -> Unit,
    onToggleHabitDate: (String) -> Unit,
    onSaveDailyGoal: (Int) -> Unit,
    contentBottomPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    val todayIso = remember { DateUtilsRu.todayIso() }
    val todaySessions = remember(sessions, todayIso) { sessions.filter { it.date == todayIso } }
    val todayMinutes = remember(todaySessions) { todaySessions.sumOf { it.durationMinutes } }
    val todaySubjects = remember(todaySessions) { todaySessions.map { it.subject }.distinct().size }
    val todayChapters = remember(todaySessions) { todaySessions.map { "${it.subject}::${it.chapter}" }.distinct().size }

    var showGoalSheet by remember { mutableStateOf(false) }

    val subjectStats = remember(sessions) {
        sessions.groupBy { it.subject }
            .map { (sub, list) ->
                SubjectBreakdownItem(
                    subject = sub,
                    totalMinutes = list.sumOf { it.durationMinutes },
                    easyCount = list.count { it.status == LearningStatus.EASY },
                    medCount = list.count { it.status == LearningStatus.MEDIUM },
                    hardCount = list.count { it.status == LearningStatus.HARD }
                )
            }
            .sortedByDescending { it.totalMinutes }
            .take(8)
    }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = contentBottomPadding.calculateBottomPadding() + 92.dp
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Daily Study Goal Card
        item(key = "daily_goal") {
            DailyGoalCard(
                todayMinutes = todayMinutes,
                goalMinutes = dailyGoalMinutes,
                onClick = { showGoalSheet = true }
            )
        }

        // 2. Today's 4 Stat Capsules (2x2 Grid)
        item(key = "today_capsules") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCapsuleCard(
                        label = "TODAY'S TIME",
                        value = DateUtilsRu.formatMinutes(todayMinutes),
                        accent = ru.blue,
                        modifier = Modifier.weight(1f)
                    )
                    StatCapsuleCard(
                        label = "TODAY SESSIONS",
                        value = todaySessions.size.toString(),
                        accent = ru.emerald,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatCapsuleCard(
                        label = "TODAY SUBJECTS",
                        value = todaySubjects.toString(),
                        accent = ru.amber,
                        modifier = Modifier.weight(1f)
                    )
                    StatCapsuleCard(
                        label = "TODAY CHAPTERS",
                        value = todayChapters.toString(),
                        accent = ru.purple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. 7-Day Weekly Habit Tracker Card
        item(key = "weekly_habit") {
            WeeklyHabitCard(
                sessions = sessions,
                weekOffset = habitWeekOffset,
                selectedDate = selectedHabitDate,
                onChangeWeek = onChangeHabitWeek,
                onJumpCurrentWeek = onJumpCurrentWeek,
                onToggleDate = onToggleHabitDate
            )
        }

        // 4. Subject-wise Time Breakdown Card
        item(key = "subject_breakdown") {
            SubjectBreakdownCard(items = subjectStats)
        }
    }

    if (showGoalSheet) {
        DailyGoalEditorSheet(
            todayMinutes = todayMinutes,
            currentGoalMinutes = dailyGoalMinutes,
            onSave = { mins ->
                onSaveDailyGoal(mins)
                showGoalSheet = false
            },
            onDismiss = { showGoalSheet = false }
        )
    }
}

@Composable
private fun DailyGoalCard(
    todayMinutes: Int,
    goalMinutes: Int,
    onClick: () -> Unit
) {
    val ru = LocalRuColors.current
    val hasGoal = goalMinutes > 0
    val rawProgress = if (hasGoal) (todayMinutes.toFloat() / goalMinutes.toFloat()).coerceIn(0f, 1f) else 0f
    val animProgress by animateFloatAsState(targetValue = rawProgress, label = "goalProgress")
    val pct = if (hasGoal) ((todayMinutes * 100f) / goalMinutes).roundToInt().coerceAtMost(999) else 0
    val barColor = if (pct >= 100) ru.emerald else ru.blue

    RuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("daily_goal_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(barColor.copy(alpha = 0.16f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = "Daily goal",
                            tint = barColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DAILY STUDY GOAL",
                            style = MaterialTheme.typography.labelSmall,
                            color = ru.mutedText
                        )
                        Text(
                            text = if (hasGoal) {
                                "Today ${DateUtilsRu.formatMinutes(todayMinutes)} / ${DateUtilsRu.formatMinutes(goalMinutes)}"
                            } else {
                                "Set daily goal"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = barColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (hasGoal) "$pct%" else "सेट करें",
                        color = barColor,
                        fontFamily = if (hasGoal) JetBrainsMono else null,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(ru.subtleFill)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(barColor)
                )
            }
        }
    }
}

@Composable
private fun StatCapsuleCard(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    RuCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = ru.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMono,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun WeeklyHabitCard(
    sessions: List<StudySession>,
    weekOffset: Int,
    selectedDate: String?,
    onChangeWeek: (Int) -> Unit,
    onJumpCurrentWeek: () -> Unit,
    onToggleDate: (String) -> Unit
) {
    val ru = LocalRuColors.current
    val todayIso = remember { DateUtilsRu.todayIso() }
    val days = remember(weekOffset) { DateUtilsRu.weekDaysForOffset(weekOffset) }
    val dayLetters = listOf("S", "M", "T", "W", "T", "F", "S")
    val rangeFmt = remember { SimpleDateFormat("dd MMM", Locale.US) }
    val rangeTitle = "${rangeFmt.format(days.first().time)} – ${rangeFmt.format(days.last().time)}"

    val sessionsByDate = remember(sessions) { sessions.groupBy { it.date } }
    var dragAccum by remember { mutableFloatStateOf(0f) }

    RuCard(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(weekOffset) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragAccum > 70f) onChangeWeek(1)
                        else if (dragAccum < -70f && weekOffset > 0) onChangeWeek(-1)
                        dragAccum = 0f
                    },
                    onHorizontalDrag = { _, dragAmount -> dragAccum += dragAmount }
                )
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { onChangeWeek(1) },
                    modifier = Modifier.testTag("habit_prev_week")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous week"
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .clickable { onJumpCurrentWeek() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("habit_date_range")
                ) {
                    if (weekOffset > 0) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(ru.blue)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        text = rangeTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = { if (weekOffset > 0) onChangeWeek(-1) },
                    enabled = weekOffset > 0,
                    modifier = Modifier.testTag("habit_next_week")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next week"
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                days.forEachIndexed { idx, cal ->
                    val iso = DateUtilsRu.calendarToIso(cal)
                    val daySessions = sessionsByDate[iso].orEmpty()
                    val count = daySessions.size
                    val isToday = iso == todayIso
                    val isFuture = iso > todayIso
                    val isSelected = selectedDate == iso
                    val isActive = count > 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isFuture) { onToggleDate(iso) }
                            .padding(vertical = 4.dp)
                            .testTag("habit_day_$idx")
                    ) {
                        Text(
                            text = dayLetters[idx],
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isToday) ru.blue else ru.mutedText
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when {
                                        isActive -> ru.emerald.copy(alpha = if (isSelected) 0.32f else 0.20f)
                                        isSelected -> ru.blue.copy(alpha = 0.16f)
                                        else -> ru.subtleFill
                                    }
                                )
                                .border(
                                    width = if (isSelected || isToday) 1.5.dp else 1.dp,
                                    color = when {
                                        isSelected -> ru.blue
                                        isToday -> ru.emerald
                                        isActive -> ru.emerald.copy(alpha = 0.45f)
                                        else -> ru.cardBorder.copy(alpha = if (isFuture) 0.4f else 0.9f)
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                        ) {
                            when {
                                isToday && isActive -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = "Active streak today",
                                            tint = ru.amber,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "$count",
                                            fontFamily = JetBrainsMono,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ru.emerald
                                        )
                                    }
                                }
                                isActive -> {
                                    Text(
                                        text = "$count",
                                        fontFamily = JetBrainsMono,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ru.emerald
                                    )
                                }
                                else -> {
                                    Text(
                                        text = cal.get(Calendar.DAY_OF_MONTH).toString(),
                                        fontFamily = JetBrainsMono,
                                        fontSize = 11.5.sp,
                                        color = ru.mutedText.copy(alpha = if (isFuture) 0.35f else 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expandable Day Detail Panel
            AnimatedVisibility(
                visible = selectedDate != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                selectedDate?.let { iso ->
                    val list = sessionsByDate[iso].orEmpty()
                    Column(modifier = Modifier.padding(top = 12.dp)) {
                        HorizontalDivider(color = ru.cardBorder)
                        Spacer(Modifier.height(10.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = DateUtilsRu.formatDisplay(iso),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${list.size} सेशन · ${DateUtilsRu.formatMinutes(list.sumOf { it.durationMinutes })}",
                                style = MaterialTheme.typography.labelMedium,
                                color = ru.blue
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        if (list.isEmpty()) {
                            Text(
                                text = "इस दिन कोई सेशन दर्ज नहीं है।",
                                style = MaterialTheme.typography.bodySmall,
                                color = ru.mutedText
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                list.forEach { s ->
                                    val sCol = subjectColor(s.subject)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = ru.subtleFill,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(28.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(sCol)
                                            )
                                            Spacer(Modifier.width(9.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "${s.subject} › ${s.chapter}",
                                                    style = MaterialTheme.typography.labelLarge,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = s.lecture,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = ru.mutedText
                                                    )
                                                    StatusPill(status = s.status)
                                                }
                                            }
                                            Text(
                                                text = "${s.durationMinutes}m",
                                                fontFamily = JetBrainsMono,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = ru.blue
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
    }
}

private data class SubjectBreakdownItem(
    val subject: String,
    val totalMinutes: Int,
    val easyCount: Int,
    val medCount: Int,
    val hardCount: Int
)

@Composable
private fun SubjectBreakdownCard(items: List<SubjectBreakdownItem>) {
    val ru = LocalRuColors.current
    val maxMinutes = remember(items) { items.maxOfOrNull { it.totalMinutes }?.coerceAtLeast(1) ?: 1 }

    RuCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SUBJECT-WISE BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = ru.mutedText
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LegendDot("Clear", ru.emerald)
                    LegendDot("Practice", ru.amber)
                    LegendDot("Relearn", ru.red)
                }
            }

            Spacer(Modifier.height(14.dp))

            if (items.isEmpty()) {
                Text(
                    text = "अभी कोई अध्ययन सत्र नहीं है। पहला सेशन जोड़ने पर विषय-वार विश्लेषण यहाँ दिखेगा।",
                    style = MaterialTheme.typography.bodySmall,
                    color = ru.mutedText
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items.forEach { item ->
                        val sCol = subjectColor(item.subject)
                        val ratio = (item.totalMinutes.toFloat() / maxMinutes.toFloat()).coerceIn(0.06f, 1f)
                        val totalDiff = (item.easyCount + item.medCount + item.hardCount).coerceAtLeast(1)

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(sCol)
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Text(
                                        text = item.subject,
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = DateUtilsRu.formatMinutes(item.totalMinutes),
                                    fontFamily = JetBrainsMono,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = ru.mutedText
                                )
                            }

                            // Proportional time bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(ru.subtleFill)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(sCol)
                                )
                            }

                            // 3-segment difficulty breakdown bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(99.dp))
                                    .background(ru.subtleFill)
                            ) {
                                if (item.easyCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(item.easyCount.toFloat() / totalDiff)
                                            .fillMaxHeight()
                                            .background(ru.emerald)
                                    )
                                }
                                if (item.medCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(item.medCount.toFloat() / totalDiff)
                                            .fillMaxHeight()
                                            .background(ru.amber)
                                    )
                                }
                                if (item.hardCount > 0) {
                                    Box(
                                        modifier = Modifier
                                            .weight(item.hardCount.toFloat() / totalDiff)
                                            .fillMaxHeight()
                                            .background(ru.red)
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
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = LocalRuColors.current.mutedText
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyGoalEditorSheet(
    todayMinutes: Int,
    currentGoalMinutes: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val ru = LocalRuColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedHours by remember {
        mutableFloatStateOf(
            if (currentGoalMinutes > 0) (currentGoalMinutes / 60f).coerceIn(0.5f, 24f) else 3f
        )
    }
    var confirmRemove by remember { mutableStateOf(false) }

    val selectedMinutes = (selectedHours * 60f).roundToInt()
    val previewPct = if (selectedMinutes > 0) ((todayMinutes * 100f) / selectedMinutes).roundToInt() else 0
    val quickHours = listOf(1f, 2f, 3f, 4f, 6f, 8f)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text("दैनिक अध्ययन लक्ष्य (Daily Study Goal)", style = MaterialTheme.typography.titleLarge)

            // Progress Preview
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = ru.subtleFill,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(14.dp)
                ) {
                    Column {
                        Text("आज की प्रगति", style = MaterialTheme.typography.labelMedium, color = ru.mutedText)
                        Text(
                            text = "${DateUtilsRu.formatMinutes(todayMinutes)} / ${DateUtilsRu.formatMinutes(selectedMinutes)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "$previewPct%",
                        fontFamily = JetBrainsMono,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (previewPct >= 100) ru.emerald else ru.blue
                    )
                }
            }

            // Quick hour chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                quickHours.forEach { hr ->
                    val active = kotlin.math.abs(selectedHours - hr) < 0.01f
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) ru.blue.copy(alpha = 0.2f) else ru.subtleFill,
                        border = BorderStroke(1.dp, if (active) ru.blue else ru.cardBorder),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedHours = hr }
                            .testTag("goal_chip_${hr.toInt()}h")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "${hr.toInt()}h",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (active) ru.blue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Custom Stepper (±0.5h)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = ru.subtleFill,
                    border = BorderStroke(1.dp, ru.cardBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { selectedHours = (selectedHours - 0.5f).coerceAtLeast(0.5f) }
                        .testTag("goal_minus_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease 0.5 hour")
                    }
                }
                Spacer(Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val labelText = if (selectedHours % 1f == 0f) "${selectedHours.toInt()}h" else String.format(Locale.US, "%.1fh", selectedHours)
                    Text(
                        text = labelText,
                        fontFamily = JetBrainsMono,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = ru.blue
                    )
                    Text("0.5h – 24h", style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                }
                Spacer(Modifier.width(24.dp))
                Surface(
                    shape = CircleShape,
                    color = ru.subtleFill,
                    border = BorderStroke(1.dp, ru.cardBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { selectedHours = (selectedHours + 0.5f).coerceAtMost(24f) }
                        .testTag("goal_plus_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = "Increase 0.5 hour")
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (currentGoalMinutes > 0) {
                    OutlinedButton(
                        onClick = { confirmRemove = true },
                        border = BorderStroke(1.dp, ru.red.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ru.red),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("goal_remove_btn")
                    ) {
                        Text("लक्ष्य हटाएँ")
                    }
                }
                Button(
                    onClick = { onSave(selectedMinutes) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                        .testTag("goal_save_btn")
                ) {
                    Text("लक्ष्य सहेजें")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }

    if (confirmRemove) {
        ConfirmActionDialog(
            title = "दैनिक लक्ष्य हटाएँ?",
            message = "क्या आप अपना दैनिक अध्ययन लक्ष्य हटाना चाहते हैं?",
            confirmLabel = "हटाएँ",
            onConfirm = {
                confirmRemove = false
                onSave(0)
            },
            onDismiss = { confirmRemove = false }
        )
    }
}
