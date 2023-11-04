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
import java.text.SimpleDateFormat
import java.util.Date

class GifticonListAdapter(private val onClick: (GifticonDetailItem) -> Unit): ListAdapter<GifticonDetailItem, GifticonListAdapter.ViewHolder>(diffUtil) {

    interface OnItemLongClickListener {
        fun onItemLongClick(position: Int)
    }

    var itemLongClickListener: OnItemLongClickListener? = null

    inner class ViewHolder(private val binding: ItemGifticonBinding): RecyclerView.ViewHolder(binding.root) {

        init {
            binding.ivCouponImage.setOnLongClickListener {
                itemLongClickListener?.onItemLongClick(adapterPosition)
                return@setOnLongClickListener true
            }
        }

        fun bind(gifticon: GifticonDetailItem) {
            if (gifticon.gifticonImagePath != null) {
                Glide.with(binding.ivCouponImage.context)
                    .load(gifticon.gifticonImagePath)
                    .into(binding.ivCouponImage)
            } else {
                binding.ivCouponImage.setImageResource(R.drawable.asset_gifticon_coffee)
            }

            binding.tvCouponName.text = gifticon.name
            binding.tvCouponExchangePlace.text = gifticon.exchangePlace

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                val dueDate = inputFormat.parse(gifticon.dueDate)

                val currentDate = Date()
                val timeDifference = dueDate.time - currentDate.time
                val daysDifference = timeDifference / (1000 * 60 * 60 * 24)

                if (daysDifference >= 0) {
                    val dDayText = "D-${daysDifference}"
                    binding.tvDDay.text = dDayText
                } else {
                    binding.tvDDay.text = "만료"
                }
            } catch (e: Exception) {
                // 날짜 파싱에 실패한 경우나 예외 처리
                binding.tvDDay.text = "날짜 형식 오류"
            }


            binding.ivCouponImage.setOnClickListener {
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

    /*fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
        this.itemLongClickListener = listener
    }*/

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<GifticonDetailItem>() {
            override fun areItemsTheSame(oldItem: GifticonDetailItem, newItem: GifticonDetailItem): Boolean {
                return oldItem.name == newItem.name
            }

            override fun areContentsTheSame(oldItem: GifticonDetailItem, newItem: GifticonDetailItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}