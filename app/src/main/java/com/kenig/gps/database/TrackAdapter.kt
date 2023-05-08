package com.kenig.gps.database

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kenig.gps.R
import com.kenig.gps.databinding.TrackItemBinding


//20

class TrackAdapter(private val listener: Listener) : ListAdapter<TrackItem, TrackAdapter.Holder>(Comparator()){ //20.6 (Listener)

    class Holder(view: View, private val listener: Listener) : RecyclerView.ViewHolder(view), View.OnClickListener { //20 (onClick нужен чтобы можно было удалять trackItem)
        private val binding = TrackItemBinding.bind(view)
        private var trackTemp: TrackItem? = null //20.8 (временная переменная)
        init { //20.1
            binding.ibDelete.setOnClickListener(this)
            binding.item.setOnClickListener(this) //21.3
        }

        fun bind(track: TrackItem) = with(binding) {
            trackTemp = track
            tvDate.text = track.date
            tvDistance.text = track.distance
            tvAvSpeed.text = track.av_speed
            tvTime.text = track.time
        }

        override fun onClick(view: View) { //20.2
            val type = when(view.id){ //21.4
                R.id.ibDelete -> ClickType.DELETE
                R.id.item -> ClickType.OPEN
                else -> ClickType.OPEN
            }
            trackTemp?.let { listener.onClick(it, type) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_item, parent, false)
        return Holder(view, listener) //20.9 (нужно передать listener)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Comparator : DiffUtil.ItemCallback<TrackItem>() {
        override fun areItemsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem.id == newItem.id //(здесь сравниваю по id)
        }
        override fun areContentsTheSame(oldItem: TrackItem, newItem: TrackItem): Boolean {
            return oldItem == newItem //(а здесь полностью)
        }
    }

    interface Listener{ //20.3
        fun onClick(track: TrackItem, type: ClickType)//21.1
    }

    enum class ClickType{ //21
        DELETE,
        OPEN
    }
}