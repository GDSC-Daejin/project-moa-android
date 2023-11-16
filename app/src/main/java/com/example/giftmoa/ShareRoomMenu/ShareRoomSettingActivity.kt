package com.example.giftmoa.ShareRoomMenu

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.*
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
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
    private var shareRoomData : Team? = null

    private var sharedGifticonAllData = ArrayList<TeamGifticon>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomSettingBinding.inflate(layoutInflater)
        setContentView(sBinding.root)

        type = intent.getStringExtra("type") as String
        shareRoomData = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("data", Team::class.java)
        } else {
            intent.getParcelableExtra("data")
        }

        setSharedGiftData()

        if (type == "SETTING_LEADER") {
            sBinding.shareLl1.visibility = View.VISIBLE
            sBinding.shareV1.visibility = View.VISIBLE
            sBinding.shareLl2.visibility = View.VISIBLE
            sBinding.shareV2.visibility = View.VISIBLE

            sBinding.shareSettingIv.setImageURI(shareRoomData!!.teamImage!!.toUri())
            sBinding.shareSettingRoomCode.text = shareRoomData!!.teamCode
            sBinding.shareSettingRoomName.text = shareRoomData!!.teamName
            sBinding.shareSettingRoomLeadername.text = shareRoomData?.teamLeaderNickname
            sBinding.shareSettingRoomNumberOfPeople.text = shareRoomData!!.teamMembers?.size.toString()
            sBinding.shareSettingRoomGifticonCount.text = sharedGifticonAllData.size.toString()

            sBinding.shareSettingRoomDelete.setOnClickListener {
                deleteShareRoom(shareRoomData?.id?.toInt()!!)
            }

        } else {
            sBinding.shareLl1.visibility = View.GONE
            sBinding.shareV1.visibility = View.GONE
            sBinding.shareLl2.visibility = View.GONE
            sBinding.shareV2.visibility = View.GONE

            sBinding.shareSettingRoomCode.text = shareRoomData!!.teamCode
            sBinding.shareSettingRoomLeadername.text = shareRoomData!!.teamLeaderNickname
            sBinding.shareSettingRoomNumberOfPeople.text = shareRoomData!!.teamMembers?.size.toString()
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
        CoroutineScope(Dispatchers.IO).launch {
            Retrofit2Generator.create(this@ShareRoomSettingActivity).getTeamGifticonList(shareRoomData!!.id!!.toLong(), 0, 10).enqueue(object :
                Callback<GetTeamGifticonListResponse> {
                override fun onResponse(
                    call: Call<GetTeamGifticonListResponse>,
                    response: Response<GetTeamGifticonListResponse>
                ) {
                    if (response.isSuccessful) {
                        println("est gift")

                        sharedGifticonAllData.clear()

                        val responseBody = response.body()
                        responseBody?.data?.dataList?.let { newList ->

                            Log.d("TAG", "Setting: newList = $newList")
                            // 새로운 데이터를 리스트에 추가합니다.
                            val currentPosition = sharedGifticonAllData.size
                            sharedGifticonAllData.addAll(newList)

                            // 어댑터에 데이터가 변경되었음을 알립니다.
                            // DiffUtil.Callback 사용을 위한 submitList는 비동기 처리를 하므로 리스트의 사본을 넘깁니다.
                            //sAdapter.submitList(shareRoomDetailList.toList())
                            // 또는
                            // DiffUtil을 사용하지 않는 경우
                            //shareGiftAdapter?.notifyItemRangeInserted(currentPosition, newList.size)
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

                override fun onFailure(call: Call<GetTeamGifticonListResponse>, t: Throwable) {
                        Log.e("onFailure", t.message.toString())
                }
            })
        }
    }

    private fun deleteShareRoom(teamId : Int) {
        Retrofit2Generator.create(this@ShareRoomSettingActivity).deleteShareRoom(teamId).enqueue(object : Callback<ShareRoomResponseData> {
            override fun onResponse(
                call: Call<ShareRoomResponseData>,
                response: Response<ShareRoomResponseData>
            ) {
                if (response.isSuccessful) {
                    Log.d("delete","Delete ShareRoom")
                    val intent = Intent(this@ShareRoomSettingActivity, ShareRoomReadActivity::class.java).apply {
                        putExtra("flag", 0)
                    }
                    setResult(RESULT_OK, intent)
                    this@ShareRoomSettingActivity.finish()
                } else {
                    Log.e("delete Error", response.errorBody()?.string()!!)
                    println("faafa")
                    Log.d("message", call.request().toString())
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<ShareRoomResponseData>, t: Throwable) {
                Log.e("onFailure", t.message.toString())
            }

        })
    }
}