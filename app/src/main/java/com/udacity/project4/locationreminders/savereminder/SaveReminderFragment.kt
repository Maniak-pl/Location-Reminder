package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceUtil
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.GEOFENCE_RADIUS
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class SaveReminderFragment : BaseFragment() {

    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private lateinit var geofenceClient: GeofencingClient
    private lateinit var geofenceUtil: GeofenceUtil

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        geofenceClient = LocationServices.getGeofencingClient(context!!)
        geofenceUtil = GeofenceUtil(context)

        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            val geofenceId = UUID.randomUUID().toString()

            if (latitude != null && longitude != null && !title.isNullOrBlank()) {
                addGeofence(LatLng(latitude, longitude), GEOFENCE_RADIUS, geofenceId)
            }

            _viewModel.validateAndSaveReminder(
                ReminderDataItem(
                    title,
                    description,
                    location,
                    latitude,
                    longitude
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun addGeofence(latLng: LatLng, radius: Float, geofenceId: String) {
        val geofence: Geofence = geofenceUtil.getGeofence(
            geofenceId,
            latLng,
            radius,
            Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
        )
        val geofencingRequest: GeofencingRequest = geofenceUtil.getGeofencingRequest(geofence)
        val pendingIntent: PendingIntent? = geofenceUtil.getGeofencePendingIntent()
        geofenceClient.addGeofences(geofencingRequest, pendingIntent)
            .addOnSuccessListener(OnSuccessListener<Void?> {
                Timber.e("Geofence Added")
            })
            .addOnFailureListener(OnFailureListener { e ->
                val errorMessage: String = geofenceUtil.getErrorString(e)
                Timber.e("fail in creating geofence: $errorMessage")
            })
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}
