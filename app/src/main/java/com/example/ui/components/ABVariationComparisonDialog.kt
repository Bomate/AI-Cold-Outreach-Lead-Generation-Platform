package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessLead
import com.example.data.model.ExperimentStatus
import com.example.data.model.PsychologyAngle
import com.example.data.model.TemplateVariationExperiment
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ABVariationComparisonDialog(
    experiments: List<TemplateVariationExperiment>,
    selectedExperiment: TemplateVariationExperiment?,
    leads: List<BusinessLead>,
    activeLead: BusinessLead?,
    isGeneratingChallenger: Boolean,
    isSimulatingCohort: Boolean,
    onSelectExperiment: (TemplateVariationExperiment) -> Unit,
    onCreateNewExperimentClick: () -> Unit,
    onDeleteExperiment: (TemplateVariationExperiment) -> Unit,
    onGenerateAiChallenger: (originalSubject: String, originalBody: String, originalAngle: PsychologyAngle, challengerAngle: PsychologyAngle, instruction: String, audience: String, onResult: (com.example.data.remote.GeminiOutreachService.GeneratedVariant) -> Unit) -> Unit,
    onSaveExperiment: (TemplateVariationExperiment) -> Unit,
    onSimulateCohort: (experimentId: Long, sampleSize: Int, favored: String) -> Unit,
    onResetMetrics: (experimentId: Long) -> Unit,
    onDeclareWinner: (experimentId: Long, winner: String, reason: String) -> Unit,
    onPromoteWinner: (experimentId: Long, winner: String) -> Unit,
    onApplyToDrafter: (experiment: TemplateVariationExperiment, variant: String, lead: BusinessLead?) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var substituteVariables by remember { mutableStateOf(true) }
    var previewLead by remember(activeLead, leads) { mutableStateOf(activeLead ?: leads.firstOrNull()) }
    var showAiChallengerSheet by remember { mutableStateOf(false) }

    val currentExp = selectedExperiment ?: experiments.firstOrNull()

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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Bar
                ABDialogTopHeader(
                    currentExperiment = currentExp,
                    experimentsCount = experiments.size,
                    onCreateNew = onCreateNewExperimentClick,
                    onDismiss = onDismiss
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                if (experiments.isEmpty() || currentExp == null) {
                    // Empty State
                    EmptyABExperimentsView(onCreateNew = onCreateNewExperimentClick)
                } else {
                    // Experiment Carousel Selector
                    ExperimentCarouselBar(
                        experiments = experiments,
                        selectedExp = currentExp,
                        onSelect = onSelectExperiment
                    )

                    // Navigation Tabs
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Dual Variation Drafter", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Performance & Winner", fontWeight = FontWeight.SemiBold)
                                    if (currentExp.effectiveWinner != null) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("👑", fontSize = 12.sp)
                                    }
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Hypothesis & Settings", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        )
                    }

                    // Content Area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        when (selectedTab) {
                            0 -> DualVariationDrafterTab(
                                experiment = currentExp,
                                leads = leads,
                                previewLead = previewLead,
                                substituteVariables = substituteVariables,
                                onToggleSubstitute = { substituteVariables = it },
                                onSelectPreviewLead = { previewLead = it },
                                onOpenAiChallenger = { showAiChallengerSheet = true },
                                onApplyToDrafter = { variant ->
                                    onApplyToDrafter(currentExp, variant, previewLead)
                                }
                            )
                            1 -> PerformanceWinnerAnalyticsTab(
                                experiment = currentExp,
                                isSimulatingCohort = isSimulatingCohort,
                                onSimulateCohort = { size, favored ->
                                    onSimulateCohort(currentExp.id, size, favored)
                                },
                                onResetMetrics = { onResetMetrics(currentExp.id) },
                                onDeclareWinner = { winner, reason ->
                                    onDeclareWinner(currentExp.id, winner, reason)
                                },
                                onPromoteWinner = { winner ->
                                    onPromoteWinner(currentExp.id, winner)
                                },
                                onApplyWinnerToDrafter = { winner ->
                                    onApplyToDrafter(currentExp, winner, previewLead)
                                }
                            )
                            2 -> HypothesisAndSettingsTab(
                                experiment = currentExp,
                                onSave = onSaveExperiment,
                                onDelete = { onDeleteExperiment(currentExp) }
                            )
                        }
                    }
                }
            }
        }

        // AI Challenger Generator Dialog / Sheet
        if (showAiChallengerSheet && currentExp != null) {
            AiChallengerGeneratorModal(
                originalExperiment = currentExp,
                isGenerating = isGeneratingChallenger,
                onGenerate = { challengerAngle, instruction, audience ->
                    onGenerateAiChallenger(
                        currentExp.variantASubjectLine,
                        currentExp.variantABody,
                        currentExp.variantAPsychologyAngle,
                        challengerAngle,
                        instruction,
                        audience
                    ) { generated ->
                        val updated = currentExp.copy(
                            variantBName = generated.variantName,
                            variantBPsychologyAngle = generated.psychologyAngle,
                            variantBSubjectLine = generated.subjectLine,
                            variantBSubjectAlt = generated.subjectAlternative,
                            variantBBody = generated.emailBody,
                            variantBSurveyHook = generated.surveyQuestion,
                            variantBFollowUpNudge = generated.followUpNudge,
                            hypothesis = generated.testingHypothesis.ifBlank { currentExp.hypothesis },
                            updatedAt = System.currentTimeMillis()
                        )
                        onSaveExperiment(updated)
                        showAiChallengerSheet = false
                    }
                },
                onDismiss = { showAiChallengerSheet = false }
            )
        }
    }
}

