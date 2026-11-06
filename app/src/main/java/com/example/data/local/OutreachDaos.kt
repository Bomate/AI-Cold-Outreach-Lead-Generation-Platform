package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.TypeConverter
import androidx.room.Update
import com.example.data.model.AlertType
import com.example.data.model.BusinessLead
import com.example.data.model.EmailDraft
import com.example.data.model.ExperimentStatus
import com.example.data.model.OutreachAlert
import com.example.data.model.OutreachStatus
import com.example.data.model.OutreachTemplate
import com.example.data.model.PsychologyAngle
import com.example.data.model.TemplateVariationExperiment
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromVerificationStatus(status: VerificationStatus): String = status.name

    @TypeConverter
    fun toVerificationStatus(value: String): VerificationStatus = runCatching {
        VerificationStatus.valueOf(value)
    }.getOrDefault(VerificationStatus.UNVERIFIED)

    @TypeConverter
    fun fromOutreachStatus(status: OutreachStatus): String = status.name

    @TypeConverter
    fun toOutreachStatus(value: String): OutreachStatus = runCatching {
        OutreachStatus.valueOf(value)
    }.getOrDefault(OutreachStatus.DRAFT)

    @TypeConverter
    fun fromPsychologyAngle(angle: PsychologyAngle): String = angle.name

    @TypeConverter
    fun toPsychologyAngle(value: String): PsychologyAngle = runCatching {
        PsychologyAngle.valueOf(value)
    }.getOrDefault(PsychologyAngle.SURVEY_HOOK)

    @TypeConverter
    fun fromAlertType(type: AlertType): String = type.name

    @TypeConverter
    fun toAlertType(value: String): AlertType = runCatching {
        AlertType.valueOf(value)
    }.getOrDefault(AlertType.EMAIL_SENT)

    @TypeConverter
    fun fromExperimentStatus(status: ExperimentStatus): String = status.name

    @TypeConverter
    fun toExperimentStatus(value: String): ExperimentStatus = runCatching {
        ExperimentStatus.valueOf(value)
    }.getOrDefault(ExperimentStatus.ACTIVE_TESTING)
}

@Dao
interface LeadDao {
    @Query("SELECT * FROM business_leads ORDER BY createdAt DESC")
    fun getAllLeads(): Flow<List<BusinessLead>>

    @Query("SELECT * FROM business_leads WHERE id = :id")
    suspend fun getLeadById(id: Long): BusinessLead?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: BusinessLead): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<BusinessLead>)

    @Update
    suspend fun updateLead(lead: BusinessLead)

    @Delete
    suspend fun deleteLead(lead: BusinessLead)

    @Query("DELETE FROM business_leads WHERE id = :id")
    suspend fun deleteLeadById(id: Long)

    @Query("SELECT COUNT(*) FROM business_leads")
    suspend fun getLeadCount(): Int
}

@Dao
interface DraftDao {
    @Query("SELECT * FROM email_drafts ORDER BY createdAt DESC")
    fun getAllDrafts(): Flow<List<EmailDraft>>

    @Query("SELECT * FROM email_drafts WHERE leadId = :leadId ORDER BY createdAt DESC")
    fun getDraftsForLead(leadId: Long): Flow<List<EmailDraft>>

    @Query("SELECT * FROM email_drafts WHERE id = :id")
    suspend fun getDraftById(id: Long): EmailDraft?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: EmailDraft): Long

    @Update
    suspend fun updateDraft(draft: EmailDraft)

    @Delete
    suspend fun deleteDraft(draft: EmailDraft)

    @Query("DELETE FROM email_drafts WHERE id = :id")
    suspend fun deleteDraftById(id: Long)

    @Query("UPDATE email_drafts SET status = :status, sentAt = :sentAt WHERE id = :draftId")
    suspend fun markDraftSent(draftId: Long, status: OutreachStatus = OutreachStatus.SENT, sentAt: Long = System.currentTimeMillis())

    @Query("UPDATE email_drafts SET status = :status, openedAt = :openedAt WHERE id = :draftId")
    suspend fun markDraftOpened(draftId: Long, status: OutreachStatus = OutreachStatus.OPENED, openedAt: Long = System.currentTimeMillis())

    @Query("UPDATE email_drafts SET status = :status, clickedAt = :clickedAt WHERE id = :draftId")
    suspend fun markDraftClicked(draftId: Long, status: OutreachStatus = OutreachStatus.CLICKED, clickedAt: Long = System.currentTimeMillis())

    @Query("UPDATE email_drafts SET status = :status, repliedAt = :repliedAt WHERE id = :draftId")
    suspend fun markDraftReplied(draftId: Long, status: OutreachStatus = OutreachStatus.REPLIED, repliedAt: Long = System.currentTimeMillis())
}

