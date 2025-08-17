package com.rio.rostry.core.database.converters

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FowlConvertersTest {
    
    private val converters = FowlConverters()
    
    @Test
    fun `test string list conversion`() {
        val originalList = listOf("item1", "item2", "item3")
        val jsonString = converters.fromStringList(originalList)
        val convertedList = converters.toStringList(jsonString)
        
        assertEquals(originalList, convertedList)
    }
    
    @Test
    fun `test empty string list conversion`() {
        val originalList = emptyList<String>()
        val jsonString = converters.fromStringList(originalList)
        val convertedList = converters.toStringList(jsonString)
        
        assertTrue(convertedList.isEmpty())
    }
    
    @Test
    fun `test null string list conversion`() {
        val jsonString = converters.fromStringList(null)
        val convertedList = converters.toStringList(jsonString)
        
        assertNull(jsonString)
        assertTrue(convertedList.isEmpty())
    }
    
    @Test
    fun `test string map conversion`() {
        val originalMap = mapOf("key1" to "value1", "key2" to "value2")
        val jsonString = converters.fromStringMap(originalMap)
        val convertedMap = converters.toStringMap(jsonString)
        
        assertEquals(originalMap, convertedMap)
    }
    
    @test
    fun `test empty string map conversion`() {
        val originalMap = emptyMap<String, String>()
        val jsonString = converters.fromStringMap(originalMap)
        val convertedMap = converters.toStringMap(jsonString)
        
        assertTrue(convertedMap.isEmpty())
    }
    
    @Test
    fun `test null string map conversion`() {
        val jsonString = converters.fromStringMap(null)
        val convertedMap = converters.toStringMap(jsonString)
        
        assertNull(jsonString)
        assertTrue(convertedMap.isEmpty())
    }
    
    @Test
    fun `test date conversion`() {
        val originalDate = Date()
        val timestamp = converters.fromDate(originalDate)
        val convertedDate = converters.toDate(timestamp)
        
        assertEquals(originalDate, convertedDate)
    }
    
    @Test
    fun `test null date conversion`() {
        val timestamp = converters.fromDate(null)
        val convertedDate = converters.toDate(timestamp)
        
        assertNull(timestamp)
        assertNull(convertedDate)
    }
}