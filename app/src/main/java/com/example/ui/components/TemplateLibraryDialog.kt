package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessLead
import com.example.data.model.OutreachTemplate
import com.example.data.model.PsychologyAngle
import com.example.ui.theme.Cyan100
import com.example.ui.theme.Cyan600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600
import com.example.ui.theme.Indigo800
import com.example.ui.theme.Indigo900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate800

@Composable
fun TemplateLibraryDialog(
    templates: List<OutreachTemplate>,
    leads: List<BusinessLead>,
    selectedLead: BusinessLead?,
    searchQuery: String,
    selectedAngleFilter: PsychologyAngle?,
    selectedCategoryFilter: String?,
    previewingTemplate: OutreachTemplate?,
    onDismiss: () -> Unit,
    onSearchChange: (String) -> Unit,
    onAngleFilterChange: (PsychologyAngle?) -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onOpenPreview: (OutreachTemplate) -> Unit,
    onClosePreview: () -> Unit,
    onOpenCreateTemplate: () -> Unit,
    onOpenEditTemplate: (OutreachTemplate) -> Unit,
    onOpenAiGenerate: () -> Unit,
    onDuplicateTemplate: (OutreachTemplate) -> Unit,
    onDeleteTemplate: (OutreachTemplate) -> Unit,
    onApplyTemplate: (OutreachTemplate, BusinessLead?) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("template_library_dialog"),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                TemplateLibraryHeader(
                    templateCount = templates.size,
                    onDismiss = onDismiss,
                    onCreateClick = onOpenCreateTemplate,
                    onAiGenClick = onOpenAiGenerate
                )

                // Search & Filter Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("Search templates, subject lines, psychology angles...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("template_search_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    // Psychology Angle Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = selectedAngleFilter == null,
                                onClick = { onAngleFilterChange(null) },
                                label = { Text("All Angles (${templates.size})", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                        items(PsychologyAngle.values()) { angle ->
                            val count = templates.count { it.psychologyAngle == angle }
                            FilterChip(
                                selected = selectedAngleFilter == angle,
                                onClick = {
                                    onAngleFilterChange(if (selectedAngleFilter == angle) null else angle)
                                },
                                label = { Text("${angle.title.take(18)} ($count)", fontSize = 11.sp) },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Categories Row (Custom vs Default vs Specific Categories)
                    val categories = remember(templates) {
                        listOf("All Categories") + templates.map { it.category }.distinct()
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = if (category == "All Categories") selectedCategoryFilter == null else selectedCategoryFilter == category
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onCategoryFilterChange(if (category == "All Categories" || selectedCategoryFilter == category) null else category)
                                },
                                label = { Text(category, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Filtered List
                val filteredTemplates = remember(templates, searchQuery, selectedAngleFilter, selectedCategoryFilter) {
                    templates.filter { t ->
                        val matchesQuery = searchQuery.isBlank() ||
                                t.title.contains(searchQuery, ignoreCase = true) ||
                                t.category.contains(searchQuery, ignoreCase = true) ||
                                t.subjectLineTemplate.contains(searchQuery, ignoreCase = true) ||
                                t.bodyTemplate.contains(searchQuery, ignoreCase = true) ||
                                t.psychologyInsight.contains(searchQuery, ignoreCase = true) ||
                                t.psychologyAngle.title.contains(searchQuery, ignoreCase = true)

                        val matchesAngle = selectedAngleFilter == null || t.psychologyAngle == selectedAngleFilter
                        val matchesCategory = selectedCategoryFilter == null || t.category.equals(selectedCategoryFilter, ignoreCase = true)

                        matchesQuery && matchesAngle && matchesCategory
                    }
                }

                if (filteredTemplates.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Text(
                                "No matching templates found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Try adjusting your filters or create a new custom psychology template.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onOpenCreateTemplate,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text("Create Custom Template")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTemplates, key = { it.id }) { template ->
                            TemplateCard(
                                template = template,
                                selectedLead = selectedLead,
                                onPreviewClick = { onOpenPreview(template) },
                                onApplyClick = { onApplyTemplate(template, selectedLead) },
                                onEditClick = { onOpenEditTemplate(template) },
                                onDuplicateClick = { onDuplicateTemplate(template) },
                                onDeleteClick = { onDeleteTemplate(template) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Template Preview Modal
    if (previewingTemplate != null) {
        TemplatePreviewDialog(
            template = previewingTemplate,
            leads = leads,
            selectedLead = selectedLead,
            onDismiss = onClosePreview,
            onApply = { lead ->
                onApplyTemplate(previewingTemplate, lead)
            },
            onEdit = {
                onClosePreview()
                onOpenEditTemplate(previewingTemplate)
            }
        )
    }
}

@Composable
private fun TemplateLibraryHeader(
    templateCount: Int,
    onDismiss: () -> Unit,
    onCreateClick: () -> Unit,
    onAiGenClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Indigo100, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Psychology,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Psychology Outreach Templates",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$templateCount reusable conversion frameworks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // AI Generate Button
                Button(
                    onClick = onAiGenClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_ai_gen_template")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("AI Generator", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Add Custom Template
                OutlinedButton(
                    onClick = onCreateClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_add_custom_template")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("New", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        }
    }
}

@Composable
fun TemplateCard(
    template: OutreachTemplate,
    selectedLead: BusinessLead?,
    onPreviewClick: () -> Unit,
    onApplyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDuplicateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPreviewClick() }
            .testTag("template_card_${template.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Row: Category + Angle Badges & Usage Count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category Badge
                    Box(
                        modifier = Modifier
                            .background(
                                if (template.isCustom) Emerald100 else Indigo100,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (template.isCustom) "Custom: ${template.category}" else template.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (template.isCustom) Emerald600 else Indigo600,
                            fontSize = 10.sp
                        )
                    }

                    // Angle Badge
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = template.psychologyAngle.subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }

                // Usage count
                if (template.usageCount > 0) {
                    Text(
                        text = "Used ${template.usageCount}x",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Title
            Text(
                text = template.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Subject Line Snippet
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Subject:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp
                )
                Text(
                    text = template.subjectLineTemplate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 11.sp
                )
            }

            // Body Snippet Preview
            Text(
                text = template.bodyTemplate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            // Psychology Insight Callout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Cyan100.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Cyan600,
                    modifier = Modifier.size(14.dp).padding(top = 1.dp)
                )
                Text(
                    text = template.psychologyInsight,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate800,
                    fontSize = 11.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onPreviewClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = "Preview Template",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDuplicateClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Duplicate Template",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Template",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    if (template.isCustom) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Template",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Apply button
                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                    modifier = Modifier.testTag("btn_apply_template_${template.id}")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = if (selectedLead != null) "Apply to ${selectedLead.companyName.take(12)}" else "Use in Campaign",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TemplatePreviewDialog(
    template: OutreachTemplate,
    leads: List<BusinessLead>,
    selectedLead: BusinessLead?,
    onDismiss: () -> Unit,
    onApply: (BusinessLead?) -> Unit,
    onEdit: () -> Unit
) {
    var previewMode by remember { mutableIntStateOf(0) } // 0 = Rendered with Lead, 1 = Raw Template
    var leadToRender by remember { mutableStateOf(selectedLead ?: leads.firstOrNull()) }
    var selectedSubjectVariant by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 20.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = template.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${template.category} • ${template.psychologyAngle.title}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Raw Template vs Live Render
                TabRow(
                    selectedTabIndex = previewMode,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = previewMode == 0,
                        onClick = { previewMode = 0 },
                        text = { Text("Live Rendered Preview", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = previewMode == 1,
                        onClick = { previewMode = 1 },
                        text = { Text("Raw Template & Tags", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Psychology Rationale
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Cyan100.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = Cyan600, modifier = Modifier.size(20.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Psychology Framework: ${template.psychologyAngle.subtitle}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate800
                                )
                                Text(
                                    text = template.psychologyInsight,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate800,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Lead Context for Live Rendering
                    if (previewMode == 0 && leads.isNotEmpty()) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rendering with: ${leadToRender?.companyName ?: "Sample Prospect"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${leadToRender?.contactName} (${leadToRender?.industry})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Subject Lines
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Subject Line Variants", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    FilterChip(
                                        selected = selectedSubjectVariant == 0,
                                        onClick = { selectedSubjectVariant = 0 },
                                        label = { Text("Variant A", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = selectedSubjectVariant == 1,
                                        onClick = { selectedSubjectVariant = 1 },
                                        label = { Text("Variant B", fontSize = 10.sp) }
                                    )
                                }
                            }

                            val rawSubject = if (selectedSubjectVariant == 0) template.subjectLineTemplate else template.subjectAlternativeTemplate
                            val renderedSubject = if (previewMode == 0 && leadToRender != null) {
                                substituteVars(rawSubject, leadToRender!!)
                            } else {
                                rawSubject
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = renderedSubject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Email Body
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Email Body", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                            val rawBody = template.bodyTemplate
                            val renderedBody = if (previewMode == 0 && leadToRender != null) {
                                substituteVars(rawBody, leadToRender!!)
                            } else {
                                rawBody
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = renderedBody,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    // Diagnostic Survey Question
                    if (template.surveyQuestionTemplate.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Cyan100.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Embedded Diagnostic Micro-Survey:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Cyan600
                                )
                                val rawQ = template.surveyQuestionTemplate
                                val renderedQ = if (previewMode == 0 && leadToRender != null) substituteVars(rawQ, leadToRender!!) else rawQ
                                Text(
                                    text = renderedQ,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate800
                                )
                            }
                        }
                    }

                    // Day +3 Follow Up Nudge
                    if (template.followUpNudgeTemplate.isNotBlank()) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Day +3 Follow-Up Nudge:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                val rawF = template.followUpNudgeTemplate
                                val renderedF = if (previewMode == 0 && leadToRender != null) substituteVars(rawF, leadToRender!!) else rawF
                                Text(
                                    text = renderedF,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Variable tags legend
                    if (previewMode == 1) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Supported Dynamic Tags", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "{{contactName}}, {{firstName}}, {{companyName}}, {{painPoint}}, {{targetGoal}}, {{industry}}, {{role}}, {{website}}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text("Customize")
                    }

                    Button(
                        onClick = { onApply(leadToRender) },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.3f).testTag("btn_apply_from_preview")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = if (leadToRender != null) "Apply to ${leadToRender?.companyName?.take(10)}" else "Apply to Campaign",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun substituteVars(text: String, lead: BusinessLead): String {
    val firstName = lead.contactName.trim().split("\\s+".toRegex()).firstOrNull() ?: lead.contactName
    return text
        .replace("{{contactName}}", lead.contactName)
        .replace("{{firstName}}", firstName)
        .replace("{{companyName}}", lead.companyName)
        .replace("{{painPoint}}", lead.painPoint)
        .replace("{{targetGoal}}", lead.targetGoal)
        .replace("{{industry}}", lead.industry)
        .replace("{{role}}", lead.role)
        .replace("{{website}}", lead.website.ifBlank { "${lead.companyName.lowercase().replace(" ", "")}.com" })
}
