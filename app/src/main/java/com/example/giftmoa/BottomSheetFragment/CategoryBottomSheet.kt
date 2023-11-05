package com.example.giftmoa.BottomSheetFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.R
import com.example.giftmoa.databinding.FragmentCategoryBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip

class CategoryBottomSheet(private var categoryList: List<CategoryItem>, private val listener: CategoryListener) : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentCategoryBottomSheetBinding
    private val TAG = "CategoryBottomSheet"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_category_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCategoryBottomSheetBinding.bind(view)

        Log.d(TAG, "onViewCreated: $categoryList")

        val behavior = BottomSheetBehavior.from(view.parent as View)
        val vto = view.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val height = view.height
                behavior.peekHeight = height
                // 레이아웃 리스너를 제거하여 중복 호출을 방지
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        for (category in categoryList) {
            val chip = category.categoryName?.let { createNewChip(it) }
            binding.chipGroupCategory.addView(chip)
        }

        binding.btnConfirm.setOnClickListener {
            val categoryName = binding.etCategory.text.toString()
            listener.onCategoryUpdated(categoryName)
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

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.category_chip_icon_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        chip.setOnCloseIconClickListener {
            // 닫기 아이콘 클릭 시 Chip 제거
            listener.onCategoryDeleted(text)
            (it.parent as? ViewGroup)?.removeView(it)
        }
        return chip
    }
}