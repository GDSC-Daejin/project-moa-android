package com.example.giftmoa.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ItemGifticonBinding
import com.example.giftmoa.databinding.ItemShareRoomBinding

class GifticonListAdapter(private val onClick: (GifticonDetailItem) -> Unit): ListAdapter<GifticonDetailItem, GifticonListAdapter.ViewHolder>(diffUtil) {

    interface OnItemClickListener {
        fun onItemClick(position: Int) {}
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ViewHolder(private val binding: ItemGifticonBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(gifticon: GifticonDetailItem) {
            if (gifticon.gifticonImagePath != null) {
                Glide.with(binding.ivCouponImage.context)
                    .load(gifticon.gifticonImagePath)
                    .into(binding.ivCouponImage)
            } else {
                binding.ivCouponImage.setImageResource(R.drawable.asset_gifticon_coffee)
            }
            binding.tvCouponName.text = gifticon.name
            binding.tvCategoryName.text = gifticon.category?.categoryName ?: "미분류"
            //binding.tvDDay.text = "D+${gifticon.remainingDay}"

            binding.root.setOnClickListener {
                onClick(gifticon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemGifticonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<GifticonDetailItem>() {
            override fun areItemsTheSame(oldItem: GifticonDetailItem, newItem: GifticonDetailItem): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: GifticonDetailItem, newItem: GifticonDetailItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}