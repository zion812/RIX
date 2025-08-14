package com.rio.rostry.core.common.utils

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Validates data integrity and business rules
 */
@Singleton
class DataValidator @Inject constructor() {
    
    /**
     * Validate basic entity fields
     */
    fun validateBasicFields(id: String, createdAt: Long): Boolean {
        return id.isNotBlank() && createdAt <= System.currentTimeMillis()
    }
    
    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Validate phone number format (Indian)
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        return cleanPhone.length == 10 && cleanPhone.matches(Regex("^[6-9].*"))
    }
    
    /**
     * Validate fowl name
     */
    fun isValidFowlName(name: String): Boolean {
        return name.isNotBlank() && 
               name.length >= 2 && 
               name.length <= 50 &&
               name.matches(Regex("^[a-zA-Z0-9\\s\\-_]+$"))
    }
    
    /**
     * Validate coin amount
     */
    fun isValidCoinAmount(amount: Int): Boolean {
        return amount > 0 && amount <= 10000
    }
    
    /**
     * Validate price in rupees
     */
    fun isValidPrice(price: Double): Boolean {
        return price > 0 && price <= 1000000 // Max 10 lakh rupees
    }
    
    /**
     * Validate image file size (in bytes)
     */
    fun isValidImageSize(sizeBytes: Long): Boolean {
        val maxSizeMB = 10
        return sizeBytes <= maxSizeMB * 1024 * 1024
    }
    
    /**
     * Validate video file size (in bytes)
     */
    fun isValidVideoSize(sizeBytes: Long): Boolean {
        val maxSizeMB = 50
        return sizeBytes <= maxSizeMB * 1024 * 1024
    }
}