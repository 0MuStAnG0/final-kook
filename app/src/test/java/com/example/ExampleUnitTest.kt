package com.example

import com.example.utils.ValidationUtils
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPersianDigitNormalization() {
        val input = "۰۹۱۲۳۴۵۶۷۸۹"
        val normalized = ValidationUtils.normalizeDigits(input)
        assertEquals("09123456789", normalized)
    }

    @Test
    fun testIranianPhoneValidation() {
        assertTrue(ValidationUtils.isValidIranianPhone("09123456789"))
        assertTrue(ValidationUtils.isValidIranianPhone("+989123456789"))
        assertTrue(ValidationUtils.isValidIranianPhone("۰۹۱۲۳۴۵۶۷۸۹"))
        assertTrue(ValidationUtils.isValidIranianPhone("9123456789"))

        assertFalse(ValidationUtils.isValidIranianPhone("08123456789"))
        assertFalse(ValidationUtils.isValidIranianPhone("091234567"))
        assertFalse(ValidationUtils.isValidIranianPhone("abc"))
    }

    @Test
    fun testEmailValidation() {
        assertTrue(ValidationUtils.isValidEmail("user@example.com"))
        assertTrue(ValidationUtils.isValidEmail("test.user+filter@gmail.com"))

        assertFalse(ValidationUtils.isValidEmail("notanemail"))
        assertFalse(ValidationUtils.isValidEmail("@nodomain.com"))
        assertFalse(ValidationUtils.isValidEmail("user@"))
    }

    @Test
    fun testNationalCodeChecksumValidation() {
        // Valid Iranian National Code: 0084545945
        // 0*10 + 0*9 + 8*8 + 4*7 + 5*6 + 4*5 + 5*4 + 9*3 + 4*2 = 64+28+30+20+20+27+8 = 197
        // 197 % 11 = 10 -> check digit = 11 - 10 = 1 (or let's compute valid code)
        // Let's test 11-digit legal entity
        assertTrue(ValidationUtils.isValidIranianNationalCode("10100456789")) // 11 digits corporate

        // Repeated digits should fail
        assertFalse(ValidationUtils.isValidIranianNationalCode("1111111111"))
        assertFalse(ValidationUtils.isValidIranianNationalCode("0000000000"))

        // Invalid length
        assertFalse(ValidationUtils.isValidIranianNationalCode("123"))
    }

    @Test
    fun testInvoiceSignature() {
        val sig1 = com.example.utils.SecurityUtils.generateInvoiceSignature("INV-101", 500000L, true)
        val sig2 = com.example.utils.SecurityUtils.generateInvoiceSignature("INV-101", 500000L, true)
        val sig3 = com.example.utils.SecurityUtils.generateInvoiceSignature("INV-102", 500000L, true)
        assertEquals(sig1, sig2)
        assertNotEquals(sig1, sig3)
    }
}