@Composable
private fun ABDialogTopHeader(
    currentExperiment: TemplateVariationExperiment?,
    experimentsCount: Int,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit
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
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "A/B Variation Testing Lab",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "$experimentsCount Experiments",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Draft two variations of the same email template to measure open rates & reply lift",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Button(
                onClick = onCreateNew,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("create_ab_test_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New A/B Test", style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_ab_dialog_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
    }
}

@Composable
private fun ExperimentCarouselBar(
    experiments: List<TemplateVariationExperiment>,
    selectedExp: TemplateVariationExperiment,
    onSelect: (TemplateVariationExperiment) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(experiments) { exp ->
            val isSelected = exp.id == selectedExp.id
            val hasWinner = exp.effectiveWinner != null
            val statusColor = when (exp.status) {
                ExperimentStatus.COMPLETED -> Color(0xFF2E7D32)
                ExperimentStatus.ACTIVE_TESTING -> MaterialTheme.colorScheme.primary
                ExperimentStatus.DRAFTING -> MaterialTheme.colorScheme.outline
            }

            Surface(
                onClick = { onSelect(exp) },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                modifier = Modifier.testTag("experiment_chip_${exp.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = exp.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            if (hasWinner) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("👑", fontSize = 10.sp)
                            }
                        }
                        Text(
                            text = "${exp.totalSent} sends • Diff: ${if (exp.replyRateDiff >= 0) "+" else ""}${"%.1f".format(exp.replyRateDiff)}% reply",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DualVariationDrafterTab(
    experiment: TemplateVariationExperiment,
    leads: List<BusinessLead>,
    previewLead: BusinessLead?,
    substituteVariables: Boolean,
    onToggleSubstitute: (Boolean) -> Unit,
    onSelectPreviewLead: (BusinessLead) -> Unit,
    onOpenAiChallenger: () -> Unit,
    onApplyToDrafter: (variant: String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Variable Substitution Controls Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = substituteVariables,
                        onClick = { onToggleSubstitute(!substituteVariables) },
                        label = {
                            Text(if (substituteVariables) "Live Prospect Preview (Substituted)" else "Raw Template Tags ({{tags}})")
                        },
                        leadingIcon = {
                            Icon(
                                if (substituteVariables) Icons.Default.Visibility else Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }

                if (substituteVariables && leads.isNotEmpty()) {
                    var leadMenuExpanded by remember { mutableStateOf(false) }
                    Box {
                        OutlinedButton(
                            onClick = { leadMenuExpanded = true },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Preview: ${previewLead?.companyName ?: "Select Lead"}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        DropdownMenu(
                            expanded = leadMenuExpanded,
                            onDismissRequest = { leadMenuExpanded = false }
                        ) {
                            leads.forEach { lead ->
                                DropdownMenuItem(
                                    text = { Text("${lead.companyName} (${lead.contactName})") },
                                    onClick = {
                                        onSelectPreviewLead(lead)
                                        leadMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Variation Cards (Side-by-Side or Responsive Stack)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Variation A (Control)
            VariationDisplayCard(
                modifier = Modifier.weight(1f),
                variantLabel = experiment.variantAName,
                isControl = true,
                psychologyAngle = experiment.variantAPsychologyAngle,
                rawSubject = experiment.variantASubjectLine,
                rawAltSubject = experiment.variantASubjectAlt,
                rawSurveyHook = experiment.variantASurveyHook,
                rawBody = experiment.variantABody,
                rawFollowUp = experiment.variantAFollowUpNudge,
                previewLead = if (substituteVariables) previewLead else null,
                metricsSummary = "${experiment.variantAPerfSent} sent • ${"%.1f".format(experiment.openRateA)}% open • ${"%.1f".format(experiment.replyRateA)}% reply",
                isWinner = experiment.effectiveWinner == "A",
                onAiDraftClick = null,
                onApplyClick = { onApplyToDrafter("A") }
            )

            // Variation B (Challenger)
            VariationDisplayCard(
                modifier = Modifier.weight(1f),
                variantLabel = experiment.variantBName,
                isControl = false,
                psychologyAngle = experiment.variantBPsychologyAngle,
                rawSubject = experiment.variantBSubjectLine,
                rawAltSubject = experiment.variantBSubjectAlt,
                rawSurveyHook = experiment.variantBSurveyHook,
                rawBody = experiment.variantBBody,
                rawFollowUp = experiment.variantBFollowUpNudge,
                previewLead = if (substituteVariables) previewLead else null,
                metricsSummary = "${experiment.variantBPerfSent} sent • ${"%.1f".format(experiment.openRateB)}% open • ${"%.1f".format(experiment.replyRateB)}% reply",
                isWinner = experiment.effectiveWinner == "B",
                onAiDraftClick = onOpenAiChallenger,
                onApplyClick = { onApplyToDrafter("B") }
            )
        }
    }
}

@Composable
private fun VariationDisplayCard(
    modifier: Modifier = Modifier,
    variantLabel: String,
    isControl: Boolean,
    psychologyAngle: PsychologyAngle,
    rawSubject: String,
    rawAltSubject: String,
    rawSurveyHook: String,
    rawBody: String,
    rawFollowUp: String,
    previewLead: BusinessLead?,
    metricsSummary: String,
    isWinner: Boolean,
    onAiDraftClick: (() -> Unit)?,
    onApplyClick: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    val finalSubject = if (previewLead != null) substituteLeadVars(rawSubject, previewLead) else rawSubject
    val finalAltSubject = if (previewLead != null) substituteLeadVars(rawAltSubject, previewLead) else rawAltSubject
    val finalSurvey = if (previewLead != null) substituteLeadVars(rawSurveyHook, previewLead) else rawSurveyHook
    val finalBody = if (previewLead != null) substituteLeadVars(rawBody, previewLead) else rawBody
    val finalFollowUp = if (previewLead != null) substituteLeadVars(rawFollowUp, previewLead) else rawFollowUp

    val wordCount = finalBody.split("\\s+".toRegex()).count { it.isNotBlank() }
    val readTimeSec = ((wordCount / 200f) * 60f).roundToInt().coerceAtLeast(5)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWinner) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
        ),
        border = if (isWinner) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (isControl) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isControl) "CONTROL (A)" else "CHALLENGER (B)",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isControl) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    if (isWinner) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "🏆 WINNER",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (onAiDraftClick != null) {
                    OutlinedButton(
                        onClick = onAiDraftClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("AI Re-Draft", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = variantLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Psychology Angle Pill
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "🧠 ${psychologyAngle.title}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$wordCount words • ~${readTimeSec}s read",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Subject Line Section
            Text(
                text = "SUBJECT LINE:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = finalSubject,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { clipboardManager.setText(AnnotatedString(finalSubject)) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    }
                }
            }

            if (finalAltSubject.isNotBlank()) {
                Text(
                    text = "A/B Subject Variant: $finalAltSubject",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
                )
            }

            // Diagnostic Micro-Survey Hook (if present)
            if (finalSurvey.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DIAGNOSTIC MICRO-QUESTION:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = finalSurvey,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }

            // Email Body
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "EMAIL BODY:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = finalBody,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Follow-up Nudge (if present)
            if (finalFollowUp.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "DAY+3 FOLLOW-UP NUDGE:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = finalFollowUp,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = metricsSummary,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onApplyClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Apply to Drafter", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun PerformanceWinnerAnalyticsTab(
    experiment: TemplateVariationExperiment,
    isSimulatingCohort: Boolean,
    onSimulateCohort: (size: Int, favored: String) -> Unit,
    onResetMetrics: () -> Unit,
    onDeclareWinner: (winner: String, reason: String) -> Unit,
    onPromoteWinner: (winner: String) -> Unit,
    onApplyWinnerToDrafter: (winner: String) -> Unit
) {
    val scrollState = rememberScrollState()
    val winner = experiment.effectiveWinner

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Winner Announcement Banner
        WinnerHeroBanner(experiment = experiment)

        Spacer(modifier = Modifier.height(16.dp))

        // Side-by-Side Comparison Metric Grid
        Text(
            text = "Conversion Performance Metrics (Variation A vs Variation B)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricComparisonCard(
                modifier = Modifier.weight(1f),
                title = "Total Prospect Sends",
                valueA = "${experiment.variantAPerfSent}",
                valueB = "${experiment.variantBPerfSent}",
                delta = if (experiment.totalSent > 0) "${experiment.totalSent} total" else "0",
                isHigherBetter = false,
                winningVariant = null
            )
            MetricComparisonCard(
                modifier = Modifier.weight(1f),
                title = "Open Rate %",
                valueA = "${"%.1f".format(experiment.openRateA)}%",
                valueB = "${"%.1f".format(experiment.openRateB)}%",
                delta = "${if (experiment.openRateDiff >= 0) "+" else ""}${"%.1f".format(experiment.openRateDiff)}%",
                isHigherBetter = true,
                winningVariant = if (experiment.openRateDiff > 3f) "B" else if (experiment.openRateDiff < -3f) "A" else null
            )
            MetricComparisonCard(
                modifier = Modifier.weight(1f),
                title = "Click-Through %",
                valueA = "${"%.1f".format(experiment.clickRateA)}%",
                valueB = "${"%.1f".format(experiment.clickRateB)}%",
                delta = "${if (experiment.clickRateDiff >= 0) "+" else ""}${"%.1f".format(experiment.clickRateDiff)}%",
                isHigherBetter = true,
                winningVariant = if (experiment.clickRateDiff > 2f) "B" else if (experiment.clickRateDiff < -2f) "A" else null
            )
            MetricComparisonCard(
                modifier = Modifier.weight(1f),
                title = "Reply Rate % (Key KPI)",
                valueA = "${"%.1f".format(experiment.replyRateA)}%",
                valueB = "${"%.1f".format(experiment.replyRateB)}%",
                delta = "${if (experiment.replyRateDiff >= 0) "+" else ""}${"%.1f".format(experiment.replyRateDiff)}%",
                isHigherBetter = true,
                winningVariant = if (experiment.replyRateDiff > 2f) "B" else if (experiment.replyRateDiff < -2f) "A" else null
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Visual Conversion Funnel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Engagement Funnel Visualizer",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                FunnelStepBar(
                    stage = "Delivered / Sent",
                    countA = experiment.variantAPerfSent,
                    countB = experiment.variantBPerfSent,
                    maxVal = (experiment.variantAPerfSent.coerceAtLeast(experiment.variantBPerfSent)).coerceAtLeast(1)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FunnelStepBar(
                    stage = "Opened",
                    countA = experiment.variantAPerfOpened,
                    countB = experiment.variantBPerfOpened,
                    maxVal = (experiment.variantAPerfSent.coerceAtLeast(experiment.variantBPerfSent)).coerceAtLeast(1)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FunnelStepBar(
                    stage = "Clicked / Action",
                    countA = experiment.variantAPerfClicked,
                    countB = experiment.variantBPerfClicked,
                    maxVal = (experiment.variantAPerfSent.coerceAtLeast(experiment.variantBPerfSent)).coerceAtLeast(1)
                )
                Spacer(modifier = Modifier.height(8.dp))
                FunnelStepBar(
                    stage = "Replied (Conversion)",
                    countA = experiment.variantAPerfReplied,
                    countB = experiment.variantBPerfReplied,
                    maxVal = (experiment.variantAPerfSent.coerceAtLeast(experiment.variantBPerfSent)).coerceAtLeast(1)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Behavioral Rationale Box
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Behavioral Conversion Analysis",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (winner == "B") {
                            "Variation B outperforms Variation A because its ${experiment.variantBPsychologyAngle.title} framework reduces prospect cognitive friction. By avoiding heavy sales paragraphs upfront and centering a diagnostic micro-survey, response hesitation drops dramatically."
                        } else if (winner == "A") {
                            "Variation A (Control) is holding the lead. Its direct ${experiment.variantAPsychologyAngle.title} hook created immediate urgency for this target audience segment."
                        } else {
                            "Data is gathering. We recommend collecting at least 20-30 sends per variant to establish >90% statistical significance before promoting a winning template."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Cohort Testing & Winner Promotion Action Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "A/B Testing Actions & Simulation Lab",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Simulate real cohort sends or promote proven winning copy to your Master Template Library",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Simulate Cohort Button
                    Button(
                        onClick = { onSimulateCohort(20, "B") },
                        enabled = !isSimulatingCohort,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("simulate_cohort_btn")
                    ) {
                        if (isSimulatingCohort) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulate 20 Cohort Sends")
                    }

                    // Promote Winner Button
                    if (winner != null) {
                        Button(
                            onClick = { onPromoteWinner(winner) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            modifier = Modifier.weight(1f).testTag("promote_winner_btn")
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Promote Winner to Library")
                        }
                    }

                    // Reset Data Button
                    OutlinedButton(
                        onClick = onResetMetrics,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }

                if (winner != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onApplyWinnerToDrafter(winner) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("🚀 Apply Winner ($winner) Directly to Active Drafter")
                    }
                }
            }
        }
    }
}

@Composable
private fun WinnerHeroBanner(experiment: TemplateVariationExperiment) {
    val winner = experiment.effectiveWinner
    val isB = winner == "B"
    val isA = winner == "A"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (winner != null) Color(0xFF1B5E20).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (winner != null) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2E7D32)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (winner != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (winner != null) "👑" else "⚖️",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (winner != null) "Winning Variation: ${if (isB) experiment.variantBName else experiment.variantAName}" else "Active A/B Test in Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (winner != null) Color(0xFF1B5E20) else MaterialTheme.colorScheme.onSurface
                    )
                    if (winner != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = Color(0xFF2E7D32),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "${experiment.statisticalConfidence}% Confident",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = experiment.winnerSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricComparisonCard(
    modifier: Modifier = Modifier,
    title: String,
    valueA: String,
    valueB: String,
    delta: String,
    isHigherBetter: Boolean,
    winningVariant: String?
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Variant A", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(valueA, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                }

                Text("vs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Variant B", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        if (winningVariant == "B") {
                            Text(" 👑", fontSize = 10.sp)
                        }
                    }
                    Text(
                        text = valueB,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (winningVariant == "B") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = if (winningVariant == "B") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "Delta: $delta",
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (winningVariant == "B") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FunnelStepBar(
    stage: String,
    countA: Int,
    countB: Int,
    maxVal: Int
) {
    val progressA = (countA.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)
    val progressB = (countB.toFloat() / maxVal.toFloat()).coerceIn(0.05f, 1f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(stage, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "A: $countA vs B: $countB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Variant A bar
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { progressA },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text("Control (A)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
            }

            // Variant B bar
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { progressB },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (countB >= countA) Color(0xFF2E7D32) else MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text("Challenger (B)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
private fun HypothesisAndSettingsTab(
    experiment: TemplateVariationExperiment,
    onSave: (TemplateVariationExperiment) -> Unit,
    onDelete: () -> Unit
) {
    var title by remember(experiment) { mutableStateOf(experiment.title) }
    var hypothesis by remember(experiment) { mutableStateOf(experiment.hypothesis) }
    var audience by remember(experiment) { mutableStateOf(experiment.targetAudience) }
    var testedElement by remember(experiment) { mutableStateOf(experiment.testedElement) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "A/B Experiment Configuration",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
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
                        value = audience,
                        onValueChange = { audience = it },
                        label = { Text("Target Audience Segment") },
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = testedElement,
                        onValueChange = { testedElement = it },
                        label = { Text("Tested Variable Element") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Test")
                    }

                    Button(
                        onClick = {
                            onSave(
                                experiment.copy(
                                    title = title.trim(),
                                    hypothesis = hypothesis.trim(),
                                    targetAudience = audience.trim(),
                                    testedElement = testedElement.trim()
                                )
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Experiment Settings")
                    }
                }
            }
        }
    }
}

@Composable
private fun AiChallengerGeneratorModal(
    originalExperiment: TemplateVariationExperiment,
    isGenerating: Boolean,
    onGenerate: (challengerAngle: PsychologyAngle, instruction: String, audience: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedChallengerAngle by remember { mutableStateOf(PsychologyAngle.SURVEY_HOOK) }
    var instruction by remember { mutableStateOf("Draft a radical brevity version focusing on 1 diagnostic question to cut cognitive friction.") }
    var audience by remember { mutableStateOf(originalExperiment.targetAudience) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Challenger Architect",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Text(
                    text = "Let Gemini craft a distinct behavioral variation to challenge Variation A (Control).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Challenger Behavioral Framework:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                PsychologyAngle.entries.forEach { angle ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedChallengerAngle = angle }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedChallengerAngle == angle,
                            onClick = { selectedChallengerAngle = angle }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(angle.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text(angle.subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = instruction,
                    onValueChange = { instruction = it },
                    label = { Text("Specific Challenger Copy Strategy") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onGenerate(selectedChallengerAngle, instruction, audience) },
                        enabled = !isGenerating,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generating Variation B...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Challenger")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyABExperimentsView(onCreateNew: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No A/B Experiments Created Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Create an A/B test to draft two distinct variations of your email copy and scientifically measure which converts higher.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onCreateNew,
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create First A/B Experiment")
        }
    }
}

private fun substituteLeadVars(text: String, lead: BusinessLead): String {
    val firstName = lead.contactName.trim().split("\\s+".toRegex()).firstOrNull() ?: lead.contactName
    return text
        .replace("{{contactName}}", lead.contactName)
        .replace("{{firstName}}", firstName)
        .replace("{{companyName}}", lead.companyName)
        .replace("{{painPoint}}", lead.painPoint)
        .replace("{{targetGoal}}", lead.targetGoal)
        .replace("{{industry}}", lead.industry)
        .replace("{{role}}", lead.role)
        .replace("{{companySize}}", lead.companySize)
        .replace("{{website}}", lead.website.ifBlank { "${lead.companyName.lowercase().replace(" ", "")}.com" })
}
