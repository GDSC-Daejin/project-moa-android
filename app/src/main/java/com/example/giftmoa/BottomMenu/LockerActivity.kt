package com.example.giftmoa.BottomMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.giftmoa.Adapter.HomeTabAdapter
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityLockerBinding
import com.google.android.material.tabs.TabLayoutMediator

class LockerActivity : AppCompatActivity() {
    private lateinit var lBinding : ActivityLockerBinding
    private val tabTextList = listOf("전체", "사용가능", "사용완료")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lBinding = ActivityLockerBinding.inflate(layoutInflater)
        setContentView(lBinding.root)


        lBinding.viewpager.adapter = HomeTabAdapter(this@LockerActivity)

        TabLayoutMediator(lBinding.categoryTabLayout, lBinding.viewpager) { tab, pos ->
            tab.text = tabTextList[pos]
            //val typeface = resources.getFont(com.example.mio.R.font.pretendard_medium)
            //tab.setIcon(tabIconList[pos])
        }.attach()

    }
}