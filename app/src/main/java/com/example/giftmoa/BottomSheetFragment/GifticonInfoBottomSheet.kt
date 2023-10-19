package com.example.giftmoa.BottomSheetFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.CustomCropTransformation
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.R
import com.example.giftmoa.databinding.LayoutGifticonInfoBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class GifticonInfoBottomSheet(gifticon: ParsedGifticon) : BottomSheetDialogFragment() {
    private var gifticon = gifticon

    private val TAG = "GifticonInfoBottomSheet"
    private lateinit var binding: LayoutGifticonInfoBottomSheetBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_gifticon_info_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = LayoutGifticonInfoBottomSheetBinding.bind(view)

        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)

        var formattedDueDate: String? = null
        if (gifticon.dueDate?.contains("년") == true) {
            val regex = Regex("(\\d+\\s*년)\\s*(\\d+\\s*월)\\s*(\\d+\\s*일)")
            val matchResult = gifticon.dueDate?.let { regex.find(it) }

            formattedDueDate = if (matchResult != null) {
                val year = matchResult.groups[1]?.value?.trim() ?: ""
                val month = matchResult.groups[2]?.value?.trim() ?: ""
                val day = matchResult.groups[3]?.value?.trim() ?: ""

                "$year $month $day"
            } else {
                gifticon.dueDate
            }
        } else {
            // 2021.12.31 -> 2021년 12월 31일
            val regex = Regex("(\\d+)\\.(\\d+)\\.(\\d+)")
            val matchResult = gifticon.dueDate?.let { regex.find(it) }

            formattedDueDate = if (matchResult != null) {
                val year = matchResult.groups[1]?.value?.trim() ?: ""
                val month = matchResult.groups[2]?.value?.trim() ?: ""
                val day = matchResult.groups[3]?.value?.trim() ?: ""

                "${year}년 ${month}월 ${day}일"
            } else {
                gifticon.dueDate
            }
        }

        Glide.with(binding.ivGifticon.context)
            .load(gifticon.image)
            .transform(CustomCropTransformation(0.5f, 0.5f))
            .into(binding.ivGifticon)
        binding.etCouponName.setText(gifticon.name)
        binding.etBarcodeNumber.setText(gifticon.barcodeNumber)
        binding.etExchangePlace.setText(gifticon.exchangePlace)
        binding.etDueDate.setText("$formattedDueDate   ${gifticon.amount}원")

        binding.btnConfirm.setOnClickListener {
            dismiss()
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        // 바깥부분 클릭시 닫힘
        binding.root.setOnClickListener {
            dismiss()
        }
    }
}