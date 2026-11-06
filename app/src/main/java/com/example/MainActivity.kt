package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ABVariationComparisonDialog
import com.example.ui.components.AddEditLeadDialog
import com.example.ui.components.AddEditTemplateDialog
import com.example.ui.components.AiTemplateGenDialog
import com.example.ui.components.CreateABExperimentDialog
import com.example.ui.components.GoogleDiscoveryDialog
import com.example.ui.components.SheetImportDialog
import com.example.ui.components.TemplateLibraryDialog
import com.example.ui.components.VerificationDetailDialog
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.ChatbotScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DrafterScreen
import com.example.ui.screens.LeadsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.OutreachViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: OutreachViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                OutreachApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun OutreachApp(viewModel: OutreachViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val leads by viewModel.leads.collectAsStateWithLifecycle()
    val drafts by viewModel.drafts.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val analytics by viewModel.analytics.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val currentFilter by viewModel.currentFilter.collectAsStateWithLifecycle()

    val selectedLead by viewModel.selectedLead.collectAsStateWithLifecycle()
    val selectedAngle by viewModel.selectedAngle.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
    val activeDraft by viewModel.activeDraft.collectAsStateWithLifecycle()

    val verifyingLeadId by viewModel.verifyingLeadId.collectAsStateWithLifecycle()
    val verificationDialogResult by viewModel.verificationDialogResult.collectAsStateWithLifecycle()

    val showAddLeadDialog by viewModel.showAddLeadDialog.collectAsStateWithLifecycle()
    val editingLead by viewModel.editingLead.collectAsStateWithLifecycle()

    // Template Library Dialog States
    val showTemplateLibrary by viewModel.showTemplateLibrary.collectAsStateWithLifecycle()
    val templateSearchQuery by viewModel.templateSearchQuery.collectAsStateWithLifecycle()
    val selectedTemplateAngleFilter by viewModel.selectedTemplateAngleFilter.collectAsStateWithLifecycle()
    val selectedTemplateCategoryFilter by viewModel.selectedTemplateCategoryFilter.collectAsStateWithLifecycle()
    val previewingTemplate by viewModel.previewingTemplate.collectAsStateWithLifecycle()
    val editingTemplate by viewModel.editingTemplate.collectAsStateWithLifecycle()
    val showAddEditTemplateDialog by viewModel.showAddEditTemplateDialog.collectAsStateWithLifecycle()
    val showAiTemplateGenDialog by viewModel.showAiTemplateGenDialog.collectAsStateWithLifecycle()
    val isGeneratingAiTemplate by viewModel.isGeneratingAiTemplate.collectAsStateWithLifecycle()

    // Google Discovery & Sheet Dialog States
    val showDiscoveryDialog by viewModel.showDiscoveryDialog.collectAsStateWithLifecycle()
    val isDiscovering by viewModel.isDiscovering.collectAsStateWithLifecycle()
    val discoveredLeads by viewModel.discoveredLeads.collectAsStateWithLifecycle()

    val showSheetImportDialog by viewModel.showSheetImportDialog.collectAsStateWithLifecycle()
    val isEnrichingSheet by viewModel.isEnrichingSheet.collectAsStateWithLifecycle()
    val enrichedSheetContacts by viewModel.enrichedSheetContacts.collectAsStateWithLifecycle()

    // A/B Variation Testing States
    val experiments by viewModel.experiments.collectAsStateWithLifecycle()
    val showABExperimentDialog by viewModel.showABExperimentDialog.collectAsStateWithLifecycle()
    val selectedExperiment by viewModel.selectedExperiment.collectAsStateWithLifecycle()
    val showCreateExperimentDialog by viewModel.showCreateExperimentDialog.collectAsStateWithLifecycle()
    val isGeneratingChallenger by viewModel.isGeneratingChallenger.collectAsStateWithLifecycle()
    val isSimulatingCohort by viewModel.isSimulatingCohort.collectAsStateWithLifecycle()

    // Chatbot States
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val selectedChatRole by viewModel.selectedChatRole.collectAsStateWithLifecycle()
    val selectedChatModel by viewModel.selectedChatModel.collectAsStateWithLifecycle()
    val isChatGenerating by viewModel.isChatGenerating.collectAsStateWithLifecycle()

    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                // Tab 0: Dashboard
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = {
                        Icon(
                            if (selectedTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard", fontSize = 10.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )

                // Tab 1: Leads
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (leads.isNotEmpty()) {
                                    Badge { Text("${leads.size}") }
                                }
                            }
                        ) {
                            Icon(
                                if (selectedTab == 1) Icons.Default.Business else Icons.Outlined.Business,
                                contentDescription = "Leads"
                            )
                        }
                    },
                    label = { Text("Leads", fontSize = 10.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_leads")
                )

                // Tab 2: AI Drafter
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = {
                        Icon(
                            if (selectedTab == 2) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Drafter"
                        )
                    },
                    label = { Text("AI Drafter", fontSize = 10.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_drafter")
                )

                // Tab 3: AI Chatbot
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    icon = {
                        Icon(
                            if (selectedTab == 3) Icons.Default.Chat else Icons.Outlined.Chat,
                            contentDescription = "Chatbot"
                        )
                    },
                    label = { Text("AI Advisor", fontSize = 10.sp, fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_chatbot")
                )

                // Tab 4: Alerts
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.setSelectedTab(4) },
                    icon = {
                        val unreadCount = alerts.count { !it.isRead }
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge { Text("$unreadCount") }
                                }
                            }
                        ) {
                            Icon(
                                if (selectedTab == 4) Icons.Default.Notifications else Icons.Outlined.Notifications,
                                contentDescription = "Alerts"
                            )
                        }
                    },
                    label = { Text("Alerts", fontSize = 10.sp, fontWeight = if (selectedTab == 4) FontWeight.Bold else FontWeight.Normal) },
                    modifier = Modifier.testTag("nav_tab_alerts")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    analytics = analytics,
                    recentAlerts = alerts,
                    onAddLeadClick = { viewModel.openAddLeadDialog() },
                    onNavigateToLeads = { viewModel.setSelectedTab(1) },
                    onNavigateToDrafter = { viewModel.setSelectedTab(2) },
                    onNavigateToAlerts = { viewModel.setSelectedTab(4) },
                    onOpenABTesting = { viewModel.openABExperimentDialog() }
                )
                1 -> LeadsScreen(
                    leads = leads,
                    searchQuery = searchQuery,
                    currentFilter = currentFilter,
                    verifyingLeadId = verifyingLeadId,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onFilterChange = { viewModel.setFilter(it) },
                    onAddLeadClick = { viewModel.openAddLeadDialog() },
                    onGoogleDiscoveryClick = { viewModel.openDiscoveryDialog() },
                    onGoogleSheetImportClick = { viewModel.openSheetImportDialog() },
                    onEditLeadClick = { viewModel.openAddLeadDialog(it) },
                    onDeleteLeadClick = { viewModel.deleteLead(it) },
                    onVerifyLeadClick = { viewModel.verifyLead(it) },
                    onDraftMailClick = { viewModel.selectLeadForDrafter(it) }
                )
                2 -> DrafterScreen(
                    leads = leads,
                    templates = templates,
                    selectedLead = selectedLead ?: leads.firstOrNull(),
                    selectedAngle = selectedAngle,
                    activeDraft = activeDraft,
                    isGenerating = isGenerating,
                    onSelectLead = { viewModel.selectLeadForDrafter(it) },
                    onSelectAngle = { viewModel.setSelectedAngle(it) },
                    onSelectTemplate = { viewModel.applyTemplateToCampaign(it) },
                    onOpenTemplateLibrary = { viewModel.openTemplateLibrary() },
                    onGenerateClick = { viewModel.generateOutreachDraft() },
                    onSaveAsTemplate = { draft, title ->
                        viewModel.saveDraftAsTemplate(draft, title)
                    },
                    onSendEmailClick = { draft, ctx, launchExternal ->
                        viewModel.sendOutreachEmail(draft, ctx, launchExternal)
                    },
                    onSimulateStatus = { draftId, status ->
                        viewModel.simulateStatusChange(draftId, status)
                    },
                    onOpenABTesting = { viewModel.openABExperimentDialog() }
                )
                3 -> ChatbotScreen(
                    messages = chatMessages,
                    selectedRole = selectedChatRole,
                    selectedModel = selectedChatModel,
                    isGenerating = isChatGenerating,
                    onRoleSelected = { viewModel.setChatRole(it) },
                    onModelSelected = { viewModel.setChatModel(it) },
                    onSendMessage = { viewModel.sendUserChatMessage(it) },
                    onClearChat = { viewModel.clearChatHistory() }
                )
                4 -> AlertsScreen(
                    alerts = alerts,
                    drafts = drafts,
                    onClearAlerts = { viewModel.clearAllAlerts() },
                    onSimulateStatus = { draftId, status ->
                        viewModel.simulateStatusChange(draftId, status)
                    }
                )
            }
        }

        // Add / Edit Lead Modal Dialog
        if (showAddLeadDialog) {
            AddEditLeadDialog(
                leadToEdit = editingLead,
                onDismiss = { viewModel.closeAddLeadDialog() },
                onSave = { companyName, contactName, email, role, industry, companySize, painPoint, targetGoal, website, preferredAngle ->
                    viewModel.saveLead(
                        companyName,
                        contactName,
                        email,
                        role,
                        industry,
                        companySize,
                        painPoint,
                        targetGoal,
                        website,
                        preferredAngle
                    )
                }
            )
        }

        // Google Business Lead Finder Dialog
        if (showDiscoveryDialog) {
            GoogleDiscoveryDialog(
                isOpen = showDiscoveryDialog,
                isDiscovering = isDiscovering,
                discoveredLeads = discoveredLeads,
                onDismiss = { viewModel.closeDiscoveryDialog() },
                onSearch = { q, loc, ind -> viewModel.discoverGoogleLeads(q, loc, ind) },
                onToggleSelect = { viewModel.toggleDiscoveredLeadSelection(it) },
                onToggleAll = { viewModel.toggleAllDiscoveredLeads(it) },
                onImportSelected = { viewModel.importSelectedDiscoveredLeads() }
            )
        }

        // Google Sheet & Email List Enrichment Dialog
        if (showSheetImportDialog) {
            SheetImportDialog(
                isOpen = showSheetImportDialog,
                isEnriching = isEnrichingSheet,
                enrichedContacts = enrichedSheetContacts,
                onDismiss = { viewModel.closeSheetImportDialog() },
                onEnrichContent = { viewModel.enrichSheetOrEmailList(it) },
                onToggleSelect = { viewModel.toggleSheetContactSelection(it) },
                onToggleAll = { viewModel.toggleAllSheetContacts(it) },
                onImportSelected = { viewModel.importSelectedSheetContacts() }
            )
        }

        // Email Verification Detail Dialog
        verificationDialogResult?.let { (lead, result) ->
            VerificationDetailDialog(
                lead = lead,
                result = result,
                onDismiss = { viewModel.closeVerificationDialog() },
                onDraftOutreach = {
                    viewModel.selectLeadForDrafter(lead)
                }
            )
        }

        // Template Library Dialog
        if (showTemplateLibrary) {
            TemplateLibraryDialog(
                templates = templates,
                leads = leads,
                selectedLead = selectedLead ?: leads.firstOrNull(),
                searchQuery = templateSearchQuery,
                selectedAngleFilter = selectedTemplateAngleFilter,
                selectedCategoryFilter = selectedTemplateCategoryFilter,
                previewingTemplate = previewingTemplate,
                onDismiss = { viewModel.closeTemplateLibrary() },
                onSearchChange = { viewModel.setTemplateSearchQuery(it) },
                onAngleFilterChange = { viewModel.setTemplateAngleFilter(it) },
                onCategoryFilterChange = { viewModel.setTemplateCategoryFilter(it) },
                onOpenPreview = { viewModel.openPreviewTemplate(it) },
                onClosePreview = { viewModel.closePreviewTemplate() },
                onOpenCreateTemplate = { viewModel.openAddEditTemplateDialog(null) },
                onOpenEditTemplate = { viewModel.openAddEditTemplateDialog(it) },
                onOpenAiGenerate = { viewModel.openAiTemplateGenDialog() },
                onDuplicateTemplate = { viewModel.duplicateTemplate(it) },
                onDeleteTemplate = { viewModel.deleteTemplate(it) },
                onApplyTemplate = { template, lead ->
                    viewModel.applyTemplateToCampaign(template, lead)
                }
            )
        }

        // Add / Edit Template Form Dialog
        if (showAddEditTemplateDialog) {
            AddEditTemplateDialog(
                templateToEdit = editingTemplate,
                onDismiss = { viewModel.closeAddEditTemplateDialog() },
                onSave = { title, category, psychologyAngle, subjectLineTemplate, subjectAlternativeTemplate, bodyTemplate, surveyQuestionTemplate, followUpNudgeTemplate, psychologyInsight ->
                    viewModel.saveTemplate(
                        title = title,
                        category = category,
                        psychologyAngle = psychologyAngle,
                        subjectLineTemplate = subjectLineTemplate,
                        subjectAlternativeTemplate = subjectAlternativeTemplate,
                        bodyTemplate = bodyTemplate,
                        surveyQuestionTemplate = surveyQuestionTemplate,
                        followUpNudgeTemplate = followUpNudgeTemplate,
                        psychologyInsight = psychologyInsight
                    )
                }
            )
        }

        // AI Custom Psychology Template Generator Dialog
        if (showAiTemplateGenDialog) {
            AiTemplateGenDialog(
                isGenerating = isGeneratingAiTemplate,
                onDismiss = { viewModel.closeAiTemplateGenDialog() },
                onGenerate = { prompt, angle, industryOrTone ->
                    viewModel.generateAiTemplate(
                        prompt = prompt,
                        angle = angle,
                        industryOrTone = industryOrTone
                    )
                }
            )
        }

        // A/B Template Variation Comparison & Experiment Dialog
        if (showABExperimentDialog) {
            ABVariationComparisonDialog(
                experiments = experiments,
                selectedExperiment = selectedExperiment,
                leads = leads,
                activeLead = selectedLead,
                isGeneratingChallenger = isGeneratingChallenger,
                isSimulatingCohort = isSimulatingCohort,
                onSelectExperiment = { viewModel.selectExperiment(it) },
                onCreateNewExperimentClick = { viewModel.openCreateExperimentDialog() },
                onDeleteExperiment = { viewModel.deleteExperiment(it) },
                onGenerateAiChallenger = { origSubj, origBody, origAngle, challengerAngle, instruction, audience, onResult ->
                    viewModel.generateChallengerWithAi(
                        originalSubject = origSubj,
                        originalBody = origBody,
                        originalAngle = origAngle,
                        challengerAngle = challengerAngle,
                        strategyInstruction = instruction,
                        targetAudience = audience,
                        onGenerated = onResult
                    )
                },
                onSaveExperiment = { viewModel.saveOrUpdateExperiment(it) },
                onSimulateCohort = { expId, sampleSize, favored ->
                    viewModel.simulateCohortTest(expId, sampleSize, favored)
                },
                onResetMetrics = { viewModel.resetExperimentMetrics(it) },
                onDeclareWinner = { expId, winner, reason ->
                    viewModel.declareExperimentWinner(expId, winner, reason)
                },
                onPromoteWinner = { expId, winner ->
                    viewModel.promoteWinnerToTemplate(expId, winner)
                },
                onApplyToDrafter = { exp, variant, lead ->
                    viewModel.applyVariantToDrafter(exp, variant, lead)
                },
                onDismiss = { viewModel.closeABExperimentDialog() }
            )
        }

        // Create New A/B Experiment Dialog
        if (showCreateExperimentDialog) {
            CreateABExperimentDialog(
                templates = templates,
                isGeneratingChallenger = isGeneratingChallenger,
                onGenerateAiChallenger = { origSubj, origBody, origAngle, challengerAngle, instruction, audience, onResult ->
                    viewModel.generateChallengerWithAi(
                        originalSubject = origSubj,
                        originalBody = origBody,
                        originalAngle = origAngle,
                        challengerAngle = challengerAngle,
                        strategyInstruction = instruction,
                        targetAudience = audience,
                        onGenerated = onResult
                    )
                },
                onSaveExperiment = { newExp ->
                    viewModel.saveOrUpdateExperiment(newExp)
                },
                onDismiss = { viewModel.closeCreateExperimentDialog() }
            )
        }
    }
}
