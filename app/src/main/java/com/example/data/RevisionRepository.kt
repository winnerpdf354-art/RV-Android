package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RevisionRepository(
    private val db: AppDatabase,
    private val prefs: PreferencesRepository
) {
    private val sessionDao = db.sessionDao()
    private val syllabusDao = db.syllabusDao()
    private val noteLabelDao = db.noteLabelDao()
    private val savedLinkDao = db.savedLinkDao()

    val sessionsFlow: Flow<List<StudySession>> = sessionDao.observeAll().map { list ->
        list.map { it.toDomain() }
    }

    val syllabusMapFlow: Flow<Map<String, Map<String, List<String>>>> =
        syllabusDao.observeAll().map { entries -> buildSyllabusTree(entries) }

    val noteLabelsFlow: Flow<List<NoteLabel>> = noteLabelDao.observeAll().map { list ->
        list.map { NoteLabel(it.id, it.name, it.colorHex, it.sortOrder) }
    }

    val savedLinksFlow: Flow<List<SavedLink>> = savedLinkDao.observeAll().map { list ->
        list.map { SavedLink(it.id, it.title, it.url, it.createdAt) }
    }

    suspend fun ensureSeeded() {
        val seeded = prefs.isSyllabusSeeded()
        if (!seeded && syllabusDao.count() == 0) {
            seedDefaultSyllabus()
            prefs.markSyllabusSeeded(true)
        }
        if (noteLabelDao.count() == 0) {
            seedDefaultLabels()
        }
    }

    suspend fun seedDefaultSyllabus() {
        val entities = mutableListOf<SyllabusEntryEntity>()
        var sOrder = 0
        DefaultSyllabusData.defaultData.forEach { (sub, chapters) ->
            var cOrder = 0
            chapters.forEach { (chap, lectures) ->
                lectures.forEachIndexed { lIdx, lec ->
                    entities.add(
                        SyllabusEntryEntity(
                            subject = sub,
                            chapter = chap,
                            lecture = lec,
                            subjectOrder = sOrder,
                            chapterOrder = cOrder,
                            lectureOrder = lIdx
                        )
                    )
                }
                cOrder++
            }
            sOrder++
        }
        syllabusDao.insertAll(entities)
    }

    suspend fun seedDefaultLabels() {
        noteLabelDao.insertAll(
            DefaultSyllabusData.defaultNoteLabels.map {
                NoteLabelEntity(it.id, it.name, it.colorHex, it.sortOrder)
            }
        )
    }

    suspend fun addSession(session: StudySession) {
        sessionDao.insert(session.toEntity())
    }

    suspend fun addSessions(sessions: List<StudySession>) {
        if (sessions.isNotEmpty()) {
            sessionDao.insertAll(sessions.map { it.toEntity() })
        }
    }

    suspend fun deleteSessions(ids: List<String>): List<StudySession> {
        if (ids.isEmpty()) return emptyList()
        val idSet = ids.toSet()
        val existing = sessionDao.getAllOnce().filter { it.id in idSet }.map { it.toDomain() }
        sessionDao.deleteByIds(ids)
        return existing
    }

    suspend fun clearAllSessions(): List<StudySession> {
        val all = sessionDao.getAllOnce().map { it.toDomain() }
        sessionDao.deleteAll()
        return all
    }

    suspend fun getAllSessionsOnce(): List<StudySession> =
        sessionDao.getAllOnce().map { it.toDomain() }

    suspend fun getSyllabusSnapshot(): List<SyllabusEntryEntity> =
        syllabusDao.getAllOnce()

    suspend fun restoreSyllabusSnapshot(entries: List<SyllabusEntryEntity>) {
        syllabusDao.deleteAll()
        if (entries.isNotEmpty()) {
            syllabusDao.insertAll(entries.map { it.copy(id = 0) })
        }
    }

    suspend fun addSubject(subjectName: String, initialChapter: String = "Chapter 1", initialLecture: String = "Lec 1") {
        val cleanSub = subjectName.trim()
        if (cleanSub.isEmpty()) return
        val existing = syllabusDao.getAllOnce()
        val maxSubOrder = existing.maxOfOrNull { it.subjectOrder } ?: -1
        syllabusDao.insert(
            SyllabusEntryEntity(
                subject = cleanSub,
                chapter = initialChapter.trim().ifEmpty { "Chapter 1" },
                lecture = initialLecture.trim().ifEmpty { "Lec 1" },
                subjectOrder = maxSubOrder + 1,
                chapterOrder = 0,
                lectureOrder = 0
            )
        )
    }

    suspend fun addChapter(subject: String, chapterName: String, lectureCount: Int = 1) {
        val cleanSub = subject.trim()
        val cleanChap = chapterName.trim()
        if (cleanSub.isEmpty() || cleanChap.isEmpty()) return
        val existing = syllabusDao.getAllOnce().filter { it.subject == cleanSub }
        val sOrder = existing.firstOrNull()?.subjectOrder
            ?: ((syllabusDao.getAllOnce().maxOfOrNull { it.subjectOrder } ?: -1) + 1)
        val cOrder = (existing.maxOfOrNull { it.chapterOrder } ?: -1) + 1
        val count = lectureCount.coerceIn(1, 200)
        val rows = (1..count).map { idx ->
            SyllabusEntryEntity(
                subject = cleanSub,
                chapter = cleanChap,
                lecture = "Lec $idx",
                subjectOrder = sOrder,
                chapterOrder = cOrder,
                lectureOrder = idx - 1
            )
        }
        syllabusDao.insertAll(rows)
    }

    suspend fun addLecturesToChapter(subject: String, chapter: String, countToAdd: Int): List<String> {
        val count = countToAdd.coerceIn(1, 100)
        val existing = syllabusDao.getAllOnce().filter { it.subject == subject && it.chapter == chapter }
        val sOrder = existing.firstOrNull()?.subjectOrder
            ?: syllabusDao.getAllOnce().firstOrNull { it.subject == subject }?.subjectOrder
            ?: ((syllabusDao.getAllOnce().maxOfOrNull { it.subjectOrder } ?: -1) + 1)
        val cOrder = existing.firstOrNull()?.chapterOrder
            ?: ((syllabusDao.getAllOnce().filter { it.subject == subject }.maxOfOrNull { it.chapterOrder } ?: -1) + 1)
        val existingNames = existing.map { it.lecture }.toMutableSet()
        var maxNum = existing.map { DateUtilsRu.naturalLectureNumber(it.lecture) }
            .filter { it != Int.MAX_VALUE }
            .maxOrNull() ?: 0
        var nextOrder = (existing.maxOfOrNull { it.lectureOrder } ?: -1) + 1
        val added = mutableListOf<String>()
        val toInsert = mutableListOf<SyllabusEntryEntity>()
        repeat(count) {
            maxNum++
            while (existingNames.contains("Lec $maxNum")) {
                maxNum++
            }
            val name = "Lec $maxNum"
            existingNames.add(name)
            added.add(name)
            toInsert.add(
                SyllabusEntryEntity(
                    subject = subject,
                    chapter = chapter,
                    lecture = name,
                    subjectOrder = sOrder,
                    chapterOrder = cOrder,
                    lectureOrder = nextOrder++
                )
            )
        }
        syllabusDao.insertAll(toInsert)
        return added
    }

    suspend fun addCustomLecture(subject: String, chapter: String, lectureName: String): Boolean {
        val clean = lectureName.trim()
        if (clean.isEmpty()) return false
        val existing = syllabusDao.getAllOnce().filter { it.subject == subject && it.chapter == chapter }
        if (existing.any { it.lecture.equals(clean, ignoreCase = true) }) return false
        val sOrder = existing.firstOrNull()?.subjectOrder
            ?: syllabusDao.getAllOnce().firstOrNull { it.subject == subject }?.subjectOrder
            ?: 0
        val cOrder = existing.firstOrNull()?.chapterOrder ?: 0
        val nextOrder = (existing.maxOfOrNull { it.lectureOrder } ?: -1) + 1
        syllabusDao.insert(
            SyllabusEntryEntity(
                subject = subject,
                chapter = chapter,
                lecture = clean,
                subjectOrder = sOrder,
                chapterOrder = cOrder,
                lectureOrder = nextOrder
            )
        )
        return true
    }

    suspend fun deleteSubjects(subjects: List<String>) {
        if (subjects.isNotEmpty()) syllabusDao.deleteSubjects(subjects)
    }

    suspend fun deleteChapters(subject: String, chapters: List<String>) {
        if (chapters.isNotEmpty()) syllabusDao.deleteChapters(subject, chapters)
    }

    suspend fun deleteLectures(subject: String, chapter: String, lectures: List<String>) {
        if (lectures.isNotEmpty()) syllabusDao.deleteLectures(subject, chapter, lectures)
    }

    suspend fun restoreMissingEntries(entries: List<Triple<String, String, String>>): Int {
        if (entries.isEmpty()) return 0
        val existing = syllabusDao.getAllOnce()
        val existingTriples = existing.map { Triple(it.subject, it.chapter, it.lecture) }.toSet()
        val subjectOrders = existing.associate { it.subject to it.subjectOrder }.toMutableMap()
        var nextSubOrder = (existing.maxOfOrNull { it.subjectOrder } ?: -1) + 1

        val defaultSubjectIndex = DefaultSyllabusData.defaultData.keys.withIndex().associate { it.value to it.index }
        val toInsert = mutableListOf<SyllabusEntryEntity>()

        entries.forEach { (sub, chap, lec) ->
            if (Triple(sub, chap, lec) in existingTriples) return@forEach
            val sOrder = subjectOrders.getOrPut(sub) {
                defaultSubjectIndex[sub] ?: nextSubOrder++
            }
            val chapKeys = DefaultSyllabusData.defaultData[sub]?.keys?.toList().orEmpty()
            val cOrder = chapKeys.indexOf(chap).takeIf { it >= 0 } ?: 999
            val lOrder = DateUtilsRu.naturalLectureNumber(lec).takeIf { it != Int.MAX_VALUE }?.minus(1) ?: 999
            toInsert.add(
                SyllabusEntryEntity(
                    subject = sub,
                    chapter = chap,
                    lecture = lec,
                    subjectOrder = sOrder,
                    chapterOrder = cOrder,
                    lectureOrder = lOrder
                )
            )
        }
        if (toInsert.isNotEmpty()) {
            syllabusDao.insertAll(toInsert)
        }
        return toInsert.size
    }

    suspend fun importSyllabusTriples(triples: List<Triple<String, String, String>>): Pair<Int, Int> {
        val existing = syllabusDao.getAllOnce()
        val existingSet = existing.map { Triple(it.subject, it.chapter, it.lecture) }.toMutableSet()
        val subjectOrderMap = existing.associate { it.subject to it.subjectOrder }.toMutableMap()
        val chapterOrderMap = existing.associate { (it.subject to it.chapter) to it.chapterOrder }.toMutableMap()
        var nextSub = (existing.maxOfOrNull { it.subjectOrder } ?: -1) + 1

        var added = 0
        var skipped = 0
        val toInsert = mutableListOf<SyllabusEntryEntity>()

        triples.forEach { (rawSub, rawChap, rawLec) ->
            val sub = rawSub.trim()
            val chap = rawChap.trim()
            val lec = rawLec.trim().ifEmpty { "Lec 1" }
            if (sub.isEmpty() || chap.isEmpty()) return@forEach
            val key = Triple(sub, chap, lec)
            if (key in existingSet) {
                skipped++
                return@forEach
            }
            existingSet.add(key)
            val sOrder = subjectOrderMap.getOrPut(sub) { nextSub++ }
            val cOrder = chapterOrderMap.getOrPut(sub to chap) {
                (chapterOrderMap.filterKeys { it.first == sub }.values.maxOrNull() ?: -1) + 1
            }
            val lOrder = DateUtilsRu.naturalLectureNumber(lec).takeIf { it != Int.MAX_VALUE }?.minus(1) ?: 999
            toInsert.add(
                SyllabusEntryEntity(
                    subject = sub,
                    chapter = chap,
                    lecture = lec,
                    subjectOrder = sOrder,
                    chapterOrder = cOrder,
                    lectureOrder = lOrder
                )
            )
            added++
        }
        if (toInsert.isNotEmpty()) {
            syllabusDao.insertAll(toInsert)
        }
        return added to skipped
    }

    suspend fun upsertNoteLabel(label: NoteLabel) {
        noteLabelDao.upsert(NoteLabelEntity(label.id, label.name, label.colorHex, label.sortOrder))
    }

    suspend fun saveNoteLabelsOrder(labels: List<NoteLabel>) {
        noteLabelDao.insertAll(
            labels.mapIndexed { idx, l -> NoteLabelEntity(l.id, l.name, l.colorHex, idx) }
        )
    }

    suspend fun deleteNoteLabel(id: String) {
        noteLabelDao.deleteById(id)
    }

    suspend fun upsertSavedLink(link: SavedLink) {
        savedLinkDao.upsert(SavedLinkEntity(link.id, link.title, link.url, link.createdAt))
    }

    suspend fun deleteSavedLink(id: String) {
        savedLinkDao.deleteById(id)
    }

    suspend fun resetAllData() {
        sessionDao.deleteAll()
        syllabusDao.deleteAll()
        noteLabelDao.deleteAll()
        savedLinkDao.deleteAll()
        prefs.resetAllPreferences()
        seedDefaultSyllabus()
        seedDefaultLabels()
        prefs.markSyllabusSeeded(true)
    }

    companion object {
        fun buildSyllabusTree(entries: List<SyllabusEntryEntity>): Map<String, Map<String, List<String>>> {
            val result = linkedMapOf<String, LinkedHashMap<String, MutableList<String>>>()
            entries.forEach { e ->
                val chapters = result.getOrPut(e.subject) { linkedMapOf() }
                val lectures = chapters.getOrPut(e.chapter) { mutableListOf() }
                if (!lectures.contains(e.lecture)) {
                    lectures.add(e.lecture)
                }
            }
            return result
        }

        fun computeMissingDefaultSyllabus(
            current: Map<String, Map<String, List<String>>>
        ): List<MissingSyllabusSubject> {
            val out = mutableListOf<MissingSyllabusSubject>()
            DefaultSyllabusData.defaultData.forEach { (sub, defChaps) ->
                val curChaps = current[sub]
                if (curChaps == null) {
                    out.add(
                        MissingSyllabusSubject(
                            subject = sub,
                            isWholeSubjectMissing = true,
                            chapters = defChaps.map { (ch, lecs) ->
                                MissingSyllabusChapter(ch, true, lecs)
                            }
                        )
                    )
                } else {
                    val missingChaps = mutableListOf<MissingSyllabusChapter>()
                    defChaps.forEach { (ch, defLecs) ->
                        val curLecs = curChaps[ch]?.toSet()
                        if (curLecs == null) {
                            missingChaps.add(MissingSyllabusChapter(ch, true, defLecs))
                        } else {
                            val missingLecs = defLecs.filter { it !in curLecs }
                            if (missingLecs.isNotEmpty()) {
                                missingChaps.add(MissingSyllabusChapter(ch, false, missingLecs))
                            }
                        }
                    }
                    if (missingChaps.isNotEmpty()) {
                        out.add(
                            MissingSyllabusSubject(
                                subject = sub,
                                isWholeSubjectMissing = false,
                                chapters = missingChaps
                            )
                        )
                    }
                }
            }
            return out
        }
    }
}

private fun SessionEntity.toDomain(): StudySession = StudySession(
    id = id,
    date = date,
    timestamp = timestamp,
    subject = subject,
    chapter = chapter,
    lecture = lecture,
    durationMinutes = durationMinutes,
    mode = StudyMode.fromKey(mode),
    status = LearningStatus.fromKey(status),
    notesJson = notesJson,
    plainNotes = plainNotes
)

private fun StudySession.toEntity(): SessionEntity = SessionEntity(
    id = id,
    date = date,
    timestamp = timestamp,
    subject = subject,
    chapter = chapter,
    lecture = lecture,
    durationMinutes = durationMinutes,
    mode = mode.key,
    status = status.key,
    notesJson = notesJson,
    plainNotes = plainNotes
)
