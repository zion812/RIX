package com.rio.rostry

import com.rio.rostry.auth.UserTier
import org.junit.Test
import org.junit.Assert.*

/**
 * Simple unit tests for basic functionality
 */
class SimpleTest {

    @Test
    fun userTier_enumValues_areCorrect() {
        // Given & When
        val tiers = UserTier.values()

        // Then
        assertEquals(3, tiers.size)
        assertTrue(tiers.contains(UserTier.GENERAL))
        assertTrue(tiers.contains(UserTier.FARMER))
        assertTrue(tiers.contains(UserTier.ENTHUSIAST))
    }

    @Test
    fun userTier_ordering_isCorrect() {
        // Given
        val general = UserTier.GENERAL
        val farmer = UserTier.FARMER
        val enthusiast = UserTier.ENTHUSIAST

        // Then
        assertTrue(general.ordinal < farmer.ordinal)
        assertTrue(farmer.ordinal < enthusiast.ordinal)
    }

    @Test
    fun basicMath_works() {
        // Given
        val a = 2
        val b = 3

        // When
        val result = a + b

        // Then
        assertEquals(5, result)
    }

    @Test
    fun stringOperations_work() {
        // Given
        val str1 = "Hello"
        val str2 = "World"

        // When
        val result = "$str1 $str2"

        // Then
        assertEquals("Hello World", result)
        assertTrue(result.contains("Hello"))
        assertTrue(result.contains("World"))
    }

    @Test
    fun listOperations_work() {
        // Given
        val list = mutableListOf<String>()

        // When
        list.add("item1")
        list.add("item2")

        // Then
        assertEquals(2, list.size)
        assertTrue(list.contains("item1"))
        assertTrue(list.contains("item2"))
    }
}
