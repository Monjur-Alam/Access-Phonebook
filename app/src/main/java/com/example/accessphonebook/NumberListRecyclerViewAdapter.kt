package com.example.accessphonebook

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.accessphonebook.BR
import com.example.accessphonebook.R
import com.example.accessphonebook.databinding.ContactListItemBinding

class NumberListRecyclerViewAdapter(
    private val contactList: ArrayList<String>,
    private val listener: NumberClickListener
) : RecyclerView.Adapter<NumberListRecyclerViewAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding: ContactListItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.contact_list_item, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataModel = contactList[position]
        holder.bind(dataModel)

        holder.binding.contactNumber.setOnClickListener { listener.onItemClick(dataModel) }
    }

    override fun getItemCount(): Int {
        return contactList.size
    }

    inner class ViewHolder(var binding: ContactListItemBinding) : RecyclerView.ViewHolder(
        binding.root
    ) {
        fun bind(obj: Any?) {
            binding.setVariable(BR.number, obj)
            binding.executePendingBindings()
        }
    }
}