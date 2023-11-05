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
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.BottomSheetFragment.GifticonEditDeleteBottomSheet
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.CouponTab.ManualRegistrationActivity
import com.example.giftmoa.databinding.FragmentHomeEntireBinding
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

    private lateinit var giftAdapter: GifticonListAdapter

    var gifticonList = mutableListOf<GifticonDetailItem>()

    private var giftAllData = ArrayList<GiftData>()
    private var gridManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)
    private var getBottomSheetData = ""

    private lateinit var manualAddGifticonResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        Log.d(TAG, "onCreate: ")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeEntireBinding.inflate(inflater, container, false)

        Log.d(TAG, "onCreateView: ")


        giftAdapter = GifticonListAdapter({ gifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)
        }, requireActivity())

        manualAddGifticonResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val updatedGifticon = if (Build.VERSION.SDK_INT >= 33) {
                    it.data?.getParcelableExtra("updatedGifticon", GifticonDetailItem::class.java)
                } else {
                    it.data?.getParcelableExtra<GifticonDetailItem>("updatedGifticon")
                }
                val isEdit = it.data?.getBooleanExtra("isEdit", false)
                Log.d(TAG, "updatedGifticon: $updatedGifticon")
                Log.d(TAG, "isEdit: $isEdit")
                if (isEdit == true) {
                    updatedGifticon?.let { it1 -> updateGifticon(it1) }
                } else {
                    // 기프티콘 추가
                    updatedGifticon?.let { it1 -> addGifticon(it1) }
                }
            }
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
                                    /*val intent = Intent(requireActivity(), ManualRegistrationActivity::class.java)
                                    intent.putExtra("gifticon", gifticon)
                                    intent.putExtra("isEdit", true)
                                    startActivity(intent)*/
                                    manualAddGifticonResult.launch(Intent(requireActivity(), ManualRegistrationActivity::class.java).apply {
                                        putExtra("gifticon", gifticon)
                                        putExtra("isEdit", true)
                                    })
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

    fun updateGifticon(updatedGifticon: GifticonDetailItem) {
        for (i in 0 until gifticonList.size) {
            if (gifticonList[i].id == updatedGifticon.id) {
                gifticonList[i] = updatedGifticon
                break
            }
        }
        giftAdapter.submitList(gifticonList)
        giftAdapter.notifyItemChanged(gifticonList.indexOf(updatedGifticon))
    }

    fun addGifticon(newGifticon: GifticonDetailItem) {
        gifticonList.add(newGifticon)
        giftAdapter.submitList(gifticonList)
        giftAdapter.notifyItemInserted(0)

        Log.d(TAG, "addGifticon: $gifticonList")
        Log.d(TAG, "newGifticon: ${newGifticon}")
    }

    fun sortByRecent() {
        if(!::giftAdapter.isInitialized) {
            Log.d(TAG, "sortByRecent: ")
            return
        }
        // 최신순은 기본으로 정렬되어 있으므로 아무것도 하지 않음
        gifticonList.sortBy { it.id }
        giftAdapter.submitList(gifticonList)
        giftAdapter.notifyDataSetChanged()
    }

    fun sortByDueDate() {
        if(!::giftAdapter.isInitialized) {
            Log.d(TAG, "sortByDueDate: ")
            return
        }
        // 유효기간이 얼마 남지 않은 순으로 정렬
        gifticonList.sortBy { it.dueDate }
        giftAdapter.submitList(gifticonList)
        giftAdapter.notifyDataSetChanged()
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
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
}