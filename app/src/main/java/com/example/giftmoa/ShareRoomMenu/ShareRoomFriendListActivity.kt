package com.example.giftmoa.ShareRoomMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.giftmoa.Adapter.MemberListAdapter
import com.example.giftmoa.Adapter.ShareRoomAdapter
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.GetTeamMembers
import com.example.giftmoa.Data.ShareRoomGetTeamData
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityShareRoomFriendListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class ShareRoomFriendListActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivityShareRoomFriendListBinding
    private var mAdapter : MemberListAdapter? = null
    private var memberList = ArrayList<GetTeamMembers>()
    private var manager : LinearLayoutManager = LinearLayoutManager(this)

    private val SERVER_URL = BuildConfig.server_URL
    private val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: MoaInterface = retrofit.create(MoaInterface::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomFriendListBinding.inflate(layoutInflater)
        setContentView(sBinding.root)

        initRecyclerView()
    }

    private fun initRecyclerView() {
        setMemberList()
        mAdapter = MemberListAdapter()
        mAdapter!!.shareRoomMemberListData = memberList
        sBinding.memberListRv.adapter = mAdapter
        sBinding.memberListRv.setHasFixedSize(true)
        sBinding.memberListRv.layoutManager = manager
    }

    private fun setMemberList() {
        CoroutineScope(Dispatchers.IO).launch {
            service.getMyShareRoom().enqueue(object : Callback<ShareRoomGetTeamData>{
                override fun onResponse(
                    call: Call<ShareRoomGetTeamData>,
                    response: Response<ShareRoomGetTeamData>
                ) {
                    if (response.isSuccessful) {
                        println("dssdsds")
                        memberList.clear()
                        for (i in response.body()!!.data.indices) {
                            for (j in response.body()!!.data[i].teamMembers.indices) {
                                memberList.add(GetTeamMembers(
                                    response.body()!!.data[i].teamMembers[j].id,
                                    response.body()!!.data[i].teamMembers[j].nickname,
                                    response.body()!!.data[i].teamMembers[j].profileImageUrl
                                ))
                            }
                        }

                        mAdapter!!.notifyDataSetChanged()

                    } else {
                        println("faafa")
                        Log.d("test", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<ShareRoomGetTeamData>, t: Throwable) {
                    Timber.tag("ERROR").e(t.message.toString())
                }
            })
        }
    }
}