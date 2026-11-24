package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Xml
import androidx.core.content.FileProvider
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ImportExportManager {

    enum class DataScope(val label: String) {
        HISTORY("History"),
        SYLLABUS("Syllabus")
    }

    data class ImportResult(
        val addedCount: Int,
        val skippedDuplicates: Int,
        val errorMessage: String? = null
    )

    // --- CSV Generation (UTF-8 BOM + Formula Injection Protection) ---

    private fun csvEscape(value: String): String {
        val safe = if (value.isNotEmpty() && value[0] in listOf('=', '+', '-', '@')) {
            "'$value"
        } else {
            value
        }
        val needsQuote = safe.contains(',') || safe.contains('"') || safe.contains('\n') || safe.contains('\r')
        return if (needsQuote) "\"${safe.replace("\"", "\"\"")}\"" else safe
    }

    fun buildHistoryCsv(sessions: List<StudySession>): ByteArray {
        val sb = StringBuilder()
        sb.append("Date,Subject,Chapter,Lecture,Time(min),Mode,Difficulty,Notes\r\n")
        sessions.forEach { s ->
            val noteText = s.plainNotes.ifBlank { NoteSerializer.toPlainSummary(s.noteCards) }
            sb.append(
                listOf(
                    s.date,
                    s.subject,
                    s.chapter,
                    s.lecture,
                    s.durationMinutes.toString(),
                    s.mode.key,
                    s.status.displayLabel,
                    noteText
                ).joinToString(",") { csvEscape(it) }
            ).append("\r\n")
        }
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        return bom + sb.toString().toByteArray(StandardCharsets.UTF_8)
    }

    fun buildSyllabusCsv(syllabus: Map<String, Map<String, List<String>>>): ByteArray {
        val sb = StringBuilder()
        sb.append("Subject,Chapter,Lecture\r\n")
        syllabus.forEach { (sub, chaps) ->
            chaps.forEach { (chap, lecs) ->
                lecs.forEach { lec ->
                    sb.append(listOf(sub, chap, lec).joinToString(",") { csvEscape(it) }).append("\r\n")
                }
            }
        }
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        return bom + sb.toString().toByteArray(StandardCharsets.UTF_8)
    }

    fun buildHistoryTsv(sessions: List<StudySession>): String {
        val sb = StringBuilder()
        sb.append("Date\tSubject\tChapter\tLecture\tTime(min)\tMode\tDifficulty\tNotes\n")
        sessions.forEach { s ->
            val noteText = (s.plainNotes.ifBlank { NoteSerializer.toPlainSummary(s.noteCards) })
                .replace("\t", " ")
                .replace("\n", " | ")
            sb.append(
                listOf(
                    s.date,
                    s.subject,
                    s.chapter,
                    s.lecture,
                    s.durationMinutes.toString(),
                    s.mode.key,
                    s.status.displayLabel,
                    noteText
                ).joinToString("\t")
            ).append("\n")
        }
        return sb.toString()
    }

    fun buildSyllabusTsv(syllabus: Map<String, Map<String, List<String>>>): String {
        val sb = StringBuilder()
        sb.append("Subject\tChapter\tLecture\n")
        syllabus.forEach { (sub, chaps) ->
            chaps.forEach { (chap, lecs) ->
                lecs.forEach { lec ->
                    sb.append("$sub\t$chap\t$lec\n")
                }
            }
        }
        return sb.toString()
    }

    // --- Pure-Kotlin Minimal Valid OpenXML .xlsx Generator ---

    private fun xmlEscape(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun colLetter(index: Int): String {
        var n = index + 1
        val sb = StringBuilder()
        while (n > 0) {
            val rem = (n - 1) % 26
            sb.insert(0, ('A' + rem))
            n = (n - 1) / 26
        }
        return sb.toString()
    }

    fun buildXlsxBytes(sheetName: String, rows: List<List<String>>): ByteArray {
        val safeSheet = sheetName.replace(Regex("[\\\\/*?:\\[\\]]"), "").take(31).ifBlank { "Sheet1" }
        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zos ->
            fun putText(path: String, text: String) {
                zos.putNextEntry(ZipEntry(path))
                zos.write(text.toByteArray(StandardCharsets.UTF_8))
                zos.closeEntry()
            }

            putText(
                "[Content_Types].xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""
            )

            putText(
                "_rels/.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""
            )

            putText(
                "xl/workbook.xml",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets><sheet name="${xmlEscape(safeSheet)}" sheetId="1" r:id="rId1"/></sheets>
</workbook>"""
            )

            putText(
                "xl/_rels/workbook.xml.rels",
                """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
</Relationships>"""
            )

            val sheetXml = StringBuilder()
            sheetXml.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            sheetXml.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><sheetData>""")
            rows.forEachIndexed { rIdx, row ->
                val rowNum = rIdx + 1
                sheetXml.append("""<row r="$rowNum">""")
                row.forEachIndexed { cIdx, cellVal ->
                    val ref = "${colLetter(cIdx)}$rowNum"
                    val safeVal = if (cellVal.isNotEmpty() && cellVal[0] in listOf('=', '+', '-', '@')) {
                        "'$cellVal"
                    } else {
                        cellVal
                    }
                    sheetXml.append("""<c r="$ref" t="inlineStr"><is><t>${xmlEscape(safeVal)}</t></is></c>""")
                }
                sheetXml.append("</row>")
            }
            sheetXml.append("</sheetData></worksheet>")
            putText("xl/worksheets/sheet1.xml", sheetXml.toString())
        }
        return baos.toByteArray()
    }

    fun buildHistoryXlsx(sessions: List<StudySession>): ByteArray {
        val rows = mutableListOf<List<String>>()
        rows.add(listOf("Date", "Subject", "Chapter", "Lecture", "Time(min)", "Mode", "Difficulty", "Notes"))
        sessions.forEach { s ->
            val noteText = s.plainNotes.ifBlank { NoteSerializer.toPlainSummary(s.noteCards) }
            rows.add(
                listOf(
                    s.date,
                    s.subject,
                    s.chapter,
                    s.lecture,
                    s.durationMinutes.toString(),
                    s.mode.key,
                    s.status.displayLabel,
                    noteText
                )
            )
        }
        return buildXlsxBytes("Revision History", rows)
    }

    fun buildSyllabusXlsx(syllabus: Map<String, Map<String, List<String>>>): ByteArray {
        val rows = mutableListOf<List<String>>()
        rows.add(listOf("Subject", "Chapter", "Lecture"))
        syllabus.forEach { (sub, chaps) ->
            chaps.forEach { (chap, lecs) ->
                lecs.forEach { lec ->
                    rows.add(listOf(sub, chap, lec))
                }
            }
        }
        return buildXlsxBytes("Master Syllabus", rows)
    }

    // --- Pure-Kotlin CSV & XLSX Parsers ---

    fun parseTableFromStream(inputStream: InputStream, fileNameHint: String): List<List<String>> {
        val bytes = inputStream.readBytes()
        val isZip = bytes.size >= 2 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()
        return if (isZip || fileNameHint.endsWith(".xlsx", ignoreCase = true)) {
            parseXlsx(bytes)
        } else {
            val text = bytes.toString(StandardCharsets.UTF_8).removePrefix("\uFEFF")
            parseCsv(text)
        }
    }

    fun parseCsv(text: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val currentRow = mutableListOf<String>()
        val currentCell = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < text.length && text[i + 1] == '"') {
                        currentCell.append('"')
                        i++
                    } else {
                        inQuotes = false
                    }
                } else {
                    currentCell.append(ch)
                }
            } else {
                when (ch) {
                    '"' -> inQuotes = true
                    ',' -> {
                        currentRow.add(currentCell.toString().trim())
                        currentCell.clear()
                    }
                    '\r' -> Unit
                    '\n' -> {
                        currentRow.add(currentCell.toString().trim())
                        currentCell.clear()
                        if (currentRow.any { it.isNotEmpty() }) {
                            rows.add(currentRow.toList())
                        }
                        currentRow.clear()
                    }
                    else -> currentCell.append(ch)
                }
            }
            i++
        }
        currentRow.add(currentCell.toString().trim())
        if (currentRow.any { it.isNotEmpty() }) {
            rows.add(currentRow.toList())
        }
        return rows
    }

    fun parseXlsx(bytes: ByteArray): List<List<String>> {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    entries[entry.name] = zis.readBytes()
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }
        val sharedStrings = entries["xl/sharedStrings.xml"]?.let { parseSharedStrings(it) }.orEmpty()
        val sheetEntry = entries["xl/worksheets/sheet1.xml"]
            ?: entries.entries.firstOrNull { it.key.startsWith("xl/worksheets/sheet") && it.key.endsWith(".xml") }?.value
            ?: return emptyList()
        return parseWorksheetXml(sheetEntry, sharedStrings)
    }

    private fun parseSharedStrings(xmlBytes: ByteArray): List<String> {
        val result = mutableListOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(xmlBytes), "UTF-8")
        var inSi = false
        val sb = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "si") {
                        inSi = true
                        sb.clear()
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inSi) sb.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "si") {
                        result.add(sb.toString())
                        inSi = false
                    }
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun parseWorksheetXml(xmlBytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val parser = Xml.newPullParser()
        parser.setInput(ByteArrayInputStream(xmlBytes), "UTF-8")
        var currentRow: MutableList<String>? = null
        var cellType = ""
        var cellColIndex = 0
        var inValue = false
        var inInlineText = false
        val cellText = StringBuilder()

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> currentRow = mutableListOf()
                        "c" -> {
                            cellType = parser.getAttributeValue(null, "t") ?: ""
                            val ref = parser.getAttributeValue(null, "r") ?: ""
                            cellColIndex = columnIndexFromRef(ref, currentRow?.size ?: 0)
                            cellText.clear()
                        }
                        "v" -> inValue = true
                        "t" -> inInlineText = true
                    }
                }
                XmlPullParser.TEXT -> {
                    if (inValue || inInlineText) cellText.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v" -> inValue = false
                        "t" -> inInlineText = false
                        "c" -> {
                            val row = currentRow
                            if (row != null) {
                                while (row.size < cellColIndex) row.add("")
                                val raw = cellText.toString()
                                val decoded = if (cellType == "s") {
                                    raw.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: raw
                                } else {
                                    raw
                                }
                                row.add(decoded.trim())
                            }
                        }
                        "row" -> {
                            currentRow?.let { r ->
                                if (r.any { it.isNotEmpty() }) rows.add(r)
                            }
                            currentRow = null
                        }
                    }
                }
            }
            event = parser.next()
        }
        return rows
    }

    private fun columnIndexFromRef(ref: String, fallback: Int): Int {
        val letters = ref.takeWhile { it.isLetter() }.uppercase(Locale.US)
        if (letters.isEmpty()) return fallback
        var idx = 0
        letters.forEach { ch ->
            idx = idx * 26 + (ch - 'A' + 1)
        }
        return (idx - 1).coerceAtLeast(0)
    }

    fun parseHistoryRows(rows: List<List<String>>): List<StudySession> {
        if (rows.isEmpty()) return emptyList()
        val firstRow = rows.first().map { it.lowercase(Locale.ROOT) }
        val hasHeader = firstRow.any { it.contains("subject") || it.contains("date") || it.contains("chapter") }
        val dataRows = if (hasHeader) rows.drop(1) else rows

        val dateIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("date") }.takeIf { it >= 0 } ?: 0 else 0
        val subIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("subject") || it == "sub" }.takeIf { it >= 0 } ?: 1 else 1
        val chapIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("chapter") || it == "chap" }.takeIf { it >= 0 } ?: 2 else 2
        val lecIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("lecture") || it == "lec" }.takeIf { it >= 0 } ?: 3 else 3
        val timeIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("time") || it.contains("min") || it.contains("duration") }.takeIf { it >= 0 } ?: 4 else 4
        val modeIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("mode") || it.contains("type") } else -1
        val diffIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("diff") || it.contains("status") }.takeIf { it >= 0 } ?: 5 else 5
        val noteIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("note") }.takeIf { it >= 0 } ?: 6 else 6

        val out = mutableListOf<StudySession>()
        dataRows.forEachIndexed { idx, row ->
            val sub = row.getOrNull(subIdx)?.trim().orEmpty()
            val chap = row.getOrNull(chapIdx)?.trim().orEmpty()
            if (sub.isEmpty() || chap.isEmpty()) return@forEachIndexed
            val rawDate = row.getOrNull(dateIdx)?.trim().orEmpty()
            val normDate = normalizeDate(rawDate)
            val lec = row.getOrNull(lecIdx)?.trim().orEmpty().ifEmpty { "Lec 1" }
            val mins = Regex("\\d+").find(row.getOrNull(timeIdx).orEmpty())?.value?.toIntOrNull()?.coerceIn(1, 1440) ?: 25
            val mode = if (modeIdx >= 0) StudyMode.fromKey(row.getOrNull(modeIdx)) else StudyMode.REVISION
            val status = LearningStatus.fromKey(row.getOrNull(diffIdx))
            val noteRaw = row.getOrNull(noteIdx)?.trim().orEmpty()
            val notesJson = if (noteRaw.startsWith("[")) noteRaw else ""
            val plain = if (notesJson.isEmpty()) noteRaw else NoteSerializer.toPlainSummary(NoteSerializer.decodeCards(notesJson))
            val ts = DateUtilsRu.parseIsoToMillis(normDate).takeIf { it > 0L }?.plus(idx) ?: (System.currentTimeMillis() + idx)
            out.add(
                StudySession(
                    id = UUID.randomUUID().toString(),
                    date = normDate,
                    timestamp = ts,
                    subject = sub,
                    chapter = chap,
                    lecture = lec,
                    durationMinutes = mins,
                    mode = mode,
                    status = status,
                    notesJson = notesJson,
                    plainNotes = plain
                )
            )
        }
        return out
    }

    fun parseSyllabusRows(rows: List<List<String>>): List<Triple<String, String, String>> {
        if (rows.isEmpty()) return emptyList()
        val firstRow = rows.first().map { it.lowercase(Locale.ROOT) }
        val hasHeader = firstRow.any { it.contains("subject") || it.contains("chapter") || it.contains("lecture") }
        val dataRows = if (hasHeader) rows.drop(1) else rows
        val subIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("subject") || it == "sub" }.takeIf { it >= 0 } ?: 0 else 0
        val chapIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("chapter") || it == "chap" }.takeIf { it >= 0 } ?: 1 else 1
        val lecIdx = if (hasHeader) firstRow.indexOfFirst { it.contains("lecture") || it == "lec" }.takeIf { it >= 0 } ?: 2 else 2

        return dataRows.mapNotNull { row ->
            val sub = row.getOrNull(subIdx)?.trim().orEmpty()
            val chap = row.getOrNull(chapIdx)?.trim().orEmpty()
            val lec = row.getOrNull(lecIdx)?.trim().orEmpty().ifEmpty { "Lec 1" }
            if (sub.isEmpty() || chap.isEmpty()) null else Triple(sub, chap, lec)
        }
    }

    private fun normalizeDate(raw: String): String {
        if (Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(raw)) return raw
        val slashMatch = Regex("^(\\d{1,2})[/-](\\d{1,2})[/-](\\d{4})$").find(raw)
        if (slashMatch != null) {
            val d = slashMatch.groupValues[1].padStart(2, '0')
            val m = slashMatch.groupValues[2].padStart(2, '0')
            val y = slashMatch.groupValues[3]
            return "$y-$m-$d"
        }
        return DateUtilsRu.todayIso()
    }

    fun sessionDedupKey(s: StudySession): String {
        val note = s.plainNotes.ifBlank { NoteSerializer.toPlainSummary(s.noteCards) }.trim()
        return "${s.date}|${s.subject.trim()}|${s.chapter.trim()}|${s.lecture.trim()}|${s.durationMinutes}|${s.status.key}|$note"
    }

    fun copyToClipboard(context: Context, label: String, text: String) {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    fun shareFile(context: Context, fileName: String, mimeType: String, bytes: ByteArray): Boolean {
        return try {
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outFile = File(dir, fileName)
            outFile.writeBytes(bytes)
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share $fileName").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }

    // --- Self Installable APK Extraction (Zero-Permission via sourceDir + SAF / FileProvider) ---

    data class AppApkInfo(
        val appName: String,
        val versionName: String,
        val packageName: String,
        val sizeBytes: Long,
        val isSplitInstall: Boolean,
        val isAvailable: Boolean,
        val suggestedFileName: String
    ) {
        val formattedSizeMb: String
            get() = String.format(Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0))
    }

    fun getOwnApkInfo(context: Context): AppApkInfo {
        val appInfo = context.applicationInfo
        val srcFile = appInfo.sourceDir?.let { File(it) }
        val pkgInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) {
            null
        }
        val version = pkgInfo?.versionName ?: "1.0"
        val appName = context.packageManager.getApplicationLabel(appInfo)?.toString() ?: "Revision Universe"
        val splitDirs = appInfo.splitSourceDirs
        val isSplit = splitDirs != null && splitDirs.isNotEmpty()
        val available = srcFile != null && srcFile.exists() && srcFile.canRead()
        val sizeBytes = if (available) srcFile!!.length() else 0L
        val safeVer = version.replace(Regex("[^0-9A-Za-z._-]"), "")
        return AppApkInfo(
            appName = appName,
            versionName = version,
            packageName = context.packageName,
            sizeBytes = sizeBytes,
            isSplitInstall = isSplit,
            isAvailable = available,
            suggestedFileName = "RevisionUniverse_v${safeVer}.apk"
        )
    }

    fun copyOwnApkToUri(context: Context, destinationUri: Uri): Boolean {
        return try {
            val srcPath = context.applicationInfo.sourceDir ?: return false
            val srcFile = File(srcPath)
            if (!srcFile.exists() || !srcFile.canRead()) return false
            val outStream = context.contentResolver.openOutputStream(destinationUri) ?: return false
            srcFile.inputStream().use { input ->
                outStream.use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                }
            }
            true
        } catch (_: Exception) {
            false
        }
    }

    fun shareOwnApk(context: Context): Boolean {
        return try {
            val info = getOwnApkInfo(context)
            val srcPath = context.applicationInfo.sourceDir ?: return false
            val srcFile = File(srcPath)
            if (!srcFile.exists() || !srcFile.canRead()) return false

            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            val outFile = File(dir, info.suggestedFileName)
            srcFile.inputStream().use { input ->
                outFile.outputStream().use { output ->
                    input.copyTo(output, bufferSize = 64 * 1024)
                }
            }

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outFile
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.android.package-archive"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "${info.appName} v${info.versionName} APK")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share ${info.suggestedFileName}").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (_: Exception) {
            false
        }
    }
}
