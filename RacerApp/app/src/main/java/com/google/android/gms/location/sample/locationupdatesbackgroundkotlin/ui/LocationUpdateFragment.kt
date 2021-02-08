/*
 * Copyright (C) 2020 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.ui

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.bupazar.User
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.*
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.data.SocketPacket
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.data.db.MyLocationEntity
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.databinding.FragmentLocationUpdateBinding
import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.viewmodels.LocationUpdateViewModel
import com.google.android.gms.maps.MapView
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.SocketException
import java.util.concurrent.Executors
import kotlin.math.floor
import kotlin.math.roundToInt

private const val TAG = "LocationUpdateFragment"

/**
 * Displays location information via PendingIntent after permissions are approved.
 *
 * Will suggest "enhanced feature" to enable background location requests if not approved.
 */
class LocationUpdateFragment : Fragment() {

    private var activityListener: Callbacks? = null

    var rosterMapView: MapView? = null

    private lateinit var binding: FragmentLocationUpdateBinding

    private val locationUpdateViewModel by lazy {
        ViewModelProviders.of(this).get(LocationUpdateViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is Callbacks) {
            activityListener = context

            // If fine location permission isn't approved, instructs the parent Activity to replace
            // this fragment with the permission request fragment.
            if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                activityListener?.requestFineLocationPermission()
            }
        } else {
            throw RuntimeException("$context must implement LocationUpdateFragment.Callbacks")
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLocationUpdateBinding.inflate(inflater, container, false)

        binding.enableBackgroundLocationButton.setOnClickListener {
            activityListener?.requestBackgroundLocationPermission()
        }

        return binding.root
    }



    private fun calcDist(a : MyLocationEntity, b : MyLocationEntity) : Double{
        val locationA = Location("point A")
        locationA.latitude = a.latitude
        locationA.longitude = a.longitude
        val locationB = Location("point B")
        locationB.latitude = b.latitude
        locationB.longitude = b.longitude
        val meters = floor(locationA.distanceTo(locationB).toDouble())

        return meters
    }

    private fun calcAllDist(locations: List<MyLocationEntity>) : Double{
        if(locations.isEmpty())
            return 0.0
        var prev = locations[0]
        var sum = 0.0
        locations.drop(0)
        for (location in locations)
        {
            sum += calcDist(prev, location)
            prev = location
        }
        return sum
    }

    private fun calcLastDist(locations: List<MyLocationEntity>) : Double{
        if(locations.size<2)
            return 0.0;
        return calcDist(locations[0], locations[1])
    }

    private fun calcTotalTime(locations: List<MyLocationEntity>) : Long{
        if(locations.isEmpty())
            return 0
        return (locations[0].date.time - locations.last().date.time)
    }

    private fun getActiveList(locations: List<MyLocationEntity>) : List<MyLocationEntity> {
        if(locations.size<1)
            return locations
        var ind = 0
        while(ind+1 < locations.size) {

            if(locations[ind].date.time - locations[ind+1].date.time > 10000)
                return locations.subList(0,ind+1)
            ind++;
        }
        return locations
    }
    private fun onLocationChangeLocationList(locations: List<MyLocationEntity>) {
        var dist = calcAllDist(locations)
        val last = calcLastDist(locations)
        val time = calcTotalTime(locations)
        val seconds =  (time/1000)%60
        val minutes = time/60000
        val speed = calcSpeed(locations)

        if(dist<1000)
            binding.totalDistText.text = "$dist Meters"
        else {
            dist /= 1000
            binding.totalDistText.text = "$dist Kilometers"
        }
        binding.totalTimeText.text = "$minutes : $seconds"

        binding.speedText.text = "$speed km/h"

        if(speed>1) {
            val totSec: Int = floor(1 / speed * 60 * 60).roundToInt()
            val paceSec = totSec % 60
            val paceMin = totSec / 60
            binding.avgPaceText.text = "$paceMin:$paceSec"
        }
        else
            binding.avgPaceText.text = "--:--"
        var curDist = calcAllDist(getActiveList(locations))
        val curTime = calcTotalTime(getActiveList(locations))
        val curSec = (curTime/1000)%60
        val curMin =  curTime/60000
        binding.timeText.text = "$curMin : $curSec"
        if(curDist<1000)
            binding.distText.text = "$curDist Meters"
        else {
            curDist /= 1000
            binding.distText.text = "$curDist Kilometers"
        }

        Executors.newSingleThreadExecutor().execute {
            sendPacket(SocketPacket(
                    operation = O_DISTANCE_UPDATE,
                    username = User.username,
                    password = User.password,
                    distTaken = last
            ))

        }

    }

    private fun calcSpeed(locations: List<MyLocationEntity>): Double {
        var newLoc = locations.subList(0,10);
        return floor(calcAllDist(newLoc)*60*60/calcTotalTime(newLoc))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUpdateViewModel.receivingLocationUpdates.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer { receivingLocation ->
                    updateStartOrStopButtonState(receivingLocation)
                }
        )

        locationUpdateViewModel.locationListLiveData.observe(
                viewLifecycleOwner,
                androidx.lifecycle.Observer { locations ->
                    locations?.let {
                        Log.d(TAG, "Got ${locations.size} locations")

                        if (locations.isEmpty()) {

                        } else {
                            onLocationChangeLocationList(locations)
                        }
                    }
                }
        )
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundButtonState()
    }

    override fun onPause() {
        super.onPause()

        // Stops location updates if background permissions aren't approved. The FusedLocationClient
        // won't trigger any PendingIntents with location updates anyway if you don't have the
        // background permission approved, but it's best practice to unsubscribing anyway.
        // To simplify the sample, we are unsubscribing from updates here in the Fragment, but you
        // could do it at the Activity level if you want to continue receiving location updates
        // while the user is moving between Fragments.
        if ((locationUpdateViewModel.receivingLocationUpdates.value == true) &&
            (!requireContext().hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
            locationUpdateViewModel.stopLocationUpdates()
        }
    }

    override fun onDetach() {
        super.onDetach()

        activityListener = null
    }

    private fun showBackgroundButton(): Boolean {
        return !requireContext().hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun updateBackgroundButtonState() {
        if (showBackgroundButton()) {
            binding.enableBackgroundLocationButton.visibility = View.VISIBLE
        } else {
            binding.enableBackgroundLocationButton.visibility = View.GONE
        }
    }

    private fun updateStartOrStopButtonState(receivingLocation: Boolean) {
        if (receivingLocation) {
            binding.startOrStopLocationUpdatesButton.apply {
                text = getString(R.string.stop_receiving_location)
                setOnClickListener {
                    locationUpdateViewModel.stopLocationUpdates()
                }
            }
        } else {
            binding.startOrStopLocationUpdatesButton.apply {
                text = getString(R.string.start_receiving_location)
                setOnClickListener {
                    locationUpdateViewModel.startLocationUpdates()
                }
            }
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface Callbacks {
        fun requestFineLocationPermission()
        fun requestBackgroundLocationPermission()
    }

    companion object {
        fun newInstance() = LocationUpdateFragment()
    }
}
