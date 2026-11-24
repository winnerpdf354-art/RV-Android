package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import com.example.data.ChapterFilterState
import com.example.data.ChapterSortOption
import com.example.data.DateUtilsRu
import com.example.data.LearningStatus
import com.example.data.StudySession
import com.example.data.ViewMode
import com.example.ui.components.EmptyStateIllustration
import com.example.ui.components.ModePill
import com.example.ui.components.RichNoteCardsViewer
import com.example.ui.components.StatusPill
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.RuCard
import com.example.ui.theme.subjectColor

private data class SubjectSummary(
    val subject: String,
    val totalChapters: Int,
    val startedChapters: Int,
    val revisionCount: Int,
    val totalMinutes: Int,
    val lastDate: String
)

@Composable
fun SyllabusRootScreen(
    syllabus: Map<String, Map<String, List<String>>>,
    sessions: List<StudySession>,
    viewMode: ViewMode,
    searchQuery: String,
    onOpenChapterDetail: (subject: String, chapter: String) -> Unit,
    onOpenLectureDetail: (subject: String, chapter: String, lecture: String) -> Unit,
    contentBottomPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    var drawerSubject by remember { mutableStateOf<String?>(null) }
    var drawerChapter by remember { mutableStateOf<String?>(null) }

    BackHandler(enabled = drawerSubject != null) {
        if (drawerChapter != null) {
            drawerChapter = null
        } else {
            drawerSubject = null
        }
    }

    val summaries = remember(syllabus, sessions, searchQuery) {
        val bySub = sessions.groupBy { it.subject }
        val q = searchQuery.trim().lowercase()
        syllabus.entries.mapNotNull { (sub, chaps) ->
            val matches = q.isEmpty() ||
                sub.lowercase().contains(q) ||
                chaps.keys.any { it.lowercase().contains(q) }
            if (!matches) return@mapNotNull null
            val subSessions = bySub[sub].orEmpty()
            val startedChaps = subSessions.map { it.chapter }.distinct().count { it in chaps.keys }
            SubjectSummary(
                subject = sub,
                totalChapters = chaps.size,
                startedChapters = startedChaps,
                revisionCount = subSessions.size,
                totalMinutes = subSessions.sumOf { it.durationMinutes },
                lastDate = subSessions.maxOfOrNull { it.date } ?: ""
            )
        }
    }

    val totalChaptersAll = remember(syllabus) { syllabus.values.sumOf { it.size } }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Summary Strip
            Surface(
                color = ru.subtleFill,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "${syllabus.size} विषय · $totalChaptersAll अध्याय",
                        style = MaterialTheme.typography.labelLarge,
                        color = ru.blue
                    )
                    Text(
                        text = "टैप करें → चैप्टर व लेक्चर",
                        style = MaterialTheme.typography.labelSmall,
                        color = ru.mutedText
                    )
                }
            }

            if (summaries.isEmpty()) {
                EmptyStateIllustration(
                    isSearchEmpty = true,
                    title = "कोई विषय नहीं मिला",
                    subtitle = "अपनी खोज बदलें या साइडबार के Syllabus Manager से विषय जोड़ें।",
                    actionLabel = null,
                    onAction = null,
                    modifier = Modifier.weight(1f)
                )
            } else {
                val listPad = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 4.dp,
                    bottom = contentBottomPadding.calculateBottomPadding() + 88.dp
                )
                when (viewMode) {
                    ViewMode.LIST, ViewMode.TIMELINE -> {
                        LazyColumn(
                            contentPadding = listPad,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(summaries, key = { it.subject }) { item ->
                                SyllabusSubjectCard(
                                    item = item,
                                    onClick = {
                                        drawerSubject = item.subject
                                        drawerChapter = null
                                    }
                                )
                            }
                        }
                    }
                    ViewMode.GRID -> {
                        val pairs = remember(summaries) { summaries.chunked(2) }
                        LazyColumn(
                            contentPadding = listPad,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(pairs, key = { it.first().subject }) { pair ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    pair.forEach { item ->
                                        SyllabusSubjectGridCard(
                                            item = item,
                                            onClick = {
                                                drawerSubject = item.subject
                                                drawerChapter = null
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                    ViewMode.COMPACT -> {
                        LazyColumn(
                            contentPadding = listPad,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(summaries, key = { it.subject }) { item ->
                                val col = subjectColor(item.subject)
                                RuCard(
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            drawerSubject = item.subject
                                            drawerChapter = null
                                        }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(col)
                                        )
                                        Spacer(Modifier.width(9.dp))
                                        Text(
                                            text = item.subject,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text(
                                            text = "${item.startedChapters}/${item.totalChapters}",
                                            fontFamily = JetBrainsMono,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = col
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = DateUtilsRu.formatMinutes(item.totalMinutes),
                                            fontFamily = JetBrainsMono,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ru.mutedText
                                        )
                                    }
                                }
                            }
                        }
                    }
                    ViewMode.TABLE -> {
                        val hScroll = rememberScrollState()
                        LazyColumn(
                            contentPadding = listPad,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item("syl_tbl_hdr") {
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(hScroll)
                                        .background(ru.subtleFill, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text("Subject", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(150.dp))
                                    Text("Chapters", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(80.dp))
                                    Text("Revisions", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(80.dp))
                                    Text("Time", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(74.dp))
                                    Text("Last", style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(90.dp))
                                }
                                HorizontalDivider(color = ru.cardBorder)
                            }
                            items(summaries, key = { it.subject }) { item ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .horizontalScroll(hScroll)
                                        .clickable {
                                            drawerSubject = item.subject
                                            drawerChapter = null
                                        }
                                        .padding(horizontal = 12.dp, vertical = 11.dp)
                                ) {
                                    Text(item.subject, style = MaterialTheme.typography.labelLarge, color = subjectColor(item.subject), maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(150.dp))
                                    Text("${item.startedChapters}/${item.totalChapters}", fontFamily = JetBrainsMono, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(80.dp))
                                    Text("${item.revisionCount}", fontFamily = JetBrainsMono, style = MaterialTheme.typography.labelMedium, modifier = Modifier.width(80.dp))
                                    Text(DateUtilsRu.formatMinutes(item.totalMinutes), fontFamily = JetBrainsMono, style = MaterialTheme.typography.labelMedium, color = ru.blue, modifier = Modifier.width(74.dp))
                                    Text(DateUtilsRu.formatShort(item.lastDate), style = MaterialTheme.typography.labelSmall, color = ru.mutedText, modifier = Modifier.width(90.dp))
                                }
                                HorizontalDivider(color = ru.cardBorder.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }

        // Two-Level Floating Side Drawer Overlay (Chapters -> Lectures)
        AnimatedVisibility(
            visible = drawerSubject != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.52f))
                    .clickable {
                        drawerChapter = null
                        drawerSubject = null
                    }
            )
        }

        AnimatedVisibility(
            visible = drawerSubject != null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            drawerSubject?.let { sub ->
                val chaptersMap = syllabus[sub].orEmpty()
                val subSessions = remember(sessions, sub) { sessions.filter { it.subject == sub } }
                val sCol = subjectColor(sub)

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                    shadowElevation = 18.dp,
                    border = BorderStroke(1.dp, ru.cardBorder),
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
                        .fillMaxHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding()
                    ) {
                        // Drawer Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            if (drawerChapter != null) {
                                IconButton(onClick = { drawerChapter = null }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to chapters")
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 6.dp, end = 10.dp)
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(sCol)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = drawerChapter ?: sub,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (drawerChapter != null) sub else "${chaptersMap.size} अध्याय",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = sCol
                                )
                            }
                            IconButton(onClick = {
                                drawerChapter = null
                                drawerSubject = null
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close drawer")
                            }
                        }
                        HorizontalDivider(color = ru.cardBorder)

                        if (drawerChapter == null) {
                            // Level 1: Chapters List
                            val chapEntries = chaptersMap.entries.toList()
                            LazyColumn(
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(chapEntries, key = { it.key }) { (chap, lecs) ->
                                    val revCount = subSessions.count { it.chapter == chap }
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = ru.subtleFill,
                                        border = BorderStroke(1.dp, ru.cardBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { drawerChapter = chap }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = chap,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${lecs.size} लेक्चर",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ru.mutedText
                                                )
                                            }
                                            if (revCount > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(99.dp),
                                                    color = ru.emerald.copy(alpha = 0.18f)
                                                ) {
                                                    Text(
                                                        text = "$revCount",
                                                        fontFamily = JetBrainsMono,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = ru.emerald,
                                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(10.dp)
                                                        .border(1.5.dp, ru.mutedText.copy(alpha = 0.45f), CircleShape)
                                                )
                                            }
                                            Spacer(Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = ru.mutedText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // Level 2: All Lectures + Individual Lectures
                            val activeChap = drawerChapter!!
                            val lectures = chaptersMap[activeChap].orEmpty()
                            val chapSessions = subSessions.filter { it.chapter == activeChap }

                            LazyColumn(
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item("all_lectures_pinned") {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = sCol.copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, sCol.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable {
                                                drawerChapter = null
                                                drawerSubject = null
                                                onOpenChapterDetail(sub, activeChap)
                                            }
                                            .testTag("drawer_all_lectures_btn")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LibraryBooks,
                                                contentDescription = null,
                                                tint = sCol,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(10.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "All Lectures (सभी लेक्चर)",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = sCol,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${chapSessions.size} कुल रिवीजन · ${DateUtilsRu.formatMinutes(chapSessions.sumOf { it.durationMinutes })}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = ru.mutedText
                                                )
                                            }
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = null,
                                                tint = sCol
                                            )
                                        }
                                    }
                                }

                                items(lectures, key = { it }) { lec ->
                                    val lecCount = chapSessions.count { it.lecture == lec }
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = ru.subtleFill,
                                        border = BorderStroke(1.dp, ru.cardBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable {
                                                drawerChapter = null
                                                drawerSubject = null
                                                onOpenLectureDetail(sub, activeChap, lec)
                                            }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                        ) {
                                            Text(lec, style = MaterialTheme.typography.titleSmall)
                                            if (lecCount > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(99.dp),
                                                    color = ru.blue.copy(alpha = 0.16f)
                                                ) {
                                                    Text(
                                                        text = "$lecCount रिवीजन",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = ru.blue,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(9.dp)
                                                        .border(1.5.dp, ru.mutedText.copy(alpha = 0.45f), CircleShape)
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
}

@Composable
private fun SyllabusSubjectCard(item: SubjectSummary, onClick: () -> Unit) {
    val ru = LocalRuColors.current
    val sCol = subjectColor(item.subject)
    val pct = if (item.totalChapters > 0) (item.startedChapters.toFloat() / item.totalChapters).coerceIn(0f, 1f) else 0f

    RuCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("syllabus_subject_${item.subject}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(38.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(sCol)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.subject,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.revisionCount} रिवीजन · ${DateUtilsRu.formatMinutes(item.totalMinutes)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = ru.mutedText
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = sCol.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "${item.startedChapters}/${item.totalChapters} चैप्टर",
                            fontFamily = JetBrainsMono,
                            style = MaterialTheme.typography.labelSmall,
                            color = sCol,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = if (item.lastDate.isNotBlank()) DateUtilsRu.formatShort(item.lastDate) else "अभी शुरू नहीं",
                        style = MaterialTheme.typography.labelSmall,
                        color = ru.mutedText
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(ru.subtleFill)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(pct)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(99.dp))
                        .background(sCol)
                )
            }
        }
    }
}

@Composable
private fun SyllabusSubjectGridCard(
    item: SubjectSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    val sCol = subjectColor(item.subject)
    val pct = if (item.totalChapters > 0) (item.startedChapters.toFloat() / item.totalChapters).coerceIn(0f, 1f) else 0f

    RuCard(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
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
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = item.subject,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.startedChapters}/${item.totalChapters} चैप्टर",
                    fontFamily = JetBrainsMono,
                    style = MaterialTheme.typography.labelMedium,
                    color = sCol
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(ru.subtleFill)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(pct)
                            .fillMaxHeight()
                            .background(sCol)
                    )
                }
                Text(
                    text = "${item.revisionCount} रिवीजन · ${DateUtilsRu.formatMinutes(item.totalMinutes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ru.mutedText
                )
            }
        }
    }
}

