package com.example.giftmoa.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.databinding.ItemShareRoomBinding

class SmallShareRoomAdapter: ListAdapter<ShareRoomItem, SmallShareRoomAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemShareRoomBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(shareRoom: ShareRoomItem) {
            Glide.with(binding.ivShareRoomImage.context)
                .load(shareRoom.teamThumbnailImage)
                .into(binding.ivShareRoomImage)
            binding.tvShareRoomName.text = shareRoom.teamName
            Glide.with(binding.ivShareRoomUserImage01.context)
                .load(shareRoom.teamMembers?.get(0)?.profileImageUrl)
                .into(binding.ivShareRoomUserImage01)
            if (shareRoom.teamMembers?.size!! > 1) {
                binding.ivShareRoomUserImage02.visibility = ViewGroup.VISIBLE
                if (shareRoom.teamMembers.size > 2) {
                    binding.tvShareRoomCount.visibility = ViewGroup.VISIBLE
                    "+${shareRoom.teamMembers.size - 2}".also { binding.tvShareRoomCount.text = it }
                }
                Glide.with(binding.ivShareRoomUserImage02.context)
                    .load(shareRoom.teamMembers[1].profileImageUrl)
                    .into(binding.ivShareRoomUserImage02)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemShareRoomBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<ShareRoomItem>() {
            override fun areItemsTheSame(oldItem: ShareRoomItem, newItem: ShareRoomItem): Boolean {
                return oldItem.teamId == newItem.teamId
            }

            override fun areContentsTheSame(oldItem: ShareRoomItem, newItem: ShareRoomItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}