/*
 * Copyright (C) 2026 Rafa Couto <caligari@treboada.net>
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Full licence text: LICENSE, at the root of this repository.
 */

package net.treboada.opensatfinder.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat

/** How a subscription attempt ended, so the caller can pick a state to show. */
enum class LocationStart {
    Started,
    NoPermission,
    NoProvider,
}

/**
 * Reads positions from the platform's own location service.
 *
 * The position is only asked for while the screen is visible, and it never leaves
 * the device: nothing here talks to a network.
 */
class LocationSource(
    private val context: Context,
    private val locationManager: LocationManager,
) {
    private var onFix: ((GeoPoint, Float?, Float) -> Unit)? = null
    private var subscribed = false

    private val listener =
        LocationListener { location ->
            publish(location)
        }

    @SuppressLint("MissingPermission") // checked on the line below, then used.
    fun start(onFix: (GeoPoint, Float?, Float) -> Unit): LocationStart {
        this.onFix = onFix
        if (!hasPermission(context)) return LocationStart.NoPermission

        val provider = chooseProvider() ?: return LocationStart.NoProvider
        Log.d(TAG, "asking $provider for positions")
        return try {
            // A position from a minute ago beats an empty screen; the accuracy is
            // shown next to it, so the user can judge it.
            locationManager.getLastKnownLocation(provider)?.let(::publish)
            locationManager.requestLocationUpdates(
                provider,
                UPDATE_INTERVAL_MS,
                MIN_DISTANCE_METRES,
                listener,
                Looper.getMainLooper(),
            )
            subscribed = true
            LocationStart.Started
        } catch (denied: SecurityException) {
            // The permission was revoked between the check and the request.
            this.onFix = null
            LocationStart.NoPermission
        }
    }

    fun stop() {
        onFix = null
        if (!subscribed) return
        locationManager.removeUpdates(listener)
        subscribed = false
    }

    @Suppress("DEPRECATION")
    private fun chooseProvider(): String? {
        // requestLocationUpdates with a provider is deprecated in favour of
        // LocationRequest, which needs API 31; minSdk here is 24, so this single
        // code path is the one that works everywhere.
        val hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        return when (locationProvider(hasGps = hasGps, hasNetwork = hasNetwork)) {
            LocationProvider.Gps -> LocationManager.GPS_PROVIDER
            LocationProvider.Network -> LocationManager.NETWORK_PROVIDER
            LocationProvider.None -> null
        }
    }

    private fun publish(location: Location) {
        val point = GeoPoint.of(location.latitude, location.longitude) ?: return
        val accuracy = if (location.hasAccuracy()) location.accuracy else null
        val altitude = if (location.hasAltitude()) location.altitude.toFloat() else 0f
        val declination = magneticDeclinationAt(point, altitudeMetres = altitude, atMillis = System.currentTimeMillis())
        onFix?.invoke(point, accuracy, declination)
    }

    companion object {
        private const val TAG = "OpenSatFinder"

        /** Two seconds and five metres: lively enough to follow a walk. */
        const val UPDATE_INTERVAL_MS = 2_000L
        const val MIN_DISTANCE_METRES = 5f

        fun hasPermission(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }
}
