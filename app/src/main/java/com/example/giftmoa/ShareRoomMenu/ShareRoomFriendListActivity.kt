package com.example.giftmoa.ShareRoomMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.giftmoa.Adapter.MemberListAdapter
import com.example.giftmoa.Adapter.ShareRoomAdapter
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.GetMyTeamListResponse
import com.example.giftmoa.Data.GetTeamMembers
import com.example.giftmoa.Data.SaveSharedPreference
import com.example.giftmoa.Data.ShareRoomGetTeamData
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.ActivityShareRoomFriendListBinding
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

        sBinding.backArrow.setOnClickListener {
            this.finish()
        }
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
        Retrofit2Generator.create(this@ShareRoomFriendListActivity).getMyTeamList().enqueue(object : Callback<GetMyTeamListResponse>{
            override fun onResponse(
                call: Call<GetMyTeamListResponse>,
                response: Response<GetMyTeamListResponse>
            ) {
                if (response.isSuccessful) {
                    println("dssdsds")
                    memberList.clear()
                    val responseBody = response.body()
                    responseBody?.data?.let { newList ->
                        val currentPosition = memberList.size
                        newList.forEach { it ->
                            it.teamMembers?.forEach {
                                memberList.add(
                                    GetTeamMembers(
                                        it.id?.toInt()!!,
                                        it.nickname?.toString()!!,
                                        it.profileImageUrl
                                    )
                                )
                            }
                        }
                        //mAdapter?.notifyItemRangeInserted(currentPosition, newList.size)
                    }
                    mAdapter?.notifyDataSetChanged()
                    /*for (i in response.body()!!.data.indices) {
                        for (j in response.body()!!.data[i].teamMembers.indices) {
                            memberList.add(GetTeamMembers(
                                response.body()!!.data[i].teamMembers[j].id,
                                response.body()!!.data[i].teamMembers[j].nickname,
                                response.body()!!.data[i].teamMembers[j].profileImageUrl
                            ))
                        }
                    }*/

                    //mAdapter!!.notifyDataSetChanged()

                } else {
                    println("faafa")
                    Log.d("test", response.errorBody()?.string()!!)
                    Log.d("message", call.request().toString())
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<GetMyTeamListResponse>, t: Throwable) {
                Timber.tag("ERROR").e(t.message.toString())
            }
        })
    }
}