package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class VerificationStatus {
    UNVERIFIED,
    VALID,
    RISKY,
    INVALID
}

enum class OutreachStatus {
    DRAFT,
    SCHEDULED,
    SENT,
    OPENED,
    CLICKED,
    REPLIED,
    BOUNCED
}

enum class AlertType {
    EMAIL_SENT,
    EMAIL_OPENED,
    LINK_CLICKED,
    LEAD_REPLIED,
    VERIFICATION_PASSED,
    VERIFICATION_FAILED
}

enum class PsychologyAngle(
    val title: String,
    val subtitle: String,
    val description: String,
    val psychologyHook: String
) {
    SURVEY_HOOK(
        title = "Diagnostic Survey Hook",
        subtitle = "Curiosity & Micro-Commitment",
        description = "Asks 1 simple diagnostic question about their industry bottleneck before offering value.",
        psychologyHook = "Leverages low-friction micro-commitment and curiosity gap to get 3x higher reply rates."
    ),
    PROBLEM_AGITATION(
        title = "Problem - Agitate - Solve (PAS)",
        subtitle = "Loss Aversion & Empathy",
        description = "Directly points out their exact pain point, quantifies the hidden cost of inaction, and offers a lightweight solution.",
        psychologyHook = "Triggers loss aversion and immediate emotional validation of their daily operational frustration."
    ),
    BEFORE_AFTER_BRIDGE(
        title = "Before - After - Bridge (BAB)",
        subtitle = "Vision & Contrast",
        description = "Shows the friction of their current state vs. the frictionless future outcome, bridging with your offering.",
        psychologyHook = "Creates a vivid mental contrast and aspirational pull for high-intent decision makers."
    ),
    SOCIAL_PROOF(
        title = "Peer Benchmark & Case Study",
        subtitle = "Bandwagon & Authority",
        description = "References benchmark metrics achieved by similar businesses in their exact niche.",
        psychologyHook = "Reduces risk perception and appeals to the fear of falling behind direct competitors."
    ),
    RECIPROCITY_AUDIT(
        title = "Reciprocity & Free Audit",
        subtitle = "Unsolicited Value Upfront",
        description = "Provides a mini audit or actionable breakdown tailored specifically to their business upfront.",
        psychologyHook = "Activates the deep psychological drive of reciprocity by giving value before asking for anything."
    )
}

@Entity(tableName = "business_leads")
data class BusinessLead(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val companyName: String,
    val contactName: String,
    val email: String,
    val role: String,
    val industry: String,
    val companySize: String,
    val painPoint: String,
    val targetGoal: String,
    val website: String = "",
    val preferredAngle: PsychologyAngle = PsychologyAngle.SURVEY_HOOK,
    val verificationStatus: VerificationStatus = VerificationStatus.UNVERIFIED,
    val verificationReason: String = "Pending email verification scan",
    val deliverabilityScore: Int = 0, // 0 to 100
    val lastOutreachStatus: OutreachStatus = OutreachStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "email_drafts")
data class EmailDraft(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leadId: Long,
    val companyName: String,
    val contactName: String,
    val recipientEmail: String,
    val psychologyAngle: PsychologyAngle,
    val subjectLine: String,
    val subjectAlternative: String, // A/B test variant
    val emailBody: String,
    val psychologyReasoning: String,
    val surveyQuestion: String,
    val followUpNudge: String,
    val status: OutreachStatus = OutreachStatus.DRAFT,
    val sentAt: Long? = null,
    val openedAt: Long? = null,
    val clickedAt: Long? = null,
    val repliedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "outreach_alerts")
data class OutreachAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val leadId: Long,
    val companyName: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

data class OutreachAnalytics(
    val totalLeads: Int,
    val verifiedLeads: Int,
    val riskyLeads: Int,
    val invalidLeads: Int,
    val totalDrafted: Int,
    val totalSent: Int,
    val totalOpened: Int,
    val totalClicked: Int,
    val totalReplied: Int,
    val openRate: Float, // percentage e.g. 68.5f
    val clickThroughRate: Float, // percentage e.g. 34.2f
    val responseRate: Float, // percentage e.g. 18.0f
    val verificationRate: Float // percentage
)

