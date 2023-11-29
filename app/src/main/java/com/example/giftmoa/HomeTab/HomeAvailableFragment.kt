package com.example.giftmoa.HomeTab

import android.app.Activity
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
import com.example.giftmoa.Adapter.CouponListAdapter
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.BottomMenu.CouponFragment
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BottomSheetFragment.GifticonEditDeleteBottomSheet
import com.example.giftmoa.BottomSheetFragment.SortBottomSheet
import com.example.giftmoa.CouponTab.ManualRegistrationActivity
import com.example.giftmoa.Data.AddCategoryRequest
import com.example.giftmoa.Data.AddCategoryResponse
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GetCategoryListResponse
import com.example.giftmoa.Data.GetGifticonListResponse
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.GifticonData
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.Data.UpdateGifticonResponse
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.FragmentHomeAvailableBinding
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
 * Use the [HomeAvailableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeAvailableFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentHomeAvailableBinding
    private val TAG = "HomeAvailableFragment"

    private lateinit var giftAdapter: GifticonListAdapter

    var gifticonList = mutableListOf<Gifticon>()

    private var categoryList = mutableListOf<CategoryItem>()

    private var gridManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
    private var getBottomSheetData = ""

    private lateinit var manualAddGifticonResult: ActivityResultLauncher<Intent>

    private lateinit var gifticonStatusResult: ActivityResultLauncher<Intent>

    private lateinit var gifticonViewModel: GifticonViewModel

    private lateinit var couponListAdapter: CouponListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gifticonViewModel.couponList.observe(viewLifecycleOwner, Observer {
            binding.giftRv.post(Runnable {
                //couponListAdapter.setAvailableCouponsData(it.filter { x -> x.status == "AVAILABLE" })
                giftAdapter.submitList(it.filter { x -> x.status == "AVAILABLE" }.toList())

                /*if (it.filter { x -> x.status == "AVAILABLE" }.toList().isEmpty()) {
                    binding.llNoGifticon.visibility = View.VISIBLE
                } else {
                    binding.llNoGifticon.visibility = View.GONE
                }*/
            })
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        gifticonViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory())[GifticonViewModel::class.java]

        binding  = FragmentHomeAvailableBinding.inflate(inflater, container, false)

        val availableCouponList = gifticonViewModel.availableCouponList.value

        Log.d(TAG, "availableCouponList: $availableCouponList")

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
        }, availableCouponList ?: emptyList<Gifticon>())

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
                if (it.resultCode == Activity.RESULT_OK) {
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
                        val selectedCategory = gifticonViewModel.selectedCategory.value
                        // 현재 선택된 카테고리와 기프티콘의 카테고리가 다른 경우
                        if (selectedCategory?.id != updatedGifticon?.category?.id && selectedCategory?.id != 0L) {
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
                    //getAllGifticonListFromServer(0)
                }
            }

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
                Log.d(TAG, "updatedGifticonWithStatus: $updatedGifticonWithStatus")
                updatedGifticonWithStatus?.let { it1 -> gifticonViewModel.updateCoupon(it1) }
            }
        }

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
                                    intent.putExtra("gifticonId", gifticonId)
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
                                    /*gifticonList.remove(gifticon)
                                    giftAdapter.submitList(gifticonList.toList())
                                    giftAdapter.notifyDataSetChanged()*/
                                }
                            }
                        }
                    })
                }
            }
        }

        if (gifticonViewModel.availableCouponList.value == null) {
            binding.llNoGifticon.visibility = View.VISIBLE
        } else {
            binding.llNoGifticon.visibility = View.GONE
        }

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


    fun getAllGifticonListFromServer(page: Int) {
        gifticonViewModel.clearCouponList()
        Retrofit2Generator.create(requireActivity()).getAllGifticonList(size = 30, page = page).enqueue(object :
            Callback<GetGifticonListResponse> {
            override fun onResponse(call: Call<GetGifticonListResponse>, response: Response<GetGifticonListResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.dataList?.let { newList ->
                        // newList를 gifticonViewModel의 addCoupon함수를 이용해서 넣어준다.
                        // id가 높은 것 부터 넣어줘야 한다.
                        newList.forEach { gifticon ->
                            gifticonViewModel.addCoupon(gifticon)
                        }
                        //gifticonViewModel.sortCouponList(binding.tvSort.text.toString())

                        val allCouponList = gifticonViewModel.allCouponList.value
                        val availableCouponList = gifticonViewModel.availableCouponList.value
                        val usedCouponList = gifticonViewModel.usedCouponList.value

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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeAvailableFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeAvailableFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}