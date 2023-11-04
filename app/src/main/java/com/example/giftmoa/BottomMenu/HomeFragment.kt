package com.example.giftmoa.BottomMenu

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.Adapter.HomeShareRoomNameAdapter
import com.example.giftmoa.Adapter.HomeSharedGifticonAdapter
import com.example.giftmoa.Adapter.HomeUsedGiftAdapter
import com.example.giftmoa.AssetLoader
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.HomeData
import com.example.giftmoa.Data.ShareRoomDetailItem
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.Data.UsedGiftData
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.LeftMarginItemDecoration
import com.example.giftmoa.R
import com.example.giftmoa.RightMarginItemDecoration
import com.example.giftmoa.databinding.FragmentHomeBinding
import com.google.android.material.chip.Chip
import com.google.gson.Gson

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
    private lateinit var giftAdapter : GifticonListAdapter

    private var giftAllData = ArrayList<GiftData>()
    private var gridManager = GridLayoutManager(activity, 2)
    //private val tabTextList = listOf("전체", "사용가능", "사용완료")
    private var gifticonList = mutableListOf<GifticonDetailItem>()
    private var shareRoomDetailList = mutableListOf<ShareRoomDetailItem>()

    private var usedGiftAdapter : HomeUsedGiftAdapter? = null
    private lateinit var homeSharedGifticonAdapter: HomeSharedGifticonAdapter
    private lateinit var homeShareRoomNameAdapter: HomeShareRoomNameAdapter

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

        giftAdapter = GifticonListAdapter { gifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)
        }
        homeShareRoomNameAdapter = HomeShareRoomNameAdapter { shareRoom ->
            // shareRoomDetailList에서 shareRoom의 teamId와 같은 teamId를 가진 ShareRoomDetailItem을 찾기
            val shareRoomDetail = shareRoomDetailList.find { it.teamId == shareRoom.teamId }
            Glide.with(requireActivity())
                .load(shareRoomDetail?.teamThumbnailImage)
                .into(hBinding.ivShareRoomImage)
        }
        homeSharedGifticonAdapter = HomeSharedGifticonAdapter({ gifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)
        }, requireActivity())

        getJsonData()

        initHomeShareRoomNameRecyclerView()
        initHomeSharedGifticonRecyclerView()
        initHomeRecyclerView()

        hBinding.homeMoveIv.setOnClickListener {
            val bottomNavigationView = requireActivity().findViewById<View>(R.id.bottom_navigation_view)
            // 바텀 네비게이션의 다른 메뉴를 선택하도록 설정
            bottomNavigationView.findViewById<View>(R.id.navigation_coupon).performClick()
        }

        return hBinding.root
    }

    /*private fun initUsedRecyclerView() {
        setUsedGiftData()
        usedGiftAdapter = HomeUsedGiftAdapter()
        usedGiftAdapter!!.usedGiftItemData = usedGiftAllData
        hBinding.usedRv.adapter = usedGiftAdapter
        hBinding.usedRv.setHasFixedSize(true)
    }*/

    private fun initHomeSharedGifticonRecyclerView() {
        val dp20 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics).toInt()
        hBinding.usedRv.apply {
            adapter = homeSharedGifticonAdapter
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(LeftMarginItemDecoration(dp20))
        }
    }

    private fun Float.fromDpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun initHomeRecyclerView() {
        hBinding.giftRv.apply {
            adapter = giftAdapter
            layoutManager = gridManager
            hBinding.giftRv.addItemDecoration(
                GridSpacingItemDecoration(spanCount = 2, spacing = 10f.fromDpToPx())
            )
        }
    }

    private fun initHomeShareRoomNameRecyclerView() {
        hBinding.rvShareRoomName?.apply {
            adapter = homeShareRoomNameAdapter
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        }

        homeShareRoomNameAdapter.submitList(shareRoomDetailList)
    }

    private fun getJsonData() {
        val assetLoader = AssetLoader()
        val homeJsonString = assetLoader.getJsonString(requireActivity(), "home.json")
        //Log.d(TAG, autoRegistrationJsonString ?: "null")

        if (!homeJsonString.isNullOrEmpty()) {
            val gson = Gson()
            val homeData = gson.fromJson(homeJsonString, HomeData::class.java)

            for (shareRoom in homeData.shareRooms) {
                shareRoomDetailList.add(shareRoom)
            }


            for (gifticon in homeData.gifticons) {
                gifticonList.add(gifticon)
            }

            giftAdapter.submitList(gifticonList)
            homeSharedGifticonAdapter.submitList(gifticonList)
            homeShareRoomNameAdapter.submitList(shareRoomDetailList)
        }
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