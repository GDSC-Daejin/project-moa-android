package com.example.giftmoa

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.giftmoa.BottomMenu.AccountFragment
import com.example.giftmoa.BottomMenu.CouponFragment
import com.example.giftmoa.BottomMenu.HomeFragment
import com.example.giftmoa.BottomMenu.ShareRoomFragment
import com.example.giftmoa.Data.GetMyProfileResponse
import com.example.giftmoa.Data.SaveSharedPreference
import com.example.giftmoa.FCM.MyFirebaseMessagingService
import com.example.giftmoa.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityMainBinding
    private val PERMISSION_REQUEST_CODE = 500
    private val TAG_HOME = "home_fragment"
    private val TAG_SHAREROOM = "shareroom_fragment"
    private val TAG_ACCOUNT = "account_fragment"
    val TAG_COUPON = "coupon_fragment"

    private var backPressedTime: Long = 0
    private val sharedPreference = SaveSharedPreference()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("TokenData", Context.MODE_PRIVATE)
        val grantType = sharedPref.getString("grantType", null) // 기본값은 null
        val accessToken = sharedPref.getString("accessToken", null) // 기본값은 null
        val refreshToken = sharedPref.getString("refreshToken", null) // 기본값은 null
        val accessTokenExpiresIn = sharedPref.getLong("accessTokenExpiresIn", -1) // 기본값은 -1
        sharedPreference.setToken(this@MainActivity, accessToken).toString()
        sharedPreference.setExpireDate(this@MainActivity, accessTokenExpiresIn.toString()).toString()

        Log.d("MainActivity", "grantType: $grantType")
        Log.d("MainActivity", "accessToken: $accessToken")
        Log.d("MainActivity", "refreshToken: $refreshToken")
        Log.d("MainActivity", "accessTokenExpiresIn: $accessTokenExpiresIn")


        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)


        checkPostNotificationPermission()
        getMyProfile()
        setFragment(TAG_HOME, HomeFragment())
        initNavigationBar()
    }

    private fun initNavigationBar() {
        mBinding.bottomNavigationView.
        setOnItemSelectedListener {item ->
            when(item.itemId) {
                R.id.navigation_home -> {
                    //setToolbarView(TAG_HOME, oldTAG)
                    setFragment(TAG_HOME, HomeFragment())

                }

                R.id.navigation_coupon -> {
                    setFragment(TAG_COUPON, CouponFragment())
                }

                R.id.navigation_share_room -> {

                    //setToolbarView(TAG_HOME, oldTAG)
                    setFragment(TAG_SHAREROOM, ShareRoomFragment())


                }

                R.id.navigation_account -> {
                    setFragment(TAG_ACCOUNT, AccountFragment())
                }

                else -> {
                    setFragment(TAG_HOME, HomeFragment())
                }

            }
            true
        }


    }

    private fun setFragment(tag : String, fragment: Fragment) {
        val manager : FragmentManager = supportFragmentManager
        val bt = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            bt.add(R.id.fragment_content, fragment, tag).addToBackStack(null)
        }

        val home = manager.findFragmentByTag(TAG_HOME)
        val shareRoom = manager.findFragmentByTag(TAG_SHAREROOM)
        val account = manager.findFragmentByTag(TAG_ACCOUNT)
        val coupon = manager.findFragmentByTag(TAG_COUPON)

        if (home != null) {
            bt.hide(home)
        }
        if (shareRoom != null) {
            bt.hide(shareRoom)
        }
        if (account != null) {
            bt.hide(account)
        }
        if (coupon != null) {
            bt.hide(coupon)
        }

        if (tag == TAG_HOME) {
            if (home != null) {
                bt.show(home)
            }
        }
        else if (tag == TAG_SHAREROOM) {
            if (shareRoom != null) {
                bt.show(shareRoom)
            }
        }
        else if (tag == TAG_COUPON) {
            if (coupon != null) {
                bt.show(coupon)
            }
        }
        else if (tag == TAG_ACCOUNT) {
            if (account != null) {
                bt.show(account)
            }
        }

        bt.commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        if(System.currentTimeMillis() - backPressedTime >= 2000) {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else {
            finish()
        }
    }

    private fun getMyProfile() {
        Retrofit2Generator.create(this).getMyProfile().enqueue(object :
            Callback<GetMyProfileResponse> {
            override fun onResponse(
                call: Call<GetMyProfileResponse>,
                response: Response<GetMyProfileResponse>
            ) {
                if (response.isSuccessful) {
                    val sharedPref = getSharedPreferences("profile_nickname", Context.MODE_PRIVATE)
                    with(sharedPref.edit()) {
                        putString("profileNickname", response.body()?.data?.nickname)
                        apply() // 비동기적으로 데이터를 저장
                    }
                }
            }

            override fun onFailure(call: Call<GetMyProfileResponse>, t: Throwable) {
                Log.d("MainActivity", "onFailure: ${t.message}")
            }
        })
    }


    //안드로이드 13 이상 PostNotification 대응
    private fun checkPostNotificationPermission() {
        //Android 13 이상 && 푸시권한 없음
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)) {
            val permissionCheck = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "권한 거부됨", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(applicationContext, "권한 승인됨", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}