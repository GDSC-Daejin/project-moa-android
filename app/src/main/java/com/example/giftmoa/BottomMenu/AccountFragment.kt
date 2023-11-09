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
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.GetMyProfileResponse
import com.example.giftmoa.Data.LogoutUserResponse
import com.example.giftmoa.Data.MyProfileData
import com.example.giftmoa.Data.RefreshTokenRequest
import com.example.giftmoa.Login2Activity
import com.example.giftmoa.LoginActivity
import com.example.giftmoa.MyProfileActivity
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
                    Log.d(TAG, "onResponse: ${response.body()}")
                    binding.tvUserName.text = response.body()?.data?.nickname
                    Glide.with(requireActivity())
                        .load(response.body()?.data?.profileImageUrl)
                        .circleCrop()
                        .into(binding.civUserProfileImage)

                    myProfile = response.body()?.data
                    Log.d(TAG, "myProfile: $myProfile")
                }
            }

            override fun onFailure(call: Call<GetMyProfileResponse>, t: Throwable) {
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