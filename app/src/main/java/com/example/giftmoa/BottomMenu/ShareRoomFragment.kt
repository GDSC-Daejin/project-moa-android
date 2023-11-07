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
import com.example.giftmoa.ShareRoomMenu.ShareRoomEditActivity
import com.example.giftmoa.ShareRoomMenu.ShareRoomReadActivity
import com.example.giftmoa.databinding.FragmentShareRoomBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private var shareRoomAllData = ArrayList<GetTeamData>()
    private var manager : LinearLayoutManager = LinearLayoutManager(activity)
    private val saveSharedPreference = SaveSharedPreference()
    private var identification = ""

    private var teamMembers = kotlin.collections.ArrayList<GetTeamMembers>() //데이터에 없어서 만든 더미데이터 나중에 나오면 바꾸기 TODO

    private val SERVER_URL = BuildConfig.server_URL
    private val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: MoaInterface = retrofit.create(MoaInterface::class.java)

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

        teamMembers.add(GetTeamMembers(0, "손민성", null))

        sAdapter!!.setItemClickListener(object : ShareRoomAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int, itemId: String) {
                val temp = shareRoomAllData[position]

                val intent = Intent(activity, ShareRoomReadActivity::class.java).apply {
                    putExtra("type", "READ")
                    putExtra("data", temp)
                }
                startActivity(intent)
            }
        })


       sBinding.shareCreateBtn.setOnClickListener {
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
                   .setView(view)
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
        CoroutineScope(Dispatchers.IO).launch {
            service.getMyShareRoom().enqueue(object : Callback<ShareRoomGetTeamData> {
                override fun onResponse(
                    call: Call<ShareRoomGetTeamData>,
                    response: Response<ShareRoomGetTeamData>
                ) {
                    if (response.isSuccessful) {
                        println("setSuc")

                        shareRoomAllData.clear()

                        for (i in response.body()!!.data.indices) {

                            shareRoomAllData.add(GetTeamData(
                                response.body()!!.data[i].id,
                                response.body()!!.data[i].teamCode,
                                response.body()!!.data[i].teamName,
                                response.body()!!.data[i].teamImage,
                                response.body()!!.data[i].teamLeaderNickName,
                                response.body()!!.data[i].teamMembers
                            ))
                        }
                        sAdapter!!.notifyDataSetChanged()

                    } else {
                        println("faafa")
                        Log.d("add", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<ShareRoomGetTeamData>, t: Throwable) {

                }

            })
        }
    }

    private fun setJoinShareRoomData(inviteCode : String) {
        val temp = TeamJoinData(inviteCode)
        CoroutineScope(Dispatchers.IO).launch {
            service.joinShareRoom(temp).enqueue(object : Callback<ShareRoomResponseData> {
                override fun onResponse(
                    call: Call<ShareRoomResponseData>,
                    response: Response<ShareRoomResponseData>
                ) {
                    if (response.isSuccessful) {
                        println("set join Suc")

                        shareRoomAllData.add(GetTeamData(
                            response.body()!!.data.teamId,
                            response.body()!!.data.teamCode,
                            response.body()!!.data.teamName,
                            null,
                            response.body()!!.data.teamLeaderNickName,
                            teamMembers
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
    }


    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val shareRoomData = it.data?.getSerializableExtra("data") as ShareRoomData

                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        setRoomData()
                        /*shareRoomAllData.add(shareRoomData)
                        sAdapter!!.notifyDataSetChanged()*/

                        /*CoroutineScope(Dispatchers.Main).launch {
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_content, ShareRoomFragment())
                                .commit()
                        }*/

                        //finish()
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