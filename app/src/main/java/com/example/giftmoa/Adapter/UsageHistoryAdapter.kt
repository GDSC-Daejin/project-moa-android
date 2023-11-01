package com.example.giftmoa.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.Data.UsageHistoryItem
import com.example.giftmoa.databinding.ItemGifticonUsageHistoryBinding
import com.example.giftmoa.databinding.ItemShareRoomBinding

class UsageHistoryAdapter: ListAdapter<UsageHistoryItem, UsageHistoryAdapter.ViewHolder>(diffUtil) {

    interface OnItemClickListener {
        fun onItemClick(position: Int) {}
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ViewHolder(private val binding: ItemGifticonUsageHistoryBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(usageHistory: UsageHistoryItem) {
            Glide.with(binding.ivUserProfileImage.context)
                .load(usageHistory.profileImageUrl)
                .into(binding.ivUserProfileImage)
            binding.tvUserName.text = usageHistory.nickname
            binding.tvUsedAmount.text = "${usageHistory.usedAmount.toString()}원"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGifticonUsageHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<UsageHistoryItem>() {
            override fun areItemsTheSame(oldItem: UsageHistoryItem, newItem: UsageHistoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: UsageHistoryItem, newItem: UsageHistoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}