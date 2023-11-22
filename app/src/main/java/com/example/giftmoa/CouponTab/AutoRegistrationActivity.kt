package com.example.giftmoa.CouponTab

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.Adapter.CategoryAdapter
import com.example.giftmoa.Adapter.RegisteredGifticonAdapter
import com.example.giftmoa.Adapter.SmallShareRoomAdapter
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BottomSheetFragment.GifticonInfoBottomSheet
import com.example.giftmoa.Data.AddCategoryRequest
import com.example.giftmoa.Data.AddCategoryResponse
import com.example.giftmoa.Data.AddGifticonRequest
import com.example.giftmoa.Data.AutoRegistrationData
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GetCategoryListResponse
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.Data.UpdateGifticonResponse
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.ActivityAutoRegistrationBinding
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.utils.CustomCropTransformation
import com.example.giftmoa.utils.FormatUtil
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

interface GifticonInfoListener {
    fun onGifticonInfoUpdated(gifticon: ParsedGifticon)
}

class AutoRegistrationActivity: AppCompatActivity(), GifticonInfoListener, CategoryListener {

    private val TAG = "AutoRegistrationActivity"
    private lateinit var binding: ActivityAutoRegistrationBinding

    private lateinit var parsedGifticonAdapter: RegisteredGifticonAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var shareRoomAdapter: SmallShareRoomAdapter

    private var parsedGifticonList = mutableListOf<ParsedGifticon>()
    private var categoryList = mutableListOf<CategoryItem>()
    private var shareRoomList = mutableListOf<ShareRoomItem>()

