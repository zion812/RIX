package com.rio.rostry.e2e

import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry

object NetworkSimulationUtil {
    enum class NetworkType { TWO_G, THREE_G, FOUR_G, WIFI }

    fun setNetworkType(type: NetworkType) {
        // This is a placeholder. In real device farms or emulators, use adb commands or test lab APIs.
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        when (type) {
            NetworkType.TWO_G -> simulateNetwork("gsm")
            NetworkType.THREE_G -> simulateNetwork("umts")
            NetworkType.FOUR_G -> simulateNetwork("lte")
            NetworkType.WIFI -> simulateNetwork("wifi")
        }
    }

    fun resetNetworkType() {
        simulateNetwork("wifi")
    }

    private fun simulateNetwork(type: String) {
        // Example: Use adb shell commands or Firebase Test Lab APIs
        // Runtime.getRuntime().exec("adb shell svc data disable")
        // For CI, use test lab network shaping
        // This is a stub for demonstration
    }
}
