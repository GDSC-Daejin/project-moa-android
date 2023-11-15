package com.example.giftmoa.HomeTab

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.res.Resources
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
import androidx.recyclerview.widget.RecyclerView
import com.example.giftmoa.Adapter.CouponListAdapter
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.BottomMenu.CouponFragment
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.BottomSheetFragment.GifticonEditDeleteBottomSheet
import com.example.giftmoa.BottomSheetFragment.SortBottomSheet
import com.example.giftmoa.Data.GiftData

import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.CouponTab.ManualRegistrationActivity
import com.example.giftmoa.Data.AddCategoryRequest
import com.example.giftmoa.Data.AddCategoryResponse
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.Data1
import com.example.giftmoa.Data.GetCategoryListResponse
import com.example.giftmoa.Data.GetGifticonListResponse
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.GifticonData
import com.example.giftmoa.Data.LogoutUserResponse
import com.example.giftmoa.Data.UpdateGifticonRequest
import com.example.giftmoa.Data.UpdateGifticonResponse

import com.example.giftmoa.Data.ShareRoomGifticon

import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.FragmentHomeEntireBinding
import com.google.android.material.chip.Chip
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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


    private lateinit var binding: FragmentHomeEntireBinding
    private val TAG = "HomeEntireFragment"

    private lateinit var giftAdapter: GifticonListAdapter

    var gifticonList = mutableListOf<Gifticon>()

    private var categoryList = mutableListOf<CategoryItem>()

    private var gridManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)

    private var getBottomSheetData = ""

    private lateinit var manualAddGifticonResult: ActivityResultLauncher<Intent>

    private lateinit var gifticonStatusResult: ActivityResultLauncher<Intent>

    private lateinit var gifticonViewModel: GifticonViewModel

    private lateinit var couponListAdapter: CouponListAdapter

    private var currentCategoryId = 0L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.d(TAG, "onCreate: ")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gifticonViewModel.couponList.observe(viewLifecycleOwner, Observer {
            binding.giftRv.post(Runnable {
                //*(binding.giftRv.adapter as? CouponListAdapter)?.setAllCouponsData(coupons)*//*
                //couponListAdapter.setAllCouponsData(it)
                giftAdapter.submitList(it.toList())
                if (it.toList().isEmpty()) {
                    binding.llNoGifticon.visibility = View.VISIBLE
                } else {
                    binding.llNoGifticon.visibility = View.GONE
                }
            })
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        gifticonViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory())[GifticonViewModel::class.java]

        binding = FragmentHomeEntireBinding.inflate(inflater, container, false)

        Log.d(TAG, "onCreateView: ")

        //var allGifticonList = couponViewModel.allGifticonList.value

        val allCouponList = gifticonViewModel.allCouponList.value

        giftAdapter = GifticonListAdapter({ gifticon ->
            /*val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)*/
            gifticonStatusResult.launch(
                Intent(
                    requireActivity(),
                    GifticonDetailActivity::class.java
                ).apply {
                    putExtra("gifticonId", gifticon.id)
                })
        }, allCouponList ?: emptyList<Gifticon>())

        /*couponListAdapter = CouponListAdapter({ gifticon ->
            *//*val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)*//*
            gifticonStatusResult.launch(
                Intent(
                    requireActivity(),
                    GifticonDetailActivity::class.java
                ).apply {
                    putExtra("gifticonId", gifticon.id)
                })
        }, allCouponList ?: emptyList<Gifticon>())
        couponListAdapter.setHasStableIds(true)*/

        initHomeRecyclerView()

        manualAddGifticonResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val updatedGifticon = if (Build.VERSION.SDK_INT >= 33) {
                        it.data?.getParcelableExtra(
                            "updatedGifticon",
                            Gifticon::class.java
                        )
                    } else {
                        it.data?.getParcelableExtra<Gifticon>("updatedGifticon")
                    }
                    val isEdit = it.data?.getBooleanExtra("isEdit", false)
                    val couponList = gifticonViewModel.allCouponList.value
                    Log.d(TAG, "updatedGifticon: $updatedGifticon")
                    Log.d(TAG, "couponList: $couponList")
                    if (isEdit == true) {
                        // 현재 선택된 카테고리와 기프티콘의 카테고리가 다른 경우
                        if (updatedGifticon?.category?.id != couponList?.get(0)?.category?.id) {
                            updatedGifticon?.let { it1 -> it1.id?.let { it2 ->
                                gifticonViewModel.deleteCouponById(
                                    it2
                                )
                            } }
                        } else {
                            updatedGifticon?.let { it1 -> gifticonViewModel.updateCoupon(it1) }
                        }
                    } else {
                        // 기프티콘 추가
                        updatedGifticon?.let { it1 -> gifticonViewModel.addCoupon(it1) }
                    }
                }
            }

        gifticonStatusResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val updatedGifticonWithStatus = if (Build.VERSION.SDK_INT >= 33) {
                    it.data?.getParcelableExtra(
                        "updatedGifticonWithStatus",
                        Gifticon::class.java
                    )
                } else {
                    it.data?.getParcelableExtra<Gifticon>("updatedGifticonWithStatus")
                }
                Log.d(TAG, "updatedGifticonWithStatus: $updatedGifticonWithStatus")
                updatedGifticonWithStatus?.let { it1 -> gifticonViewModel.updateCoupon(it1) }
            }
        }

        /*if (gifticonViewModel.allCouponList.value == null) {
            binding.tvNoGifticon.visibility = View.VISIBLE
            binding.tvOfferCouponRegistration.visibility = View.VISIBLE
        } else {
            binding.tvNoGifticon.visibility = View.GONE
            binding.tvOfferCouponRegistration.visibility = View.GONE
        }*/

        return binding.root
    }

    private fun initHomeRecyclerView() {
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

    /*private fun getAllGifticonListFromServer(page: Int) {
        Retrofit2Generator.create(requireActivity()).getAllGifticonList(size = 30, page = page).enqueue(object :
            Callback<GetGifticonListResponse> {
            override fun onResponse(call: Call<GetGifticonListResponse>, response: Response<GetGifticonListResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.dataList?.let { newList ->
                        if (page == 0) {
                            // 첫 페이지인 경우 리스트를 새로 채웁니다.
                            gifticonList.clear()
                        }
                        // 새로운 데이터를 리스트에 추가합니다.
                        val currentPosition = gifticonList.size
                        gifticonList.addAll(newList)

                        // 어댑터에 데이터가 변경되었음을 알립니다.
                        // DiffUtil.Callback 사용을 위한 submitList는 비동기 처리를 하므로 리스트의 사본을 넘깁니다.
                        giftAdapter.submitList(gifticonList.toList().sortedBy { it.dueDate })
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
    }*/

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")

        giftAdapter.itemLongClickListener = object : GifticonListAdapter.OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {
                // 길게 클릭한 아이템에 대한 처리 로직을 여기에 작성
                //val selectedGifticon = couponListAdapter.Coupons[position]
                val selectedGifticon = giftAdapter.currentList[position]

                val bottomSheet = GifticonEditDeleteBottomSheet()
                bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
                bottomSheet.apply {
                    setCallback(object : GifticonEditDeleteBottomSheet.OnSendFromBottomSheetDialog {
                        override fun sendValue(value: String) {
                            Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                            getBottomSheetData = value
                            //myViewModel.postCheckSearchFilter(getBottomSheetData)
                            when (value) {
                                "수정하기" -> {
                                    /*val intent = Intent(requireActivity(), ManualRegistrationActivity::class.java)
                                    intent.putExtra("gifticonId", selectedGifticon.id)
                                    intent.putExtra("isEdit", true)
                                    startActivity(intent)*/
                                    manualAddGifticonResult.launch(
                                        Intent(
                                            requireActivity(),
                                            ManualRegistrationActivity::class.java
                                        ).apply {
                                            putExtra("gifticonId", selectedGifticon.id)
                                            putExtra("isEdit", true)
                                        })
                                }

                                "삭제하기" -> {
                                    //deleteGifticon(gifticon)
                                    /*gifticonList.remove(gifticonId)
                                    giftAdapter.submitList(gifticonList.toList())
                                    giftAdapter.notifyDataSetChanged()*/
                                    gifticonViewModel.deleteCoupon(selectedGifticon)
                                }
                            }
                        }
                    })
                }
            }
        }

        /*val currentPage = 0

        getAllGifticonListFromServer(currentPage)*/
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
    }

  

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
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