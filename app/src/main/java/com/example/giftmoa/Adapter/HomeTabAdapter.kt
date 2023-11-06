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
    private val fragmentList: MutableList<Fragment> = ArrayList()

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> HomeEntireFragment()
            1 -> HomeAvailableFragment()
            else -> HomeUsedFragment()
        }
        // 프래그먼트 참조를 리스트에 추가
        fragmentList.add(position, fragment)
        return fragment
    }

    fun getFragment(position: Int): Fragment? {
        return fragmentList.getOrNull(position)
    }
}
