package com.kenig.gps.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.kenig.gps.R
import com.kenig.gps.databinding.FragmentMainBinding
import com.kenig.gps.location.LocationService
import com.kenig.gps.utils.DialogManager
import com.kenig.gps.utils.isPermissionGranted
import com.kenig.gps.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainFragment : Fragment() {
    private var isServiceRunning = false //8.5.1
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //5
    private lateinit var binding: FragmentMainBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm() //4.0.1
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPermissions() //5.1.1
        setOnClicks() //8.4.1
        checkServiceType() //8.6.1
    }

    private fun setOnClicks() = with(binding){ //8.4
        val listener = onClicks() //8.3.1
        fStartStop.setOnClickListener(listener)
    }

    private fun onClicks(): View.OnClickListener{ //8.3
        return View.OnClickListener {
            when(it.id){
                R.id.fStartStop -> startStopService()
            }
        }
    }

    private fun startStopService(){ //8.5
        if(!isServiceRunning){ //(false)
            startLocService() //8.2.1
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_start)
        }
        isServiceRunning = !isServiceRunning
    }

    private fun checkServiceType(){//8.6
        isServiceRunning = LocationService.isRunning
        if(isServiceRunning){
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
        }
    }

    private fun startLocService(){ //8.2
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.fStartStop.setImageResource(R.drawable.ic_stop)
    }

    override fun onResume() {
        super.onResume()
        checkLocPermission() //5.2.1
    }

    private fun settingsOsm(){ //4 (инициализация OSM)
        Configuration.getInstance().load(activity as AppCompatActivity,
            activity?.getSharedPreferences("osm_pref.bg", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    private fun initOsm() = with(binding){ //4.1 (не забыть добавить разрешения в Manifest)
        mapView.controller.setZoom(13.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, mapView)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation() //(карта автоматически переместится в то место где я нахожусь из за включенной геолокации в настройках)
        mLocOverlay.runOnFirstFix{
            mapView.overlays.clear()
            mapView.overlays.add(mLocOverlay)
        }
    }

    private fun registerPermissions(){ //5.1
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){
            if(isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                initOsm() //4.1.1
                checkLocationEnabled() //6.0.1
            } else {
                showToast("Вы не дали разрешение на использование местоположения")
            }
        }
    }

    private fun checkLocPermission(){ //5.2
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            checkPermissionAfter10() //5.3.1
        } else {
            checkPermissionBefore10() //5.4.1
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun checkPermissionAfter10(){ //5.3
        if(isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)
            && isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)){
            initOsm()
            checkLocationEnabled() //6.0.2
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }

    private fun checkPermissionBefore10(){ //5.4
        if(isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)){
            initOsm()
            checkLocationEnabled() //6.0.3
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun checkLocationEnabled() { //6
        val locManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isEnabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isEnabled) { //(знак "!" обозначает "не")
            DialogManager.showLocEnabledDialog(activity as AppCompatActivity,
                object : DialogManager.Listener { //7.0.1
                    override fun onClick() {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) //(через Intent открываются настройки)
                    }
                }
            )
        } else {
            showToast("GPS включено")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}