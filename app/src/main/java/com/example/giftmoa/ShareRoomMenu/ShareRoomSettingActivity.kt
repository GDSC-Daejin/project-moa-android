package com.example.giftmoa.ShareRoomMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.GetTeamData
import com.example.giftmoa.Data.ShareRoomGetTeamGifticonData
import com.example.giftmoa.Data.ShareRoomGifticon
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityShareRoomSettingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    }


    private fun setSharedGiftData() {
        CoroutineScope(Dispatchers.IO).launch {
            service.getMyShareRoomGifticon(shareRoomData!!.id, 0, 10).enqueue(object :
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
                            )
                            )
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