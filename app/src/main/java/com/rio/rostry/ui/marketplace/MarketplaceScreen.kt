package com.rio.rostry.ui.marketplace

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.rio.rostry.core.database.di.DatabaseProvider
import com.rio.rostry.core.database.entities.FowlEntity
import com.rio.rostry.ui.marketplace.models.*
import com.rio.rostry.core.fieldtesting.FieldTestingManager
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import com.google.firebase.perf.FirebasePerformance

/**
 * Enhanced Marketplace Screen - Phase 2.1
 * Functional marketplace using existing fowl database
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    navController: NavController,
    fieldTestingManager: FieldTestingManager? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Track screen view for field testing
    LaunchedEffect(Unit) {
        fieldTestingManager?.trackUserAction("marketplace_screen_viewed")
    }
    var listings by remember { mutableStateOf<List<FowlEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showOrderDialog by remember { mutableStateOf(false) }
    var selectedListing by remember { mutableStateOf<FowlEntity?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Database setup
    val database = remember { DatabaseProvider.getDatabase(context) }

    // Load marketplace listings (fowls for sale)
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            // Get fowls that are marked for sale using the DAO directly
            val fowlDao = database.fowlDao()
            listings = fowlDao.getFowlsForSale()

            // If no listings exist, create some demo listings
            if (listings.isEmpty()) {
                val demoListings = createDemoMarketplaceListings()
                demoListings.forEach { fowl ->
                    fowlDao.insertFowl(fowl)
                }
                listings = fowlDao.getFowlsForSale()
            }
        } catch (e: Exception) {
            // Handle error
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Fowl Marketplace",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filters")
                    }
                    IconButton(onClick = {
                        // Instrument a search perf trace (placeholder until full search implemented)
                        try {
                            val trace =
                                FirebasePerformance.getInstance().newTrace("search_marketplace")
                            trace.start()
                            // Simulate lightweight search work window
                            scope.launch {
                                kotlinx.coroutines.delay(200)
                                trace.putAttribute("query_length", searchQuery.length.toString())
                                trace.stop()
                            }
                        } catch (_: Exception) {
                        }
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Enhanced Stats Section
            MarketplaceStatsSection(listings = listings)

            // Filter Section
            if (showFilters) {
                FilterSection(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it }
                )
            }

            // Content Section
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Loading marketplace...")
                        }
                    }
                } else if (listings.isEmpty()) {
                    EmptyMarketplaceState()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(listings.filter { fowl ->
                            when (selectedFilter) {
                                "All" -> true
                                "Under ₹1000" -> (fowl.price ?: 0.0) < 1000
                                "₹1000-5000" -> (fowl.price ?: 0.0) in 1000.0..5000.0
                                "Above ₹5000" -> (fowl.price ?: 0.0) > 5000
                                else -> fowl.breed.contains(selectedFilter, ignoreCase = true)
                            }
                        }) { listing ->
                            EnhancedMarketplaceListingCard(
                                listing = listing,
                                onContact = { listing ->
                                    // TODO: Open chat with seller
                                },
                                onViewDetails = { listing ->
                                    // TODO: Navigate to detailed view
                                },
                                onPlaceOrder = { listing ->
                                    fieldTestingManager?.trackUserAction(
                                        "order_dialog_opened",
                                        "listing_id=${listing.id}, breed=${listing.breed}, price=${listing.price}"
                                    )
                                    selectedListing = listing
                                    showOrderDialog = true
                                },
                                onAddToWatchlist = { listing ->
                                    // TODO: Add to watchlist
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Order placement dialog
    if (showOrderDialog && selectedListing != null) {
        OrderPlacementDialog(
            listing = selectedListing!!,
            onDismiss = {
                showOrderDialog = false
                selectedListing = null
            },
            onOrderPlaced = { order ->
                // Track successful order placement
                fieldTestingManager?.trackUserAction(
                    "order_placed_successfully",
                    "listing_id=${order.listingId}, payment=${order.paymentMethod.name}, delivery=${order.deliveryMethod.name}"
                )

                // Handle order placement
                showOrderDialog = false
                selectedListing = null
                showSuccessMessage = true

                // Auto-hide success message after 3 seconds
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                    kotlinx.coroutines.delay(3000)
                    showSuccessMessage = false
                }
            }
        )
    }

    // Success message
    if (showSuccessMessage) {
        LaunchedEffect(showSuccessMessage) {
            // Show success message
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
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
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Order placed successfully! The seller will contact you soon.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Create demo marketplace listings for first-time users
 */
