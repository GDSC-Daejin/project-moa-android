package com.example.giftmoa

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.MyProfileData
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.Data.UpdateUserRequest
import com.example.giftmoa.Data.UpdateUserResponse
import com.example.giftmoa.databinding.ActivityMyProfileBinding
import com.example.giftmoa.utils.FileGalleryPermissionUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyProfileActivity: AppCompatActivity() {

    private val TAG = "MyProfileActivity"
    private lateinit var binding: ActivityMyProfileBinding

    private val imageLoadLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.civUserProfileImage)
            Toast.makeText(this, "이미지 로드 성공", Toast.LENGTH_SHORT).show()
        } ?: kotlin.run {
            Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val nickname = intent.getStringExtra("nickname")
        val profileImageUrl = intent.getStringExtra("profileImageUrl")

        binding.etUserName.setText(nickname)
        Glide.with(this)
            .load(profileImageUrl)
            .circleCrop()
            .into(binding.civUserProfileImage)


        binding.ivEditProfileImage.setOnClickListener {
            FileGalleryPermissionUtil().checkPermission(this, imageLoadLauncher)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            // editText에 있는 값 가져오기
            val userName = binding.etUserName.text.toString()

            // userName을 UpdateUserRequest에 담아서 서버로 보내기
            val updateUserRequest = UpdateUserRequest(userName)
            updateMyProfile(updateUserRequest)
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }

    private fun updateMyProfile(userName: UpdateUserRequest) {
        Retrofit2Generator.create(this).updateUser(userName).enqueue(object :
            Callback<UpdateUserResponse> {
            override fun onResponse(
                call: Call<UpdateUserResponse>,
                response: Response<UpdateUserResponse>
            ) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: ${response.body()}")
                    Toast.makeText(this@MyProfileActivity, "프로필 수정 성공", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onFailure(call: Call<UpdateUserResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }
}