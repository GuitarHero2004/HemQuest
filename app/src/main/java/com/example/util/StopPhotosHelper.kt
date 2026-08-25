package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.QuestStop

object StopPhotosHelper {

    /**
     * Opens Google Maps for direct turn-by-turn routing and navigation to the checkpoint.
     */
    fun openGoogleMapsRoute(context: Context, latitude: Double, longitude: Double, name: String = "") {
        try {
            val encodedName = Uri.encode(name)
            val uri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=w")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Web / Universal fallback
            try {
                val encodedName = Uri.encode(name)
                val fallbackUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=walking")
                val intent = Intent(Intent.ACTION_VIEW, fallbackUri)
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Không thể mở Google Maps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens Google Maps to inspect the specific pinpoint location and landmark details.
     */
    fun openGoogleMapsLocation(context: Context, latitude: Double, longitude: Double, name: String = "") {
        try {
            val encodedName = Uri.encode(name)
            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedName)")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.google.android.apps.maps")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallbackUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                val intent = Intent(Intent.ACTION_VIEW, fallbackUri)
                context.startActivity(intent)
            } catch (ex: Exception) {
                Toast.makeText(context, "Không thể mở Google Maps", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Opens official Google Maps Street View 360° interactive real panorama.
     */
    fun openGoogleStreetView360(context: Context, latitude: Double, longitude: Double) {
        try {
            val streetViewUri = Uri.parse("google.streetview:cbll=$latitude,$longitude")
            val intent = Intent(Intent.ACTION_VIEW, streetViewUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Web fallback to Google Maps Panorama
            try {
                val webUri = Uri.parse("https://www.google.com/maps/@?api=1&map_action=pano&viewpoint=$latitude,$longitude")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
            } catch (ex: Exception) {
                Toast.makeText(context, "Không thể mở Google Street View", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Helper to get list of photos if needed
     */
    fun getPhotos(stop: QuestStop): List<String> {
        return emptyList()
    }
}
