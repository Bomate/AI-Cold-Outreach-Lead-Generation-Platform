package com.example

import com.example.data.model.BusinessLead
import com.example.data.model.PsychologyAngle
import com.example.data.model.VerificationStatus
import com.example.data.verification.EmailVerificationService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class EmailVerificationServiceTest {

    private lateinit var service: EmailVerificationService

    @Before
    fun setUp() {
        service = EmailVerificationService()
    }

    @Test
    fun testValidEmailRegexFormat() {
        assertTrue(service.isValidEmailFormat("alex.chen@fintechflow.io"))
        assertTrue(service.isValidEmailFormat("sarah.kim@cloudscale.tech"))
        assertTrue(service.isValidEmailFormat("marcus.vance@supplyai.com"))
        assertTrue(service.isValidEmailFormat("lead_dev+sales@enterprise.co.uk"))
        assertTrue(service.isValidEmailFormat("contact@company.ai"))
    }

    @Test
    fun testInvalidEmailRegexFormat() {
        assertFalse(service.isValidEmailFormat(""))
        assertFalse(service.isValidEmailFormat("plainaddress"))
        assertFalse(service.isValidEmailFormat("@missingusername.com"))
        assertFalse(service.isValidEmailFormat("missingatsign.com"))
        assertFalse(service.isValidEmailFormat("username@"))
        assertFalse(service.isValidEmailFormat("username@.com"))
        assertFalse(service.isValidEmailFormat("user..name@company.com"))
        assertFalse(service.isValidEmailFormat("username@company..com"))
        assertFalse(service.isValidEmailFormat("user name@company.com"))
    }

    @Test
    fun testValidDomainValidation() {
        assertTrue(service.isValidDomain("fintechflow.io"))
        assertTrue(service.isValidDomain("cloudscale.tech"))
        assertTrue(service.isValidDomain("acme-corp.com"))
        assertTrue(service.isValidDomain("subdomain.company.org"))
        assertTrue(service.isValidDomain("startup.ai"))
    }

    @Test
    fun testInvalidDomainValidation() {
        assertFalse(service.isValidDomain(""))
        assertFalse(service.isValidDomain("nodotsindomain"))
        assertFalse(service.isValidDomain(".leadingdot.com"))
        assertFalse(service.isValidDomain("trailingdot.com."))
        assertFalse(service.isValidDomain("-leadinghyphen.com"))
        assertFalse(service.isValidDomain("invalidtld.c")) // TLD must be >= 2 characters
        assertFalse(service.isValidDomain("domain.123")) // TLD must be letters
    }

    @Test
    fun testDisposableDomainDetection() {
        assertTrue(service.isDisposableDomain("mailinator.com"))
        assertTrue(service.isDisposableDomain("tempmail.com"))
        assertTrue(service.isDisposableDomain("guerrillamail.com"))
        assertTrue(service.isDisposableDomain("10minutemail.com"))
        assertFalse(service.isDisposableDomain("fintechflow.io"))
        assertFalse(service.isDisposableDomain("gmail.com"))

        // Service.isValidDomain should also reject disposable domains
        assertFalse(service.isValidDomain("mailinator.com"))
        assertFalse(service.isValidDomain("tempmail.com"))
    }

    @Test
    fun testVerifyCorporateEmail() {
        val result = service.verify("alex.chen@fintechflow.io", "FintechFlow")
        assertEquals(VerificationStatus.VALID, result.status)
        assertTrue(result.score >= 80)
        assertTrue(result.checks.any { it.name.contains("Regex") && it.passed })
        assertTrue(result.checks.any { it.name.contains("Domain") && it.passed })
    }

    @Test
    fun testVerifyMalformedEmail() {
        val result = service.verify("invalid-email-address")
        assertEquals(VerificationStatus.INVALID, result.status)
        assertEquals(0, result.score)
        assertFalse(result.checks.first { it.name.contains("Regex") }.passed)
    }

    @Test
    fun testVerifyDisposableEmail() {
        val result = service.verify("scammer@mailinator.com")
        assertEquals(VerificationStatus.INVALID, result.status)
        assertTrue(result.summary.contains("Burner") || result.summary.contains("Temporary"))
    }

    @Test
    fun testValidateProspectBeforeDrafting_ValidProspect() {
        val lead = BusinessLead(
            id = 1L,
            companyName = "FintechFlow",
            contactName = "Alex Chen",
            email = "alex.chen@fintechflow.io",
            role = "Head of Engineering",
            industry = "Fintech",
            companySize = "50-200",
            painPoint = "Slow onboarding",
            targetGoal = "Automate API setup",
            website = "fintechflow.io",
            preferredAngle = PsychologyAngle.SURVEY_HOOK,
            verificationStatus = VerificationStatus.VALID,
            deliverabilityScore = 95
        )

        val outcome = service.validateProspectBeforeDrafting(lead)
        assertTrue(outcome.isValid)
        assertTrue(outcome.canProceed)
        assertEquals(VerificationStatus.VALID, outcome.status)
    }

    @Test
    fun testValidateProspectBeforeDrafting_MalformedEmail() {
        val lead = BusinessLead(
            id = 2L,
            companyName = "Bad Corp",
            contactName = "Test Contact",
            email = "not-a-valid-email",
            role = "Tester",
            industry = "Testing",
            companySize = "1-10",
            painPoint = "Testing pain point",
            targetGoal = "Testing goal",
            website = "badcorp.com",
            preferredAngle = PsychologyAngle.PROBLEM_AGITATION,
            verificationStatus = VerificationStatus.UNVERIFIED,
            deliverabilityScore = 0
        )

        val outcome = service.validateProspectBeforeDrafting(lead)
        assertFalse(outcome.isValid)
        assertFalse(outcome.canProceed)
        assertTrue(outcome.message.contains("Invalid email format"))
    }

    @Test
    fun testValidateProspectBeforeDrafting_DisposableEmail() {
        val lead = BusinessLead(
            id = 3L,
            companyName = "Spam Inc",
            contactName = "Spam User",
            email = "temp_user@tempmail.com",
            role = "Tester",
            industry = "Testing",
            companySize = "1-10",
            painPoint = "Spam issue",
            targetGoal = "None",
            website = "tempmail.com",
            preferredAngle = PsychologyAngle.RECIPROCITY_AUDIT,
            verificationStatus = VerificationStatus.UNVERIFIED,
            deliverabilityScore = 0
        )

        val outcome = service.validateProspectBeforeDrafting(lead)
        assertFalse(outcome.isValid)
        assertFalse(outcome.canProceed)
        assertTrue(outcome.message.contains("Disposable email domain"))
    }
}
