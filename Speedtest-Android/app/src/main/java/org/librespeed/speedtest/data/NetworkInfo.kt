package org.librespeed.speedtest.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build

object NetworkInfo {

    /** Best-effort description like "Wi-Fi 6", "Wi-Fi", "Ethernet" or "Cellular"; null when unknown. */
    fun describe(context: Context): String? {
        return try {
            val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork) ?: return null
            when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> wifiName(context, capabilities)
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun wifiName(context: Context, capabilities: NetworkCapabilities): String {
        val standard = try {
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                capabilities.transportInfo as? WifiInfo
            } else {
                null
            } ?: @Suppress("DEPRECATION") run {
                (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.connectionInfo
            }
            if (info != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) info.wifiStandard else 0
        } catch (_: Exception) {
            0
        }
        return when (standard) {
            8 -> "Wi-Fi 7"  //ScanResult.WIFI_STANDARD_11BE
            6 -> "Wi-Fi 6"  //WIFI_STANDARD_11AX
            5 -> "Wi-Fi 5"  //WIFI_STANDARD_11AC
            4 -> "Wi-Fi 4"  //WIFI_STANDARD_11N
            else -> "Wi-Fi"
        }
    }

}
