package com.enesky.guvenlikbildir.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.enesky.guvenlikbildir.databinding.ItemOptionBinding
import com.enesky.guvenlikbildir.model.OptionItem

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

class OptionAdapter(private var optionItemList: List<OptionItem>,
                    private val optionListListener: OptionListListener)
    : RecyclerView.Adapter<OptionAdapter.OptionListViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OptionListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOptionBinding.inflate(inflater, parent, false)
        return OptionListViewHolder(binding)
    }

    override fun getItemCount(): Int = optionItemList.size

    override fun onBindViewHolder(holder: OptionListViewHolder, pos: Int) = holder.bind(pos, optionItemList[pos])

    inner class OptionListViewHolder(private val binding: ItemOptionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pos: Int, optionItem: OptionItem) {
            binding.optionItem = optionItem
            binding.clOptionItem.setOnClickListener { optionListListener.onItemClick(pos, optionItem) }
            binding.executePendingBindings()
        }
    }

    override fun getItemId(position: Int): Long = position.toLong()

    interface OptionListListener {
        fun onItemClick(pos: Int, optionItem: OptionItem)
    }

}