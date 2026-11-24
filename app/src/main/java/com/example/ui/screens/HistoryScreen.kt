package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DateUtilsRu
import com.example.data.HistoryFilterState
import com.example.data.HistorySortOption
import com.example.data.StudySession
import com.example.data.TimeFilterMode
import com.example.data.ViewMode
import com.example.ui.components.EmptyStateIllustration
import com.example.ui.components.ModePill
import com.example.ui.components.RichNoteCardsViewer
import com.example.ui.components.StatusPill
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.RuCard
import com.example.ui.theme.subjectColor

@Composable
fun HistoryScreen(
    allSessionsCount: Int,
    sessions: List<StudySession>,
    viewMode: ViewMode,
    filter: HistoryFilterState,
    searchQuery: String,
    selectedIds: Set<String>,
    onToggleSelect: (String) -> Unit,
    onUpdateFilter: (HistoryFilterState) -> Unit,
    onResetFilter: () -> Unit,
    onStartStudy: () -> Unit,
    contentBottomPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    val selectionMode = selectedIds.isNotEmpty()
    var expandedNoteIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun toggleNote(id: String) {
        expandedNoteIds = if (id in expandedNoteIds) expandedNoteIds - id else expandedNoteIds + id
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Active Filter Chips Bar
        if (filter.activeCount > 0) {
            ActiveHistoryFilterChips(
                filter = filter,
                onUpdate = onUpdateFilter,
                onClearAll = onResetFilter
            )
        }

        if (sessions.isEmpty()) {
            if (allSessionsCount == 0) {
                EmptyStateIllustration(
                    isSearchEmpty = false,
                    title = "शुरुआत करें",
                    subtitle = "अपना पहला स्टडी या रिवीजन सेशन शुरू करें — सारा इतिहास और नोट्स यहाँ सहेजे जाएँगे।",
                    actionLabel = "+ नया स्टडी सेशन",
                    onAction = onStartStudy,
                    modifier = Modifier.weight(1f)
                )
            } else {
                EmptyStateIllustration(
                    isSearchEmpty = true,
                    title = "कुछ नहीं मिला",
                    subtitle = if (searchQuery.isNotBlank()) "\"$searchQuery\" से मिलता कोई रिकॉर्ड नहीं मिला।" else "चुने गए फ़िल्टर से कोई सेशन मेल नहीं खाता।",
                    actionLabel = "फ़िल्टर हटाएँ",
                    onAction = onResetFilter,
                    modifier = Modifier.weight(1f)
                )
            }
            return
        }

        val listPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = contentBottomPadding.calculateBottomPadding() + 92.dp
        )

        when (viewMode) {
            ViewMode.LIST -> {
                LazyColumn(
                    contentPadding = listPadding,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sessions, key = { it.id }) { session ->
                        HistoryListCard(
                            session = session,
                            isSelected = session.id in selectedIds,
                            selectionMode = selectionMode,
                            isExpanded = session.id in expandedNoteIds,
                            onTap = {
                                if (selectionMode) onToggleSelect(session.id)
                                else if (session.hasNotes) toggleNote(session.id)
                            },
                            onLongPress = { onToggleSelect(session.id) }
                        )
                    }
                }
            }
            ViewMode.GRID -> {
                val pairs = remember(sessions) { sessions.chunked(2) }
                LazyColumn(
                    contentPadding = listPadding,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pairs, key = { it.first().id }) { rowPair ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowPair.forEach { session ->
                                HistoryGridCard(
                                    session = session,
                                    isSelected = session.id in selectedIds,
                                    selectionMode = selectionMode,
                                    isExpanded = session.id in expandedNoteIds,
                                    onTap = {
                                        if (selectionMode) onToggleSelect(session.id)
                                        else if (session.hasNotes) toggleNote(session.id)
                                    },
                                    onLongPress = { onToggleSelect(session.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowPair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            ViewMode.TIMELINE -> {
                val grouped = remember(sessions) { sessions.groupBy { it.date }.entries.toList() }
                LazyColumn(
                    contentPadding = listPadding,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(grouped, key = { it.key }) { (date, list) ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(ru.blue)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = DateUtilsRu.formatDisplay(date),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "${list.size} रिकॉर्ड · ${DateUtilsRu.formatMinutes(list.sumOf { it.durationMinutes })}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ru.mutedText
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 4.dp, end = 12.dp)
                                        .width(2.dp)
                                        .height((list.size * 72).dp.coerceAtLeast(52.dp))
                                        .background(ru.blue.copy(alpha = 0.25f))
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    list.forEach { session ->
                                        HistoryListCard(
                                            session = session,
                                            isSelected = session.id in selectedIds,
                                            selectionMode = selectionMode,
                                            isExpanded = session.id in expandedNoteIds,
                                            onTap = {
                                                if (selectionMode) onToggleSelect(session.id)
                                                else if (session.hasNotes) toggleNote(session.id)
                                            },
                                            onLongPress = { onToggleSelect(session.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            ViewMode.COMPACT -> {
                LazyColumn(
                    contentPadding = listPadding,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(sessions, key = { it.id }) { session ->
                        HistoryCompactRow(
                            session = session,
                            isSelected = session.id in selectedIds,
                            selectionMode = selectionMode,
                            isExpanded = session.id in expandedNoteIds,
                            onTap = {
                                if (selectionMode) onToggleSelect(session.id)
                                else if (session.hasNotes) toggleNote(session.id)
                            },
                            onLongPress = { onToggleSelect(session.id) }
                        )
                    }
                }
            }
            ViewMode.TABLE -> {
                HistoryTableView(
                    sessions = sessions,
                    selectedIds = selectedIds,
                    selectionMode = selectionMode,
                    expandedIds = expandedNoteIds,
                    onTap = { s ->
                        if (selectionMode) onToggleSelect(s.id)
                        else if (s.hasNotes) toggleNote(s.id)
                    },
                    onLongPress = { onToggleSelect(it.id) },
                    contentPadding = listPadding
                )
            }
        }
    }
}

@Composable
private fun ActiveHistoryFilterChips(
    filter: HistoryFilterState,
    onUpdate: (HistoryFilterState) -> Unit,
    onClearAll: () -> Unit
) {
    val ru = LocalRuColors.current
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (filter.sort != HistorySortOption.NEWEST) {
            item("chip_sort") {
                RemovableChip(
                    label = "क्रम: ${filter.sort.labelHi}",
                    onRemove = { onUpdate(filter.copy(sort = HistorySortOption.NEWEST)) }
                )
            }
        }
        if (filter.isTimeFilterActive) {
            item("chip_time") {
                val timeDesc = when (filter.timeMode) {
                    TimeFilterMode.MONTH -> "Month (${filter.selectedMonths.size})"
                    TimeFilterMode.YEAR -> "Year (${filter.selectedYears.joinToString()})"
                    TimeFilterMode.RANGE -> "${filter.rangeFrom}..${filter.rangeTo}"
                    TimeFilterMode.EXACT -> filter.exactDate
                    TimeFilterMode.PAST_DAYS -> if (filter.pastDaysExactAgo) "${filter.pastDays}d ago" else "Last ${filter.pastDays}d"
                    TimeFilterMode.NONE -> ""
                }
                RemovableChip(
                    label = timeDesc,
                    onRemove = { onUpdate(filter.copy(timeMode = TimeFilterMode.NONE)) }
                )
            }
        }
        if (filter.selectedSubjects.isNotEmpty() || filter.selectedChapters.values.any { it.isNotEmpty() }) {
            item("chip_sub") {
                val subCount = (filter.selectedSubjects + filter.selectedChapters.keys).distinct().size
                RemovableChip(
                    label = "विषय ($subCount)",
                    onRemove = { onUpdate(filter.copy(selectedSubjects = emptySet(), selectedChapters = emptyMap())) }
                )
            }
        }
        if (filter.selectedModes.isNotEmpty()) {
            item("chip_mode") {
                RemovableChip(
                    label = filter.selectedModes.joinToString { it.labelHi },
                    onRemove = { onUpdate(filter.copy(selectedModes = emptySet())) }
                )
            }
        }
        if (filter.selectedStatuses.isNotEmpty()) {
            item("chip_status") {
                RemovableChip(
                    label = filter.selectedStatuses.joinToString { it.displayLabel },
                    onRemove = { onUpdate(filter.copy(selectedStatuses = emptySet())) }
                )
            }
        }
        item("chip_clear_all") {
            Surface(
                shape = RoundedCornerShape(99.dp),
                color = ru.red.copy(alpha = 0.14f),
                modifier = Modifier
                    .clip(RoundedCornerShape(99.dp))
                    .clickable(onClick = onClearAll)
                    .testTag("clear_all_filters_chip")
            ) {
                Text(
                    text = "Clear",
                    color = ru.red,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun RemovableChip(label: String, onRemove: () -> Unit) {
    val ru = LocalRuColors.current
    Surface(
        shape = RoundedCornerShape(99.dp),
        color = ru.blue.copy(alpha = 0.16f),
        border = BorderStroke(1.dp, ru.blue.copy(alpha = 0.4f)),
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .clickable(onClick = onRemove)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = ru.blue)
            Spacer(Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove filter",
                tint = ru.blue,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryListCard(
    session: StudySession,
    isSelected: Boolean,
    selectionMode: Boolean,
    isExpanded: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val ru = LocalRuColors.current
    val sCol = subjectColor(session.subject)
    RuCard(
        borderColor = if (isSelected) ru.blue else ru.cardBorder,
        borderWidth = if (isSelected) 1.5.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = onTap,
                onLongClick = onLongPress
            )
            .testTag("history_card_${session.id}")
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (selectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Select session",
                        tint = if (isSelected) ru.blue else ru.mutedText,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(42.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(sCol)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${session.chapter} · ${session.lecture}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(3.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = session.subject,
                            style = MaterialTheme.typography.labelMedium,
                            color = sCol,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text("•", color = ru.mutedText, style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = DateUtilsRu.formatDisplay(session.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = ru.mutedText
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        if (session.hasNotes) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Has notes",
                                tint = ru.blue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            text = "${session.durationMinutes}m",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.labelLarge,
                            color = ru.blue
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        ModePill(session.mode)
                        StatusPill(session.status)
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded && session.hasNotes,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = ru.cardBorder)
                    Spacer(Modifier.height(8.dp))
                    RichNoteCardsViewer(cards = session.noteCards)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryGridCard(
    session: StudySession,
    isSelected: Boolean,
    selectionMode: Boolean,
    isExpanded: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    val sCol = subjectColor(session.subject)
    RuCard(
        borderColor = if (isSelected) ru.blue else ru.cardBorder,
        borderWidth = if (isSelected) 1.5.dp else 1.dp,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(sCol)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(11.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = session.subject,
                        style = MaterialTheme.typography.labelSmall,
                        color = sCol,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (selectionMode) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = "Select",
                            tint = if (isSelected) ru.blue else ru.mutedText,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
                Text(
                    text = session.chapter,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${session.lecture} · ${DateUtilsRu.formatShort(session.date)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ru.mutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    StatusPill(session.status)
                    Text(
                        text = "${session.durationMinutes}m",
                        fontFamily = JetBrainsMono,
                        style = MaterialTheme.typography.labelLarge,
                        color = ru.blue
                    )
                }
                if (isExpanded && session.hasNotes) {
                    RichNoteCardsViewer(cards = session.noteCards)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryCompactRow(
    session: StudySession,
    isSelected: Boolean,
    selectionMode: Boolean,
    isExpanded: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit
) {
    val ru = LocalRuColors.current
    val sCol = subjectColor(session.subject)
    RuCard(
        shape = RoundedCornerShape(12.dp),
        borderColor = if (isSelected) ru.blue else ru.cardBorder,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(onClick = onTap, onLongClick = onLongPress)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (selectionMode) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                        contentDescription = "Select",
                        tint = if (isSelected) ru.blue else ru.mutedText,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(sCol)
                )
                Text(
                    text = "${session.chapter} · ${session.lecture}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = DateUtilsRu.formatShort(session.date),
                    style = MaterialTheme.typography.labelSmall,
                    color = ru.mutedText
                )
                Text(
                    text = "${session.durationMinutes}m",
                    fontFamily = JetBrainsMono,
                    style = MaterialTheme.typography.labelMedium,
                    color = ru.blue
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(ru.statusColor(session.status))
                )
            }
            if (isExpanded && session.hasNotes) {
                Spacer(Modifier.height(8.dp))
                RichNoteCardsViewer(cards = session.noteCards)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryTableView(
    sessions: List<StudySession>,
    selectedIds: Set<String>,
    selectionMode: Boolean,
    expandedIds: Set<String>,
    onTap: (StudySession) -> Unit,
    onLongPress: (StudySession) -> Unit,
    contentPadding: PaddingValues
) {
    val ru = LocalRuColors.current
    val hScroll = rememberScrollState()
    LazyColumn(
        contentPadding = contentPadding,
        modifier = Modifier.fillMaxSize()
    ) {
        item("table_header") {
            Row(
                modifier = Modifier
                    .horizontalScroll(hScroll)
                    .background(ru.subtleFill, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Text("Date", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(86.dp))
                Text("Subject", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(120.dp))
                Text("Chapter", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(150.dp))
                Text("Lecture", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(74.dp))
                Text("Time", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(56.dp))
                Text("Status", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(76.dp))
            }
            HorizontalDivider(color = ru.cardBorder)
        }
        items(sessions, key = { it.id }) { s ->
            val isSelected = s.id in selectedIds
            val isExpanded = s.id in expandedIds
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isSelected) ru.blue.copy(alpha = 0.14f) else Color.Transparent)
                    .combinedClickable(onClick = { onTap(s) }, onLongClick = { onLongPress(s) })
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .horizontalScroll(hScroll)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(DateUtilsRu.formatShort(s.date), style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(86.dp))
                    Text(s.subject, style = MaterialTheme.typography.labelMedium, color = subjectColor(s.subject), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(120.dp))
                    Text(s.chapter, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(150.dp))
                    Text(s.lecture, style = MaterialTheme.typography.labelSmall, color = ru.mutedText, maxLines = 1, modifier = Modifier.width(74.dp))
                    Text("${s.durationMinutes}m", fontFamily = JetBrainsMono, style = MaterialTheme.typography.labelMedium, color = ru.blue, modifier = Modifier.width(56.dp))
                    Box(modifier = Modifier.width(76.dp)) { StatusPill(s.status) }
                }
                if (isExpanded && s.hasNotes) {
                    Box(modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 10.dp)) {
                        RichNoteCardsViewer(cards = s.noteCards)
                    }
                }
                HorizontalDivider(color = ru.cardBorder.copy(alpha = 0.6f))
            }
        }
    }
}
