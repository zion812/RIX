package com.rio.rostry.ui.dashboard

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rostry.auth.UserClaims
import com.rio.rostry.auth.UserTier
import com.rio.rostry.ui.theme.ROSTRYTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardScreensTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testUserClaims = UserClaims(
        tier = UserTier.GENERAL
    )

    @Test
    fun generalUserDashboard_displaysCorrectContent() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                GeneralUserDashboard(
                    claims = testUserClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("RIO - General User").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome to RIO!").assertIsDisplayed()
        composeTestRule.onNodeWithText("You're currently a General user. Explore the fowl community and consider upgrading for more features.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upgrade to Farmer").assertIsDisplayed()
        composeTestRule.onNodeWithText("View Fowls").assertIsDisplayed()
        composeTestRule.onNodeWithText("Marketplace").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertIsDisplayed()
    }

    @Test
    fun farmerDashboard_displaysCorrectContent() {
        // Given
        val farmerClaims = testUserClaims.copy(
            tier = UserTier.FARMER
        )

        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                FarmerDashboard(
                    claims = farmerClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("RIO - Farmer Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome, Farmer!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manage your fowls, create marketplace listings, and track your breeding success.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Your Farm Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fowls").assertIsDisplayed()
        composeTestRule.onNodeWithText("Listings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transfers").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertIsDisplayed()
    }

    @Test
    fun enthusiastDashboard_displaysCorrectContent() {
        // Given
        val enthusiastClaims = testUserClaims.copy(
            tier = UserTier.ENTHUSIAST
        )

        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                EnthusiastDashboard(
                    claims = enthusiastClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("RIO - Enthusiast Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Premium Enthusiast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Access all premium features including advanced breeding analytics, priority marketplace listings, and exclusive community features.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced Analytics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Premium Features").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertIsDisplayed()
    }

    @Test
    fun generalUserDashboard_hasUpgradePrompt() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                GeneralUserDashboard(
                    claims = testUserClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Upgrade to Farmer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get full access to fowl management, marketplace, and more for ₹500/year").assertIsDisplayed()
    }

    @Test
    fun farmerDashboard_hasEnthusiastUpgrade() {
        // Given
        val farmerClaims = testUserClaims.copy(tier = UserTier.FARMER)

        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                FarmerDashboard(
                    claims = farmerClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Upgrade to Enthusiast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Get advanced breeding analytics, premium marketplace features, and priority support for ₹2000/year").assertIsDisplayed()
    }

    @Test
    fun enthusiastDashboard_showsPremiumBenefits() {
        // Given
        val enthusiastClaims = testUserClaims.copy(tier = UserTier.ENTHUSIAST)

        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                EnthusiastDashboard(
                    claims = enthusiastClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Premium Enthusiast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Advanced Analytics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Premium Features").assertIsDisplayed()
    }

    @Test
    fun dashboards_haveNavigationActions() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                GeneralUserDashboard(
                    claims = testUserClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Out").assertIsDisplayed()
    }

    @Test
    fun dashboards_displayBasicStats() {
        // Given
        val farmerClaims = testUserClaims.copy(tier = UserTier.FARMER)

        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                FarmerDashboard(
                    claims = farmerClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Your Farm Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fowls").assertIsDisplayed()
        composeTestRule.onNodeWithText("Listings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Transfers").assertIsDisplayed()
    }

    @Test
    fun enthusiastDashboard_displaysAdvancedStats() {
        // Given
        val enthusiastClaims = testUserClaims.copy(tier = UserTier.ENTHUSIAST)

        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                EnthusiastDashboard(
                    claims = enthusiastClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("Advanced Analytics").assertIsDisplayed()
        composeTestRule.onNodeWithText("Premium Features").assertIsDisplayed()
    }

    @Test
    fun featureButtons_areClickable() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                val navController = rememberNavController()
                GeneralUserDashboard(
                    claims = testUserClaims,
                    navController = navController,
                    onSignOut = { }
                )
            }
        }

        // When & Then
        composeTestRule.onNodeWithText("Marketplace").assertHasClickAction()
        composeTestRule.onNodeWithText("View Fowls").assertHasClickAction()
    }
}
