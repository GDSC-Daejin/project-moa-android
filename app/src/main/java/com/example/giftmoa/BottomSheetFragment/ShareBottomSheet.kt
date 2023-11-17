package com.example.giftmoa.BottomSheetFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.giftmoa.Adapter.ShareBottomSheetAdapter
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GetMyTeamListResponse
import com.example.giftmoa.Data.ShareRoomGifticonResponseData
import com.example.giftmoa.Data.Team
import com.example.giftmoa.Data.TeamShareGiftIcon
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.ShareBottomSheetListener
import com.example.giftmoa.databinding.FragmentCategoryBottomSheetBinding
import com.example.giftmoa.databinding.FragmentShareBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareBottomSheet(private val gifticonId: Long, private val listener: ShareBottomSheetListener): BottomSheetDialogFragment() {

    private lateinit var binding: FragmentShareBottomSheetBinding
    private val TAG = "ShareBottomSheet"
    private val  teamList = mutableListOf<Team>()
    private lateinit var shareBottomSheetAdapter: ShareBottomSheetAdapter

    private var team: Team? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_share_bottom_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentShareBottomSheetBinding.bind(view)

        val behavior = BottomSheetBehavior.from(view.parent as View)
        val vto = view.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val height = view.height
                behavior.peekHeight = height
                // 레이아웃 리스너를 제거하여 중복 호출을 방지
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        shareBottomSheetAdapter = ShareBottomSheetAdapter { team ->
            Log.d(TAG, "team = $team")
            team?.let {
                this.team = it
            }
        }

        initRecyclerView()

        getMyTeamListFromServer()

        binding.btnShare.setOnClickListener {

            if (team == null) {
                Toast.makeText(requireContext(), "팀을 선택해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                teamShareGificon()
            }

            dismiss()
        }

        binding.btnBack.setOnClickListener {
            dismiss()
        }

        binding.root.setOnClickListener {
            dismiss()
        }
    }

    private fun initRecyclerView() {
        binding.rvShareRoom.apply {
            adapter = shareBottomSheetAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
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
                        teamList.addAll(newList)
                        Log.d(TAG, "getMyTeamListFromServer: teamList = $teamList")

                        // 어댑터에게 리스트가 갱신되었음을 알려줍니다.
                        shareBottomSheetAdapter.submitList(teamList.toList())

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

    private fun teamShareGificon() {
        val teamShareGiftIcon = team?.id?.toInt()?.let { TeamShareGiftIcon(it, gifticonId.toInt()) }

        if (teamShareGiftIcon != null) {
            Retrofit2Generator.create(requireActivity()).teamShareGificon(teamShareGiftIcon).enqueue(object :
                Callback<ShareRoomGifticonResponseData> {
                override fun onResponse(call: Call<ShareRoomGifticonResponseData>, response: Response<ShareRoomGifticonResponseData>) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()

                        team?.let { listener.onTeamUpdated(it) }

                    } else {
                        Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ShareRoomGifticonResponseData>, t: Throwable) {
                    Log.e(TAG, "Retrofit onFailure: ", t)
                }
            })
        }
    }
}