package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AppPreferences
import com.example.data.BgStyle
import com.example.data.CardStyle
import com.example.data.DateUtilsRu
import com.example.data.ImportExportManager
import com.example.data.LearningStatus
import com.example.data.MissingSyllabusSubject
import com.example.data.ProjectExportManager
import com.example.data.StudyMode
import com.example.data.ToolsPage
import com.example.data.ViewMode
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.theme.AppBackgroundLayer
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.RuCard
import com.example.ui.theme.buildRuExtendedColors
import com.example.ui.theme.subjectColor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppSidebarContent(
    preferences: AppPreferences,
    missingDefaultCount: Int,
    onSetDarkTheme: (Boolean) -> Unit,
    onSetDefaultMode: (StudyMode) -> Unit,
    onSetDefaultStatus: (LearningStatus) -> Unit,
    onSetDefaultViewMode: (ViewMode) -> Unit,
    onOpenToolsPage: (ToolsPage) -> Unit,
    onClearAllHistory: () -> Unit,
    onResetAllData: () -> Unit
) {
    val ru = LocalRuColors.current
    var confirmClearHistory by remember { mutableStateOf(false) }
    var confirmResetAll by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.width(310.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Brand Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ru.blue.copy(alpha = 0.18f))
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = ru.blue)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("Revision Universe", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("100% Offline Study Suite", style = MaterialTheme.typography.labelSmall, color = ru.emerald)
                }
            }

            HorizontalDivider(color = ru.cardBorder)

            // Quick Controls: Theme
            SidebarControlLabel("थीम (Theme)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SidebarToggleChip(
                    label = "Dark",
                    icon = Icons.Default.DarkMode,
                    selected = preferences.isDarkTheme,
                    onClick = { onSetDarkTheme(true) },
                    modifier = Modifier.weight(1f)
                )
                SidebarToggleChip(
                    label = "Light",
                    icon = Icons.Default.LightMode,
                    selected = !preferences.isDarkTheme,
                    onClick = { onSetDarkTheme(false) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Default Study Mode
            SidebarControlLabel("डिफ़ॉल्ट स्टडी मोड")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                SidebarToggleChip(
                    label = "Revision",
                    icon = null,
                    selected = preferences.defaultMode == StudyMode.REVISION,
                    onClick = { onSetDefaultMode(StudyMode.REVISION) },
                    modifier = Modifier.weight(1f)
                )
                SidebarToggleChip(
                    label = "New",
                    icon = null,
                    selected = preferences.defaultMode == StudyMode.FIRST_STUDY,
                    onClick = { onSetDefaultMode(StudyMode.FIRST_STUDY) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Default Difficulty
            SidebarControlLabel("डिफ़ॉल्ट Learning Status")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(
                    LearningStatus.EASY to "Clear",
                    LearningStatus.MEDIUM to "Practice",
                    LearningStatus.HARD to "दोहराना"
                ).forEach { (st, lbl) ->
                    SidebarToggleChip(
                        label = lbl,
                        icon = null,
                        selected = preferences.defaultStatus == st,
                        accent = ru.statusColor(st),
                        onClick = { onSetDefaultStatus(st) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Default View Mode
            SidebarControlLabel("डिफ़ॉल्ट व्यू मोड")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ViewMode.entries.forEach { vm ->
                    FilterPill(
                        label = vm.label,
                        selected = preferences.defaultViewMode == vm,
                        onClick = { onSetDefaultViewMode(vm) }
                    )
                }
            }

            HorizontalDivider(color = ru.cardBorder)

            // Tools Section
            SidebarControlLabel("टूल्स (Tools)")
            SidebarActionRow(
                icon = Icons.Default.MenuBook,
                title = "Syllabus Manager",
                subtitle = "विषय, चैप्टर व लेक्चर जोड़ें या हटाएँ",
                badge = if (missingDefaultCount > 0) "$missingDefaultCount" else null,
                onClick = { onOpenToolsPage(ToolsPage.SyllabusSubjects) },
                testTag = "sidebar_syllabus_mgr"
            )
            SidebarActionRow(
                icon = Icons.Default.FileUpload,
                title = "Import (CSV / Excel)",
                subtitle = "History या Syllabus फ़ाइल से जोड़ें",
                onClick = { onOpenToolsPage(ToolsPage.ImportData) },
                testTag = "sidebar_import"
            )
            SidebarActionRow(
                icon = Icons.Default.FileDownload,
                title = "Export & Share",
                subtitle = "Excel, CSV, Share या Copy TSV",
                onClick = { onOpenToolsPage(ToolsPage.ExportData) },
                testTag = "sidebar_export"
            )
            SidebarActionRow(
                icon = Icons.Default.Palette,
                title = "Background & Cards",
                subtitle = "${preferences.bgStyle.labelHi} · ${preferences.cardStyle.labelHi}",
                onClick = { onOpenToolsPage(ToolsPage.Appearance) },
                testTag = "sidebar_appearance"
            )
            SidebarActionRow(
                icon = Icons.Default.Android,
                title = "App APK (इंस्टॉल फ़ाइल)",
                subtitle = "इसी ऐप की APK सेव या शेयर करें",
                tint = ru.emerald,
                onClick = { onOpenToolsPage(ToolsPage.AppApk) },
                testTag = "sidebar_app_apk"
            )
            SidebarActionRow(
                icon = Icons.Default.FolderZip,
                title = "Export",
                subtitle = "app_export.zip (source + index.html + data.json)",
                tint = ru.blue,
                badge = "ZIP",
                onClick = { onOpenToolsPage(ToolsPage.ExportProject) },
                testTag = "sidebar_export_project"
            )

            HorizontalDivider(color = ru.cardBorder)

            // Danger Zone
            SidebarControlLabel("डेटा प्रबंधन (Offline Storage)")
            SidebarActionRow(
                icon = Icons.Default.Delete,
                title = "सारा इतिहास साफ़ करें",
                subtitle = "Undo विकल्प उपलब्ध रहेगा",
                tint = ru.amber,
                onClick = { confirmClearHistory = true },
                testTag = "sidebar_clear_history"
            )
            SidebarActionRow(
                icon = Icons.Default.RestartAlt,
                title = "पूरा ऐप डेटा रीसेट करें",
                subtitle = "डिफ़ॉल्ट सिलेबस बहाल होगा",
                tint = ru.red,
                onClick = { confirmResetAll = true },
                testTag = "sidebar_reset_all"
            )
        }
    }

    if (confirmClearHistory) {
        ConfirmActionDialog(
            title = "सारा इतिहास साफ़ करें?",
            message = "सभी स्टडी सेशन रिकॉर्ड हट जाएँगे। आप तुरंत 'पूर्ववत' दबाकर वापस ला सकते हैं।",
            confirmLabel = "साफ़ करें",
            onConfirm = {
                confirmClearHistory = false
                onClearAllHistory()
            },
            onDismiss = { confirmClearHistory = false }
        )
    }

    if (confirmResetAll) {
        ConfirmActionDialog(
            title = "पूरा डेटा रीसेट करें?",
            message = "सारा इतिहास, कस्टम लेबल, सेव लिंक्स और सेटिंग्स हट जाएँगी और 21 डिफ़ॉल्ट विषय बहाल हो जाएँगे।",
            confirmLabel = "रीसेट करें",
            onConfirm = {
                confirmResetAll = false
                onResetAllData()
            },
            onDismiss = { confirmResetAll = false }
        )
    }
}

@Composable
private fun SidebarControlLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = LocalRuColors.current.mutedText
    )
}

@Composable
private fun SidebarToggleChip(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    accent: Color = LocalRuColors.current.blue,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) accent.copy(alpha = 0.20f) else ru.subtleFill,
        border = BorderStroke(1.dp, if (selected) accent else ru.cardBorder),
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) accent else ru.mutedText,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(5.dp))
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) accent else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SidebarActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color = LocalRuColors.current.blue,
    badge: String? = null,
    onClick: () -> Unit,
    testTag: String
) {
    val ru = LocalRuColors.current
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ru.subtleFill,
        border = BorderStroke(1.dp, ru.cardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.16f))
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ru.mutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (badge != null) {
                Surface(shape = RoundedCornerShape(99.dp), color = ru.amber.copy(alpha = 0.22f)) {
                    Text(
                        text = badge,
                        fontFamily = JetBrainsMono,
                        style = MaterialTheme.typography.labelSmall,
                        color = ru.amber,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Spacer(Modifier.width(4.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ru.mutedText, modifier = Modifier.size(18.dp))
        }
    }
}

// --- Full-Screen Tools Host ---

@Composable
fun ToolsPagesHost(
    page: ToolsPage,
    syllabus: Map<String, Map<String, List<String>>>,
    sessionsCount: Int,
    preferences: AppPreferences,
    missingSyllabus: List<MissingSyllabusSubject>,
    onBack: () -> Unit,
    onNavigate: (ToolsPage) -> Unit,
    onAddSubject: (String, String) -> Unit,
    onAddChapter: (String, String, Int) -> Unit,
    onAddLecturesQuick: (String, String, Int) -> Unit,
    onAddCustomLecture: (String, String, String) -> Unit,
    onDeleteSubjects: (List<String>) -> Unit,
    onDeleteChapters: (String, List<String>) -> Unit,
    onDeleteLectures: (String, String, List<String>) -> Unit,
    onRestoreMissing: (List<Triple<String, String, String>>) -> Unit,
    onImportFiles: (List<Uri>, ImportExportManager.DataScope, (ImportExportManager.ImportResult) -> Unit) -> Unit,
    onExportToUri: (Uri, ImportExportManager.DataScope, Boolean) -> Unit,
    onShareExport: (ImportExportManager.DataScope, Boolean) -> Unit,
    onCopyTsv: (ImportExportManager.DataScope) -> Unit,
    onSetBgStyle: (BgStyle) -> Unit,
    onSetCardStyle: (CardStyle) -> Unit,
    apkInfoProvider: () -> ImportExportManager.AppApkInfo,
    onExportOwnApkToUri: (Uri, (Boolean) -> Unit) -> Unit,
    onShareOwnApk: ((Boolean) -> Unit) -> Unit,
    projectExportSummaryProvider: () -> ProjectExportManager.ExportBundleSummary,
    onExportProjectZipAndShare: ((ProjectExportManager.ZipBuildOutcome) -> Unit) -> Unit,
    onSaveProjectZipToDownloads: (() -> Unit, (ProjectExportManager.ZipBuildOutcome) -> Unit) -> Unit,
    onWriteProjectZipToUri: (Uri, (ProjectExportManager.ZipBuildOutcome) -> Unit) -> Unit
) {
    BackHandler { onBack() }

    AppBackgroundLayer {
        when (page) {
            ToolsPage.None -> Unit
            ToolsPage.SyllabusSubjects -> {
                SyllabusSubjectsManagerScreen(
                    syllabus = syllabus,
                    missingCount = missingSyllabus.sumOf { s -> s.chapters.sumOf { it.missingLectures.size } },
                    onBack = onBack,
                    onOpenSubject = { onNavigate(ToolsPage.SyllabusChapters(it)) },
                    onOpenRestore = { onNavigate(ToolsPage.RestoreDeletedSyllabus) },
                    onAddSubject = onAddSubject,
                    onDeleteSubjects = onDeleteSubjects
                )
            }
            is ToolsPage.SyllabusChapters -> {
                SyllabusChaptersManagerScreen(
                    subject = page.subject,
                    chapters = syllabus[page.subject].orEmpty(),
                    onBack = onBack,
                    onOpenChapter = { ch -> onNavigate(ToolsPage.SyllabusLectures(page.subject, ch)) },
                    onQuickAddOneLec = { ch -> onAddLecturesQuick(page.subject, ch, 1) },
                    onAddChapter = { ch, cnt -> onAddChapter(page.subject, ch, cnt) },
                    onDeleteChapters = { chaps -> onDeleteChapters(page.subject, chaps) }
                )
            }
            is ToolsPage.SyllabusLectures -> {
                SyllabusLecturesManagerScreen(
                    subject = page.subject,
                    chapter = page.chapter,
                    lectures = syllabus[page.subject]?.get(page.chapter).orEmpty(),
                    onBack = onBack,
                    onAddQuick = { cnt -> onAddLecturesQuick(page.subject, page.chapter, cnt) },
                    onAddCustom = { name -> onAddCustomLecture(page.subject, page.chapter, name) },
                    onDeleteLectures = { lecs -> onDeleteLectures(page.subject, page.chapter, lecs) }
                )
            }
            ToolsPage.RestoreDeletedSyllabus -> {
                RestoreDeletedSyllabusScreen(
                    missing = missingSyllabus,
                    onBack = onBack,
                    onRestore = onRestoreMissing
                )
            }
            ToolsPage.ImportData -> {
                ImportDataScreen(
                    onBack = onBack,
                    onImportFiles = onImportFiles
                )
            }
            ToolsPage.ExportData -> {
                val totalSyllabusRows = syllabus.values.sumOf { ch -> ch.values.sumOf { it.size } }
                ExportDataScreen(
                    historyCount = sessionsCount,
                    syllabusRowCount = totalSyllabusRows,
                    onBack = onBack,
                    onExportToUri = onExportToUri,
                    onShareExport = onShareExport,
                    onCopyTsv = onCopyTsv
                )
            }
            ToolsPage.Appearance -> {
                AppearanceScreen(
                    preferences = preferences,
                    onBack = onBack,
                    onSetBgStyle = onSetBgStyle,
                    onSetCardStyle = onSetCardStyle
                )
            }
            ToolsPage.AppApk -> {
                AppApkExportScreen(
                    apkInfo = remember { apkInfoProvider() },
                    onBack = onBack,
                    onSaveToUri = onExportOwnApkToUri,
                    onShareApk = onShareOwnApk
                )
            }
            ToolsPage.ExportProject -> {
                ExportProjectScreen(
                    summary = remember(sessionsCount, syllabus) { projectExportSummaryProvider() },
                    onBack = onBack,
                    onExportZipAndShare = onExportProjectZipAndShare,
                    onSaveToDownloads = onSaveProjectZipToDownloads,
                    onWriteZipToUri = onWriteProjectZipToUri
                )
            }
        }
    }
}

@Composable
private fun ToolsTopBar(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    val ru = LocalRuColors.current
    Column(modifier = Modifier.statusBarsPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("tools_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (!subtitle.isNullOrBlank()) {
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = ru.blue, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            actions()
        }
        HorizontalDivider(color = ru.cardBorder)
    }
}

private fun fillIndexGaps(allItems: List<String>, selected: Set<String>): Set<String> {
    val indices = selected.mapNotNull { name -> allItems.indexOf(name).takeIf { it >= 0 } }
    if (indices.size < 2) return selected
    val minI = indices.min()
    val maxI = indices.max()
    return allItems.subList(minI, maxI + 1).toSet()
}

@Composable
private fun SyllabusSubjectsManagerScreen(
    syllabus: Map<String, Map<String, List<String>>>,
    missingCount: Int,
    onBack: () -> Unit,
    onOpenSubject: (String) -> Unit,
    onOpenRestore: () -> Unit,
    onAddSubject: (String, String) -> Unit,
    onDeleteSubjects: (List<String>) -> Unit
) {
    val ru = LocalRuColors.current
    var search by remember { mutableStateOf("") }
    var deleteMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val subjects = remember(syllabus, search) {
        val q = search.trim().lowercase()
        syllabus.keys.filter { q.isEmpty() || it.lowercase().contains(q) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            ToolsTopBar(
                title = if (deleteMode) "${selected.size} चुने गए" else "Syllabus Manager",
                subtitle = "${syllabus.size} विषय",
                onBack = {
                    if (deleteMode) {
                        deleteMode = false; selected = emptySet()
                    } else onBack()
                },
                actions = {
                    if (missingCount > 0 && !deleteMode) {
                        TextButton(onClick = onOpenRestore) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("बहाल ($missingCount)")
                        }
                    }
                    if (deleteMode) {
                        if (selected.size >= 2) {
                            TextButton(onClick = { selected = fillIndexGaps(subjects, selected) }) {
                                Text("Range")
                            }
                        }
                        IconButton(
                            onClick = { if (selected.isNotEmpty()) confirmDelete = true },
                            enabled = selected.isNotEmpty()
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete selected", tint = ru.red)
                        }
                    } else {
                        IconButton(onClick = { deleteMode = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete mode")
                        }
                    }
                }
            )

            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                placeholder = { Text("विषय खोजें...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            )

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(subjects, key = { it }) { sub ->
                    val chCount = syllabus[sub]?.size ?: 0
                    val sCol = subjectColor(sub)
                    val isSel = sub in selected
                    RuCard(
                        borderColor = if (isSel) ru.red else ru.cardBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                if (deleteMode) {
                                    selected = if (isSel) selected - sub else selected + sub
                                } else {
                                    onOpenSubject(sub)
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp)
                        ) {
                            if (deleteMode) {
                                Icon(
                                    imageVector = if (isSel) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (isSel) ru.red else ru.mutedText
                                )
                                Spacer(Modifier.width(10.dp))
                            }
                            Box(Modifier.width(4.dp).height(26.dp).clip(RoundedCornerShape(2.dp)).background(sCol))
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(sub, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("$chCount अध्याय", style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                            }
                            if (!deleteMode) {
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ru.mutedText)
                            }
                        }
                    }
                }
            }
        }

        if (!deleteMode) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("नया विषय जोड़ें") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(18.dp)
                    .testTag("add_subject_fab")
            )
        }
    }

    if (showAddDialog) {
        var subName by remember { mutableStateOf("") }
        var firstChap by remember { mutableStateOf("Chapter 1") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("नया विषय जोड़ें") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = subName,
                        onValueChange = { subName = it },
                        label = { Text("विषय का नाम") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = firstChap,
                        onValueChange = { firstChap = it },
                        label = { Text("पहला चैप्टर") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (subName.isNotBlank()) {
                        onAddSubject(subName, firstChap)
                        showAddDialog = false
                    }
                }) { Text("जोड़ें") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("रद्द करें") } }
        )
    }

    if (confirmDelete) {
        ConfirmActionDialog(
            title = "${selected.size} विषय हटाएँ?",
            message = "चुने हुए विषय और उनके सभी चैप्टर/लेक्चर सिलेबस से हट जाएँगे (Undo उपलब्ध रहेगा)।",
            onConfirm = {
                val list = selected.toList()
                confirmDelete = false
                deleteMode = false
                selected = emptySet()
                onDeleteSubjects(list)
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun SyllabusChaptersManagerScreen(
    subject: String,
    chapters: Map<String, List<String>>,
    onBack: () -> Unit,
    onOpenChapter: (String) -> Unit,
    onQuickAddOneLec: (String) -> Unit,
    onAddChapter: (String, Int) -> Unit,
    onDeleteChapters: (List<String>) -> Unit
) {
    val ru = LocalRuColors.current
    var deleteMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    val chapKeys = remember(chapters) { chapters.keys.toList() }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
            ToolsTopBar(
                title = if (deleteMode) "${selected.size} चुने गए" else subject,
                subtitle = "${chapKeys.size} अध्याय",
                onBack = {
                    if (deleteMode) {
                        deleteMode = false; selected = emptySet()
                    } else onBack()
                },
                actions = {
                    if (deleteMode) {
                        if (selected.size >= 2) {
                            TextButton(onClick = { selected = fillIndexGaps(chapKeys, selected) }) { Text("Range") }
                        }
                        IconButton(
                            onClick = { if (selected.isNotEmpty()) confirmDelete = true },
                            enabled = selected.isNotEmpty()
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete chapters", tint = ru.red)
                        }
                    } else {
                        IconButton(onClick = { deleteMode = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete mode")
                        }
                    }
                }
            )

            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 92.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(chapKeys, key = { _, ch -> ch }) { idx, ch ->
                    val lecs = chapters[ch].orEmpty()
                    val isSel = ch in selected
                    RuCard(
                        borderColor = if (isSel) ru.red else ru.cardBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (deleteMode) {
                                    selected = if (isSel) selected - ch else selected + ch
                                } else {
                                    onOpenChapter(ch)
                                }
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                        ) {
                            if (deleteMode) {
                                Icon(
                                    imageVector = if (isSel) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                    contentDescription = null,
                                    tint = if (isSel) ru.red else ru.mutedText
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(
                                text = "${idx + 1}.",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelMedium,
                                color = ru.mutedText,
                                modifier = Modifier.width(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(ch, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text("${lecs.size} लेक्चर", style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                            }
                            if (!deleteMode) {
                                Surface(
                                    shape = RoundedCornerShape(99.dp),
                                    color = ru.emerald.copy(alpha = 0.16f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(99.dp))
                                        .clickable { onQuickAddOneLec(ch) }
                                ) {
                                    Text(
                                        text = "+1 Lec",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ru.emerald,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = ru.mutedText, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        if (!deleteMode) {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("नया चैप्टर जोड़ें") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(18.dp)
            )
        }
    }

    if (showAddDialog) {
        var chapName by remember { mutableStateOf("") }
        var lecCountStr by remember { mutableStateOf("5") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("नया चैप्टर जोड़ें") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = chapName,
                        onValueChange = { chapName = it },
                        label = { Text("चैप्टर का नाम") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lecCountStr,
                        onValueChange = { lecCountStr = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("शुरुआती लेक्चर संख्या") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (chapName.isNotBlank()) {
                        onAddChapter(chapName, lecCountStr.toIntOrNull()?.coerceIn(1, 200) ?: 1)
                        showAddDialog = false
                    }
                }) { Text("जोड़ें") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("रद्द करें") } }
        )
    }

    if (confirmDelete) {
        ConfirmActionDialog(
            title = "${selected.size} चैप्टर हटाएँ?",
            message = "चुने गए चैप्टर इस विषय से हट जाएँगे।",
            onConfirm = {
                val list = selected.toList()
                confirmDelete = false
                deleteMode = false
                selected = emptySet()
                onDeleteChapters(list)
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun SyllabusLecturesManagerScreen(
    subject: String,
    chapter: String,
    lectures: List<String>,
    onBack: () -> Unit,
    onAddQuick: (Int) -> Unit,
    onAddCustom: (String) -> Unit,
    onDeleteLectures: (List<String>) -> Unit
) {
    val ru = LocalRuColors.current
    var deleteMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showCustomDialog by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ToolsTopBar(
            title = if (deleteMode) "${selected.size} चुने गए" else chapter,
            subtitle = "$subject · ${lectures.size} लेक्चर",
            onBack = {
                if (deleteMode) {
                    deleteMode = false; selected = emptySet()
                } else onBack()
            },
            actions = {
                if (deleteMode) {
                    if (selected.size >= 2) {
                        TextButton(onClick = { selected = fillIndexGaps(lectures, selected) }) { Text("Range") }
                    }
                    IconButton(
                        onClick = { if (selected.isNotEmpty()) confirmDelete = true },
                        enabled = selected.isNotEmpty()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = "Delete lectures", tint = ru.red)
                    }
                } else {
                    IconButton(onClick = { deleteMode = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete mode")
                    }
                }
            }
        )

        // Quick Add Row (+1, +3, +5, +10, + Custom)
        if (!deleteMode) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                listOf(1, 3, 5, 10).forEach { cnt ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ru.emerald.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, ru.emerald.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAddQuick(cnt) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 9.dp)) {
                            Text("+$cnt", fontFamily = JetBrainsMono, style = MaterialTheme.typography.labelLarge, color = ru.emerald)
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ru.blue.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, ru.blue.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1.3f)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showCustomDialog = true }
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 9.dp)) {
                        Text("+ Custom", style = MaterialTheme.typography.labelMedium, color = ru.blue)
                    }
                }
            }
        }

        // 3-Column Lecture Grid
        val rows = remember(lectures) { lectures.chunked(3) }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(rows) { triple ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    triple.forEach { lec ->
                        val isSel = lec in selected
                        RuCard(
                            shape = RoundedCornerShape(12.dp),
                            borderColor = if (isSel) ru.red else ru.cardBorder,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable(enabled = deleteMode) {
                                    selected = if (isSel) selected - lec else selected + lec
                                }
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp)
                            ) {
                                if (deleteMode) {
                                    Icon(
                                        imageVector = if (isSel) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = null,
                                        tint = if (isSel) ru.red else ru.mutedText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(lec, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    repeat(3 - triple.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }

    if (showCustomDialog) {
        var customName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("कस्टम लेक्चर जोड़ें") },
            text = {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("लेक्चर का नाम (जैसे Revision Special)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    if (customName.isNotBlank()) {
                        onAddCustom(customName)
                        showCustomDialog = false
                    }
                }) { Text("जोड़ें") }
            },
            dismissButton = { TextButton(onClick = { showCustomDialog = false }) { Text("रद्द करें") } }
        )
    }

    if (confirmDelete) {
        ConfirmActionDialog(
            title = "${selected.size} लेक्चर हटाएँ?",
            message = "चुने गए लेक्चर इस चैप्टर से हट जाएँगे।",
            onConfirm = {
                val list = selected.toList()
                confirmDelete = false
                deleteMode = false
                selected = emptySet()
                onDeleteLectures(list)
            },
            onDismiss = { confirmDelete = false }
        )
    }
}

@Composable
private fun RestoreDeletedSyllabusScreen(
    missing: List<MissingSyllabusSubject>,
    onBack: () -> Unit,
    onRestore: (List<Triple<String, String, String>>) -> Unit
) {
    val ru = LocalRuColors.current
    val allTriples = remember(missing) {
        missing.flatMap { s -> s.chapters.flatMap { c -> c.missingLectures.map { l -> Triple(s.subject, c.chapter, l) } } }
    }
    var selected by remember(allTriples) { mutableStateOf(allTriples.toSet()) }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ToolsTopBar(
            title = "हटाया गया सिलेबस बहाल करें",
            subtitle = "${allTriples.size} डिफ़ॉल्ट लेक्चर उपलब्ध",
            onBack = onBack
        )

        if (missing.isEmpty()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f).fillMaxWidth()) {
                Text("सारा डिफ़ॉल्ट सिलेबस पहले से मौजूद है।", color = ru.mutedText)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(missing, key = { it.subject }) { subItem ->
                    val subTriples = subItem.chapters.flatMap { c -> c.missingLectures.map { Triple(subItem.subject, c.chapter, it) } }
                    val allSubSelected = subTriples.all { it in selected }
                    RuCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selected = if (allSubSelected) selected - subTriples.toSet() else selected + subTriples
                                    }
                            ) {
                                Checkbox(
                                    checked = allSubSelected,
                                    onCheckedChange = {
                                        selected = if (it) selected + subTriples else selected - subTriples.toSet()
                                    }
                                )
                                Text(
                                    text = subItem.subject,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = subjectColor(subItem.subject),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            subItem.chapters.forEach { chap ->
                                val chapTriples = chap.missingLectures.map { Triple(subItem.subject, chap.chapter, it) }
                                val chapChecked = chapTriples.all { it in selected }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 24.dp)
                                        .clickable {
                                            selected = if (chapChecked) selected - chapTriples.toSet() else selected + chapTriples
                                        }
                                ) {
                                    Checkbox(
                                        checked = chapChecked,
                                        onCheckedChange = {
                                            selected = if (it) selected + chapTriples else selected - chapTriples.toSet()
                                        }
                                    )
                                    Column {
                                        Text(chap.chapter, style = MaterialTheme.typography.bodyMedium)
                                        Text("${chap.missingLectures.size} लेक्चर", style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Surface(color = MaterialTheme.colorScheme.surface, border = BorderStroke(1.dp, ru.cardBorder)) {
                Button(
                    onClick = {
                        onRestore(selected.toList())
                        onBack()
                    },
                    enabled = selected.isNotEmpty(),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp)
                ) {
                    Text("चुने हुए बहाल करें (${selected.size})")
                }
            }
        }
    }
}

@Composable
private fun ImportDataScreen(
    onBack: () -> Unit,
    onImportFiles: (List<Uri>, ImportExportManager.DataScope, (ImportExportManager.ImportResult) -> Unit) -> Unit
) {
    val ru = LocalRuColors.current
    var scope by remember { mutableStateOf(ImportExportManager.DataScope.HISTORY) }
    var lastReport by remember { mutableStateOf<ImportExportManager.ImportResult?>(null) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportFiles(uris, scope) { report -> lastReport = report }
        }
    }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ToolsTopBar(
            title = "Import Data",
            subtitle = "CSV व Excel (.xlsx) ऑफ़लाइन आयात",
            onBack = onBack
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                ImportExportManager.DataScope.entries.forEach { s ->
                    SidebarToggleChip(
                        label = s.label,
                        icon = null,
                        selected = scope == s,
                        onClick = { scope = s; lastReport = null },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            RuCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = if (scope == ImportExportManager.DataScope.HISTORY) {
                            "History CSV / XLSX फ़ाइल चुनें"
                        } else {
                            "Syllabus CSV / XLSX फ़ाइल चुनें"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (scope == ImportExportManager.DataScope.HISTORY) {
                            "कॉलम: Date, Subject, Chapter, Lecture, Time(min), Mode, Difficulty, Notes — डुप्लिकेट रिकॉर्ड अपने आप छूट जाएँगे।"
                        } else {
                            "कॉलम: Subject, Chapter, Lecture — पहले से मौजूद लेक्चर अपने आप छूट जाएँगे।"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ru.mutedText
                    )
                    Button(
                        onClick = {
                            picker.launch(
                                arrayOf(
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                    "application/octet-stream",
                                    "*/*"
                                )
                            )
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("फ़ाइलें चुनें (.csv / .xlsx)")
                    }
                }
            }

            lastReport?.let { rep ->
                RuCard(
                    borderColor = if (rep.errorMessage != null) ru.red else ru.emerald,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = if (rep.errorMessage != null) "त्रुटि" else "आयात सारांश (Summary)",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (rep.errorMessage != null) ru.red else ru.emerald
                        )
                        if (rep.errorMessage != null) {
                            Text(rep.errorMessage, style = MaterialTheme.typography.bodySmall)
                        } else {
                            Text("नए जोड़े गए: ${rep.addedCount}", style = MaterialTheme.typography.bodyMedium)
                            Text("डुप्लिकेट छोड़े गए: ${rep.skippedDuplicates}", style = MaterialTheme.typography.bodySmall, color = ru.mutedText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportDataScreen(
    historyCount: Int,
    syllabusRowCount: Int,
    onBack: () -> Unit,
    onExportToUri: (Uri, ImportExportManager.DataScope, Boolean) -> Unit,
    onShareExport: (ImportExportManager.DataScope, Boolean) -> Unit,
    onCopyTsv: (ImportExportManager.DataScope) -> Unit
) {
    val ru = LocalRuColors.current
    var scope by remember { mutableStateOf(ImportExportManager.DataScope.HISTORY) }

    val createXlsxLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    ) { uri ->
        if (uri != null) onExportToUri(uri, scope, true)
    }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) onExportToUri(uri, scope, false)
    }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ToolsTopBar(
            title = "Export & Share",
            subtitle = "100% ऑफ़लाइन Excel, CSV और TSV",
            onBack = onBack
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SidebarToggleChip(
                    label = "History ($historyCount)",
                    icon = null,
                    selected = scope == ImportExportManager.DataScope.HISTORY,
                    onClick = { scope = ImportExportManager.DataScope.HISTORY },
                    modifier = Modifier.weight(1f)
                )
                SidebarToggleChip(
                    label = "Syllabus ($syllabusRowCount)",
                    icon = null,
                    selected = scope == ImportExportManager.DataScope.SYLLABUS,
                    onClick = { scope = ImportExportManager.DataScope.SYLLABUS },
                    modifier = Modifier.weight(1f)
                )
            }

            val baseName = "revision_universe_${scope.name.lowercase()}_${DateUtilsRu.todayIso()}"

            RuCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
                    Text("फ़ाइल सेव करें (Save to Device)", style = MaterialTheme.typography.titleSmall)
                    Button(
                        onClick = { createXlsxLauncher.launch("$baseName.xlsx") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Excel (.xlsx) सेव करें")
                    }
                    OutlinedButton(
                        onClick = { createCsvLauncher.launch("$baseName.csv") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("CSV (.csv UTF-8 BOM) सेव करें")
                    }
                }
            }

            RuCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(16.dp)) {
                    Text("शेयर और कॉपी (Quick Share)", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { onShareExport(scope, true) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share .xlsx")
                        }
                        OutlinedButton(
                            onClick = { onShareExport(scope, false) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).height(46.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Share .csv")
                        }
                    }
                    OutlinedButton(
                        onClick = { onCopyTsv(scope) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(46.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Copy TSV to Clipboard")
                    }
                }
            }
        }
    }
}

@Composable
private fun AppearanceScreen(
    preferences: AppPreferences,
    onBack: () -> Unit,
    onSetBgStyle: (BgStyle) -> Unit,
    onSetCardStyle: (CardStyle) -> Unit
) {
    val ru = LocalRuColors.current

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ToolsTopBar(
            title = "Background & Cards",
            subtitle = "लाइव प्रीव्यू के साथ तुरंत बदलें",
            onBack = onBack
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text("बैकग्राउंड स्टाइल (5 विकल्प)", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BgStyle.entries.forEach { style ->
                    val active = preferences.bgStyle == style
                    RuCard(
                        borderColor = if (active) ru.blue else ru.cardBorder,
                        borderWidth = if (active) 1.5.dp else 1.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSetBgStyle(style) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, ru.cardBorder, RoundedCornerShape(12.dp))
                            ) {
                                AppBackgroundLayer(bgStyle = style, isDark = preferences.isDarkTheme) {}
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${style.id}. ${style.labelHi}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(style.descHi, style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                            }
                            if (active) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = ru.blue)
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = ru.cardBorder)

            Text("कार्ड सतह स्टाइल (4 विकल्प)", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CardStyle.entries.forEach { cStyle ->
                    val active = preferences.cardStyle == cStyle
                    val previewColors = buildRuExtendedColors(preferences.isDarkTheme, preferences.bgStyle, cStyle)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = previewColors.cardFill,
                        border = BorderStroke(if (active) 1.5.dp else 1.dp, if (active) ru.blue else previewColors.cardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSetCardStyle(cStyle) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${cStyle.id}. ${cStyle.labelHi}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(cStyle.descHi, style = MaterialTheme.typography.labelSmall, color = ru.mutedText)
                            }
                            if (active) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = ru.blue)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppApkExportScreen(
    apkInfo: ImportExportManager.AppApkInfo,
    onBack: () -> Unit,
    onSaveToUri: (Uri, (Boolean) -> Unit) -> Unit,
    onShareApk: ((Boolean) -> Unit) -> Unit
) {
    val ru = LocalRuColors.current
    var isWorking by remember { mutableStateOf(false) }

    val saveApkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")
    ) { uri ->
        if (uri != null) {
            isWorking = true
            onSaveToUri(uri) { isWorking = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().navigationBarsPadding()) {
        ToolsTopBar(
            title = "App APK (इंस्टॉल फ़ाइल)",
            subtitle = "इसी ऐप की साइंड .apk कॉपी निकालें",
            onBack = onBack
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            RuCard(
                borderColor = ru.emerald.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ru.emerald.copy(alpha = 0.16f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Android,
                                contentDescription = null,
                                tint = ru.emerald,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = apkInfo.appName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "v${apkInfo.versionName} · ${apkInfo.formattedSizeMb}",
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelMedium,
                                color = ru.emerald
                            )
                        }
                        if (apkInfo.isAvailable) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Installable APK ready",
                                tint = ru.emerald
                            )
                        }
                    }

                    HorizontalDivider(color = ru.cardBorder)

                    Text(
                        text = "फ़ाइल नाम: ${apkInfo.suggestedFileName}",
                        fontFamily = JetBrainsMono,
                        style = MaterialTheme.typography.labelSmall,
                        color = ru.mutedText
                    )
                    Text(
                        text = "पैकेज आईडी: ${apkInfo.packageName}",
                        fontFamily = JetBrainsMono,
                        style = MaterialTheme.typography.labelSmall,
                        color = ru.mutedText
                    )
                    Text(
                        text = if (apkInfo.isSplitInstall) {
                            "नोट: यह डिवाइस Split APK के रूप में इंस्टॉल है — बेस APK कॉपी की जाएगी।"
                        } else {
                            "यह पूरी सिंगल साइंड APK फ़ाइल है जिसे किसी भी Android फ़ोन में सीधे सेव या शेयर करके इंस्टॉल किया जा सकता है।"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ru.mutedText
                    )
                }
            }

            RuCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("APK प्राप्त करें (100% ऑफ़लाइन)", style = MaterialTheme.typography.titleSmall)

                    Button(
                        onClick = { saveApkLauncher.launch(apkInfo.suggestedFileName) },
                        enabled = apkInfo.isAvailable && !isWorking,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ru.emerald),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_own_apk_btn")
                    ) {
                        if (isWorking) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("तैयार हो रही है...", color = Color.White)
                        } else {
                            Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("डिवाइस में APK सेव करें (.apk)", color = Color.White)
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isWorking = true
                            onShareApk { isWorking = false }
                        },
                        enabled = apkInfo.isAvailable && !isWorking,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("share_own_apk_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("APK शेयर करें (WhatsApp / Drive / Files)")
                    }
                }
            }
        }
    }
}

// --- Full Project Export Screen ("Export") ---

@Composable
private fun ExportProjectScreen(
    summary: ProjectExportManager.ExportBundleSummary,
    onBack: () -> Unit,
    onExportZipAndShare: ((ProjectExportManager.ZipBuildOutcome) -> Unit) -> Unit,
    onSaveToDownloads: (() -> Unit, (ProjectExportManager.ZipBuildOutcome) -> Unit) -> Unit,
    onWriteZipToUri: (Uri, (ProjectExportManager.ZipBuildOutcome) -> Unit) -> Unit
) {
    val ru = LocalRuColors.current
    var isBuilding by remember { mutableStateOf(false) }
    var activeAction by remember { mutableStateOf("") }
    var lastOutcome by remember { mutableStateOf<ProjectExportManager.ZipBuildOutcome?>(null) }

    val saveZipSafLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            isBuilding = true
            activeAction = "Downloads में सेव हो रहा है..."
            onWriteZipToUri(uri) { outcome ->
                isBuilding = false
                activeAction = ""
                lastOutcome = outcome
            }
        } else {
            isBuilding = false
            activeAction = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
    ) {
        ToolsTopBar(
            title = "Export",
            subtitle = ProjectExportManager.EXPORT_ZIP_NAME,
            onBack = onBack
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Hero Header Card
            RuCard(
                borderColor = ru.blue.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(ru.blue.copy(alpha = 0.16f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderZip,
                                contentDescription = "Export ZIP bundle",
                                tint = ru.blue,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Complete Project Bundle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = ProjectExportManager.EXPORT_ZIP_NAME,
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelMedium,
                                color = ru.blue
                            )
                        }
                    }

                    Text(
                        text = "एक ही ZIP फ़ाइल में इस ऐप का पूरा असली Android सोर्स कोड, ऑफ़लाइन चलने वाला सिंगल-फ़ाइल HTML वर्ज़न और आपका पूरा Room डेटाबेस पैक होगा।",
                        style = MaterialTheme.typography.bodySmall,
                        color = ru.mutedText
                    )

                    HorizontalDivider(color = ru.cardBorder)

                    ExportContentItemRow(
                        icon = Icons.Default.Code,
                        tint = ru.blue,
                        title = "source/",
                        detail = "${summary.sourceFileCount} असली प्रोजेक्ट फ़ाइलें (.kt, build.gradle.kts, AndroidManifest.xml, res/*.xml)"
                    )
                    ExportContentItemRow(
                        icon = Icons.Default.Language,
                        tint = ru.emerald,
                        title = "index.html",
                        detail = "पूरा सिंगल-फ़ाइल HTML + CSS + JS वेब ऐप (${summary.formattedWebSizeKb}, localStorage)"
                    )
                    ExportContentItemRow(
                        icon = Icons.Default.DataObject,
                        tint = ru.amber,
                        title = "data.json",
                        detail = "${summary.sessionsCount} सेशन · ${summary.syllabusEntriesCount} सिलेबस · ${summary.labelsCount} लेबल · ${summary.linksCount} लिंक"
                    )
                }
            }

            // Primary Action Card: Big "Export ZIP" + "Save to Downloads"
            RuCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Button(
                        onClick = {
                            isBuilding = true
                            activeAction = "app_export.zip तैयार हो रही है..."
                            onExportZipAndShare { outcome ->
                                isBuilding = false
                                activeAction = ""
                                lastOutcome = outcome
                            }
                        },
                        enabled = !isBuilding,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ru.blue),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("export_project_zip_btn")
                    ) {
                        if (isBuilding && activeAction.contains("app_export.zip")) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = activeAction,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Export ZIP",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isBuilding = true
                            activeAction = "Downloads में सेव हो रहा है..."
                            onSaveToDownloads(
                                {
                                    saveZipSafLauncher.launch(ProjectExportManager.EXPORT_ZIP_NAME)
                                }
                            ) { outcome ->
                                isBuilding = false
                                activeAction = ""
                                lastOutcome = outcome
                            }
                        },
                        enabled = !isBuilding,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, ru.emerald.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_zip_downloads_btn")
                    ) {
                        if (isBuilding && activeAction.contains("Downloads")) {
                            CircularProgressIndicator(
                                color = ru.emerald,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(activeAction, color = ru.emerald)
                        } else {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = ru.emerald,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Save to Downloads",
                                style = MaterialTheme.typography.titleSmall,
                                color = ru.emerald
                            )
                        }
                    }

                    Text(
                        text = "Export ZIP दबाने पर app_export.zip बनते ही फ़ोन का Share Sheet खुल जाएगा। Save to Downloads सीधे आपके फ़ोन के Downloads फ़ोल्डर में app_export.zip सेव करता है।",
                        style = MaterialTheme.typography.labelSmall,
                        color = ru.mutedText
                    )
                }
            }

            // Last Export Result Feedback Card
            lastOutcome?.let { res ->
                val accent = if (res.success) ru.emerald else ru.red
                RuCard(
                    borderColor = accent.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("export_project_result_card")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(14.dp)
                    ) {
                        Icon(
                            imageVector = if (res.success) Icons.Default.Verified else Icons.Default.Delete,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (res.success) "ZIP सफलतापूर्वक तैयार!" else "Export विफल",
                                style = MaterialTheme.typography.titleSmall,
                                color = accent
                            )
                            Text(
                                text = if (res.success) {
                                    "${res.savedLocationLabel} · ${res.formattedSize} · ${res.totalEntries} फ़ाइलें पैक हुईं"
                                } else {
                                    res.errorMessage ?: "अज्ञात त्रुटि"
                                },
                                fontFamily = JetBrainsMono,
                                style = MaterialTheme.typography.labelSmall,
                                color = ru.mutedText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportContentItemRow(
    icon: ImageVector,
    tint: Color,
    title: String,
    detail: String
) {
    val ru = LocalRuColors.current
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tint.copy(alpha = 0.14f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = JetBrainsMono,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.labelSmall,
                color = ru.mutedText
            )
        }
    }
}
