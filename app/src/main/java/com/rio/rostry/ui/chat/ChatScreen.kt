package com.rio.rostry.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/**
 * Enhanced Community Chat Screen with Social Features
 * Implements P2P messaging, group discussions, and community engagement
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Chats", "Groups", "Community")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search conversations and groups")
                    }
                    IconButton(onClick = { /* TODO: New chat/group */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Start new conversation or create group")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> DirectChatsContent(navController)
                1 -> GroupChatsContent(navController)
                2 -> CommunityFeedContent(navController)
            }
        }
    }
}

/**
 * Direct chats content - P2P messaging
 */
@Composable
private fun DirectChatsContent(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Recent conversations
        items(getDemoConversations()) { conversation ->
            ConversationCard(
                conversation = conversation,
                onClick = {
                    // TODO: Navigate to conversation detail
                }
            )
        }

        if (getDemoConversations().isEmpty()) {
            item {
                EmptyStateCard(
                    icon = Icons.Default.Email,
                    title = "No conversations yet",
                    description = "Start chatting with farmers and enthusiasts about roosters, breeding, and marketplace listings.",
                    actionText = "Start a Chat",
                    onAction = { /* TODO: Start new conversation */ }
                )
            }
        }
    }
}

/**
 * Group chats content - Community groups
 */
@Composable
private fun GroupChatsContent(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Popular Groups",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(getDemoGroups()) { group ->
            GroupCard(
                group = group,
                onClick = {
                    // TODO: Navigate to group detail
                }
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Create New Group",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start a community group for your region, breed, or interest",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Community feed content - Posts, discussions, and social features
 */
@Composable
private fun CommunityFeedContent(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            CreatePostCard(
                onCreatePost = { /* TODO: Create post */ }
            )
        }

        items(getDemoPosts()) { post ->
            CommunityPostCard(
                post = post,
                onLike = { /* TODO: Like post */ },
                onComment = { /* TODO: Comment on post */ },
                onShare = { /* TODO: Share post */ }
            )
        }
    }
}

/**
 * Conversation card for direct chats
 */
@Composable
private fun ConversationCard(
    conversation: DemoConversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = conversation.name.first().toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = conversation.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = conversation.lastMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = conversation.time,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (conversation.unreadCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Badge {
                        Text(conversation.unreadCount.toString())
                    }
                }
            }
        }
    }
}

/**
 * Group card for community groups
 */
@Composable
private fun GroupCard(
    group: DemoGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.People,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${group.memberCount} members",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (group.isJoined) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Joined") },
                        leadingIcon = { Icon(Icons.Default.Done, contentDescription = null) }
                    )
                } else {
                    Button(
                        onClick = { /* TODO: Join group */ }
                    ) {
                        Text("Join")
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Create post card
 */
@Composable
private fun CreatePostCard(
    onCreatePost: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        MaterialTheme.colorScheme.primary,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = { Text("Share your farming experience...") },
                modifier = Modifier.weight(1f),
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = onCreatePost) {
                        Icon(Icons.Default.Send, contentDescription = "Post")
                    }
                }
            )
        }
    }
}

/**
 * Community post card
 */
@Composable
private fun CommunityPostCard(
    post: DemoPost,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Post header
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.authorName.first().toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.authorName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = post.timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { /* TODO: More options */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post content
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium
            )

            if (post.imageUrl != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Photo,
                            contentDescription = "Post image",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row {
                    TextButton(
                        onClick = onLike
                    ) {
                        Icon(
                            if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.likeCount}")
                    }

                    TextButton(
                        onClick = onComment
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Comment")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${post.commentCount}")
                    }
                }

                TextButton(
                    onClick = onShare
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        }
    }
}

/**
 * Empty state card component
 */
@Composable
private fun EmptyStateCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAction) {
                Text(actionText)
            }
        }
    }
}

// Demo data models
data class DemoConversation(
    val id: String,
    val name: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0
)

data class DemoGroup(
    val id: String,
    val name: String,
    val description: String,
    val memberCount: Int,
    val isJoined: Boolean = false
)

data class DemoPost(
    val id: String,
    val authorName: String,
    val content: String,
    val timestamp: String,
    val likeCount: Int,
    val commentCount: Int,
    val isLiked: Boolean = false,
    val imageUrl: String? = null
)

// Demo data functions
private fun getDemoConversations(): List<DemoConversation> {
    return listOf(
        DemoConversation(
            id = "1",
            name = "Rajesh Kumar",
            lastMessage = "I have some excellent Aseel roosters available",
            time = "2 min ago",
            unreadCount = 2
        ),
        DemoConversation(
            id = "2",
            name = "Priya Sharma",
            lastMessage = "Thank you for the breeding advice!",
            time = "1 hour ago"
        ),
        DemoConversation(
            id = "3",
            name = "Dr. Veterinarian",
            lastMessage = "The vaccination schedule looks good",
            time = "Yesterday"
        )
    )
}

private fun getDemoGroups(): List<DemoGroup> {
    return listOf(
        DemoGroup(
            id = "1",
            name = "Aseel Breeders India",
            description = "Community for Aseel rooster breeders and enthusiasts",
            memberCount = 1247,
            isJoined = true
        ),
        DemoGroup(
            id = "2",
            name = "Karnataka Poultry Farmers",
            description = "Local farmers group for Karnataka region",
            memberCount = 856
        ),
        DemoGroup(
            id = "3",
            name = "Rooster Health & Care",
            description = "Expert advice on rooster health, nutrition, and care",
            memberCount = 2341,
            isJoined = true
        ),
        DemoGroup(
            id = "4",
            name = "Marketplace Tips",
            description = "Tips and tricks for successful selling",
            memberCount = 567
        )
    )
}

private fun getDemoPosts(): List<DemoPost> {
    return listOf(
        DemoPost(
            id = "1",
            authorName = "Farmer Ravi",
            content = "Just got my first Kadaknath rooster! Any tips for first-time Kadaknath breeders? Looking forward to learning from this amazing community. 🐓",
            timestamp = "2 hours ago",
            likeCount = 24,
            commentCount = 8,
            isLiked = true,
            imageUrl = "demo_image_1"
        ),
        DemoPost(
            id = "2",
            authorName = "Dr. Poultry Expert",
            content = "Important reminder: Vaccination schedule is crucial for healthy roosters. Make sure to follow the recommended timeline for Newcastle disease and other common poultry diseases.",
            timestamp = "5 hours ago",
            likeCount = 67,
            commentCount = 15
        ),
        DemoPost(
            id = "3",
            authorName = "Breeding Enthusiast",
            content = "Successful hatch! 8 out of 10 eggs hatched successfully. The key was maintaining consistent temperature and humidity. Happy to share my incubation setup details.",
            timestamp = "1 day ago",
            likeCount = 45,
            commentCount = 22,
            imageUrl = "demo_image_2"
        ),
        DemoPost(
            id = "4",
            authorName = "Market Seller",
            content = "Market update: High demand for Aseel roosters this week. Prices are up 15% compared to last month. Great time for sellers!",
            timestamp = "2 days ago",
            likeCount = 33,
            commentCount = 12
        )
    )
}
