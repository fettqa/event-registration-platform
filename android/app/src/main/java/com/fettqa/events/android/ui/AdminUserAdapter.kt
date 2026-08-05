package com.fettqa.events.android.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fettqa.events.android.R
import com.fettqa.events.android.databinding.ItemAdminUserBinding
import com.fettqa.events.android.model.UserResponse

class AdminUserAdapter(
    private val onSave: (UserResponse, String) -> Unit,
) : RecyclerView.Adapter<AdminUserAdapter.Holder>() {
    private val items = mutableListOf<UserResponse>()

    fun submit(users: List<UserResponse>) {
        items.clear()
        items.addAll(users)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = ItemAdminUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class Holder(
        private val binding: ItemAdminUserBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: UserResponse) {
            binding.userFullName.text = user.fullName.orEmpty()
            binding.userEmail.text = user.email.orEmpty()
            val roles = binding.root.resources.getStringArray(R.array.assignable_roles)
            binding.roleSpinner.adapter = ArrayAdapter(
                binding.root.context,
                android.R.layout.simple_spinner_dropdown_item,
                roles,
            )
            val index = roles.indexOf(user.role).takeIf { it >= 0 } ?: 0
            binding.roleSpinner.setSelection(index)
            binding.saveRoleButton.setOnClickListener {
                val selected = binding.roleSpinner.selectedItem as String
                onSave(user, selected)
            }
        }
    }
}
