package com.example.giftmoa.ShareRoomMenu

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.giftmoa.Adapter.SharedTabAdapter
import com.example.giftmoa.databinding.ActivitySharedLockerBinding
import com.google.android.material.tabs.TabLayoutMediator

class SharedLockerActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivitySharedLockerBinding
    private val tabTextList = listOf("전체", "사용가능", "사용완료")
    private var teamId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivitySharedLockerBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
        sBinding.viewpager.adapter = SharedTabAdapter(this)

        teamId = intent.getLongExtra("teamId", 0L).toInt()

        val sharedPref = getSharedPreferences("readTeamId", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt("teamId", teamId)
            apply()
        }

        TabLayoutMediator(sBinding.categoryTabLayout, sBinding.viewpager) { tab, pos ->
            tab.text = tabTextList[pos]
        }.attach()

        sBinding.backArrow.setOnClickListener {
            this.finish()
        }
    }
}