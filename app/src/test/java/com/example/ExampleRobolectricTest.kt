package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.VerificationStatus
import com.example.data.verification.EmailVerifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("OutreachAI", appName)
    }

    @Test
    fun `verify corporate email passes validation`() {
        val result = EmailVerifier.verify("marcus@apexflow.io", "ApexFlow")
        assertEquals(VerificationStatus.VALID, result.status)
        assertTrue(result.score >= 80)
    }

    @Test
    fun `verify invalid email fails validation`() {
        val result = EmailVerifier.verify("invalid-email-address", "Test")
        assertEquals(VerificationStatus.INVALID, result.status)
        assertEquals(0, result.score)
    }

    @Test
    fun `verify disposable email detected`() {
        val result = EmailVerifier.verify("testuser@mailinator.com", "Test")
        assertEquals(VerificationStatus.INVALID, result.status)
    }
}
