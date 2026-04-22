package com.v2ray.ng.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.content.ContextCompat

object WifiSsidUtil {

    enum class CurrentWifiSsidStatus {
        AVAILABLE,
        NOT_CONNECTED,
        PERMISSION_REQUIRED,
        LOCATION_DISABLED,
        UNAVAILABLE,
    }

    data class CurrentWifiSsid(
        val status: CurrentWifiSsidStatus,
        val ssid: String? = null,
    )

    fun resolveCurrentWifiSsid(context: Context): CurrentWifiSsid {
        if (!hasLocationPermission(context)) {
            return CurrentWifiSsid(CurrentWifiSsidStatus.PERMISSION_REQUIRED)
        }

        if (!isLocationEnabled(context)) {
            return CurrentWifiSsid(CurrentWifiSsidStatus.LOCATION_DISABLED)
        }

        val connectivity = context.getSystemService(ConnectivityManager::class.java)
            ?: return CurrentWifiSsid(CurrentWifiSsidStatus.UNAVAILABLE)
        val activeNetwork = connectivity.activeNetwork
            ?: return CurrentWifiSsid(CurrentWifiSsidStatus.NOT_CONNECTED)
        val networkCapabilities = connectivity.getNetworkCapabilities(activeNetwork)
            ?: return CurrentWifiSsid(CurrentWifiSsidStatus.NOT_CONNECTED)

        if (!networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return CurrentWifiSsid(CurrentWifiSsidStatus.NOT_CONNECTED)
        }

        val ssid = resolveSsidFromCapabilities(networkCapabilities)
            ?: resolveSsidFromWifiManager(context)

        return if (ssid != null) {
            CurrentWifiSsid(CurrentWifiSsidStatus.AVAILABLE, ssid)
        } else {
            CurrentWifiSsid(CurrentWifiSsidStatus.UNAVAILABLE)
        }
    }

    fun parseSsidList(raw: String?): List<String>? {
        val parsed = raw
            ?.split(',', '\n')
            ?.mapNotNull(::normalizeSsid)
            ?.distinct()
            .orEmpty()
        return parsed.ifEmpty { null }
    }

    fun matches(currentSsid: String?, configuredSsids: List<String>?): Boolean {
        val normalizedCurrentSsid = normalizeSsid(currentSsid) ?: return false
        return configuredSsids
            ?.asSequence()
            ?.mapNotNull(::normalizeSsid)
            ?.any { it == normalizedCurrentSsid }
            ?: false
    }

    fun normalizeSsid(rawSsid: String?): String? {
        val trimmed = rawSsid?.trim().orEmpty()
        if (trimmed.isBlank()) {
            return null
        }

        val unquoted = trimmed.removePrefix("\"").removeSuffix("\"").trim()
        if (unquoted.isBlank()) {
            return null
        }

        if (unquoted.equals(WifiManager.UNKNOWN_SSID, ignoreCase = true)
            || unquoted.equals("<unknown ssid>", ignoreCase = true)
        ) {
            return null
        }

        return unquoted
    }

    private fun hasLocationPermission(context: Context): Boolean {
        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return coarseGranted || fineGranted
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(LocationManager::class.java) ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            locationManager.isLocationEnabled
        } else {
            @Suppress("DEPRECATION")
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        }
    }

    private fun resolveSsidFromCapabilities(networkCapabilities: NetworkCapabilities): String? {
        val transportInfo = networkCapabilities.transportInfo
        return if (transportInfo is WifiInfo) {
            normalizeSsid(transportInfo.ssid)
        } else {
            null
        }
    }

    private fun resolveSsidFromWifiManager(context: Context): String? {
        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java) ?: return null
        @Suppress("DEPRECATION")
        return normalizeSsid(wifiManager.connectionInfo?.ssid)
    }
}
