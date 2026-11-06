package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExperimentStatus
import com.example.data.model.OutreachTemplate
import com.example.data.model.PsychologyAngle
import com.example.data.model.TemplateVariationExperiment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateABExperimentDialog(
    templates: List<OutreachTemplate>,
    isGeneratingChallenger: Boolean,
    onGenerateAiChallenger: (originalSubject: String, originalBody: String, originalAngle: PsychologyAngle, challengerAngle: PsychologyAngle, instruction: String, audience: String, onResult: (com.example.data.remote.GeminiOutreachService.GeneratedVariant) -> Unit) -> Unit,
    onSaveExperiment: (TemplateVariationExperiment) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("Survey Hook vs Direct ROI (B2B)") }
    var hypothesis by remember { mutableStateOf("Asking a 10-second micro-survey question will increase reply rates by >20% compared to a standard value pitch.") }
    var targetAudience by remember { mutableStateOf("Founders & Operations Directors") }
    var testedElement by remember { mutableStateOf("Opening Hook & Micro-Survey CTA") }

    // Variant A State
    var variantAName by remember { mutableStateOf("Variation A (Control - Problem Agitation)") }
    var variantAAngle by remember { mutableStateOf(PsychologyAngle.PROBLEM_AGITATION) }
    var variantASubject by remember { mutableStateOf("quick question on {{companyName}}'s onboarding drop-off") }
    var variantAAltSubject by remember { mutableStateOf("{{contactName}}, thought on {{painPoint}}?") }
    var variantASurvey by remember { mutableStateOf("Is {{painPoint}} currently costing your team more in lost hours or delayed revenue?") }
    var variantABody by remember {
        mutableStateOf(
            """Hi {{contactName}},

Most {{role}}s in {{industry}} we speak with mention the same silent drain: {{painPoint}}.

When left unresolved, this quietly costs 15+ lost hours each week and delays {{targetGoal}}.

We built a specialized engine that solves {{painPoint}} and delivers {{targetGoal}} automatically.

Open to a 2-minute glance at how {{companyName}} can test this?

Best,
AI Outreach Team"""
        )
    }
    var variantAFollowUp by remember { mutableStateOf("Hi {{contactName}}, circling back in case resolving {{painPoint}} is on your roadmap this quarter.") }

    // Variant B State
    var variantBName by remember { mutableStateOf("Variation B (Challenger - Micro-Survey)") }
    var variantBAngle by remember { mutableStateOf(PsychologyAngle.SURVEY_HOOK) }
    var variantBSubject by remember { mutableStateOf("quick 10-sec question for {{contactName}}") }
    var variantBAltSubject by remember { mutableStateOf("{{companyName}} + {{targetGoal}} benchmarks") }
    var variantBSurvey by remember { mutableStateOf("Between {{painPoint}} and team bandwidth, which is causing more friction in reaching {{targetGoal}}?") }
    var variantBBody by remember {
        mutableStateOf(
            """Hi {{contactName}},

Noticed {{companyName}}'s growth in {{industry}} and had a quick 10-second diagnostic question:

Between {{painPoint}} and day-to-day team bandwidth, which is causing more friction in reaching {{targetGoal}} this quarter?

We recently surveyed 40+ leaders in {{industry}} and compiled a 1-page benchmark sheet on how high-performing teams streamline this.

If you're open to the data sheet, happy to send it over. No pitch, just raw insights.

Best,
AI Outreach Team"""
        )
    }
    var variantBFollowUp by remember { mutableStateOf("Hi {{contactName}}, quick follow-up on my note above—would that 1-page benchmark data on {{targetGoal}} be useful?") }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showTemplatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Science,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Create New A/B Template Experiment",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Set up two distinct variations to measure open rates and reply lift",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Tabs: Overview -> Variant A -> Variant B
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("1. Hypothesis & Setup", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("2. Variation A (Control)", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("3. Variation B (Challenger)", fontWeight = FontWeight.SemiBold) }
                    )
                }

                // Body content
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            // Setup & Hypothesis
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Experiment Overview", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                        if (templates.isNotEmpty()) {
                                            Box {
                                                OutlinedButton(
                                                    onClick = { showTemplatePicker = true },
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                                ) {
                                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Import Base Template", style = MaterialTheme.typography.labelSmall)
                                                }
                                                DropdownMenu(
                                                    expanded = showTemplatePicker,
                                                    onDismissRequest = { showTemplatePicker = false }
                                                ) {
                                                    templates.forEach { tpl ->
                                                        DropdownMenuItem(
                                                            text = { Text("${tpl.title} (${tpl.psychologyAngle.title})") },
                                                            onClick = {
                                                                title = "${tpl.title} A/B Test"
                                                                variantAName = "Control (${tpl.psychologyAngle.title})"
                                                                variantAAngle = tpl.psychologyAngle
                                                                variantASubject = tpl.subjectLineTemplate
                                                                variantAAltSubject = tpl.subjectAlternativeTemplate
                                                                variantABody = tpl.bodyTemplate
                                                                variantASurvey = tpl.surveyQuestionTemplate
                                                                variantAFollowUp = tpl.followUpNudgeTemplate
                                                                showTemplatePicker = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = title,
                                        onValueChange = { title = it },
                                        label = { Text("Experiment Title") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = hypothesis,
                                        onValueChange = { hypothesis = it },
                                        label = { Text("Scientific Testing Hypothesis") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = targetAudience,
                                            onValueChange = { targetAudience = it },
                                            label = { Text("Target Audience") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = testedElement,
                                            onValueChange = { testedElement = it },
                                            label = { Text("Tested Element (e.g. CTA, Hook)") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            // Variation A Setup
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Variation A (Control)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantAName,
                                        onValueChange = { variantAName = it },
                                        label = { Text("Variant A Label") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Angle Selector
                                    AngleSelectorDropdown(
                                        selected = variantAAngle,
                                        onSelect = { variantAAngle = it }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantASubject,
                                        onValueChange = { variantASubject = it },
                                        label = { Text("Primary Subject Line") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantASurvey,
                                        onValueChange = { variantASurvey = it },
                                        label = { Text("Diagnostic Micro-Survey Question") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantABody,
                                        onValueChange = { variantABody = it },
                                        label = { Text("Email Body Template (use {{tags}})") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 6
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantAFollowUp,
                                        onValueChange = { variantAFollowUp = it },
                                        label = { Text("Follow-up Nudge (Day +3)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                        2 -> {
                            // Variation B Setup (Challenger)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Variation B (Challenger)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                                        Button(
                                            onClick = {
                                                onGenerateAiChallenger(
                                                    variantASubject,
                                                    variantABody,
                                                    variantAAngle,
                                                    variantBAngle,
                                                    "Draft a high-converting challenger using $testedElement strategy with $targetAudience.",
                                                    targetAudience
                                                ) { generated ->
                                                    variantBName = generated.variantName
                                                    variantBAngle = generated.psychologyAngle
                                                    variantBSubject = generated.subjectLine
                                                    variantBAltSubject = generated.subjectAlternative
                                                    variantBBody = generated.emailBody
                                                    variantBSurvey = generated.surveyQuestion
                                                    variantBFollowUp = generated.followUpNudge
                                                    if (generated.testingHypothesis.isNotBlank()) {
                                                        hypothesis = generated.testingHypothesis
                                                    }
                                                }
                                            },
                                            enabled = !isGeneratingChallenger,
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            if (isGeneratingChallenger) {
                                                CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("AI Generating...")
                                            } else {
                                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("AI Auto-Draft Challenger")
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantBName,
                                        onValueChange = { variantBName = it },
                                        label = { Text("Variant B Label") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Angle Selector
                                    AngleSelectorDropdown(
                                        selected = variantBAngle,
                                        onSelect = { variantBAngle = it }
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantBSubject,
                                        onValueChange = { variantBSubject = it },
                                        label = { Text("Primary Subject Line") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantBSurvey,
                                        onValueChange = { variantBSurvey = it },
                                        label = { Text("Diagnostic Micro-Survey Question") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantBBody,
                                        onValueChange = { variantBBody = it },
                                        label = { Text("Email Body Template (use {{tags}})") },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 6
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = variantBFollowUp,
                                        onValueChange = { variantBFollowUp = it },
                                        label = { Text("Follow-up Nudge (Day +3)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // Footer Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedTab > 0) {
                        OutlinedButton(
                            onClick = { selectedTab-- },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        if (selectedTab < 2) {
                            Button(
                                onClick = { selectedTab++ },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Next")
                            }
                        } else {
                            Button(
                                onClick = {
                                    val experiment = TemplateVariationExperiment(
                                        title = title.trim().ifBlank { "A/B Variation Experiment" },
                                        hypothesis = hypothesis.trim(),
                                        targetAudience = targetAudience.trim(),
                                        testedElement = testedElement.trim(),
                                        variantAName = variantAName.trim(),
                                        variantAPsychologyAngle = variantAAngle,
                                        variantASubjectLine = variantASubject.trim(),
                                        variantASubjectAlt = variantAAltSubject.trim(),
                                        variantABody = variantABody.trim(),
                                        variantASurveyHook = variantASurvey.trim(),
                                        variantAFollowUpNudge = variantAFollowUp.trim(),
                                        variantBName = variantBName.trim(),
                                        variantBPsychologyAngle = variantBAngle,
                                        variantBSubjectLine = variantBSubject.trim(),
                                        variantBSubjectAlt = variantBAltSubject.trim(),
                                        variantBBody = variantBBody.trim(),
                                        variantBSurveyHook = variantBSurvey.trim(),
                                        variantBFollowUpNudge = variantBFollowUp.trim(),
                                        status = ExperimentStatus.ACTIVE_TESTING
                                    )
                                    onSaveExperiment(experiment)
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("save_new_experiment_btn")
                            ) {
                                Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Launch Experiment")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AngleSelectorDropdown(
    selected: PsychologyAngle,
    onSelect: (PsychologyAngle) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Psychology Angle: ${selected.title}", style = MaterialTheme.typography.bodyMedium)
                Text("▼", fontSize = 10.sp)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PsychologyAngle.entries.forEach { angle ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(angle.title, fontWeight = FontWeight.Bold)
                            Text(angle.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    onClick = {
                        onSelect(angle)
                        expanded = false
                    }
                )
            }
        }
    }
}
