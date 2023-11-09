package com.example.giftmoa.ShareRoomMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.giftmoa.Adapter.MemberListAdapter
import com.example.giftmoa.Adapter.ShareRoomAdapter
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.GetTeamMembers
import com.example.giftmoa.Data.SaveSharedPreference
import com.example.giftmoa.Data.ShareRoomGetTeamData
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
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
        val saveSharedPreference = SaveSharedPreference()
        val token = saveSharedPreference.getToken(this).toString()
        val getExpireDate = saveSharedPreference.getExpireDate(this).toString()

        val SERVER_URL = BuildConfig.server_URL
        val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create())
        //.client(clientBuilder)

        //Authorization jwt토큰 로그인
        val interceptor = Interceptor { chain ->
            var newRequest: Request
            if (token != null && token != "") { // 토큰이 없는 경우
                // Authorization 헤더에 토큰 추가
                newRequest =
                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()

                val expireDate: Long = getExpireDate.toLong()
                if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                    //refresh 들어갈 곳
                    newRequest =
                        chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                    return@Interceptor chain.proceed(newRequest)
                }
            } else newRequest = chain.request()
            chain.proceed(newRequest)
        }
        val builder = OkHttpClient.Builder()
        builder.interceptors().add(interceptor)
        val client: OkHttpClient = builder.build()
        retrofit.client(client)
        val retrofit2: Retrofit = retrofit.build()
        val api = retrofit2.create(MoaInterface::class.java)
        CoroutineScope(Dispatchers.IO).launch {
            api.getMyShareRoom().enqueue(object : Callback<ShareRoomGetTeamData>{
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