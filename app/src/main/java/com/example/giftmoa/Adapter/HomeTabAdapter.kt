package com.example.giftmoa.Adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.giftmoa.BottomMenu.CouponFragment
import com.example.giftmoa.HomeTab.HomeAvailableFragment
import com.example.giftmoa.HomeTab.HomeEntireFragment
import com.example.giftmoa.HomeTab.HomeUsedFragment


//여기 페이지 변경하기
class HomeTabAdapter(fragment: Fragment): FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when(position) {
            0 -> HomeEntireFragment()
            1 -> HomeAvailableFragment()
            else -> HomeUsedFragment()
        }
    }
}
