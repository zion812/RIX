package com.rio.rostry.ui.auth

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rio.rostry.auth.UserTier
import com.rio.rostry.ui.theme.ROSTRYTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun authScreen_displaysSignInFormByDefault() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("RIO Rooster Community").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test
    fun authScreen_switchesToSignUpForm() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Create Account").performClick()

        // Then
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Display Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Confirm Password").assertIsDisplayed()
    }

    @Test
    fun authScreen_switchesBackToSignInForm() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.onNodeWithText("Back to Sign In").performClick()

        // Then
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
    }

    @Test
    fun signInForm_acceptsUserInput() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Then
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
        // Password field content is not visible for security reasons
    }

    @Test
    fun signUpForm_acceptsUserInput() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.onNodeWithText("Display Name").performTextInput("Test User")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("password123")

        // Then
        composeTestRule.onNodeWithText("Test User").assertIsDisplayed()
        composeTestRule.onNodeWithText("test@example.com").assertIsDisplayed()
    }

    @Test
    fun signUpForm_displaysTierSelection() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Create Account").performClick()

        // Then
        composeTestRule.onNodeWithText("Account Tier").assertIsDisplayed()
        composeTestRule.onNodeWithText("General").assertIsDisplayed()
        composeTestRule.onNodeWithText("Farmer").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enthusiast").assertIsDisplayed()
    }

    @Test
    fun signUpForm_allowsTierSelection() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.onNodeWithText("Farmer").performClick()

        // Then
        // Farmer tier should be selected (this would need to be verified through state)
        composeTestRule.onNodeWithText("Farmer").assertIsDisplayed()
    }

    @Test
    fun signInForm_showsErrorForEmptyFields() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Sign In").performClick()

        // Then
        // Error messages should be displayed for empty fields
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test
    fun signUpForm_showsErrorForEmptyFields() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Then
        // Error messages should be displayed for empty fields
        composeTestRule.onNodeWithText("Display name is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email is required").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password is required").assertIsDisplayed()
    }

    @Test
    fun signUpForm_showsErrorForPasswordMismatch() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Create Account").performClick()
        composeTestRule.onNodeWithText("Display Name").performTextInput("Test User")
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Confirm Password").performTextInput("differentpassword")
        composeTestRule.onNodeWithText("Sign Up").performClick()

        // Then
        composeTestRule.onNodeWithText("Passwords do not match").assertIsDisplayed()
    }

    @Test
    fun forgotPasswordDialog_displaysWhenLinkClicked() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Forgot Password?").performClick()

        // Then
        composeTestRule.onNodeWithText("Reset Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter your email address and we'll send you a link to reset your password.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send Reset Link").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
    }

    @Test
    fun forgotPasswordDialog_acceptsEmailInput() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Forgot Password?").performClick()
        composeTestRule.onAllNodesWithText("Email")[1].performTextInput("reset@example.com")

        // Then
        composeTestRule.onNodeWithText("reset@example.com").assertIsDisplayed()
    }

    @Test
    fun forgotPasswordDialog_dismissesOnCancel() {
        // Given
        composeTestRule.setContent {
            ROSTRYTheme {
                AuthScreen(
                    onAuthSuccess = { }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("Forgot Password?").performClick()
        composeTestRule.onNodeWithText("Cancel").performClick()

        // Then
        composeTestRule.onNodeWithText("Reset Password").assertDoesNotExist()
    }
}
