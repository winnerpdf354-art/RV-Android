package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.DefaultSyllabusData
import com.example.data.ImportExportManager
import com.example.data.LinkCategory
import com.example.data.NoteCard
import com.example.data.NoteLabel
import com.example.data.SavedLink
import com.example.data.extractUrlsFromText
import com.example.data.guessLabelFromUrl
import com.example.ui.components.ConfirmActionDialog
import com.example.ui.components.RichNoteCardsViewer
import com.example.ui.theme.LocalRuColors
import com.example.ui.theme.RuCard
import com.example.ui.theme.parseHexColor
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesEditorScreen(
    notes: List<NoteCard>,
    labels: List<NoteLabel>,
    savedLinks: List<SavedLink>,
    onUpdateNotes: ((List<NoteCard>) -> List<NoteCard>) -> Unit,
    onAddLabel: (String, String) -> Unit,
    onUpdateLabel: (NoteLabel) -> Unit,
    onMoveLabel: (String, Int) -> Unit,
    onDeleteLabel: (String) -> Unit,
    onUpsertLink: (SavedLink) -> Unit,
    onDeleteLink: (String) -> Unit,
    onShowToast: (String) -> Unit,
    onClose: () -> Unit
) {
    val ru = LocalRuColors.current
    val context = LocalContext.current

    var activeBottomTab by remember { mutableIntStateOf(0) } // 0: Notes, 1: Links
    var selectedLabelId by remember { mutableStateOf<String?>(null) } // null = All
    var editingCardId by remember { mutableStateOf<String?>(null) }
    var filterByLinkUrl by remember { mutableStateOf<String?>(null) }

    // Dialog states
    var showAddMenu by remember { mutableStateOf(false) }
    var showPickLabelForNewNote by remember { mutableStateOf(false) }
    var showLabelDialog by remember { mutableStateOf<NoteLabel?>(null) }
    var isCreatingLabel by remember { mutableStateOf(false) }
    var labelMenuTarget by remember { mutableStateOf<NoteLabel?>(null) }
    var deletingNoteTarget by remember { mutableStateOf<NoteCard?>(null) }

    // Links tab states
    var linkCategory by remember { mutableStateOf(LinkCategory.ALL) }
    var linkSearchOpen by remember { mutableStateOf(false) }
    var linkSearchQuery by remember { mutableStateOf("") }
    var editingLinkDialog by remember { mutableStateOf<SavedLink?>(null) }
    var isCreatingLink by remember { mutableStateOf(false) }
    var deletingLinkTarget by remember { mutableStateOf<SavedLink?>(null) }

    // Picker mode when inserting a saved link into an editing note
    var pickSavedLinkCallback by remember { mutableStateOf<((SavedLink) -> Unit)?>(null) }

    BackHandler {
        when {
            pickSavedLinkCallback != null -> pickSavedLinkCallback = null
            editingCardId != null -> editingCardId = null
            filterByLinkUrl != null -> filterByLinkUrl = null
            else -> onClose()
        }
    }

    val editingCard = remember(notes, editingCardId) { notes.firstOrNull { it.id == editingCardId } }

    // Focused Edit Mode Overlay
    if (editingCard != null && pickSavedLinkCallback == null) {
        FocusedNoteEditor(
            card = editingCard,
            savedLinks = savedLinks,
            onPickFromSavedLinks = { cb ->
                pickSavedLinkCallback = cb
                activeBottomTab = 1
            },
            onSave = { updatedText ->
                onUpdateNotes { list ->
                    list.map {
                        if (it.id == editingCard.id) it.copy(content = updatedText, updatedAt = System.currentTimeMillis())
                        else it
                    }
                }
                editingCardId = null
            },
            onCancel = { editingCardId = null }
        )
        return
    }

    val sortedNotes = remember(notes, selectedLabelId, filterByLinkUrl) {
        notes.filter { c ->
            val lblMatch = selectedLabelId == null || c.labelId == selectedLabelId
            val urlMatch = filterByLinkUrl == null || c.content.contains(filterByLinkUrl!!, ignoreCase = true)
            lblMatch && urlMatch
        }.sortedWith(compareByDescending<NoteCard> { it.pinned })
    }

    val allUrlsInNotes = remember(notes) {
        notes.flatMap { extractUrlsFromText(it.content) }.map { it.lowercase() }.toSet()
    }

    fun createNoteWithLabel(lbl: NoteLabel) {
        val newCard = NoteCard(
            id = UUID.randomUUID().toString(),
            labelId = lbl.id,
            labelName = lbl.name,
            colorHex = lbl.colorHex,
            content = ""
        )
        onUpdateNotes { listOf(newCard) + it }
        editingCardId = newCard.id
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Top Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        when {
                            pickSavedLinkCallback != null -> pickSavedLinkCallback = null
                            filterByLinkUrl != null -> filterByLinkUrl = null
                            else -> onClose()
                        }
                    },
                    modifier = Modifier.testTag("notes_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }

                if (activeBottomTab == 1 && linkSearchOpen) {
                    OutlinedTextField(
                        value = linkSearchQuery,
                        onValueChange = { linkSearchQuery = it },
                        placeholder = { Text("लिंक खोजें...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        linkSearchOpen = false
                        linkSearchQuery = ""
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close search")
                    }
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                pickSavedLinkCallback != null -> "लिंक चुनें (Saved Links)"
                                filterByLinkUrl != null -> "लिंक वाले नोट्स"
                                activeBottomTab == 0 -> "Study Notes (${notes.size})"
                                else -> "Saved Links (${savedLinks.size})"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (filterByLinkUrl != null) {
                            Text(
                                text = filterByLinkUrl!!,
                                style = MaterialTheme.typography.labelSmall,
                                color = ru.blue,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (activeBottomTab == 1 && pickSavedLinkCallback == null) {
                        IconButton(onClick = { linkSearchOpen = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search links")
                        }
                    }

                    if (pickSavedLinkCallback == null) {
                        Box {
                            IconButton(
                                onClick = {
                                    if (activeBottomTab == 0) {
                                        showAddMenu = true
                                    } else {
                                        isCreatingLink = true
                                        editingLinkDialog = SavedLink(title = "", url = "")
                                    }
                                },
                                modifier = Modifier.testTag("notes_add_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add")
                            }
                            DropdownMenu(
                                expanded = showAddMenu,
                                onDismissRequest = { showAddMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("नया नोट जोड़ें") },
                                    onClick = {
                                        showAddMenu = false
                                        val activeLbl = labels.firstOrNull { it.id == selectedLabelId }
                                        if (activeLbl != null) {
                                            createNoteWithLabel(activeLbl)
                                        } else {
                                            showPickLabelForNewNote = true
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("नया लेबल बनाएँ") },
                                    onClick = {
                                        showAddMenu = false
                                        isCreatingLabel = true
                                        showLabelDialog = NoteLabel(
                                            name = "",
                                            colorHex = DefaultSyllabusData.labelColorPalette.first()
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = ru.cardBorder)

            // Main Body
            if (activeBottomTab == 0 && pickSavedLinkCallback == null) {
                // Label Chips Strip
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item("lbl_all") {
                        FilterPill(
                            label = "All (${notes.size})",
                            selected = selectedLabelId == null,
                            onClick = { selectedLabelId = null }
                        )
                    }
                    items(labels, key = { it.id }) { lbl ->
                        val active = selectedLabelId == lbl.id
                        val c = parseHexColor(lbl.colorHex, ru.blue)
                        val cnt = notes.count { it.labelId == lbl.id }
                        Box {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (active) c.copy(alpha = 0.22f) else ru.subtleFill,
                                border = BorderStroke(1.dp, if (active) c else ru.cardBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .combinedClickable(
                                        onClick = {
                                            if (active) labelMenuTarget = lbl else selectedLabelId = lbl.id
                                        },
                                        onLongClick = { labelMenuTarget = lbl }
                                    )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                ) {
                                    Box(Modifier.size(8.dp).clip(CircleShape).background(c))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (cnt > 0) "${lbl.name} $cnt" else lbl.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (active) c else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = labelMenuTarget?.id == lbl.id,
                                onDismissRequest = { labelMenuTarget = null }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("बाएँ ले जाएँ (Move Left)") },
                                    onClick = {
                                        labelMenuTarget = null
                                        onMoveLabel(lbl.id, -1)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("दाएँ ले जाएँ (Move Right)") },
                                    onClick = {
                                        labelMenuTarget = null
                                        onMoveLabel(lbl.id, 1)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("नाम / रंग बदलें") },
                                    onClick = {
                                        labelMenuTarget = null
                                        isCreatingLabel = false
                                        showLabelDialog = lbl
                                    }
                                )
                                if (labels.size > 1) {
                                    DropdownMenuItem(
                                        text = { Text("लेबल हटाएँ", color = ru.red) },
                                        onClick = {
                                            labelMenuTarget = null
                                            if (selectedLabelId == lbl.id) selectedLabelId = null
                                            onDeleteLabel(lbl.id)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Notes List
                if (sortedNotes.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = ru.mutedText.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text("कोई नोट नहीं है", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "सूत्र, ट्रिक, गलती या संदेह जोड़ने के लिए नीचे टैप करें।",
                            style = MaterialTheme.typography.bodySmall,
                            color = ru.mutedText
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val targetLbl = labels.firstOrNull { it.id == selectedLabelId }
                                    ?: labels.firstOrNull()
                                    ?: DefaultSyllabusData.defaultNoteLabels.first()
                                createNoteWithLabel(targetLbl)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("notes_empty_add_btn")
                        ) {
                            Text("+ नया नोट लिखें")
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(sortedNotes, key = { it.id }) { card ->
                            var cardMenuOpen by remember { mutableStateOf(false) }
                            val accent = parseHexColor(card.colorHex, ru.blue)
                            RuCard(
                                borderColor = accent.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { editingCardId = card.id }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
                                            Spacer(Modifier.width(7.dp))
                                            Text(card.labelName, style = MaterialTheme.typography.labelLarge, color = accent)
                                            if (card.pinned) {
                                                Spacer(Modifier.width(6.dp))
                                                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = ru.amber, modifier = Modifier.size(14.dp))
                                            }
                                        }
                                        Box {
                                            IconButton(
                                                onClick = { cardMenuOpen = true },
                                                modifier = Modifier.size(34.dp)
                                            ) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Note options", modifier = Modifier.size(18.dp))
                                            }
                                            DropdownMenu(
                                                expanded = cardMenuOpen,
                                                onDismissRequest = { cardMenuOpen = false }
                                            ) {
                                                // Quick label color switcher row
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    labels.take(6).forEach { lbl ->
                                                        val lc = parseHexColor(lbl.colorHex, ru.blue)
                                                        Box(
                                                            modifier = Modifier
                                                                .size(22.dp)
                                                                .clip(CircleShape)
                                                                .background(lc)
                                                                .border(
                                                                    width = if (lbl.id == card.labelId) 2.dp else 0.dp,
                                                                    color = Color.White,
                                                                    shape = CircleShape
                                                                )
                                                                .clickable {
                                                                    cardMenuOpen = false
                                                                    onUpdateNotes { list ->
                                                                        list.map {
                                                                            if (it.id == card.id) it.copy(
                                                                                labelId = lbl.id,
                                                                                labelName = lbl.name,
                                                                                colorHex = lbl.colorHex
                                                                            ) else it
                                                                        }
                                                                    }
                                                                }
                                                        )
                                                    }
                                                }
                                                HorizontalDivider()
                                                DropdownMenuItem(
                                                    text = { Text(if (card.pinned) "Unpin" else "Pin ऊपर रखें") },
                                                    onClick = {
                                                        cardMenuOpen = false
                                                        onUpdateNotes { list ->
                                                            list.map { if (it.id == card.id) it.copy(pinned = !it.pinned) else it }
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("कॉपी करें (Copy)") },
                                                    onClick = {
                                                        cardMenuOpen = false
                                                        ImportExportManager.copyToClipboard(context, card.labelName, card.content)
                                                        onShowToast("नोट कॉपी किया गया")
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("डुप्लिकेट बनाएँ") },
                                                    onClick = {
                                                        cardMenuOpen = false
                                                        val dup = card.copy(id = UUID.randomUUID().toString(), pinned = false)
                                                        onUpdateNotes { list -> list + dup }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("ऊपर ले जाएँ") },
                                                    onClick = {
                                                        cardMenuOpen = false
                                                        onUpdateNotes { list ->
                                                            val m = list.toMutableList()
                                                            val idx = m.indexOfFirst { it.id == card.id }
                                                            if (idx > 0) {
                                                                val item = m.removeAt(idx)
                                                                m.add(idx - 1, item)
                                                            }
                                                            m
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("नीचे ले जाएँ") },
                                                    onClick = {
                                                        cardMenuOpen = false
                                                        onUpdateNotes { list ->
                                                            val m = list.toMutableList()
                                                            val idx = m.indexOfFirst { it.id == card.id }
                                                            if (idx in 0 until m.lastIndex) {
                                                                val item = m.removeAt(idx)
                                                                m.add(idx + 1, item)
                                                            }
                                                            m
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("हटाएँ", color = ru.red) },
                                                    onClick = {
                                                        cardMenuOpen = false
                                                        deletingNoteTarget = card
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(Modifier.height(4.dp))
                                    if (card.content.isBlank()) {
                                        Text("लिखने के लिए टैप करें...", style = MaterialTheme.typography.bodySmall, color = ru.mutedText)
                                    } else {
                                        RichNoteCardsViewer(
                                            cards = listOf(card),
                                            highlightUrl = filterByLinkUrl,
                                            onToggleChecklistLine = { cid, lineIdx ->
                                                onUpdateNotes { list ->
                                                    list.map { c ->
                                                        if (c.id != cid) c else {
                                                            val ls = c.content.lines().toMutableList()
                                                            if (lineIdx in ls.indices) {
                                                                val l = ls[lineIdx]
                                                                ls[lineIdx] = when {
                                                                    l.trimStart().startsWith("[ ] ") -> l.replaceFirst("[ ] ", "[x] ")
                                                                    l.trimStart().startsWith("[x] ", ignoreCase = true) -> l.replaceFirst(Regex("\\[[xX]\\] "), "[ ] ")
                                                                    else -> l
                                                                }
                                                            }
                                                            c.copy(content = ls.joinToString("\n"))
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Links Tab (also acts as Saved Links Picker)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(LinkCategory.entries, key = { it.name }) { cat ->
                        FilterPill(
                            label = cat.label,
                            selected = linkCategory == cat,
                            onClick = { linkCategory = cat }
                        )
                    }
                }

                val filteredLinks = remember(savedLinks, linkCategory, linkSearchQuery, allUrlsInNotes) {
                    val q = linkSearchQuery.trim().lowercase()
                    savedLinks.filter { link ->
                        val catOk = when (linkCategory) {
                            LinkCategory.ALL -> true
                            LinkCategory.IN_NOTES -> link.url.lowercase() in allUrlsInNotes
                            else -> link.category == linkCategory
                        }
                        val qOk = q.isEmpty() || link.title.lowercase().contains(q) || link.url.lowercase().contains(q)
                        catOk && qOk
                    }
                }

                if (filteredLinks.isEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = null, tint = ru.mutedText.copy(alpha = 0.5f), modifier = Modifier.size(46.dp))
                        Spacer(Modifier.height(10.dp))
                        Text("कोई सेव लिंक नहीं", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = {
                                isCreatingLink = true
                                editingLinkDialog = SavedLink(title = "", url = "")
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("+ लिंक जोड़ें")
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredLinks, key = { it.id }) { link ->
                            val usageCount = notes.count { it.content.contains(link.url, ignoreCase = true) }
                            var linkMenu by remember { mutableStateOf(false) }

                            RuCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        val pickerCb = pickSavedLinkCallback
                                        if (pickerCb != null) {
                                            pickerCb(link)
                                            pickSavedLinkCallback = null
                                            activeBottomTab = 0
                                        } else {
                                            try {
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                                            } catch (_: Exception) {
                                                onShowToast("लिंक खोलने में समस्या")
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(ru.blue.copy(alpha = 0.14f))
                                    ) {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = ru.blue, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(link.title.ifBlank { link.host }, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(link.host, style = MaterialTheme.typography.labelSmall, color = ru.mutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    if (usageCount > 0 && pickSavedLinkCallback == null) {
                                        Surface(
                                            shape = RoundedCornerShape(99.dp),
                                            color = ru.emerald.copy(alpha = 0.18f),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(99.dp))
                                                .clickable {
                                                    filterByLinkUrl = link.url
                                                    activeBottomTab = 0
                                                }
                                        ) {
                                            Text(
                                                text = "$usageCount नोट",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ru.emerald,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    if (pickSavedLinkCallback == null) {
                                        Box {
                                            IconButton(onClick = { linkMenu = true }) {
                                                Icon(Icons.Default.MoreVert, contentDescription = "Link menu", modifier = Modifier.size(18.dp))
                                            }
                                            DropdownMenu(expanded = linkMenu, onDismissRequest = { linkMenu = false }) {
                                                DropdownMenuItem(
                                                    text = { Text("नोट में जोड़ें (Insert in note)") },
                                                    onClick = {
                                                        linkMenu = false
                                                        val lbl = labels.firstOrNull() ?: DefaultSyllabusData.defaultNoteLabels.first()
                                                        val c = NoteCard(
                                                            labelId = lbl.id,
                                                            labelName = lbl.name,
                                                            colorHex = lbl.colorHex,
                                                            content = "${link.title}: ${link.url}"
                                                        )
                                                        onUpdateNotes { listOf(c) + it }
                                                        activeBottomTab = 0
                                                        onShowToast("लिंक नए नोट में जोड़ा गया")
                                                    }
                                                )
                                                if (usageCount > 0) {
                                                    DropdownMenuItem(
                                                        text = { Text("नोट्स में देखें") },
                                                        onClick = {
                                                            linkMenu = false
                                                            filterByLinkUrl = link.url
                                                            activeBottomTab = 0
                                                        }
                                                    )
                                                }
                                                DropdownMenuItem(
                                                    text = { Text("ब्राउज़र में खोलें") },
                                                    onClick = {
                                                        linkMenu = false
                                                        try {
                                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                                                        } catch (_: Exception) {
                                                        }
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("URL कॉपी करें") },
                                                    onClick = {
                                                        linkMenu = false
                                                        ImportExportManager.copyToClipboard(context, link.title, link.url)
                                                        onShowToast("URL कॉपी हुआ")
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("संपादित करें (Edit)") },
                                                    onClick = {
                                                        linkMenu = false
                                                        isCreatingLink = false
                                                        editingLinkDialog = link
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("हटाएँ", color = ru.red) },
                                                    onClick = {
                                                        linkMenu = false
                                                        deletingLinkTarget = link
                                                    }
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

            // Bottom Tab Switcher (Notes | Links)
            if (pickSavedLinkCallback == null) {
                HorizontalDivider(color = ru.cardBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    BottomNotesTabButton(
                        label = "Notes",
                        icon = Icons.Default.Description,
                        selected = activeBottomTab == 0,
                        onClick = { activeBottomTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    BottomNotesTabButton(
                        label = "Links",
                        icon = Icons.Default.Link,
                        selected = activeBottomTab == 1,
                        onClick = { activeBottomTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Pick Label Dialog before creating Note when on "All"
    if (showPickLabelForNewNote) {
        AlertDialog(
            onDismissRequest = { showPickLabelForNewNote = false },
            title = { Text("नोट के लिए लेबल चुनें") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    labels.forEach { lbl ->
                        val c = parseHexColor(lbl.colorHex, ru.blue)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = c.copy(alpha = 0.14f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    showPickLabelForNewNote = false
                                    createNoteWithLabel(lbl)
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Box(Modifier.size(10.dp).clip(CircleShape).background(c))
                                Spacer(Modifier.width(10.dp))
                                Text(lbl.name, style = MaterialTheme.typography.titleSmall, color = c)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPickLabelForNewNote = false }) { Text("रद्द करें") }
            }
        )
    }

    // Create / Edit Label Dialog
    showLabelDialog?.let { initLbl ->
        var name by remember(initLbl) { mutableStateOf(initLbl.name) }
        var colorHex by remember(initLbl) { mutableStateOf(initLbl.colorHex) }
        AlertDialog(
            onDismissRequest = { showLabelDialog = null },
            title = { Text(if (isCreatingLabel) "नया लेबल" else "लेबल बदलें") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("लेबल का नाम") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        DefaultSyllabusData.labelColorPalette.forEach { hex ->
                            val c = parseHexColor(hex)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(
                                        width = if (colorHex.equals(hex, ignoreCase = true)) 2.5.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { colorHex = hex }
                            ) {
                                if (colorHex.equals(hex, ignoreCase = true)) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            if (isCreatingLabel) onAddLabel(name, colorHex)
                            else onUpdateLabel(initLbl.copy(name = name.trim(), colorHex = colorHex))
                            showLabelDialog = null
                        }
                    }
                ) { Text("सहेजें") }
            },
            dismissButton = {
                TextButton(onClick = { showLabelDialog = null }) { Text("रद्द करें") }
            }
        )
    }

    // Add / Edit Saved Link Dialog
    editingLinkDialog?.let { initLink ->
        var url by remember(initLink) { mutableStateOf(initLink.url) }
        var title by remember(initLink) { mutableStateOf(initLink.title) }
        AlertDialog(
            onDismissRequest = { editingLinkDialog = null },
            title = { Text(if (isCreatingLink) "नया लिंक जोड़ें" else "लिंक संपादित करें") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { next ->
                            url = next
                            if (title.isBlank() && next.contains(".")) {
                                title = guessLabelFromUrl(next)
                            }
                        },
                        label = { Text("URL (https://...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("लेबल / शीर्षक") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanUrl = url.trim().let {
                            if (it.isNotEmpty() && !it.startsWith("http://") && !it.startsWith("https://")) "https://$it" else it
                        }
                        if (cleanUrl.isNotEmpty()) {
                            val finalTitle = title.trim().ifEmpty { guessLabelFromUrl(cleanUrl) }
                            onUpsertLink(
                                if (isCreatingLink) SavedLink(title = finalTitle, url = cleanUrl)
                                else initLink.copy(title = finalTitle, url = cleanUrl)
                            )
                            editingLinkDialog = null
                        }
                    }
                ) { Text("सहेजें") }
            },
            dismissButton = {
                TextButton(onClick = { editingLinkDialog = null }) { Text("रद्द करें") }
            }
        )
    }

    deletingNoteTarget?.let { target ->
        ConfirmActionDialog(
            title = "नोट हटाएँ?",
            message = "क्या आप \"${target.labelName}\" का यह नोट हटाना चाहते हैं?",
            onConfirm = {
                val removed = target
                deletingNoteTarget = null
                onUpdateNotes { list -> list.filter { it.id != removed.id } }
                onShowToast("नोट हटाया गया")
            },
            onDismiss = { deletingNoteTarget = null }
        )
    }

    deletingLinkTarget?.let { target ->
        ConfirmActionDialog(
            title = "लिंक हटाएँ?",
            message = "क्या आप \"${target.title}\" को सेव लिंक्स से हटाना चाहते हैं?",
            onConfirm = {
                val id = target.id
                deletingLinkTarget = null
                onDeleteLink(id)
            },
            onDismiss = { deletingLinkTarget = null }
        )
    }
}

@Composable
private fun FocusedNoteEditor(
    card: NoteCard,
    savedLinks: List<SavedLink>,
    onPickFromSavedLinks: ((SavedLink) -> Unit) -> Unit,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    val ru = LocalRuColors.current
    val accent = parseHexColor(card.colorHex, ru.blue)
    val undoStack = remember { mutableStateListOf(card.content) }
    var stackIdx by remember { mutableIntStateOf(0) }
    var text by remember(card.id) { mutableStateOf(card.content) }
    var showInsertLinkDialog by remember { mutableStateOf(false) }

    fun pushText(next: String) {
        if (next == text) return
        while (undoStack.size > stackIdx + 1) undoStack.removeAt(undoStack.lastIndex)
        undoStack.add(next)
        if (undoStack.size > 40) undoStack.removeAt(0) else stackIdx++
        text = next
    }

    fun appendSnippet(snippet: String) {
        val sep = if (text.isEmpty() || text.endsWith("\n")) "" else "\n"
        pushText("$text$sep$snippet")
    }

    fun wrapLastTokenOrAppend(prefix: String, suffix: String, placeholder: String) {
        if (text.isBlank()) {
            pushText("$prefix$placeholder$suffix")
        } else {
            pushText("$text $prefix$placeholder$suffix")
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Focused Edit Header: Cancel, Undo, Redo, Save
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel edit")
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
                    Spacer(Modifier.width(7.dp))
                    Text(card.labelName, style = MaterialTheme.typography.titleMedium, color = accent)
                }
                IconButton(
                    onClick = {
                        if (stackIdx > 0) {
                            stackIdx--
                            text = undoStack[stackIdx]
                        }
                    },
                    enabled = stackIdx > 0
                ) {
                    Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                }
                IconButton(
                    onClick = {
                        if (stackIdx < undoStack.lastIndex) {
                            stackIdx++
                            text = undoStack[stackIdx]
                        }
                    },
                    enabled = stackIdx < undoStack.lastIndex
                ) {
                    Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = "Redo")
                }
                IconButton(
                    onClick = { onSave(text) },
                    modifier = Modifier.testTag("focused_note_save_btn")
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save note", tint = ru.emerald)
                }
            }
            HorizontalDivider(color = ru.cardBorder)

            // Full-height editor field
            OutlinedTextField(
                value = text,
                onValueChange = { pushText(it) },
                placeholder = { Text("सूत्र, ट्रिक, चेकलिस्ट या लिंक यहाँ लिखें...") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(14.dp)
                    .testTag("focused_note_textfield")
            )

            // Scrollable Formatting Toolbar
            HorizontalDivider(color = ru.cardBorder)
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                FmtIconBtn(Icons.Default.FormatBold, "Bold") { wrapLastTokenOrAppend("**", "**", "मुख्य") }
                FmtIconBtn(Icons.Default.FormatItalic, "Italic") { wrapLastTokenOrAppend("*", "*", "तिरछा") }
                FmtIconBtn(Icons.Default.FormatUnderlined, "Underline") { wrapLastTokenOrAppend("__", "__", "रेखांकित") }
                FmtIconBtn(Icons.Default.FormatStrikethrough, "Strike") { wrapLastTokenOrAppend("~~", "~~", "कटा हुआ") }
                FmtIconBtn(Icons.Default.Title, "Heading") { appendSnippet("## शीर्षक") }
                FmtIconBtn(Icons.AutoMirrored.Filled.FormatListBulleted, "Bullet") { appendSnippet("- ") }
                FmtIconBtn(Icons.Default.FormatListNumbered, "Numbered") { appendSnippet("1. ") }
                FmtIconBtn(Icons.Default.CheckBox, "Checklist") { appendSnippet("[ ] ") }
                FmtIconBtn(Icons.Default.FormatQuote, "Quote") { appendSnippet("> ") }
                FmtIconBtn(Icons.Default.Highlight, "Highlight") { wrapLastTokenOrAppend("==", "==", "खास") }
                FmtIconBtn(Icons.Default.HorizontalRule, "Divider") { appendSnippet("---") }
                FmtIconBtn(Icons.Default.Link, "Insert Link") { showInsertLinkDialog = true }
                FmtIconBtn(Icons.Default.FormatClear, "Clear format") {
                    pushText(
                        text.replace("**", "")
                            .replace("==", "")
                            .replace("~~", "")
                            .replace("__", "")
                    )
                }
            }
        }
    }

    if (showInsertLinkDialog) {
        var linkUrl by remember { mutableStateOf("") }
        var linkLabel by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showInsertLinkDialog = false },
            title = { Text("लिंक जोड़ें (Insert Link)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text("URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = linkLabel,
                        onValueChange = { linkLabel = it },
                        label = { Text("नाम (वैकल्पिक)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (savedLinks.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                showInsertLinkDialog = false
                                onPickFromSavedLinks { picked ->
                                    appendSnippet("${picked.title}: ${picked.url}")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Saved Links से चुनें (${savedLinks.size})")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val u = linkUrl.trim().let {
                            if (it.isNotEmpty() && !it.startsWith("http")) "https://$it" else it
                        }
                        if (u.isNotEmpty()) {
                            val label = linkLabel.trim()
                            appendSnippet(if (label.isNotEmpty()) "$label: $u" else u)
                            showInsertLinkDialog = false
                        }
                    }
                ) { Text("जोड़ें") }
            },
            dismissButton = {
                TextButton(onClick = { showInsertLinkDialog = false }) { Text("रद्द करें") }
            }
        )
    }
}

@Composable
private fun FmtIconBtn(icon: ImageVector, desc: String, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(icon, contentDescription = desc, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun BottomNotesTabButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ru = LocalRuColors.current
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) ru.blue.copy(alpha = 0.18f) else Color.Transparent,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) ru.blue else ru.mutedText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) ru.blue else ru.mutedText
            )
        }
    }
}
