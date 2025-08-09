package com.rio.rostry.ui.payment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rio.rostry.core.payment.DemoPaymentMethod
import com.rio.rostry.ui.theme.RIOTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Demo Payment Test Activity - Testing Interface for Payment Gateway
 * Provides comprehensive testing tools for payment scenarios
 */
@AndroidEntryPoint
class DemoPaymentTestActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            RIOTheme {
                DemoPaymentTestScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoPaymentTestScreen(
    viewModel: DemoPaymentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var selectedAmount by remember { mutableStateOf(50.0) }
    var selectedPackage by remember { mutableStateOf("basic_10") }
    
    val testAmounts = listOf(
        50.0 to "₹50 - Basic Package",
        125.0 to "₹125 - Standard Package", 
        250.0 to "₹250 - Premium Package",
        500.0 to "₹500 - Farmer Basic",
        1000.0 to "₹1000 - Farmer Elite",
        2500.0 to "₹2500 - Enthusiast Pro",
        5000.0 to "₹5000 - Maximum Demo Amount"
    )
    
    val testScenarios = listOf(
        PaymentScenario.SUCCESS to "✅ Successful Payment",
        PaymentScenario.FAILURE to "❌ Payment Failure",
        PaymentScenario.TIMEOUT to "⏱️ Payment Timeout",
        PaymentScenario.INSUFFICIENT_BALANCE to "💰 Insufficient Balance",
        PaymentScenario.NETWORK_ERROR to "🌐 Network Error"
    )
    
    val paymentMethods = listOf(
        DemoPaymentMethod.UPI to "UPI Payment",
        DemoPaymentMethod.GOOGLE_PAY to "Google Pay",
        DemoPaymentMethod.CARD to "Card Payment",
        DemoPaymentMethod.NET_BANKING to "Net Banking",
        DemoPaymentMethod.WALLET to "Digital Wallet"
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🧪 Demo Payment Gateway Testing",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Test all payment scenarios and methods",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Quick Test Amounts
        item {
            Text(
                text = "Quick Test Amounts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(testAmounts.chunked(2)) { amountPair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                amountPair.forEach { (amount, description) ->
                    OutlinedButton(
                        onClick = {
                            selectedAmount = amount
                            val intent = Intent(context, DemoPaymentActivity::class.java).apply {
                                putExtra("amount", amount)
                                putExtra("packageId", "test_package_${amount.toInt()}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "₹${amount.toInt()}",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = description.split(" - ")[1],
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // Fill remaining space if odd number of items
                if (amountPair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        
        // Payment Method Tests
        item {
            Text(
                text = "Test Payment Methods",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(paymentMethods) { (method, description) ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = getMethodTestDescription(method),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = {
                            val intent = Intent(context, DemoPaymentActivity::class.java).apply {
                                putExtra("amount", 100.0)
                                putExtra("packageId", "test_${method.name.lowercase()}")
                                putExtra("preferredMethod", method.name)
                            }
                            context.startActivity(intent)
                        }
                    ) {
                        Text("Test")
                    }
                }
            }
        }
        
        // Scenario Testing
        item {
            Text(
                text = "Test Payment Scenarios",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(testScenarios) { (scenario, description) ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = getScenarioDescription(scenario),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Button(
                        onClick = {
                            viewModel.simulatePaymentScenario(scenario)
                        }
                    ) {
                        Text("Simulate")
                    }
                }
            }
        }
        
        // Payment Statistics
        item {
            val stats = viewModel.getDemoPaymentStats()
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "📊 Demo Payment Statistics",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Total Transactions", stats.totalTransactions.toString())
                        StatItem("Success Rate", "${stats.successRate}%")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatItem("Avg Processing", "${stats.averageProcessingTime}s")
                        StatItem("Most Used", stats.mostUsedMethod)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    StatItem("Total Amount", "₹${String.format("%.2f", stats.totalAmount)}")
                }
            }
        }
        
        // Test Tools
        item {
            Text(
                text = "Testing Tools",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Open payment logs
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("View Logs")
                }
                
                OutlinedButton(
                    onClick = {
                        // Clear test data
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear Data")
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        // Export test results
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Export Results")
                }
                
                OutlinedButton(
                    onClick = {
                        // Generate test report
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test Report")
                }
            }
        }
        
        // Demo Credentials
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔑 Demo Test Credentials",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text("UPI ID: test@demo", style = MaterialTheme.typography.bodySmall)
                    Text("UPI PIN: 1234", style = MaterialTheme.typography.bodySmall)
                    Text("Card: 4111 1111 1111 1111", style = MaterialTheme.typography.bodySmall)
                    Text("Expiry: 12/25, CVV: 123", style = MaterialTheme.typography.bodySmall)
                    Text("Name: Demo User", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getMethodTestDescription(method: DemoPaymentMethod): String {
    return when (method) {
        DemoPaymentMethod.UPI -> "Test UPI flow with demo credentials"
        DemoPaymentMethod.GOOGLE_PAY -> "Simulate Google Pay authentication"
        DemoPaymentMethod.CARD -> "Test card validation and processing"
        DemoPaymentMethod.NET_BANKING -> "Simulate bank redirect flow"
        DemoPaymentMethod.WALLET -> "Test wallet balance and payment"
    }
}

private fun getScenarioDescription(scenario: PaymentScenario): String {
    return when (scenario) {
        PaymentScenario.SUCCESS -> "Test successful payment flow"
        PaymentScenario.FAILURE -> "Test payment failure handling"
        PaymentScenario.TIMEOUT -> "Test timeout scenarios"
        PaymentScenario.INSUFFICIENT_BALANCE -> "Test insufficient balance error"
        PaymentScenario.NETWORK_ERROR -> "Test network connectivity issues"
    }
}