@Composable
fun ChapterDetailScreen(
    subject: String,
    chapter: String,
    lecturesInSyllabus: List<String>,
    allChapterSessions: List<StudySession>,
    filteredSessions: List<StudySession>,
    filter: ChapterFilterState,
    onBackToRoot: () -> Unit,
    onOpenLectureDetail: (String) -> Unit,
    contentBottomPadding: PaddingValues
) {
    val ru = LocalRuColors.current
    val sCol = subjectColor(subject)
    val doneLectures = remember(allChapterSessions, lecturesInSyllabus) {
        allChapterSessions.map { it.lecture }.distinct().size
    }
    val totalTime = remember(allChapterSessions) { allChapterSessions.sumOf { it.durationMinutes } }
    val lastDate = remember(allChapterSessions) { allChapterSessions.maxOfOrNull { it.date } ?: "" }

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = contentBottomPadding.calculateBottomPadding() + 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        // Breadcrumb Ribbon
        item("breadcrumb") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(sCol.copy(alpha = 0.14f))
                    .clickable { onBackToRoot() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(subject, style = MaterialTheme.typography.labelLarge, color = sCol)
                Text("  ›  ", color = ru.mutedText)
                Text(chapter, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        // 4-Stat Grid
        item("stats") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniStatCard("रिवीजन", allChapterSessions.size.toString(), ru.blue, Modifier.weight(1f))
                    MiniStatCard("कुल समय", DateUtilsRu.formatMinutes(totalTime), ru.emerald, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniStatCard("लेक्चर", "$doneLectures/${lecturesInSyllabus.size.coerceAtLeast(doneLectures)}", ru.purple, Modifier.weight(1f))
                    MiniStatCard("आख़िरी", if (lastDate.isNotBlank()) DateUtilsRu.formatShort(lastDate) else "—", ru.amber, Modifier.weight(1f))
                }
            }
        }

        if (filteredSessions.isEmpty()) {
            item("empty") {
                EmptyStateIllustration(
                    isSearchEmpty = allChapterSessions.isNotEmpty(),
                    title = if (allChapterSessions.isEmpty()) "अभी कोई रिवीजन नहीं" else "फ़िल्टर से कोई रिकॉर्ड नहीं",
                    subtitle = "इस अध्याय का स्टडी सेशन सेव करने पर यहाँ पूरा रिकॉर्ड दिखेगा।",
                    actionLabel = null,
                    onAction = null
                )
            }
        } else if (filter.sort == ChapterSortOption.BY_LECTURE) {
            val grouped = filteredSessions.groupBy { it.lecture }
                .entries
                .sortedBy { DateUtilsRu.naturalLectureNumber(it.key) }
            grouped.forEach { (lec, recs) ->
                item("hdr_$lec") {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onOpenLectureDetail(lec) }
                            .padding(vertical = 4.dp)
                    ) {
                        Text(lec, style = MaterialTheme.typography.titleSmall, color = sCol, fontWeight = FontWeight.Bold)
                        Text("${recs.size} रिवीजन ›", style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                    }
                }
                items(recs, key = { it.id }) { s ->
                    ChapterSessionRecordCard(session = s, onLectureClick = { onOpenLectureDetail(s.lecture) })
                }
            }
        } else {
            items(filteredSessions, key = { it.id }) { s ->
                ChapterSessionRecordCard(session = s, onLectureClick = { onOpenLectureDetail(s.lecture) })
            }
        }
    }
}

