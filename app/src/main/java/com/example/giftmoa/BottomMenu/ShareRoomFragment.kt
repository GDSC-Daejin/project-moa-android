package com.example.giftmoa.BottomMenu

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.giftmoa.Adapter.ShareRoomAdapter
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.*
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.ShareRoomMenu.ShareRoomEditActivity
import com.example.giftmoa.ShareRoomMenu.ShareRoomReadActivity
import com.example.giftmoa.databinding.FragmentShareRoomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShareRoomFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShareRoomFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var sBinding : FragmentShareRoomBinding
    private var sAdapter : ShareRoomAdapter? = null
    private var shareRoomAllData = ArrayList<Team>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private val saveSharedPreference = SaveSharedPreference()
    private var identification = ""


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
        sBinding = FragmentShareRoomBinding.inflate(inflater, container, false)

        initRoomRecyclerView()

        identification = saveSharedPreference.getName(activity).toString()


        sAdapter!!.setItemClickListener(object : ShareRoomAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int, itemId: String?) {
                val temp = shareRoomAllData[position]

                val intent = Intent(activity, ShareRoomReadActivity::class.java).apply {
                    putExtra("type", "READ")
                    putExtra("data", temp)
                }
                startActivity(intent)
            }
        })


        sBinding.shareCreateBtn.setOnClickListener {
            println(identification)
            println(saveSharedPreference.getToken(requireActivity()).toString())

            val layoutInflater = LayoutInflater.from(activity)
            val view = layoutInflater.inflate(R.layout.dialog_layout, null)
            val alertDialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setView(view)
                .create()

            val dialogCreate = view.findViewById<TextView>(R.id.dialog_create)
            val dialogEntrance = view.findViewById<TextView>(R.id.dialog_entrance)

            dialogCreate.setOnClickListener {
                val intent = Intent(requireActivity(), ShareRoomEditActivity::class.java).apply {
                    putExtra("type", "ADD")
                }
                requestActivity.launch(intent)
                alertDialog.dismiss()
            }

            dialogEntrance.setOnClickListener {
                alertDialog.dismiss()
                val layoutInflater1 = LayoutInflater.from(activity)
                val view1 = layoutInflater1.inflate(R.layout.dialog_join_layout, null)
                val alertDialog1 = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                    .setView(view1)
                    .create()

                val dialogET = view1.findViewById<EditText>(R.id.join_et)
                val dialogCancel = view1.findViewById<Button>(R.id.join_cancel)
                val dialogOk = view1.findViewById<Button>(R.id.join_ok)

                dialogCancel.setOnClickListener {
                    alertDialog1.dismiss()
                }

                dialogOk.setOnClickListener {
                    val inviteCode = dialogET.text.toString()
                    setJoinShareRoomData(inviteCode)
                    alertDialog1.dismiss()
                }
                alertDialog1.show()
            }
            alertDialog.show()
        }

        /*
        sBinding.shareEntranceCodeBtn.setOnClickListener {
            //사용할 곳
            val layoutInflater = LayoutInflater.from(activity)
            val view = layoutInflater.inflate(R.layout.dialog_layout, null)
            val alertDialog = AlertDialog.Builder(activity, R.style.CustomAlertDialog)
                .setView(view)
                .create()
            val dialogBtn = view.findViewById<TextView>(R.id.dialog_entrance_btn)
            val dialogET = view.findViewById<EditText>(R.id.dialog_et)

            dialogBtn.setOnClickListener {
                println(dialogET.text)
                alertDialog.dismiss()
            }

            alertDialog.show()
        }*/

        return sBinding.root
    }

    private fun initRoomRecyclerView() {
        setRoomData()
        sAdapter = ShareRoomAdapter()
        sAdapter!!.shareRoomItemData = shareRoomAllData
        sBinding.shareRoomRv.adapter = sAdapter
        sBinding.shareRoomRv.setHasFixedSize(true)
        sBinding.shareRoomRv.layoutManager = manager
    }

    private fun setRoomData() {
        Retrofit2Generator.create(requireActivity()).getMyTeamList().enqueue(object : Callback<GetMyTeamListResponse> {
            override fun onResponse(
                call: Call<GetMyTeamListResponse>,
                response: Response<GetMyTeamListResponse>
            ) {
                if (response.isSuccessful) {
                    Log.e("TEST", "setSucccccc")
                    println("setSuc")
                    shareRoomAllData.clear()
                    val responseBody = response.body()
                    responseBody?.data?.let { newList ->

                        Log.d("TAG", "Room: newList = $newList")
                        // 새로운 데이터를 리스트에 추가합니다.
                        val currentPosition = shareRoomAllData.size
                        shareRoomAllData.addAll(newList)
                        println(shareRoomAllData)
                        println(currentPosition)
                        println(newList.size)

                        // 어댑터에 데이터가 변경되었음을 알립니다.
                        // DiffUtil.Callback 사용을 위한 submitList는 비동기 처리를 하므로 리스트의 사본을 넘깁니다.
                        //sAdapter.submitList(shareRoomDetailList.toList())
                        // 또는
                        // DiffUtil을 사용하지 않는 경우
                        //sAdapter!!.notifyItemRangeInserted(currentPosition, newList.size)
                    }
                    sAdapter!!.notifyDataSetChanged()

                } else {
                    println("faafa")
                    Log.d("add", response.errorBody()?.string()!!)
                    Log.d("message", call.request().toString())
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<GetMyTeamListResponse>, t: Throwable) {

            }

        })
    }

    private fun setJoinShareRoomData(inviteCode : String) {
        val temp = TeamJoinData(inviteCode)
        Retrofit2Generator.create(requireActivity()).joinShareRoom(temp).enqueue(object : Callback<ShareRoomResponseData> {
            override fun onResponse(
                call: Call<ShareRoomResponseData>,
                response: Response<ShareRoomResponseData>
            ) {
                if (response.isSuccessful) {
                    println("set join Suc")

                    shareRoomAllData.add(Team(
                        response.body()!!.data.teamId.toLong(),
                        response.body()!!.data.teamCode,
                        response.body()!!.data.teamName,
                        response.body()!!.data.teamImage,
                        response.body()!!.data.teamLeaderNickName,
                        null
                    ))

                    sAdapter!!.notifyDataSetChanged()

                } else {
                    println("faafa")
                    Log.d("add", response.errorBody()?.string()!!)
                    Log.d("message", call.request().toString())
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<ShareRoomResponseData>, t: Throwable) {

            }

        })
    }


    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val shareRoomData = it.data?.getSerializableExtra("data") as ShareRoomData

                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        setRoomData()
                    }
                    //수정 테스트 해보기 todo//edit
                    1 -> {
                        /*oldFragment = HomeFragment()
                        oldTAG = TAG_HOME
                        //setToolbarView(TAG_HOME, oldTAG)
                        setFragment(TAG_HOME, HomeFragment())

                        mBinding.bottomNavigationView.selectedItemId = R.id.navigation_home

                        CoroutineScope(Dispatchers.Main).launch {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_content, HomeFragment())
                                .commit()*/
                    }

                    //finish()
                }
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
         * @return A new instance of fragment ShareRoomFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShareRoomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}