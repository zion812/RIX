package com.rio.rostry.core.data.model

import java.util.Date

/**
 * Compact timeline summary DTO for quick display during transfer flow
 * Contains only essential information to improve performance and reduce data transfer
 */
data class TimelineSummary(
    val recordType: String,
    val recordDate: Date,
    val description: String?,
    val proofCount: Int,
    val isVerified: Boolean = false
)