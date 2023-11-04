package com.example.giftmoa.HomeTab

import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.Adapter.HomeGiftAdapter
import com.example.giftmoa.AssetLoader
import com.example.giftmoa.BottomSheetFragment.BottomSheetFragment
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BottomSheetFragment.GifticonEditDeleteBottomSheet
import com.example.giftmoa.CouponTab.CouponAutoAddActivity
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GifticonRegistrationActivity
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.ManualRegistrationActivity
import com.example.giftmoa.R
import com.example.giftmoa.databinding.FragmentHomeEntireBinding
import com.google.android.material.chip.Chip
import com.google.gson.Gson

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeEntireFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeEntireFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentHomeEntireBinding
    private val TAG = "HomeEntireFragment"

    private lateinit var giftAdapter : GifticonListAdapter

    private var gifticonList = mutableListOf<GifticonDetailItem>()

    private var giftAllData = ArrayList<GiftData>()
    private var gridManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
    private var getBottomSheetData = ""

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
        binding = FragmentHomeEntireBinding.inflate(inflater, container, false)

        giftAdapter = GifticonListAdapter { gifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)
        }

        giftAdapter.itemLongClickListener = object : GifticonListAdapter.OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {
                // 길게 클릭한 아이템에 대한 처리 로직을 여기에 작성
                val gifticon = giftAdapter.currentList[position]

                val bottomSheet = GifticonEditDeleteBottomSheet()
                bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
                bottomSheet.apply {
                    setCallback(object : GifticonEditDeleteBottomSheet.OnSendFromBottomSheetDialog{
                        override fun sendValue(value: String) {
                            Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                            getBottomSheetData = value
                            //myViewModel.postCheckSearchFilter(getBottomSheetData)
                            when (value) {
                                "수정하기" -> {
                                    val intent = Intent(requireActivity(), ManualRegistrationActivity::class.java)
                                    intent.putExtra("gifticon", gifticon)
                                    startActivity(intent)
                                }

                                "삭제하기" -> {

                                }
                            }
                        }
                    })
                }
            }
        }



        getJsonData()

        initHomeRecyclerView()


        return binding.root
    }

    private fun initHomeRecyclerView() {
        binding.giftRv.apply {
            adapter = giftAdapter
            layoutManager = gridManager
            binding.giftRv.addItemDecoration(
                GridSpacingItemDecoration(spanCount = 2, spacing = 10f.fromDpToPx())
            )
        }
    }

    private fun Float.fromDpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()


    private fun getJsonData() {
        val assetLoader = AssetLoader()
        val storageJsonString = assetLoader.getJsonString(requireActivity(), "storage.json")

        if (!storageJsonString.isNullOrEmpty()) {
            val gson = Gson()
            val storageData = gson.fromJson(storageJsonString, StorageData::class.java)

            for (gifticon in storageData.gifticons) {
                gifticonList.add(gifticon)
            }

            if (gifticonList.size == 0) {
                binding.tvNoGifticon.visibility = View.VISIBLE
            } else {
                binding.tvNoGifticon.visibility = View.GONE
                giftAdapter.submitList(gifticonList)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeEntireFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeEntireFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}