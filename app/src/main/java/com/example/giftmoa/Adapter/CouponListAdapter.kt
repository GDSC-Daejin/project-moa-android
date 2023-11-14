package com.example.giftmoa.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ItemGifticonBinding
import com.example.giftmoa.utils.CustomCropTransformation
import java.text.SimpleDateFormat
import java.util.Date

class CouponListAdapter(private val onClick: (Gifticon) -> Unit, var Coupons: List<Gifticon>) :
    RecyclerView.Adapter<CouponListAdapter.ViewHolder>() {

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

        fun bind(gifticon: Gifticon) {
            if (gifticon.gifticonImagePath != null) {
                // 자르고 싶은 위치와 크기 지정
                val cropX = 30 // X 시작 위치
                val cropY = 30 // Y 시작 위치
                val cropWidth = 415 // 잘라낼 너비
                val cropHeight = 390 // 잘라낼 높이

                Glide.with(binding.ivCouponImage.context)
                    .load(gifticon.gifticonImagePath)
                    .apply(RequestOptions().transform(CustomCropTransformation(cropX, cropY, cropWidth, cropHeight)))
                    .into(binding.ivCouponImage)

            } else {
                binding.ivCouponImage.setPadding(100, 100, 100, 100)
                binding.ivCouponImage.setBackgroundColor(binding.ivCouponImage.context.getColor(R.color.moa_gray_200))
                binding.ivCouponImage.setImageResource(R.drawable.icon_logo)
            }

            binding.tvCouponName.text = gifticon.name
            binding.tvCouponExchangePlace.text = gifticon.exchangePlace

            if (gifticon.status == "AVAILABLE") {
                binding.viewAlpha.visibility = android.view.View.GONE
                binding.tvCouponUsedComplete.visibility = android.view.View.GONE
            } else {
                binding.viewAlpha.visibility = android.view.View.VISIBLE
                binding.tvCouponUsedComplete.visibility = android.view.View.VISIBLE
            }

            try {
                //2024-01-26T00:00:00.000+00:00 -> inputFormat
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                val dueDate = inputFormat.parse(gifticon.dueDate)

                val currentDate = Date()
                val timeDifference = dueDate.time - currentDate.time
                val daysDifference = timeDifference / (1000 * 60 * 60 * 24)

                if (daysDifference >= 0) {
                    val dDayText = "D-${daysDifference}"
                    if (daysDifference == 0L)
                        binding.tvDDay.text = "D-Day"
                    else
                        binding.tvDDay.text = dDayText

                } else {
                    binding.tvDDay.text = "만료"
                    binding.viewAlpha.visibility = android.view.View.VISIBLE
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
        return ViewHolder(
            ItemGifticonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(Coupons[position])
    }

    override fun getItemCount(): Int {
        return Coupons.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setAllCouponsData(Coupons: List<Gifticon>) {
        this.Coupons = Coupons
        notifyDataSetChanged()
    }

    fun setAvailableCouponsData(Coupons: List<Gifticon>) {
        this.Coupons = Coupons
        notifyDataSetChanged()
    }

    fun setUsedCouponsData(Coupons: List<Gifticon>) {
        this.Coupons = Coupons
        notifyDataSetChanged()
    }
}