package com.rio.rostry.ui.reviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rio.rostry.core.fieldtesting.FieldTestingManager
import com.rio.rostry.ui.reviews.models.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Reviews and Ratings Screen
 * Displays seller ratings, customer reviews, and allows writing new reviews
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    navController: NavController,
    sellerId: String? = null,
    fieldTestingManager: FieldTestingManager? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var sellerRating by remember { mutableStateOf<SellerRating?>(null) }
    var showWriteReview by remember { mutableStateOf(false) }
    var reviewFilter by remember { mutableStateOf(ReviewFilter()) }
    
    // Track screen view
    LaunchedEffect(Unit) {
        fieldTestingManager?.trackUserAction("reviews_screen_viewed", "seller_id=$sellerId")
        
        // Load demo data
        reviews = generateDemoReviews()
        sellerRating = generateDemoSellerRating(sellerId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reviews & Ratings") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        showWriteReview = true
                        fieldTestingManager?.trackUserAction("write_review_clicked")
                    }) {
                        Icon(Icons.Default.RateReview, contentDescription = "Write Review")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    showWriteReview = true
                    fieldTestingManager?.trackUserAction("fab_write_review_clicked")
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Write Review")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { 
                        selectedTab = 0
                        fieldTestingManager?.trackUserAction("reviews_tab_overview")
                    },
                    text = { Text("Overview") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { 
                        selectedTab = 1
                        fieldTestingManager?.trackUserAction("reviews_tab_all_reviews")
                    },
                    text = { Text("All Reviews") }
                )
            }
            
            // Tab Content
            when (selectedTab) {
                0 -> OverviewTab(sellerRating, reviews.take(3))
                1 -> AllReviewsTab(reviews, reviewFilter) { newFilter ->
                    reviewFilter = newFilter
                    fieldTestingManager?.trackUserAction("filter_changed")
                }
            }
        }
    }
    
    if (showWriteReview) {
        WriteReviewDialog(
            onDismiss = { showWriteReview = false },
            onReviewSubmitted = { review ->
                reviews = reviews + review
                showWriteReview = false
                fieldTestingManager?.trackUserAction("review_submitted", "rating=${review.rating}")
            }
        )
    }
}

@Composable
private fun SellerRatingCard(sellerRating: SellerRating) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sellerRating.sellerName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            if (index < sellerRating.averageRating.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${sellerRating.averageRating}/5.0",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Text(
                    text = "${sellerRating.totalReviews} reviews • ${sellerRating.totalSales} sales",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (sellerRating.isVerified) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified Seller",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Verified",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    review: Review,
    onHelpfulClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.userName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (review.verified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Verified,
                                contentDescription = "Verified Purchase",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    RatingStars(rating = review.rating)
                }
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(review.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Review Title
            if (review.title.isNotBlank()) {
                Text(
                    text = review.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            // Review Comment
            Text(
                text = review.comment,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onHelpfulClick) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Helpful (${review.helpful})")
                }
                
                TextButton(onClick = { /* Report review */ }) {
                    Icon(
                        Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Report")
                }
            }
        }
    }
}

