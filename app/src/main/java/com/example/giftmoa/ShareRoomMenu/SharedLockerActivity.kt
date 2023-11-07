package com.example.giftmoa.ShareRoomMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.giftmoa.Adapter.SharedTabAdapter
import com.example.giftmoa.databinding.ActivitySharedLockerBinding
import com.google.android.material.tabs.TabLayoutMediator

class SharedLockerActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivitySharedLockerBinding
    private val tabTextList = listOf("전체", "사용가능", "사용완료")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivitySharedLockerBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
        sBinding.viewpager.adapter = SharedTabAdapter(this)

        TabLayoutMediator(sBinding.categoryTabLayout, sBinding.viewpager) { tab, pos ->
            tab.text = tabTextList[pos]
        }.attach()
    }
}