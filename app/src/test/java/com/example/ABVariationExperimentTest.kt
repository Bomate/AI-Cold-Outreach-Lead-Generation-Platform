package com.example

import com.example.data.model.BusinessLead
import com.example.data.model.ExperimentStatus
import com.example.data.model.PsychologyAngle
import com.example.data.model.TemplateVariationExperiment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ABVariationExperimentTest {

    @Test
    fun testExperimentPerformanceMetricCalculations() {
        val experiment = TemplateVariationExperiment(
            id = 1L,
            title = "Survey vs Problem Agitation",
            hypothesis = "Testing micro-survey CTR lift",
            targetAudience = "Founders",
            testedElement = "Opening Hook",
            variantAName = "Control (PAS)",
            variantAPsychologyAngle = PsychologyAngle.PROBLEM_AGITATION,
            variantASubjectLine = "quick question",
            variantASubjectAlt = "follow up",
            variantABody = "Problem Agitation Body",
            variantASurveyHook = "Is pain point costing hours?",
            variantAFollowUpNudge = "Following up",
            variantASentCount = 100,
            variantAOpenedCount = 40,
            variantAClickedCount = 20,
            variantARepliedCount = 10,
            variantBName = "Challenger (Survey Hook)",
            variantBPsychologyAngle = PsychologyAngle.SURVEY_HOOK,
            variantBSubjectLine = "quick 10-sec diagnostic",
            variantBSubjectAlt = "benchmark question",
            variantBBody = "Diagnostic Survey Hook Body",
            variantBSurveyHook = "Which is costing more?",
            variantBFollowUpNudge = "Quick follow-up",
            variantBSentCount = 100,
            variantBOpenedCount = 60,
            variantBClickedCount = 35,
            variantBRepliedCount = 25,
            status = ExperimentStatus.ACTIVE_TESTING
        )

        // Variant A Metrics
        assertEquals(40.0, experiment.variantAOpenRate, 0.01)
        assertEquals(20.0, experiment.variantAClickRate, 0.01)
        assertEquals(10.0, experiment.variantAReplyRate, 0.01)

        // Variant B Metrics
        assertEquals(60.0, experiment.variantBOpenRate, 0.01)
        assertEquals(35.0, experiment.variantBClickRate, 0.01)
        assertEquals(25.0, experiment.variantBReplyRate, 0.01)

        // Total Sent
        assertEquals(200, experiment.totalSent)

        // Reply Lift: (25 - 10) / 10 * 100 = +150%
        assertEquals(150.0, experiment.replyRateLift, 0.01)

        // Open Lift: (60 - 40) / 40 * 100 = +50%
        assertEquals(50.0, experiment.openRateLift, 0.01)

        // Winning Variant: B is clearly outperforming A
        val calculatedLeader = if (experiment.variantBReplyRate > experiment.variantAReplyRate) "B" else "A"
        assertEquals("B", calculatedLeader)
    }

    @Test
    fun testVariableSubstitution() {
        val lead = BusinessLead(
            id = 5L,
            companyName = "Apex Logistics",
            contactName = "Sarah Jenkins",
            email = "sarah@apexlogistics.com",
            role = "VP of Operations",
            industry = "Supply Chain & Freight",
            companySize = "50-200",
            painPoint = "driver scheduling delays",
            targetGoal = "reduce route turnaround times by 30%",
            website = "https://apexlogistics.com",
            preferredAngle = PsychologyAngle.SURVEY_HOOK
        )

        val templateString = "Hi {{contactName}}, noticed {{companyName}}'s work in {{industry}}. Is {{painPoint}} slowing down your goal to {{targetGoal}}?"

        val substituted = templateString
            .replace("{{companyName}}", lead.companyName)
            .replace("{{contactName}}", lead.contactName)
            .replace("{{industry}}", lead.industry)
            .replace("{{painPoint}}", lead.painPoint)
            .replace("{{targetGoal}}", lead.targetGoal)
            .replace("{{role}}", lead.role)

        assertTrue(substituted.contains("Sarah Jenkins"))
        assertTrue(substituted.contains("Apex Logistics"))
        assertTrue(substituted.contains("driver scheduling delays"))
        assertTrue(substituted.contains("reduce route turnaround times by 30%"))
    }
}
