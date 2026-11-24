package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "study_sessions",
    indices = [
        Index(value = ["date"]),
        Index(value = ["subject", "chapter", "lecture"])
    ]
)
data class SessionEntity(
    @PrimaryKey val id: String,
    val date: String,
    val timestamp: Long,
    val subject: String,
    val chapter: String,
    val lecture: String,
    val durationMinutes: Int,
    val mode: String,
    val status: String,
    val notesJson: String,
    val plainNotes: String
)

@Entity(
    tableName = "syllabus_entries",
    indices = [
        Index(value = ["subject", "chapter", "lecture"], unique = true),
        Index(value = ["subject"]),
        Index(value = ["subject", "chapter"])
    ]
)
data class SyllabusEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val chapter: String,
    val lecture: String,
    val subjectOrder: Int = 0,
    val chapterOrder: Int = 0,
    val lectureOrder: Int = 0
)

@Entity(tableName = "note_labels")
data class NoteLabelEntity(
    @PrimaryKey val id: String,
    val name: String,
    val colorHex: String,
    val sortOrder: Int
)

@Entity(tableName = "saved_links")
data class SavedLinkEntity(
    @PrimaryKey val id: String,
    val title: String,
    val url: String,
    val createdAt: Long
)

@Dao
interface SessionDao {
    @Query("SELECT * FROM study_sessions ORDER BY date DESC, timestamp DESC")
    fun observeAll(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM study_sessions ORDER BY date DESC, timestamp DESC")
    suspend fun getAllOnce(): List<SessionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<SessionEntity>)

    @Query("DELETE FROM study_sessions WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM study_sessions")
    suspend fun deleteAll()
}

@Dao
interface SyllabusDao {
    @Query("SELECT * FROM syllabus_entries ORDER BY subjectOrder ASC, subject ASC, chapterOrder ASC, chapter ASC, lectureOrder ASC, id ASC")
    fun observeAll(): Flow<List<SyllabusEntryEntity>>

    @Query("SELECT * FROM syllabus_entries ORDER BY subjectOrder ASC, subject ASC, chapterOrder ASC, chapter ASC, lectureOrder ASC, id ASC")
    suspend fun getAllOnce(): List<SyllabusEntryEntity>

    @Query("SELECT COUNT(*) FROM syllabus_entries")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: SyllabusEntryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entities: List<SyllabusEntryEntity>)

    @Query("DELETE FROM syllabus_entries WHERE subject IN (:subjects)")
    suspend fun deleteSubjects(subjects: List<String>)

    @Query("DELETE FROM syllabus_entries WHERE subject = :subject AND chapter IN (:chapters)")
    suspend fun deleteChapters(subject: String, chapters: List<String>)

    @Query("DELETE FROM syllabus_entries WHERE subject = :subject AND chapter = :chapter AND lecture IN (:lectures)")
    suspend fun deleteLectures(subject: String, chapter: String, lectures: List<String>)

    @Query("DELETE FROM syllabus_entries")
    suspend fun deleteAll()
}

@Dao
interface NoteLabelDao {
    @Query("SELECT * FROM note_labels ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<NoteLabelEntity>>

    @Query("SELECT * FROM note_labels ORDER BY sortOrder ASC")
    suspend fun getAllOnce(): List<NoteLabelEntity>

    @Query("SELECT COUNT(*) FROM note_labels")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(labels: List<NoteLabelEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(label: NoteLabelEntity)

    @Query("DELETE FROM note_labels WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM note_labels")
    suspend fun deleteAll()
}

@Dao
interface SavedLinkDao {
    @Query("SELECT * FROM saved_links ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<SavedLinkEntity>>

    @Query("SELECT * FROM saved_links ORDER BY createdAt DESC")
    suspend fun getAllOnce(): List<SavedLinkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(link: SavedLinkEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(links: List<SavedLinkEntity>)

    @Query("DELETE FROM saved_links WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM saved_links")
    suspend fun deleteAll()
}

@Database(
    entities = [
        SessionEntity::class,
        SyllabusEntryEntity::class,
        NoteLabelEntity::class,
        SavedLinkEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
    abstract fun syllabusDao(): SyllabusDao
    abstract fun noteLabelDao(): NoteLabelDao
    abstract fun savedLinkDao(): SavedLinkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "revision_universe_offline.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
