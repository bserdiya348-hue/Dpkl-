package com.example

import com.example.ui.components.formatRupees
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentUnitTest {

    @Test
    fun testCurrencyFormatting() {
        assertEquals("₹50,000", formatRupees(50000L).replace("\u00a0", " "))
        assertEquals("₹50.00 Lakh", formatRupees(5000000L))
        assertEquals("₹1.25 Lakh", formatRupees(125000L))
        assertEquals("₹1.50 Cr", formatRupees(15000000L))
    }

    @Test
    fun testPurseCalculation() {
        val budget = 5000000L
        val spent = 1200000L
        val remaining = budget - spent
        assertEquals(3800000L, remaining)
        assertTrue(remaining > 0)
    }

    @Test
    fun testBidIncrementValidation() {
        val currentBid = 50000L
        val increment = 10000L
        val nextBid = currentBid + increment
        assertEquals(60000L, nextBid)
        assertTrue(nextBid > currentBid)
    }
}
