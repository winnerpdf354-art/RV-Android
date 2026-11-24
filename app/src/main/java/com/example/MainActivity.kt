package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.DateUtilsRu
import com.example.data.SyllabusDetailPage
import com.example.data.ToolsPage
import com.example.data.ViewMode
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.IslandToastHost
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AppSidebarContent
import com.example.ui.screens.ChapterDetailScreen
import com.example.ui.screens.ChapterFilterBottomSheet
import com.example.ui.screens.HistoryFilterBottomSheet
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.LectureDetailScreen
import com.example.ui.screens.NotesEditorScreen
import com.example.ui.screens.StudySessionScreen
import com.example.ui.screens.SyllabusRootScreen
import com.example.ui.screens.ToolsPagesHost
import com.example.ui.theme.AppBackgroundLayer
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.RevisionUniverseTheme
import com.example.viewmodel.RevisionViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: RevisionViewModel = viewModel()
            val prefs by vm.preferences.collectAsStateWithLifecycle()

            LaunchedEffect(prefs.isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = if (prefs.isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    },
                    navigationBarStyle = if (prefs.isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                    }
                )
            }

            RevisionUniverseTheme(
                isDarkTheme = prefs.isDarkTheme,
                bgStyle = prefs.bgStyle,
                cardStyle = prefs.cardStyle
            ) {
                RevisionUniverseRoot(vm = vm)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RevisionUniverseRoot(vm: RevisionViewModel) {
    val prefs by vm.preferences.collectAsStateWithLifecycle()
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val filteredHistory by vm.filteredHistory.collectAsStateWithLifecycle()
    val syllabus by vm.syllabus.collectAsStateWithLifecycle()
    val noteLabels by vm.noteLabels.collectAsStateWithLifecycle()
    val savedLinks by vm.savedLinks.collectAsStateWithLifecycle()
    val missingSyllabus by vm.missingDefaultSyllabus.collectAsStateWithLifecycle()

    val activeTab by vm.activeTab.collectAsStateWithLifecycle()
    val syllabusDetailPage by vm.syllabusDetailPage.collectAsStateWithLifecycle()
    val showStudySession by vm.showStudySession.collectAsStateWithLifecycle()
    val showNotesEditor by vm.showNotesEditor.collectAsStateWithLifecycle()
    val toolsPage by vm.toolsPage.collectAsStateWithLifecycle()

    val historyViewMode by vm.historyViewMode.collectAsStateWithLifecycle()
    val syllabusViewMode by vm.syllabusViewMode.collectAsStateWithLifecycle()
    val historySearchOpen by vm.historySearchOpen.collectAsStateWithLifecycle()
    val historySearchQuery by vm.historySearchQuery.collectAsStateWithLifecycle()
    val syllabusSearchOpen by vm.syllabusSearchOpen.collectAsStateWithLifecycle()
    val syllabusSearchQuery by vm.syllabusSearchQuery.collectAsStateWithLifecycle()

    val historyFilter by vm.historyFilter.collectAsStateWithLifecycle()
    val chapterFilter by vm.chapterFilter.collectAsStateWithLifecycle()
    val selectedHistoryIds by vm.selectedHistoryIds.collectAsStateWithLifecycle()

    val habitWeekOffset by vm.habitWeekOffset.collectAsStateWithLifecycle()
    val selectedHabitDate by vm.selectedHabitDate.collectAsStateWithLifecycle()
    val draftSession by vm.draftSession.collectAsStateWithLifecycle()
    val studyError by vm.studyError.collectAsStateWithLifecycle()
    val toast by vm.toast.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = activeTab, pageCount = { 3 })

    var showHistoryFilterSheet by remember { mutableStateOf(false) }
    var showChapterFilterSheet by remember { mutableStateOf(false) }
    var showViewModeMenu by remember { mutableStateOf(false) }
    var confirmDeleteSelectedHistory by remember { mutableStateOf(false) }

    // Sync pager <-> activeTab
    LaunchedEffect(activeTab) {
        if (pagerState.currentPage != activeTab) {
            pagerState.animateScrollToPage(activeTab)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != activeTab) {
            vm.selectTab(pagerState.currentPage)
        }
    }

    val isNestedSyllabus = activeTab == 2 && syllabusDetailPage != SyllabusDetailPage.Root
    val isHistorySelection = activeTab == 1 && selectedHistoryIds.isNotEmpty()
    val isSearchActive = (activeTab == 1 && historySearchOpen) || (activeTab == 2 && !isNestedSyllabus && syllabusSearchOpen)

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }
    BackHandler(enabled = !drawerState.isOpen && isHistorySelection) {
        vm.clearHistorySelection()
    }
    BackHandler(enabled = !drawerState.isOpen && !isHistorySelection && isSearchActive) {
        if (activeTab == 1) vm.setHistorySearchOpen(false)
        else vm.setSyllabusSearchOpen(false)
    }
    BackHandler(enabled = !drawerState.isOpen && isNestedSyllabus) {
        vm.navigateBackFromSyllabusDetail()
    }

    val ru = LocalRuColors.current
    val missingCount = remember(missingSyllabus) {
        missingSyllabus.sumOf { s -> s.chapters.sumOf { it.missingLectures.size } }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = !isNestedSyllabus && !showStudySession && !showNotesEditor && toolsPage == ToolsPage.None,
            drawerContent = {
                AppSidebarContent(
                    preferences = prefs,
                    missingDefaultCount = missingCount,
                    onSetDarkTheme = vm::setDarkTheme,
                    onSetDefaultMode = vm::setDefaultStudyMode,
                    onSetDefaultStatus = vm::setDefaultLearningStatus,
                    onSetDefaultViewMode = vm::setDefaultViewMode,
                    onOpenToolsPage = { page ->
                        scope.launch { drawerState.close() }
                        vm.openToolsPage(page)
                    },
                    onClearAllHistory = {
                        scope.launch { drawerState.close() }
                        vm.clearAllHistory()
                    },
                    onResetAllData = {
                        scope.launch { drawerState.close() }
                        vm.resetAllAppData()
                    }
                )
            }
        ) {
            AppBackgroundLayer {
                Scaffold(
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    topBar = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.78f))
                                .statusBarsPadding()
                        ) {
                            when {
                                isHistorySelection -> {
                                    // Multi-select History Top Bar
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        IconButton(onClick = vm::clearHistorySelection) {
                                            Icon(Icons.Default.Close, contentDescription = "Exit selection")
                                        }
                                        Text(
                                            text = "${selectedHistoryIds.size} Selected",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                if (selectedHistoryIds.size == filteredHistory.size) vm.clearHistorySelection()
                                                else vm.selectAllVisibleHistory()
                                            }
                                        ) {
                                            Icon(Icons.Default.SelectAll, contentDescription = "Select all")
                                        }
                                        IconButton(
                                            onClick = { confirmDeleteSelectedHistory = true },
                                            modifier = Modifier.testTag("history_delete_selected_btn")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete selected", tint = ru.red)
                                        }
                                    }
                                }
                                isSearchActive -> {
                                    val query = if (activeTab == 1) historySearchQuery else syllabusSearchQuery
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = query,
                                            onValueChange = {
                                                if (activeTab == 1) vm.setHistorySearchQuery(it)
                                                else vm.setSyllabusSearchQuery(it)
                                            },
                                            placeholder = {
                                                Text(if (activeTab == 1) "विषय, चैप्टर, नोट या तारीख खोजें..." else "विषय या अध्याय खोजें...")
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("header_search_input")
                                        )
                                        IconButton(
                                            onClick = {
                                                if (activeTab == 1) vm.setHistorySearchOpen(false)
                                                else vm.setSyllabusSearchOpen(false)
                                            }
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Close search")
                                        }
                                    }
                                }
                                else -> {
                                    // Standard Top Bar
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        if (isNestedSyllabus) {
                                            IconButton(
                                                onClick = vm::navigateBackFromSyllabusDetail,
                                                modifier = Modifier.testTag("nested_back_btn")
                                            ) {
                                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                            }
                                        } else {
                                            IconButton(
                                                onClick = { scope.launch { drawerState.open() } },
                                                modifier = Modifier.testTag("drawer_menu_btn")
                                            ) {
                                                Icon(Icons.Default.Menu, contentDescription = "Open menu")
                                            }
                                        }

                                        val headerTitle = when {
                                            isNestedSyllabus -> when (val p = syllabusDetailPage) {
                                                is SyllabusDetailPage.ChapterDetail -> p.chapter
                                                is SyllabusDetailPage.LectureDetail -> p.lecture
                                                SyllabusDetailPage.Root -> "Syllabus"
                                            }
                                            activeTab == 0 -> "Analytics"
                                            activeTab == 1 -> "History"
                                            else -> "Syllabus"
                                        }

                                        Text(
                                            text = headerTitle,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )

                                        // Search Action (History & Syllabus Root)
                                        if (activeTab == 1 || (activeTab == 2 && !isNestedSyllabus)) {
                                            IconButton(
                                                onClick = {
                                                    if (activeTab == 1) vm.setHistorySearchOpen(true)
                                                    else vm.setSyllabusSearchOpen(true)
                                                },
                                                modifier = Modifier.testTag("header_search_btn")
                                            ) {
                                                Icon(Icons.Default.Search, contentDescription = "Search")
                                            }
                                        }

                                        // View Mode Switcher (History & Syllabus Root) — tap cycles, long-press pins default
                                        if (activeTab == 1 || (activeTab == 2 && !isNestedSyllabus)) {
                                            val curView = if (activeTab == 1) historyViewMode else syllabusViewMode
                                            Box {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier
                                                        .size(44.dp)
                                                        .clip(CircleShape)
                                                        .combinedClickable(
                                                            onClick = {
                                                                if (activeTab == 1) vm.cycleHistoryViewMode()
                                                                else vm.cycleSyllabusViewMode()
                                                            },
                                                            onLongClick = { showViewModeMenu = true }
                                                        )
                                                        .testTag("view_mode_switcher_btn")
                                                ) {
                                                    Icon(
                                                        imageVector = viewModeIcon(curView),
                                                        contentDescription = "View mode: ${curView.label}"
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = showViewModeMenu,
                                                    onDismissRequest = { showViewModeMenu = false }
                                                ) {
                                                    ViewMode.entries.forEach { mode ->
                                                        DropdownMenuItem(
                                                            text = {
                                                                Text(
                                                                    text = if (prefs.defaultViewMode == mode) "${mode.label} (Default)" else mode.label,
                                                                    fontWeight = if (curView == mode) FontWeight.Bold else FontWeight.Normal
                                                                )
                                                            },
                                                            leadingIcon = {
                                                                Icon(viewModeIcon(mode), contentDescription = null, modifier = Modifier.size(18.dp))
                                                            },
                                                            trailingIcon = if (curView == mode) {
                                                                { Icon(Icons.Default.Check, contentDescription = null, tint = ru.blue, modifier = Modifier.size(16.dp)) }
                                                            } else null,
                                                            onClick = {
                                                                showViewModeMenu = false
                                                                if (activeTab == 1) vm.setHistoryViewMode(mode, saveAsDefault = true)
                                                                else vm.setSyllabusViewMode(mode, saveAsDefault = true)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Filter Action (History & Chapter Detail)
                                        val showFilterBtn = activeTab == 1 || (activeTab == 2 && syllabusDetailPage is SyllabusDetailPage.ChapterDetail)
                                        if (showFilterBtn) {
                                            val badgeCount = if (activeTab == 1) historyFilter.activeCount else chapterFilter.activeCount
                                            IconButton(
                                                onClick = {
                                                    if (activeTab == 1) showHistoryFilterSheet = true
                                                    else showChapterFilterSheet = true
                                                },
                                                modifier = Modifier.testTag("header_filter_btn")
                                            ) {
                                                BadgedBox(
                                                    badge = {
                                                        if (badgeCount > 0) {
                                                            Badge(containerColor = ru.blue, contentColor = Color.White) {
                                                                Text("$badgeCount")
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(Icons.Default.FilterList, contentDescription = "Filter & Sort")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // 3-Tab Indicator Bar (hidden on nested syllabus detail pages)
                            if (!isNestedSyllabus) {
                                val tabs = listOf("Analytics", "History", "Syllabus")
                                TabRow(
                                    selectedTabIndex = activeTab,
                                    containerColor = Color.Transparent,
                                    contentColor = ru.blue,
                                    indicator = { tabPositions ->
                                        if (activeTab < tabPositions.size) {
                                            TabRowDefaults.SecondaryIndicator(
                                                modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                                                color = ru.blue,
                                                height = 3.dp
                                            )
                                        }
                                    },
                                    divider = { HorizontalDivider(color = ru.cardBorder) }
                                ) {
                                    tabs.forEachIndexed { idx, label ->
                                        Tab(
                                            selected = activeTab == idx,
                                            onClick = { vm.selectTab(idx) },
                                            text = {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = if (activeTab == idx) FontWeight.Bold else FontWeight.Medium
                                                )
                                            },
                                            selectedContentColor = ru.blue,
                                            unselectedContentColor = ru.mutedText,
                                            modifier = Modifier.testTag("tab_${label.lowercase()}")
                                        )
                                    }
                                }
                            } else {
                                HorizontalDivider(color = ru.cardBorder)
                            }
                        }
                    },
                    floatingActionButton = {
                        val showFab = (activeTab == 0 || activeTab == 1) &&
                            !isHistorySelection &&
                            !isSearchActive &&
                            !isNestedSyllabus &&
                            !drawerState.isOpen &&
                            !showStudySession &&
                            toolsPage == ToolsPage.None

                        if (showFab) {
                            StudyMorphingFab(
                                isRunning = draftSession.isRunning,
                                elapsedSeconds = draftSession.elapsedSeconds,
                                onClick = vm::openStudySession
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = innerPadding.calculateTopPadding())
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            userScrollEnabled = !isNestedSyllabus && !isHistorySelection,
                            modifier = Modifier.fillMaxSize()
                        ) { pageIndex ->
                            when (pageIndex) {
                                0 -> AnalyticsScreen(
                                    sessions = sessions,
                                    dailyGoalMinutes = prefs.dailyGoalMinutes,
                                    habitWeekOffset = habitWeekOffset,
                                    selectedHabitDate = selectedHabitDate,
                                    onChangeHabitWeek = vm::changeHabitWeek,
                                    onJumpCurrentWeek = vm::jumpHabitToCurrentWeek,
                                    onToggleHabitDate = vm::toggleHabitDate,
                                    onSaveDailyGoal = vm::setDailyGoalMinutes,
                                    contentBottomPadding = innerPadding
                                )
                                1 -> HistoryScreen(
                                    allSessionsCount = sessions.size,
                                    sessions = filteredHistory,
                                    viewMode = historyViewMode,
                                    filter = historyFilter,
                                    searchQuery = historySearchQuery,
                                    selectedIds = selectedHistoryIds,
                                    onToggleSelect = vm::toggleHistorySelection,
                                    onUpdateFilter = vm::updateHistoryFilter,
                                    onResetFilter = {
                                        vm.resetHistoryFilter()
                                        vm.setHistorySearchOpen(false)
                                    },
                                    onStartStudy = vm::openStudySession,
                                    contentBottomPadding = innerPadding
                                )
                                else -> {
                                    when (val detail = syllabusDetailPage) {
                                        SyllabusDetailPage.Root -> SyllabusRootScreen(
                                            syllabus = syllabus,
                                            sessions = sessions,
                                            viewMode = syllabusViewMode,
                                            searchQuery = syllabusSearchQuery,
                                            onOpenChapterDetail = vm::openChapterDetail,
                                            onOpenLectureDetail = vm::openLectureDetail,
                                            contentBottomPadding = innerPadding
                                        )
                                        is SyllabusDetailPage.ChapterDetail -> {
                                            val allChap = remember(sessions, detail) {
                                                sessions.filter { it.subject == detail.subject && it.chapter == detail.chapter }
                                            }
                                            val filteredChap = remember(allChap, chapterFilter) {
                                                vm.applyChapterFilter(allChap, chapterFilter)
                                            }
                                            ChapterDetailScreen(
                                                subject = detail.subject,
                                                chapter = detail.chapter,
                                                lecturesInSyllabus = syllabus[detail.subject]?.get(detail.chapter).orEmpty(),
                                                allChapterSessions = allChap,
                                                filteredSessions = filteredChap,
                                                filter = chapterFilter,
                                                onBackToRoot = vm::navigateBackFromSyllabusDetail,
                                                onOpenLectureDetail = { lec ->
                                                    vm.openLectureDetail(detail.subject, detail.chapter, lec)
                                                },
                                                contentBottomPadding = innerPadding
                                            )
                                        }
                                        is SyllabusDetailPage.LectureDetail -> {
                                            val lecSess = remember(sessions, detail) {
                                                sessions.filter {
                                                    it.subject == detail.subject &&
                                                        it.chapter == detail.chapter &&
                                                        it.lecture == detail.lecture
                                                }
                                            }
                                            LectureDetailScreen(
                                                subject = detail.subject,
                                                chapter = detail.chapter,
                                                lecture = detail.lecture,
                                                lectureSessions = lecSess,
                                                onBackToChapter = vm::navigateBackFromSyllabusDetail,
                                                contentBottomPadding = innerPadding
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

        // Tools Full-Screen Pages Overlay
        AnimatedVisibility(
            visible = toolsPage != ToolsPage.None,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ToolsPagesHost(
                page = toolsPage,
                syllabus = syllabus,
                sessionsCount = sessions.size,
                preferences = prefs,
                missingSyllabus = missingSyllabus,
                onBack = vm::navigateBackTools,
                onNavigate = vm::openToolsPage,
                onAddSubject = vm::addSubject,
                onAddChapter = vm::addChapter,
                onAddLecturesQuick = vm::addLecturesQuick,
                onAddCustomLecture = vm::addCustomLecture,
                onDeleteSubjects = vm::deleteSubjects,
                onDeleteChapters = vm::deleteChapters,
                onDeleteLectures = vm::deleteLectures,
                onRestoreMissing = vm::restoreMissingSyllabus,
                onImportFiles = vm::importFiles,
                onExportToUri = vm::exportToUri,
                onShareExport = vm::shareExport,
                onCopyTsv = vm::copyTsvToClipboard,
                onSetBgStyle = vm::setBgStyle,
                onSetCardStyle = vm::setCardStyle,
                apkInfoProvider = vm::getOwnApkInfo,
                onExportOwnApkToUri = vm::exportOwnApkToUri,
                onShareOwnApk = vm::shareOwnApk,
                projectExportSummaryProvider = vm::getProjectExportSummary,
                onExportProjectZipAndShare = vm::exportProjectZipAndShare,
                onSaveProjectZipToDownloads = vm::saveProjectZipToDownloads,
                onWriteProjectZipToUri = vm::writeProjectZipToUri
            )
        }

        // Study Session Full-Screen Cockpit Overlay
        AnimatedVisibility(
            visible = showStudySession,
            enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
        ) {
            StudySessionScreen(
                draft = draftSession,
                syllabus = syllabus,
                errorText = studyError,
                onBack = vm::closeStudySession,
                onSelectSubject = vm::selectStudySubject,
                onSelectChapter = vm::selectStudyChapter,
                onSelectLecture = vm::selectStudyLecture,
                onSetMode = vm::setStudyMode,
                onSetStatus = vm::setStudyStatus,
                onSetManualMinutes = vm::setManualMinutes,
                onStartTimer = vm::startTimerFromManual,
                onTogglePauseResume = vm::toggleTimerPauseResume,
                onStopTimer = vm::stopTimerForReview,
                onResumeFromReview = vm::resumeFromReview,
                onResetTimer = vm::resetTimerToManual,
                onSaveSession = vm::saveCurrentSession,
                onOpenNotesEditor = vm::openNotesEditor
            )
        }

        // Full-Screen Notes & Links Editor Overlay
        AnimatedVisibility(
            visible = showNotesEditor,
            enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
        ) {
            NotesEditorScreen(
                notes = draftSession.activeNotes,
                labels = noteLabels,
                savedLinks = savedLinks,
                onUpdateNotes = vm::updateActiveDraftNotes,
                onAddLabel = vm::addNoteLabel,
                onUpdateLabel = vm::updateNoteLabel,
                onMoveLabel = vm::moveNoteLabel,
                onDeleteLabel = vm::deleteNoteLabel,
                onUpsertLink = vm::upsertSavedLink,
                onDeleteLink = vm::deleteSavedLink,
                onShowToast = { vm.showToast(it) },
                onClose = vm::closeNotesEditor
            )
        }

        // Top Dynamic Island Toast Host
        IslandToastHost(
            toast = toast,
            onDismiss = vm::dismissToast,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 10.dp)
        )
    }

    // Bottom Sheets & Dialogs
    if (showHistoryFilterSheet) {
        HistoryFilterBottomSheet(
            initial = historyFilter,
            allSessions = sessions,
            syllabus = syllabus,
            previewCountProvider = { f -> vm.applyHistoryFilter(sessions, f, historySearchQuery).size },
            onApply = { f ->
                vm.updateHistoryFilter(f)
                showHistoryFilterSheet = false
            },
            onDismiss = { showHistoryFilterSheet = false }
        )
    }

    if (showChapterFilterSheet && syllabusDetailPage is SyllabusDetailPage.ChapterDetail) {
        val detail = syllabusDetailPage as SyllabusDetailPage.ChapterDetail
        val allChap = remember(sessions, detail) {
            sessions.filter { it.subject == detail.subject && it.chapter == detail.chapter }
        }
        val lecs = remember(syllabus, allChap, detail) {
            (syllabus[detail.subject]?.get(detail.chapter).orEmpty() + allChap.map { it.lecture }).distinct()
        }
        ChapterFilterBottomSheet(
            initial = chapterFilter,
            chapterSessions = allChap,
            availableLectures = lecs,
            previewCountProvider = { f -> vm.applyChapterFilter(allChap, f).size },
            onApply = { f ->
                vm.updateChapterFilter(f)
                showChapterFilterSheet = false
            },
            onDismiss = { showChapterFilterSheet = false }
        )
    }

    if (confirmDeleteSelectedHistory) {
        ConfirmActionDialog(
            title = "${selectedHistoryIds.size} रिकॉर्ड हटाएँ?",
            message = "चुने गए स्टडी सेशन इतिहास से हट जाएँगे। आप तुरंत 'पूर्ववत' से वापस ला सकते हैं।",
            onConfirm = {
                confirmDeleteSelectedHistory = false
                vm.deleteSelectedHistorySessions()
            },
            onDismiss = { confirmDeleteSelectedHistory = false }
        )
    }
}

@Composable
private fun StudyMorphingFab(
    isRunning: Boolean,
    elapsedSeconds: Long,
    onClick: () -> Unit
) {
    val ru = LocalRuColors.current
    val isActiveTimer = isRunning || elapsedSeconds > 0L

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(99.dp),
        color = if (isActiveTimer) {
            if (isRunning) ru.emerald else ru.amber
        } else {
            ru.blue
        },
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("study_fab")
    ) {
        if (isActiveTimer) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(9.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
                Spacer(Modifier.width(9.dp))
                Text(
                    text = DateUtilsRu.formatDurationClock(elapsedSeconds),
                    fontFamily = JetBrainsMono,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Study Session",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

private fun viewModeIcon(mode: ViewMode) = when (mode) {
    ViewMode.LIST -> Icons.AutoMirrored.Filled.ViewList
    ViewMode.GRID -> Icons.Default.GridView
    ViewMode.TIMELINE -> Icons.Default.Timeline
    ViewMode.COMPACT -> Icons.Default.ViewHeadline
    ViewMode.TABLE -> Icons.Default.TableRows
}
