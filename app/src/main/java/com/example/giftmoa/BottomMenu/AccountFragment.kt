package com.example.giftmoa.BottomMenu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.GetMyProfileResponse
import com.example.giftmoa.Data.LogoutUserResponse
import com.example.giftmoa.Data.MyProfileData
import com.example.giftmoa.Data.RefreshTokenRequest
import com.example.giftmoa.Data.UpdateUserResponse
import com.example.giftmoa.HomeTab.GifticonViewModel
import com.example.giftmoa.Login2Activity
import com.example.giftmoa.LoginActivity
import com.example.giftmoa.MyProfileActivity
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.FragmentAccountBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentAccountBinding
    private val TAG = "AccountFragment"

    private var myProfile: MyProfileData? = null

    private lateinit var gifticonViewModel: GifticonViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gifticonViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory())[GifticonViewModel::class.java]

        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false)

        // SharedPreferences 에서 토큰 정보 가져오기
        val sharedPref = requireActivity().getSharedPreferences("TokenData", Context.MODE_PRIVATE)
        val refreshToken = sharedPref.getString("refreshToken", null) // 기본값은 null

        binding.tvUserName.setOnClickListener {
            val intent = Intent(context, MyProfileActivity::class.java)
            intent.putExtra("nickname", myProfile?.nickname)
            intent.putExtra("profileImageUrl", myProfile?.profileImageUrl)
            startActivity(intent)
        }
        // 스위치 버튼이 클릭되어 있으면
        if (binding.switchCouponAmount.isChecked) {
            binding.tvPushNotificationDate.setTextColor(ContextCompat.getColor(requireActivity(), R.color.moa_gray_800))
            binding.tvPushNotificationTime.setTextColor(ContextCompat.getColor(requireActivity(), R.color.moa_gray_800))
        } else {
            binding.tvPushNotificationDate.setTextColor(ContextCompat.getColor(requireActivity(), R.color.moa_gray_400))
            binding.tvPushNotificationTime.setTextColor(ContextCompat.getColor(requireActivity(), R.color.moa_gray_400))
        }

        // 스위치 버튼 클릭 시
        binding.switchCouponAmount.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvPushNotificationDate.setTextColor(ContextCompat.getColor(requireActivity(), R.color.moa_gray_800))
                binding.tvPushNotificationTime.setTextColor(ContextCompat.getColor(requireActivity(), R.color.moa_gray_800))
            } else {
                binding.tvPushNotificationDate.setTextColor(ContextCompat.getColor(requireActivity(), R.color.moa_gray_400))
                binding.tvPushNotificationTime.setTextColor(ContextCompat.getColor(requireActivity(), R.color.moa_gray_400))
            }
        }

        binding.tvLogout.setOnClickListener {
            val requestBody = RefreshTokenRequest(refreshToken!!)
            Log.d(TAG, "onCreateView: $requestBody")

            Toast.makeText(requireActivity(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireActivity(), Login2Activity::class.java))
            requireActivity().finish()

            //logout(requestBody)
        }

        binding.tvWithdrawal.setOnClickListener {
            Toast.makeText(requireActivity(), "회원탈퇴 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireActivity(), Login2Activity::class.java))
            requireActivity().finish()

            //withdraw()
        }

        return binding.root
    }

    private fun getMyProfile() {
        Retrofit2Generator.create(requireActivity()).getMyProfile().enqueue(object : Callback<GetMyProfileResponse> {
            override fun onResponse(
                call: Call<GetMyProfileResponse>,
                response: Response<GetMyProfileResponse>
            ) {
                if (response.isSuccessful) {
                    myProfile = response.body()?.data
                    binding.tvUserName.text = myProfile?.nickname
                    Glide.with(requireActivity())
                        .load(myProfile?.profileImageUrl)
                        .error(R.drawable.member_profile_default_icon)
                        .circleCrop()
                        .into(binding.civUserProfileImage)

                    Log.d(TAG, "myProfile: $myProfile")
                }
            }

            override fun onFailure(call: Call<GetMyProfileResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun getAllGifticonCount() {
        Retrofit2Generator.create(requireActivity()).getMyGifticonCount().enqueue(object : Callback<LogoutUserResponse> {
            override fun onResponse(call: Call<LogoutUserResponse>, response: Response<LogoutUserResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: ${response.body()}")
                    binding.tvAllCouponCount.text = "${response.body()?.data}개"
                }
            }

            override fun onFailure(call: Call<LogoutUserResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun getAvaliableGifticonCount() {
        Retrofit2Generator.create(requireActivity()).getUsableGifticonCount().enqueue(object : Callback<LogoutUserResponse> {
            override fun onResponse(call: Call<LogoutUserResponse>, response: Response<LogoutUserResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: ${response.body()}")
                    binding.tvAvailableCouponCount.text = "${response.body()?.data}개"
                }
            }

            override fun onFailure(call: Call<LogoutUserResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun getUsedGifticonCount() {
        Retrofit2Generator.create(requireActivity()).getUsedGifticonCount().enqueue(object : Callback<LogoutUserResponse> {
            override fun onResponse(call: Call<LogoutUserResponse>, response: Response<LogoutUserResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: ${response.body()}")
                    binding.tvUsedCouponCount.text = "${response.body()?.data}개"
                }
            }

            override fun onFailure(call: Call<LogoutUserResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun logout(refreshToken: RefreshTokenRequest) {
        Log.d(TAG, "logout: $refreshToken")

        Retrofit2Generator.create(requireActivity()).logoutUser(refreshToken).enqueue(object : Callback<LogoutUserResponse> {
            override fun onResponse(call: Call<LogoutUserResponse>, response: Response<LogoutUserResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: ${response.body()}")
                    Toast.makeText(requireActivity(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finish()
                }
            }

            override fun onFailure(call: Call<LogoutUserResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    private fun withdraw() {
        Retrofit2Generator.create(requireActivity()).deleteUser().enqueue(object : Callback<LogoutUserResponse> {
            override fun onResponse(call: Call<LogoutUserResponse>, response: Response<LogoutUserResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "onResponse: ${response.body()}")
                    Toast.makeText(requireActivity(), "회원탈퇴 되었습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireActivity(), LoginActivity::class.java))
                    requireActivity().finish()
                }
            }

            override fun onFailure(call: Call<LogoutUserResponse>, t: Throwable) {
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }

    override fun onStart() {
        super.onStart()

        getMyProfile()
        getAllGifticonCount()
        getAvaliableGifticonCount()
        getUsedGifticonCount()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}