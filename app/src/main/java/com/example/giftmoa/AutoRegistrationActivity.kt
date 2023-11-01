package com.example.giftmoa

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.giftmoa.Adapter.CategoryAdapter
import com.example.giftmoa.Adapter.RegisteredGifticonAdapter
import com.example.giftmoa.Adapter.SmallShareRoomAdapter
import com.example.giftmoa.BottomSheetFragment.GifticonInfoBottomSheet
import com.example.giftmoa.Data.AutoRegistrationData
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.CustomCropTransformation
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.Data.UploadGifticonItem
import com.example.giftmoa.databinding.ActivityAutoRegistrationBinding
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

interface GifticonInfoListener {
    fun onGifticonInfoUpdated(gifticon: ParsedGifticon)
}

class AutoRegistrationActivity: AppCompatActivity(), GifticonInfoListener {

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

        uploadImageToFirebase(Uri.parse(parsedGifticon?.image), {
            imageUrl = it
            Log.d(TAG, "uploadImageToFirebase: $imageUrl")
        }, {
            //Toast.makeText(this, "이미지 업로드에 실패했습니다.", Toast.LENGTH_SHORT).show()
        })

        parsedGifticonAdapter = RegisteredGifticonAdapter()
        categoryAdapter = CategoryAdapter()
        shareRoomAdapter = SmallShareRoomAdapter()

        getJsonData()

        binding.rvShareRoom.apply {
            adapter = shareRoomAdapter
            layoutManager = LinearLayoutManager(this@AutoRegistrationActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.ivAddCategory.setOnClickListener {
            val newCategory = binding.etCategory.text.toString().trim()

            if (newCategory.isNotEmpty()) {
                val chip = createNewChip(newCategory)
                val positionToInsert = binding.chipGroupCategory.childCount - 1
                binding.chipGroupCategory.addView(chip, positionToInsert)
                binding.etCategory.text.clear()
            }
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
            for (gifticon in autoRegistrationData.gifticons) {
                parsedGifticonList.add(gifticon)
            }
            for (category in autoRegistrationData.categories) {
                category.categoryName?.let { createNewChip(it.trim()) }
                categoryList.add(category)
                val chip = category.categoryName?.let { createNewChip(it) }
                val positionToInsert = binding.chipGroupCategory.childCount - 1
                binding.chipGroupCategory.addView(chip, positionToInsert)
                binding.etCategory.text.clear()
            }
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

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, null, false) as Chip
        chip.text = text
        chip.isCloseIconVisible = false
        chip.setOnCloseIconClickListener {
            // 닫기 아이콘 클릭 시 Chip 제거
            (it.parent as? ViewGroup)?.removeView(it)
        }
        return chip
    }

    private fun showGifticonBottomSheet(gifticon: ParsedGifticon) {
        val gifticonInfoBottomSheet = GifticonInfoBottomSheet(gifticon, this)
        gifticonInfoBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        gifticonInfoBottomSheet.show(supportFragmentManager, "gifticonInfoBottomSheet")
    }

    override fun onGifticonInfoUpdated(gifticon: ParsedGifticon) {
        // 바텀시트에서 받은 데이터 처리
        binding.llRegisterCoupon.visibility = ViewGroup.VISIBLE
        binding.llCouponDueDate.visibility = ViewGroup.VISIBLE
        if (binding.llRegisterCoupon.isVisible) {
            binding.tvCouponName.text = gifticon.name
            Glide.with(binding.ivCouponImage.context)
                .load(gifticon.image)
                .transform(CustomCropTransformation(0.5f, 0.5f))
                .into(binding.ivCouponImage)
            isUploading = true
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

        uploadGifticonToServer(gifticon)
    }

    private fun uploadGifticonToServer(gifticon: ParsedGifticon) {
        val inputFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.KOREA)
        val utcTimeZone = TimeZone.getTimeZone("UTC")
        inputFormat.timeZone = utcTimeZone
        outputFormat.timeZone = utcTimeZone

        var formattedDate: String? = null

        try {
            val date = inputFormat.parse(gifticon.dueDate)
            if (date != null) {
                val calendar = Calendar.getInstance(utcTimeZone)
                calendar.time = date
                calendar.add(Calendar.DATE, 1)  // 하루 늘리기

                formattedDate = outputFormat.format(calendar.time)
                // 이제 formattedDate 변수에 변환된 날짜가 저장되어 있음
            } else {
                // 날짜 파싱 실패
                Log.d(TAG, "uploadGifticonToServer: 날짜 파싱 실패")
            }
        } catch (e: Exception) {
            // 날짜 파싱 중 예외가 발생한 경우
            Log.e(TAG, "uploadGifticonToServer: 날짜 파싱 중 예외 발생", e)
        }


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
                if (categoryId != null) {
                    val newGifticon = UploadGifticonItem(
                        name = gifticon.name,
                        barcodeNumber = gifticon.barcodeNumber?.trim(),
                        image = imageUrl,
                        exchangePlace = gifticon.exchangePlace,
                        dueDate = formattedDate,
                        orderNumber = gifticon.orderNumber,
                        gifticonType = gifticonType,
                        categoryId = categoryId,
                        amount = gifticon.amount
                    )
                    Log.d(TAG, "onCreate: $newGifticon")
                    //Toast.makeText(this, "쿠폰이 등록되었습니다.", Toast.LENGTH_SHORT).show()
                    //finish()
                    if (formattedDate != null) {
                        Log.d(TAG, "date: $formattedDate")
                    }
                } else {
                    Snackbar.make(binding.root, "카테고리를 선택해주세요.", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(binding.root, "체크박스를 체크해주세요.", Snackbar.LENGTH_SHORT).show()
            }
        }
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
}