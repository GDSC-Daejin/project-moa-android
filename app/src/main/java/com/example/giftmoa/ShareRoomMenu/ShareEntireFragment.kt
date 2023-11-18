package com.example.giftmoa.ShareRoomMenu

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.*
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.Adapter.MemberListAdapter
import com.example.giftmoa.Adapter.ShareRoomGifticonAdapter
import com.example.giftmoa.Adapter.TeamGifticonListAdapter
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BottomSheetFragment.SortBottomSheet
import com.example.giftmoa.Data.*
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.HomeTab.GifticonViewModel
import com.example.giftmoa.databinding.FragmentShareEntireBinding
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShareEntireFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShareEntireFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding : FragmentShareEntireBinding

    private val TAG = "ShareEntireFragment"

    private var gridManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)

    private var teamId : Int? = 0

    private lateinit var giftAdapter: TeamGifticonListAdapter
    private lateinit var teamGifticonViewModel: TeamGifticonViewModel
    private lateinit var gifticonStatusResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        teamGifticonViewModel.couponList.observe(viewLifecycleOwner, Observer {
            binding.giftRv.post(Runnable {
                giftAdapter.submitList(it.toList())
            })
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        teamGifticonViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory())[TeamGifticonViewModel::class.java]
        binding = FragmentShareEntireBinding.inflate(inflater, container, false)

        val sharedPref = activity?.getSharedPreferences("readTeamId", Context.MODE_PRIVATE)
        teamId = sharedPref?.getInt("teamId", 0)?.toInt()// 기본값은 null
        println("entire"+teamId)

        val allCouponList = teamGifticonViewModel.allCouponList.value

        Log.d(TAG, "allCouponList: $allCouponList")

        giftAdapter = TeamGifticonListAdapter({ gifticon ->
            gifticonStatusResult.launch(
                Intent(
                    requireActivity(),
                    GifticonDetailActivity::class.java
                ).apply {
                    putExtra("gifticonId", gifticon.gifticonId)
                })
        }, allCouponList ?: emptyList<TeamGifticon>())

        initShareEntireRecyclerView()

        gifticonStatusResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val updatedGifticonWithStatus = if (Build.VERSION.SDK_INT >= 33) {
                    it.data?.getParcelableExtra(
                        "updatedGifticonWithStatus",
                        Gifticon::class.java
                    )
                } else {
                    it.data?.getParcelableExtra<Gifticon>("updatedGifticonWithStatus")
                }

                val teamGifticon = TeamGifticon(
                    gifticonId = updatedGifticonWithStatus?.id,
                    name = updatedGifticonWithStatus?.name,
                    gifticonImagePath = updatedGifticonWithStatus?.gifticonImagePath,
                    exchangePlace = updatedGifticonWithStatus?.exchangePlace,
                    dueDate = updatedGifticonWithStatus?.dueDate,
                    gifticonType = updatedGifticonWithStatus?.gifticonType,
                    orderNumber = null,
                    status = updatedGifticonWithStatus?.status,
                    usedDate = updatedGifticonWithStatus?.usedDate,
                    author = updatedGifticonWithStatus?.author,
                    category = updatedGifticonWithStatus?.category,
                    gifticonMoney = null
                )

                Log.d(TAG, "updatedGifticonWithStatus: $updatedGifticonWithStatus")
                teamGifticonViewModel.updateCoupon(teamGifticon)
            }
        }

        return binding.root
    }


    /*private fun initShareEntireRecyclerView() {
        getEntireListFromServer(0)
        println("entire")

        giftAdapter = TeamGifticonListAdapter()
        giftAdapter!!.shareRoomGifticonItemData = gifticonList
        binding.giftRv.adapter = giftAdapter
        binding.giftRv.setHasFixedSize(true)
        binding.giftRv.layoutManager = gridManager

        binding.giftRv.addItemDecoration(
            GridSpacingItemDecoration(spanCount = 2, spacing = 10f.fromDpToPx())
        )

        *//*binding.giftRv.apply {
            giftAdapter = ShareRoomGifticonAdapter()
            adapter = giftAdapter
            giftAdapter!!.shareRoomGifticonItemData = gifticonList
            layoutManager = gridManager

        }*//*
    }*/

    private fun initShareEntireRecyclerView() {
        binding.giftRv.apply {
            adapter = giftAdapter
            //adapter = couponListAdapter
            layoutManager = gridManager
            binding.giftRv.addItemDecoration(
                GridSpacingItemDecoration(spanCount = 2, spacing = 10f.fromDpToPx())
            )
        }
    }

    private fun Float.fromDpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ShareEntireFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShareEntireFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}