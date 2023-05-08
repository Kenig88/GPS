package com.kenig.gps.fragments

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.kenig.gps.utils.MainApp
import com.kenig.gps.utils.MainViewModel
import com.kenig.gps.R
import com.kenig.gps.database.TrackItem
import com.kenig.gps.databinding.FragmentMainBinding
import com.kenig.gps.location.LocationModel
import com.kenig.gps.location.LocationService
import com.kenig.gps.utils.DialogManager
import com.kenig.gps.utils.TimeUtils
import com.kenig.gps.utils.isPermissionGranted
import com.kenig.gps.utils.showToast
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*


class MainFragment : Fragment() {
    private var locationModel: LocationModel? = null //19.1
    private var pl: Polyline? = null //16
    private var firstStart = true //16.7
    private var isServiceRunning = false //8.5.1
    private var timer: Timer? = null //9.1
    private var startTime = 0L //9.2
    private lateinit var permissionLauncher: ActivityResultLauncher<String> //5
    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels{ //14.1
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }



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
        checkServiceState() //8.6.1
        updateTime() //9.3.2
        registerLocReceiver() //13.4.1
        locationUpdates() //14.3\
        model.tracks.observe(viewLifecycleOwner){ //19.4
            Log.d("MyLog", "List size: ${it.size}")
        }
    }

    private fun setOnClicks() = with(binding){ //8.4
        val listener = onClicks() //8.3.1
        fStartStop.setOnClickListener(listener)
    }

    private fun updateTime(){ //9.3.1
        model.timeData.observe(viewLifecycleOwner){
            binding.tvTime.text = it
        }
    }

    private fun locationUpdates() = with(binding){ //14.2
        model.locationUpdates.observe(viewLifecycleOwner){
            val distance = "${resources.getString(R.string.distance)} ${String.format("%.1f", it.distance)} m"
            val speed = "${resources.getString(R.string.speed)} ${String.format("%.1f", 3.6f * it.speed)} km/h"
            val averageSpeed = "${resources.getString(R.string.average_speed)} ${getAverageSpeed(it.distance)} km/h" //15.1
            tvDistance.text = distance
            tvAvSpeed.text = speed
            tvAverageSpeed.text = averageSpeed //15.2
            locationModel = it //19.2
            updatePolyline(it.geoPointsList) //16.6.1
        }
    }

    private fun startTimer(){ //9.1.1
        timer?.cancel()
        timer = Timer()
        startTime = LocationService.startTime //9.5.1
        timer?.schedule(object : TimerTask(){
            override fun run() {
                activity?.runOnUiThread{
                    model.timeData.value = getCurrentTime() //9.4.1
                }
            }
        }, 1, 1)
    }

    private fun getAverageSpeed(distance: Float): String{ //15
        return String.format("%.1f", 3.6f * (distance / ((System.currentTimeMillis() - startTime) / 1000)))
    }

    private fun getCurrentTime(): String{ //9.4
        return "Time: ${TimeUtils.getTime(System.currentTimeMillis() - startTime)}"
    }

    private fun onClicks(): View.OnClickListener{ //8.3
        return View.OnClickListener {
            when(it.id){
                R.id.fStartStop -> startStopService()
            }
        }
    }

    private fun geoPointsToString(list: List<GeoPoint>): String{ //19
        val sb = StringBuilder()
        list.forEach{
            sb.append("${it.latitude}, ${it.longitude}/")
        }
        Log.d("MyLog", "Points: $sb")
        return sb.toString()
    }

    private fun startStopService(){ //8.5
        if(!isServiceRunning){ //(false)
            startLocService() //8.2.1
        } else {
            activity?.stopService(Intent(activity, LocationService::class.java))
            binding.fStartStop.setImageResource(R.drawable.ic_start)
            timer?.cancel() //9.1.2
            val track = getTrackItem() //19.5
            DialogManager.showSaveDialog(requireContext(),
                track, //19.5.1
                object : DialogManager.Listener{ //17.1
                override fun onClick() {
                    showToast("Track saved!")
                    model.insertTrack(track)
                }
            })
        }
        isServiceRunning = !isServiceRunning
    }

    private fun getTrackItem(): TrackItem{ //19.3
        return TrackItem(
                null,
                getCurrentTime(),
                TimeUtils.getDate(),
                "${resources.getString(R.string.avg_speed)} ${getAverageSpeed(locationModel!!.distance)} km/h",
                "${resources.getString(R.string.distance)} ${String.format("%.1f", locationModel!!.distance)} m",
                geoPointsToString(locationModel!!.geoPointsList)
            )
    }

    private fun checkServiceState(){//8.6
        isServiceRunning = LocationService.isRunning
        if(isServiceRunning){
            binding.fStartStop.setImageResource(R.drawable.ic_stop)
            startTimer()
        }
    }

    private fun startLocService(){ //8.2
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            activity?.startForegroundService(Intent(activity, LocationService::class.java))
        } else {
            activity?.startService(Intent(activity, LocationService::class.java))
        }
        binding.fStartStop.setImageResource(R.drawable.ic_stop)
        LocationService.startTime = System.currentTimeMillis() //9.5.2
        startTimer() //9.4.2
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
        pl = Polyline() //16.1
        pl?.outlinePaint?.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("color_key", "#14AD00"))
        mapView.controller.setZoom(18.0)
        val mLocProvider = GpsMyLocationProvider(activity)
        val mLocOverlay = MyLocationNewOverlay(mLocProvider, mapView)
        mLocOverlay.enableMyLocation()
        mLocOverlay.enableFollowLocation() //(карта автоматически переместится в то место где я нахожусь из за включенной геолокации в настройках)
        mLocOverlay.runOnFirstFix{
            mapView.overlays.clear()
            mapView.overlays.add(mLocOverlay)
            mapView.overlays.add(pl) //16.5
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

    private val receiver = object : BroadcastReceiver(){ //13.3(получает BroadcastIntent(locModel) с LocationService)
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent?.action == LocationService.LOC_MODEL_INTENT){
                val locModel = intent.getSerializableExtra(LocationService.LOC_MODEL_INTENT) as LocationModel
                model.locationUpdates.value = locModel //14.3
            }
        }
    }

    private fun registerLocReceiver(){ //13.4
        val locFilter = IntentFilter(LocationService.LOC_MODEL_INTENT)
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).registerReceiver(receiver, locFilter)
    }

    private fun addPoint(list: List<GeoPoint>){ //16.3
        pl?.addPoint(list[list.size - 1])
    }

    private fun fillPolyline(list: List<GeoPoint>){ //16.4
        list.forEach {
            pl?.addPoint(it)
        }
    }

    private fun updatePolyline(list: List<GeoPoint>){ //16.6
        if(list.size > 1 && firstStart){
            fillPolyline(list)
            firstStart = false
        } else {
            addPoint(list)
        }
    }

    override fun onDetach() { //16.8
        super.onDetach()
        LocalBroadcastManager.getInstance(activity as AppCompatActivity).unregisterReceiver(receiver)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}