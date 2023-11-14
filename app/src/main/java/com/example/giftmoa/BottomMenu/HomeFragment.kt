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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.Adapter.HomeShareRoomNameAdapter
import com.example.giftmoa.Adapter.HomeSharedGifticonAdapter
import com.example.giftmoa.Adapter.HomeUsedGiftAdapter
import com.example.giftmoa.Data.AddCategoryResponse
import com.example.giftmoa.Data.GetGifticonListResponse
import com.example.giftmoa.Data.GetMyTeamListResponse
import com.example.giftmoa.Data.GetTeamGifticonListResponse
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.HomeData
import com.example.giftmoa.Data.ShareRoomDetailItem
import com.example.giftmoa.Data.Team
import com.example.giftmoa.Data.TeamGifticon
import com.example.giftmoa.Data.UsedGiftData
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.LeftMarginItemDecoration
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.FragmentHomeBinding
import com.google.gson.Gson
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

    private val TAG = "HomeFragment"

    private lateinit var giftAdapter : GifticonListAdapter

    private var giftAllData = ArrayList<GiftData>()
    private var gridManager = GridLayoutManager(activity, 2)
    //private val tabTextList = listOf("전체", "사용가능", "사용완료")
    private var gifticonList = mutableListOf<Gifticon>()

    private var shareRoomDetailList = mutableListOf<Team>()
    private var teamGifticonList = mutableListOf<TeamGifticon>()

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

        giftAdapter = GifticonListAdapter({ gifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)
        }, gifticonList?: emptyList<Gifticon>())

        homeShareRoomNameAdapter = HomeShareRoomNameAdapter { team ->
            // shareRoomDetailList에서 shareRoom의 teamId와 같은 teamId를 가진 ShareRoomDetailItem을 찾기
            val teamDetail = shareRoomDetailList.find { it.id == team.id }
            if (teamDetail != null) {
                if (teamDetail.teamImage == null) {
                    hBinding.ivShareRoomImage.setImageResource(R.drawable.icon_logo)
                    hBinding.ivShareRoomImage.setPadding(100, 100, 100, 100)
                } else {
                    Glide.with(requireActivity())
                        .load(teamDetail?.teamImage)
                        .into(hBinding.ivShareRoomImage)
                }
            }
            teamGifticonList.clear()
            getTeamGifticonListFromServer(team.id!!)
        }
        homeSharedGifticonAdapter = HomeSharedGifticonAdapter({ teamGifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", teamGifticon.gifticonId)
            startActivity(intent)
        }, requireActivity())

        getMyTeamListFromServer()

        initHomeShareRoomNameRecyclerView()
        initHomeSharedGifticonRecyclerView()
        initHomeRecyclerView()

        //getHomeShareRoomInfoFromServer()

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

        //homeShareRoomNameAdapter.submitList(shareRoomDetailList)
    }

    private fun getHomeGifticonListFromServer(page: Int) {
        Retrofit2Generator.create(requireActivity()).getRecentGifticonList(size = 30, page = page).enqueue(object :
            Callback<GetGifticonListResponse> {
            override fun onResponse(call: Call<GetGifticonListResponse>, response: Response<GetGifticonListResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.dataList?.let { newList ->
                        Log.d(TAG, "getHomeGifticonListFromServer: newList = $newList")

                        if (page == 0) {
                            // 첫 페이지인 경우 리스트를 새로 채웁니다.
                            gifticonList.clear()
                        }
                        // 새로운 데이터를 리스트에 추가합니다.
                        val currentPosition = gifticonList.size
                        gifticonList.addAll(newList)

                        // 어댑터에 데이터가 변경되었음을 알립니다.
                        // DiffUtil.Callback 사용을 위한 submitList는 비동기 처리를 하므로 리스트의 사본을 넘깁니다.
                        giftAdapter.submitList(gifticonList.toList())
                        // 또는
                        // DiffUtil을 사용하지 않는 경우
                        //giftAdapter.notifyItemRangeInserted(currentPosition, newList.size)
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun getMyTeamListFromServer() {
        Retrofit2Generator.create(requireActivity()).getMyTeamList().enqueue(object :
            Callback<GetMyTeamListResponse> {
            override fun onResponse(call: Call<GetMyTeamListResponse>, response: Response<GetMyTeamListResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.let { newList ->

                        Log.d(TAG, "getMyTeamListFromServer: newList = $newList")
                        // 새로운 데이터를 리스트에 추가합니다.
                        val currentPosition = shareRoomDetailList.size
                        shareRoomDetailList.addAll(newList)

                        // 어댑터에 데이터가 변경되었음을 알립니다.
                        // DiffUtil.Callback 사용을 위한 submitList는 비동기 처리를 하므로 리스트의 사본을 넘깁니다.
                        homeShareRoomNameAdapter.submitList(shareRoomDetailList.toList())
                        // 또는
                        // DiffUtil을 사용하지 않는 경우
                        //giftAdapter.notifyItemRangeInserted(currentPosition, newList.size)
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetMyTeamListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun getTeamGifticonListFromServer(teamId: Long) {
        Log.d(TAG, "getTeamGifticonListFromServer: teamId = $teamId")

        Retrofit2Generator.create(requireActivity()).getTeamGifticonList(teamId, 0, 10).enqueue(object :
            Callback<GetTeamGifticonListResponse> {
            override fun onResponse(call: Call<GetTeamGifticonListResponse>, response: Response<GetTeamGifticonListResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.dataList?.let { newList ->
                        // 새로운 데이터를 리스트에 추가합니다.
                        val currentPosition = teamGifticonList.size
                        teamGifticonList.addAll(newList)

                        Log.d(TAG, "getTeamGifticonListFromServer: teamGifticonList = $teamGifticonList")

                        // 어댑터에 데이터가 변경되었음을 알립니다.
                        // DiffUtil.Callback 사용을 위한 submitList는 비동기 처리를 하므로 리스트의 사본을 넘깁니다.
                        homeSharedGifticonAdapter.submitList(teamGifticonList.toList())
                        // 또는
                        // DiffUtil을 사용하지 않는 경우
                        //giftAdapter.notifyItemRangeInserted(currentPosition, newList.size)
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetTeamGifticonListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    // 팀 목록 가져오기
    /*private fun getHomeShareRoomInfoFromServer() {

        Log.d(TAG, "getHomeShareRoomInfoFromServer: ")
        val retrofitService = Retrofit2Generator.create(requireActivity())

        lifecycleScope.launch {
            try {
                val teamResponse = retrofitService.getMyTeamList()
                if (teamResponse.isSuccessful) {
                    val teamList = teamResponse.body()?.data ?: emptyList()
                    Log.d(TAG, "getHomeShareRoomInfoFromServer: teamList = $teamList")
                    shareRoomDetailList.addAll(teamList)

                    // 병렬로 네트워크 요청을 시작합니다.
                    val gifticonLists = teamList.mapNotNull { team ->
                        async { team.id?.let { retrofitService.getTeamGifticonList(it) } }
                    }.awaitAll()

                    // 모든 네트워크 요청이 완료된 후에 리스트를 업데이트합니다.
                    gifticonLists.forEach { response ->
                        if (response?.isSuccessful == true) {
                            teamGifticonList.addAll(response.body()?.data?.dataList ?: emptyList())
                        }
                    }

                    // 최종적으로 어댑터에 데이터를 제공합니다.
                    homeShareRoomNameAdapter.submitList(shareRoomDetailList.toList())
                }
            } catch (e: Exception) {
                Log.e(TAG, "getHomeShareRoomInfoFromServer: ", e)
            }
        }
    }*/


    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")

        getHomeGifticonListFromServer(0)
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