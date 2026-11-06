package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BusinessLead
import com.example.data.model.EmailDraft
import com.example.data.model.OutreachAlert
import com.example.data.model.OutreachAnalytics
import com.example.data.model.OutreachStatus
import com.example.data.model.PsychologyAngle
import com.example.data.model.VerificationStatus
import com.example.data.repository.OutreachRepository
import com.example.data.verification.EmailVerificationService
import com.example.data.verification.EmailVerifier
import com.example.data.verification.VerificationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class LeadFilter {
    ALL,
    VERIFIED,
    RISKY,
    DRAFTED,
    SENT,
    REPLIED
}

class OutreachViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    private val repository = OutreachRepository(
        leadDao = database.leadDao(),
        draftDao = database.draftDao(),
        alertDao = database.alertDao(),
        templateDao = database.templateDao(),
        experimentDao = database.experimentDao()
    )

    val leads: StateFlow<List<BusinessLead>> = repository.allLeads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val drafts: StateFlow<List<EmailDraft>> = repository.allDrafts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<OutreachAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<com.example.data.model.OutreachTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val experiments: StateFlow<List<com.example.data.model.TemplateVariationExperiment>> = repository.allExperiments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val analytics: StateFlow<OutreachAnalytics> = repository.analytics
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            OutreachAnalytics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0f, 0f, 0f, 0f)
        )

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentFilter = MutableStateFlow(LeadFilter.ALL)
    val currentFilter: StateFlow<LeadFilter> = _currentFilter.asStateFlow()

    // Drafter state
    private val _selectedLead = MutableStateFlow<BusinessLead?>(null)
    val selectedLead: StateFlow<BusinessLead?> = _selectedLead.asStateFlow()

    private val _selectedAngle = MutableStateFlow(PsychologyAngle.SURVEY_HOOK)
    val selectedAngle: StateFlow<PsychologyAngle> = _selectedAngle.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _activeDraft = MutableStateFlow<EmailDraft?>(null)
    val activeDraft: StateFlow<EmailDraft?> = _activeDraft.asStateFlow()

    // Verification state
    private val _verifyingLeadId = MutableStateFlow<Long?>(null)
    val verifyingLeadId: StateFlow<Long?> = _verifyingLeadId.asStateFlow()

    private val _verificationDialogResult = MutableStateFlow<Pair<BusinessLead, VerificationResult>?>(null)
    val verificationDialogResult: StateFlow<Pair<BusinessLead, VerificationResult>?> = _verificationDialogResult.asStateFlow()

    // Add / Edit Lead Dialog state
    private val _showAddLeadDialog = MutableStateFlow(false)
    val showAddLeadDialog: StateFlow<Boolean> = _showAddLeadDialog.asStateFlow()

    private val _editingLead = MutableStateFlow<BusinessLead?>(null)
    val editingLead: StateFlow<BusinessLead?> = _editingLead.asStateFlow()

    // Google Business Discovery State (Google Maps & Search Grounding)
    private val _showDiscoveryDialog = MutableStateFlow(false)
    val showDiscoveryDialog: StateFlow<Boolean> = _showDiscoveryDialog.asStateFlow()

    private val _isDiscovering = MutableStateFlow(false)
    val isDiscovering: StateFlow<Boolean> = _isDiscovering.asStateFlow()

    private val _discoveredLeads = MutableStateFlow<List<com.example.data.model.DiscoveredBusinessLead>>(emptyList())
    val discoveredLeads: StateFlow<List<com.example.data.model.DiscoveredBusinessLead>> = _discoveredLeads.asStateFlow()

    // Google Sheet & Email List Context Enrichment State
    private val _showSheetImportDialog = MutableStateFlow(false)
    val showSheetImportDialog: StateFlow<Boolean> = _showSheetImportDialog.asStateFlow()

    private val _isEnrichingSheet = MutableStateFlow(false)
    val isEnrichingSheet: StateFlow<Boolean> = _isEnrichingSheet.asStateFlow()

    private val _enrichedSheetContacts = MutableStateFlow<List<com.example.data.model.EnrichedSheetContact>>(emptyList())
    val enrichedSheetContacts: StateFlow<List<com.example.data.model.EnrichedSheetContact>> = _enrichedSheetContacts.asStateFlow()

    // Chatbot State
    private val _selectedChatRole = MutableStateFlow(com.example.data.model.ChatbotRole.STRATEGIST)
    val selectedChatRole: StateFlow<com.example.data.model.ChatbotRole> = _selectedChatRole.asStateFlow()

    private val _selectedChatModel = MutableStateFlow("gemini-3.5-flash")
    val selectedChatModel: StateFlow<String> = _selectedChatModel.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<com.example.data.model.ChatMessage>>(
        listOf(
            com.example.data.model.ChatMessage(
                sender = com.example.data.model.ChatSender.ASSISTANT,
                text = "Welcome to your AI Cold Outreach & Market Strategy Hub. I can craft micro-survey hooks, research local businesses via Google Maps, perform deep pain-point discovery, or analyze CTR optimization. How can I accelerate your campaigns today?",
                role = "Outreach Strategist",
                groundingSources = listOf("Google Search & Maps Intelligence")
            )
        )
    )
    val chatMessages: StateFlow<List<com.example.data.model.ChatMessage>> = _chatMessages.asStateFlow()

    // Template Library State
    private val _showTemplateLibrary = MutableStateFlow(false)
    val showTemplateLibrary: StateFlow<Boolean> = _showTemplateLibrary.asStateFlow()

    private val _templateSearchQuery = MutableStateFlow("")
    val templateSearchQuery: StateFlow<String> = _templateSearchQuery.asStateFlow()

    private val _selectedTemplateAngleFilter = MutableStateFlow<PsychologyAngle?>(null)
    val selectedTemplateAngleFilter: StateFlow<PsychologyAngle?> = _selectedTemplateAngleFilter.asStateFlow()

    private val _selectedTemplateCategoryFilter = MutableStateFlow<String?>(null)
    val selectedTemplateCategoryFilter: StateFlow<String?> = _selectedTemplateCategoryFilter.asStateFlow()

    private val _previewingTemplate = MutableStateFlow<com.example.data.model.OutreachTemplate?>(null)
    val previewingTemplate: StateFlow<com.example.data.model.OutreachTemplate?> = _previewingTemplate.asStateFlow()

    private val _editingTemplate = MutableStateFlow<com.example.data.model.OutreachTemplate?>(null)
    val editingTemplate: StateFlow<com.example.data.model.OutreachTemplate?> = _editingTemplate.asStateFlow()

    private val _showAddEditTemplateDialog = MutableStateFlow(false)
    val showAddEditTemplateDialog: StateFlow<Boolean> = _showAddEditTemplateDialog.asStateFlow()

    private val _showAiTemplateGenDialog = MutableStateFlow(false)
    val showAiTemplateGenDialog: StateFlow<Boolean> = _showAiTemplateGenDialog.asStateFlow()

    private val _isGeneratingAiTemplate = MutableStateFlow(false)
    val isGeneratingAiTemplate: StateFlow<Boolean> = _isGeneratingAiTemplate.asStateFlow()

    // A/B Experiment Testing Lab State
    private val _showABExperimentDialog = MutableStateFlow(false)
    val showABExperimentDialog: StateFlow<Boolean> = _showABExperimentDialog.asStateFlow()

    private val _selectedExperiment = MutableStateFlow<com.example.data.model.TemplateVariationExperiment?>(null)
    val selectedExperiment: StateFlow<com.example.data.model.TemplateVariationExperiment?> = _selectedExperiment.asStateFlow()

    private val _showCreateExperimentDialog = MutableStateFlow(false)
    val showCreateExperimentDialog: StateFlow<Boolean> = _showCreateExperimentDialog.asStateFlow()

    private val _isGeneratingChallenger = MutableStateFlow(false)
    val isGeneratingChallenger: StateFlow<Boolean> = _isGeneratingChallenger.asStateFlow()

    private val _isSimulatingCohort = MutableStateFlow(false)
    val isSimulatingCohort: StateFlow<Boolean> = _isSimulatingCohort.asStateFlow()

    // Toast / User feedback message
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()


    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun setSelectedTab(tab: Int) {
        _selectedTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: LeadFilter) {
        _currentFilter.value = filter
    }

    fun selectLeadForDrafter(lead: BusinessLead) {
        _selectedLead.value = lead
        _selectedAngle.value = lead.preferredAngle
        _selectedTab.value = 2 // Switch to Drafter tab
        // Check if there is an existing draft for this lead
        viewModelScope.launch {
            val existing = drafts.value.firstOrNull { it.leadId == lead.id }
            if (existing != null) {
                _activeDraft.value = existing
            } else {
                _activeDraft.value = null
            }
        }
    }

    fun setSelectedAngle(angle: PsychologyAngle) {
        _selectedAngle.value = angle
    }

    fun openAddLeadDialog(leadToEdit: BusinessLead? = null) {
        _editingLead.value = leadToEdit
        _showAddLeadDialog.value = true
    }

    fun closeAddLeadDialog() {
        _showAddLeadDialog.value = false
        _editingLead.value = null
    }

    fun saveLead(
        companyName: String,
        contactName: String,
        email: String,
        role: String,
        industry: String,
        companySize: String,
        painPoint: String,
        targetGoal: String,
        website: String,
        preferredAngle: PsychologyAngle
    ) {
        viewModelScope.launch {
            val existing = _editingLead.value
            val leadToSave = existing?.copy(
                companyName = companyName,
                contactName = contactName,
                email = email,
                role = role,
                industry = industry,
                companySize = companySize,
                painPoint = painPoint,
                targetGoal = targetGoal,
                website = website,
                preferredAngle = preferredAngle
            ) ?: BusinessLead(
                companyName = companyName,
                contactName = contactName,
                email = email,
                role = role,
                industry = industry,
                companySize = companySize,
                painPoint = painPoint,
                targetGoal = targetGoal,
                website = website,
                preferredAngle = preferredAngle
            )

            val id = repository.saveLead(leadToSave)
            closeAddLeadDialog()
            _userMessage.value = "Saved lead for $companyName"

            // Auto-verify email
            verifyLead(leadToSave.copy(id = if (leadToSave.id == 0L) id else leadToSave.id))
        }
    }

    fun deleteLead(lead: BusinessLead) {
        viewModelScope.launch {
            repository.deleteLead(lead.id)
            if (_selectedLead.value?.id == lead.id) {
                _selectedLead.value = null
                _activeDraft.value = null
            }
            _userMessage.value = "Removed ${lead.companyName}"
        }
    }

    fun verifyLead(lead: BusinessLead) {
        viewModelScope.launch {
            _verifyingLeadId.value = lead.id
            val result = EmailVerifier.verify(lead.email, lead.companyName)
            val updated = repository.verifyLeadEmail(lead.id)
            _verifyingLeadId.value = null
            if (updated != null) {
                _verificationDialogResult.value = Pair(updated, result)
                if (_selectedLead.value?.id == lead.id) {
                    _selectedLead.value = updated
                }
            }
        }
    }

    fun closeVerificationDialog() {
        _verificationDialogResult.value = null
    }

    fun generateOutreachDraft(lead: BusinessLead? = _selectedLead.value, angle: PsychologyAngle = _selectedAngle.value) {
        val targetLead = lead ?: return
        
        // Validate prospect email format and domain before drafting
        val validation = EmailVerificationService.getInstance().validateProspectBeforeDrafting(targetLead)
        if (!validation.canProceed) {
            _userMessage.value = "Cannot draft email: ${validation.message}"
            return
        }

        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val draft = repository.generateAiDraft(targetLead, angle)
                _activeDraft.value = draft
                _selectedLead.value = targetLead
                _userMessage.value = "AI drafted personalized outreach for ${targetLead.companyName}"
            } catch (e: Exception) {
                _userMessage.value = "Drafting error: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun sendOutreachEmail(draft: EmailDraft, context: Context, launchExternalMailClient: Boolean = false) {
        viewModelScope.launch {
            repository.sendOutreach(draft.id)
            _userMessage.value = "Outreach dispatched to ${draft.recipientEmail}"

            if (launchExternalMailClient) {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${draft.recipientEmail}")
                        putExtra(Intent.EXTRA_SUBJECT, draft.subjectLine)
                        putExtra(Intent.EXTRA_TEXT, draft.emailBody)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(context, "No default email app found, draft sent in-app!", Toast.LENGTH_SHORT).show()
                }
            }

            // Reload active draft
            val updated = database.draftDao().getDraftById(draft.id)
            _activeDraft.value = updated
        }
    }

    fun simulateStatusChange(draftId: Long, nextStatus: OutreachStatus) {
        viewModelScope.launch {
            repository.simulateEngagementEvent(draftId, nextStatus)
            val updated = database.draftDao().getDraftById(draftId)
            _activeDraft.value = updated
            val statusLabel = when (nextStatus) {
                OutreachStatus.OPENED -> "Email Opened"
                OutreachStatus.CLICKED -> "Link Clicked (CTR +1)"
                OutreachStatus.REPLIED -> "Prospect Replied!"
                else -> nextStatus.name
            }
            _userMessage.value = "Simulated event: $statusLabel recorded"
        }
    }

    fun markAlertRead(alertId: Long) {
        viewModelScope.launch {
            repository.markAlertRead(alertId)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            repository.clearAlerts()
            _userMessage.value = "Activity alerts cleared"
        }
    }

    // Google Business Discovery Actions
    fun openDiscoveryDialog() {
        _showDiscoveryDialog.value = true
    }

    fun closeDiscoveryDialog() {
        _showDiscoveryDialog.value = false
    }

    fun discoverGoogleLeads(query: String, location: String, industry: String) {
        viewModelScope.launch {
            _isDiscovering.value = true
            try {
                val results = com.example.data.remote.GeminiOutreachService.discoverBusinessContacts(
                    query = query,
                    location = location,
                    industry = industry
                )
                _discoveredLeads.value = results
                _userMessage.value = "Discovered ${results.size} businesses from Google"
            } catch (e: Exception) {
                _userMessage.value = "Discovery failed: ${e.message}"
            } finally {
                _isDiscovering.value = false
            }
        }
    }

    fun toggleDiscoveredLeadSelection(id: String) {
        _discoveredLeads.value = _discoveredLeads.value.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun toggleAllDiscoveredLeads(selected: Boolean) {
        _discoveredLeads.value = _discoveredLeads.value.map { it.copy(isSelected = selected) }
    }

    fun importSelectedDiscoveredLeads() {
        viewModelScope.launch {
            val count = repository.importDiscoveredLeads(_discoveredLeads.value)
            _showDiscoveryDialog.value = false
            _discoveredLeads.value = emptyList()
            _userMessage.value = "Imported & verified $count businesses!"
        }
    }

    // Google Sheet & Email List Enrichment Actions
    fun openSheetImportDialog() {
        _showSheetImportDialog.value = true
    }

    fun closeSheetImportDialog() {
        _showSheetImportDialog.value = false
    }

    fun enrichSheetOrEmailList(rawContent: String) {
        if (rawContent.isBlank()) {
            _userMessage.value = "Please provide email list or sheet content"
            return
        }

        viewModelScope.launch {
            _isEnrichingSheet.value = true
            try {
                val enriched = com.example.data.remote.GeminiOutreachService.parseAndEnrichGoogleSheet(rawContent)
                _enrichedSheetContacts.value = enriched
                _userMessage.value = "Enriched ${enriched.size} contacts with Gemini context!"
            } catch (e: Exception) {
                _userMessage.value = "Enrichment error: ${e.message}"
            } finally {
                _isEnrichingSheet.value = false
            }
        }
    }

    fun toggleSheetContactSelection(id: String) {
        _enrichedSheetContacts.value = _enrichedSheetContacts.value.map {
            if (it.id == id) it.copy(isSelected = !it.isSelected) else it
        }
    }

    fun toggleAllSheetContacts(selected: Boolean) {
        _enrichedSheetContacts.value = _enrichedSheetContacts.value.map { it.copy(isSelected = selected) }
    }

    fun importSelectedSheetContacts() {
        viewModelScope.launch {
            val count = repository.importEnrichedSheetContacts(_enrichedSheetContacts.value)
            _showSheetImportDialog.value = false
            _enrichedSheetContacts.value = emptyList()
            _userMessage.value = "Successfully imported $count enriched leads!"
        }
    }

    // Chatbot Actions
    fun setChatRole(role: com.example.data.model.ChatbotRole) {
        _selectedChatRole.value = role
        _selectedChatModel.value = role.defaultModel
    }

    fun setChatModel(model: String) {
        _selectedChatModel.value = model
    }

    fun sendUserChatMessage(prompt: String) {
        if (prompt.isBlank()) return

        val userMessage = com.example.data.model.ChatMessage(
            sender = com.example.data.model.ChatSender.USER,
            text = prompt.trim()
        )
        val updatedHistory = _chatMessages.value + userMessage
        _chatMessages.value = updatedHistory

        viewModelScope.launch {
            _isChatGenerating.value = true
            try {
                val aiResponse = com.example.data.remote.GeminiOutreachService.sendChatMessage(
                    history = updatedHistory,
                    userPrompt = prompt.trim(),
                    role = _selectedChatRole.value,
                    modelOverride = _selectedChatModel.value
                )
                _chatMessages.value = _chatMessages.value + aiResponse
            } catch (e: Exception) {
                _chatMessages.value = _chatMessages.value + com.example.data.model.ChatMessage(
                    sender = com.example.data.model.ChatSender.ASSISTANT,
                    text = "Encountered an issue processing request: ${e.message}",
                    isError = true
                )
            } finally {
                _isChatGenerating.value = false
            }
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            com.example.data.model.ChatMessage(
                sender = com.example.data.model.ChatSender.ASSISTANT,
                text = "Chat history refreshed. I am ready to advise on your cold outreach campaigns, market intelligence, and email psychology angles.",
                role = _selectedChatRole.value.title
            )
        )
        _userMessage.value = "Chat thread cleared"
    }

    fun createLeadFromChatSuggestion(
        companyName: String,
        contactName: String,
        email: String,
        painPoint: String
    ) {
        val lead = BusinessLead(
            companyName = companyName,
            contactName = contactName,
            email = email,
            role = "Decision Maker",
            industry = "B2B",
            companySize = "20-50",
            painPoint = painPoint,
            targetGoal = "Scale automated outreach and solve operational bottlenecks",
            preferredAngle = PsychologyAngle.SURVEY_HOOK
        )
        openAddLeadDialog(lead)
    }

    // Template Library Actions
    fun openTemplateLibrary() {
        _showTemplateLibrary.value = true
    }

    fun closeTemplateLibrary() {
        _showTemplateLibrary.value = false
        _previewingTemplate.value = null
    }

    fun setTemplateSearchQuery(query: String) {
        _templateSearchQuery.value = query
    }

    fun setTemplateAngleFilter(angle: PsychologyAngle?) {
        _selectedTemplateAngleFilter.value = angle
    }

    fun setTemplateCategoryFilter(category: String?) {
        _selectedTemplateCategoryFilter.value = category
    }

    fun openPreviewTemplate(template: com.example.data.model.OutreachTemplate) {
        _previewingTemplate.value = template
    }

    fun closePreviewTemplate() {
        _previewingTemplate.value = null
    }

    fun openAddEditTemplateDialog(templateToEdit: com.example.data.model.OutreachTemplate? = null) {
        _editingTemplate.value = templateToEdit
        _showAddEditTemplateDialog.value = true
    }

    fun closeAddEditTemplateDialog() {
        _showAddEditTemplateDialog.value = false
        _editingTemplate.value = null
    }

    fun openAiTemplateGenDialog() {
        _showAiTemplateGenDialog.value = true
    }

    fun closeAiTemplateGenDialog() {
        _showAiTemplateGenDialog.value = false
    }

    fun saveTemplate(
        title: String,
        category: String,
        psychologyAngle: PsychologyAngle,
        subjectLineTemplate: String,
        subjectAlternativeTemplate: String,
        bodyTemplate: String,
        surveyQuestionTemplate: String,
        followUpNudgeTemplate: String,
        psychologyInsight: String,
        isCustom: Boolean = true
    ) {
        viewModelScope.launch {
            val existing = _editingTemplate.value
            val templateToSave = existing?.copy(
                title = title.trim(),
                category = category.trim().ifBlank { "Custom" },
                psychologyAngle = psychologyAngle,
                subjectLineTemplate = subjectLineTemplate.trim(),
                subjectAlternativeTemplate = subjectAlternativeTemplate.trim(),
                bodyTemplate = bodyTemplate.trim(),
                surveyQuestionTemplate = surveyQuestionTemplate.trim(),
                followUpNudgeTemplate = followUpNudgeTemplate.trim(),
                psychologyInsight = psychologyInsight.trim().ifBlank { psychologyAngle.psychologyHook },
                isCustom = isCustom
            ) ?: com.example.data.model.OutreachTemplate(
                title = title.trim(),
                category = category.trim().ifBlank { "Custom" },
                psychologyAngle = psychologyAngle,
                subjectLineTemplate = subjectLineTemplate.trim(),
                subjectAlternativeTemplate = subjectAlternativeTemplate.trim(),
                bodyTemplate = bodyTemplate.trim(),
                surveyQuestionTemplate = surveyQuestionTemplate.trim(),
                followUpNudgeTemplate = followUpNudgeTemplate.trim(),
                psychologyInsight = psychologyInsight.trim().ifBlank { psychologyAngle.psychologyHook },
                isCustom = isCustom
            )

            repository.saveTemplate(templateToSave)
            closeAddEditTemplateDialog()
            _userMessage.value = "Saved template: ${templateToSave.title}"
        }
    }

    fun deleteTemplate(template: com.example.data.model.OutreachTemplate) {
        viewModelScope.launch {
            repository.deleteTemplate(template.id)
            if (_previewingTemplate.value?.id == template.id) {
                _previewingTemplate.value = null
            }
            _userMessage.value = "Deleted template '${template.title}'"
        }
    }

    fun duplicateTemplate(template: com.example.data.model.OutreachTemplate) {
        viewModelScope.launch {
            val duplicate = template.copy(
                id = 0,
                title = "${template.title} (Copy)",
                category = "Custom",
                isCustom = true,
                usageCount = 0,
                createdAt = System.currentTimeMillis()
            )
            repository.saveTemplate(duplicate)
            _userMessage.value = "Duplicated to your custom template library!"
        }
    }

    fun applyTemplateToCampaign(
        template: com.example.data.model.OutreachTemplate,
        targetLead: BusinessLead? = null
    ) {
        viewModelScope.launch {
            val lead = targetLead ?: _selectedLead.value ?: leads.value.firstOrNull()
            if (lead == null) {
                _userMessage.value = "Please select or add a target prospect first"
                return@launch
            }

            _selectedLead.value = lead
            _selectedAngle.value = template.psychologyAngle
            val draft = repository.applyTemplateToLead(template, lead)
            _activeDraft.value = draft
            _showTemplateLibrary.value = false
            _previewingTemplate.value = null
            _selectedTab.value = 2 // Navigate to Drafter Screen
            _userMessage.value = "Applied '${template.title}' to ${lead.companyName}!"
        }
    }

    fun saveDraftAsTemplate(draft: EmailDraft, templateTitle: String) {
        viewModelScope.launch {
            val lead = leads.value.firstOrNull { it.id == draft.leadId }
            
            // Abstract lead-specific values to reusable placeholders if present
            var bodyTpl = draft.emailBody
            var subjectTpl = draft.subjectLine
            var subjectAltTpl = draft.subjectAlternative
            var surveyTpl = draft.surveyQuestion
            var followUpTpl = draft.followUpNudge

            if (lead != null) {
                bodyTpl = bodyTpl
                    .replace(lead.contactName, "{{contactName}}")
                    .replace(lead.companyName, "{{companyName}}")
                    .replace(lead.painPoint, "{{painPoint}}")
                    .replace(lead.targetGoal, "{{targetGoal}}")
                    .replace(lead.industry, "{{industry}}")
                subjectTpl = subjectTpl
                    .replace(lead.contactName, "{{contactName}}")
                    .replace(lead.companyName, "{{companyName}}")
                    .replace(lead.painPoint, "{{painPoint}}")
                subjectAltTpl = subjectAltTpl
                    .replace(lead.contactName, "{{contactName}}")
                    .replace(lead.companyName, "{{companyName}}")
                surveyTpl = surveyTpl
                    .replace(lead.painPoint, "{{painPoint}}")
                    .replace(lead.targetGoal, "{{targetGoal}}")
                followUpTpl = followUpTpl
                    .replace(lead.contactName, "{{contactName}}")
                    .replace(lead.companyName, "{{companyName}}")
            }

            val newTemplate = com.example.data.model.OutreachTemplate(
                title = templateTitle.ifBlank { "Custom ${draft.psychologyAngle.title}" },
                category = "Custom Saved",
                psychologyAngle = draft.psychologyAngle,
                subjectLineTemplate = subjectTpl,
                subjectAlternativeTemplate = subjectAltTpl,
                bodyTemplate = bodyTpl,
                surveyQuestionTemplate = surveyTpl,
                followUpNudgeTemplate = followUpTpl,
                psychologyInsight = draft.psychologyReasoning.ifBlank { draft.psychologyAngle.psychologyHook },
                isCustom = true,
                usageCount = 1
            )

            repository.saveTemplate(newTemplate)
            _userMessage.value = "Saved draft as reusable template '${newTemplate.title}'!"
        }
    }

    fun generateAiTemplate(
        prompt: String,
        angle: PsychologyAngle,
        industryOrTone: String
    ) {
        if (prompt.isBlank()) {
            _userMessage.value = "Please describe the outreach goal or scenario"
            return
        }

        viewModelScope.launch {
            _isGeneratingAiTemplate.value = true
            try {
                val generated = com.example.data.remote.GeminiOutreachService.generateCustomTemplateWithAi(
                    frameworkPrompt = prompt.trim(),
                    targetAngle = angle,
                    industryOrTone = industryOrTone.trim()
                )
                repository.saveTemplate(generated)
                closeAiTemplateGenDialog()
                _userMessage.value = "Generated and saved AI template: '${generated.title}'!"
            } catch (e: Exception) {
                _userMessage.value = "Template generation error: ${e.message}"
            } finally {
                _isGeneratingAiTemplate.value = false
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // A/B Experiment Actions
    fun openABExperimentDialog(experiment: com.example.data.model.TemplateVariationExperiment? = null) {
        _selectedExperiment.value = experiment ?: experiments.value.firstOrNull()
        _showABExperimentDialog.value = true
    }

    fun closeABExperimentDialog() {
        _showABExperimentDialog.value = false
    }

    fun selectExperiment(experiment: com.example.data.model.TemplateVariationExperiment) {
        _selectedExperiment.value = experiment
    }

    fun openCreateExperimentDialog(
        fromTemplate: com.example.data.model.OutreachTemplate? = null,
        fromDraft: EmailDraft? = null
    ) {
        _showCreateExperimentDialog.value = true
    }

    fun closeCreateExperimentDialog() {
        _showCreateExperimentDialog.value = false
    }

    fun saveOrUpdateExperiment(experiment: com.example.data.model.TemplateVariationExperiment) {
        viewModelScope.launch {
            if (experiment.id == 0L) {
                val newId = repository.insertExperiment(experiment)
                _selectedExperiment.value = experiment.copy(id = newId)
                _userMessage.value = "Created new A/B Experiment: '${experiment.title}'"
            } else {
                repository.updateExperiment(experiment)
                _selectedExperiment.value = experiment
                _userMessage.value = "Updated A/B Experiment '${experiment.title}'"
            }
            closeCreateExperimentDialog()
        }
    }

    fun deleteExperiment(experiment: com.example.data.model.TemplateVariationExperiment) {
        viewModelScope.launch {
            repository.deleteExperiment(experiment)
            if (_selectedExperiment.value?.id == experiment.id) {
                _selectedExperiment.value = experiments.value.firstOrNull { it.id != experiment.id }
            }
            _userMessage.value = "Deleted experiment '${experiment.title}'"
        }
    }

    fun generateChallengerWithAi(
        originalSubject: String,
        originalBody: String,
        originalAngle: PsychologyAngle,
        challengerAngle: PsychologyAngle,
        strategyInstruction: String,
        targetAudience: String,
        onGenerated: (com.example.data.remote.GeminiOutreachService.GeneratedVariant) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingChallenger.value = true
            try {
                val variant = com.example.data.remote.GeminiOutreachService.generateChallengerVariation(
                    originalSubject = originalSubject,
                    originalBody = originalBody,
                    originalAngle = originalAngle,
                    challengerAngle = challengerAngle,
                    strategyInstruction = strategyInstruction,
                    targetAudience = targetAudience
                )
                onGenerated(variant)
                _userMessage.value = "AI drafted Variation B challenger (${challengerAngle.title})!"
            } catch (e: Exception) {
                _userMessage.value = "Challenger generation error: ${e.message}"
            } finally {
                _isGeneratingChallenger.value = false
            }
        }
    }

    fun simulateCohortTest(experimentId: Long, sampleSize: Int = 20, favoredVariant: String = "B") {
        viewModelScope.launch {
            _isSimulatingCohort.value = true
            try {
                val updated = repository.simulateCohortTest(experimentId, sampleSize, favoredVariant)
                if (updated != null) {
                    _selectedExperiment.value = updated
                    _userMessage.value = "Simulated $sampleSize prospect cohort test! Metrics updated."
                }
            } catch (e: Exception) {
                _userMessage.value = "Cohort simulation error: ${e.message}"
            } finally {
                _isSimulatingCohort.value = false
            }
        }
    }

    fun recordManualEngagement(
        experimentId: Long,
        variant: String,
        sent: Int,
        opened: Int,
        clicked: Int,
        replied: Int
    ) {
        viewModelScope.launch {
            repository.recordVariantPerformance(experimentId, variant, sent, opened, clicked, replied)
            val updated = repository.getExperimentById(experimentId)
            _selectedExperiment.value = updated
            _userMessage.value = "Recorded engagement for Variant $variant"
        }
    }

    fun resetExperimentMetrics(experimentId: Long) {
        viewModelScope.launch {
            repository.resetExperimentMetrics(experimentId)
            val updated = repository.getExperimentById(experimentId)
            _selectedExperiment.value = updated
            _userMessage.value = "Reset experiment performance metrics"
        }
    }

    fun declareExperimentWinner(experimentId: Long, winner: String, customReason: String = "") {
        viewModelScope.launch {
            val reason = customReason.ifBlank { "Manually declared Variant $winner as winner based on performance." }
            repository.declareWinner(experimentId, winner, reason)
            val updated = repository.getExperimentById(experimentId)
            _selectedExperiment.value = updated
            _userMessage.value = "Declared Variant $winner as the winning variation!"
        }
    }

    fun promoteWinnerToTemplate(experimentId: Long, winningVariant: String) {
        viewModelScope.launch {
            try {
                val template = repository.promoteWinnerToTemplateLibrary(experimentId, winningVariant)
                val updated = repository.getExperimentById(experimentId)
                _selectedExperiment.value = updated
                _userMessage.value = "🏆 Promoted winning variation to Template Library: '${template.title}'!"
            } catch (e: Exception) {
                _userMessage.value = "Promotion error: ${e.message}"
            }
        }
    }

    fun applyVariantToDrafter(
        experiment: com.example.data.model.TemplateVariationExperiment,
        variant: String,
        lead: BusinessLead? = null
    ) {
        val targetLead = lead ?: selectedLead.value ?: leads.value.firstOrNull()
        val isA = variant == "A"
        val angle = if (isA) experiment.variantAPsychologyAngle else experiment.variantBPsychologyAngle
        val rawSubject = if (isA) experiment.variantASubjectLine else experiment.variantBSubjectLine
        val rawAltSubject = if (isA) experiment.variantASubjectAlt else experiment.variantBSubjectAlt
        val rawBody = if (isA) experiment.variantABody else experiment.variantBBody
        val rawSurvey = if (isA) experiment.variantASurveyHook else experiment.variantBSurveyHook
        val rawFollowUp = if (isA) experiment.variantAFollowUpNudge else experiment.variantBFollowUpNudge

        val subject = if (targetLead != null) repository.substituteTemplateVariables(rawSubject, targetLead) else rawSubject
        val altSubject = if (targetLead != null) repository.substituteTemplateVariables(rawAltSubject, targetLead) else rawAltSubject
        val body = if (targetLead != null) repository.substituteTemplateVariables(rawBody, targetLead) else rawBody
        val survey = if (targetLead != null) repository.substituteTemplateVariables(rawSurvey, targetLead) else rawSurvey
        val followUp = if (targetLead != null) repository.substituteTemplateVariables(rawFollowUp, targetLead) else rawFollowUp

        val draft = EmailDraft(
            leadId = targetLead?.id ?: 0L,
            companyName = targetLead?.companyName ?: "Sample Prospect",
            contactName = targetLead?.contactName ?: "Decision Maker",
            recipientEmail = targetLead?.email ?: "prospect@example.com",
            psychologyAngle = angle,
            subjectLine = subject,
            subjectAlternative = altSubject,
            emailBody = body,
            psychologyReasoning = "Applied from A/B Experiment '${experiment.title}' (Variant $variant - ${angle.title})",
            surveyQuestion = survey,
            followUpNudge = followUp,
            status = OutreachStatus.DRAFT
        )

        viewModelScope.launch {
            val insertedId = repository.saveDraft(draft)
            _activeDraft.value = draft.copy(id = insertedId)
            if (targetLead != null) {
                _selectedLead.value = targetLead
            }
            _selectedAngle.value = angle
            _selectedTab.value = 2 // Switch to Drafter tab
            closeABExperimentDialog()
            _userMessage.value = "Applied Variant $variant to Email Drafter!"
        }
    }
}


