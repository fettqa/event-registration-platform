package com.fettqa.events.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fettqa.events.android.databinding.ItemRegistrationBinding
import com.fettqa.events.android.model.EventRegistrationResponse

class RegistrationAdapter(
    private val canDelete: (EventRegistrationResponse) -> Boolean,
    private val onDelete: (EventRegistrationResponse) -> Unit,
) : RecyclerView.Adapter<RegistrationAdapter.Holder>() {
    private val items = mutableListOf<EventRegistrationResponse>()

    fun submit(list: List<EventRegistrationResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemRegistrationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(
        private val binding: ItemRegistrationBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reg: EventRegistrationResponse) {
            binding.regFullName.text = reg.fullName.orEmpty()
            binding.regEmail.text = reg.email.orEmpty()
            val showDelete = canDelete(reg)
            binding.deleteRegButton.visibility = if (showDelete) View.VISIBLE else View.GONE
            binding.deleteRegButton.isEnabled = showDelete
            binding.deleteRegButton.setOnClickListener {
                if (showDelete) onDelete(reg)
            }
        }
    }
}
