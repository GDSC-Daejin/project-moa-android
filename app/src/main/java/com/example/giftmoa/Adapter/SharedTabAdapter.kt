package com.example.giftmoa.Adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.giftmoa.ShareRoomMenu.ShareAvailableFragment
import com.example.giftmoa.ShareRoomMenu.ShareEntireFragment
import com.example.giftmoa.ShareRoomMenu.ShareUsedFragment

//여기 페이지 변경하기
class SharedTabAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> ShareEntireFragment()
            1 -> ShareAvailableFragment()
            else -> ShareUsedFragment()
        }
    }
}
