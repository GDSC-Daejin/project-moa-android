package com.example.giftmoa

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.giftmoa.Adapter.CategoryAdapter
import com.example.giftmoa.Data.Author
import com.example.giftmoa.Data.AutoRegistrationData
import com.example.giftmoa.Data.Category
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.Data.UpdateGifticonItem
import com.example.giftmoa.Data.UploadGifticonItem
import com.example.giftmoa.databinding.ActivityManualRegistrationBinding
import com.example.giftmoa.util.FormatUtil
import com.google.android.material.chip.Chip
import com.google.gson.Gson

class ManualRegistrationActivity : AppCompatActivity() {

    private val TAG = "ManualRegistrationActivity"
    private lateinit var binding: ActivityManualRegistrationBinding

    private val categoryList = mutableListOf<CategoryItem>()
    private lateinit var categoryAdapter: CategoryAdapter

    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManualRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gifticon = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("gifticon", GifticonDetailItem::class.java)
        } else {
            intent.getParcelableExtra<GifticonDetailItem>("gifticon")
        }
        isEdit = intent.getBooleanExtra("isEdit", false)

        if (gifticon != null && isEdit) {
            binding.tvManualRegistrationTitle.text = "기프티콘 수정"
            binding.btnConfirm.text = "수정"

            binding.etCouponName.setText(gifticon.name)
            binding.etBarcodeNumber.setText(gifticon.barcodeNumber.toString())
            binding.etExchangePlace.setText(gifticon.exchangePlace)
            binding.etDueDate.setText(gifticon.dueDate?.let { FormatUtil().DateToString(it) })
            if (gifticon.gifticonType == "MONEY") {
                binding.etCouponAmount.visibility = android.view.View.VISIBLE
                binding.tvCouponAmountUnit.visibility = android.view.View.VISIBLE
                binding.switchCouponAmount.isChecked = true
                binding.etCouponAmount.setText(gifticon.amount.toString())
            }
        } else {
            binding.switchCouponAmount.isClickable = true
        }

        categoryAdapter = CategoryAdapter()

        getJsonData()


        binding.switchCouponAmount.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etCouponAmount.visibility = android.view.View.VISIBLE
                binding.tvCouponAmountUnit.visibility = android.view.View.VISIBLE
            } else {
                binding.etCouponAmount.visibility = android.view.View.GONE
                binding.tvCouponAmountUnit.visibility = android.view.View.GONE
            }
        }

        binding.btnConfirm.setOnClickListener {
            if (gifticon != null && isEdit) {
                editGifticon(gifticon)
            } else {
                registerGifticon()
            }
        }
    }

    private fun editGifticon(gifticon: GifticonDetailItem) {
        // 클릭된 카테고리 chip의 text를 가져옴
        val selectedCategory = binding.chipGroupCategory.findViewById<Chip>(binding.chipGroupCategory.checkedChipId)
        val categoryName = selectedCategory?.text

        var categoryId: Long? = null

        // categoryList에 있는 카테고리 이름과 같은 카테고리를 찾아서 categoryId를 가져옴
        for (category in categoryList) {
            if (categoryName == category.categoryName) {
                categoryId = category.id
            }
        }

        val updatedGifticon = GifticonDetailItem(
            id = gifticon.id,
            name = binding.etCouponName.text.toString(),
            barcodeNumber = binding.etBarcodeNumber.text.toString(),
            exchangePlace = binding.etExchangePlace.text.toString(),
            dueDate = gifticon.dueDate,
            category = Category(id = categoryId, categoryName = categoryName.toString()),
            amount = if (binding.switchCouponAmount.isChecked) binding.etCouponAmount.text.toString().toLong() else null,
        )

        Log.d(TAG, "editGifticon: $updatedGifticon")

        val data = Intent().apply {
            putExtra("updatedGifticon", updatedGifticon)
            putExtra("isEdit", isEdit)
        }
        setResult(RESULT_OK, data)

        finish()
    }

    private fun registerGifticon() {
        // 클릭된 카테고리 chip의 text를 가져옴
        val selectedCategory = binding.chipGroupCategory.findViewById<Chip>(binding.chipGroupCategory.checkedChipId)
        val categoryName = selectedCategory?.text

        var categoryId: Long? = null

        // categoryList에 있는 카테고리 이름과 같은 카테고리를 찾아서 categoryId를 가져옴
        for (category in categoryList) {
            if (categoryName == category.categoryName) {
                categoryId = category.id
            }
        }

        val updatedGifticon = GifticonDetailItem(
            name = binding.etCouponName.text.toString(),
            barcodeNumber = binding.etBarcodeNumber.text.toString(),
            exchangePlace = binding.etExchangePlace.text.toString(),
            dueDate = FormatUtil().StringToDate(binding.etDueDate.text.toString(), TAG),
            gifticonType = if (binding.switchCouponAmount.isChecked) "MONEY" else "GENERAL",
            orderNumber = binding.etOrderNumber.text.toString(),
            category = Category(id = categoryId, categoryName = categoryName.toString()),
            amount = if (binding.switchCouponAmount.isChecked) binding.etCouponAmount.text.toString().toLong() else null,
        )

        Log.d(TAG, "registerGifticon: $updatedGifticon")

        val data = Intent().apply {
            putExtra("updatedGifticon", updatedGifticon)
            putExtra("isEdit", isEdit)
        }
        setResult(RESULT_OK, data)

        finish()
    }

    private fun getJsonData() {
        val assetLoader = AssetLoader()
        val autoRegistrationJsonString = assetLoader.getJsonString(this, "autoRegistration.json")
        //Log.d(TAG, autoRegistrationJsonString ?: "null")

        if (!autoRegistrationJsonString.isNullOrEmpty()) {
            val gson = Gson()
            val autoRegistrationData = gson.fromJson(autoRegistrationJsonString, AutoRegistrationData::class.java)

            for (category in autoRegistrationData.categories) {
                category.categoryName?.let { createNewChip(it.trim()) }
                categoryList.add(category)
                val chip = category.categoryName?.let { createNewChip(it) }
                val positionToInsert = binding.chipGroupCategory.childCount - 1
                binding.chipGroupCategory.addView(chip, positionToInsert)
            }

            categoryAdapter.submitList(categoryList)
        }
    }

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        chip.setOnCloseIconClickListener {
            // 닫기 아이콘 클릭 시 Chip 제거
            (it.parent as? ViewGroup)?.removeView(it)
        }
        return chip
    }
}