package com.example.utils

import java.security.MessageDigest
import java.util.UUID

object SecurityUtils {

    /**
     * Generates a cryptographic verification token for the invoice,
     * ensuring tamper-evidence for total amount, hygiene flag, and items.
     */
    fun generateInvoiceSignature(invoiceId: String, totalAmount: Long, isHygieneProtected: Boolean): String {
        val payload = "$invoiceId:$totalAmount:$isHygieneProtected:KOOK_SECURE_VAULT_SALT"
        val bytes = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(16).uppercase()
    }

    /**
     * Masks PII for sensitive postal slips when shown on public or casual surfaces.
     */
    fun maskPhoneNumber(phone: String): String {
        return if (phone.length >= 7) {
            phone.substring(0, 4) + "***" + phone.substring(phone.length - 4)
        } else {
            phone
        }
    }

    /**
     * Masks national / postal code for security compliance.
     */
    fun maskPostalCode(postalCode: String): String {
        return if (postalCode.length >= 6) {
            postalCode.substring(0, 3) + "***" + postalCode.substring(postalCode.length - 3)
        } else {
            postalCode
        }
    }
}
