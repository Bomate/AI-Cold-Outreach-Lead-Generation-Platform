package com.example

import com.example.data.model.ChatbotRole
import com.example.data.model.PsychologyAngle
import com.example.data.remote.GeminiOutreachService
import com.example.data.verification.EmailVerificationService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessDiscoveryTest {

    @Test
    fun testDiscoverBusinessContactsFallback() = runBlocking {
        val results = GeminiOutreachService.discoverBusinessContacts(
            query = "B2B SaaS and AI Startups",
            location = "Austin, TX",
            industry = "Technology"
        )

        assertNotNull(results)
        assertTrue(results.isNotEmpty())
        val first = results.first()
        assertTrue(first.companyName.isNotBlank())
        assertTrue(first.email.contains("@"))
        assertTrue(first.painPoint.isNotBlank())
        assertTrue(first.isSelected)

        // Verify each discovered lead passes basic email format validation
        val emailService = EmailVerificationService.getInstance()
        results.forEach { lead ->
            assertTrue(emailService.isValidEmailFormat(lead.email))
        }
    }

    @Test
    fun testParseAndEnrichGoogleSheetFallback() = runBlocking {
        val sampleSheetContent = """
            Company, Contact Name, Email, Role, Website
            OmniFlow Systems, Sarah Jenkins, sarah@omniflow.tech, VP of Growth, omniflow.tech
            Horizon Fintech Group, Kenji Sato, kenji.sato@horizonfintech.com, CTO, horizonfintech.com
        """.trimIndent()

        val results = GeminiOutreachService.parseAndEnrichGoogleSheet(sampleSheetContent)

        assertNotNull(results)
        assertTrue(results.isNotEmpty())
        val first = results.first()
        assertEquals("OmniFlow Systems", first.companyName)
        assertEquals("Sarah Jenkins", first.contactName)
        assertEquals("sarah@omniflow.tech", first.email)
        assertTrue(first.inferredPainPoint.isNotBlank())
        assertNotNull(first.recommendedAngle)
    }

    @Test
    fun testChatbotRolesConfiguration() {
        assertEquals("gemini-3.5-flash", ChatbotRole.STRATEGIST.defaultModel)
        assertEquals("gemini-3.1-pro-preview", ChatbotRole.MARKET_RESEARCHER.defaultModel)
        assertEquals("gemini-3.1-flash-lite-preview", ChatbotRole.FAST_COPYWRITER.defaultModel)

        assertTrue(ChatbotRole.STRATEGIST.systemInstruction.contains("Cold Outreach Strategist"))
        assertTrue(ChatbotRole.MARKET_RESEARCHER.systemInstruction.contains("Google Maps"))
        assertTrue(ChatbotRole.FAST_COPYWRITER.systemInstruction.contains("high-speed copywriter"))
    }
}