private fun createDemoMarketplaceListings(): List<FowlEntity> {
    return listOf(
        FowlEntity(
            id = "marketplace-demo-${System.currentTimeMillis()}-1",
            ownerId = "demo-seller-1",
            name = "Premium Rhode Island Red",
            breed = "Rhode Island Red",
            gender = "FEMALE",
            birthDate = Date(),
            color = "Deep Red",
            weight = 2.8,
            status = "ACTIVE",
            description = "Excellent laying hen with consistent egg production. Healthy and vaccinated. Perfect for backyard farming.",
            imageUrls = null,
            price = 850.0,
            isForSale = true,
            region = "north",
            district = "demo-district",
            createdAt = Date(),
            updatedAt = Date()
        ),
        FowlEntity(
            id = "marketplace-demo-${System.currentTimeMillis()}-2",
            ownerId = "demo-seller-2",
            name = "Champion Leghorn Rooster",
            breed = "White Leghorn",
            gender = "MALE",
            birthDate = Date(),
            color = "Pure White",
            weight = 3.5,
            status = "ACTIVE",
            description = "Award-winning breeding rooster with excellent genetics. Strong and healthy bloodline.",
            imageUrls = null,
            price = 1200.0,
            isForSale = true,
            region = "south",
            district = "demo-district-2",
            createdAt = Date(),
            updatedAt = Date()
        ),
        FowlEntity(
            id = "marketplace-demo-${System.currentTimeMillis()}-3",
            ownerId = "demo-seller-3",
            name = "Golden Buff Orpington",
            breed = "Buff Orpington",
            gender = "FEMALE",
            birthDate = Date(),
            color = "Golden Buff",
            weight = 3.0,
            status = "ACTIVE",
            description = "Beautiful dual-purpose hen. Great for both eggs and meat. Very docile and friendly.",
            imageUrls = null,
            price = 950.0,
            isForSale = true,
            region = "east",
            district = "demo-district-3",
            createdAt = Date(),
            updatedAt = Date()
        ),
        FowlEntity(
            id = "marketplace-demo-${System.currentTimeMillis()}-4",
            ownerId = "demo-seller-4",
            name = "Rare Silkie Bantam",
            breed = "Silkie",
            gender = "FEMALE",
            birthDate = Date(),
            color = "Black",
            weight = 1.2,
            status = "ACTIVE",
            description = "Rare ornamental breed with unique fluffy feathers. Perfect for exhibition or as pets.",
            imageUrls = null,
            price = 2500.0,
            isForSale = true,
            region = "west",
            district = "demo-district-4",
            createdAt = Date(),
            updatedAt = Date()
        ),
        FowlEntity(
            id = "marketplace-demo-${System.currentTimeMillis()}-5",
            ownerId = "demo-seller-5",
            name = "Productive Sussex Hen",
            breed = "Sussex",
            gender = "FEMALE",
            birthDate = Date(),
            color = "Light Sussex",
            weight = 2.7,
            status = "ACTIVE",
            description = "High-producing laying hen with excellent feed conversion. Calm temperament and hardy.",
            imageUrls = null,
            price = 750.0,
            isForSale = true,
            region = "central",
            district = "demo-district-5",
            createdAt = Date(),
            updatedAt = Date()
        )
    )
}

