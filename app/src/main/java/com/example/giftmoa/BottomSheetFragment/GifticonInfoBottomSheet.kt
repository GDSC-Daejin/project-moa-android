package com.example.giftmoa.BottomSheetFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.CustomCropTransformation
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.GifticonInfoListener
import com.example.giftmoa.R
import com.example.giftmoa.databinding.FragmentGifticonInfoBottomSheetBinding
import com.example.giftmoa.util.FormatUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
class GifticonInfoBottomSheet(private var gifticon: ParsedGifticon, private val listener: GifticonInfoListener) : BottomSheetDialogFragment() {
    private val TAG = "GifticonInfoBottomSheet"
    private lateinit var binding: FragmentGifticonInfoBottomSheetBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_gifticon_info_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGifticonInfoBottomSheetBinding.bind(view)

        val behavior = BottomSheetBehavior.from(view.parent as View)
        behavior.peekHeight = resources.getDimensionPixelSize(R.dimen.bottom_sheet_peek_height)

        val formattedDueDate = gifticon.dueDate?.let { FormatUtil().ParsedDateToString(it) }

        // gifticon.amount 50000 -> 50,000
        val formattedAmount = gifticon.amount?.let { String.format("%,d", it) }

        Glide.with(binding.ivGifticon.context)
            .load(gifticon.image)
            .transform(CustomCropTransformation(0.5f, 0.5f))
            .into(binding.ivGifticon)
        binding.etCouponName.setText(gifticon.name)
        binding.etBarcodeNumber.setText(gifticon.barcodeNumber)
        binding.etExchangePlace.setText(gifticon.exchangePlace)
        binding.etDueDate.setText("$formattedDueDate   ${formattedAmount}원")

        binding.btnConfirm.setOnClickListener {
            // 쿠폰 정보 업데이트
            val updatedGifticon = gifticon.copy(
                name = binding.etCouponName.text.toString(),
                barcodeNumber = binding.etBarcodeNumber.text.toString(),
                dueDate = formattedDueDate,
            )
            listener.onGifticonInfoUpdated(updatedGifticon)
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