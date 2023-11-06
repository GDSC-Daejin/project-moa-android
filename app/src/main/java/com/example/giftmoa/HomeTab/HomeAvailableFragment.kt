package com.example.giftmoa.HomeTab

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.BottomMenu.CouponFragment
import com.example.giftmoa.BottomMenu.GifticonDataReceiver
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.databinding.FragmentHomeAvailableBinding
import com.google.gson.Gson

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeAvailableFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeAvailableFragment : Fragment(), GifticonDataReceiver {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding : FragmentHomeAvailableBinding
    private val TAG = "HomeEntireFragment"

    private lateinit var giftAdapter : GifticonListAdapter

    private lateinit var couponViewModel: CouponViewModel

    private var gridManager = GridLayoutManager(activity, 2, GridLayoutManager.VERTICAL, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*couponViewModel.gifticonList.observe(viewLifecycleOwner, Observer{
            binding.giftRv.post(Runnable { giftAdapter.setAvailableGifticonList(it.filter { x -> x.status == "AVAILABLE" }) })
        })*/
        couponViewModel.availableGifticonList.observe(viewLifecycleOwner, Observer { availableGifticons ->
            // RecyclerView의 어댑터에 "AVAILABLE" 상태인 기프티콘 목록만 설정
            giftAdapter.submitList(availableGifticons.filter { it.status == "AVAILABLE" })
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        couponViewModel =
            ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory()).get(
                CouponViewModel::class.java
            )

        binding  = FragmentHomeAvailableBinding.inflate(inflater, container, false)

        var gifticonList = couponViewModel.allGifticonList.value

        giftAdapter = GifticonListAdapter({ gifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)
        }, gifticonList?: emptyList<Gifticon>())

        //getJsonData()

        initHomeRecyclerView()

        /*couponViewModel.availableGifticonList.observe(viewLifecycleOwner) {
            Log.d(TAG, "availableGifticonList: ${it}")
            giftAdapter.submitList(it)
            giftAdapter.notifyDataSetChanged()
        }*/

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


    /*private fun getJsonData() {
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
    }*/

    override fun receiveGifticonData(gifticonList: List<GifticonDetailItem>) {
        Log.d(TAG, "receiveGifticonData: ${gifticonList}")
        /*this.gifticonList = gifticonList as MutableList<GifticonDetailItem>
        giftAdapter.submitList(gifticonList)
        giftAdapter.notifyDataSetChanged()*/
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