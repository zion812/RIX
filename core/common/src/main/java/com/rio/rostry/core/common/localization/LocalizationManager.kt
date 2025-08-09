package com.rio.rostry.core.common.localization

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import com.rio.rostry.core.common.model.Language
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Localization manager for multi-language support in rural India
 */
@Singleton
class LocalizationManager @Inject constructor(
    private val context: Context
) {

    private var currentLanguage: Language = Language.ENGLISH

    /**
     * Set application language
     */
    fun setLanguage(language: Language) {
        currentLanguage = language
        updateAppLocale(language)
    }

    /**
     * Get current language
     */
    fun getCurrentLanguage(): Language {
        return currentLanguage
    }

    /**
     * Update application locale
     */
    private fun updateAppLocale(language: Language) {
        val locale = Locale(language.code)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))

        context.createConfigurationContext(configuration)
    }

    /**
     * Get localized string with formatting
     */
    fun getString(resourceId: Int, vararg formatArgs: Any): String {
        return try {
            context.getString(resourceId, *formatArgs)
        } catch (e: Exception) {
            "String not found"
        }
    }

    /**
     * Get localized breed name
     */
    fun getLocalizedBreedName(breedKey: String): String {
        return when (currentLanguage) {
            Language.TELUGU -> getTeluguBreedName(breedKey)
            Language.HINDI -> getHindiBreedName(breedKey)
            Language.ENGLISH -> getEnglishBreedName(breedKey)
        }
    }

    /**
     * Get localized region name
     */
    fun getLocalizedRegionName(regionKey: String): String {
        return when (currentLanguage) {
            Language.TELUGU -> getTeluguRegionName(regionKey)
            Language.HINDI -> getHindiRegionName(regionKey)
            Language.ENGLISH -> getEnglishRegionName(regionKey)
        }
    }

    /**
     * Get localized district name
     */
    fun getLocalizedDistrictName(districtKey: String): String {
        return when (currentLanguage) {
            Language.TELUGU -> getTeluguDistrictName(districtKey)
            Language.HINDI -> getHindiDistrictName(districtKey)
            Language.ENGLISH -> getEnglishDistrictName(districtKey)
        }
    }

    /**
     * Get localized fowl gender
     */
    fun getLocalizedGender(gender: String): String {
        return when (currentLanguage) {
            Language.TELUGU -> when (gender.lowercase()) {
                "male" -> "మగ"
                "female" -> "ఆడ"
                else -> "తెలియదు"
            }
            Language.HINDI -> when (gender.lowercase()) {
                "male" -> "नर"
                "female" -> "मादा"
                else -> "अज्ञात"
            }
            Language.ENGLISH -> when (gender.lowercase()) {
                "male" -> "Male"
                "female" -> "Female"
                else -> "Unknown"
            }
        }
    }

    /**
     * Get localized age category
     */
    fun getLocalizedAgeCategory(ageCategory: String): String {
        return when (currentLanguage) {
            Language.TELUGU -> when (ageCategory.lowercase()) {
                "chick" -> "కోడిపిల్ల"
                "juvenile" -> "యువ"
                "adult" -> "పెద్ద"
                "senior" -> "వృద్ధ"
                else -> "తెలియదు"
            }
            Language.HINDI -> when (ageCategory.lowercase()) {
                "chick" -> "चूजा"
                "juvenile" -> "युवा"
                "adult" -> "वयस्क"
                "senior" -> "बुजुर्ग"
                else -> "अज्ञात"
            }
            Language.ENGLISH -> when (ageCategory.lowercase()) {
                "chick" -> "Chick"
                "juvenile" -> "Juvenile"
                "adult" -> "Adult"
                "senior" -> "Senior"
                else -> "Unknown"
            }
        }
    }

    /**
     * Get localized currency format
     */
    fun formatCurrency(amount: Double): String {
        return when (currentLanguage) {
            Language.TELUGU -> "₹${String.format("%.0f", amount)}"
            Language.HINDI -> "₹${String.format("%.0f", amount)}"
            Language.ENGLISH -> "₹${String.format("%,.0f", amount)}"
        }
    }

    /**
     * Get localized date format
     */
    fun formatDate(date: Date): String {
        val dateFormat = when (currentLanguage) {
            Language.TELUGU -> java.text.SimpleDateFormat("dd/MM/yyyy", Locale("te"))
            Language.HINDI -> java.text.SimpleDateFormat("dd/MM/yyyy", Locale("hi"))
            Language.ENGLISH -> java.text.SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH)
        }
        return dateFormat.format(date)
    }

    /**
     * Get localized time format
     */
    fun formatTime(date: Date): String {
        val timeFormat = when (currentLanguage) {
            Language.TELUGU -> java.text.SimpleDateFormat("HH:mm", Locale("te"))
            Language.HINDI -> java.text.SimpleDateFormat("HH:mm", Locale("hi"))
            Language.ENGLISH -> java.text.SimpleDateFormat("h:mm a", Locale.ENGLISH)
        }
        return timeFormat.format(date)
    }

    /**
     * Get localized number format
     */
    fun formatNumber(number: Long): String {
        return when (currentLanguage) {
            Language.TELUGU -> {
                // Telugu number formatting (Indian numbering system)
                String.format(Locale("te"), "%,d", number)
            }
            Language.HINDI -> {
                // Hindi number formatting (Indian numbering system)
                String.format(Locale("hi"), "%,d", number)
            }
            Language.ENGLISH -> {
                // English number formatting (Indian numbering system)
                val formatter = java.text.NumberFormat.getNumberInstance(Locale("en", "IN"))
                formatter.format(number)
            }
        }
    }

    // Telugu breed names
    private fun getTeluguBreedName(breedKey: String): String {
        return when (breedKey.lowercase()) {
            "aseel" -> "అసీల్"
            "kadaknath" -> "కడక్‌నాథ్"
            "desi" -> "దేశీ"
            "brahma" -> "బ్రహ్మ"
            "leghorn" -> "లెగ్‌హార్న్"
            "rhode_island_red" -> "రోడ్ ఐలాండ్ రెడ్"
            "cochin" -> "కోచిన్"
            "silkie" -> "సిల్కీ"
            else -> breedKey.replaceFirstChar { it.uppercase() }
        }
    }

    // Hindi breed names
    private fun getHindiBreedName(breedKey: String): String {
        return when (breedKey.lowercase()) {
            "aseel" -> "असील"
            "kadaknath" -> "कड़कनाथ"
            "desi" -> "देसी"
            "brahma" -> "ब्रह्मा"
            "leghorn" -> "लेगहॉर्न"
            "rhode_island_red" -> "रोड आइलैंड रेड"
            "cochin" -> "कोचिन"
            "silkie" -> "सिल्की"
            else -> breedKey.replaceFirstChar { it.uppercase() }
        }
    }

    // English breed names
    private fun getEnglishBreedName(breedKey: String): String {
        return when (breedKey.lowercase()) {
            "aseel" -> "Aseel"
            "kadaknath" -> "Kadaknath"
            "desi" -> "Desi"
            "brahma" -> "Brahma"
            "leghorn" -> "Leghorn"
            "rhode_island_red" -> "Rhode Island Red"
            "cochin" -> "Cochin"
            "silkie" -> "Silkie"
            else -> breedKey.replaceFirstChar { it.uppercase() }
        }
    }

    // Telugu region names
    private fun getTeluguRegionName(regionKey: String): String {
        return when (regionKey.lowercase()) {
            "andhra_pradesh" -> "ఆంధ్రప్రదేశ్"
            "telangana" -> "తెలంగాణ"
            "other" -> "ఇతర"
            else -> regionKey.replaceFirstChar { it.uppercase() }
        }
    }

    // Hindi region names
    private fun getHindiRegionName(regionKey: String): String {
        return when (regionKey.lowercase()) {
            "andhra_pradesh" -> "आंध्र प्रदेश"
            "telangana" -> "तेलंगाना"
            "other" -> "अन्य"
            else -> regionKey.replaceFirstChar { it.uppercase() }
        }
    }

    // English region names
    private fun getEnglishRegionName(regionKey: String): String {
        return when (regionKey.lowercase()) {
            "andhra_pradesh" -> "Andhra Pradesh"
            "telangana" -> "Telangana"
            "other" -> "Other"
            else -> regionKey.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    // Telugu district names
    private fun getTeluguDistrictName(districtKey: String): String {
        return when (districtKey.lowercase()) {
            "guntur" -> "గుంటూరు"
            "krishna" -> "కృష్ణ"
            "west_godavari" -> "పశ్చిమ గోదావరి"
            "east_godavari" -> "తూర్పు గోదావరి"
            "hyderabad" -> "హైదరాబాద్"
            "warangal" -> "వరంగల్"
            "khammam" -> "ఖమ్మం"
            "nalgonda" -> "నల్గొండ"
            else -> districtKey.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    // Hindi district names
    private fun getHindiDistrictName(districtKey: String): String {
        return when (districtKey.lowercase()) {
            "guntur" -> "गुंटूर"
            "krishna" -> "कृष्णा"
            "west_godavari" -> "पश्चिम गोदावरी"
            "east_godavari" -> "पूर्व गोदावरी"
            "hyderabad" -> "हैदराबाद"
            "warangal" -> "वारंगल"
            "khammam" -> "खम्मम"
            "nalgonda" -> "नलगोंडा"
            else -> districtKey.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    // English district names
    private fun getEnglishDistrictName(districtKey: String): String {
        return when (districtKey.lowercase()) {
            "guntur" -> "Guntur"
            "krishna" -> "Krishna"
            "west_godavari" -> "West Godavari"
            "east_godavari" -> "East Godavari"
            "hyderabad" -> "Hyderabad"
            "warangal" -> "Warangal"
            "khammam" -> "Khammam"
            "nalgonda" -> "Nalgonda"
            else -> districtKey.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    /**
     * Get supported languages
     */
    fun getSupportedLanguages(): List<Language> {
        return listOf(Language.ENGLISH, Language.TELUGU, Language.HINDI)
    }

    /**
     * Detect language from text (basic implementation)
     */
    fun detectLanguage(text: String): Language {
        return when {
            text.any { it in '\u0C00'..'\u0C7F' } -> Language.TELUGU // Telugu Unicode range
            text.any { it in '\u0900'..'\u097F' } -> Language.HINDI // Hindi Unicode range
            else -> Language.ENGLISH
        }
    }

    /**
     * Get RTL support (not needed for current languages but future-ready)
     */
    fun isRTL(): Boolean {
        return false // Telugu, Hindi, and English are all LTR
    }
}
