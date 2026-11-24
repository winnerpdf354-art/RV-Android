package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class StudyMode(val key: String, val labelEn: String, val labelHi: String) {
    REVISION("Revision", "Revision", "रिवीजन"),
    FIRST_STUDY("First Study", "First Study", "नया");

    companion object {
        fun fromKey(key: String?): StudyMode = when (key?.trim()?.lowercase(Locale.ROOT)) {
            "first study", "new", "नया" -> FIRST_STUDY
            else -> REVISION
        }
    }
}

enum class LearningStatus(
    val key: String,
    val displayLabel: String,
    val hindiShort: String,
    val priority: Int
) {
    EASY("Easy", "Clear", "पक्का", 1),
    MEDIUM("Medium", "Practice", "अभ्यास", 2),
    HARD("Hard", "Relearn", "दोहराना", 3);

    companion object {
        fun fromKey(key: String?): LearningStatus = when (key?.trim()?.lowercase(Locale.ROOT)) {
            "easy", "clear", "आसान" -> EASY
            "hard", "relearn", "कठिन", "दोहराना" -> HARD
            else -> MEDIUM
        }
    }
}

enum class ViewMode(val key: String, val label: String) {
    LIST("list", "List"),
    GRID("grid", "Grid"),
    TIMELINE("timeline", "Timeline"),
    COMPACT("compact", "Compact"),
    TABLE("table", "Table");

    fun next(): ViewMode {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }

    companion object {
        fun fromKey(key: String?): ViewMode =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: LIST
    }
}

enum class BgStyle(val id: Int, val labelHi: String, val descHi: String) {
    GRAIN_GLOW(1, "दानेदार + चमक", "हल्का टेक्सचर और सॉफ्ट एम्बिएंट ग्लो"),
    SUBTLE_GRAIN(2, "हल्के दाने", "शांत और प्रीमियम मैट सतह"),
    PURE_CLEAN(3, "एकदम साफ़", "न्यूनतम और विकर्षण-मुक्त बैकग्राउंड"),
    DOT_GRID(4, "बिंदुओं वाली जाली", "स्टडी नोटबुक जैसा डॉट मैट्रिक्स"),
    FLOATING_ORBS(5, "तैरते रंगीन गोले", "समय के अनुसार बदलते कोमल रंगीन गोले");

    companion object {
        fun fromId(id: Int): BgStyle = entries.firstOrNull { it.id == id } ?: GRAIN_GLOW
    }
}

enum class CardStyle(val id: Int, val labelHi: String, val descHi: String) {
    FROSTED(1, "छना हुआ (फ़्रॉस्ट)", "हल्की धुंधली सतह और बारीक बॉर्डर"),
    GLASS(2, "सामान्य काँच", "संतुलित पारदर्शिता और सॉफ्ट एलिवेशन"),
    CLEAR_GLASS(3, "पारदर्शी काँच", "न्यूनतम भराव और क्रिस्प किनारा"),
    SOLID(4, "ठोस (सादा)", "हाई-कॉन्ट्रास्ट ठोस मैट कार्ड");

    companion object {
        fun fromId(id: Int): CardStyle = entries.firstOrNull { it.id == id } ?: FROSTED
    }
}

enum class TimeFilterMode(val label: String) {
    NONE("All"),
    MONTH("Month"),
    YEAR("Year"),
    RANGE("Date Range"),
    EXACT("Date"),
    PAST_DAYS("Past Days")
}

enum class HistorySortOption(val key: String, val labelHi: String) {
    NEWEST("newest", "नया पहले"),
    OLDEST("oldest", "पुराना पहले"),
    MOST_TIME("most_time", "ज़्यादा समय"),
    LEAST_TIME("least_time", "कम समय"),
    SUBJECT_AZ("subject", "विषय A–Z"),
    HARD_FIRST("hard", "Relearn पहले")
}

enum class ChapterSortOption(val key: String, val labelHi: String) {
    NEWEST("newest", "नया पहले"),
    OLDEST("oldest", "पुराना पहले"),
    BY_LECTURE("lecture", "लेक्चर क्रम से"),
    MOST_TIME("most_time", "ज़्यादा समय"),
    LEAST_TIME("least_time", "कम समय"),
    HARD_FIRST("hard", "Relearn पहले")
}

enum class LinkCategory(val label: String) {
    ALL("All"),
    IN_NOTES("In notes"),
    DOCS("Docs"),
    VIDEO("Video"),
    AI_CHAT("AI chat"),
    OTHER("Other")
}

data class NoteLabel(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val colorHex: String,
    val sortOrder: Int = 0
)

