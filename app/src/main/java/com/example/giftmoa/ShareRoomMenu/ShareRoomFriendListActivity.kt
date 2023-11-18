package com.example.giftmoa.ShareRoomMenu

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.giftmoa.Adapter.MemberListAdapter
import com.example.giftmoa.Adapter.ShareRoomAdapter
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.*
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.ActivityShareRoomFriendListBinding
import com.kakao.sdk.common.KakaoSdk.type
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
    private var temp = ArrayList<Team>()

    private var roomId:Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomFriendListBinding.inflate(layoutInflater)
        setContentView(sBinding.root)

        roomId = intent.getLongExtra("RoomId", 0L).toInt()

        initRecyclerView()
        setMemberList()

        sBinding.backArrow.setOnClickListener {
            this.finish()
        }
    }

    private fun initRecyclerView() {
        mAdapter = MemberListAdapter()
        mAdapter!!.shareRoomMemberListData = memberList
        sBinding.memberListRv.adapter = mAdapter
        sBinding.memberListRv.setHasFixedSize(true)
        sBinding.memberListRv.layoutManager = manager

    }

   /* private fun setMemberList() {
        val teamMembers: Array<Member>? = intent.getParcelableArrayExtra("teamMembers")?.map { it as Member }?.toTypedArray()
        for
            mAdapter?.notifyDataSetChanged()
        Log.d("memberList", "memberList" + memberList)
    }*/
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

                    responseBody?.data?.let {
                        temp.addAll(it)
                    }

                    val t = temp.filter { it.id?.toInt() == roomId }
                    for (i in t.indices) {
                        for (j in t[i].teamMembers?.indices!!) {
                            println("memberList"+t[i].teamMembers)
                            memberList.add(GetTeamMembers(
                                t[i].teamMembers?.get(j)?.id?.toInt()!!,
                                t[i].teamMembers?.get(j)?.nickname!!,
                                t[i].teamMembers?.get(j)?.profileImageUrl
                            ))
                        }
                    }
                    mAdapter?.notifyDataSetChanged()
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