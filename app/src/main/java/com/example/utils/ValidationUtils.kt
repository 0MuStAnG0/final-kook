package com.example.utils

object ValidationUtils {

    /**
     * Converts Persian and Arabic digits to standard ASCII English digits.
     */
    fun normalizeDigits(input: String): String {
        val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        val arabic = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        var result = input
        for (i in 0..9) {
            result = result.replace(persian[i], ('0' + i)).replace(arabic[i], ('0' + i))
        }
        return result
    }

    /**
     * Validates an Iranian mobile phone number.
     * Accepts formats: 09123456789, +989123456789, 00989123456789, 9123456789
     * Handles Persian/Arabic digits and spaces.
     */
    fun isValidIranianPhone(phone: String): Boolean {
        val normalized = normalizePhoneNumber(phone)
        return normalized.matches(Regex("^09\\d{9}$"))
    }

    /**
     * Normalizes an Iranian phone number to standard 11-digit format starting with 09.
     */
    fun normalizePhoneNumber(phone: String): String {
        var clean = normalizeDigits(phone).replace("[^0-9+]".toRegex(), "")
        if (clean.startsWith("+98")) {
            clean = "0" + clean.substring(3)
        } else if (clean.startsWith("0098")) {
            clean = "0" + clean.substring(4)
        } else if (clean.startsWith("98") && clean.length == 12) {
            clean = "0" + clean.substring(2)
        } else if (clean.startsWith("9") && clean.length == 10) {
            clean = "0$clean"
        }
        return clean
    }

    /**
     * Validates email / Gmail address format.
     */
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")
        return trimmed.matches(emailRegex)
    }

    /**
     * Validates Iranian National Code (کد ملی ۱۰ رقمی با محاسبه رقم کنترل)
     * and Iranian Corporate Registration / Legal Entity ID (شناسه ملی ۱۱ رقمی).
     */
    fun isValidIranianNationalCode(input: String): Boolean {
        val clean = normalizeDigits(input).filter { it.isDigit() }
        
        // 11 digits: Valid for corporate legal entity ID (شناسه ملی اشخاص حقوقی)
        if (clean.length == 11) {
            return true
        }

        // 10 digits: Individual National ID with checksum
        if (clean.length != 10) {
            return false
        }

        // Reject repeated digits like 0000000000, 1111111111, etc.
        val firstChar = clean[0]
        if (clean.all { it == firstChar }) {
            return false
        }

        var sum = 0
        for (i in 0 until 9) {
            val digit = clean[i] - '0'
            sum += digit * (10 - i)
        }

        val remainder = sum % 11
        val checkDigit = clean[9] - '0'

        return if (remainder < 2) {
            checkDigit == remainder
        } else {
            checkDigit == (11 - remainder)
        }
    }

    /**
     * Formats Iranian National Code into standard 3-6-1 display pattern (XXX-XXXXXX-X)
     */
    fun formatNationalCode(code: String): String {
        val clean = normalizeDigits(code).filter { it.isDigit() }
        return if (clean.length == 10) {
            "${clean.substring(0, 3)}-${clean.substring(3, 9)}-${clean.substring(9)}"
        } else {
            clean
        }
    }
}
