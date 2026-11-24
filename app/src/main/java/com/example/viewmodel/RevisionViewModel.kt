package com.example.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppPreferences
import com.example.data.BgStyle
import com.example.data.CardStyle
import com.example.data.ChapterFilterState
import com.example.data.ChapterSortOption
import com.example.data.DateUtilsRu
import com.example.data.DraftSessionState
import com.example.data.HistoryFilterState
import com.example.data.HistorySortOption
import com.example.data.ImportExportManager
import com.example.data.LearningStatus
import com.example.data.MissingSyllabusSubject
import com.example.data.NoteCard
import com.example.data.NoteLabel
import com.example.data.NoteSerializer
import com.example.data.PreferencesRepository
import com.example.data.ProjectExportManager
import com.example.data.RevisionRepository
import com.example.data.SavedLink
import com.example.data.StudyMode
import com.example.data.StudySession
import com.example.data.SyllabusDetailPage
import com.example.data.TimeFilterMode
import com.example.data.ToastMessage
import com.example.data.ToastTone
import com.example.data.ToolsPage
import com.example.data.ViewMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class RevisionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val prefsRepo = PreferencesRepository(application)
    private val repo = RevisionRepository(db, prefsRepo)

    val preferences: StateFlow<AppPreferences> = prefsRepo.preferencesFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        AppPreferences()
    )

    val sessions: StateFlow<List<StudySession>> = repo.sessionsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val syllabus: StateFlow<Map<String, Map<String, List<String>>>> = repo.syllabusMapFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyMap()
    )

    val noteLabels: StateFlow<List<NoteLabel>> = repo.noteLabelsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val savedLinks: StateFlow<List<SavedLink>> = repo.savedLinksFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val missingDefaultSyllabus: StateFlow<List<MissingSyllabusSubject>> = syllabus.map {
        RevisionRepository.computeMissingDefaultSyllabus(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation & UI Shell States
    private val _activeTab = MutableStateFlow(0) // 0: Analytics, 1: History, 2: Syllabus
    val activeTab: StateFlow<Int> = _activeTab.asStateFlow()

    private val _syllabusDetailPage = MutableStateFlow<SyllabusDetailPage>(SyllabusDetailPage.Root)
    val syllabusDetailPage: StateFlow<SyllabusDetailPage> = _syllabusDetailPage.asStateFlow()

    private val _showStudySession = MutableStateFlow(false)
    val showStudySession: StateFlow<Boolean> = _showStudySession.asStateFlow()

    private val _showNotesEditor = MutableStateFlow(false)
    val showNotesEditor: StateFlow<Boolean> = _showNotesEditor.asStateFlow()

    private val _toolsPage = MutableStateFlow<ToolsPage>(ToolsPage.None)
    val toolsPage: StateFlow<ToolsPage> = _toolsPage.asStateFlow()

    // View Modes (synced with defaultViewMode on startup)
    private val _historyViewMode = MutableStateFlow(ViewMode.LIST)
    val historyViewMode: StateFlow<ViewMode> = _historyViewMode.asStateFlow()

    private val _syllabusViewMode = MutableStateFlow(ViewMode.LIST)
    val syllabusViewMode: StateFlow<ViewMode> = _syllabusViewMode.asStateFlow()

    // Search States
    private val _historySearchOpen = MutableStateFlow(false)
    val historySearchOpen: StateFlow<Boolean> = _historySearchOpen.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    private val _syllabusSearchOpen = MutableStateFlow(false)
    val syllabusSearchOpen: StateFlow<Boolean> = _syllabusSearchOpen.asStateFlow()

    private val _syllabusSearchQuery = MutableStateFlow("")
    val syllabusSearchQuery: StateFlow<String> = _syllabusSearchQuery.asStateFlow()

    // Filter States
    private val _historyFilter = MutableStateFlow(HistoryFilterState())
    val historyFilter: StateFlow<HistoryFilterState> = _historyFilter.asStateFlow()

    private val _chapterFilter = MutableStateFlow(ChapterFilterState())
    val chapterFilter: StateFlow<ChapterFilterState> = _chapterFilter.asStateFlow()

    // Multi-Select History
    private val _selectedHistoryIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedHistoryIds: StateFlow<Set<String>> = _selectedHistoryIds.asStateFlow()

    // Analytics Habit State
    private val _habitWeekOffset = MutableStateFlow(0)
    val habitWeekOffset: StateFlow<Int> = _habitWeekOffset.asStateFlow()

    private val _selectedHabitDate = MutableStateFlow<String?>(null)
    val selectedHabitDate: StateFlow<String?> = _selectedHabitDate.asStateFlow()

    // Study Session Draft & Live Timer State
    private val _draftSession = MutableStateFlow(DraftSessionState())
    val draftSession: StateFlow<DraftSessionState> = _draftSession.asStateFlow()

    private val _studyError = MutableStateFlow<String?>(null)
    val studyError: StateFlow<String?> = _studyError.asStateFlow()

    // Toast / Island Notification
    private val _toast = MutableStateFlow<ToastMessage?>(null)
    val toast: StateFlow<ToastMessage?> = _toast.asStateFlow()
    private var toastDismissJob: Job? = null
    private var tickerJob: Job? = null

    val filteredHistory: StateFlow<List<StudySession>> = combine(
        sessions,
        historyFilter,
        historySearchQuery
    ) { all, filter, query ->
        applyHistoryFilter(all, filter, query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repo.ensureSeeded()
            val prefs = prefsRepo.preferencesFlow.first()
            _historyViewMode.value = prefs.defaultViewMode
            _syllabusViewMode.value = prefs.defaultViewMode
            val restored = prefsRepo.loadDraftSession(prefs)
            _draftSession.value = restored
            if (restored.isRunning) {
                startTicker()
            }
        }
    }

    // --- Toast Helper ---
    fun showToast(
        message: String,
        tone: ToastTone = ToastTone.INFO,
        undoLabel: String? = null,
        onUndo: (() -> Unit)? = null
    ) {
        toastDismissJob?.cancel()
        val msg = ToastMessage(
            id = System.currentTimeMillis(),
            message = message,
            tone = tone,
            undoLabel = undoLabel,
            onUndo = onUndo
        )
        _toast.value = msg
        toastDismissJob = viewModelScope.launch {
            delay(if (onUndo != null) 4800L else 2800L)
            if (_toast.value?.id == msg.id) {
                _toast.value = null
            }
        }
    }

    fun dismissToast() {
        toastDismissJob?.cancel()
        _toast.value = null
    }

    // --- Navigation ---
    fun selectTab(index: Int) {
        _activeTab.value = index.coerceIn(0, 2)
        if (index != 1) clearHistorySelection()
    }

    fun openChapterDetail(subject: String, chapter: String) {
        _chapterFilter.value = ChapterFilterState()
        _syllabusDetailPage.value = SyllabusDetailPage.ChapterDetail(subject, chapter)
    }

    fun openLectureDetail(subject: String, chapter: String, lecture: String) {
        _syllabusDetailPage.value = SyllabusDetailPage.LectureDetail(subject, chapter, lecture)
    }

    fun navigateBackFromSyllabusDetail() {
        when (val page = _syllabusDetailPage.value) {
            is SyllabusDetailPage.LectureDetail -> {
                _syllabusDetailPage.value = SyllabusDetailPage.ChapterDetail(page.subject, page.chapter)
            }
            is SyllabusDetailPage.ChapterDetail -> {
                _syllabusDetailPage.value = SyllabusDetailPage.Root
            }
            SyllabusDetailPage.Root -> Unit
        }
    }

    fun openStudySession() {
        _studyError.value = null
        _showStudySession.value = true
    }

    fun closeStudySession() {
        _showStudySession.value = false
        _studyError.value = null
    }

    fun openNotesEditor() {
        _showNotesEditor.value = true
    }

    fun closeNotesEditor() {
        _showNotesEditor.value = false
    }

    fun openToolsPage(page: ToolsPage) {
        _toolsPage.value = page
    }

    fun navigateBackTools() {
        _toolsPage.value = when (val cur = _toolsPage.value) {
            is ToolsPage.SyllabusLectures -> ToolsPage.SyllabusChapters(cur.subject)
            is ToolsPage.SyllabusChapters -> ToolsPage.SyllabusSubjects
            ToolsPage.RestoreDeletedSyllabus -> ToolsPage.SyllabusSubjects
            else -> ToolsPage.None
        }
    }

    // --- View Modes & Search ---
    fun cycleHistoryViewMode() {
        _historyViewMode.update { it.next() }
    }

    fun setHistoryViewMode(mode: ViewMode, saveAsDefault: Boolean = false) {
        _historyViewMode.value = mode
        if (saveAsDefault) setDefaultViewMode(mode)
    }

    fun cycleSyllabusViewMode() {
        _syllabusViewMode.update { it.next() }
    }

    fun setSyllabusViewMode(mode: ViewMode, saveAsDefault: Boolean = false) {
        _syllabusViewMode.value = mode
        if (saveAsDefault) setDefaultViewMode(mode)
    }

    fun setHistorySearchOpen(open: Boolean) {
        _historySearchOpen.value = open
        if (!open) _historySearchQuery.value = ""
    }

    fun setHistorySearchQuery(q: String) {
        _historySearchQuery.value = q
    }

    fun setSyllabusSearchOpen(open: Boolean) {
        _syllabusSearchOpen.value = open
        if (!open) _syllabusSearchQuery.value = ""
    }

    fun setSyllabusSearchQuery(q: String) {
        _syllabusSearchQuery.value = q
    }

    // --- Preferences ---
    fun toggleTheme() {
        val next = !preferences.value.isDarkTheme
        viewModelScope.launch { prefsRepo.setDarkTheme(next) }
    }

    fun setDarkTheme(isDark: Boolean) {
        viewModelScope.launch { prefsRepo.setDarkTheme(isDark) }
    }

    fun setBgStyle(style: BgStyle) {
        viewModelScope.launch { prefsRepo.setBgStyle(style) }
    }

    fun setCardStyle(style: CardStyle) {
        viewModelScope.launch { prefsRepo.setCardStyle(style) }
    }

    fun setDailyGoalMinutes(minutes: Int) {
        viewModelScope.launch {
            prefsRepo.setDailyGoalMinutes(minutes)
            if (minutes <= 0) {
                showToast("दैनिक लक्ष्य हटाया गया", ToastTone.INFO)
            } else {
                showToast("दैनिक लक्ष्य सहेजा गया: ${DateUtilsRu.formatMinutes(minutes)}", ToastTone.SUCCESS)
            }
        }
    }

    fun setDefaultStudyMode(mode: StudyMode) {
        viewModelScope.launch {
            prefsRepo.setDefaultMode(mode)
            if (!_draftSession.value.isTimerLocked) {
                updateDraft { it.copy(mode = mode) }
            }
        }
    }

    fun setDefaultLearningStatus(status: LearningStatus) {
        viewModelScope.launch {
            prefsRepo.setDefaultStatus(status)
            if (!_draftSession.value.isTimerLocked) {
                updateDraft { it.copy(status = status) }
            }
        }
    }

    fun setDefaultViewMode(mode: ViewMode) {
        _historyViewMode.value = mode
        _syllabusViewMode.value = mode
        viewModelScope.launch {
            prefsRepo.setDefaultViewMode(mode)
            showToast("Default view: ${mode.label}", ToastTone.SUCCESS)
        }
    }

    // --- Analytics Habit ---
    fun changeHabitWeek(delta: Int) {
        _habitWeekOffset.update { (it + delta).coerceAtLeast(0) }
        _selectedHabitDate.value = null
    }

    fun jumpHabitToCurrentWeek() {
        _habitWeekOffset.value = 0
        _selectedHabitDate.value = null
    }

    fun toggleHabitDate(isoDate: String) {
        _selectedHabitDate.update { if (it == isoDate) null else isoDate }
    }

    // --- Study Session & Live Timer ---
    private fun updateDraft(persist: Boolean = true, transform: (DraftSessionState) -> DraftSessionState) {
        _draftSession.update(transform)
        if (persist) {
            val snap = _draftSession.value
            viewModelScope.launch { prefsRepo.saveDraftSession(snap) }
        }
    }

    fun selectStudySubject(subject: String) {
        if (_draftSession.value.isTimerLocked) return
        _studyError.value = null
        updateDraft {
            if (it.subject == subject) it
            else it.copy(subject = subject, chapter = "", lecture = "")
        }
    }

    fun selectStudyChapter(chapter: String) {
        if (_draftSession.value.isTimerLocked) return
        _studyError.value = null
        val lectures = syllabus.value[_draftSession.value.subject]?.get(chapter).orEmpty()
        updateDraft {
            it.copy(
                chapter = chapter,
                lecture = if (lectures.size == 1) lectures.first() else ""
            )
        }
    }

    fun selectStudyLecture(lecture: String) {
        if (_draftSession.value.isTimerLocked) return
        _studyError.value = null
        updateDraft { it.copy(lecture = lecture) }
    }

    fun setStudyMode(mode: StudyMode) {
        if (_draftSession.value.isTimerLocked) return
        updateDraft { it.copy(mode = mode) }
    }

    fun setStudyStatus(status: LearningStatus) {
        updateDraft { it.copy(status = status) }
    }

    fun setManualMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 1440)
        _studyError.value = null
        updateDraft { it.copy(manualMinutes = clamped) }
    }

    fun clearStudyError() {
        _studyError.value = null
    }

    private fun validateSelection(): Boolean {
        val d = _draftSession.value
        return when {
            d.subject.isBlank() -> {
                _studyError.value = "कृपया पहले विषय (Subject) चुनें।"
                false
            }
            d.chapter.isBlank() -> {
                _studyError.value = "कृपया अध्याय (Chapter) चुनें।"
                false
            }
            d.lecture.isBlank() -> {
                _studyError.value = "कृपया लेक्चर (Lecture) चुनें।"
                false
            }
            else -> true
        }
    }

    fun startTimerFromManual() {
        if (!validateSelection()) return
        _studyError.value = null
        updateDraft {
            it.copy(
                isTimerMode = true,
                isRunning = true,
                isReviewingStopped = false,
                startedAtEpochMs = System.currentTimeMillis()
            )
        }
        startTicker()
    }

    fun toggleTimerPauseResume() {
        val d = _draftSession.value
        if (d.isRunning) {
            tickerJob?.cancel()
            updateDraft { it.copy(isRunning = false, startedAtEpochMs = 0L) }
        } else {
            updateDraft {
                it.copy(
                    isTimerMode = true,
                    isRunning = true,
                    isReviewingStopped = false,
                    startedAtEpochMs = System.currentTimeMillis()
                )
            }
            startTicker()
        }
    }

    fun stopTimerForReview() {
        val d = _draftSession.value
        if (d.elapsedSeconds < 10L) {
            _studyError.value = "सेशन सहेजने के लिए कम से कम 10 सेकंड पढ़ाई ज़रूरी है।"
            return
        }
        tickerJob?.cancel()
        playSoftChime()
        _studyError.value = null
        updateDraft {
            it.copy(
                isRunning = false,
                isReviewingStopped = true,
                startedAtEpochMs = 0L
            )
        }
    }

    fun resumeFromReview() {
        _studyError.value = null
        updateDraft {
            it.copy(
                isTimerMode = true,
                isRunning = true,
                isReviewingStopped = false,
                startedAtEpochMs = System.currentTimeMillis()
            )
        }
        startTicker()
    }

    fun resetTimerToManual() {
        tickerJob?.cancel()
        _studyError.value = null
        updateDraft {
            it.copy(
                isTimerMode = false,
                isRunning = false,
                elapsedSeconds = 0L,
                startedAtEpochMs = 0L,
                isReviewingStopped = false,
                timerNotes = emptyList()
            )
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            var tickCount = 0
            while (true) {
                delay(1000L)
                val cur = _draftSession.value
                if (!cur.isRunning) break
                val next = cur.copy(
                    elapsedSeconds = cur.elapsedSeconds + 1L,
                    startedAtEpochMs = System.currentTimeMillis()
                )
                _draftSession.value = next
                tickCount++
                if (tickCount % 10 == 0) {
                    prefsRepo.saveDraftSession(next)
                }
            }
        }
    }

    private fun playSoftChime() {
        try {
            val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 55)
            tg.startTone(ToneGenerator.TONE_PROP_ACK, 240)
            viewModelScope.launch {
                delay(400L)
                try { tg.release() } catch (_: Exception) {}
            }
        } catch (_: Exception) {
        }
    }

    fun saveCurrentSession() {
        if (!validateSelection()) return
        val d = _draftSession.value
        val mins = if (d.isTimerMode || d.isReviewingStopped) {
            ((d.elapsedSeconds + 30L) / 60L).toInt().coerceAtLeast(1)
        } else {
            d.manualMinutes.coerceIn(1, 1440)
        }
        val notes = d.activeNotes.filter { it.content.isNotBlank() }
        val notesJson = NoteSerializer.encodeCards(notes)
        val plain = NoteSerializer.toPlainSummary(notes)

        val session = StudySession(
            id = UUID.randomUUID().toString(),
            date = DateUtilsRu.todayIso(),
            timestamp = System.currentTimeMillis(),
            subject = d.subject,
            chapter = d.chapter,
            lecture = d.lecture,
            durationMinutes = mins,
            mode = d.mode,
            status = d.status,
            notesJson = notesJson,
            plainNotes = plain
        )

        tickerJob?.cancel()
        val defaults = preferences.value
        val freshDraft = DraftSessionState(
            subject = "",
            chapter = "",
            lecture = "",
            mode = defaults.defaultMode,
            status = defaults.defaultStatus,
            manualMinutes = 25,
            isTimerMode = false,
            isRunning = false,
            elapsedSeconds = 0L,
            startedAtEpochMs = 0L,
            isReviewingStopped = false,
            manualNotes = emptyList(),
            timerNotes = emptyList()
        )

        viewModelScope.launch {
            repo.addSession(session)
            _draftSession.value = freshDraft
            prefsRepo.saveDraftSession(freshDraft)
            _showStudySession.value = false
            _activeTab.value = 1
            showToast("Saved to History", ToastTone.SUCCESS)
        }
    }

    // --- Draft Notes Management ---
    fun updateActiveDraftNotes(transform: (List<NoteCard>) -> List<NoteCard>) {
        updateDraft { d ->
            if (d.isTimerMode || d.isRunning || d.elapsedSeconds > 0L) {
                d.copy(timerNotes = transform(d.timerNotes))
            } else {
                d.copy(manualNotes = transform(d.manualNotes))
            }
        }
    }

    // --- Note Labels & Saved Links ---
    fun addNoteLabel(name: String, colorHex: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        viewModelScope.launch {
            val nextOrder = (noteLabels.value.maxOfOrNull { it.sortOrder } ?: -1) + 1
            repo.upsertNoteLabel(
                NoteLabel(
                    id = "lbl_${UUID.randomUUID().toString().take(8)}",
                    name = clean,
                    colorHex = colorHex,
                    sortOrder = nextOrder
                )
            )
            showToast("लेबल \"$clean\" जोड़ा गया", ToastTone.SUCCESS)
        }
    }

    fun updateNoteLabel(label: NoteLabel) {
        viewModelScope.launch {
            repo.upsertNoteLabel(label)
            // Update matching draft cards metadata
            updateActiveDraftNotes { cards ->
                cards.map { c ->
                    if (c.labelId == label.id) c.copy(labelName = label.name, colorHex = label.colorHex) else c
                }
            }
        }
    }

    fun moveNoteLabel(labelId: String, direction: Int) {
        val list = noteLabels.value.toMutableList()
        val idx = list.indexOfFirst { it.id == labelId }
        if (idx < 0) return
        val target = idx + direction
        if (target !in list.indices) return
        val item = list.removeAt(idx)
        list.add(target, item)
        viewModelScope.launch { repo.saveNoteLabelsOrder(list) }
    }

    fun deleteNoteLabel(labelId: String) {
        val remaining = noteLabels.value.filter { it.id != labelId }
        val fallback = remaining.lastOrNull() ?: NoteLabel("lbl_general", "सामान्य", "#A1A1AA", 0)
        viewModelScope.launch {
            repo.deleteNoteLabel(labelId)
            updateActiveDraftNotes { cards ->
                cards.map { c ->
                    if (c.labelId == labelId) {
                        c.copy(labelId = fallback.id, labelName = fallback.name, colorHex = fallback.colorHex)
                    } else c
                }
            }
            showToast("लेबल हटाया गया", ToastTone.INFO)
        }
    }

    fun upsertSavedLink(link: SavedLink) {
        viewModelScope.launch {
            repo.upsertSavedLink(link)
            showToast("लिंक सहेजा गया", ToastTone.SUCCESS)
        }
    }

    fun deleteSavedLink(id: String) {
        viewModelScope.launch {
            repo.deleteSavedLink(id)
            showToast("लिंक हटाया गया", ToastTone.INFO)
        }
    }

    // --- History Selection & Deletion ---
    fun toggleHistorySelection(id: String) {
        _selectedHistoryIds.update { set ->
            if (id in set) set - id else set + id
        }
    }

    fun selectAllVisibleHistory() {
        _selectedHistoryIds.value = filteredHistory.value.map { it.id }.toSet()
    }

    fun clearHistorySelection() {
        _selectedHistoryIds.value = emptySet()
    }

    fun deleteSelectedHistorySessions() {
        val ids = _selectedHistoryIds.value.toList()
        if (ids.isEmpty()) return
        _selectedHistoryIds.value = emptySet()
        viewModelScope.launch {
            val removed = repo.deleteSessions(ids)
            showToast(
                message = "${removed.size} रिकॉर्ड हटाए गए",
                tone = ToastTone.WARNING,
                undoLabel = "पूर्ववत",
                onUndo = {
                    viewModelScope.launch {
                        repo.addSessions(removed)
                        showToast("रिकॉर्ड वापस जोड़े गए", ToastTone.SUCCESS)
                    }
                }
            )
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            val backup = repo.clearAllSessions()
            if (backup.isEmpty()) {
                showToast("इतिहास पहले से खाली है", ToastTone.INFO)
                return@launch
            }
            showToast(
                message = "सारा इतिहास (${backup.size} सेशन) साफ़ किया गया",
                tone = ToastTone.WARNING,
                undoLabel = "पूर्ववत",
                onUndo = {
                    viewModelScope.launch {
                        repo.addSessions(backup)
                        showToast("सारा इतिहास वापस आ गया", ToastTone.SUCCESS)
                    }
                }
            )
        }
    }

    fun resetAllAppData() {
        tickerJob?.cancel()
        viewModelScope.launch {
            repo.resetAllData()
            _draftSession.value = DraftSessionState()
            _historyFilter.value = HistoryFilterState()
            _chapterFilter.value = ChapterFilterState()
            _selectedHistoryIds.value = emptySet()
            _syllabusDetailPage.value = SyllabusDetailPage.Root
            _toolsPage.value = ToolsPage.None
            _showStudySession.value = false
            _showNotesEditor.value = false
            _historyViewMode.value = ViewMode.LIST
            _syllabusViewMode.value = ViewMode.LIST
            showToast("ऐप का पूरा डेटा रीसेट किया गया", ToastTone.SUCCESS)
        }
    }

    // --- Filters ---
    fun updateHistoryFilter(filter: HistoryFilterState) {
        _historyFilter.value = filter
    }

    fun resetHistoryFilter() {
        _historyFilter.value = HistoryFilterState()
    }

    fun updateChapterFilter(filter: ChapterFilterState) {
        _chapterFilter.value = filter
    }

    fun resetChapterFilter() {
        _chapterFilter.value = ChapterFilterState()
    }

    // --- Syllabus Manager CRUD ---
    fun addSubject(name: String, initialChapter: String) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        if (syllabus.value.keys.any { it.equals(clean, ignoreCase = true) }) {
            showToast("यह विषय पहले से मौजूद है", ToastTone.WARNING)
            return
        }
        viewModelScope.launch {
            repo.addSubject(clean, initialChapter.trim().ifEmpty { "Chapter 1" })
            showToast("विषय \"$clean\" जोड़ा गया", ToastTone.SUCCESS)
        }
    }

    fun addChapter(subject: String, chapterName: String, lectureCount: Int) {
        val clean = chapterName.trim()
        if (clean.isEmpty()) return
        if (syllabus.value[subject]?.keys?.any { it.equals(clean, ignoreCase = true) } == true) {
            showToast("यह चैप्टर पहले से मौजूद है", ToastTone.WARNING)
            return
        }
        viewModelScope.launch {
            repo.addChapter(subject, clean, lectureCount)
            showToast("चैप्टर \"$clean\" जोड़ा गया", ToastTone.SUCCESS)
        }
    }

    fun addLecturesQuick(subject: String, chapter: String, count: Int) {
        viewModelScope.launch {
            val added = repo.addLecturesToChapter(subject, chapter, count)
            showToast("${added.size} लेक्चर जोड़े गए", ToastTone.SUCCESS)
        }
    }

    fun addCustomLecture(subject: String, chapter: String, name: String) {
        viewModelScope.launch {
            val ok = repo.addCustomLecture(subject, chapter, name)
            if (ok) showToast("\"$name\" जोड़ा गया", ToastTone.SUCCESS)
            else showToast("यह लेक्चर पहले से मौजूद है या नाम खाली है", ToastTone.WARNING)
        }
    }

    fun deleteSubjects(subjects: List<String>) {
        val d = _draftSession.value
        if (d.isTimerLocked && d.subject in subjects) {
            showToast("लाइव टाइमर इस विषय पर चल रहा है — पहले टाइमर बंद करें", ToastTone.ERROR)
            return
        }
        viewModelScope.launch {
            val snapshot = repo.getSyllabusSnapshot()
            repo.deleteSubjects(subjects)
            if (!d.isTimerLocked && d.subject in subjects) {
                updateDraft { it.copy(subject = "", chapter = "", lecture = "") }
            }
            showToast(
                message = "${subjects.size} विषय हटाए गए",
                tone = ToastTone.WARNING,
                undoLabel = "पूर्ववत",
                onUndo = {
                    viewModelScope.launch {
                        repo.restoreSyllabusSnapshot(snapshot)
                        showToast("सिलेबस वापस आ गया", ToastTone.SUCCESS)
                    }
                }
            )
        }
    }

    fun deleteChapters(subject: String, chapters: List<String>) {
        val d = _draftSession.value
        if (d.isTimerLocked && d.subject == subject && d.chapter in chapters) {
            showToast("लाइव टाइमर इस चैप्टर पर चल रहा है — पहले टाइमर बंद करें", ToastTone.ERROR)
            return
        }
        viewModelScope.launch {
            val snapshot = repo.getSyllabusSnapshot()
            repo.deleteChapters(subject, chapters)
            if (!d.isTimerLocked && d.subject == subject && d.chapter in chapters) {
                updateDraft { it.copy(chapter = "", lecture = "") }
            }
            showToast(
                message = "${chapters.size} चैप्टर हटाए गए",
                tone = ToastTone.WARNING,
                undoLabel = "पूर्ववत",
                onUndo = {
                    viewModelScope.launch {
                        repo.restoreSyllabusSnapshot(snapshot)
                        showToast("चैप्टर वापस आ गए", ToastTone.SUCCESS)
                    }
                }
            )
        }
    }

    fun deleteLectures(subject: String, chapter: String, lectures: List<String>) {
        val d = _draftSession.value
        if (d.isTimerLocked && d.subject == subject && d.chapter == chapter && d.lecture in lectures) {
            showToast("लाइव टाइमर इस लेक्चर पर चल रहा है — पहले टाइमर बंद करें", ToastTone.ERROR)
            return
        }
        viewModelScope.launch {
            val snapshot = repo.getSyllabusSnapshot()
            repo.deleteLectures(subject, chapter, lectures)
            if (!d.isTimerLocked && d.subject == subject && d.chapter == chapter && d.lecture in lectures) {
                updateDraft { it.copy(lecture = "") }
            }
            showToast(
                message = "${lectures.size} लेक्चर हटाए गए",
                tone = ToastTone.WARNING,
                undoLabel = "पूर्ववत",
                onUndo = {
                    viewModelScope.launch {
                        repo.restoreSyllabusSnapshot(snapshot)
                        showToast("लेक्चर वापस आ गए", ToastTone.SUCCESS)
                    }
                }
            )
        }
    }

    fun restoreMissingSyllabus(entries: List<Triple<String, String, String>>) {
        if (entries.isEmpty()) return
        viewModelScope.launch {
            val snapshot = repo.getSyllabusSnapshot()
            val added = repo.restoreMissingEntries(entries)
            showToast(
                message = "$added लेक्चर बहाल किए गए",
                tone = ToastTone.SUCCESS,
                undoLabel = "पूर्ववत",
                onUndo = {
                    viewModelScope.launch {
                        repo.restoreSyllabusSnapshot(snapshot)
                        showToast("बहाली पूर्ववत की गई", ToastTone.INFO)
                    }
                }
            )
        }
    }

    // --- Offline Import & Export ---
    fun importFiles(
        uris: List<Uri>,
        scope: ImportExportManager.DataScope,
        onReport: (ImportExportManager.ImportResult) -> Unit
    ) {
        if (uris.isEmpty()) return
        val app = getApplication<Application>()
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    val allRows = mutableListOf<List<String>>()
                    uris.forEach { uri ->
                        val name = uri.lastPathSegment ?: "import.csv"
                        app.contentResolver.openInputStream(uri)?.use { stream ->
                            allRows.addAll(ImportExportManager.parseTableFromStream(stream, name))
                        }
                    }
                    if (allRows.isEmpty()) {
                        return@withContext ImportExportManager.ImportResult(0, 0, "फ़ाइल में कोई वैध डेटा नहीं मिला।")
                    }
                    if (scope == ImportExportManager.DataScope.HISTORY) {
                        val parsed = ImportExportManager.parseHistoryRows(allRows)
                        val existing = repo.getAllSessionsOnce()
                        val existingKeys = existing.map { ImportExportManager.sessionDedupKey(it) }.toMutableSet()
                        val toAdd = mutableListOf<StudySession>()
                        var skipped = 0
                        parsed.forEach { s ->
                            val k = ImportExportManager.sessionDedupKey(s)
                            if (k in existingKeys) {
                                skipped++
                            } else {
                                existingKeys.add(k)
                                toAdd.add(s)
                            }
                        }
                        if (toAdd.isNotEmpty()) {
                            repo.addSessions(toAdd)
                        }
                        ImportExportManager.ImportResult(toAdd.size, skipped) to toAdd.map { it.id }
                    } else {
                        val triples = ImportExportManager.parseSyllabusRows(allRows)
                        val snap = repo.getSyllabusSnapshot()
                        val (added, skipped) = repo.importSyllabusTriples(triples)
                        ImportExportManager.ImportResult(added, skipped) to snap
                    }
                } catch (e: Exception) {
                    ImportExportManager.ImportResult(0, 0, e.message ?: "आयात विफल रहा।")
                }
            }

            when (result) {
                is ImportExportManager.ImportResult -> {
                    onReport(result)
                    showToast(result.errorMessage ?: "आयात पूर्ण", ToastTone.ERROR)
                }
                is Pair<*, *> -> {
                    val report = result.first as ImportExportManager.ImportResult
                    onReport(report)
                    if (scope == ImportExportManager.DataScope.HISTORY) {
                        @Suppress("UNCHECKED_CAST")
                        val addedIds = result.second as List<String>
                        showToast(
                            message = "${report.addedCount} सेशन जोड़े गए (${report.skippedDuplicates} डुप्लिकेट छोड़े)",
                            tone = ToastTone.SUCCESS,
                            undoLabel = if (addedIds.isNotEmpty()) "पूर्ववत" else null,
                            onUndo = if (addedIds.isNotEmpty()) {
                                {
                                    viewModelScope.launch {
                                        repo.deleteSessions(addedIds)
                                        showToast("आयात पूर्ववत किया गया", ToastTone.INFO)
                                    }
                                }
                            } else null
                        )
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val snap = result.second as List<com.example.data.SyllabusEntryEntity>
                        showToast(
                            message = "${report.addedCount} सिलेबस आइटम जोड़े गए (${report.skippedDuplicates} पहले से मौजूद)",
                            tone = ToastTone.SUCCESS,
                            undoLabel = if (report.addedCount > 0) "पूर्ववत" else null,
                            onUndo = if (report.addedCount > 0) {
                                {
                                    viewModelScope.launch {
                                        repo.restoreSyllabusSnapshot(snap)
                                        showToast("सिलेबस आयात पूर्ववत", ToastTone.INFO)
                                    }
                                }
                            } else null
                        )
                    }
                }
            }
        }
    }

    fun exportToUri(uri: Uri, scope: ImportExportManager.DataScope, isXlsx: Boolean) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                try {
                    val bytes = buildExportBytes(scope, isXlsx)
                    app.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    true
                } catch (_: Exception) {
                    false
                }
            }
            if (ok) showToast("फ़ाइल सफलतापूर्वक सेव हुई", ToastTone.SUCCESS)
            else showToast("फ़ाइल सेव करने में त्रुटि", ToastTone.ERROR)
        }
    }

    fun shareExport(scope: ImportExportManager.DataScope, isXlsx: Boolean) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val bytes = withContext(Dispatchers.IO) { buildExportBytes(scope, isXlsx) }
            val ext = if (isXlsx) "xlsx" else "csv"
            val mime = if (isXlsx) {
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            } else {
                "text/csv"
            }
            val fileName = "revision_universe_${scope.name.lowercase(Locale.US)}_${DateUtilsRu.todayIso()}.$ext"
            val ok = ImportExportManager.shareFile(app, fileName, mime, bytes)
            if (!ok) showToast("शेयर करने में त्रुटि", ToastTone.ERROR)
        }
    }

    fun copyTsvToClipboard(scope: ImportExportManager.DataScope) {
        val app = getApplication<Application>()
        val tsv = if (scope == ImportExportManager.DataScope.HISTORY) {
            ImportExportManager.buildHistoryTsv(sessions.value)
        } else {
            ImportExportManager.buildSyllabusTsv(syllabus.value)
        }
        ImportExportManager.copyToClipboard(app, "Revision Universe ${scope.label}", tsv)
        showToast("${scope.label} TSV क्लिपबोर्ड पर कॉपी हुआ", ToastTone.SUCCESS)
    }

    private fun buildExportBytes(scope: ImportExportManager.DataScope, isXlsx: Boolean): ByteArray {
        return when {
            scope == ImportExportManager.DataScope.HISTORY && isXlsx ->
                ImportExportManager.buildHistoryXlsx(sessions.value)
            scope == ImportExportManager.DataScope.HISTORY ->
                ImportExportManager.buildHistoryCsv(sessions.value)
            isXlsx ->
                ImportExportManager.buildSyllabusXlsx(syllabus.value)
            else ->
                ImportExportManager.buildSyllabusCsv(syllabus.value)
        }
    }

    fun getOwnApkInfo(): ImportExportManager.AppApkInfo {
        return ImportExportManager.getOwnApkInfo(getApplication())
    }

    fun exportOwnApkToUri(uri: Uri, onDone: (Boolean) -> Unit = {}) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                ImportExportManager.copyOwnApkToUri(app, uri)
            }
            onDone(ok)
            if (ok) {
                showToast("इंस्टॉल करने योग्य APK फ़ाइल सेव हो गई", ToastTone.SUCCESS)
            } else {
                showToast("APK सेव करने में त्रुटि हुई", ToastTone.ERROR)
            }
        }
    }

    fun shareOwnApk(onDone: (Boolean) -> Unit = {}) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val ok = withContext(Dispatchers.IO) {
                ImportExportManager.shareOwnApk(app)
            }
            onDone(ok)
            if (!ok) {
                showToast("APK शेयर करने में त्रुटि हुई", ToastTone.ERROR)
            }
        }
    }

    // --- Full Project ZIP Export (source/ + index.html + data.json) ---
    fun getProjectExportSummary(): ProjectExportManager.ExportBundleSummary {
        val totalSyllabusRows = syllabus.value.values.sumOf { ch -> ch.values.sumOf { it.size } }
        return ProjectExportManager.getSummary(
            context = getApplication(),
            sessionsCount = sessions.value.size,
            syllabusEntriesCount = totalSyllabusRows,
            labelsCount = noteLabels.value.size,
            linksCount = savedLinks.value.size
        )
    }

    private suspend fun buildLiveDatabaseJson(): String {
        val allSessions = repo.getAllSessionsOnce()
        val allSyllabus = repo.getSyllabusSnapshot()
        return ProjectExportManager.buildDatabaseJson(
            sessions = allSessions,
            syllabusEntries = allSyllabus,
            labels = noteLabels.value,
            links = savedLinks.value,
            preferences = preferences.value
        )
    }

    fun exportProjectZipAndShare(onDone: (ProjectExportManager.ZipBuildOutcome) -> Unit = {}) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val outcome = withContext(Dispatchers.IO) {
                val json = buildLiveDatabaseJson()
                ProjectExportManager.buildZipInCache(app, json)
            }
            if (outcome.success && outcome.zipFile != null) {
                val shared = ProjectExportManager.shareZipFile(app, outcome.zipFile)
                onDone(outcome)
                if (shared) {
                    showToast("app_export.zip तैयार है (${outcome.formattedSize})", ToastTone.SUCCESS)
                } else {
                    showToast("ZIP बना पर Share Sheet खोलने में त्रुटि हुई", ToastTone.ERROR)
                }
            } else {
                onDone(outcome)
                showToast(outcome.errorMessage ?: "app_export.zip बनाने में त्रुटि हुई", ToastTone.ERROR)
            }
        }
    }

    fun saveProjectZipToDownloads(
        onNeedsSafFallback: () -> Unit,
        onDone: (ProjectExportManager.ZipBuildOutcome) -> Unit = {}
    ) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val outcome = withContext(Dispatchers.IO) {
                val json = buildLiveDatabaseJson()
                ProjectExportManager.saveZipDirectlyToDownloadsApi29Plus(app, json)
            }
            if (!outcome.success && outcome.errorMessage == "REQUIRES_SAF") {
                onNeedsSafFallback()
                return@launch
            }
            onDone(outcome)
            if (outcome.success) {
                showToast("Downloads/app_export.zip में सेव हो गया", ToastTone.SUCCESS)
            } else {
                showToast(outcome.errorMessage ?: "Downloads में सेव करने में त्रुटि हुई", ToastTone.ERROR)
            }
        }
    }

    fun writeProjectZipToUri(uri: Uri, onDone: (ProjectExportManager.ZipBuildOutcome) -> Unit = {}) {
        val app = getApplication<Application>()
        viewModelScope.launch {
            val outcome = withContext(Dispatchers.IO) {
                val json = buildLiveDatabaseJson()
                ProjectExportManager.writeZipToUri(app, uri, json)
            }
            onDone(outcome)
            if (outcome.success) {
                showToast("app_export.zip सेव हो गया (${outcome.formattedSize})", ToastTone.SUCCESS)
            } else {
                showToast(outcome.errorMessage ?: "app_export.zip सेव करने में त्रुटि हुई", ToastTone.ERROR)
            }
        }
    }

    // --- Filtering & Sorting Engines ---
    fun applyHistoryFilter(
        all: List<StudySession>,
        f: HistoryFilterState,
        query: String
    ): List<StudySession> {
        val q = query.trim().lowercase(Locale.ROOT)
        val filtered = all.filter { s ->
            val matchQuery = if (q.isEmpty()) true else {
                s.subject.lowercase(Locale.ROOT).contains(q) ||
                    s.chapter.lowercase(Locale.ROOT).contains(q) ||
                    s.lecture.lowercase(Locale.ROOT).contains(q) ||
                    s.plainNotes.lowercase(Locale.ROOT).contains(q) ||
                    s.date.contains(q) ||
                    s.status.displayLabel.lowercase(Locale.ROOT).contains(q) ||
                    s.status.hindiShort.contains(q)
            }
            val matchTime = matchesTimeFilter(
                s.date,
                f.timeMode,
                f.selectedYear,
                f.selectedMonths,
                f.selectedYears,
                f.rangeFrom,
                f.rangeTo,
                f.exactDate,
                f.pastDays,
                f.pastDaysExactAgo
            )
            val matchSubjectChapter = if (f.selectedSubjects.isEmpty() && f.selectedChapters.values.all { it.isEmpty() }) {
                true
            } else {
                val specificChaps = f.selectedChapters[s.subject].orEmpty()
                when {
                    specificChaps.isNotEmpty() -> s.chapter in specificChaps
                    s.subject in f.selectedSubjects -> true
                    else -> false
                }
            }
            val matchMode = f.selectedModes.isEmpty() || s.mode in f.selectedModes
            val matchStatus = f.selectedStatuses.isEmpty() || s.status in f.selectedStatuses
            matchQuery && matchTime && matchSubjectChapter && matchMode && matchStatus
        }

        return when (f.sort) {
            HistorySortOption.NEWEST -> filtered.sortedWith(compareByDescending<StudySession> { it.date }.thenByDescending { it.timestamp })
            HistorySortOption.OLDEST -> filtered.sortedWith(compareBy<StudySession> { it.date }.thenBy { it.timestamp })
            HistorySortOption.MOST_TIME -> filtered.sortedByDescending { it.durationMinutes }
            HistorySortOption.LEAST_TIME -> filtered.sortedBy { it.durationMinutes }
            HistorySortOption.SUBJECT_AZ -> filtered.sortedWith(compareBy<StudySession> { it.subject.lowercase(Locale.ROOT) }.thenByDescending { it.date })
            HistorySortOption.HARD_FIRST -> filtered.sortedWith(compareByDescending<StudySession> { it.status.priority }.thenByDescending { it.date })
        }
    }

    fun applyChapterFilter(
        chapterSessions: List<StudySession>,
        f: ChapterFilterState
    ): List<StudySession> {
        val filtered = chapterSessions.filter { s ->
            val matchTime = matchesTimeFilter(
                s.date,
                f.timeMode,
                f.selectedYear,
                f.selectedMonths,
                f.selectedYears,
                f.rangeFrom,
                f.rangeTo,
                f.exactDate,
                f.pastDays,
                f.pastDaysExactAgo
            )
            val matchLec = f.selectedLectures.isEmpty() || s.lecture in f.selectedLectures
            val matchMode = f.selectedModes.isEmpty() || s.mode in f.selectedModes
            val matchStatus = f.selectedStatuses.isEmpty() || s.status in f.selectedStatuses
            matchTime && matchLec && matchMode && matchStatus
        }

        return when (f.sort) {
            ChapterSortOption.NEWEST -> filtered.sortedWith(compareByDescending<StudySession> { it.date }.thenByDescending { it.timestamp })
            ChapterSortOption.OLDEST -> filtered.sortedWith(compareBy<StudySession> { it.date }.thenBy { it.timestamp })
            ChapterSortOption.BY_LECTURE -> filtered.sortedWith(
                compareBy<StudySession> { DateUtilsRu.naturalLectureNumber(it.lecture) }
                    .thenBy { it.lecture }
                    .thenByDescending { it.date }
            )
            ChapterSortOption.MOST_TIME -> filtered.sortedByDescending { it.durationMinutes }
            ChapterSortOption.LEAST_TIME -> filtered.sortedBy { it.durationMinutes }
            ChapterSortOption.HARD_FIRST -> filtered.sortedWith(compareByDescending<StudySession> { it.status.priority }.thenByDescending { it.date })
        }
    }

    private fun matchesTimeFilter(
        isoDate: String,
        mode: TimeFilterMode,
        selectedYear: Int?,
        selectedMonths: Set<Int>,
        selectedYears: Set<Int>,
        rangeFrom: String,
        rangeTo: String,
        exactDate: String,
        pastDays: Int,
        pastDaysExactAgo: Boolean
    ): Boolean {
        if (mode == TimeFilterMode.NONE) return true
        val cal = DateUtilsRu.parseIsoCalendar(isoDate) ?: return false
        return when (mode) {
            TimeFilterMode.NONE -> true
            TimeFilterMode.MONTH -> {
                val yrOk = selectedYear == null || cal.get(Calendar.YEAR) == selectedYear
                val mOk = selectedMonths.isEmpty() || cal.get(Calendar.MONTH) in selectedMonths
                yrOk && mOk
            }
            TimeFilterMode.YEAR -> {
                selectedYears.isEmpty() || cal.get(Calendar.YEAR) in selectedYears
            }
            TimeFilterMode.RANGE -> {
                val fromOk = rangeFrom.isBlank() || isoDate >= rangeFrom
                val toOk = rangeTo.isBlank() || isoDate <= rangeTo
                fromOk && toOk
            }
            TimeFilterMode.EXACT -> {
                exactDate.isBlank() || isoDate == exactDate
            }
            TimeFilterMode.PAST_DAYS -> {
                val days = pastDays.coerceIn(1, 365)
                if (pastDaysExactAgo) {
                    isoDate == DateUtilsRu.daysAgoIso(days)
                } else {
                    val startIso = DateUtilsRu.daysAgoIso(days - 1)
                    val endIso = DateUtilsRu.todayIso()
                    isoDate in startIso..endIso
                }
            }
        }
    }
}