/**
 * Enhanced Marketplace Stats Section
 */
@Composable
private fun MarketplaceStatsSection(listings: List<FowlEntity>) {
    val totalListings = listings.size
    val averagePrice = if (listings.isNotEmpty()) {
        listings.mapNotNull { it.price }.average()
    } else 0.0
    val uniqueBreeds = listings.map { it.breed }.distinct().size
    val totalValue = listings.mapNotNull { it.price }.sum()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Marketplace Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MarketplaceStatCard("Listings", totalListings.toString(), Icons.Default.Store)
                MarketplaceStatCard("Breeds", uniqueBreeds.toString(), Icons.Default.Category)
                MarketplaceStatCard("Avg Price", "₹${String.format("%.0f", averagePrice)}", Icons.Default.AttachMoney)
            }

            if (totalValue > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Total Market Value: ₹${String.format("%.2f", totalValue)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun MarketplaceStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Filter Section
 */
@Composable
private fun FilterSection(
    selectedFilter: String,
    onFilterSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Filter by Price Range",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("All", "Under ₹1000", "₹1000-5000", "Above ₹5000")
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { onFilterSelected(filter) },
                        label = { Text(filter) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Empty Marketplace State
 */
@Composable
private fun EmptyMarketplaceState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Store,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Listings Available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Be the first to list your fowl in the marketplace! Create listings to reach potential buyers in your area.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* TODO: Navigate to create listing */ },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Your First Listing")
            }
        }
    }
}

/**
 * Enhanced Marketplace Listing Card with Order Functionality
 */
