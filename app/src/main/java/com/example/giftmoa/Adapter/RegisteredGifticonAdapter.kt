package com.example.giftmoa.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.databinding.ItemRegisteredGifticonBinding
class RegisteredGifticonAdapter: ListAdapter<ParsedGifticon, RegisteredGifticonAdapter.ViewHolder>(
    diffUtil
) {

    interface OnItemClickListener {
        fun onItemClick(position: Int) {}
    }

    var itemClickListener: OnItemClickListener? = null

    inner class ViewHolder(private val binding: ItemRegisteredGifticonBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(gifticon: ParsedGifticon) {
            binding.tvCouponName.text = gifticon.name
            Glide.with(binding.ivCouponImage.context)
                .load(gifticon.image)
                .into(binding.ivCouponImage)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRegisteredGifticonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<ParsedGifticon>() {
            override fun areItemsTheSame(oldItem: ParsedGifticon, newItem: ParsedGifticon): Boolean {
                return oldItem.orderNumber == newItem.orderNumber
            }

            override fun areContentsTheSame(oldItem: ParsedGifticon, newItem: ParsedGifticon): Boolean {
                return oldItem == newItem
            }
        }
    }
}