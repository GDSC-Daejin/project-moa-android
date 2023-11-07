package com.example.giftmoa

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.giftmoa.BottomMenu.AccountFragment
import com.example.giftmoa.BottomMenu.CouponFragment
import com.example.giftmoa.BottomMenu.HomeFragment
import com.example.giftmoa.BottomMenu.ShareRoomFragment
import com.example.giftmoa.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding : ActivityMainBinding
    private val TAG_HOME = "home_fragment"
    private val TAG_SHAREROOM = "shareroom_fragment"
    private val TAG_ACCOUNT = "account_fragment"
    val TAG_COUPON = "coupon_fragment"

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("TokenData", Context.MODE_PRIVATE)
        val grantType = sharedPref.getString("grantType", null) // 기본값은 null
        val accessToken = sharedPref.getString("accessToken", null) // 기본값은 null
        val refreshToken = sharedPref.getString("refreshToken", null) // 기본값은 null
        val accessTokenExpiresIn = sharedPref.getLong("accessTokenExpiresIn", -1) // 기본값은 -1

        Log.d("MainActivity", "grantType: $grantType")
        Log.d("MainActivity", "accessToken: $accessToken")
        Log.d("MainActivity", "refreshToken: $refreshToken")
        Log.d("MainActivity", "accessTokenExpiresIn: $accessTokenExpiresIn")


        mBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(mBinding.root)
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
}