@Composable
private fun EnhancedMarketplaceListingCard(
    listing: FowlEntity,
    onContact: (FowlEntity) -> Unit,
    onViewDetails: (FowlEntity) -> Unit,
    onPlaceOrder: (FowlEntity) -> Unit,
    onAddToWatchlist: (FowlEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = listing.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${listing.breed} • ${listing.gender}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Price Badge
                Surface(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "₹${String.format("%.0f", listing.price ?: 0.0)}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Details Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    listing.weight?.let { weight ->
                        MarketplaceDetailItem("Weight", "${weight} kg")
                    }
                    listing.color?.let { color ->
                        MarketplaceDetailItem("Color", color)
                    }
                }

                Column {
                    MarketplaceDetailItem("Location", "${listing.region}, ${listing.district}")
                    MarketplaceDetailItem("Status", listing.status)
                }
            }

            // Description
            listing.description?.let { description ->
                if (description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verification badge
            if (listing.ownerId.startsWith("verified")) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified Seller",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Verified Farmer",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onContact(listing) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Contact", fontSize = 11.sp)
                }

                Button(
                    onClick = { onPlaceOrder(listing) },
                    modifier = Modifier.weight(1.2f)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Order", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = { onViewDetails(listing) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Details", fontSize = 11.sp)
                }

                IconButton(
                    onClick = { onAddToWatchlist(listing) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = "Add to Watchlist", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MarketplaceDetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Order Placement Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderPlacementDialog(
    listing: FowlEntity,
    onDismiss: () -> Unit,
    onOrderPlaced: (CreateOrderRequest) -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH_ON_DELIVERY) }
    var selectedDeliveryMethod by remember { mutableStateOf(DeliveryMethod.PICKUP_FROM_FARM) }
    var orderNotes by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var deliveryAddress by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var addressError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Validation functions
    fun validatePhone(phone: String): String? {
        return when {
            phone.isBlank() -> "Phone number is required"
            phone.length != 10 -> "Phone number must be 10 digits"
            !phone.all { it.isDigit() } -> "Phone number must contain only digits"
            else -> null
        }
    }

    fun validateAddress(address: String): String? {
        return when {
            selectedDeliveryMethod == DeliveryMethod.DELIVERY_TO_BUYER && address.isBlank() ->
                "Delivery address is required"
            selectedDeliveryMethod == DeliveryMethod.DELIVERY_TO_BUYER && address.length < 10 ->
                "Please provide a complete address"
            else -> null
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Place Order",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Fowl details summary
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                text = listing.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${listing.breed} • ${listing.gender}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "₹${listing.price?.toInt() ?: 0}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Contact information
                item {
                    Text(
                        text = "Contact Information",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        value = buyerPhone,
                        onValueChange = {
                            buyerPhone = it
                            phoneError = validatePhone(it)
                        },
                        label = { Text("Your Phone Number *") },
                        placeholder = { Text("Enter 10-digit phone number") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = phoneError != null,
                        supportingText = phoneError?.let { { Text(it) } },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                    )
                }

                // Payment method selection
                item {
                    Text(
                        text = "Payment Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    PaymentMethod.values().forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPaymentMethod = method }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedPaymentMethod == method,
                                onClick = { selectedPaymentMethod = method }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (method) {
                                    PaymentMethod.CASH_ON_DELIVERY -> "Cash on Delivery"
                                    PaymentMethod.ADVANCE_PAYMENT -> "Full Advance Payment"
                                    PaymentMethod.PARTIAL_ADVANCE -> "Partial Advance (50%)"
                                    PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
                                    PaymentMethod.UPI_PAYMENT -> "UPI Payment"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Delivery method selection
                item {
                    Text(
                        text = "Delivery Method",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    DeliveryMethod.values().forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDeliveryMethod = method }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedDeliveryMethod == method,
                                onClick = { selectedDeliveryMethod = method }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (method) {
                                    DeliveryMethod.PICKUP_FROM_FARM -> "Pickup from Farm"
                                    DeliveryMethod.DELIVERY_TO_BUYER -> "Delivery to Your Location"
                                    DeliveryMethod.MEET_HALFWAY -> "Meet Halfway"
                                    DeliveryMethod.TRANSPORT_SERVICE -> "Transport Service"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Delivery address (if delivery selected)
                if (selectedDeliveryMethod == DeliveryMethod.DELIVERY_TO_BUYER) {
                    item {
                        OutlinedTextField(
                            value = deliveryAddress,
                            onValueChange = { deliveryAddress = it },
                            label = { Text("Delivery Address") },
                            placeholder = { Text("Enter your complete address") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                }

                // Order notes
                item {
                    OutlinedTextField(
                        value = orderNotes,
                        onValueChange = { orderNotes = it },
                        label = { Text("Additional Notes (Optional)") },
                        placeholder = { Text("Any special requirements or questions") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    // Validate all fields
                    phoneError = validatePhone(buyerPhone)
                    addressError = validateAddress(deliveryAddress)

                    if (phoneError == null && addressError == null) {
                        isSubmitting = true

                        // Simulate API call
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            kotlinx.coroutines.delay(1500) // Simulate network delay

                            val orderRequest = CreateOrderRequest(
                                listingId = listing.id,
                                fowlId = listing.id,
                                sellerId = listing.ownerId,
                                orderType = OrderType.DIRECT_PURCHASE,
                                paymentMethod = selectedPaymentMethod,
                                deliveryMethod = selectedDeliveryMethod,
                                deliveryInfo = if (selectedDeliveryMethod == DeliveryMethod.DELIVERY_TO_BUYER) {
                                    DeliveryInfo(address = deliveryAddress)
                                } else null,
                                notes = orderNotes.takeIf { it.isNotBlank() }
                            )

                            isSubmitting = false
                            onOrderPlaced(orderRequest)
                        }
                    }
                },
                enabled = !isSubmitting && buyerPhone.isNotBlank() &&
                         (selectedDeliveryMethod != DeliveryMethod.DELIVERY_TO_BUYER || deliveryAddress.isNotBlank())
            ) {
                if (isSubmitting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Placing Order...")
                    }
                } else {
                    Text("Place Order")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