    private var isUploading = false
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAutoRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val parsedGifticon = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("PARSED_GIFTICON", ParsedGifticon::class.java)
        } else {
            intent.getParcelableExtra<ParsedGifticon>("PARSED_GIFTICON")
        }

        Log.d(TAG, parsedGifticon.toString())

        parsedGifticon?.let {
            showGifticonBottomSheet(it)
        }

        /*uploadImageToFirebase(Uri.parse(parsedGifticon?.image), {
            imageUrl = it
            Log.d(TAG, "uploadImageToFirebase: $imageUrl")
        }, {
            //Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        })*/

        if (parsedGifticon != null) {
            if (parsedGifticon.platform == "toss") {
                uploadCroppedImageToFirebase(this, Uri.parse(parsedGifticon?.image), 335, 210, 314, 314, {
                    imageUrl = it
                    //Toast.makeText(this, "이미지 업로드에 성공했습니다.", Toast.LENGTH_SHORT).show()
                }, {
                    //Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                })
            } else {
                uploadCroppedImageToFirebase(this, Uri.parse(parsedGifticon.image), 50, 60, 700, 650, {
                    imageUrl = it
                    //Toast.makeText(this, "이미지 업로드에 성공했습니다.", Toast.LENGTH_SHORT).show()
                }, {
                    //Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
                })
            }
        }

        /*uploadCroppedImageToFirebase(this, Uri.parse(parsedGifticon?.image), 50, 60, 700, 650, {
            imageUrl = it
            Toast.makeText(this, "이미지 업로드에 성공했습니다.", Toast.LENGTH_SHORT).show()
        }, {
            //Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        })*/

        parsedGifticonAdapter = RegisteredGifticonAdapter()
        categoryAdapter = CategoryAdapter()
        shareRoomAdapter = SmallShareRoomAdapter()

        getJsonData()

        getCategoryListFromServer()

        binding.rvShareRoom.apply {
            adapter = shareRoomAdapter
            layoutManager = LinearLayoutManager(this@AutoRegistrationActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.ivAddCategory.setOnClickListener {
            showCategoryBottomSheet(categoryList)
        }

        // 체크박스 클릭 시
        binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
            isUploading = if (isChecked) {
                binding.btnConfirm.setBackgroundResource(R.drawable.background_enabled_button)
                binding.btnConfirm.setTextColor(ContextCompat.getColor(this, R.color.moa_gray_white))
                false
            } else {
                binding.btnConfirm.setBackgroundResource(R.drawable.background_disabled_button)
                binding.btnConfirm.setTextColor(ContextCompat.getColor(this, R.color.moa_gray_400))
                true
            }
        }
        // 스위치 버튼 클릭 시
        binding.switchCouponAmount.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.etCouponAmount.visibility = ViewGroup.VISIBLE
                binding.tvCouponAmountUnit.visibility = ViewGroup.VISIBLE
            } else {
                binding.etCouponAmount.visibility = ViewGroup.INVISIBLE
                binding.tvCouponAmountUnit.visibility = ViewGroup.INVISIBLE
            }
        }

    }

    private fun getJsonData() {
        val assetLoader = AssetLoader()
        val autoRegistrationJsonString = assetLoader.getJsonString(this, "autoRegistration.json")
        //Log.d(TAG, autoRegistrationJsonString ?: "null")

        if (!autoRegistrationJsonString.isNullOrEmpty()) {
            val gson = Gson()
            val autoRegistrationData = gson.fromJson(autoRegistrationJsonString, AutoRegistrationData::class.java)

            //Log.d(TAG, autoRegistrationData.toString())
            /*for (gifticon in autoRegistrationData.gifticons) {
                parsedGifticonList.add(gifticon)
            }*/
            /*for (category in autoRegistrationData.categories) {
                category.categoryName?.let { createNewChip(it.trim()) }
                categoryList.add(category)
                val chip = category.categoryName?.let { createNewChip(it) }
                val positionToInsert = binding.chipGroupCategory.childCount - 1
                binding.chipGroupCategory.addView(chip, positionToInsert)
            }*/
            // autoRegistrationData.shareRooms가 null이 아닐 때 실행
            if (autoRegistrationData.shareRooms.size > 0) {
                binding.tvShareRoom.visibility = ViewGroup.VISIBLE
                binding.svShareRoom.visibility = ViewGroup.VISIBLE
                for (shareRoom in autoRegistrationData.shareRooms) {
                    shareRoomList.add(shareRoom)
                }
            }
            // 어댑터에 데이터 전달
            parsedGifticonAdapter.submitList(parsedGifticonList)
            categoryAdapter.submitList(categoryList)
            shareRoomAdapter.submitList(shareRoomList)
        }
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

                                if (category.categoryName == "미분류") {
                                    continue
                                } else {
                                    // categoryList에 추가
                                    val chip = category.categoryName?.let { createNewChip(it) }
                                    val positionToInsert = binding.chipGroupCategory.childCount - 1
                                    binding.chipGroupCategory.addView(chip, positionToInsert)
                                }
                                /*val chip = category.categoryName?.let { createNewChip(it) }
                                val positionToInsert = binding.chipGroupCategory.childCount - 1
                                binding.chipGroupCategory.addView(chip, positionToInsert)*/
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

    private fun showGifticonBottomSheet(gifticon: ParsedGifticon) {
        val gifticonInfoBottomSheet = GifticonInfoBottomSheet(gifticon, this)
        gifticonInfoBottomSheet.setStyle(DialogFragment.STYLE_NORMAL,
            R.style.RoundCornerBottomSheetDialogTheme
        )
        gifticonInfoBottomSheet.show(supportFragmentManager, "gifticonInfoBottomSheet")
    }

    override fun onGifticonInfoUpdated(gifticon: ParsedGifticon) {

        Log.d(TAG, "onGifticonInfoUpdated: $gifticon")

        // 바텀시트에서 받은 데이터 처리
        binding.llRegisterCoupon.visibility = ViewGroup.VISIBLE
        binding.llCouponDueDate.visibility = ViewGroup.VISIBLE
        if (binding.llRegisterCoupon.isVisible) {
            binding.tvCouponName.text = gifticon.name
            // 자르고 싶은 위치와 크기 지정
            val cropX = 5 // X 시작 위치
            val cropY = 10 // Y 시작 위치
            val cropWidth = 80 // 잘라낼 너비
            val cropHeight = 75 // 잘라낼 높이

            Glide.with(binding.ivCouponImage.context)
                .asBitmap()
                .load(gifticon.image)
                .apply(RequestOptions().transform(CustomCropTransformation(cropX, cropY, cropWidth, cropHeight)))
                .into(binding.ivCouponImage)
        }
        if (binding.llCouponDueDate.isVisible) {
            binding.tvCouponDueDate.text = gifticon.dueDate
        }
        val formattedAmount = gifticon.amount?.let { String.format("%,d", it) }
        if (gifticon.amount != null) {
            binding.etCouponAmount.visibility = ViewGroup.VISIBLE
            binding.switchCouponAmount.isChecked = true
            binding.etCouponAmount.setText(formattedAmount)
        } else {
            binding.switchCouponAmount.isChecked = false
        }

        uploadGifticon(gifticon)
    }

    private fun uploadGifticon(gifticon: ParsedGifticon) {
        val formattedDate = FormatUtil().StringToDate(gifticon.dueDate.toString(), TAG)

        binding.btnConfirm.setOnClickListener {
            val gifticonType = if (gifticon.amount == null) {
                "GENERAL"
            } else {
                "MONEY"
            }

            Log.d(TAG, "gifticon.amount: $gifticon.amount")

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

            if (!isUploading) {
                val newGifticon = AddGifticonRequest(
                    name = gifticon.name,
                    barcodeNumber = gifticon.barcodeNumber,
                    gifticonImagePath = imageUrl,
                    exchangePlace = gifticon.exchangePlace,
                    dueDate = formattedDate,
                    orderNumber = gifticon.orderNumber,
                    gifticonType = gifticonType,
                    gifticonMoney = gifticon.amount.toString(),
                    categoryId = categoryId
                )
                uploadGifticonToServer(newGifticon)
            } else {
                Snackbar.make(binding.root, "체크박스를 체크해주세요.", Snackbar.LENGTH_SHORT).show()
            }
        }
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
                        category = responseBody?.data?.category
                    )

                    Log.i(TAG, "coupon: $coupon")

                    val data = Intent().apply {
                        putExtra("uploadedGifticon", coupon)
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

    fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child("images/${System.currentTimeMillis()}_image.jpeg")
        storageReference.putFile(uri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                    //Toast.makeText(this, downloadUri.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }

    fun uploadCroppedImageToFirebase(
        context: Context,
        originalUri: Uri,
        cropX: Int,
        cropY: Int,
        cropWidth: Int,
        cropHeight: Int,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            // 이미지 로드 및 크롭
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, originalUri)
            val croppedBitmap = CustomCropTransformation(cropX, cropY, cropWidth, cropHeight).transform(
                Glide.get(context).bitmapPool, bitmap, bitmap.width, bitmap.height
            )

            // 임시 파일 생성
            val tempFile = File(context.cacheDir, "cropped_${System.currentTimeMillis()}_image.jpeg")
            FileOutputStream(tempFile).use { out ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out) // PNG, JPEG 등 원하는 형식으로 변경 가능
            }

            // Firebase에 임시 파일 업로드
            val storageReference = FirebaseStorage.getInstance().reference.child("images/${tempFile.name}")
            storageReference.putFile(Uri.fromFile(tempFile))
                .addOnSuccessListener {
                    storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                        onSuccess(downloadUri.toString())
                        Log.d(TAG, "downloadUri2: $downloadUri")
                    }
                }
                .addOnFailureListener {
                    onFailure(it)
                }
        } catch (e: Exception) {
            onFailure(e)
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
        Retrofit2Generator.create(this).addCategory(categoryRequest).enqueue(object :
            Callback<AddCategoryResponse> {
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
                break
            }
        }
    }
}