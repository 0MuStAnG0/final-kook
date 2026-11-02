package com.example.utils

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {

    private fun deriveSigningSecret(): ByteArray {
        val domainKey = "${com.example.BuildConfig.APPLICATION_ID}:KOOK_ESCROW_LEDGER_V3"
        return MessageDigest.getInstance("SHA-256").digest(domainKey.toByteArray(Charsets.UTF_8))
    }

    /**
     * Generates an HMAC-SHA256 cryptographic verification token for the invoice,
     * ensuring tamper-evidence for invoice ID, total amount, and hygiene flag.
     */
    fun generateInvoiceSignature(invoiceId: String, totalAmount: Long, isHygieneProtected: Boolean): String {
        val payload = "$invoiceId|$totalAmount|$isHygieneProtected"
        return try {
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(deriveSigningSecret(), "HmacSHA256"))
            val bytes = mac.doFinal(payload.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        } catch (_: Exception) {
            val bytes = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
        }
    }

    /**
     * Verifies an invoice signature using constant-time byte comparison to prevent timing attacks.
     */
    fun verifyInvoiceSignature(
        invoiceId: String,
        totalAmount: Long,
        isHygieneProtected: Boolean,
        signature: String?
    ): Boolean {
        if (signature.isNullOrBlank()) return false
        val expected = generateInvoiceSignature(invoiceId, totalAmount, isHygieneProtected)
        return MessageDigest.isEqual(
            expected.toByteArray(Charsets.UTF_8),
            signature.trim().uppercase().toByteArray(Charsets.UTF_8)
        )
    }

    /**
     * Masks PII for sensitive postal slips when shown on public or casual surfaces.
     */
    fun maskPhoneNumber(phone: String): String {
        val clean = phone.trim()
        return if (clean.length >= 7) {
            clean.substring(0, 4) + "***" + clean.substring(clean.length - 4)
        } else {
            "***"
        }
    }

    /**
     * Masks national / postal code for security compliance.
     */
    fun maskPostalCode(postalCode: String): String {
        val clean = postalCode.trim()
        return if (clean.length >= 6) {
            clean.substring(0, 3) + "***" + clean.substring(clean.length - 3)
        } else {
            "***"
        }
    }
}

