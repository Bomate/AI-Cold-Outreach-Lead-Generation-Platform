package com.example.data.verification

/**
 * Backward-compatible wrapper object delegating to [EmailVerificationService].
 */
object EmailVerifier {

    private val service = EmailVerificationService.getInstance()

    fun verify(email: String, companyName: String = ""): VerificationResult {
        return service.verify(email, companyName)
    }

    fun isValidEmailFormat(email: String): Boolean {
        return service.isValidEmailFormat(email)
    }

    fun isValidDomain(domain: String): Boolean {
        return service.isValidDomain(domain)
    }
}

