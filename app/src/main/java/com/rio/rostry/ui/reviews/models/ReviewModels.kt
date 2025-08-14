package com.rio.rostry.ui.reviews.models

import java.util.Date

data class Review(
    val id: String,
    val reviewerId: String,
    val reviewerName: String,
    val reviewerAvatar: String? = null,
    val rating: Float,
    val title: String,
    val content: String,
    val date: Date,
    val isVerifiedPurchase: Boolean = false,
    val helpfulCount: Int = 0,
    val images: List<String> = emptyList()
)

data class SellerRating(
    val sellerId: String,
    val sellerName: String,
    val averageRating: Float,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>, // star rating to count
    val responseRate: Float,
    val responseTime: String,
    val joinDate: Date,
    val totalSales: Int,
    val isVerified: Boolean = false
)

data class ReviewFilter(
    val rating: Int? = null,
    val verifiedOnly: Boolean = false,
    val sortBy: ReviewSortBy = ReviewSortBy.NEWEST
)

enum class ReviewSortBy {
    NEWEST,
    OLDEST,
    HIGHEST_RATING,
    LOWEST_RATING,
    MOST_HELPFUL
}