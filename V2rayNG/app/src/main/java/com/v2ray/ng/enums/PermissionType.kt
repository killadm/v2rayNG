package com.v2ray.ng.enums

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

/**
 * Permission types used in the app, handling API level differences.
 */
enum class PermissionType {
    /** Camera permission (used for scanning QR codes) */
    CAMERA {
        override fun getPermissions(): Array<String> = arrayOf(Manifest.permission.CAMERA)
    },

    /** Read storage / media permission (adapts to Android version) */
    READ_STORAGE {
        override fun getPermissions(): Array<String> = arrayOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
        )
    },

    /** Notification permission (Android 13+) */
    POST_NOTIFICATIONS {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        override fun getPermissions(): Array<String> = arrayOf(Manifest.permission.POST_NOTIFICATIONS)
    },

    /** Permissions required to read the current Wi-Fi SSID */
    WIFI_SSID {
        override fun getPermissions(): Array<String> = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )

        override fun isGranted(context: Context, requestResult: Map<String, Boolean>?): Boolean {
            return locationPermissions.any { permission ->
                requestResult?.get(permission) == true ||
                    ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
            }
        }
    };

    /** Return the Android permission strings required for the action */
    abstract fun getPermissions(): Array<String>

    open fun isGranted(context: Context, requestResult: Map<String, Boolean>? = null): Boolean {
        return getPermissions().all { permission ->
            requestResult?.get(permission) == true ||
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    /** Return a human-readable label for the permission */
    fun getLabel(): String {
        return when (this) {
            CAMERA -> "Camera"
            READ_STORAGE -> "Storage"
            POST_NOTIFICATIONS -> "Notification"
            WIFI_SSID -> "Location"
        }
    }

    companion object {
        private val locationPermissions = setOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}
