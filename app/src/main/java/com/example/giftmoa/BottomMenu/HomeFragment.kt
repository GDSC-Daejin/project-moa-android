package com.example.giftmoa.BottomMenu

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.Adapter.HomeGiftAdapter
import com.example.giftmoa.Adapter.HomeTabAdapter
import com.example.giftmoa.Adapter.HomeUsedGiftAdapter
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.UsedGiftData
import com.example.giftmoa.R
import com.example.giftmoa.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var hBinding : FragmentHomeBinding
    private var giftAdapter : HomeGiftAdapter? = null
    private var giftAllData = ArrayList<GiftData>()
    private var gridManager = GridLayoutManager(activity, 2)
    //private val tabTextList = listOf("전체", "사용가능", "사용완료")

    private var usedGiftAdapter : HomeUsedGiftAdapter? = null
    private var usedGiftAllData = ArrayList<UsedGiftData>()


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
    ): View {
        hBinding = FragmentHomeBinding.inflate(inflater, container, false)
        initUsedRecyclerView()
        initHomeRecyclerView()

        hBinding.homeMoveIv.setOnClickListener {
            val intent = Intent(requireActivity(), LockerActivity::class.java)
            startActivity(intent)
        }
        /*hBinding.viewpager.adapter = HomeTabAdapter(requireActivity())

        TabLayoutMediator(hBinding.categoryTabLayout, hBinding.viewpager) { tab, pos ->
            tab.text = tabTextList[pos]
            //val typeface = resources.getFont(com.example.mio.R.font.pretendard_medium)
            //tab.setIcon(tabIconList[pos])
        }.attach()*/

        return hBinding.root
    }

    private fun initUsedRecyclerView() {
        setUsedGiftData()
        usedGiftAdapter = HomeUsedGiftAdapter()
        usedGiftAdapter!!.usedGiftItemData = usedGiftAllData
        hBinding.usedRv.adapter = usedGiftAdapter
        hBinding.usedRv.setHasFixedSize(true)
    }

    private fun initHomeRecyclerView() {
        setGiftData()

        giftAdapter = HomeGiftAdapter()
        giftAdapter!!.giftItemData = giftAllData
        hBinding.giftRv.adapter = giftAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        hBinding.giftRv.setHasFixedSize(true)
        hBinding.giftRv.layoutManager = gridManager

    }

    private fun setUsedGiftData() {
        usedGiftAllData.add(UsedGiftData(12, "스타벅스", "아이스 아메리카노", 0, "그지1", null))
        usedGiftAllData.add(UsedGiftData(13, "스타벅스", "아이스 아메리카노", 0, "그지1", null))
        usedGiftAllData.add(UsedGiftData(14, "스타벅스", "아이스 아메리카노", 0, "그지1", null))
    }

    private fun setGiftData() {
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}