@Composable
private fun EmptyReviewsCard(tabIndex: Int) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.RateReview,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when (tabIndex) {
                    0 -> "No reviews yet"
                    1 -> "No reviews yet"
                    else -> "No reviews found"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = when (tabIndex) {
                    0 -> "Be the first to leave a review"
                    1 -> "Be the first to leave a review"
                    else -> "Try adjusting your filters"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun WriteReviewDialog(
    onDismiss: () -> Unit,
    onReviewSubmitted: (Review) -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var title by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }
    var selectedTransaction by remember { mutableStateOf<String?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write a Review") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text("Rate your experience:")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            IconButton(
                                onClick = { rating = index + 1 }
                            ) {
                                Icon(
                                    if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700)
                                )
                            }
                        }
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Review Title") },
                        placeholder = { Text("Summarize your experience") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your Review") },
                        placeholder = { Text("Share details about your experience") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val review = Review(
                        id = "review_${System.currentTimeMillis()}",
                        userId = "current_user",
                        userName = "Field Test User",
                        rating = rating,
                        title = title,
                        comment = comment,
                        date = Date(),
                        verified = true,
                        helpful = 0
                    )
                    onReviewSubmitted(review)
                },
                enabled = title.isNotBlank() && comment.isNotBlank()
            ) {
                Text("Submit Review")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortChip(
    label: String,
    value: String,
    currentSort: String,
    onSortChange: (String) -> Unit,
    fieldTestingManager: FieldTestingManager?
) {
    FilterChip(
        onClick = { 
            onSortChange(value)
            fieldTestingManager?.trackUserAction("reviews_sort_changed", "sort=$value")
        },
        label = { Text(label) },
        selected = currentSort == value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChip(
    label: String,
    value: Int,
    currentFilter: Int,
    onFilterChange: (Int) -> Unit,
    fieldTestingManager: FieldTestingManager?
) {
    FilterChip(
        onClick = { 
            onFilterChange(value)
            fieldTestingManager?.trackUserAction("reviews_filter_changed", "filter=$value")
        },
        label = { Text(label) },
        selected = currentFilter == value
    )
}

@Composable
private fun RatingStars(rating: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            Icon(
                if (index < rating) Icons.Default.Star else Icons.Default.StarBorder,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Demo data generation functions
private fun generateDemoReviews(): List<Review> {
    return listOf(
        Review(
            id = "R001",
            userId = "U001",
            userName = "Rajesh Kumar",
            rating = 5,
            title = "Excellent quality birds!",
            comment = "Purchased 5 Aseel chicks and they are all healthy and growing well. Great genetics and very responsive seller.",
            date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -2) }.time,
            verified = true,
            helpful = 12
        ),
        Review(
            id = "R002",
            userId = "U002",
            userName = "Priya Sharma",
            rating = 4,
            title = "Good breeding stock",
            comment = "The roosters I bought have good fighting spirit and excellent build. Delivery was on time.",
            date = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -5) }.time,
            verified = true,
            helpful = 8
        ),
        Review(
            id = "R003",
            userId = "U003",
            userName = "Mohammed Ali",
            rating = 5,
            title = "Highly recommended!",
            comment = "Best quality birds in the region. The seller is very knowledgeable and provides excellent after-sales support.",
            date = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -1) }.time,
            verified = true,
            helpful = 15
        ),
        Review(
            id = "R004",
            userId = "U004",
            userName = "Lakshmi Devi",
            rating = 3,
            title = "Average experience",
            comment = "Birds are okay but not as described. Some health issues noticed after a few weeks.",
            date = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, -2) }.time,
            verified = false,
            helpful = 3
        ),
        Review(
            id = "R005",
            userId = "U005",
            userName = "Suresh Reddy",
            rating = 5,
            title = "Outstanding service",
            comment = "Professional handling, healthy birds, and great customer service. Will definitely buy again.",
            date = Calendar.getInstance().apply { add(Calendar.MONTH, -1) }.time,
            verified = true,
            helpful = 20
        )
    )
}

private fun generateDemoReviewSummary(reviews: List<Review>): ReviewSummary {
    val totalReviews = reviews.size
    val averageRating = reviews.map { it.rating }.average()
    val ratingDistribution = reviews.groupBy { it.rating }.mapValues { it.value.size }
    
    return ReviewSummary(
        averageRating = averageRating,
        totalReviews = totalReviews,
        ratingDistribution = ratingDistribution
    )
}

// Data classes
data class Review(
    val id: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val title: String,
    val comment: String,
    val date: Date,
    val verified: Boolean,
    val helpful: Int
)

data class ReviewSummary(
    val averageRating: Double,
    val totalReviews: Int,
    val ratingDistribution: Map<Int, Int>
)