data class NoteCard(
    val id: String = UUID.randomUUID().toString(),
    val labelId: String = "lbl_general",
    val labelName: String = "सामान्य",
    val colorHex: String = "#A1A1AA",
    val content: String = "",
    val pinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class SavedLink(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val url: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    val host: String
        get() = extractHost(url)

    val category: LinkCategory
        get() = categorizeUrl(url)
}

data class StudySession(
    val id: String = UUID.randomUUID().toString(),
    val date: String, // yyyy-MM-dd
    val timestamp: Long = System.currentTimeMillis(),
    val subject: String,
    val chapter: String,
    val lecture: String,
    val durationMinutes: Int,
    val mode: StudyMode = StudyMode.REVISION,
    val status: LearningStatus = LearningStatus.MEDIUM,
    val notesJson: String = "",
    val plainNotes: String = ""
) {
    val noteCards: List<NoteCard>
        get() = NoteSerializer.decodeCards(notesJson, plainNotes)

    val hasNotes: Boolean
        get() = noteCards.isNotEmpty() || plainNotes.isNotBlank()
}

data class AppPreferences(
    val isDarkTheme: Boolean = true,
    val bgStyle: BgStyle = BgStyle.GRAIN_GLOW,
    val cardStyle: CardStyle = CardStyle.FROSTED,
    val dailyGoalMinutes: Int = 180,
    val defaultMode: StudyMode = StudyMode.REVISION,
    val defaultStatus: LearningStatus = LearningStatus.MEDIUM,
    val defaultViewMode: ViewMode = ViewMode.LIST
)

data class DraftSessionState(
    val subject: String = "",
    val chapter: String = "",
    val lecture: String = "",
    val mode: StudyMode = StudyMode.REVISION,
    val status: LearningStatus = LearningStatus.MEDIUM,
    val manualMinutes: Int = 25,
    val isTimerMode: Boolean = false,
    val isRunning: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val startedAtEpochMs: Long = 0L,
    val isReviewingStopped: Boolean = false,
    val manualNotes: List<NoteCard> = emptyList(),
    val timerNotes: List<NoteCard> = emptyList()
) {
    val activeNotes: List<NoteCard>
        get() = if (isTimerMode || isRunning || elapsedSeconds > 0L) timerNotes else manualNotes

    val isTimerLocked: Boolean
        get() = isRunning || elapsedSeconds > 0L || isReviewingStopped
}

data class HistoryFilterState(
    val sort: HistorySortOption = HistorySortOption.NEWEST,
    val timeMode: TimeFilterMode = TimeFilterMode.NONE,
    val selectedYear: Int? = null,
    val selectedMonths: Set<Int> = emptySet(), // 0..11
    val selectedYears: Set<Int> = emptySet(),
    val rangeFrom: String = "",
    val rangeTo: String = "",
    val exactDate: String = "",
    val pastDays: Int = 7,
    val pastDaysExactAgo: Boolean = false, // false = Last Nd, true = Nd ago
    val selectedSubjects: Set<String> = emptySet(),
    val selectedChapters: Map<String, Set<String>> = emptyMap(),
    val selectedModes: Set<StudyMode> = emptySet(),
    val selectedStatuses: Set<LearningStatus> = emptySet()
) {
    val activeCount: Int
        get() {
            var count = 0
            if (sort != HistorySortOption.NEWEST) count++
            if (isTimeFilterActive) count++
            if (selectedSubjects.isNotEmpty() || selectedChapters.values.any { it.isNotEmpty() }) count++
            if (selectedModes.isNotEmpty()) count++
            if (selectedStatuses.isNotEmpty()) count++
            return count
        }

    val isTimeFilterActive: Boolean
        get() = when (timeMode) {
            TimeFilterMode.NONE -> false
            TimeFilterMode.MONTH -> selectedMonths.isNotEmpty() || selectedYear != null
            TimeFilterMode.YEAR -> selectedYears.isNotEmpty()
            TimeFilterMode.RANGE -> rangeFrom.isNotBlank() || rangeTo.isNotBlank()
            TimeFilterMode.EXACT -> exactDate.isNotBlank()
            TimeFilterMode.PAST_DAYS -> pastDays > 0
        }
}

data class ChapterFilterState(
    val sort: ChapterSortOption = ChapterSortOption.NEWEST,
    val timeMode: TimeFilterMode = TimeFilterMode.NONE,
    val selectedYear: Int? = null,
    val selectedMonths: Set<Int> = emptySet(),
    val selectedYears: Set<Int> = emptySet(),
    val rangeFrom: String = "",
    val rangeTo: String = "",
    val exactDate: String = "",
    val pastDays: Int = 7,
    val pastDaysExactAgo: Boolean = false,
    val selectedLectures: Set<String> = emptySet(),
    val selectedModes: Set<StudyMode> = emptySet(),
    val selectedStatuses: Set<LearningStatus> = emptySet()
) {
    val activeCount: Int
        get() {
            var count = 0
            if (sort != ChapterSortOption.NEWEST) count++
            if (isTimeFilterActive) count++
            if (selectedLectures.isNotEmpty()) count++
            if (selectedModes.isNotEmpty()) count++
            if (selectedStatuses.isNotEmpty()) count++
            return count
        }

    val isTimeFilterActive: Boolean
        get() = when (timeMode) {
            TimeFilterMode.NONE -> false
            TimeFilterMode.MONTH -> selectedMonths.isNotEmpty() || selectedYear != null
            TimeFilterMode.YEAR -> selectedYears.isNotEmpty()
            TimeFilterMode.RANGE -> rangeFrom.isNotBlank() || rangeTo.isNotBlank()
            TimeFilterMode.EXACT -> exactDate.isNotBlank()
            TimeFilterMode.PAST_DAYS -> pastDays > 0
        }
}

data class MissingSyllabusSubject(
    val subject: String,
    val isWholeSubjectMissing: Boolean,
    val chapters: List<MissingSyllabusChapter>
)

data class MissingSyllabusChapter(
    val chapter: String,
    val isWholeChapterMissing: Boolean,
    val missingLectures: List<String>
)

enum class ToastTone { INFO, SUCCESS, WARNING, ERROR }

data class ToastMessage(
    val id: Long = System.currentTimeMillis(),
    val message: String,
    val tone: ToastTone = ToastTone.INFO,
    val undoLabel: String? = null,
    val onUndo: (() -> Unit)? = null
)

sealed interface SyllabusDetailPage {
    data object Root : SyllabusDetailPage
    data class ChapterDetail(val subject: String, val chapter: String) : SyllabusDetailPage
    data class LectureDetail(val subject: String, val chapter: String, val lecture: String) : SyllabusDetailPage
}

sealed interface ToolsPage {
    data object None : ToolsPage
    data object SyllabusSubjects : ToolsPage
    data class SyllabusChapters(val subject: String) : ToolsPage
    data class SyllabusLectures(val subject: String, val chapter: String) : ToolsPage
    data object RestoreDeletedSyllabus : ToolsPage
    data object ImportData : ToolsPage
    data object ExportData : ToolsPage
    data object Appearance : ToolsPage
    data object AppApk : ToolsPage
}

object NoteSerializer {
    fun encodeCards(cards: List<NoteCard>): String {
        if (cards.isEmpty()) return ""
        val arr = JSONArray()
        cards.forEach { c ->
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("labelId", c.labelId)
            obj.put("labelName", c.labelName)
            obj.put("colorHex", c.colorHex)
            obj.put("content", c.content)
            obj.put("pinned", c.pinned)
            obj.put("createdAt", c.createdAt)
            obj.put("updatedAt", c.updatedAt)
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decodeCards(json: String, fallbackPlain: String = ""): List<NoteCard> {
        val trimmed = json.trim()
        if (trimmed.startsWith("[")) {
            try {
                val arr = JSONArray(trimmed)
                val out = mutableListOf<NoteCard>()
                for (i in 0 until arr.length()) {
                    val o = arr.optJSONObject(i) ?: continue
                    out.add(
                        NoteCard(
                            id = o.optString("id", UUID.randomUUID().toString()),
                            labelId = o.optString("labelId", "lbl_general"),
                            labelName = o.optString("labelName", o.optString("tag", "सामान्य")),
                            colorHex = o.optString("colorHex", o.optString("color", "#A1A1AA")),
                            content = o.optString("content", o.optString("text", "")),
                            pinned = o.optBoolean("pinned", false),
                            createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                            updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                        )
                    )
                }
                return out
            } catch (_: Exception) {
            }
        }
        if (fallbackPlain.isNotBlank()) {
            return listOf(
                NoteCard(
                    id = "legacy_${fallbackPlain.hashCode()}",
                    labelId = "lbl_general",
                    labelName = "सामान्य",
                    colorHex = "#A1A1AA",
                    content = fallbackPlain.trim()
                )
            )
        }
        return emptyList()
    }

    fun toPlainSummary(cards: List<NoteCard>): String {
        return cards.joinToString("\n\n") { c ->
            val header = if (c.labelName.isNotBlank()) "[${c.labelName}] " else ""
            header + c.content.trim()
        }.trim()
    }
}

object DateUtilsRu {
    private val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val shortFormat = SimpleDateFormat("dd MMM", Locale.US)

    fun todayIso(): String = isoFormat.format(Date())

    fun daysAgoIso(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return isoFormat.format(cal.time)
    }

    fun parseIsoToMillis(iso: String): Long {
        return try {
            isoFormat.parse(iso.trim())?.time ?: 0L
        } catch (_: Exception) {
            0L
        }
    }

    fun parseIsoCalendar(iso: String): Calendar? {
        val ms = parseIsoToMillis(iso)
        if (ms == 0L) return null
        return Calendar.getInstance().apply { timeInMillis = ms }
    }

    fun formatDisplay(iso: String): String {
        if (iso.isBlank()) return "—"
        val today = todayIso()
        if (iso == today) return "Today"
        if (iso == daysAgoIso(1)) return "Yesterday"
        val ms = parseIsoToMillis(iso)
        return if (ms > 0L) displayFormat.format(Date(ms)) else iso
    }

    fun formatShort(iso: String): String {
        if (iso.isBlank()) return "—"
        val ms = parseIsoToMillis(iso)
        return if (ms > 0L) shortFormat.format(Date(ms)) else iso
    }

    fun formatMinutes(minutes: Int): String {
        if (minutes <= 0) return "0m"
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h > 0 && m > 0 -> "${h}h ${m}m"
            h > 0 -> "${h}h"
            else -> "${m}m"
        }
    }

    fun formatDurationClock(totalSeconds: Long): String {
        val clamped = totalSeconds.coerceAtLeast(0L)
        val mins = clamped / 60
        val secs = clamped % 60
        return String.format(Locale.US, "%02d:%02d", mins, secs)
    }

    fun naturalLectureNumber(lecture: String): Int {
        val match = Regex("\\d+").find(lecture)
        return match?.value?.toIntOrNull() ?: Int.MAX_VALUE
    }

    fun weekDaysForOffset(weekOffset: Int): List<Calendar> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val dow = cal.get(Calendar.DAY_OF_WEEK) // Sun = 1 .. Sat = 7
        cal.add(Calendar.DAY_OF_YEAR, -(dow - Calendar.SUNDAY))
        cal.add(Calendar.WEEK_OF_YEAR, -weekOffset)
        return (0..6).map { idx ->
            (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, idx) }
        }
    }

    fun calendarToIso(cal: Calendar): String = isoFormat.format(cal.time)
}

