package com.example.giftmoa.BottomSheetFragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.CouponTab.GifticonInfoListener
import com.example.giftmoa.R
import com.example.giftmoa.databinding.FragmentGifticonInfoBottomSheetBinding
import com.example.giftmoa.utils.CustomCropTransformation
import com.example.giftmoa.utils.FormatUtil
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

        // 자르고 싶은 위치와 크기 지정
        val cropX = 25 // X 시작 위치
        val cropY = 25 // Y 시작 위치
        val cropWidth = 285 // 잘라낼 너비
        val cropHeight = 275 // 잘라낼 높이

        Glide.with(binding.ivCouponImage.context)
            .asBitmap()
            .load(gifticon.image)
            .apply(RequestOptions().transform(CustomCropTransformation(cropX, cropY, cropWidth, cropHeight)))
            .into(binding.ivCouponImage)

        binding.etCouponName.setText(gifticon.name)
        binding.etBarcodeNumber.setText(gifticon.barcodeNumber)
        binding.etExchangePlace.setText(gifticon.exchangePlace)
        binding.etDueDate.setText(formattedDueDate)
        binding.etOrderNumber.setText(gifticon.orderNumber)
        if (gifticon.amount != null) {
            binding.etCouponAmount.visibility = View.VISIBLE
            binding.tvCouponAmountUnit.visibility = View.VISIBLE
            binding.switchCouponAmount.isChecked = true
            binding.etCouponAmount.setText(formattedAmount)
        }

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