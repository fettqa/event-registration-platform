package com.fettqa.events.android.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fettqa.events.android.R
import com.fettqa.events.android.databinding.ItemEventBinding
import com.fettqa.events.android.model.EventResponse

class EventAdapter(
    private val onClick: (EventResponse) -> Unit,
) : RecyclerView.Adapter<EventAdapter.Holder>() {
    private val items = mutableListOf<EventResponse>()

    fun submit(events: List<EventResponse>) {
        items.clear()
        items.addAll(events)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(
        private val binding: ItemEventBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: EventResponse) {
            binding.eventName.text = event.name
            binding.eventSeats.text = binding.root.context.getString(R.string.max_seats, event.maxSeats)
            binding.root.setOnClickListener { onClick(event) }
        }
    }
}