@Dao
interface AlertDao {
    @Query("SELECT * FROM outreach_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<OutreachAlert>>

    @Query("SELECT * FROM outreach_alerts WHERE isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadAlerts(): Flow<List<OutreachAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: OutreachAlert): Long

    @Query("UPDATE outreach_alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAlertRead(id: Long)

    @Query("UPDATE outreach_alerts SET isRead = 1")
    suspend fun markAllAlertsRead()

    @Query("DELETE FROM outreach_alerts")
    suspend fun clearAlerts()
}

@Dao
interface TemplateDao {
    @Query("SELECT * FROM outreach_templates ORDER BY isCustom DESC, usageCount DESC, createdAt DESC")
    fun getAllTemplates(): Flow<List<OutreachTemplate>>

    @Query("SELECT * FROM outreach_templates WHERE psychologyAngle = :angle ORDER BY usageCount DESC")
    fun getTemplatesByAngle(angle: PsychologyAngle): Flow<List<OutreachTemplate>>

    @Query("SELECT * FROM outreach_templates WHERE id = :id")
    suspend fun getTemplateById(id: Long): OutreachTemplate?

    @Query("SELECT COUNT(*) FROM outreach_templates")
    suspend fun getTemplateCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: OutreachTemplate): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<OutreachTemplate>)

    @Update
    suspend fun updateTemplate(template: OutreachTemplate)

    @Delete
    suspend fun deleteTemplate(template: OutreachTemplate)

    @Query("DELETE FROM outreach_templates WHERE id = :id")
    suspend fun deleteTemplateById(id: Long)

    @Query("UPDATE outreach_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementTemplateUsage(id: Long)
}

@Dao
interface ExperimentDao {
    @Query("SELECT * FROM template_variation_experiments ORDER BY updatedAt DESC")
    fun getAllExperiments(): Flow<List<TemplateVariationExperiment>>

    @Query("SELECT * FROM template_variation_experiments WHERE id = :id")
    suspend fun getExperimentById(id: Long): TemplateVariationExperiment?

    @Query("SELECT * FROM template_variation_experiments WHERE templateId = :templateId ORDER BY updatedAt DESC")
    fun getExperimentsForTemplate(templateId: Long): Flow<List<TemplateVariationExperiment>>

    @Query("SELECT COUNT(*) FROM template_variation_experiments")
    suspend fun getExperimentCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiment(experiment: TemplateVariationExperiment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExperiments(experiments: List<TemplateVariationExperiment>)

    @Update
    suspend fun updateExperiment(experiment: TemplateVariationExperiment)

    @Delete
    suspend fun deleteExperiment(experiment: TemplateVariationExperiment)

    @Query("DELETE FROM template_variation_experiments WHERE id = :id")
    suspend fun deleteExperimentById(id: Long)

    @Query("""
        UPDATE template_variation_experiments 
        SET variantAPerfSent = variantAPerfSent + :sent,
            variantAPerfOpened = variantAPerfOpened + :opened,
            variantAPerfClicked = variantAPerfClicked + :clicked,
            variantAPerfReplied = variantAPerfReplied + :replied,
            updatedAt = :now
        WHERE id = :id
    """)
    suspend fun recordVariantAPerformance(id: Long, sent: Int, opened: Int, clicked: Int, replied: Int, now: Long = System.currentTimeMillis())

    @Query("""
        UPDATE template_variation_experiments 
        SET variantBPerfSent = variantBPerfSent + :sent,
            variantBPerfOpened = variantBPerfOpened + :opened,
            variantBPerfClicked = variantBPerfClicked + :clicked,
            variantBPerfReplied = variantBPerfReplied + :replied,
            updatedAt = :now
        WHERE id = :id
    """)
    suspend fun recordVariantBPerformance(id: Long, sent: Int, opened: Int, clicked: Int, replied: Int, now: Long = System.currentTimeMillis())

    @Query("UPDATE template_variation_experiments SET status = :status, declaredWinner = :winner, customWinnerReason = :reason, updatedAt = :now WHERE id = :id")
    suspend fun declareWinner(id: Long, status: ExperimentStatus, winner: String, reason: String, now: Long = System.currentTimeMillis())

    @Query("UPDATE template_variation_experiments SET variantAPerfSent = 0, variantAPerfOpened = 0, variantAPerfClicked = 0, variantAPerfReplied = 0, variantAPerfBounced = 0, variantBPerfSent = 0, variantBPerfOpened = 0, variantBPerfClicked = 0, variantBPerfReplied = 0, variantBPerfBounced = 0, status = 'ACTIVE_TESTING', declaredWinner = NULL, updatedAt = :now WHERE id = :id")
    suspend fun resetExperimentMetrics(id: Long, now: Long = System.currentTimeMillis())
}
