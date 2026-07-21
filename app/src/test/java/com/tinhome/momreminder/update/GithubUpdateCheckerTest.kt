package com.tinhome.momreminder.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GithubUpdateCheckerTest {

    @Test
    fun `parses version code from standard tag`() {
        assertEquals(3, parseVersionCodeFromTag("v3"))
    }

    @Test
    fun `parses version code from multi-digit tag`() {
        assertEquals(42, parseVersionCodeFromTag("v42"))
    }

    @Test
    fun `returns null for tag without v prefix number`() {
        assertNull(parseVersionCodeFromTag("release-1"))
    }

    @Test
    fun `returns null for empty tag`() {
        assertNull(parseVersionCodeFromTag(""))
    }

    @Test
    fun `returns null for tag with only v`() {
        assertNull(parseVersionCodeFromTag("v"))
    }
}
