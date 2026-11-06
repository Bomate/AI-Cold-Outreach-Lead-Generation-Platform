package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessLead
import com.example.data.model.EmailDraft
import com.example.data.model.OutreachStatus
import com.example.data.model.OutreachTemplate
import com.example.data.model.PsychologyAngle
import com.example.data.verification.EmailVerificationService
import com.example.ui.components.OutreachStatusBadge
import com.example.ui.components.PsychologyAngleChip
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.Cyan100
import com.example.ui.theme.Cyan600
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Indigo100
import com.example.ui.theme.Indigo600

@Composable
fun DrafterScreen(
    leads: List<BusinessLead>,
    templates: List<OutreachTemplate>,
    selectedLead: BusinessLead?,
    selectedAngle: PsychologyAngle,
    activeDraft: EmailDraft?,
    isGenerating: Boolean,
    onSelectLead: (BusinessLead) -> Unit,
    onSelectAngle: (PsychologyAngle) -> Unit,
    onSelectTemplate: (OutreachTemplate) -> Unit,
    onOpenTemplateLibrary: () -> Unit,
    onGenerateClick: () -> Unit,
    onSaveAsTemplate: (EmailDraft, String) -> Unit,
    onSendEmailClick: (EmailDraft, Context, Boolean) -> Unit,
    onSimulateStatus: (Long, OutreachStatus) -> Unit,
    onOpenABTesting: () -> Unit = {}
) {
    val context = LocalContext.current
    var showLeadDropdown by remember { mutableStateOf(false) }
    var selectedSubjectVariant by remember { mutableIntStateOf(0) } // 0 = Primary, 1 = Alternative
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var customTemplateTitle by remember { mutableStateOf("") }

    var editableBody by remember(activeDraft?.id, activeDraft?.emailBody) {
        mutableStateOf(activeDraft?.emailBody ?: "")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("drafter_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Banner / Template Library & A/B Testing Shortcut
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Indigo100.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Indigo600, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CollectionsBookmark, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "Psychology Templates & A/B Lab",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${templates.size} frameworks • A/B test 2 variations to measure reply lift",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onOpenABTesting,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_open_ab_lab_drafter")
                        ) {
                            Text("A/B Lab 👑", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onOpenTemplateLibrary,
                            colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_open_template_library_drafter")
                        ) {
                            Icon(Icons.Default.CollectionsBookmark, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text("Templates", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        // Target Lead Selector Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Target Business Prospect",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Prospect Dropdown Trigger
                    Box {
                        OutlinedButton(
                            onClick = { showLeadDropdown = true },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().testTag("select_prospect_btn")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = selectedLead?.companyName ?: "Choose a business prospect...",
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (selectedLead != null) {
                                        Text(
                                            text = "${selectedLead.contactName} (${selectedLead.role}) • ${selectedLead.email}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        }

                        DropdownMenu(
                            expanded = showLeadDropdown,
                            onDismissRequest = { showLeadDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            leads.forEach { lead ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(lead.companyName, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${lead.contactName} • ${lead.email}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onSelectLead(lead)
                                        showLeadDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Lead Context Summary
                    if (selectedLead != null) {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Target Context",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    VerificationBadge(
                                        status = selectedLead.verificationStatus,
                                        score = selectedLead.deliverabilityScore
                                    )
                                }

                                val preDraftCheck = remember(selectedLead.email) {
                                    EmailVerificationService.getInstance().validateProspectBeforeDrafting(selectedLead)
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (preDraftCheck.isValid) Icons.Default.Check else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (preDraftCheck.isValid) Emerald600 else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (preDraftCheck.isValid) "Regex & Domain checks passed (${selectedLead.email})" else preDraftCheck.message,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (preDraftCheck.isValid) Emerald600 else MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Text(
                                    text = "• Pain Point: ${selectedLead.painPoint}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "• Goal: ${selectedLead.targetGoal}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Psychology Angle Selector
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. Select Human Psychology Framework",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(PsychologyAngle.values()) { angle ->
                            PsychologyAngleChip(
                                angle = angle,
                                selected = selectedAngle == angle,
                                onClick = { onSelectAngle(angle) }
                            )
                        }
                    }

                    // Psychology Explainer Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp).padding(top = 2.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = selectedAngle.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = selectedAngle.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Psychology: ${selectedAngle.psychologyHook}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Quick Template Suggestions for this Psychology Angle
                    val matchingTemplates = remember(templates, selectedAngle) {
                        templates.filter { it.psychologyAngle == selectedAngle }
                    }

                    if (matchingTemplates.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Ready-Made Templates for ${selectedAngle.title.take(15)}:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap to load",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 10.sp
                                )
                            }

                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(matchingTemplates) { template ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { onSelectTemplate(template) },
                                        label = {
                                            Text(
                                                text = "${template.title} (${template.category})",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.CollectionsBookmark,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Generate AI Button
                    Button(
                        onClick = onGenerateClick,
                        modifier = Modifier.fillMaxWidth().testTag("btn_generate_ai_draft"),
                        shape = RoundedCornerShape(14.dp),
                        enabled = selectedLead != null && !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text("Gemini AI is Crafting...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                text = if (activeDraft == null) "Draft Personalized Outreach" else "Regenerate with Gemini AI",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Active Draft Result
        if (activeDraft != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth().testTag("generated_draft_card")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Draft Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AI Personalized Draft",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Recipient: ${activeDraft.recipientEmail}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            OutreachStatusBadge(status = activeDraft.status)
                        }

                        // Subject Line A/B Variations
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Subject Line (A/B Test Variant)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FilterChip(
                                        selected = selectedSubjectVariant == 0,
                                        onClick = { selectedSubjectVariant = 0 },
                                        label = { Text("Variant A (Primary)", fontSize = 10.sp) }
                                    )
                                    FilterChip(
                                        selected = selectedSubjectVariant == 1,
                                        onClick = { selectedSubjectVariant = 1 },
                                        label = { Text("Variant B", fontSize = 10.sp) }
                                    )
                                }
                            }

                            val currentSubject = if (selectedSubjectVariant == 0) activeDraft.subjectLine else activeDraft.subjectAlternative

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = currentSubject,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Diagnostic Survey Question Highlight
                        if (activeDraft.surveyQuestion.isNotBlank()) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Cyan100.copy(alpha = 0.6f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.HelpOutline,
                                        contentDescription = null,
                                        tint = Cyan600,
                                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Embedded Diagnostic Survey Hook:",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Cyan600
                                        )
                                        Text(
                                            text = activeDraft.surveyQuestion,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Email Body Editor
                        OutlinedTextField(
                            value = editableBody,
                            onValueChange = { editableBody = it },
                            label = { Text("Personalized Email Body") },
                            modifier = Modifier.fillMaxWidth().testTag("email_body_field"),
                            minLines = 8,
                            shape = RoundedCornerShape(14.dp)
                        )

                        // Follow-up Sequence Preview
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Day +3 Follow-Up Nudge Draft:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = activeDraft.followUpNudge,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Send & Dispatch Actions
                        Text(
                            text = "Dispatch & Tracking Controls",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val currentSubject = if (selectedSubjectVariant == 0) activeDraft.subjectLine else activeDraft.subjectAlternative
                                    val draftToSend = activeDraft.copy(subjectLine = currentSubject, emailBody = editableBody)
                                    onSendEmailClick(draftToSend, context, true)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("btn_send_mail_app")
                            ) {
                                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Send in Mail App", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    val currentSubject = if (selectedSubjectVariant == 0) activeDraft.subjectLine else activeDraft.subjectAlternative
                                    val draftToSend = activeDraft.copy(subjectLine = currentSubject, emailBody = editableBody)
                                    onSendEmailClick(draftToSend, context, false)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Emerald600
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("btn_send_in_app")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Dispatch & Track", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Save as Reusable Template Button
                        OutlinedButton(
                            onClick = {
                                customTemplateTitle = "${selectedLead?.companyName ?: "Custom"} ${activeDraft.psychologyAngle.title} Sequence"
                                showSaveTemplateDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_save_draft_as_template")
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Indigo600)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Save As Reusable Template in Library", fontWeight = FontWeight.Bold, color = Indigo600)
                        }

                        // Copy & Simulation Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Outreach Email", editableBody)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Copied email text!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Copy", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onSimulateStatus(activeDraft.id, OutreachStatus.OPENED) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("Open", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { onSimulateStatus(activeDraft.id, OutreachStatus.CLICKED) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.size(4.dp))
                                Text("CTR", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    // Save as Reusable Template Dialog
    if (showSaveTemplateDialog && activeDraft != null) {
        Dialog(
            onDismissRequest = { showSaveTemplateDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Save as Reusable Template",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "This will generalize prospect-specific values (e.g., {{contactName}}, {{companyName}}, {{painPoint}}) so you can reuse this outreach campaign across any lead.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = customTemplateTitle,
                        onValueChange = { customTemplateTitle = it },
                        label = { Text("Template Name") },
                        placeholder = { Text("e.g., Series A CAC Breakthrough Hook") },
                        modifier = Modifier.fillMaxWidth().testTag("save_template_title_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showSaveTemplateDialog = false },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val currentSubject = if (selectedSubjectVariant == 0) activeDraft.subjectLine else activeDraft.subjectAlternative
                                val draftWithEdits = activeDraft.copy(subjectLine = currentSubject, emailBody = editableBody)
                                onSaveAsTemplate(draftWithEdits, customTemplateTitle)
                                showSaveTemplateDialog = false
                            },
                            enabled = customTemplateTitle.isNotBlank(),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1.3f).testTag("btn_confirm_save_template")
                        ) {
                            Text("Save to Library", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
