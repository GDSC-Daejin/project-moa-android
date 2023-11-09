package com.example.giftmoa.ShareRoomMenu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.GetTeamData
import com.example.giftmoa.Data.SaveSharedPreference
import com.example.giftmoa.Data.ShareRoomGetTeamGifticonData
import com.example.giftmoa.Data.ShareRoomGifticon
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityShareRoomSettingBinding
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

class ShareRoomSettingActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivityShareRoomSettingBinding
    private var type = ""
    private var shareRoomData : GetTeamData? = null

    private var sharedGifticonAllData = ArrayList<ShareRoomGifticon>()


    private val SERVER_URL = BuildConfig.server_URL
    private val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: MoaInterface = retrofit.create(MoaInterface::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomSettingBinding.inflate(layoutInflater)
        setContentView(sBinding.root)

        type = intent.getStringExtra("type") as String
        shareRoomData = intent.getSerializableExtra("data") as GetTeamData

        setSharedGiftData()

        if (type == "SETTING_LEADER") {
            sBinding.shareLl1.visibility = View.VISIBLE
            sBinding.shareV1.visibility = View.VISIBLE
            sBinding.shareLl2.visibility = View.VISIBLE
            sBinding.shareV2.visibility = View.VISIBLE

            sBinding.shareSettingIv.setImageURI(shareRoomData!!.teamImage!!.toUri())
            sBinding.shareSettingRoomCode.text = shareRoomData!!.teamCode
            sBinding.shareSettingRoomName.text = shareRoomData!!.teamName
            sBinding.shareSettingRoomLeadername.text = shareRoomData!!.teamLeaderNickName
            sBinding.shareSettingRoomNumberOfPeople.text = shareRoomData!!.teamMembers.size.toString()
            sBinding.shareSettingRoomGifticonCount.text = sharedGifticonAllData.size.toString()


        } else {
            sBinding.shareLl1.visibility = View.GONE
            sBinding.shareV1.visibility = View.GONE
            sBinding.shareLl2.visibility = View.GONE
            sBinding.shareV2.visibility = View.GONE

            sBinding.shareSettingRoomCode.text = shareRoomData!!.teamCode
            sBinding.shareSettingRoomLeadername.text = shareRoomData!!.teamLeaderNickName
            sBinding.shareSettingRoomNumberOfPeople.text = shareRoomData!!.teamMembers.size.toString()
            sBinding.shareSettingRoomGifticonCount.text = sharedGifticonAllData.size.toString()
        }

        sBinding.backArrow.setOnClickListener {
            this.finish()
        }

        sBinding.shareLl5.setOnClickListener {
            val intent = Intent(this@ShareRoomSettingActivity, ShareRoomFriendListActivity::class.java).apply {
                putExtra("RoomId", shareRoomData!!.id)
            }
            startActivity(intent)
        }
    }


    private fun setSharedGiftData() {
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
            api.getMyShareRoomGifticon(shareRoomData!!.id, 0, 10).enqueue(object :
                Callback<ShareRoomGetTeamGifticonData> {
                override fun onResponse(
                    call: Call<ShareRoomGetTeamGifticonData>,
                    response: Response<ShareRoomGetTeamGifticonData>
                ) {
                    if (response.isSuccessful) {
                        println("est gift")

                        sharedGifticonAllData.clear()

                        for (i in response.body()!!.data.data.indices) {

                            sharedGifticonAllData.add(
                                ShareRoomGifticon(
                                response.body()!!.data.data[i].gifticonId,
                                response.body()!!.data.data[i].name,
                                response.body()!!.data.data[i].barcodeNumber,
                                response.body()!!.data.data[i].gifticonImagePath,
                                response.body()!!.data.data[i].exchangePlace,
                                response.body()!!.data.data[i].dueDate,
                                response.body()!!.data.data[i].gifticonType,
                                response.body()!!.data.data[i].orderNumber,
                                response.body()!!.data.data[i].status,
                                response.body()!!.data.data[i].usedDate,
                                response.body()!!.data.data[i].author,
                                response.body()!!.data.data[i].category,
                                response.body()!!.data.data[i].gifticonMoney,
                                    false
                            ))
                        }
                        /*totalCount = response.body()!!.data.totalCount
                        nextPage = response.body()!!.data.nextPage

                        shareGiftAdapter!!.notifyDataSetChanged()*/

                    } else {
                        println("faafa")
                        Log.d("add", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<ShareRoomGetTeamGifticonData>, t: Throwable) {

                }

            })
        }
    }
}