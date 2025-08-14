package com.rio.rostry.navigation

import org.junit.Test
import org.junit.Assert.*

/**
 * Basic navigation tests for RIO Phase 1
 */
class NavigationTest {

    @Test
    fun testDestinationConstants() {
        // Verify all navigation destinations are properly defined
    assertEquals("auth", ROSTRYDestinations.AUTH)
    assertEquals("loading", ROSTRYDestinations.LOADING)
    assertEquals("general_dashboard", ROSTRYDestinations.GENERAL_DASHBOARD)
    assertEquals("farmer_dashboard", ROSTRYDestinations.FARMER_DASHBOARD)
    assertEquals("enthusiast_dashboard", ROSTRYDestinations.ENTHUSIAST_DASHBOARD)
    assertEquals("fowl_management", ROSTRYDestinations.FOWL_MANAGEMENT)
    assertEquals("marketplace", ROSTRYDestinations.MARKETPLACE)
    assertEquals("family_tree", ROSTRYDestinations.FAMILY_TREE)
    assertEquals("chat", ROSTRYDestinations.CHAT)
    assertEquals("profile", ROSTRYDestinations.PROFILE)
    assertEquals("payment", ROSTRYDestinations.PAYMENT)
    }

    @Test
    fun testDestinationUniqueness() {
        // Verify all destinations are unique
        val destinations = listOf(
            ROSTRYDestinations.AUTH,
            ROSTRYDestinations.LOADING,
            ROSTRYDestinations.GENERAL_DASHBOARD,
            ROSTRYDestinations.FARMER_DASHBOARD,
            ROSTRYDestinations.ENTHUSIAST_DASHBOARD,
            ROSTRYDestinations.FOWL_MANAGEMENT,
            ROSTRYDestinations.MARKETPLACE,
            ROSTRYDestinations.FAMILY_TREE,
            ROSTRYDestinations.CHAT,
            ROSTRYDestinations.PROFILE,
            ROSTRYDestinations.PAYMENT
        )
        
        assertEquals(destinations.size, destinations.toSet().size)
    }
}
