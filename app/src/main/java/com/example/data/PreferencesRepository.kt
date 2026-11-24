package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.ruDataStore: DataStore<Preferences> by preferencesDataStore(name = "revision_universe_prefs")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val IS_DARK = booleanPreferencesKey("is_dark_theme")
        val BG_STYLE = intPreferencesKey("bg_style")
        val CARD_STYLE = intPreferencesKey("card_style")
        val DAILY_GOAL_MIN = intPreferencesKey("daily_goal_minutes")
        val DEFAULT_MODE = stringPreferencesKey("default_study_mode")
        val DEFAULT_STATUS = stringPreferencesKey("default_learning_status")
        val DEFAULT_VIEW_MODE = stringPreferencesKey("default_view_mode")
        val SYLLABUS_SEEDED = booleanPreferencesKey("syllabus_seeded_v1")

        // Persistent Study Session Draft / Live Timer
        val DRAFT_SUBJECT = stringPreferencesKey("draft_subject")
        val DRAFT_CHAPTER = stringPreferencesKey("draft_chapter")
        val DRAFT_LECTURE = stringPreferencesKey("draft_lecture")
        val DRAFT_MODE = stringPreferencesKey("draft_mode")
        val DRAFT_STATUS = stringPreferencesKey("draft_status")
        val DRAFT_MANUAL_MIN = intPreferencesKey("draft_manual_min")
        val DRAFT_IS_TIMER = booleanPreferencesKey("draft_is_timer")
        val DRAFT_IS_RUNNING = booleanPreferencesKey("draft_is_running")
        val DRAFT_ELAPSED_SEC = longPreferencesKey("draft_elapsed_sec")
        val DRAFT_STARTED_AT = longPreferencesKey("draft_started_at")
        val DRAFT_REVIEWING = booleanPreferencesKey("draft_reviewing")
        val DRAFT_MANUAL_NOTES = stringPreferencesKey("draft_manual_notes")
        val DRAFT_TIMER_NOTES = stringPreferencesKey("draft_timer_notes")
    }

    val preferencesFlow: Flow<AppPreferences> = context.ruDataStore.data.map { p ->
        AppPreferences(
            isDarkTheme = p[Keys.IS_DARK] ?: true,
            bgStyle = BgStyle.fromId(p[Keys.BG_STYLE] ?: 1),
            cardStyle = CardStyle.fromId(p[Keys.CARD_STYLE] ?: 1),
            dailyGoalMinutes = p[Keys.DAILY_GOAL_MIN] ?: 180,
            defaultMode = StudyMode.fromKey(p[Keys.DEFAULT_MODE]),
            defaultStatus = LearningStatus.fromKey(p[Keys.DEFAULT_STATUS]),
            defaultViewMode = ViewMode.fromKey(p[Keys.DEFAULT_VIEW_MODE])
        )
    }

    suspend fun isSyllabusSeeded(): Boolean {
        return context.ruDataStore.data.first()[Keys.SYLLABUS_SEEDED] ?: false
    }

    suspend fun markSyllabusSeeded(seeded: Boolean = true) {
        context.ruDataStore.edit { it[Keys.SYLLABUS_SEEDED] = seeded }
    }

    suspend fun setDarkTheme(isDark: Boolean) {
        context.ruDataStore.edit { it[Keys.IS_DARK] = isDark }
    }

    suspend fun setBgStyle(style: BgStyle) {
        context.ruDataStore.edit { it[Keys.BG_STYLE] = style.id }
    }

    suspend fun setCardStyle(style: CardStyle) {
        context.ruDataStore.edit { it[Keys.CARD_STYLE] = style.id }
    }

    suspend fun setDailyGoalMinutes(minutes: Int) {
        context.ruDataStore.edit { it[Keys.DAILY_GOAL_MIN] = minutes.coerceAtLeast(0) }
    }

    suspend fun setDefaultMode(mode: StudyMode) {
        context.ruDataStore.edit { it[Keys.DEFAULT_MODE] = mode.key }
    }

    suspend fun setDefaultStatus(status: LearningStatus) {
        context.ruDataStore.edit { it[Keys.DEFAULT_STATUS] = status.key }
    }

    suspend fun setDefaultViewMode(mode: ViewMode) {
        context.ruDataStore.edit { it[Keys.DEFAULT_VIEW_MODE] = mode.key }
    }

    suspend fun loadDraftSession(defaults: AppPreferences): DraftSessionState {
        val p = context.ruDataStore.data.first()
        val isRunning = p[Keys.DRAFT_IS_RUNNING] ?: false
        val savedElapsed = p[Keys.DRAFT_ELAPSED_SEC] ?: 0L
        val startedAt = p[Keys.DRAFT_STARTED_AT] ?: 0L
        val computedElapsed = if (isRunning && startedAt > 0L) {
            val delta = ((System.currentTimeMillis() - startedAt) / 1000L).coerceAtLeast(0L)
            savedElapsed + delta
        } else {
            savedElapsed
        }
        return DraftSessionState(
            subject = p[Keys.DRAFT_SUBJECT] ?: "",
            chapter = p[Keys.DRAFT_CHAPTER] ?: "",
            lecture = p[Keys.DRAFT_LECTURE] ?: "",
            mode = p[Keys.DRAFT_MODE]?.let { StudyMode.fromKey(it) } ?: defaults.defaultMode,
            status = p[Keys.DRAFT_STATUS]?.let { LearningStatus.fromKey(it) } ?: defaults.defaultStatus,
            manualMinutes = (p[Keys.DRAFT_MANUAL_MIN] ?: 25).coerceIn(1, 1440),
            isTimerMode = p[Keys.DRAFT_IS_TIMER] ?: false,
            isRunning = isRunning,
            elapsedSeconds = computedElapsed,
            startedAtEpochMs = if (isRunning) System.currentTimeMillis() else 0L,
            isReviewingStopped = p[Keys.DRAFT_REVIEWING] ?: false,
            manualNotes = NoteSerializer.decodeCards(p[Keys.DRAFT_MANUAL_NOTES] ?: ""),
            timerNotes = NoteSerializer.decodeCards(p[Keys.DRAFT_TIMER_NOTES] ?: "")
        )
    }

    suspend fun saveDraftSession(draft: DraftSessionState) {
        context.ruDataStore.edit { p ->
            p[Keys.DRAFT_SUBJECT] = draft.subject
            p[Keys.DRAFT_CHAPTER] = draft.chapter
            p[Keys.DRAFT_LECTURE] = draft.lecture
            p[Keys.DRAFT_MODE] = draft.mode.key
            p[Keys.DRAFT_STATUS] = draft.status.key
            p[Keys.DRAFT_MANUAL_MIN] = draft.manualMinutes
            p[Keys.DRAFT_IS_TIMER] = draft.isTimerMode
            p[Keys.DRAFT_IS_RUNNING] = draft.isRunning
            p[Keys.DRAFT_ELAPSED_SEC] = draft.elapsedSeconds
            p[Keys.DRAFT_STARTED_AT] = if (draft.isRunning) System.currentTimeMillis() else 0L
            p[Keys.DRAFT_REVIEWING] = draft.isReviewingStopped
            p[Keys.DRAFT_MANUAL_NOTES] = NoteSerializer.encodeCards(draft.manualNotes)
            p[Keys.DRAFT_TIMER_NOTES] = NoteSerializer.encodeCards(draft.timerNotes)
        }
    }

    suspend fun resetAllPreferences() {
        context.ruDataStore.edit { it.clear() }
    }
}
