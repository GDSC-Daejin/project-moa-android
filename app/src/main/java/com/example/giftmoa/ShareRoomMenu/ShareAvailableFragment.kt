package com.example.giftmoa.ShareRoomMenu

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.*
import com.example.giftmoa.Adapter.ShareRoomGifticonAdapter
import com.example.giftmoa.BottomMenu.CategoryListener
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BottomSheetFragment.SortBottomSheet
import com.example.giftmoa.Data.*
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.databinding.FragmentShareAvailableBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShareAvailableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShareAvailableFragment : Fragment(), CategoryListener{
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentShareAvailableBinding

    private val SERVER_URL = BuildConfig.server_URL
    private val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: MoaInterface = retrofit.create(MoaInterface::class.java)

    private var giftAdapter: ShareRoomGifticonAdapter? = null

    var gifticonList = ArrayList<ShareRoomGifticon>()

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
        binding = FragmentShareAvailableBinding.inflate(inflater, container, false)
        getCategoryListFromServer()
        initShareEntireRecyclerView()

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

                                CoroutineScope(Dispatchers.IO).launch {
                                    val page = 0
                                    Retrofit2Generator.create(requireActivity()).getRecentGifticonList(size = 30, page = page).enqueue(object :
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
                                                    gifticonList.addAll(listOf(newList as ShareRoomGifticon))

                                                    giftAdapter!!.notifyDataSetChanged()
                                                }
                                            } else {
                                                Log.e("ERROR", "Error: ${response.errorBody()?.string()}")
                                            }
                                        }

                                        override fun onFailure(call: Call<GetGifticonListResponse>, t: Throwable) {
                                            Log.e("ERROR", "Retrofit onFailure: ", t)
                                        }
                                    })
                                }
                            }

                            "마감임박 순" -> {
                                binding.tvSort.text = "마감임박 순"
                                gifticonList.sortBy { it.dueDate }
                                giftAdapter!!.notifyDataSetChanged()
                            }
                        }
                    }
                })
            }
        }

        return binding.root
    }

    private fun initShareEntireRecyclerView() {
        getAvailableListFromServer(0)
        println("available")
        binding.giftRv.apply {

            giftAdapter = ShareRoomGifticonAdapter()
            adapter = giftAdapter
            giftAdapter!!.shareRoomGifticonItemData = gifticonList
            layoutManager = gridManager
            binding.giftRv.addItemDecoration(
                GridSpacingItemDecoration(spanCount = 2, spacing = 10f.fromDpToPx())
            )
        }
    }

    private fun Float.fromDpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun showCategoryBottomSheet(categoryList: List<CategoryItem>) {
        val categoryBottomSheet = CategoryBottomSheet(categoryList, this@ShareAvailableFragment)
        categoryBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        categoryBottomSheet.show(requireActivity().supportFragmentManager, categoryBottomSheet.tag)
    }


    private fun getAvailableListFromServer(page: Int) {
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
                        var temp = ArrayList<Gifticon>()
                        for (i in responseBody.data.dataList.indices) {
                            temp = responseBody.data.dataList.filter { it.status == "AVAILABLE" } as ArrayList<Gifticon>
                        }

                        gifticonList.addAll(listOf(temp as ShareRoomGifticon))

                        giftAdapter!!.notifyDataSetChanged()
                    }
                } else {
                    Log.e("ERROR", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonListResponse>, t: Throwable) {
                Log.e("ERROR", "Retrofit onFailure: ", t)
            }
        })
    }

    private fun getCategoryListFromServer() {
        Retrofit2Generator.create(requireActivity()).getCategoryList().enqueue(object :
            Callback<GetCategoryListResponse> {
            override fun onResponse(call: Call<GetCategoryListResponse>, response: Response<GetCategoryListResponse>) {
                if (response.isSuccessful) {
                    Log.d("Success", "Retrofit onResponse: ${response.body()}")
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
                                    binding.chipUnclassified.visibility = View.VISIBLE
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
                    Log.e("ERROR", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetCategoryListResponse>, t: Throwable) {
                Log.e("ERROR", "Retrofit onFailure: ", t)
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
        Retrofit2Generator.create(requireActivity()).addCategory(categoryRequest).enqueue(object : Callback<AddCategoryResponse> {
            override fun onResponse(call: Call<AddCategoryResponse>, response: Response<AddCategoryResponse>) {
                if (response.isSuccessful) {
                    Log.d("TAG", "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    Log.d("TAG", "responseBody category: $responseBody")
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
                    Log.e("TAG", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AddCategoryResponse>, t: Throwable) {
                Log.e("TAG", "Retrofit onFailure: ", t)
            }
        })
    }

    override fun onCategoryDeleted(categoryName: String) {
        var categoryId: Long? = null
        Log.d("TAG", "onCategoryDeleted: $categoryName")
        for (category in categoryList) {
            if (categoryName == category.categoryName) {
                // categoryList에서 해당 카테고리의 id 값 가져오기
                categoryId = category.id
                // categoryList에서 해당 카테고리 삭제
                categoryList.remove(category)
                deleteChip(categoryName)
                Log.d("TAG", "onCategoryDeleted: $categoryId")
                Log.d("TAG", "onCategoryDeleted: $categoryList")
                break
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
         * @return A new instance of fragment ShareAvailableFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShareAvailableFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }


}