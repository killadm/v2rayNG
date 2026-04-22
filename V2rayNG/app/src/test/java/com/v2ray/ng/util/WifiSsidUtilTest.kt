package com.v2ray.ng.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiSsidUtilTest {

    @Test
    fun `normalizeSsid strips quotes and blanks`() {
        assertEquals("Home WiFi", WifiSsidUtil.normalizeSsid("\"Home WiFi\""))
        assertEquals("Office", WifiSsidUtil.normalizeSsid(" Office "))
        assertNull(WifiSsidUtil.normalizeSsid(" "))
        assertNull(WifiSsidUtil.normalizeSsid("<unknown ssid>"))
    }

    @Test
    fun `parseSsidList normalizes and deduplicates`() {
        assertEquals(
            listOf("Home", "Office"),
            WifiSsidUtil.parseSsidList("\"Home\", Office,\nHome")
        )
    }

    @Test
    fun `matches uses normalized current ssid and configured list`() {
        assertTrue(WifiSsidUtil.matches("\"Home\"", listOf("Office", "Home")))
        assertFalse(WifiSsidUtil.matches("Cafe", listOf("Office", "Home")))
        assertFalse(WifiSsidUtil.matches(null, listOf("Home")))
    }
}