data class DiscoveredBusinessLead(
    val id: String = java.util.UUID.randomUUID().toString(),
    val companyName: String,
    val contactName: String,
    val email: String,
    val role: String,
    val industry: String,
    val companySize: String = "10-50",
    val painPoint: String,
    val targetGoal: String,
    val website: String,
    val address: String = "",
    val phone: String = "",
    val suggestedAngle: PsychologyAngle = PsychologyAngle.SURVEY_HOOK,
    val isSelected: Boolean = true
)

data class EnrichedSheetContact(
    val id: String = java.util.UUID.randomUUID().toString(),
    val companyName: String,
    val contactName: String,
    val email: String,
    val role: String,
    val industry: String,
    val website: String,
    val inferredPainPoint: String,
    val inferredTargetGoal: String,
    val recommendedAngle: PsychologyAngle = PsychologyAngle.SURVEY_HOOK,
    val contextSummary: String = "",
    val isSelected: Boolean = true
)

enum class ChatSender {
    USER,
    ASSISTANT
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String = "gemini-3.5-flash",
    val role: String = "Strategist",
    val groundingSources: List<String> = emptyList(),
    val isError: Boolean = false
)

@Entity(tableName = "outreach_templates")
data class OutreachTemplate(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // e.g. "Micro-Surveys", "Problem & Agitation", "Vision & BAB", "Social Proof", "Reciprocity Audits", "Follow-ups & Nudges", "Custom"
    val psychologyAngle: PsychologyAngle,
    val subjectLineTemplate: String,
    val subjectAlternativeTemplate: String, // A/B test variant
    val bodyTemplate: String,
    val surveyQuestionTemplate: String = "",
    val followUpNudgeTemplate: String = "",
    val psychologyInsight: String, // Explains why this framework works and conversion psychology
    val isCustom: Boolean = false,
    val usageCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ChatbotRole(
    val title: String,
    val subtitle: String,
    val defaultModel: String,
    val iconName: String,
    val systemInstruction: String
) {
    STRATEGIST(
        title = "Outreach Strategist",
        subtitle = "Cold Email Psychology & High CTR",
        defaultModel = "gemini-3.5-flash",
        iconName = "psychology",
        systemInstruction = "You are an elite B2B Cold Outreach Strategist and Copywriter. You advise on response rates, micro-commitment survey hooks, PAS / BAB frameworks, deliverability optimization, and email sequencing. Use Google Search grounding when up-to-date industry data or competitor benchmarks are relevant."
    ),
    MARKET_RESEARCHER(
        title = "Deep Market & Maps Researcher",
        subtitle = "Enterprise B2B & Google Maps Lead Intelligence",
        defaultModel = "gemini-3.1-pro-preview",
        iconName = "map",
        systemInstruction = "You are a senior Market Researcher and Lead Intelligence analyst. You analyze companies, pain points, competitor gaps, and extract high-value local business insights using Google Maps and Google Search grounding."
    ),
    FAST_COPYWRITER(
        title = "Rapid Hook Crafter",
        subtitle = "Ultra-Fast Subject Lines & A/B Variants",
        defaultModel = "gemini-3.1-flash-lite-preview",
        iconName = "bolt",
        systemInstruction = "You are a high-speed copywriter specialized in punchy 3-6 word email subject lines, A/B test variants, 10-second micro-surveys, and 2-sentence Day+3 follow-up nudges."
    )
}

enum class ExperimentStatus(val label: String) {
    DRAFTING("Drafting"),
    ACTIVE_TESTING("Live A/B Testing"),
    COMPLETED("Winner Declared")
}

@Entity(tableName = "template_variation_experiments")
data class TemplateVariationExperiment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long? = null,
    val title: String,
    val hypothesis: String,
    val targetAudience: String = "B2B Decision Makers",
    val testedElement: String = "Subject Hook & Micro-Survey",
    
    // Variation A (Control)
    val variantAName: String = "Variation A (Control)",
    val variantAPsychologyAngle: PsychologyAngle = PsychologyAngle.PROBLEM_AGITATION,
    val variantASubjectLine: String,
    val variantASubjectAlt: String = "",
    val variantABody: String,
    val variantASurveyHook: String = "",
    val variantAFollowUpNudge: String = "",
    val variantAPerfSent: Int = 0,
    val variantAPerfOpened: Int = 0,
    val variantAPerfClicked: Int = 0,
    val variantAPerfReplied: Int = 0,
    val variantAPerfBounced: Int = 0,
    
    // Variation B (Challenger)
    val variantBName: String = "Variation B (Challenger)",
    val variantBPsychologyAngle: PsychologyAngle = PsychologyAngle.SURVEY_HOOK,
    val variantBSubjectLine: String,
    val variantBSubjectAlt: String = "",
    val variantBBody: String,
    val variantBSurveyHook: String = "",
    val variantBFollowUpNudge: String = "",
    val variantBPerfSent: Int = 0,
    val variantBPerfOpened: Int = 0,
    val variantBPerfClicked: Int = 0,
    val variantBPerfReplied: Int = 0,
    val variantBPerfBounced: Int = 0,
    
    val status: ExperimentStatus = ExperimentStatus.ACTIVE_TESTING,
    val declaredWinner: String? = null, // "A", "B", or null
    val customWinnerReason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalSent: Int get() = variantAPerfSent + variantBPerfSent
    
    val openRateA: Float get() = if (variantAPerfSent > 0) (variantAPerfOpened.toFloat() / variantAPerfSent.toFloat()) * 100f else 0f
    val openRateB: Float get() = if (variantBPerfSent > 0) (variantBPerfOpened.toFloat() / variantBPerfSent.toFloat()) * 100f else 0f
    val openRateDiff: Float get() = openRateB - openRateA

    val clickRateA: Float get() = if (variantAPerfOpened > 0) (variantAPerfClicked.toFloat() / variantAPerfOpened.toFloat()) * 100f else 0f
    val clickRateB: Float get() = if (variantBPerfOpened > 0) (variantBPerfClicked.toFloat() / variantBPerfOpened.toFloat()) * 100f else 0f
    val clickRateDiff: Float get() = clickRateB - clickRateA

    val replyRateA: Float get() = if (variantAPerfSent > 0) (variantAPerfReplied.toFloat() / variantAPerfSent.toFloat()) * 100f else 0f
    val replyRateB: Float get() = if (variantBPerfSent > 0) (variantBPerfReplied.toFloat() / variantBPerfSent.toFloat()) * 100f else 0f
    val replyRateDiff: Float get() = replyRateB - replyRateA

    val effectiveWinner: String? get() {
        if (!declaredWinner.isNullOrBlank()) return declaredWinner
        if (variantAPerfSent < 5 && variantBPerfSent < 5) return null
        return when {
            replyRateB > replyRateA + 2f -> "B"
            replyRateA > replyRateB + 2f -> "A"
            openRateB > openRateA + 5f -> "B"
            openRateA > openRateB + 5f -> "A"
            else -> null
        }
    }

    val statisticalConfidence: Int get() {
        if (totalSent < 10) return 35 + (totalSent * 3).coerceAtMost(30)
        val diff = Math.abs(replyRateDiff) * 2f + Math.abs(openRateDiff)
        val sampleFactor = (totalSent.toFloat() / 80f).coerceAtMost(1f)
        val score = (55f + diff * 1.2f * sampleFactor).coerceIn(50f, 99f)
        return score.toInt()
    }

    val winnerSummary: String get() {
        val winner = effectiveWinner
        return when (winner) {
            "B" -> {
                val lift = if (replyRateA > 0) ((replyRateB - replyRateA) / replyRateA) * 100f else replyRateB
                "Variation B is outperforming with a +${"%.1f".format(replyRateDiff)}% reply rate lift (${"%.0f".format(lift)}% relative increase at $statisticalConfidence% confidence)."
            }
            "A" -> {
                val lift = if (replyRateB > 0) ((replyRateA - replyRateB) / replyRateB) * 100f else replyRateA
                "Variation A (Control) is outperforming with a +${"%.1f".format(-replyRateDiff)}% reply rate lead at $statisticalConfidence% confidence."
            }
            else -> "A/B test in progress. Sample sizes are gathering to establish statistical significance."
        }
    }
}

