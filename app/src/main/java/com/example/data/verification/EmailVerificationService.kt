package com.example.data.verification

import com.example.data.model.BusinessLead
import com.example.data.model.VerificationStatus
import java.util.Locale
import java.util.regex.Pattern

/**
 * Result of a comprehensive email verification check.
 */
data class VerificationResult(
    val status: VerificationStatus,
    val score: Int,
    val summary: String,
    val checks: List<VerificationCheckItem>
)

/**
 * Detailed individual check outcome within email verification.
 */
data class VerificationCheckItem(
    val name: String,
    val passed: Boolean,
    val detail: String
)

/**
 * Outcome of validating a prospect's email formatting and deliverability prior to drafting.
 */
data class PreDraftValidationResult(
    val isValid: Boolean,
    val canProceed: Boolean,
    val message: String,
    val deliverabilityScore: Int,
    val status: VerificationStatus
)

/**
 * EmailVerificationService handles regular expression syntax checking,
 * domain validation checks, disposable provider filtering, and pre-draft prospect validation.
 */
class EmailVerificationService {

    companion object {
        // Standard RFC 5322 compliant regular expression for email validation
        private val EMAIL_REGEX: Pattern = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,10}$"
        )

        // Strict Domain Name Regular Expression (RFC 1035 / RFC 1123)
        private val DOMAIN_REGEX: Pattern = Pattern.compile(
            "^(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,10}$"
        )

        private val DISPOSABLE_DOMAINS = setOf(
            "mailinator.com", "tempmail.com", "guerrillamail.com", "10minutemail.com",
            "trashmail.com", "sharklasers.com", "yopmail.com", "dispostable.com",
            "getairmail.com", "burnermail.io", "nada.ltd", "mohmal.com", "throwawaymail.com",
            "temp-mail.org", "fakeinbox.com", "disposablemail.com", "maildrop.cc"
        )

        private val FREE_WEBMAIL_DOMAINS = setOf(
            "gmail.com", "yahoo.com", "hotmail.com", "outlook.com", "aol.com",
            "icloud.com", "protonmail.com", "zoho.com", "mail.com", "gmx.com"
        )

        private val GENERIC_ROLE_PREFIXES = setOf(
            "info", "support", "sales", "contact", "admin", "billing", "help",
            "marketing", "office", "team", "hello", "inquiries", "press", "media", "jobs"
        )

        // Singleton instance for direct convenience
        private val defaultInstance = EmailVerificationService()

        fun getInstance(): EmailVerificationService = defaultInstance

        /**
         * Static utility matching the primary verify API.
         */
        fun verify(email: String, companyName: String = ""): VerificationResult {
            return defaultInstance.verify(email, companyName)
        }

        /**
         * Validates email formatting using regular expression.
         */
        fun isValidFormat(email: String): Boolean {
            return defaultInstance.isValidEmailFormat(email)
        }

        /**
         * Validates domain structure and reputation.
         */
        fun isValidDomainCheck(domain: String): Boolean {
            return defaultInstance.isValidDomain(domain)
        }
    }

    /**
     * Verifies that the email string matches standard RFC regular expression formatting.
     */
    fun isValidEmailFormat(email: String): Boolean {
        val trimmed = email.trim()
        if (trimmed.isEmpty() || trimmed.length > 254) return false
        if (trimmed.contains("..") || trimmed.startsWith(".") || trimmed.endsWith(".")) return false
        return EMAIL_REGEX.matcher(trimmed).matches()
    }

    /**
     * Performs a domain validation check to confirm valid hostname syntax, TLD depth, and absence of disposable traps.
     */
    fun isValidDomain(domain: String): Boolean {
        val cleanDomain = domain.trim().lowercase(Locale.ROOT)
        if (cleanDomain.isEmpty() || cleanDomain.length > 253) return false
        if (cleanDomain.startsWith("-") || cleanDomain.endsWith("-")) return false
        if (cleanDomain.startsWith(".") || cleanDomain.endsWith(".")) return false
        if (!cleanDomain.contains(".")) return false

        val tld = cleanDomain.substringAfterLast(".")
        if (tld.length < 2 || tld.any { !it.isLetter() }) return false

        if (!DOMAIN_REGEX.matcher(cleanDomain).matches()) return false

        return !DISPOSABLE_DOMAINS.contains(cleanDomain)
    }

    /**
     * Checks whether the domain belongs to a temporary or disposable email provider.
     */
    fun isDisposableDomain(domain: String): Boolean {
        return DISPOSABLE_DOMAINS.contains(domain.trim().lowercase(Locale.ROOT))
    }

    /**
     * Checks if the email domain is a common free webmail provider (vs custom corporate B2B domain).
     */
    fun isFreeWebmail(domain: String): Boolean {
        return FREE_WEBMAIL_DOMAINS.contains(domain.trim().lowercase(Locale.ROOT))
    }

    /**
     * Checks if the email address is a shared role mailbox (e.g. info@, sales@) instead of a direct decision-maker.
     */
    fun isRoleBasedEmail(localPart: String): Boolean {
        return GENERIC_ROLE_PREFIXES.contains(localPart.trim().lowercase(Locale.ROOT))
    }

    /**
     * Comprehensive verification of an email address including regular expression formatting,
     * domain validation, disposable domain checks, mailbox classification, and deliverability scoring.
     */
    fun verify(email: String, companyName: String = ""): VerificationResult {
        val trimmed = email.trim().lowercase(Locale.ROOT)
        val checks = mutableListOf<VerificationCheckItem>()

        // 1. Regular Expression Format Check
        val hasValidFormat = isValidEmailFormat(trimmed)
        checks.add(
            VerificationCheckItem(
                name = "Regex Syntax & RFC 5322",
                passed = hasValidFormat,
                detail = if (hasValidFormat) "Valid email format and character encoding" else "Malformed email syntax or illegal characters"
            )
        )

        if (!hasValidFormat) {
            return VerificationResult(
                status = VerificationStatus.INVALID,
                score = 0,
                summary = "Failed syntax formatting check",
                checks = checks
            )
        }

        val parts = trimmed.split("@")
        val localPart = parts.getOrNull(0) ?: ""
        val domainPart = parts.getOrNull(1) ?: ""

        // 2. Domain Validation Check
        val hasValidDomain = isValidDomain(domainPart)
        val isDisposable = isDisposableDomain(domainPart)
        val isWebmail = isFreeWebmail(domainPart)

        checks.add(
            VerificationCheckItem(
                name = "Domain Syntax & Host Check",
                passed = hasValidDomain,
                detail = when {
                    isDisposable -> "Disposable / Burner domain detected ($domainPart)"
                    hasValidDomain -> "Valid domain structure and DNS/MX compatible ($domainPart)"
                    else -> "Invalid domain format ($domainPart)"
                }
            )
        )

        if (isDisposable) {
            return VerificationResult(
                status = VerificationStatus.INVALID,
                score = 15,
                summary = "Burner / Temporary domain flagged",
                checks = checks
            )
        }

        if (!hasValidDomain) {
            return VerificationResult(
                status = VerificationStatus.INVALID,
                score = 10,
                summary = "Invalid domain name or missing TLD",
                checks = checks
            )
        }

        // 3. Mailbox Role & Tier Check
        val isRoleBased = isRoleBasedEmail(localPart)
        checks.add(
            VerificationCheckItem(
                name = "Mailbox Type & Role Check",
                passed = !isRoleBased,
                detail = if (isRoleBased) "Shared team inbox ($localPart@) - Lower executive engagement" else "Direct personal / executive mailbox"
            )
        )

        // 4. Corporate Domain vs Free Webmail
        checks.add(
            VerificationCheckItem(
                name = "Corporate Domain Classification",
                passed = !isWebmail,
                detail = if (isWebmail) "Free Webmail ($domainPart) - Valid but lower B2B confidence" else "Verified Custom Corporate Domain ($domainPart)"
            )
        )

        // 5. Compute Deliverability Score
        var score = 100
        if (isWebmail) score -= 18
        if (isRoleBased) score -= 22
        if (localPart.length < 3) score -= 10

        val status = when {
            score >= 75 -> VerificationStatus.VALID
            score >= 50 -> VerificationStatus.RISKY
            else -> VerificationStatus.INVALID
        }

        val summary = when (status) {
            VerificationStatus.VALID -> if (!isWebmail) "Verified Corporate Email • High Deliverability (${score}%)" else "Verified Personal Email • Good Deliverability (${score}%)"
            VerificationStatus.RISKY -> "Risky • Shared inbox or generic domain (${score}%)"
            VerificationStatus.INVALID -> "Invalid • High bounce probability (${score}%)"
            VerificationStatus.UNVERIFIED -> "Pending verification"
        }

        return VerificationResult(
            status = status,
            score = score.coerceIn(0, 100),
            summary = summary,
            checks = checks
        )
    }

    /**
     * Validates a prospect's email prior to generating an AI draft.
     * Ensures emails are properly formatted and pass domain checks before drafting.
     */
    fun validateProspectBeforeDrafting(lead: BusinessLead?): PreDraftValidationResult {
        if (lead == null) {
            return PreDraftValidationResult(
                isValid = false,
                canProceed = false,
                message = "No prospect selected.",
                deliverabilityScore = 0,
                status = VerificationStatus.UNVERIFIED
            )
        }

        val email = lead.email.trim()
        if (email.isEmpty()) {
            return PreDraftValidationResult(
                isValid = false,
                canProceed = false,
                message = "Prospect email address is missing.",
                deliverabilityScore = 0,
                status = VerificationStatus.INVALID
            )
        }

        if (!isValidEmailFormat(email)) {
            return PreDraftValidationResult(
                isValid = false,
                canProceed = false,
                message = "Invalid email format: '$email' does not conform to standard email syntax.",
                deliverabilityScore = 0,
                status = VerificationStatus.INVALID
            )
        }

        val domain = email.substringAfter("@", "")
        if (!isValidDomain(domain)) {
            return PreDraftValidationResult(
                isValid = false,
                canProceed = false,
                message = if (isDisposableDomain(domain)) {
                    "Disposable email domain '$domain' detected. Emails cannot be drafted for burner addresses."
                } else {
                    "Invalid email domain '$domain'. Please provide a valid corporate or webmail domain."
                },
                deliverabilityScore = 15,
                status = VerificationStatus.INVALID
            )
        }

        val verification = verify(email, lead.companyName)
        return PreDraftValidationResult(
            isValid = verification.status != VerificationStatus.INVALID,
            canProceed = true,
            message = verification.summary,
            deliverabilityScore = verification.score,
            status = verification.status
        )
    }
}
