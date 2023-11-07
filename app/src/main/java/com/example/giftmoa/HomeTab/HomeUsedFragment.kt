package com.example.giftmoa.HomeTab

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.BottomMenu.CouponFragment
import com.example.giftmoa.BottomMenu.GifticonDataReceiver
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
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.FragmentHomeUsedBinding
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
 * Use the [HomeUsedFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeUsedFragment : Fragment(), CategoryListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentHomeUsedBinding
    private val TAG = "HomeUsedFragment"

    private lateinit var giftAdapter: GifticonListAdapter

    var gifticonList = mutableListOf<Gifticon>()

    private var categoryList = mutableListOf<CategoryItem>()

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
    ): View? {
        binding  = FragmentHomeUsedBinding.inflate(inflater, container, false)

        giftAdapter = GifticonListAdapter({ gifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)
        }, gifticonList?: emptyList<Gifticon>())

        getCategoryListFromServer()

        initHomeRecyclerView()

        giftAdapter.itemLongClickListener = object : GifticonListAdapter.OnItemLongClickListener {
            override fun onItemLongClick(position: Int) {
                // 길게 클릭한 아이템에 대한 처리 로직을 여기에 작성
                val gifticon = giftAdapter.currentList[position]

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
                                    val intent = Intent(requireActivity(), ManualRegistrationActivity::class.java)
                                    intent.putExtra("gifticonId", gifticon.id)
                                    intent.putExtra("isEdit", true)
                                    startActivity(intent)
                                    /*manualAddGifticonResult.launch(
                                        Intent(
                                            requireActivity(),
                                            ManualRegistrationActivity::class.java
                                        ).apply {
                                            putExtra("gifticon", gifticon)
                                            putExtra("isEdit", true)
                                        })*/
                                }

                                "삭제하기" -> {
                                    gifticonList.remove(gifticon)
                                    giftAdapter.submitList(gifticonList)
                                    giftAdapter.notifyDataSetChanged()
                                }
                            }
                        }
                    })
                }
            }
        }

        binding.ivAddCategory.setOnClickListener {
            showCategoryBottomSheet(categoryList)
        }

        binding.tvSort.setOnClickListener {
            val bottomSheet = SortBottomSheet()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : SortBottomSheet.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        getBottomSheetData = value
                        when (value) {
                            "최신 순" -> {
                                binding.tvSort.text = "최신 순"
                            }

                            "마감임박 순" -> {
                                binding.tvSort.text = "마감임박 순"
                            }
                        }
                    }
                })
            }
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

    private fun getUsedGifticonListFromServer(page: Int) {
        Retrofit2Generator.create(requireActivity()).getUsedGifticonList(size = 10, page = page).enqueue(object :
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
                        giftAdapter.submitList(gifticonList.toList())
                        Log.d(TAG, "gifticonList.size: ${gifticonList.size}")
                        if (gifticonList.size > 0) {
                            binding.tvNoGifticon.visibility = View.GONE
                        } else {
                            binding.tvNoGifticon.visibility = View.VISIBLE
                        }
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

    private fun getCategoryListFromServer() {
        Retrofit2Generator.create(requireActivity()).getCategoryList().enqueue(object :
            Callback<GetCategoryListResponse> {
            override fun onResponse(call: Call<GetCategoryListResponse>, response: Response<GetCategoryListResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val resposeBody = responseBody.data

                        if (resposeBody != null) {
                            for (category in resposeBody) {
                                if (category.categoryName == null) {
                                    continue
                                }
                                if (category.categoryName == "미분류") {
                                    // 항상 가장 마지막에 미분류 카테고리가 추가되도록
                                    // categoryList의 맨 뒤에 추가
                                    categoryList.add(category)
                                    continue
                                }
                                val chip = category.categoryName!!.let { createNewChip(it) }

                                // 마지막 Chip 뷰의 인덱스를 계산
                                val lastChildIndex = binding.chipGroupCategory.childCount - 1

                                // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                                // 현재 Chip을 바로 그 앞에 추가
                                if (lastChildIndex >= 0) {
                                    binding.chipGroupCategory.addView(chip, lastChildIndex)
                                } else {
                                    // ChipGroup에 자식이 없는 경우, 그냥 추가
                                    binding.chipGroupCategory.addView(chip)
                                }

                                categoryList.add(category)
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetCategoryListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        chip.setOnCloseIconClickListener {
            // 닫기 아이콘 클릭 시 Chip 제거
            (it.parent as? ViewGroup)?.removeView(it)
        }
        return chip
    }

    private fun deleteChip(text: String) {
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val childView = binding.chipGroupCategory.getChildAt(i)
            if (childView is Chip) {
                val chip = childView as Chip
                if (chip.text == text) {
                    binding.chipGroupCategory.removeView(chip)
                    break
                }
            }
        }
    }

    override fun onCategoryUpdated(categoryName: String) {
        val categoryRequest = AddCategoryRequest(categoryName)
        // Retrofit을 이용해서 서버에 카테고리 추가 요청
        // 서버에서 카테고리 추가가 완료되면 아래 코드를 실행
        Retrofit2Generator.create(requireActivity()).addCategory(categoryRequest).enqueue(object :
            Callback<AddCategoryResponse> {
            override fun onResponse(call: Call<AddCategoryResponse>, response: Response<AddCategoryResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    Log.d(TAG, "responseBody category: $responseBody")
                    if (responseBody != null) {
                        val category = responseBody.data
                        // categoryList에 추가
                        if (category != null) {
                            val chip = category.categoryName?.let { createNewChip(it) }
                            val positionToInsert = binding.chipGroupCategory.childCount - 1
                            binding.chipGroupCategory.addView(chip, positionToInsert)
                            categoryList.add(category)
                        }
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AddCategoryResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    override fun onCategoryDeleted(categoryName: String) {
        var categoryId: Long? = null
        Log.d(TAG, "onCategoryDeleted: $categoryName")
        for (category in categoryList) {
            if (categoryName == category.categoryName) {
                // categoryList에서 해당 카테고리의 id 값 가져오기
                categoryId = category.id
                // categoryList에서 해당 카테고리 삭제
                categoryList.remove(category)
                deleteChip(categoryName)
                Log.d(TAG, "onCategoryDeleted: $categoryId")
                Log.d(TAG, "onCategoryDeleted: $categoryList")
                break
            }
        }
    }

    private fun showCategoryBottomSheet(categoryList: List<CategoryItem>) {
        val categoryBottomSheet = CategoryBottomSheet(categoryList, this)
        categoryBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        categoryBottomSheet.show(requireActivity().supportFragmentManager, categoryBottomSheet.tag)
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")

        val currentPage = 0

        getUsedGifticonListFromServer(currentPage)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeUsedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeUsedFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}