fun extractHost(url: String): String {
    val cleaned = url.trim().removePrefix("https://").removePrefix("http://").removePrefix("www.")
    return cleaned.substringBefore('/').substringBefore('?').ifBlank { url.trim() }
}

fun categorizeUrl(url: String): LinkCategory {
    val u = url.lowercase(Locale.ROOT)
    return when {
        u.contains("youtube.com") || u.contains("youtu.be") || u.contains("vimeo.com") || u.contains("loom.com") -> LinkCategory.VIDEO
        u.contains("chatgpt.com") || u.contains("openai.com") || u.contains("gemini.google") || u.contains("claude.ai") || u.contains("perplexity.ai") -> LinkCategory.AI_CHAT
        u.contains("docs.google") || u.contains("drive.google") || u.contains("notion.so") || u.contains("pdf") || u.contains("wikipedia.org") || u.contains("github.com") -> LinkCategory.DOCS
        else -> LinkCategory.OTHER
    }
}

fun guessLabelFromUrl(url: String): String {
    val u = url.lowercase(Locale.ROOT)
    return when {
        u.contains("youtube.com") || u.contains("youtu.be") -> "YouTube Video"
        u.contains("drive.google") -> "Google Drive"
        u.contains("docs.google") -> "Google Docs"
        u.contains("chatgpt.com") || u.contains("openai.com") -> "ChatGPT"
        u.contains("gemini.google") -> "Gemini"
        u.contains("claude.ai") -> "Claude"
        u.contains("notion.so") -> "Notion"
        u.contains("wikipedia.org") -> "Wikipedia"
        u.contains("t.me") || u.contains("telegram") -> "Telegram"
        else -> extractHost(url).substringBefore('.').replaceFirstChar { it.uppercase() }.ifBlank { "Saved Link" }
    }
}

fun extractUrlsFromText(text: String): List<String> {
    val regex = Regex("https?://[^\\s\\])>\"']+")
    return regex.findAll(text).map { it.value.trimEnd('.', ',', ';') }.toList()
}
