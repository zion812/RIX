package com.rio.rostry.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Bottom navigation item model
 */
data class BottomNavigationItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

/**
 * Navigation items for General Users
 * Navigation: Market | Explore | Create | Cart | Profile
 */
object GeneralUserNavigation {
    val items = listOf(
        BottomNavigationItem(
            route = ROSTRYDestinations.MARKETPLACE,
            label = "Market",
            icon = Icons.Default.Store,
            selectedIcon = Icons.Filled.Store
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.EXPLORE,
            label = "Explore",
            icon = Icons.Default.Search,
            selectedIcon = Icons.Filled.Search
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.CREATE,
            label = "Create",
            icon = Icons.Default.Add,
            selectedIcon = Icons.Filled.Add
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.CART,
            label = "Cart",
            icon = Icons.Default.ShoppingCart,
            selectedIcon = Icons.Filled.ShoppingCart
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.PROFILE,
            label = "Profile",
            icon = Icons.Default.Person,
            selectedIcon = Icons.Filled.Person
        )
    )
}

/**
 * Navigation items for Farmer Users
 * Navigation: Home | Market | Create/List | Community | Profile
 */
object FarmerNavigation {
    val items = listOf(
        BottomNavigationItem(
            route = ROSTRYDestinations.FARMER_DASHBOARD,
            label = "Home",
            icon = Icons.Default.Home,
            selectedIcon = Icons.Filled.Home
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.MARKETPLACE,
            label = "Market",
            icon = Icons.Default.Store,
            selectedIcon = Icons.Filled.Store
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.CREATE,
            label = "Create/List",
            icon = Icons.Default.Add,
            selectedIcon = Icons.Filled.Add
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.COMMUNITY,
            label = "Community",
            icon = Icons.Default.Group,
            selectedIcon = Icons.Filled.Group
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.PROFILE,
            label = "Profile",
            icon = Icons.Default.Person,
            selectedIcon = Icons.Filled.Person
        )
    )
}

/**
 * Navigation items for Enthusiast Users
 * Navigation: Home | Explore | Create/List | Dashboard | Transfers
 */
object EnthusiastNavigation {
    val items = listOf(
        BottomNavigationItem(
            route = ROSTRYDestinations.ENTHUSIAST_DASHBOARD,
            label = "Home",
            icon = Icons.Default.Home,
            selectedIcon = Icons.Filled.Home
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.EXPLORE,
            label = "Explore",
            icon = Icons.Default.Search,
            selectedIcon = Icons.Filled.Search
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.CREATE,
            label = "Create/List",
            icon = Icons.Default.Add,
            selectedIcon = Icons.Filled.Add
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.ANALYTICS_DASHBOARD,
            label = "Dashboard",
            icon = Icons.Default.Dashboard,
            selectedIcon = Icons.Filled.Dashboard
        ),
        BottomNavigationItem(
            route = ROSTRYDestinations.TRANSFERS,
            label = "Transfers",
            icon = Icons.Default.SwapHoriz,
            selectedIcon = Icons.Filled.SwapHoriz
        )
    )
}
