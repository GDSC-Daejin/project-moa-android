package com.example.giftmoa

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.giftmoa.Adapter.CategoryAdapter
import com.example.giftmoa.Adapter.RegisteredGifticonAdapter
import com.example.giftmoa.Adapter.SmallShareRoomAdapter
import com.example.giftmoa.BottomSheetFragment.GifticonInfoBottomSheet
import com.example.giftmoa.Data.AutoRegistrationData
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.databinding.ActivityAutoRegistrationBinding
import com.google.android.material.chip.Chip
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson


class AutoRegistrationActivity: AppCompatActivity() {

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

        // 액티비티 실행되자마자 bottom sheet 띄우기
        showGifticonBottomSheet(parsedGifticon!!)

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

       /* binding.rvCoupon.apply {
            adapter = parsedGifticonAdapter
            layoutManager = LinearLayoutManager(this@AutoRegistrationActivity)
        }
        binding.rvCategory.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(this@AutoRegistrationActivity, LinearLayoutManager.HORIZONTAL, false)
        }*/
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
            if (isChecked) {
                binding.btnConfirm.setBackgroundResource(R.drawable.background_enabled_button)
                binding.btnConfirm.setTextColor(ContextCompat.getColor(this, R.color.moa_gray_white))
            } else {
                binding.btnConfirm.setBackgroundResource(R.drawable.background_disabled_button)
                binding.btnConfirm.setTextColor(ContextCompat.getColor(this, R.color.moa_gray_400))
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
                categoryList.add(category)
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
        val gifticonInfoBottomSheet = GifticonInfoBottomSheet(gifticon)
        gifticonInfoBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        gifticonInfoBottomSheet.show(supportFragmentManager, "gifticonInfoBottomSheet")
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