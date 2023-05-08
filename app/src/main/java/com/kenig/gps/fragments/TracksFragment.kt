package com.kenig.gps.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.kenig.gps.utils.MainApp
import com.kenig.gps.utils.MainViewModel
import com.kenig.gps.database.TrackAdapter
import com.kenig.gps.database.TrackItem
import com.kenig.gps.databinding.FragmentTracksBinding
import com.kenig.gps.utils.openFragment


class TracksFragment : Fragment(), TrackAdapter.Listener { //20.4 (Listener)
    private lateinit var binding: FragmentTracksBinding
    private lateinit var adapter: TrackAdapter
    private val model: MainViewModel by activityViewModels{ //20.2
        MainViewModel.ViewModelFactory((requireContext().applicationContext as MainApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRcView() //20.1.1
        getTracks() //20.3.1
    }

    private fun initRcView() = with(binding){ //20.1
        adapter = TrackAdapter(this@TracksFragment) //20.7
        rcView.layoutManager = LinearLayoutManager(requireContext())
        rcView.adapter = adapter
    }

    private fun getTracks(){ //20.3
        model.tracks.observe(viewLifecycleOwner){
            adapter.submitList(it)
            binding.tvEmpty.visibility = if(it.isEmpty()) View.VISIBLE else View.GONE //(если в списке пусто, то TextView будет видно )
        }
    }

    override fun onClick(track: TrackItem, type: TrackAdapter.ClickType) { //20.5
       when(type){
           TrackAdapter.ClickType.DELETE -> model.deleteTrack(track) //20.12
           TrackAdapter.ClickType.OPEN -> {
               model.currentTrack.value = track //21.3
               openFragment(ViewTrackFragment.newInstance())
           }
       }
    }

    companion object {
        @JvmStatic
        fun newInstance() = TracksFragment()
    }
}