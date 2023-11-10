package com.example.giftmoa.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.utils.CustomCropTransformation
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.TeamGifticon
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ItemHomeSharedGifticonBinding
import com.example.giftmoa.utils.ImageUtil
import java.text.SimpleDateFormat
import java.util.Date

class HomeSharedGifticonAdapter(private val onClick: (TeamGifticon) -> Unit, private val context: Context): ListAdapter<TeamGifticon, HomeSharedGifticonAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemHomeSharedGifticonBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(gifticon: TeamGifticon) {
            if (gifticon.gifticonImagePath != null) {
                // 자르고 싶은 위치와 크기 지정
                val cropX = 23 // X 시작 위치
                val cropY = 25 // Y 시작 위치
                val cropWidth = 270 // 잘라낼 너비
                val cropHeight = 250 // 잘라낼 높이

                Glide.with(binding.ivCouponImage.context)
                    .asBitmap()
                    .load(gifticon.gifticonImagePath)
                    .apply(RequestOptions().transform(CustomCropTransformation(cropX, cropY, cropWidth, cropHeight)))
                    .into(binding.ivCouponImage)
            } else {
                binding.ivCouponImage.setImageResource(R.drawable.asset_gifticon_coffee)
            }

            binding.tvCouponName.text = gifticon.name
            binding.tvCouponExchangePlace.text = gifticon.exchangePlace

            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
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

           /* val barcode = ImageUtil(context).createBarcode(gifticon..toString().trim())
            binding.ivBarcodeImage.setImageBitmap(barcode)*/


            binding.root.setOnClickListener {
                onClick(gifticon)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHomeSharedGifticonBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<TeamGifticon>() {
            override fun areItemsTheSame(oldItem: TeamGifticon, newItem: TeamGifticon): Boolean {
                return oldItem.gifticonId == newItem.gifticonId
            }

            override fun areContentsTheSame(oldItem: TeamGifticon, newItem: TeamGifticon): Boolean {
                return oldItem == newItem
            }
        }
    }
}