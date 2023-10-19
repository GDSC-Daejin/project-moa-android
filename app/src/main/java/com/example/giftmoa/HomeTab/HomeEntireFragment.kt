package com.example.giftmoa.HomeTab

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.Adapter.HomeGiftAdapter
import com.example.giftmoa.BottomSheetFragment.BottomSheetFragment
import com.example.giftmoa.CouponTab.CouponAutoAddActivity
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.GifticonRegistrationActivity
import com.example.giftmoa.R
import com.example.giftmoa.databinding.FragmentHomeEntireBinding

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
    private var giftAdapter : HomeGiftAdapter? = null
    private var giftAllData = ArrayList<GiftData>()
    private var gridManager = GridLayoutManager(activity, 2)
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
        initHomeRecyclerView()

        binding.giftAdd.setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        getBottomSheetData = value
                        //myViewModel.postCheckSearchFilter(getBottomSheetData)
                        when (value) {
                            "자동 등록" -> {
                                //val intent = Intent(requireActivity(), CouponAutoAddActivity::class.java)
                                val intent = Intent(requireActivity(), GifticonRegistrationActivity::class.java)
                                startActivity(intent)
                            }

                            "수동 등록" -> {

                            }
                        }
                    }
                })
            }
        }


        return binding.root
    }

    private fun initHomeRecyclerView() {
        setGiftData()

        giftAdapter = HomeGiftAdapter()
        giftAdapter!!.giftItemData = giftAllData
        binding.giftRv.adapter = giftAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        binding.giftRv.setHasFixedSize(true)
        binding.giftRv.layoutManager = gridManager

    }

    private fun setGiftData() {
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        giftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))

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