@Composable
fun LectureDetailScreen(
    subject: String,
    chapter: String,
    lecture: String,
    lectureSessions: List<StudySession>,
    onBackToChapter: () -> Unit,
    contentBottomPadding: PaddingValues
) {
    val ru = LocalRuColors.current
    val sCol = subjectColor(subject)
    val sortedDesc = remember(lectureSessions) {
        lectureSessions.sortedWith(compareByDescending<StudySession> { it.date }.thenByDescending { it.timestamp })
    }
    val totalMinutes = remember(sortedDesc) { sortedDesc.sumOf { it.durationMinutes } }
    val avgMinutes = if (sortedDesc.isNotEmpty()) totalMinutes / sortedDesc.size else 0
    val lastDate = sortedDesc.firstOrNull()?.date.orEmpty()
    val firstDate = sortedDesc.lastOrNull()?.date.orEmpty()

    val easy = sortedDesc.count { it.status == LearningStatus.EASY }
    val med = sortedDesc.count { it.status == LearningStatus.MEDIUM }
    val hard = sortedDesc.count { it.status == LearningStatus.HARD }
    val totalCount = sortedDesc.size.coerceAtLeast(1)

    LazyColumn(
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = contentBottomPadding.calculateBottomPadding() + 40.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item("lec_breadcrumb") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(sCol.copy(alpha = 0.14f))
                    .clickable { onBackToChapter() }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text("$subject › $chapter › ", style = MaterialTheme.typography.labelMedium, color = sCol, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                Text(lecture, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            }
        }

        item("lec_stats") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniStatCard("रिवीजन", sortedDesc.size.toString(), ru.blue, Modifier.weight(1f))
                    MiniStatCard("कुल समय", DateUtilsRu.formatMinutes(totalMinutes), ru.emerald, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    MiniStatCard("औसत", DateUtilsRu.formatMinutes(avgMinutes), ru.purple, Modifier.weight(1f))
                    MiniStatCard("आख़िरी", if (lastDate.isNotBlank()) DateUtilsRu.formatShort(lastDate) else "—", ru.amber, Modifier.weight(1f))
                }
            }
        }

        if (sortedDesc.isNotEmpty()) {
            item("lec_diff_bar") {
                RuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Status Breakdown", style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = "पहला: ${DateUtilsRu.formatShort(firstDate)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ru.mutedText
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(99.dp))
                                .background(ru.subtleFill)
                        ) {
                            if (easy > 0) Box(Modifier.weight(easy.toFloat() / totalCount).fillMaxHeight().background(ru.emerald))
                            if (med > 0) Box(Modifier.weight(med.toFloat() / totalCount).fillMaxHeight().background(ru.amber))
                            if (hard > 0) Box(Modifier.weight(hard.toFloat() / totalCount).fillMaxHeight().background(ru.red))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Clear: $easy", style = MaterialTheme.typography.labelSmall, color = ru.emerald)
                            Text("Practice: $med", style = MaterialTheme.typography.labelSmall, color = ru.amber)
                            Text("Relearn: $hard", style = MaterialTheme.typography.labelSmall, color = ru.red)
                        }
                    }
                }
            }

            items(sortedDesc.indices.toList(), key = { sortedDesc[it].id }) { idx ->
                val s = sortedDesc[idx]
                val revNum = sortedDesc.size - idx
                RuCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = sCol.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "रिवीजन #$revNum",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = sCol,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Text(DateUtilsRu.formatDisplay(s.date), style = MaterialTheme.typography.labelMedium, color = ru.mutedText)
                            }
                            Text(
                                text = "${s.durationMinutes}m",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelLarge,
                                color = ru.blue
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            ModePill(s.mode)
                            StatusPill(s.status)
                        }
                        if (s.hasNotes) {
                            RichNoteCardsViewer(cards = s.noteCards)
                        }
                    }
                }
            }
        } else {
            item("lec_empty") {
                EmptyStateIllustration(
                    isSearchEmpty = false,
                    title = "यह लेक्चर अभी शुरू नहीं हुआ",
                    subtitle = "इस लेक्चर का पहला सेशन जोड़ने पर पूरा इतिहास और नोट्स यहाँ दिखाई देंगे।",
                    actionLabel = null,
                    onAction = null
                )
            }
        }
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    val ru = LocalRuColors.current
    RuCard(shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMono,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}

@Composable
private fun ChapterSessionRecordCard(
    session: StudySession,
    onLectureClick: () -> Unit
) {
    val ru = LocalRuColors.current
    RuCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(13.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = session.lecture,
                        style = MaterialTheme.typography.titleSmall,
                        color = ru.blue,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onLectureClick() }
                    )
                    Text(DateUtilsRu.formatDisplay(session.date), style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                }
                Text(
                    text = "${session.durationMinutes}m",
                    fontFamily = JetBrainsMono,
                    style = MaterialTheme.typography.labelLarge,
                    color = ru.blue
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ModePill(session.mode)
                StatusPill(session.status)
            }
            if (session.hasNotes) {
                RichNoteCardsViewer(cards = session.noteCards)
            }
        }
    }
}
