package com.example.giftmoa.CouponTab

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.giftmoa.Adapter.CategoryAdapter
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.Data.AddCategoryRequest
import com.example.giftmoa.Data.AddCategoryResponse
import com.example.giftmoa.Data.AddGifticonRequest
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GetCategoryListResponse
import com.example.giftmoa.Data.GetGifticonDetailResponse
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.GifticonDetail
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.UpdateGifticonRequest
import com.example.giftmoa.Data.UpdateGifticonResponse
import com.example.giftmoa.HomeTab.GifticonViewModel
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.ActivityManualRegistrationBinding
import com.example.giftmoa.utils.FormatUtil
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ManualRegistrationActivity : AppCompatActivity(), CategoryListener {

    private val TAG = "ManualRegistrationActivity"
    private lateinit var binding: ActivityManualRegistrationBinding

    private val categoryList = mutableListOf<CategoryItem>()
    private lateinit var categoryAdapter: CategoryAdapter

    private var gifticon: GifticonDetailItem? = null

    //private var gifticonId: Long? = null

    private var isEdit = false

    private var gifticonDetail: GifticonDetail? = null

    private lateinit var gifticonViewModel: GifticonViewModel

    private var gifticonId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: ")

        //gifticonViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[GifticonViewModel::class.java]

        binding = ActivityManualRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*gifticon = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("gifticon", GifticonDetailItem::class.java)
        } else {
            intent.getParcelableExtra<GifticonDetailItem>("gifticon")
        }*/
        isEdit = intent.getBooleanExtra("isEdit", false)
        gifticonId = intent.getLongExtra("gifticonId", 0)

        if (isEdit) {
            binding.tvToolbarTitle.text = "기프티콘 수정"
            binding.btnConfirm.text = "수정"
            binding.viewLoading.visibility = android.view.View.VISIBLE
            binding.ivProgressBar.visibility = android.view.View.VISIBLE
            gifticonId?.let { getGiftionDetailFromServer(it) }
        } else {
            binding.viewLoading.visibility = android.view.View.GONE
            binding.ivProgressBar.visibility = android.view.View.GONE
            binding.switchCouponAmount.isClickable = true
        }
        getCategoryListFromServer()

        categoryAdapter = CategoryAdapter()

        binding.switchCouponAmount.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etCouponAmount.visibility = android.view.View.VISIBLE
                binding.tvCouponAmountUnit.visibility = android.view.View.VISIBLE

                Glide.with(this)
                    .asGif()
                    .load(R.drawable.icon_progress_bar)
                    .into(binding.ivProgressBar)

            } else {
                binding.etCouponAmount.visibility = android.view.View.GONE
                binding.tvCouponAmountUnit.visibility = android.view.View.GONE
            }
        }

        binding.ivAddCategory.setOnClickListener {
            showCategoryBottomSheet(categoryList)
        }

        binding.btnConfirm.setOnClickListener {
            if (gifticonId != 0L && isEdit) {
                gifticonDetail?.let { it1 -> editGifticon(it1) }
            } else {
                addGifticon()
            }
        }
    }

    private fun getGiftionDetailFromServer(gifticonId: Long) {
        Retrofit2Generator.create(this).getGifticonDetail(gifticonId = gifticonId).enqueue(object :
            Callback<GetGifticonDetailResponse> {
            override fun onResponse(call: Call<GetGifticonDetailResponse>, response: Response<GetGifticonDetailResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    if (responseBody != null) {
                        if (responseBody.data != null) {
                            gifticonDetail = responseBody.data.gifticon

                            binding.viewLoading.visibility = android.view.View.GONE
                            binding.ivProgressBar.visibility = android.view.View.GONE

                            binding.etCouponName.setText(gifticonDetail!!.name)
                            binding.etBarcodeNumber.setText(gifticonDetail!!.barcodeNumber.toString())
                            binding.etExchangePlace.setText(gifticonDetail!!.exchangePlace)
                            binding.etDueDate.setText(gifticonDetail!!.dueDate?.let { FormatUtil().DateToString(it) })
                            binding.etOrderNumber.setText(gifticonDetail!!.orderNumber)
                            if (gifticonDetail!!.gifticonType == "MONEY") {
                                binding.etCouponAmount.visibility = android.view.View.VISIBLE
                                binding.tvCouponAmountUnit.visibility = android.view.View.VISIBLE
                                binding.switchCouponAmount.isChecked = true
                                binding.etCouponAmount.setText(gifticonDetail!!.gifticonMoney.toString())
                            }
                            // giftcionDetail의 카테고리 이름을 가져와서 해당 카테고리 chip을 선택
                            for (i in 0 until binding.chipGroupCategory.childCount) {
                                val childView = binding.chipGroupCategory.getChildAt(i)
                                if (childView is Chip) {
                                    val chip = childView as Chip
                                    if (chip.text == gifticonDetail!!.category?.categoryName) {
                                        chip.isChecked = true
                                        break
                                    }
                                }
                            }
                        }
                    }


                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonDetailResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun getCategoryListFromServer() {
        Retrofit2Generator.create(this).getCategoryList().enqueue(object : Callback<GetCategoryListResponse> {
            override fun onResponse(call: Call<GetCategoryListResponse>, response: Response<GetCategoryListResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val resposeBody = responseBody.data

                        Log.d(TAG, "categories: $resposeBody")
                        if (resposeBody != null) {
                            for (category in resposeBody) {
                                if (category.categoryName == null) continue
                                categoryList.add(category)
                                val chip = category.categoryName?.let { createNewChip(it) }
                                val positionToInsert = binding.chipGroupCategory.childCount - 1
                                binding.chipGroupCategory.addView(chip, positionToInsert)
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetCategoryListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun editGifticon(gifticon: GifticonDetail) {
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

        val gifticonType = if (binding.switchCouponAmount.isChecked) "MONEY" else "GENERAL"

        val updatedGifticon = UpdateGifticonRequest(
            id = gifticon.gifticonId,
            name = binding.etCouponName.text.toString(),
            barcodeNumber = binding.etBarcodeNumber.text.toString(),
            gifticonImagePath = gifticon.gifticonImagePath,
            exchangePlace = binding.etExchangePlace.text.toString(),
            dueDate = FormatUtil().StringToDate(binding.etDueDate.text.toString(), TAG),
            orderNumber = binding.etOrderNumber.text.toString(),
            gifticonType = gifticonType,
            gifticonMoney = if (binding.switchCouponAmount.isChecked) binding.etCouponAmount.text.toString() else null,
            categoryId = categoryId,
        )

        Log.d(TAG, "editGifticon: $updatedGifticon")

        /*Log.d(TAG, "editGifticon: $updatedGifticon")

        val data = Intent().apply {
            putExtra("updatedGifticon", updatedGifticon)
            putExtra("isEdit", isEdit)
        }
        setResult(RESULT_OK, data)

        finish()*/
        updateGifticonToServer(updatedGifticon)
    }

    private fun updateGifticonToServer(gifticon: UpdateGifticonRequest) {
        Retrofit2Generator.create(this).updateGifticon(gifticon).enqueue(object :
            Callback<UpdateGifticonResponse> {
            override fun onResponse(call: Call<UpdateGifticonResponse>, response: Response<UpdateGifticonResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    Log.d(TAG, "responseBody gifticon: $responseBody")

                    val coupon = Gifticon(
                        id = responseBody?.data?.gifticonId,
                        name = responseBody?.data?.name,
                        gifticonImagePath = responseBody?.data?.gifticonImagePath,
                        exchangePlace = responseBody?.data?.exchangePlace,
                        dueDate = responseBody?.data?.dueDate,
                        gifticonType = responseBody?.data?.gifticonType,
                        status = responseBody?.data?.status,
                        usedDate = responseBody?.data?.usedDate,
                        author = responseBody?.data?.author,
                        category = responseBody?.data?.category,
                    )

                    val data = Intent().apply {
                        putExtra("updatedGifticon", coupon)
                        putExtra("isEdit", isEdit)
                    }

                    setResult(RESULT_OK, data)

                    finish()
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UpdateGifticonResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun addGifticon() {
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

        val updatedGifticon = AddGifticonRequest(
            name = binding.etCouponName.text.toString(),
            barcodeNumber = binding.etBarcodeNumber.text.toString(),
            exchangePlace = binding.etExchangePlace.text.toString(),
            dueDate = FormatUtil().StringToDate(binding.etDueDate.text.toString(), TAG),
            orderNumber = binding.etOrderNumber.text.toString(),
            gifticonType = if (binding.switchCouponAmount.isChecked) "MONEY" else "GENERAL",
            gifticonMoney = if (binding.switchCouponAmount.isChecked) binding.etCouponAmount.text.toString() else null,
            categoryId = categoryId,
        )

        Log.d(TAG, "registerGifticon: $updatedGifticon")

        /*val data = Intent().apply {
            putExtra("updatedGifticon", updatedGifticon)
            putExtra("isEdit", isEdit)
        }
        setResult(RESULT_OK, data)

        finish()*/
        uploadGifticonToServer(updatedGifticon)
    }

    private fun uploadGifticonToServer(gifticon: AddGifticonRequest) {
        Retrofit2Generator.create(this).addGifticon(gifticon).enqueue(object :
            Callback<UpdateGifticonResponse> {
            override fun onResponse(call: Call<UpdateGifticonResponse>, response: Response<UpdateGifticonResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    Log.d(TAG, "responseBody gifticon: $responseBody")

                    val coupon = Gifticon(
                        id = responseBody?.data?.gifticonId,
                        name = responseBody?.data?.name,
                        gifticonImagePath = responseBody?.data?.gifticonImagePath,
                        exchangePlace = responseBody?.data?.exchangePlace,
                        dueDate = responseBody?.data?.dueDate,
                        gifticonType = responseBody?.data?.gifticonType,
                        status = responseBody?.data?.status,
                        usedDate = responseBody?.data?.usedDate,
                        author = responseBody?.data?.author,
                        category = responseBody?.data?.category,
                    )

                    val data = Intent().apply {
                        putExtra("updatedGifticon", coupon)
                        putExtra("isEdit", isEdit)
                    }
                    setResult(RESULT_OK, data)

                    finish()

                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<UpdateGifticonResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }


    /*private fun getJsonData() {
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
    }*/

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

    private fun deleteChip(text: String) {
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val childView = binding.chipGroupCategory.getChildAt(i)
            if (childView is Chip) {
                val chip = childView as Chip
                if (chip.text == text) {
                    binding.chipGroupCategory.removeView(chip)
                    break
                }
            }
        }
    }

    private fun showCategoryBottomSheet(cateogoryList: List<CategoryItem>) {
        val categoryBottomSheet = CategoryBottomSheet(categoryList, this)
        categoryBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        categoryBottomSheet.show(this.supportFragmentManager, categoryBottomSheet.tag)
    }

    override fun onCategoryUpdated(categoryName: String) {
        val categoryRequest = AddCategoryRequest(categoryName)
        // Retrofit을 이용해서 서버에 카테고리 추가 요청
        // 서버에서 카테고리 추가가 완료되면 아래 코드를 실행
        Retrofit2Generator.create(this).addCategory(categoryRequest).enqueue(object : Callback<AddCategoryResponse> {
            override fun onResponse(call: Call<AddCategoryResponse>, response: Response<AddCategoryResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    Log.d(TAG, "responseBody category: $responseBody")
                    if (responseBody != null) {
                        val category = responseBody.data
                        // categoryList에 추가
                        if (category != null) {
                            val chip = category.categoryName?.let { createNewChip(it) }
                            val positionToInsert = binding.chipGroupCategory.childCount - 1
                            binding.chipGroupCategory.addView(chip, positionToInsert)
                            categoryList.add(category)
                        }
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AddCategoryResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    override fun onCategoryDeleted(categoryName: String) {
        var categoryId: Long? = null
        Log.d(TAG, "onCategoryDeleted: $categoryName")
        for (category in categoryList) {
            if (categoryName == category.categoryName) {
                // categoryList에서 해당 카테고리의 id 값 가져오기
                categoryId = category.id
                // categoryList에서 해당 카테고리 삭제
                categoryList.remove(category)
                deleteChip(categoryName)
                Log.d(TAG, "onCategoryDeleted: $categoryId")
                Log.d(TAG, "onCategoryDeleted: $categoryList")
                break
            }
        }
    }

    override fun onStart() {
        super.onStart()

    }

    /*private fun editGifticon2(gifticon: GifticonDetailItem): GifticonDetailItem {
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

        return updatedGifticon
    }*/

    /*private fun registerGifticon2(): GifticonDetailItem {
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

        return updatedGifticon
    }*/
}