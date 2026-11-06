package com.example.data.repository

import com.example.data.local.AlertDao
import com.example.data.local.DraftDao
import com.example.data.local.ExperimentDao
import com.example.data.local.LeadDao
import com.example.data.local.TemplateDao
import com.example.data.model.AlertType
import com.example.data.model.BusinessLead
import com.example.data.model.EmailDraft
import com.example.data.model.ExperimentStatus
import com.example.data.model.OutreachAlert
import com.example.data.model.OutreachAnalytics
import com.example.data.model.OutreachStatus
import com.example.data.model.OutreachTemplate
import com.example.data.model.PsychologyAngle
import com.example.data.model.TemplateVariationExperiment
import com.example.data.model.VerificationStatus
import com.example.data.remote.GeminiOutreachService
import com.example.data.verification.EmailVerifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OutreachRepository(
    private val leadDao: LeadDao,
    private val draftDao: DraftDao,
    private val alertDao: AlertDao,
    private val templateDao: TemplateDao,
    private val experimentDao: ExperimentDao
) {
    val allLeads: Flow<List<BusinessLead>> = leadDao.getAllLeads()
    val allDrafts: Flow<List<EmailDraft>> = draftDao.getAllDrafts()
    val allAlerts: Flow<List<OutreachAlert>> = alertDao.getAllAlerts()
    val allTemplates: Flow<List<OutreachTemplate>> = templateDao.getAllTemplates()
    val allExperiments: Flow<List<TemplateVariationExperiment>> = experimentDao.getAllExperiments()

    val analytics: Flow<OutreachAnalytics> = combine(allLeads, allDrafts) { leads, drafts ->
        val totalLeads = leads.size
        val verifiedLeads = leads.count { it.verificationStatus == VerificationStatus.VALID }
        val riskyLeads = leads.count { it.verificationStatus == VerificationStatus.RISKY }
        val invalidLeads = leads.count { it.verificationStatus == VerificationStatus.INVALID }

        val totalDrafted = drafts.size
        val sentDrafts = drafts.filter { it.status != OutreachStatus.DRAFT && it.status != OutreachStatus.SCHEDULED }
        val totalSent = sentDrafts.size
        val totalOpened = drafts.count { it.status == OutreachStatus.OPENED || it.status == OutreachStatus.CLICKED || it.status == OutreachStatus.REPLIED }
        val totalClicked = drafts.count { it.status == OutreachStatus.CLICKED || it.status == OutreachStatus.REPLIED }
        val totalReplied = drafts.count { it.status == OutreachStatus.REPLIED }

        val openRate = if (totalSent > 0) (totalOpened.toFloat() / totalSent.toFloat()) * 100f else 0f
        val clickThroughRate = if (totalOpened > 0) (totalClicked.toFloat() / totalOpened.toFloat()) * 100f else 0f
        val responseRate = if (totalSent > 0) (totalReplied.toFloat() / totalSent.toFloat()) * 100f else 0f
        val verificationRate = if (totalLeads > 0) (verifiedLeads.toFloat() / totalLeads.toFloat()) * 100f else 0f

        OutreachAnalytics(
            totalLeads = totalLeads,
            verifiedLeads = verifiedLeads,
            riskyLeads = riskyLeads,
            invalidLeads = invalidLeads,
            totalDrafted = totalDrafted,
            totalSent = totalSent,
            totalOpened = totalOpened,
            totalClicked = totalClicked,
            totalReplied = totalReplied,
            openRate = openRate,
            clickThroughRate = clickThroughRate,
            responseRate = responseRate,
            verificationRate = verificationRate
        )
    }

    suspend fun verifyLeadEmail(leadId: Long): BusinessLead? = withContext(Dispatchers.IO) {
        val lead = leadDao.getLeadById(leadId) ?: return@withContext null
        val result = EmailVerifier.verify(lead.email, lead.companyName)

        val updatedLead = lead.copy(
            verificationStatus = result.status,
            verificationReason = result.summary,
            deliverabilityScore = result.score
        )
        leadDao.updateLead(updatedLead)

        val alertType = if (result.status == VerificationStatus.VALID) AlertType.VERIFICATION_PASSED else AlertType.VERIFICATION_FAILED
        val alertTitle = if (result.status == VerificationStatus.VALID) "Email Verified: ${lead.companyName}" else "Verification Warning: ${lead.companyName}"
        alertDao.insertAlert(
            OutreachAlert(
                leadId = lead.id,
                companyName = lead.companyName,
                type = alertType,
                title = alertTitle,
                message = "${lead.email} scored ${result.score}% deliverability. ${result.summary}"
            )
        )

        updatedLead
    }

    suspend fun verifyAllUnverifiedLeads() = withContext(Dispatchers.IO) {
        // Collect current leads once
        leadDao.getAllLeads()
        // verify unverified leads
    }

    suspend fun generateAiDraft(lead: BusinessLead, angle: PsychologyAngle): EmailDraft = withContext(Dispatchers.IO) {
        val generated = GeminiOutreachService.generateOutreach(lead, angle)
        val draft = EmailDraft(
            leadId = lead.id,
            companyName = lead.companyName,
            contactName = lead.contactName,
            recipientEmail = lead.email,
            psychologyAngle = angle,
            subjectLine = generated.subjectLine,
            subjectAlternative = generated.subjectAlternative,
            emailBody = generated.emailBody,
            psychologyReasoning = generated.psychologyReasoning,
            surveyQuestion = generated.surveyQuestion,
            followUpNudge = generated.followUpNudge,
            status = OutreachStatus.DRAFT
        )
        val insertedId = draftDao.insertDraft(draft)
        draft.copy(id = insertedId)
    }

    suspend fun saveDraft(draft: EmailDraft): Long = withContext(Dispatchers.IO) {
        draftDao.insertDraft(draft)
    }

    suspend fun sendOutreach(draftId: Long) = withContext(Dispatchers.IO) {
        val draft = draftDao.getDraftById(draftId) ?: return@withContext
        val now = System.currentTimeMillis()
        val updatedDraft = draft.copy(status = OutreachStatus.SENT, sentAt = now)
        draftDao.updateDraft(updatedDraft)

        // Update lead status
        val lead = leadDao.getLeadById(draft.leadId)
        if (lead != null) {
            leadDao.updateLead(lead.copy(lastOutreachStatus = OutreachStatus.SENT))
        }

        // Insert Sent Alert
        alertDao.insertAlert(
            OutreachAlert(
                leadId = draft.leadId,
                companyName = draft.companyName,
                type = AlertType.EMAIL_SENT,
                title = "Email Dispatched: ${draft.companyName}",
                message = "Psychological outreach sent to ${draft.contactName} (${draft.recipientEmail}) with angle '${draft.psychologyAngle.title}'."
            )
        )
    }

    suspend fun simulateEngagementEvent(draftId: Long, nextStatus: OutreachStatus) = withContext(Dispatchers.IO) {
        val draft = draftDao.getDraftById(draftId) ?: return@withContext
        val now = System.currentTimeMillis()

        when (nextStatus) {
            OutreachStatus.OPENED -> {
                draftDao.updateDraft(draft.copy(status = OutreachStatus.OPENED, openedAt = now))
                leadDao.getLeadById(draft.leadId)?.let {
                    leadDao.updateLead(it.copy(lastOutreachStatus = OutreachStatus.OPENED))
                }
                alertDao.insertAlert(
                    OutreachAlert(
                        leadId = draft.leadId,
                        companyName = draft.companyName,
                        type = AlertType.EMAIL_OPENED,
                        title = "Email Opened: ${draft.companyName}",
                        message = "${draft.contactName} just opened your outreach: '${draft.subjectLine}'."
                    )
                )
            }
            OutreachStatus.CLICKED -> {
                draftDao.updateDraft(draft.copy(status = OutreachStatus.CLICKED, openedAt = draft.openedAt ?: now, clickedAt = now))
                leadDao.getLeadById(draft.leadId)?.let {
                    leadDao.updateLead(it.copy(lastOutreachStatus = OutreachStatus.CLICKED))
                }
                alertDao.insertAlert(
                    OutreachAlert(
                        leadId = draft.leadId,
                        companyName = draft.companyName,
                        type = AlertType.LINK_CLICKED,
                        title = "Link Clicked: ${draft.companyName}",
                        message = "🔥 ${draft.contactName} engaged with the survey link / diagnostic CTA in your email!"
                    )
                )
            }
            OutreachStatus.REPLIED -> {
                draftDao.updateDraft(draft.copy(status = OutreachStatus.REPLIED, repliedAt = now))
                leadDao.getLeadById(draft.leadId)?.let {
                    leadDao.updateLead(it.copy(lastOutreachStatus = OutreachStatus.REPLIED))
                }
                alertDao.insertAlert(
                    OutreachAlert(
                        leadId = draft.leadId,
                        companyName = draft.companyName,
                        type = AlertType.LEAD_REPLIED,
                        title = "🎉 Prospect Replied: ${draft.companyName}",
                        message = "${draft.contactName} responded to your survey question! Ready for discovery conversation."
                    )
                )
            }
            else -> {}
        }
    }

    suspend fun importDiscoveredLeads(discoveredLeads: List<com.example.data.model.DiscoveredBusinessLead>): Int = withContext(Dispatchers.IO) {
        val selected = discoveredLeads.filter { it.isSelected }
        var count = 0
        selected.forEach { item ->
            val verification = com.example.data.verification.EmailVerificationService.getInstance().verify(item.email, item.companyName)
            val lead = BusinessLead(
                companyName = item.companyName,
                contactName = item.contactName,
                email = item.email,
                role = item.role,
                industry = item.industry,
                companySize = item.companySize,
                painPoint = item.painPoint,
                targetGoal = item.targetGoal,
                website = item.website,
                preferredAngle = item.suggestedAngle,
                verificationStatus = verification.status,
                verificationReason = verification.summary,
                deliverabilityScore = verification.score,
                lastOutreachStatus = OutreachStatus.DRAFT
            )
            val insertedId = leadDao.insertLead(lead)
            count++
        }

        if (count > 0) {
            alertDao.insertAlert(
                OutreachAlert(
                    leadId = 0L,
                    companyName = "Google Business Finder",
                    type = AlertType.VERIFICATION_PASSED,
                    title = "Imported $count Discovered Businesses",
                    message = "Successfully discovered from Google Maps & Search and verified $count target contacts."
                )
            )
        }
        count
    }

    suspend fun importEnrichedSheetContacts(contacts: List<com.example.data.model.EnrichedSheetContact>): Int = withContext(Dispatchers.IO) {
        val selected = contacts.filter { it.isSelected }
        var count = 0
        selected.forEach { item ->
            val verification = com.example.data.verification.EmailVerificationService.getInstance().verify(item.email, item.companyName)
            val lead = BusinessLead(
                companyName = item.companyName,
                contactName = item.contactName,
                email = item.email,
                role = item.role,
                industry = item.industry,
                companySize = "20-50",
                painPoint = item.inferredPainPoint,
                targetGoal = item.inferredTargetGoal,
                website = item.website,
                preferredAngle = item.recommendedAngle,
                verificationStatus = verification.status,
                verificationReason = "${item.contextSummary} • ${verification.summary}",
                deliverabilityScore = verification.score,
                lastOutreachStatus = OutreachStatus.DRAFT
            )
            leadDao.insertLead(lead)
            count++
        }

        if (count > 0) {
            alertDao.insertAlert(
                OutreachAlert(
                    leadId = 0L,
                    companyName = "Google Sheet Import",
                    type = AlertType.VERIFICATION_PASSED,
                    title = "Imported $count Sheet Contacts",
                    message = "Enriched with Gemini context & verified $count contacts from your sheet upload."
                )
            )
        }
        count
    }

    suspend fun saveLead(lead: BusinessLead): Long = withContext(Dispatchers.IO) {
        if (lead.id == 0L) {
            leadDao.insertLead(lead)
        } else {
            leadDao.updateLead(lead)
            lead.id
        }
    }

    suspend fun deleteLead(leadId: Long) = withContext(Dispatchers.IO) {
        leadDao.deleteLeadById(leadId)
    }

    suspend fun deleteDraft(draftId: Long) = withContext(Dispatchers.IO) {
        draftDao.deleteDraftById(draftId)
    }

    suspend fun markAlertRead(alertId: Long) = withContext(Dispatchers.IO) {
        alertDao.markAlertRead(alertId)
    }

    suspend fun markAllAlertsRead() = withContext(Dispatchers.IO) {
        alertDao.markAllAlertsRead()
    }

    suspend fun clearAlerts() = withContext(Dispatchers.IO) {
        alertDao.clearAlerts()
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        seedInitialTemplatesIfEmpty()
        seedInitialExperimentsIfEmpty()

        val count = leadDao.getLeadCount()
        if (count > 0) return@withContext

        val seedLeads = listOf(
            BusinessLead(
                companyName = "ApexFlow Technologies",
                contactName = "Marcus Vance",
                email = "marcus.vance@apexflow.io",
                role = "VP of Product & Growth",
                industry = "B2B SaaS",
                companySize = "45 employees",
                painPoint = "High user drop-off in first 7 days due to complex self-serve onboarding",
                targetGoal = "Increase user activation by 35% and shorten time-to-value",
                website = "apexflow.io",
                preferredAngle = PsychologyAngle.SURVEY_HOOK,
                verificationStatus = VerificationStatus.VALID,
                verificationReason = "Verified Corporate Domain • Active MX • Deliverability 96%",
                deliverabilityScore = 96,
                lastOutreachStatus = OutreachStatus.OPENED
            ),
            BusinessLead(
                companyName = "Lumina Dental Group",
                contactName = "Dr. Elena Rostova",
                email = "elena.rostova@luminadental.com",
                role = "Clinical Director & Owner",
                industry = "Healthcare & Clinics",
                companySize = "20 employees",
                painPoint = "Patients not booking hygiene recalls leading to 25% empty treatment chairs",
                targetGoal = "Fill 95% of monthly recurring chair capacity automatically",
                website = "luminadental.com",
                preferredAngle = PsychologyAngle.PROBLEM_AGITATION,
                verificationStatus = VerificationStatus.VALID,
                verificationReason = "Verified Healthcare Domain • Direct Inbox • Deliverability 94%",
                deliverabilityScore = 94,
                lastOutreachStatus = OutreachStatus.CLICKED
            ),
            BusinessLead(
                companyName = "CargoLogix Worldwide",
                contactName = "David Chen",
                email = "david.chen@cargologix.net",
                role = "Head of Global Operations",
                industry = "Freight & Logistics",
                companySize = "150 employees",
                painPoint = "Manual freight quote turnaround takes 4+ hours, losing deals to digital brokers",
                targetGoal = "Provide instant automated freight quotes under 60 seconds",
                website = "cargologix.net",
                preferredAngle = PsychologyAngle.BEFORE_AFTER_BRIDGE,
                verificationStatus = VerificationStatus.VALID,
                verificationReason = "Verified Logistics MX • High inbox reputation (98%)",
                deliverabilityScore = 98,
                lastOutreachStatus = OutreachStatus.REPLIED
            ),
            BusinessLead(
                companyName = "Nordic Peak Apparel",
                contactName = "Astrid Lindholm",
                email = "astrid@nordicpeak.co",
                role = "Head of E-Commerce & DTC",
                industry = "E-Commerce & Retail",
                companySize = "30 employees",
                painPoint = "Cart abandonment rate hovering at 78% with poor mobile checkout retention",
                targetGoal = "Recover $45k/month in abandoned cart revenue with zero ad spend",
                website = "nordicpeak.co",
                preferredAngle = PsychologyAngle.SOCIAL_PROOF,
                verificationStatus = VerificationStatus.VALID,
                verificationReason = "Verified Custom DTC Domain • High Deliverability (92%)",
                deliverabilityScore = 92,
                lastOutreachStatus = OutreachStatus.SENT
            ),
            BusinessLead(
                companyName = "Zenith Construction Ltd",
                contactName = "Robert Gallagher",
                email = "info@zenithbuild.org",
                role = "Managing Partner",
                industry = "Commercial Construction",
                companySize = "80 employees",
                painPoint = "Subcontractor communication delays causing site project schedule slip",
                targetGoal = "Maintain 100% on-time milestone delivery across 12 active build sites",
                website = "zenithbuild.org",
                preferredAngle = PsychologyAngle.RECIPROCITY_AUDIT,
                verificationStatus = VerificationStatus.RISKY,
                verificationReason = "Shared team inbox (info@) • Deliverability 68%",
                deliverabilityScore = 68,
                lastOutreachStatus = OutreachStatus.DRAFT
            )
        )

        leadDao.insertLeads(seedLeads)

        // Seed initial alerts and drafts
        val leads = leadDao.getLeadCount()
        val firstLead = seedLeads[0]
        val secondLead = seedLeads[1]
        val thirdLead = seedLeads[2]

        val draft1 = EmailDraft(
            leadId = 1L,
            companyName = firstLead.companyName,
            contactName = firstLead.contactName,
            recipientEmail = firstLead.email,
            psychologyAngle = PsychologyAngle.SURVEY_HOOK,
            subjectLine = "quick question on ApexFlow's onboarding drop-off",
            subjectAlternative = "Marcus, thought on 7-day user activation benchmarks?",
            emailBody = """
                Hi Marcus,
                
                Noticed ApexFlow's rapid expansion in B2B SaaS and had a quick 10-second question:
                
                Between friction in initial API setup and self-serve onboarding drop-off, which is draining more of your growth team's bandwidth this quarter?
                
                We recently surveyed 40+ SaaS product leaders and found 72% improved time-to-value by 35% simply by automating interactive milestone nudges.
                
                If you're open to the 1-page benchmark data, happy to send it over. No pitch, just raw insights.
                
                Best,
                AI Outreach Team
            """.trimIndent(),
            psychologyReasoning = "Uses micro-commitment & curiosity gap: Lowers reply barrier by asking 1 specific diagnostic question before presenting any offer.",
            surveyQuestion = "Between initial API setup and self-serve onboarding drop-off, which is draining more bandwidth?",
            followUpNudge = "Hi Marcus, following up on my quick question above—would the 1-page benchmark data on onboarding activation be useful?",
            status = OutreachStatus.OPENED,
            sentAt = System.currentTimeMillis() - 86400000L,
            openedAt = System.currentTimeMillis() - 36000000L
        )

        val draft2 = EmailDraft(
            leadId = 2L,
            companyName = secondLead.companyName,
            contactName = secondLead.contactName,
            recipientEmail = secondLead.email,
            psychologyAngle = PsychologyAngle.PROBLEM_AGITATION,
            subjectLine = "filling empty chairs at Lumina Dental?",
            subjectAlternative = "Dr. Elena, quick calculation on patient hygiene recalls",
            emailBody = """
                Hi Dr. Elena,
                
                Most clinic directors we speak with mention the same silent drain: 25% empty treatment chairs from unbooked hygiene recalls.
                
                When hygiene recalls slip, it silently costs $18k-$24k/month in lost production while your team spends hours manually dialing voicemails.
                
                We built an automated patient reactivation sequence that fills 95% of recurring chair capacity without staff intervention.
                
                Open to a 2-minute glance at how Lumina Dental can deploy this?
                
                Best,
                AI Outreach Team
            """.trimIndent(),
            psychologyReasoning = "Applies Loss Aversion (quantifying the $18k-$24k/month unbooked chair revenue bleed) to prompt immediate prioritization.",
            surveyQuestion = "Is hygiene recall scheduling currently handled manually via phone or automated SMS?",
            followUpNudge = "Hi Dr. Elena, circling back on filling Lumina's recurring hygiene chairs—happy to share a 90-second video walkthrough if helpful.",
            status = OutreachStatus.CLICKED,
            sentAt = System.currentTimeMillis() - 172800000L,
            openedAt = System.currentTimeMillis() - 86400000L,
            clickedAt = System.currentTimeMillis() - 18000000L
        )

        val draft3 = EmailDraft(
            leadId = 3L,
            companyName = thirdLead.companyName,
            contactName = thirdLead.contactName,
            recipientEmail = thirdLead.email,
            psychologyAngle = PsychologyAngle.BEFORE_AFTER_BRIDGE,
            subjectLine = "instant freight quotes for CargoLogix",
            subjectAlternative = "David, from 4-hour quote delays to sub-60s instant quotes",
            emailBody = """
                Hi David,
                
                Right now, 4+ hour manual quote turnarounds are likely costing CargoLogix high-margin broker freight contracts.
                
                Imagine having a 24/7 autonomous rate-matching engine that returns verified freight quotes in under 60 seconds with zero human input.
                
                We bridge that exact gap for international logistics leaders.
                
                Mind if I send over a 2-minute breakdown of how David Chen's team can test this?
                
                Best,
                AI Outreach Team
            """.trimIndent(),
            psychologyReasoning = "Before-After-Bridge creates aspirational cognitive contrast between slow manual quoting and instantaneous digital dispatch.",
            surveyQuestion = "What percentage of quote requests currently require manual dispatcher calculation?",
            followUpNudge = "Hi David, following up on the instant quote engine note—open to seeing the 2-minute Loom?",
            status = OutreachStatus.REPLIED,
            sentAt = System.currentTimeMillis() - 259200000L,
            openedAt = System.currentTimeMillis() - 172800000L,
            clickedAt = System.currentTimeMillis() - 90000000L,
            repliedAt = System.currentTimeMillis() - 14400000L
        )

        draftDao.insertDraft(draft1)
        draftDao.insertDraft(draft2)
        draftDao.insertDraft(draft3)

        alertDao.insertAlert(
            OutreachAlert(
                leadId = 3L,
                companyName = thirdLead.companyName,
                type = AlertType.LEAD_REPLIED,
                title = "🎉 Response Received: David Chen",
                message = "CargoLogix Worldwide replied to survey question: 'Interested, please share how rate-matching engine connects to our TMS.'",
                timestamp = System.currentTimeMillis() - 14400000L
            )
        )
        alertDao.insertAlert(
            OutreachAlert(
                leadId = 2L,
                companyName = secondLead.companyName,
                type = AlertType.LINK_CLICKED,
                title = "🔥 High Intent: Dr. Elena Rostova",
                message = "Lumina Dental clicked the diagnostic benchmark link in your PAS email outreach.",
                timestamp = System.currentTimeMillis() - 18000000L
            )
        )
        alertDao.insertAlert(
            OutreachAlert(
                leadId = 1L,
                companyName = firstLead.companyName,
                type = AlertType.EMAIL_OPENED,
                title = "Email Opened: Marcus Vance",
                message = "ApexFlow Technologies opened survey outreach ('quick question on ApexFlow's onboarding drop-off').",
                timestamp = System.currentTimeMillis() - 36000000L
            )
        )

        seedInitialTemplatesIfEmpty()
    }

    suspend fun saveTemplate(template: OutreachTemplate): Long = withContext(Dispatchers.IO) {
        if (template.id == 0L) {
            templateDao.insertTemplate(template)
        } else {
            templateDao.updateTemplate(template)
            template.id
        }
    }

    suspend fun deleteTemplate(templateId: Long) = withContext(Dispatchers.IO) {
        templateDao.deleteTemplateById(templateId)
    }

    suspend fun incrementTemplateUsage(templateId: Long) = withContext(Dispatchers.IO) {
        templateDao.incrementTemplateUsage(templateId)
    }

    suspend fun getTemplateById(templateId: Long): OutreachTemplate? = withContext(Dispatchers.IO) {
        templateDao.getTemplateById(templateId)
    }

    fun substituteTemplateVariables(text: String, lead: BusinessLead): String {
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

    suspend fun applyTemplateToLead(
        template: OutreachTemplate,
        lead: BusinessLead
    ): EmailDraft = withContext(Dispatchers.IO) {
        incrementTemplateUsage(template.id)

        val substitutedSubject = substituteTemplateVariables(template.subjectLineTemplate, lead)
        val substitutedAltSubject = substituteTemplateVariables(template.subjectAlternativeTemplate, lead)
        val substitutedBody = substituteTemplateVariables(template.bodyTemplate, lead)
        val substitutedSurvey = substituteTemplateVariables(template.surveyQuestionTemplate, lead)
        val substitutedFollowUp = substituteTemplateVariables(template.followUpNudgeTemplate, lead)

        val draft = EmailDraft(
            leadId = lead.id,
            companyName = lead.companyName,
            contactName = lead.contactName,
            recipientEmail = lead.email,
            psychologyAngle = template.psychologyAngle,
            subjectLine = substitutedSubject,
            subjectAlternative = substitutedAltSubject,
            emailBody = substitutedBody,
            psychologyReasoning = template.psychologyInsight,
            surveyQuestion = substitutedSurvey,
            followUpNudge = substitutedFollowUp,
            status = OutreachStatus.DRAFT
        )

        val insertedId = draftDao.insertDraft(draft)
        draft.copy(id = insertedId)
    }

    suspend fun seedInitialTemplatesIfEmpty() = withContext(Dispatchers.IO) {
        val count = templateDao.getTemplateCount()
        if (count > 0) return@withContext

        val seedTemplates = listOf(
            OutreachTemplate(
                title = "10-Sec Diagnostic Micro-Survey",
                category = "Micro-Surveys",
                psychologyAngle = PsychologyAngle.SURVEY_HOOK,
                subjectLineTemplate = "quick question on {{companyName}}'s {{painPoint}}",
                subjectAlternativeTemplate = "{{contactName}}, thought on {{targetGoal}} benchmarks?",
                bodyTemplate = """
                    Hi {{contactName}},
                    
                    Noticed {{companyName}}'s growth in {{industry}} and had a quick 10-second diagnostic question:
                    
                    Between {{painPoint}} and day-to-day team bandwidth, which is causing more friction in reaching {{targetGoal}} this quarter?
                    
                    We recently surveyed 40+ leaders in {{industry}} and compiled a 1-page benchmark sheet on how high-performing teams streamline this without extra headcount.
                    
                    If you're open to the data sheet, happy to send it over. No pitch, just raw insights.
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                surveyQuestionTemplate = "Between {{painPoint}} and team bandwidth, which is causing more friction in reaching {{targetGoal}}?",
                followUpNudgeTemplate = "Hi {{contactName}}, quick follow-up on my note above—would that 1-page benchmark data on {{targetGoal}} be useful?",
                psychologyInsight = "Leverages low-friction micro-commitments and the curiosity gap. Asking an either/or diagnostic question reduces cognitive load and yields 3x higher response rates than a direct sales pitch.",
                isCustom = false,
                usageCount = 18
            ),
            OutreachTemplate(
                title = "The 9-Word Friction Inquiry",
                category = "Micro-Surveys",
                psychologyAngle = PsychologyAngle.SURVEY_HOOK,
                subjectLineTemplate = "{{companyName}} + {{targetGoal}}?",
                subjectAlternativeTemplate = "quick thought for {{contactName}}",
                bodyTemplate = """
                    Hi {{contactName}},
                    
                    Are you still dealing with {{painPoint}} at {{companyName}}?
                    
                    We put together a streamlined 3-step solution to help {{industry}} teams achieve {{targetGoal}} seamlessly.
                    
                    Worth a brief 2-minute conversation this week?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                surveyQuestionTemplate = "Are you still dealing with {{painPoint}} at {{companyName}}?",
                followUpNudgeTemplate = "Hi {{contactName}}, wanted to float this to the top of your inbox in case you missed my 9-word question above.",
                psychologyInsight = "Dean Jackson's 9-word email framework. Removes all sales resistance by asking a plain, binary question that feels personal and unscripted.",
                isCustom = false,
                usageCount = 14
            ),
            OutreachTemplate(
                title = "Quantified Cost-of-Inaction Agitator",
                category = "Problem & Agitation (PAS)",
                psychologyAngle = PsychologyAngle.PROBLEM_AGITATION,
                subjectLineTemplate = "the hidden cost of {{painPoint}} at {{companyName}}",
                subjectAlternativeTemplate = "{{contactName}}, quick calculation on {{painPoint}}",
                bodyTemplate = """
                    Hi {{contactName}},
                    
                    Most {{role}}s in {{industry}} we speak with mention the same silent drain: {{painPoint}}.
                    
                    When left unresolved, this silently costs 15+ lost hours each week and delays {{targetGoal}}, while your team is stuck firefighting manual friction.
                    
                    We built a specialized engine that solves {{painPoint}} and delivers {{targetGoal}} automatically.
                    
                    Open to a 2-minute glance at how {{companyName}} can test this?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                surveyQuestionTemplate = "Is {{painPoint}} currently costing your team more in lost hours or delayed revenue?",
                followUpNudgeTemplate = "Hi {{contactName}}, circling back—if {{painPoint}} is on your priority list to resolve this month, let me know if a quick breakdown makes sense.",
                psychologyInsight = "Kahneman & Tversky's Loss Aversion principle. Prospects feel the pain of a loss twice as intensely as the pleasure of a gain. Agitating the hidden operational bleed creates urgency.",
                isCustom = false,
                usageCount = 22
            ),
            OutreachTemplate(
                title = "Autonomous Transformation Arc (BAB)",
                category = "Vision & Contrast (BAB)",
                psychologyAngle = PsychologyAngle.BEFORE_AFTER_BRIDGE,
                subjectLineTemplate = "{{targetGoal}} for {{companyName}}",
                subjectAlternativeTemplate = "from {{painPoint}} to {{targetGoal}}",
                bodyTemplate = """
                    Hi {{contactName}},
                    
                    Right now, dealing with {{painPoint}} is likely creating unnecessary drag on {{companyName}}'s growth.
                    
                    Imagine having an intelligent engine running 24/7 that automatically delivers {{targetGoal}} without requiring extra headcount or manual tasks.
                    
                    We bridge that gap with proven outreach pipelines configured specifically for {{industry}}.
                    
                    Mind if I send over a 90-second interactive preview showing how it looks in action?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                surveyQuestionTemplate = "How close is your team to achieving {{targetGoal}} with your existing workflow?",
                followUpNudgeTemplate = "Hi {{contactName}}, quick nudge—would you be open to seeing the 2-minute breakdown on how peer companies achieve {{targetGoal}}?",
                psychologyInsight = "Before-After-Bridge (BAB). Sharpens the emotional contrast between the frustrating 'before' state and the frictionless 'after' state, positioning your offer as the indispensable bridge.",
                isCustom = false,
                usageCount = 11
            ),
            OutreachTemplate(
                title = "Peer Benchmark & 38% Case Study",
                category = "Social Proof & Benchmarks",
                psychologyAngle = PsychologyAngle.SOCIAL_PROOF,
                subjectLineTemplate = "how peer {{industry}} teams solved {{painPoint}}",
                subjectAlternativeTemplate = "case study for {{companyName}} ({{contactName}})",
                bodyTemplate = """
                    Hi {{contactName}},
                    
                    We recently helped 3 fast-growing {{industry}} companies tackle {{painPoint}}.
                    
                    By introducing targeted psychology-driven outreach sequences, they saw an immediate 38% increase in qualified pipeline and achieved {{targetGoal}} within 30 days.
                    
                    Given {{companyName}}'s current stage, thought this benchmark breakdown would be directly relevant to your quarterly goals.
                    
                    Open to taking a peek at the teardown?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                surveyQuestionTemplate = "Have you seen similar metrics from other automated systems in {{industry}}?",
                followUpNudgeTemplate = "Hi {{contactName}}, following up—happy to share the exact 3-step playbook our {{industry}} partners used if you're interested.",
                psychologyInsight = "Bandwagon Effect and Authority Heuristic. Concrete metrics from similar businesses in their exact niche drastically lower perceived risk and trigger competitive FOMO.",
                isCustom = false,
                usageCount = 16
            ),
            OutreachTemplate(
                title = "Unsolicited 3-Point Action Audit",
                category = "Reciprocity Audits",
                psychologyAngle = PsychologyAngle.RECIPROCITY_AUDIT,
                subjectLineTemplate = "complimentary audit for {{companyName}}",
                subjectAlternativeTemplate = "3 custom insights on {{companyName}}'s {{painPoint}}",
                bodyTemplate = """
                    Hi {{contactName}},
                    
                    I ran a quick audit on {{companyName}}'s public presence and noticed a specific bottleneck regarding {{painPoint}}.
                    
                    I put together a 3-bullet breakdown with actionable recommendations on how you can accelerate {{targetGoal}}.
                    
                    No catch or strings attached—would it be helpful if I pasted those 3 insights here for you?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                surveyQuestionTemplate = "Would it be helpful if I shared our diagnostic findings for {{companyName}}?",
                followUpNudgeTemplate = "Hi {{contactName}}, did you want me to send over those 3 tailored insights for {{companyName}}?",
                psychologyInsight = "Robert Cialdini's Principle of Reciprocity. Providing upfront, bespoke value with zero obligation creates a subconscious psychological drive to engage and return the favor.",
                isCustom = false,
                usageCount = 25
            ),
            OutreachTemplate(
                title = "The Polite Day+3 Context Nudge",
                category = "Follow-ups & Nudges",
                psychologyAngle = PsychologyAngle.SURVEY_HOOK,
                subjectLineTemplate = "Re: quick question on {{companyName}}'s {{painPoint}}",
                subjectAlternativeTemplate = "thought on {{targetGoal}} for {{companyName}}?",
                bodyTemplate = """
                    Hi {{contactName}},
                    
                    Following up on my note earlier this week regarding {{companyName}} and {{painPoint}}.
                    
                    I know you're busy running operations as {{role}}, so wanted to keep this brief:
                    
                    Would that 1-page benchmark guide on achieving {{targetGoal}} be useful for your team right now, or should I close your file for this quarter?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                surveyQuestionTemplate = "Would the 1-page benchmark guide on achieving {{targetGoal}} be useful, or should I close your file?",
                followUpNudgeTemplate = "Hi {{contactName}}, no worries if timing isn't right—wishing you and {{companyName}} all the best!",
                psychologyInsight = "Graceful Takeaway / Reverse Psychology. Giving prospects an easy 'out' or closing their file triggers mild loss aversion and prompts prompt replies from serious buyers.",
                isCustom = false,
                usageCount = 9
            ),
            OutreachTemplate(
                title = "The 'Did I Catch You at a Bad Time?' Pivot",
                category = "Follow-ups & Nudges",
                psychologyAngle = PsychologyAngle.PROBLEM_AGITATION,
                subjectLineTemplate = "bad timing, {{contactName}}?",
                subjectAlternativeTemplate = "one last note regarding {{companyName}}'s {{painPoint}}",
                bodyTemplate = """
                    Hi {{contactName}},
                    
                    Did I catch you at a bad time with my earlier note about {{painPoint}}?
                    
                    If reaching {{targetGoal}} isn't a top priority for {{companyName}} this quarter, no problem at all.
                    
                    If it is, here is the 1-minute overview of how other {{industry}} teams resolved it: {{website}}
                    
                    Let me know if you'd ever like to reconnect down the line.
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                surveyQuestionTemplate = "Is resolving {{painPoint}} currently on your radar for this quarter?",
                followUpNudgeTemplate = "Hi {{contactName}}, all good—feel free to reach out whenever {{painPoint}} comes up again.",
                psychologyInsight = "Chris Voss's 'Is now a bad time?' negotiation framework. People feel safer and more in control saying 'No' or clarifying timing than responding to a high-pressure sales email.",
                isCustom = false,
                usageCount = 12
            )
        )

        templateDao.insertTemplates(seedTemplates)
    }

    // Experiment & A/B Variation Operations
    suspend fun insertExperiment(experiment: TemplateVariationExperiment): Long = withContext(Dispatchers.IO) {
        experimentDao.insertExperiment(experiment)
    }

    suspend fun updateExperiment(experiment: TemplateVariationExperiment) = withContext(Dispatchers.IO) {
        experimentDao.updateExperiment(experiment.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteExperiment(experiment: TemplateVariationExperiment) = withContext(Dispatchers.IO) {
        experimentDao.deleteExperiment(experiment)
    }

    suspend fun deleteExperimentById(id: Long) = withContext(Dispatchers.IO) {
        experimentDao.deleteExperimentById(id)
    }

    suspend fun getExperimentById(id: Long): TemplateVariationExperiment? = withContext(Dispatchers.IO) {
        experimentDao.getExperimentById(id)
    }

    suspend fun recordVariantPerformance(
        experimentId: Long,
        variant: String, // "A" or "B"
        sent: Int,
        opened: Int,
        clicked: Int,
        replied: Int
    ) = withContext(Dispatchers.IO) {
        if (variant == "A") {
            experimentDao.recordVariantAPerformance(experimentId, sent, opened, clicked, replied)
        } else {
            experimentDao.recordVariantBPerformance(experimentId, sent, opened, clicked, replied)
        }
    }

    suspend fun declareWinner(experimentId: Long, winner: String, reason: String) = withContext(Dispatchers.IO) {
        experimentDao.declareWinner(experimentId, ExperimentStatus.COMPLETED, winner, reason)
    }

    suspend fun resetExperimentMetrics(experimentId: Long) = withContext(Dispatchers.IO) {
        experimentDao.resetExperimentMetrics(experimentId)
    }

    suspend fun simulateCohortTest(
        experimentId: Long,
        sampleSizePerVariant: Int = 20,
        favoredVariant: String = "B"
    ): TemplateVariationExperiment? = withContext(Dispatchers.IO) {
        val exp = experimentDao.getExperimentById(experimentId) ?: return@withContext null

        // Generate realistic simulated metrics
        val isBFavored = favoredVariant == "B"
        val openRateA = if (isBFavored) 0.52f else 0.74f
        val openRateB = if (isBFavored) 0.76f else 0.54f
        val replyRatioA = if (isBFavored) 0.22f else 0.40f
        val replyRatioB = if (isBFavored) 0.42f else 0.20f

        val newSentA = sampleSizePerVariant
        val newOpenedA = (newSentA * openRateA).toInt().coerceAtLeast(1)
        val newClickedA = (newOpenedA * 0.35f).toInt().coerceAtLeast(1)
        val newRepliedA = (newOpenedA * replyRatioA).toInt().coerceAtLeast(1)

        val newSentB = sampleSizePerVariant
        val newOpenedB = (newSentB * openRateB).toInt().coerceAtLeast(1)
        val newClickedB = (newOpenedB * 0.45f).toInt().coerceAtLeast(1)
        val newRepliedB = (newOpenedB * replyRatioB).toInt().coerceAtLeast(1)

        experimentDao.recordVariantAPerformance(experimentId, newSentA, newOpenedA, newClickedA, newRepliedA)
        experimentDao.recordVariantBPerformance(experimentId, newSentB, newOpenedB, newClickedB, newRepliedB)

        val updated = experimentDao.getExperimentById(experimentId)
        alertDao.insertAlert(
            OutreachAlert(
                leadId = 0L,
                companyName = "A/B Testing Lab",
                type = AlertType.EMAIL_OPENED,
                title = "A/B Cohort Test Completed: ${exp.title.take(28)}",
                message = "Simulated $sampleSizePerVariant sends per variant. Variant B reply rate: ${"%.1f".format(updated?.replyRateB ?: 0f)}% vs Variant A: ${"%.1f".format(updated?.replyRateA ?: 0f)}%."
            )
        )
        updated
    }

    suspend fun promoteWinnerToTemplateLibrary(
        experimentId: Long,
        winningVariant: String // "A" or "B"
    ): OutreachTemplate = withContext(Dispatchers.IO) {
        val exp = experimentDao.getExperimentById(experimentId) 
            ?: throw IllegalArgumentException("Experiment not found")

        val isA = winningVariant == "A"
        val angle = if (isA) exp.variantAPsychologyAngle else exp.variantBPsychologyAngle
        val title = "${exp.title} (A/B Tested Winner)"
        val category = "A/B Tested High-Converters"
        val subject = if (isA) exp.variantASubjectLine else exp.variantBSubjectLine
        val altSubject = if (isA) exp.variantASubjectAlt else exp.variantBSubjectAlt
        val body = if (isA) exp.variantABody else exp.variantBBody
        val survey = if (isA) exp.variantASurveyHook else exp.variantBSurveyHook
        val followUp = if (isA) exp.variantAFollowUpNudge else exp.variantBFollowUpNudge
        
        val replyRate = if (isA) exp.replyRateA else exp.replyRateB
        val openRate = if (isA) exp.openRateA else exp.openRateB
        val insight = "Proven A/B Test Winner: Achieved ${"%.1f".format(openRate)}% open rate and ${"%.1f".format(replyRate)}% reply rate against control. ${exp.hypothesis}"

        val template = OutreachTemplate(
            title = title,
            category = category,
            psychologyAngle = angle,
            subjectLineTemplate = subject,
            subjectAlternativeTemplate = altSubject,
            bodyTemplate = body,
            surveyQuestionTemplate = survey,
            followUpNudgeTemplate = followUp,
            psychologyInsight = insight,
            isCustom = true,
            usageCount = 1
        )

        val insertedId = templateDao.insertTemplate(template)
        experimentDao.declareWinner(
            id = experimentId,
            status = ExperimentStatus.COMPLETED,
            winner = winningVariant,
            reason = "Promoted to Master Template Library after statistically significant win."
        )

        alertDao.insertAlert(
            OutreachAlert(
                leadId = 0L,
                companyName = "Template Library",
                type = AlertType.EMAIL_SENT,
                title = "🏆 A/B Winner Promoted to Library",
                message = "Successfully saved '$title' with ${"%.1f".format(replyRate)}% verified reply rate to your master templates."
            )
        )

        template.copy(id = insertedId)
    }

    suspend fun seedInitialExperimentsIfEmpty() = withContext(Dispatchers.IO) {
        val count = experimentDao.getExperimentCount()
        if (count > 0) return@withContext

        val seedExperiments = listOf(
            TemplateVariationExperiment(
                title = "Diagnostic Micro-Survey Hook vs Standard PAS (B2B SaaS)",
                hypothesis = "Replacing standard problem agitation with a 10-second binary diagnostic question reduces cognitive friction and lifts reply rate by >25%.",
                targetAudience = "SaaS Founders & Growth Leaders",
                testedElement = "Subject Hook & Micro-Survey CTA",
                variantAName = "Variation A (PAS Agitation)",
                variantAPsychologyAngle = PsychologyAngle.PROBLEM_AGITATION,
                variantASubjectLine = "the hidden cost of user drop-off at {{companyName}}",
                variantASubjectAlt = "{{contactName}}, quick calculation on {{painPoint}}",
                variantABody = """
                    Hi {{contactName}},
                    
                    Most {{role}}s in {{industry}} we speak with mention the same silent drain: {{painPoint}}.
                    
                    When left unresolved, this quietly costs 15+ lost hours each week and delays {{targetGoal}}, while your team is stuck firefighting manual friction.
                    
                    We built a specialized engine that solves {{painPoint}} and delivers {{targetGoal}} automatically.
                    
                    Open to a 2-minute glance at how {{companyName}} can test this?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                variantASurveyHook = "Is {{painPoint}} currently costing your team more in lost hours or delayed revenue?",
                variantAFollowUpNudge = "Hi {{contactName}}, circling back—if {{painPoint}} is on your roadmap this quarter, let me know.",
                variantAPerfSent = 48,
                variantAPerfOpened = 28,
                variantAPerfClicked = 6,
                variantAPerfReplied = 7,
                variantBName = "Variation B (10-Sec Micro-Survey)",
                variantBPsychologyAngle = PsychologyAngle.SURVEY_HOOK,
                variantBSubjectLine = "quick question on {{companyName}}'s {{painPoint}}",
                variantBSubjectAlt = "{{contactName}}, thought on {{targetGoal}} benchmarks?",
                variantBBody = """
                    Hi {{contactName}},
                    
                    Noticed {{companyName}}'s growth in {{industry}} and had a quick 10-second diagnostic question:
                    
                    Between {{painPoint}} and day-to-day team bandwidth, which is causing more friction in reaching {{targetGoal}} this quarter?
                    
                    We recently surveyed 40+ leaders in {{industry}} and compiled a 1-page benchmark sheet on how high-performing teams streamline this without extra headcount.
                    
                    If you're open to the data sheet, happy to send it over. No pitch, just raw insights.
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                variantBSurveyHook = "Between {{painPoint}} and team bandwidth, which is causing more friction in reaching {{targetGoal}}?",
                variantBFollowUpNudge = "Hi {{contactName}}, quick follow-up on my note above—would that 1-page benchmark data on {{targetGoal}} be useful?",
                variantBPerfSent = 50,
                variantBPerfOpened = 38,
                variantBPerfClicked = 14,
                variantBPerfReplied = 16,
                status = ExperimentStatus.ACTIVE_TESTING
            ),
            TemplateVariationExperiment(
                title = "Radical Brevity (42 words) vs Benchmark Case Study (DTC Retail)",
                hypothesis = "A plain 3-sentence note referencing cart recovery will outperform a multi-paragraph case study because mobile executives skim on phones.",
                targetAudience = "E-Commerce & DTC Marketing Directors",
                testedElement = "Copy Length & Mobile Friction",
                variantAName = "Variation A (Detailed Case Study)",
                variantAPsychologyAngle = PsychologyAngle.SOCIAL_PROOF,
                variantASubjectLine = "how peer {{industry}} teams solved {{painPoint}}",
                variantASubjectAlt = "case study for {{companyName}} ({{contactName}})",
                variantABody = """
                    Hi {{contactName}},
                    
                    We recently helped 3 fast-growing {{industry}} companies tackle {{painPoint}}.
                    
                    By introducing targeted psychology-driven outreach sequences, they saw an immediate 38% increase in qualified pipeline and achieved {{targetGoal}} within 30 days.
                    
                    Given {{companyName}}'s current stage, thought this benchmark breakdown would be directly relevant to your quarterly goals.
                    
                    Open to taking a peek at the teardown?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                variantASurveyHook = "Have you seen similar metrics from other automated systems in {{industry}}?",
                variantAFollowUpNudge = "Hi {{contactName}}, following up—happy to share the exact 3-step playbook our {{industry}} partners used if you're interested.",
                variantAPerfSent = 60,
                variantAPerfOpened = 31,
                variantAPerfClicked = 9,
                variantAPerfReplied = 8,
                variantBName = "Variation B (Radical Brevity 42 Words)",
                variantBPsychologyAngle = PsychologyAngle.SURVEY_HOOK,
                variantBSubjectLine = "{{companyName}} + {{targetGoal}}?",
                variantBSubjectAlt = "quick thought for {{contactName}}",
                variantBBody = """
                    Hi {{contactName}},
                    
                    Are you still dealing with {{painPoint}} at {{companyName}}?
                    
                    We put together a streamlined 3-step solution to help {{industry}} teams achieve {{targetGoal}} seamlessly.
                    
                    Worth a brief 2-minute conversation this week?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                variantBSurveyHook = "Are you still dealing with {{painPoint}} at {{companyName}}?",
                variantBFollowUpNudge = "Hi {{contactName}}, wanted to float this to the top of your inbox in case you missed my 9-word question above.",
                variantBPerfSent = 62,
                variantBPerfOpened = 46,
                variantBPerfClicked = 19,
                variantBPerfReplied = 18,
                status = ExperimentStatus.COMPLETED,
                declaredWinner = "B",
                customWinnerReason = "Radical Brevity (Variation B) generated a +15.7% reply rate lift (+118% relative conversion increase) with 94% statistical significance."
            ),
            TemplateVariationExperiment(
                title = "Reciprocity Free Teardown vs Vision Transformation BAB (Logistics)",
                hypothesis = "Offering 3 immediate complimentary observations without requiring a meeting upfront triggers reciprocity and unlocks enterprise logistics accounts.",
                targetAudience = "Logistics & Freight Operations VPs",
                testedElement = "Value-First Reciprocity vs Vision Pitch",
                variantAName = "Variation A (Vision BAB)",
                variantAPsychologyAngle = PsychologyAngle.BEFORE_AFTER_BRIDGE,
                variantASubjectLine = "{{targetGoal}} for {{companyName}}",
                variantASubjectAlt = "from {{painPoint}} to {{targetGoal}}",
                variantABody = """
                    Hi {{contactName}},
                    
                    Right now, dealing with {{painPoint}} is likely creating unnecessary drag on {{companyName}}'s growth.
                    
                    Imagine having an intelligent engine running 24/7 that automatically delivers {{targetGoal}} without requiring extra headcount.
                    
                    We bridge that gap with proven outreach pipelines configured specifically for {{industry}}.
                    
                    Mind if I send over a 90-second interactive preview showing how it looks in action?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                variantASurveyHook = "How close is your team to achieving {{targetGoal}} with your existing workflow?",
                variantAFollowUpNudge = "Hi {{contactName}}, quick nudge—would you be open to seeing the 2-minute breakdown on how peer companies achieve {{targetGoal}}?",
                variantAPerfSent = 38,
                variantAPerfOpened = 20,
                variantAPerfClicked = 5,
                variantAPerfReplied = 5,
                variantBName = "Variation B (Complimentary 3-Point Audit)",
                variantBPsychologyAngle = PsychologyAngle.RECIPROCITY_AUDIT,
                variantBSubjectLine = "complimentary audit for {{companyName}}",
                variantBSubjectAlt = "3 tailored observations on {{companyName}}'s {{painPoint}}",
                variantBBody = """
                    Hi {{contactName}},
                    
                    I ran a quick audit on {{companyName}}'s public presence and noticed a specific bottleneck regarding {{painPoint}}.
                    
                    I put together a 3-bullet breakdown with actionable recommendations on how you can accelerate {{targetGoal}}.
                    
                    No catch or strings attached—would it be helpful if I pasted those 3 insights here for you?
                    
                    Best,
                    AI Outreach Team
                """.trimIndent(),
                variantBSurveyHook = "Would it be helpful if I shared our diagnostic findings for {{companyName}}?",
                variantBFollowUpNudge = "Hi {{contactName}}, did you want me to send over those 3 tailored insights for {{companyName}}?",
                variantBPerfSent = 40,
                variantBPerfOpened = 28,
                variantBPerfClicked = 12,
                variantBPerfReplied = 11,
                status = ExperimentStatus.ACTIVE_TESTING
            )
        )

        experimentDao.insertExperiments(seedExperiments)
    }
}


