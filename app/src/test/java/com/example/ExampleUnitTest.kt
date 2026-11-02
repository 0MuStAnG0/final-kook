package com.example

import com.example.data.MockData
import com.example.utils.SecurityUtils
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
        // Valid 10-digit Iranian National Code: 0084545941
        // 0*10 + 0*9 + 8*8 + 4*7 + 5*6 + 4*5 + 5*4 + 9*3 + 4*2 = 197
        // 197 % 11 = 10 -> check digit = 11 - 10 = 1
        assertTrue(ValidationUtils.isValidIranianNationalCode("0084545941"))

        // Verify that invalid 001 / 123 prefixes without valid checksum are rejected
        assertFalse(ValidationUtils.isValidIranianNationalCode("0012345670"))
        assertFalse(ValidationUtils.isValidIranianNationalCode("1234567890"))

        // 11-digit corporate legal entity ID
        assertTrue(ValidationUtils.isValidIranianNationalCode("10100456789"))

        // Repeated digits should fail
        assertFalse(ValidationUtils.isValidIranianNationalCode("1111111111"))
        assertFalse(ValidationUtils.isValidIranianNationalCode("0000000000"))

        // Invalid length
        assertFalse(ValidationUtils.isValidIranianNationalCode("123"))
    }

    @Test
    fun testInvoiceHmacSignatureAndVerification() {
        val sig1 = SecurityUtils.generateInvoiceSignature("INV-101", 500000L, true)
        val sig2 = SecurityUtils.generateInvoiceSignature("INV-101", 500000L, true)
        val sig3 = SecurityUtils.generateInvoiceSignature("INV-102", 500000L, true)
        assertEquals(sig1, sig2)
        assertNotEquals(sig1, sig3)

        assertTrue(SecurityUtils.verifyInvoiceSignature("INV-101", 500000L, true, sig1))
        assertFalse(SecurityUtils.verifyInvoiceSignature("INV-101", 600000L, true, sig1))
        assertFalse(SecurityUtils.verifyInvoiceSignature("INV-101", 500000L, false, sig1))
    }

    @Test
    fun testBrandAuthorMappingAndVideoStreamsInMockData() {
        val novinPost = MockData.posts.first { it.id == "post_novin_1" }
        val hakopianPost = MockData.posts.first { it.id == "post_hakopian_1" }
        val dorsaPost = MockData.posts.first { it.id == "post_dorsa_1" }
        val maralPost = MockData.posts.first { it.id == "post_maral_1" }
        val melliPost = MockData.posts.first { it.id == "post_melli_1" }
        val bodySpinnerPost = MockData.posts.first { it.id == "post_bodyspinner_1" }
        val lcmanPost = MockData.posts.first { it.id == "post_lcman_1" }

        assertEquals("brand_novin_leather", novinPost.author.id)
        assertEquals("brand_hakopian", hakopianPost.author.id)
        assertEquals("brand_dorian_leather", dorsaPost.author.id)
        assertEquals("brand_maral_leather", maralPost.author.id)
        assertEquals("brand_melli_shoes", melliPost.author.id)
        assertEquals("brand_body_spinner", bodySpinnerPost.author.id)
        assertEquals("brand_lcman", lcmanPost.author.id)

        // Ensure no expired Aparat wmsAuthSign or 403 gtv-videos-bucket URLs remain
        MockData.posts.forEach { post ->
            val video = post.videoUrl
            assertNotNull(video)
            assertFalse(video!!.contains("wmsAuthSign"))
            assertFalse(video.contains("gtv-videos-bucket"))
            assertTrue(video.startsWith("https://"))
        }

        // Verify PlayerManager.resolvePlayableVideoUrl rewrites legacy 403 hosts to verified streams
        val legacyGtv = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        val resolvedGtv = com.example.utils.PlayerManager.resolvePlayableVideoUrl(legacyGtv, "post_novin_1")
        assertNotNull(resolvedGtv)
        assertFalse(resolvedGtv!!.contains("gtv-videos-bucket"))
        assertTrue(com.example.utils.PlayerManager.VERIFIED_STREAM_URLS.contains(resolvedGtv))

        // Verify Codec2 C2_OMITTED (status 6) emulator detection
        assertTrue(
            com.example.utils.PlayerManager.isEmulatorWithRestrictedCodec2(
                fingerprint = "google/sdk_gphone64_x86_64/emu64xa:14/UE1A.230829.036.A4/11886954:userdebug/dev-keys",
                model = "sdk_gphone64_x86_64",
                hardware = "ranchu"
            )
        )
        assertFalse(
            com.example.utils.PlayerManager.isEmulatorWithRestrictedCodec2(
                fingerprint = "samsung/dm3qxxx/dm3q:14/UP1A.231005.007/S918BXXS3BWK5:user/release-keys",
                model = "SM-S918B",
                hardware = "qcom",
                product = "dm3qxxx",
                brand = "samsung",
                device = "dm3q",
                board = "kalama"
            )
        )
    }
}
