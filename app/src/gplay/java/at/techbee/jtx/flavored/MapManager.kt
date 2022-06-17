/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.content.Context
import android.content.SharedPreferences
import android.location.Geocoder
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import at.techbee.jtx.R
import at.techbee.jtx.database.ICalObject
import at.techbee.jtx.databinding.FragmentIcalEditLocationpickerDialogBinding
import at.techbee.jtx.databinding.FragmentIcalEditLocationpickerDialogChipBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class MapManager(val context: Context): MapManagerDefinition {

    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    private var lastLocation: LatLng? = null


    companion object {
        const val LAST_USED_LOCATION_LAT = "lastUsedLocationLat"
        const val LAST_USED_LOCATION_LNG = "lastUsedLocationLong"
    }

    init {
        MapsInitializer.initialize(context)
        val lat = prefs.getLong(LAST_USED_LOCATION_LAT, 0).let { java.lang.Double.longBitsToDouble(it) }
        val long = prefs.getLong(LAST_USED_LOCATION_LNG, 0).let { java.lang.Double.longBitsToDouble(it) }

        if(lat != 0.0 && long != 0.0)
            lastLocation = LatLng(lat, long)
    }


    override fun showLocationPickerDialog(inflater: LayoutInflater, iCalObject: MutableLiveData<ICalObject>) {

        var selectedLocationText: String? = null
        var selectedLatLong: LatLng? = null

        val locationPickerDialog = FragmentIcalEditLocationpickerDialogBinding.inflate(inflater)
        val mapView = MapView(context)
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync { googleMap ->

            selectedLatLong = if(iCalObject.value?.geoLat != null && iCalObject.value?.geoLong != null)
                LatLng(iCalObject.value!!.geoLat!!, iCalObject.value!!.geoLong!!)
            else
                lastLocation
            var marker: Marker? = null
            selectedLatLong?.let {
                marker = googleMap.addMarker(
                    MarkerOptions().apply {
                        position(it)
                        //.title("Marker")
                        icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    }
                )
            }

            // Enable GPS marker in Map
            //googleMap.isMyLocationEnabled = true
            selectedLatLong?.let { googleMap.moveCamera(CameraUpdateFactory.newLatLng(it)) }
            googleMap.uiSettings.isZoomControlsEnabled = true
            //googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f), 1000, null)
            selectedLatLong?.let{ googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f)) }
            googleMap.setOnMapClickListener {
                if(marker == null) {
                    marker = googleMap.addMarker(
                        MarkerOptions().apply {
                            position(it)
                            icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        }
                    )
                } else marker?.position = it
                selectedLatLong = it

                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(it.latitude, it.longitude, 5)
                locationPickerDialog.fragmentIcalEditLocationpickerDialogChipgroup.removeAllViews()
                addresses.forEach { address ->
                    if(address.maxAddressLineIndex >= 0) {
                        val chip = FragmentIcalEditLocationpickerDialogChipBinding.inflate(inflater).root
                        chip.text = address.getAddressLine(0)
                        locationPickerDialog.fragmentIcalEditLocationpickerDialogChipgroup.addView(chip)
                        chip.setOnCheckedChangeListener { _, checked ->
                            if(checked)
                                selectedLocationText = address.getAddressLine(0)
                        }
                    }
                }
            }
        }

        locationPickerDialog.fragmentIcalEditLocationpickerDialogLinearLayout.removeAllViews()
        locationPickerDialog.fragmentIcalEditLocationpickerDialogLinearLayout.addView(mapView)

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.location)
            .setView(locationPickerDialog.root)
            .setIcon(R.drawable.ic_location)
            .setPositiveButton(R.string.save)  { _, _ ->
                if(selectedLocationText?.isNotBlank() == true) {
                    iCalObject.value?.location = selectedLocationText
                }
                selectedLatLong?.let {
                    iCalObject.value?.geoLat = it.latitude
                    iCalObject.value?.geoLong = it.longitude

                    // save last location
                    prefs.edit().putLong(LAST_USED_LOCATION_LAT, java.lang.Double.doubleToRawLongBits(it.latitude)).apply()
                    prefs.edit().putLong(LAST_USED_LOCATION_LNG, java.lang.Double.doubleToRawLongBits(it.longitude)).apply()
                }
                iCalObject.postValue(iCalObject.value)    // post value to use two-way binding and update the ui

            }
            .setNeutralButton(R.string.cancel)  { _, _ -> return@setNeutralButton  /* nothing to do */  }
            .setNegativeButton(R.string.reset) { _, _, ->
                iCalObject.value?.geoLat = null
                iCalObject.value?.geoLong = null
                iCalObject.value?.location = null
                iCalObject.postValue(iCalObject.value)
            }
            .show()

    }

    override fun addMap(layout: LinearLayout, lat: Double, lng: Double, label: String?) {

        val latLng = LatLng(lat, lng)
        val mapView = MapView(context)
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync { googleMap ->

            googleMap.addMarker(
                MarkerOptions().apply {
                    position(latLng)
                    label?.let { title(it) }
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                }
            )

            // Enable GPS marker in Map
            //googleMap.isMyLocationEnabled = true
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
            googleMap.uiSettings.isZoomControlsEnabled = true
            //googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f), 1000, null)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        }
        layout.removeAllViews()
        layout.layoutParams = layout.layoutParams.apply {
            this.height = 400
        }
        layout.addView(mapView)
    }
}