package com.example.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ProjectExportManager {

    const val EXPORT_ZIP_NAME = "app_export.zip"
    private const val SOURCE_MANIFEST_ASSET = "project_source_manifest.txt"
    private const val SOURCE_ASSET_PREFIX = "project_source/"
    private const val WEB_INDEX_ASSET = "web_export/index.html"

    data class ExportBundleSummary(
        val sourceFileCount: Int,
        val webHtmlSizeBytes: Long,
        val sessionsCount: Int,
        val syllabusEntriesCount: Int,
        val labelsCount: Int,
        val linksCount: Int
    ) {
        val formattedWebSizeKb: String
            get() = String.format(Locale.US, "%.1f KB", webHtmlSizeBytes / 1024.0)
    }

    data class ZipBuildOutcome(
        val success: Boolean,
        val zipFile: File? = null,
        val sizeBytes: Long = 0L,
        val totalEntries: Int = 0,
        val savedLocationLabel: String = "",
        val errorMessage: String? = null
    ) {
        val formattedSize: String
            get() = if (sizeBytes >= 1024 * 1024) {
                String.format(Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0))
            } else {
                String.format(Locale.US, "%.1f KB", sizeBytes / 1024.0)
            }
    }

    fun readSourceManifest(context: Context): List<String> {
        return try {
            context.assets.open(SOURCE_MANIFEST_ASSET).bufferedReader(StandardCharsets.UTF_8).useLines { lines ->
                lines.map { it.trim() }.filter { it.isNotEmpty() }.toList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getSummary(
        context: Context,
        sessionsCount: Int,
        syllabusEntriesCount: Int,
        labelsCount: Int,
        linksCount: Int
    ): ExportBundleSummary {
        val manifest = readSourceManifest(context)
        val htmlSize = try {
            context.assets.open(WEB_INDEX_ASSET).use { it.readBytes().size.toLong() }
        } catch (_: Exception) {
            0L
        }
        return ExportBundleSummary(
            sourceFileCount = manifest.size,
            webHtmlSizeBytes = htmlSize,
            sessionsCount = sessionsCount,
            syllabusEntriesCount = syllabusEntriesCount,
            labelsCount = labelsCount,
            linksCount = linksCount
        )
    }

    fun buildDatabaseJson(
        sessions: List<StudySession>,
        syllabusEntries: List<SyllabusEntryEntity>,
        labels: List<NoteLabel>,
        links: List<SavedLink>,
        preferences: AppPreferences
    ): String {
        val root = JSONObject()
        root.put("exportedAt", System.currentTimeMillis())
        root.put("exportedDate", DateUtilsRu.todayIso())
        root.put("appName", "Revision Universe")

        val sessionsArr = JSONArray()
        sessions.forEach { s ->
            val o = JSONObject()
            o.put("id", s.id)
            o.put("date", s.date)
            o.put("timestamp", s.timestamp)
            o.put("subject", s.subject)
            o.put("chapter", s.chapter)
            o.put("lecture", s.lecture)
            o.put("durationMinutes", s.durationMinutes)
            o.put("mode", s.mode.key)
            o.put("status", s.status.key)
            o.put("notesJson", s.notesJson)
            o.put("plainNotes", s.plainNotes)
            val cardsArr = JSONArray()
            s.noteCards.forEach { c ->
                val co = JSONObject()
                co.put("id", c.id)
                co.put("labelId", c.labelId)
                co.put("labelName", c.labelName)
                co.put("colorHex", c.colorHex)
                co.put("content", c.content)
                co.put("pinned", c.pinned)
                co.put("createdAt", c.createdAt)
                co.put("updatedAt", c.updatedAt)
                cardsArr.put(co)
            }
            o.put("noteCards", cardsArr)
            sessionsArr.put(o)
        }
        root.put("study_sessions", sessionsArr)

        val syllabusArr = JSONArray()
        syllabusEntries.forEach { e ->
            val o = JSONObject()
            o.put("id", e.id)
            o.put("subject", e.subject)
            o.put("chapter", e.chapter)
            o.put("lecture", e.lecture)
            o.put("subjectOrder", e.subjectOrder)
            o.put("chapterOrder", e.chapterOrder)
            o.put("lectureOrder", e.lectureOrder)
            syllabusArr.put(o)
        }
        root.put("syllabus_entries", syllabusArr)

        val labelsArr = JSONArray()
        labels.forEach { l ->
            val o = JSONObject()
            o.put("id", l.id)
            o.put("name", l.name)
            o.put("colorHex", l.colorHex)
            o.put("sortOrder", l.sortOrder)
            labelsArr.put(o)
        }
        root.put("note_labels", labelsArr)

        val linksArr = JSONArray()
        links.forEach { lk ->
            val o = JSONObject()
            o.put("id", lk.id)
            o.put("title", lk.title)
            o.put("url", lk.url)
            o.put("createdAt", lk.createdAt)
            linksArr.put(o)
        }
        root.put("saved_links", linksArr)

        val prefsObj = JSONObject()
        prefsObj.put("isDarkTheme", preferences.isDarkTheme)
        prefsObj.put("bgStyle", preferences.bgStyle.id)
        prefsObj.put("cardStyle", preferences.cardStyle.id)
        prefsObj.put("dailyGoalMinutes", preferences.dailyGoalMinutes)
        prefsObj.put("defaultMode", preferences.defaultMode.key)
        prefsObj.put("defaultStatus", preferences.defaultStatus.key)
        prefsObj.put("defaultViewMode", preferences.defaultViewMode.key)
        root.put("preferences", prefsObj)

        return root.toString(2)
    }

    fun writeProjectZipToStream(
        context: Context,
        outputStream: OutputStream,
        dataJson: String
    ): Int {
        var entryCount = 0
        val buffer = ByteArray(32 * 1024)
        ZipOutputStream(outputStream).use { zos ->
            // 1. source/ folder: every real project source file with original folder structure
            val sourcePaths = readSourceManifest(context)
            sourcePaths.forEach { relPath ->
                val cleanRel = relPath.removePrefix("/")
                try {
                    context.assets.open(SOURCE_ASSET_PREFIX + cleanRel).use { input ->
                        zos.putNextEntry(ZipEntry("source/$cleanRel"))
                        var read = input.read(buffer)
                        while (read >= 0) {
                            if (read > 0) zos.write(buffer, 0, read)
                            read = input.read(buffer)
                        }
                        zos.closeEntry()
                        entryCount++
                    }
                } catch (_: Exception) {
                }
            }

            // 2. index.html: complete working single-file HTML version
            context.assets.open(WEB_INDEX_ASSET).use { input ->
                zos.putNextEntry(ZipEntry("index.html"))
                var read = input.read(buffer)
                while (read >= 0) {
                    if (read > 0) zos.write(buffer, 0, read)
                    read = input.read(buffer)
                }
                zos.closeEntry()
                entryCount++
            }

            // 3. data.json: complete live Room database + preferences dump
            zos.putNextEntry(ZipEntry("data.json"))
            zos.write(dataJson.toByteArray(StandardCharsets.UTF_8))
            zos.closeEntry()
            entryCount++
        }
        return entryCount
    }

    fun buildZipInCache(context: Context, dataJson: String): ZipBuildOutcome {
        return try {
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outFile = File(dir, EXPORT_ZIP_NAME)
            val entries = outFile.outputStream().use { os ->
                writeProjectZipToStream(context, os, dataJson)
            }
            ZipBuildOutcome(
                success = true,
                zipFile = outFile,
                sizeBytes = outFile.length(),
                totalEntries = entries,
                savedLocationLabel = outFile.name
            )
        } catch (e: Exception) {
            ZipBuildOutcome(success = false, errorMessage = e.message ?: "ZIP बनाने में त्रुटि हुई")
        }
    }

    fun shareZipFile(context: Context, zipFile: File): Boolean {
        return try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                zipFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/zip"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, EXPORT_ZIP_NAME)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share $EXPORT_ZIP_NAME").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun saveZipDirectlyToDownloadsApi29Plus(context: Context, dataJson: String): ZipBuildOutcome {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return ZipBuildOutcome(success = false, errorMessage = "REQUIRES_SAF")
        }
        return try {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, EXPORT_ZIP_NAME)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: return ZipBuildOutcome(success = false, errorMessage = "Downloads में फ़ाइल बनाने में त्रुटि")

            var entries = 0
            val tempOutcome = buildZipInCache(context, dataJson)
            if (!tempOutcome.success || tempOutcome.zipFile == null) return tempOutcome
            entries = tempOutcome.totalEntries

            resolver.openOutputStream(uri)?.use { out ->
                tempOutcome.zipFile.inputStream().use { inp ->
                    inp.copyTo(out, 32 * 1024)
                }
            } ?: return ZipBuildOutcome(success = false, errorMessage = "आउटपुट स्ट्रीम खोलने में त्रुटि")

            val doneValues = ContentValues().apply {
                put(MediaStore.Downloads.IS_PENDING, 0)
            }
            resolver.update(uri, doneValues, null, null)

            ZipBuildOutcome(
                success = true,
                zipFile = tempOutcome.zipFile,
                sizeBytes = tempOutcome.sizeBytes,
                totalEntries = entries,
                savedLocationLabel = "Downloads/$EXPORT_ZIP_NAME"
            )
        } catch (e: Exception) {
            ZipBuildOutcome(success = false, errorMessage = e.message ?: "Downloads में सेव करने में त्रुटि")
        }
    }

    fun writeZipToUri(context: Context, uri: Uri, dataJson: String): ZipBuildOutcome {
        return try {
            val tempOutcome = buildZipInCache(context, dataJson)
            if (!tempOutcome.success || tempOutcome.zipFile == null) return tempOutcome
            val out = context.contentResolver.openOutputStream(uri)
                ?: return ZipBuildOutcome(success = false, errorMessage = "फ़ाइल लिखने में असमर्थ")
            out.use { os ->
                tempOutcome.zipFile.inputStream().use { inp ->
                    inp.copyTo(os, 32 * 1024)
                }
            }
            tempOutcome.copy(savedLocationLabel = EXPORT_ZIP_NAME)
        } catch (e: Exception) {
            ZipBuildOutcome(success = false, errorMessage = e.message ?: "फ़ाइल सेव करने में त्रुटि")
        }
    }
}
