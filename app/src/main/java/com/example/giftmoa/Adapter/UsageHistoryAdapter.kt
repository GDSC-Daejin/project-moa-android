package com.example.giftmoa.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.GifticonHistoryData
import com.example.giftmoa.Data.GifticonHistoryResponse
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.Data.UsageHistoryItem
import com.example.giftmoa.databinding.ItemGifticonUsageHistoryBinding
import com.example.giftmoa.databinding.ItemShareRoomBinding

class UsageHistoryAdapter: ListAdapter<GifticonHistoryData, UsageHistoryAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemGifticonUsageHistoryBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(usageHistory: GifticonHistoryData) {
            Glide.with(binding.ivUserProfileImage.context)
                .load(usageHistory.usedUser?.profileImageUrl)
                .into(binding.ivUserProfileImage)
            binding.tvUserName.text = usageHistory.usedUser?.nickname
            binding.tvUsedAmount.text = "${usageHistory.usedPrice.toString()}원"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGifticonUsageHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<GifticonHistoryData>() {
            override fun areItemsTheSame(oldItem: GifticonHistoryData, newItem: GifticonHistoryData): Boolean {
                return oldItem.gifticonHistoryId == newItem.gifticonHistoryId
            }

            override fun areContentsTheSame(oldItem: GifticonHistoryData, newItem: GifticonHistoryData): Boolean {
                return oldItem == newItem
            }
        }
    }
}