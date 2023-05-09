package com.kenig.gps.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.kenig.gps.R
import com.kenig.gps.databinding.FragmentViewTrackBinding
import com.kenig.gps.utils.MainApp
import com.kenig.gps.utils.MainViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline


class ViewTrackFragment : Fragment() {
    private var startPoint: GeoPoint? = null //23.1
    private lateinit var binding: FragmentViewTrackBinding
    private val model: MainViewModel by activityViewModels{ //21.4
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        settingsOsm() //21.1
        binding = FragmentViewTrackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getTrack() //21.5.1
        buttonMyLoc() //23.2.2
    }

    private fun buttonMyLoc() = with(binding){ //23.2
        fMyLoc.setOnClickListener{
            if(startPoint != null) mapView.controller.animateTo(startPoint)
        }
    }

    private fun getTrack() = with(binding){ //21.5
        model.currentTrack.observe(viewLifecycleOwner){
            val date = "Date: ${it.date}"
            tvDate.text = date
            tvTime.text = it.time
            tvAverageSpeed.text = it.av_speed
            tvDistance.text = it.distance
            val polyline = getPolyline(it.geoPoints) //22.2
            mapView.overlays.add(polyline) //22.2.1
            setMarkers(polyline.actualPoints) //22.3.1
            goToStartPosition(polyline.actualPoints[0]) //22.1.1
            startPoint = polyline.actualPoints[0] //23.2.1
        }
    }

    private fun getPolyline(geoPoints: String): Polyline{ //22 (отрисовка Polyline на карте)
        val polyline = Polyline()
        polyline.outlinePaint.color = Color.parseColor(
            PreferenceManager.getDefaultSharedPreferences(requireContext())
                .getString("color_key", "#14AD00")
        )
        val list = geoPoints.split('/')
        list.forEach{
            if(it.isEmpty()) return@forEach
            val points = it.split(',')
            polyline.addPoint(GeoPoint(points[0].toDouble(), points[1].toDouble()))
        }
        return polyline
    }

    private fun goToStartPosition(startPosition: GeoPoint){ //22.1
        binding.mapView.controller.setZoom(16.0)
        binding.mapView.controller.animateTo(startPosition)
    }

    private fun setMarkers(list: List<GeoPoint>) = with(binding) { //22.3
        val startMarker = Marker(mapView)
        val finishMarker = Marker(mapView)
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        finishMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        startMarker.icon = getDrawable(requireContext(), R.drawable.ic_start_marker)
        finishMarker.icon = getDrawable(requireContext(), R.drawable.ic_finish_marker)
        startMarker.position = list[0]
        finishMarker.position = list[list.size - 1]
        mapView.overlays.add(startMarker)
        mapView.overlays.add(finishMarker)
    }

    private fun settingsOsm(){ //21
        Configuration.getInstance().load(
            activity as AppCompatActivity,
            activity?.getSharedPreferences("osm.pref", Context.MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    companion object {
        @JvmStatic
        fun newInstance() = ViewTrackFragment()
    }
}