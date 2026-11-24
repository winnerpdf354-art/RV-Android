package com.example.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ChapterFilterState
import com.example.data.ChapterSortOption
import com.example.data.DateUtilsRu
import com.example.data.HistoryFilterState
import com.example.data.HistorySortOption
import com.example.data.LearningStatus
import com.example.data.StudyMode
import com.example.data.StudySession
import com.example.data.TimeFilterMode
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.subjectColor
import java.util.Calendar
import java.util.Locale

private val MONTH_NAMES = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HistoryFilterBottomSheet(
    initial: HistoryFilterState,
    allSessions: List<StudySession>,
    syllabus: Map<String, Map<String, List<String>>>,
    previewCountProvider: (HistoryFilterState) -> Int,
    onApply: (HistoryFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val ru = LocalRuColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember(initial) { mutableStateOf(initial) }
    var expandedChapterSubject by remember { mutableStateOf<String?>(null) }
    var chapterSearch by remember { mutableStateOf("") }

    val availableYears = remember(allSessions) {
        val yrs = allSessions.mapNotNull { DateUtilsRu.parseIsoCalendar(it.date)?.get(Calendar.YEAR) }.toMutableSet()
        yrs.add(Calendar.getInstance().get(Calendar.YEAR))
        yrs.sortedDescending()
    }
    val allSubjects = remember(allSessions, syllabus) {
        (syllabus.keys + allSessions.map { it.subject }).distinct()
    }
    val liveCount = remember(draft, allSessions) { previewCountProvider(draft) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text("फ़िल्टर और क्रम (Filter & Sort)", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            HorizontalDivider(color = ru.cardBorder)

            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                // 1. Sort By
                SectionHeading("क्रम (Sort By)")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistorySortOption.entries.forEach { opt ->
                        FilterPill(
                            label = opt.labelHi,
                            selected = draft.sort == opt,
                            onClick = { draft = draft.copy(sort = opt) }
                        )
                    }
                }

                // 2. Time Filter (5 Modes)
                TimeFilterSection(
                    timeMode = draft.timeMode,
                    availableYears = availableYears,
                    selectedYear = draft.selectedYear,
                    selectedMonths = draft.selectedMonths,
                    selectedYears = draft.selectedYears,
                    rangeFrom = draft.rangeFrom,
                    rangeTo = draft.rangeTo,
                    exactDate = draft.exactDate,
                    pastDays = draft.pastDays,
                    pastDaysExactAgo = draft.pastDaysExactAgo,
                    onChangeMode = { m -> draft = draft.copy(timeMode = m) },
                    onSelectYear = { y -> draft = draft.copy(selectedYear = y) },
                    onToggleMonth = { m ->
                        val next = if (m in draft.selectedMonths) draft.selectedMonths - m else draft.selectedMonths + m
                        draft = draft.copy(selectedMonths = next)
                    },
                    onToggleYearMulti = { y ->
                        val next = if (y in draft.selectedYears) draft.selectedYears - y else draft.selectedYears + y
                        draft = draft.copy(selectedYears = next)
                    },
                    onRangeFrom = { v -> draft = draft.copy(rangeFrom = v) },
                    onRangeTo = { v -> draft = draft.copy(rangeTo = v) },
                    onExactDate = { v -> draft = draft.copy(exactDate = v) },
                    onPastDays = { d -> draft = draft.copy(pastDays = d) },
                    onPastDaysExactAgo = { ex -> draft = draft.copy(pastDaysExactAgo = ex) }
                )

                // 3. Subject & Chapter Split Pills
                SectionHeading("विषय और अध्याय (Subject & Chapter)")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val allSelected = draft.selectedSubjects.isEmpty() && draft.selectedChapters.values.all { it.isEmpty() }
                    FilterPill(
                        label = "All",
                        selected = allSelected,
                        onClick = {
                            draft = draft.copy(selectedSubjects = emptySet(), selectedChapters = emptyMap())
                            expandedChapterSubject = null
                        }
                    )
                    allSubjects.forEach { sub ->
                        val chaps = draft.selectedChapters[sub].orEmpty()
                        val isSubOn = sub in draft.selectedSubjects || chaps.isNotEmpty()
                        val sCol = subjectColor(sub)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSubOn) sCol.copy(alpha = 0.18f) else ru.subtleFill,
                            border = BorderStroke(1.dp, if (isSubOn) sCol else ru.cardBorder)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable {
                                            val nextSubs = if (sub in draft.selectedSubjects) {
                                                draft.selectedSubjects - sub
                                            } else {
                                                draft.selectedSubjects + sub
                                            }
                                            val nextChaps = draft.selectedChapters - sub
                                            draft = draft.copy(
                                                selectedSubjects = nextSubs,
                                                selectedChapters = nextChaps
                                            )
                                        }
                                        .padding(start = 10.dp, end = 6.dp, top = 7.dp, bottom = 7.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(sCol)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (chaps.isNotEmpty()) "$sub (${chaps.size})" else sub,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSubOn) sCol else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .height(18.dp)
                                        .background(ru.cardBorder)
                                )
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clickable {
                                            expandedChapterSubject = if (expandedChapterSubject == sub) null else sub
                                            chapterSearch = ""
                                        }
                                        .padding(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Pick chapters for $sub",
                                        tint = if (expandedChapterSubject == sub) ru.blue else ru.mutedText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Inline Chapter Picker
                expandedChapterSubject?.let { sub ->
                    val availableChapters = remember(sub, syllabus, allSessions) {
                        val fromSyl = syllabus[sub]?.keys?.toList().orEmpty()
                        val fromSess = allSessions.filter { it.subject == sub }.map { it.chapter }
                        (fromSyl + fromSess).distinct()
                    }
                    val filteredChapters = availableChapters.filter {
                        chapterSearch.isBlank() || it.contains(chapterSearch, ignoreCase = true)
                    }
                    val picked = draft.selectedChapters[sub].orEmpty()

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ru.subtleFill,
                        border = BorderStroke(1.dp, subjectColor(sub).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "$sub — अध्याय चुनें",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = subjectColor(sub)
                                )
                                Row {
                                    TextButton(onClick = {
                                        val map = draft.selectedChapters.toMutableMap()
                                        map[sub] = availableChapters.toSet()
                                        draft = draft.copy(
                                            selectedSubjects = draft.selectedSubjects + sub,
                                            selectedChapters = map
                                        )
                                    }) { Text("All") }
                                    TextButton(onClick = {
                                        val map = draft.selectedChapters.toMutableMap()
                                        map.remove(sub)
                                        draft = draft.copy(selectedChapters = map)
                                    }) { Text("Clear") }
                                }
                            }
                            if (availableChapters.size > 6) {
                                OutlinedTextField(
                                    value = chapterSearch,
                                    onValueChange = { chapterSearch = it },
                                    placeholder = { Text("चैप्टर खोजें...") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                filteredChapters.forEachIndexed { idx, ch ->
                                    val on = ch in picked
                                    FilterPill(
                                        label = "${idx + 1}. $ch",
                                        selected = on,
                                        accent = subjectColor(sub),
                                        onClick = {
                                            val nextSet = if (on) picked - ch else picked + ch
                                            val map = draft.selectedChapters.toMutableMap()
                                            if (nextSet.isEmpty()) map.remove(sub) else map[sub] = nextSet
                                            draft = draft.copy(
                                                selectedSubjects = if (nextSet.isNotEmpty()) draft.selectedSubjects + sub else draft.selectedSubjects,
                                                selectedChapters = map
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Study Mode (Type)
                SectionHeading("प्रकार (Study Mode)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudyMode.entries.forEach { m ->
                        FilterPill(
                            label = "${m.labelHi} (${m.labelEn})",
                            selected = m in draft.selectedModes,
                            onClick = {
                                val next = if (m in draft.selectedModes) draft.selectedModes - m else draft.selectedModes + m
                                draft = draft.copy(selectedModes = next)
                            }
                        )
                    }
                }

                // 5. Learning Status (Difficulty)
                SectionHeading("स्थिति (Learning Status)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(LearningStatus.HARD, LearningStatus.MEDIUM, LearningStatus.EASY).forEach { st ->
                        FilterPill(
                            label = "${st.displayLabel} (${st.hindiShort})",
                            selected = st in draft.selectedStatuses,
                            accent = ru.statusColor(st),
                            onClick = {
                                val next = if (st in draft.selectedStatuses) draft.selectedStatuses - st else draft.selectedStatuses + st
                                draft = draft.copy(selectedStatuses = next)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = ru.cardBorder)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                OutlinedButton(
                    onClick = { draft = HistoryFilterState() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("hf_reset_btn")
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = { onApply(draft) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.8f)
                        .height(48.dp)
                        .testTag("hf_apply_btn")
                ) {
                    Text("Apply • $liveCount Results")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ChapterFilterBottomSheet(
    initial: ChapterFilterState,
    chapterSessions: List<StudySession>,
    availableLectures: List<String>,
    previewCountProvider: (ChapterFilterState) -> Int,
    onApply: (ChapterFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val ru = LocalRuColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var draft by remember(initial) { mutableStateOf(initial) }

    val availableYears = remember(chapterSessions) {
        val yrs = chapterSessions.mapNotNull { DateUtilsRu.parseIsoCalendar(it.date)?.get(Calendar.YEAR) }.toMutableSet()
        yrs.add(Calendar.getInstance().get(Calendar.YEAR))
        yrs.sortedDescending()
    }
    val liveCount = remember(draft, chapterSessions) { previewCountProvider(draft) }

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
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
            ) {
                Text("चैप्टर फ़िल्टर और क्रम", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            HorizontalDivider(color = ru.cardBorder)

            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp)
            ) {
                SectionHeading("क्रम (Sort By)")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ChapterSortOption.entries.forEach { opt ->
                        FilterPill(
                            label = opt.labelHi,
                            selected = draft.sort == opt,
                            onClick = { draft = draft.copy(sort = opt) }
                        )
                    }
                }

                TimeFilterSection(
                    timeMode = draft.timeMode,
                    availableYears = availableYears,
                    selectedYear = draft.selectedYear,
                    selectedMonths = draft.selectedMonths,
                    selectedYears = draft.selectedYears,
                    rangeFrom = draft.rangeFrom,
                    rangeTo = draft.rangeTo,
                    exactDate = draft.exactDate,
                    pastDays = draft.pastDays,
                    pastDaysExactAgo = draft.pastDaysExactAgo,
                    onChangeMode = { m -> draft = draft.copy(timeMode = m) },
                    onSelectYear = { y -> draft = draft.copy(selectedYear = y) },
                    onToggleMonth = { m ->
                        val next = if (m in draft.selectedMonths) draft.selectedMonths - m else draft.selectedMonths + m
                        draft = draft.copy(selectedMonths = next)
                    },
                    onToggleYearMulti = { y ->
                        val next = if (y in draft.selectedYears) draft.selectedYears - y else draft.selectedYears + y
                        draft = draft.copy(selectedYears = next)
                    },
                    onRangeFrom = { v -> draft = draft.copy(rangeFrom = v) },
                    onRangeTo = { v -> draft = draft.copy(rangeTo = v) },
                    onExactDate = { v -> draft = draft.copy(exactDate = v) },
                    onPastDays = { d -> draft = draft.copy(pastDays = d) },
                    onPastDaysExactAgo = { ex -> draft = draft.copy(pastDaysExactAgo = ex) }
                )

                SectionHeading("लेक्चर चुनें (Lectures)")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterPill(
                        label = "All",
                        selected = draft.selectedLectures.isEmpty(),
                        onClick = { draft = draft.copy(selectedLectures = emptySet()) }
                    )
                    availableLectures.forEach { lec ->
                        FilterPill(
                            label = lec,
                            selected = lec in draft.selectedLectures,
                            onClick = {
                                val next = if (lec in draft.selectedLectures) draft.selectedLectures - lec else draft.selectedLectures + lec
                                draft = draft.copy(selectedLectures = next)
                            }
                        )
                    }
                }

                SectionHeading("प्रकार (Study Mode)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudyMode.entries.forEach { m ->
                        FilterPill(
                            label = m.labelHi,
                            selected = m in draft.selectedModes,
                            onClick = {
                                val next = if (m in draft.selectedModes) draft.selectedModes - m else draft.selectedModes + m
                                draft = draft.copy(selectedModes = next)
                            }
                        )
                    }
                }

                SectionHeading("स्थिति (Learning Status)")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(LearningStatus.HARD, LearningStatus.MEDIUM, LearningStatus.EASY).forEach { st ->
                        FilterPill(
                            label = st.displayLabel,
                            selected = st in draft.selectedStatuses,
                            accent = ru.statusColor(st),
                            onClick = {
                                val next = if (st in draft.selectedStatuses) draft.selectedStatuses - st else draft.selectedStatuses + st
                                draft = draft.copy(selectedStatuses = next)
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = ru.cardBorder)
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                OutlinedButton(
                    onClick = { draft = ChapterFilterState() },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Reset")
                }
                Button(
                    onClick = { onApply(draft) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1.8f)
                        .height(48.dp)
                ) {
                    Text("Apply • $liveCount Results")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeFilterSection(
    timeMode: TimeFilterMode,
    availableYears: List<Int>,
    selectedYear: Int?,
    selectedMonths: Set<Int>,
    selectedYears: Set<Int>,
    rangeFrom: String,
    rangeTo: String,
    exactDate: String,
    pastDays: Int,
    pastDaysExactAgo: Boolean,
    onChangeMode: (TimeFilterMode) -> Unit,
    onSelectYear: (Int?) -> Unit,
    onToggleMonth: (Int) -> Unit,
    onToggleYearMulti: (Int) -> Unit,
    onRangeFrom: (String) -> Unit,
    onRangeTo: (String) -> Unit,
    onExactDate: (String) -> Unit,
    onPastDays: (Int) -> Unit,
    onPastDaysExactAgo: (Boolean) -> Unit
) {
    val ru = LocalRuColors.current
    val context = LocalContext.current

    fun openNativeDatePicker(currentIso: String, onPicked: (String) -> Unit) {
        val cal = DateUtilsRu.parseIsoCalendar(currentIso) ?: Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, y, m, d ->
                val iso = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                onPicked(iso)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeading("समय (Time)")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TimeFilterMode.entries.forEach { m ->
                FilterPill(
                    label = m.label,
                    selected = timeMode == m,
                    onClick = { onChangeMode(m) }
                )
            }
        }

        when (timeMode) {
            TimeFilterMode.NONE -> Unit
            TimeFilterMode.MONTH -> {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterPill(label = "All Years", selected = selectedYear == null, onClick = { onSelectYear(null) })
                    availableYears.forEach { yr ->
                        FilterPill(
                            label = yr.toString(),
                            selected = selectedYear == yr,
                            onClick = { onSelectYear(yr) }
                        )
                    }
                }
                // 4x3 Month Grid
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    MONTH_NAMES.chunked(4).forEachIndexed { rowIdx, rowMonths ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowMonths.forEachIndexed { colIdx, mName ->
                                val monthIdx = rowIdx * 4 + colIdx
                                val on = monthIdx in selectedMonths
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (on) ru.blue.copy(alpha = 0.2f) else ru.subtleFill,
                                    border = BorderStroke(1.dp, if (on) ru.blue else ru.cardBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onToggleMonth(monthIdx) }
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = mName,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (on) ru.blue else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            TimeFilterMode.YEAR -> {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableYears.forEach { yr ->
                        FilterPill(
                            label = yr.toString(),
                            selected = yr in selectedYears,
                            onClick = { onToggleYearMulti(yr) }
                        )
                    }
                }
            }
            TimeFilterMode.RANGE -> {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DatePickBox(
                        label = "From",
                        value = rangeFrom,
                        onClick = { openNativeDatePicker(rangeFrom, onRangeFrom) },
                        modifier = Modifier.weight(1f)
                    )
                    DatePickBox(
                        label = "To",
                        value = rangeTo,
                        onClick = { openNativeDatePicker(rangeTo, onRangeTo) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            TimeFilterMode.EXACT -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val today = DateUtilsRu.todayIso()
                    val yesterday = DateUtilsRu.daysAgoIso(1)
                    FilterPill("Today", selected = exactDate == today, onClick = { onExactDate(today) })
                    FilterPill("Yesterday", selected = exactDate == yesterday, onClick = { onExactDate(yesterday) })
                }
                DatePickBox(
                    label = "Select Date",
                    value = exactDate,
                    onClick = { openNativeDatePicker(exactDate, onExactDate) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            TimeFilterMode.PAST_DAYS -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 7, 15, 30).forEach { d ->
                        FilterPill(
                            label = "${d}d",
                            selected = pastDays == d,
                            onClick = { onPastDays(d) }
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onPastDays((pastDays - 1).coerceAtLeast(1)) }) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease days")
                        }
                        Text(
                            text = "$pastDays दिन",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.titleMedium,
                            color = ru.blue
                        )
                        IconButton(onClick = { onPastDays((pastDays + 1).coerceAtMost(365)) }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase days")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterPill(
                            label = "Last ${pastDays}d",
                            selected = !pastDaysExactAgo,
                            onClick = { onPastDaysExactAgo(false) }
                        )
                        FilterPill(
                            label = "${pastDays}d ago",
                            selected = pastDaysExactAgo,
                            onClick = { onPastDaysExactAgo(true) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePickBox(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ru.subtleFill,
        border = BorderStroke(1.dp, ru.cardBorder),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                Text(
                    text = value.ifBlank { "तारीख चुनें" },
                    fontFamily = if (value.isNotBlank()) JetBrainsMono else null,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = label,
                tint = ru.blue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SectionHeading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun FilterPill(
    label: String,
    selected: Boolean,
    accent: Color = LocalRuColors.current.blue,
    onClick: () -> Unit
) {
    val ru = LocalRuColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = 0.20f) else ru.subtleFill,
        border = BorderStroke(1.dp, if (selected) accent else ru.cardBorder),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp)
        )
    }
}
