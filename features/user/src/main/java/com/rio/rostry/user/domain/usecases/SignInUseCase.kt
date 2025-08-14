package com.rio.rostry.user.domain.usecases

import com.rio.rostry.core.common.model.Result
import com.rio.rostry.user.domain.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for signing in users
 */
class SignInUseCase @Inject constructor(
    // TODO: Inject AuthRepository when available
) {
    
    suspend operator fun invoke(email: String, password: String): Flow&lt;Result&lt;User&gt;&gt; = flow {
        emit(Result.Loading)
        
        try {
            // Simulate network delay
            delay(1500)
            
            // Demo authentication logic
            if (isValidCredentials(email, password)) {
                val user = createDemoUser(email)
                emit(Result.Success(user))
            } else {
                emit(Result.Error(Exception(&quot;Invalid email or password&quot;)))
            }
        } catch (e: Exception) {
            emit(Result.Error(e))
        }
    }
    
    private fun isValidCredentials(email: String, password: String): Boolean {
        return when {
            email == &quot;farmer@rio.com&quot; &amp;&amp; password == &quot;demo123&quot; -&gt; true
            email == &quot;enthusiast@rio.com&quot; &amp;&amp; password == &quot;demo123&quot; -&gt; true
            email == &quot;user@rio.com&quot; &amp;&amp; password == &quot;demo123&quot; -&gt; true
            else -&gt; false
        }
    }
    
    private fun createDemoUser(email: String): User {
        return when (email) {
            &quot;farmer@rio.com&quot; -&gt; User(
                id = &quot;farmer_001&quot;,
                email = email,
                displayName = &quot;John Farmer&quot;,
                phoneNumber = &quot;+1234567890&quot;,
                tier = com.rio.rostry.core.common.model.UserTier.FARMER,
                isEmailVerified = true,
                isPhoneVerified = true
            )
            &quot;enthusiast@rio.com&quot; -&gt; User(
                id = &quot;enthusiast_001&quot;,
                email = email,
                displayName = &quot;Jane Enthusiast&quot;,
                phoneNumber = &quot;+1234567891&quot;,
                tier = com.rio.rostry.core.common.model.UserTier.ENTHUSIAST,
                isEmailVerified = true,
                isPhoneVerified = false
            )
            else -&gt; User(
                id = &quot;user_001&quot;,
                email = email,
                displayName = &quot;Demo User&quot;,
                phoneNumber = &quot;+1234567892&quot;,
                tier = com.rio.rostry.core.common.model.UserTier.GENERAL,
                isEmailVerified = false,
                isPhoneVerified = false
            )
        }
    }
}