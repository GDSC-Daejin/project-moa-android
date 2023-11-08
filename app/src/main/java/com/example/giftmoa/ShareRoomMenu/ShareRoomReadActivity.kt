package com.example.giftmoa.ShareRoomMenu

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.Adapter.HomeGiftAdapter
import com.example.giftmoa.Adapter.HomeUsedGiftAdapter
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.*
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityShareRoomReadBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShareRoomReadActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivityShareRoomReadBinding
    private var gridManager = GridLayoutManager(this@ShareRoomReadActivity, 2)
    private var shareGiftAdapter : HomeGiftAdapter? = null
    private var shareUsedGiftAdapter : HomeUsedGiftAdapter? = null

    private var saveSharedPreference = SaveSharedPreference()

    private var shareRoomData : GetTeamData? = null
    private var shareUsedGiftAllData = ArrayList<ShareRoomGifticon>()
    private var shareGiftAllData = ArrayList<ShareRoomGifticon>()
    private var type = ""

    private var totalCount = 0
    private var nextPage = 0

    private val SERVER_URL = BuildConfig.server_URL
    private val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: MoaInterface = retrofit.create(MoaInterface::class.java)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomReadBinding.inflate(layoutInflater)

        type = intent.getStringExtra("type") as String

        if (type == "READ") {
            shareRoomData = intent.getSerializableExtra("data") as GetTeamData
            println(shareRoomData)
            initUsedRecyclerView()
            initSharedGifticonRecyclerView()
            sBinding.shareTitle.text = shareRoomData!!.teamName
            val requestOptions = RequestOptions()
                .centerCrop() // 또는 .fitCenter()
                .override(1800, 1300) // 원하는 크기로 조절

            Glide.with(this@ShareRoomReadActivity)
                .load(shareRoomData!!.teamImage!!.toUri())
                .error(R.drawable.image)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d("Glide", "Image load failed: ${e?.message}")
                        println(e?.message.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        println("glide")
                        return false
                    }
                })
                .into(sBinding.shareBackgroundImageview)
            if (shareRoomData!!.teamLeaderNickName == saveSharedPreference.getName(this@ShareRoomReadActivity).toString()) {
                sBinding.shareSettingIcon.setOnClickListener {
                    val intent = Intent(this@ShareRoomReadActivity, ShareRoomSettingActivity::class.java).apply {
                        putExtra("type", "SETTING_LEADER")
                        putExtra("data", shareRoomData)
                    }
                    requestActivity.launch(intent)
                }
            } else {
                sBinding.shareSettingIcon.setOnClickListener {
                    val intent = Intent(this@ShareRoomReadActivity, ShareRoomSettingActivity::class.java).apply {
                        putExtra("type", "SETTING_MEMBER")
                        putExtra("data", shareRoomData)
                    }
                    requestActivity.launch(intent)
                }
            }
        }

        if (shareRoomData!!.teamImage != null) {
            sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_update_icon)
            sBinding.shareTitle.text = shareRoomData!!.teamName
            CoroutineScope(Main).launch {
                sBinding.shareTitle.apply {
                    sBinding.shareTitle.setTextColor(ContextCompat.getColor(this@ShareRoomReadActivity ,R.color.moa_gray_white))
                }
                sBinding.shareSettingIcon.apply {
                    sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_update_icon)
                }
            }
        } else {
            sBinding.shareTitle.text = shareRoomData!!.teamName
            sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_icon)
        }


        sBinding.shareGifticonBtn.setOnClickListener {
            val intent = Intent(this@ShareRoomReadActivity, ShareGifticonActivity::class.java).apply {
                putExtra("teamId", shareRoomData!!.id)
            }
            startActivity(intent)
        }

        sBinding.shareMoveIv.setOnClickListener {
            val intent = Intent(this@ShareRoomReadActivity, SharedLockerActivity::class.java).apply {

            }
            startActivity(intent)
        }



        setContentView(sBinding.root)
        
    }


    //여긴 공유된것을 사용한거
   private fun initUsedRecyclerView() {
        setUsedGiftData()
        shareUsedGiftAdapter = HomeUsedGiftAdapter()
        shareUsedGiftAdapter!!.usedGiftItemData = shareUsedGiftAllData
        sBinding.shareUsedRv.adapter = shareUsedGiftAdapter
        sBinding.shareUsedRv.setHasFixedSize(true)
    }

    //여긴 공유된거
    private fun initSharedGifticonRecyclerView() {
        setSharedGiftData()
        shareGiftAdapter = HomeGiftAdapter()
        shareGiftAdapter!!.giftItemData = shareGiftAllData
        sBinding.shareGiftRv.adapter = shareGiftAdapter
        //레이아웃 뒤집기 안씀
        //manager.reverseLayout = true
        //manager.stackFromEnd = true
        sBinding.shareGiftRv.setHasFixedSize(true)
        sBinding.shareGiftRv.layoutManager = gridManager
    }

    private fun setUsedGiftData() {

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

        println("setting start")

        CoroutineScope(Dispatchers.IO).launch {
            api.getMyShareRoomGifticon(shareRoomData!!.id, 0, 10).enqueue(object : Callback<ShareRoomGetTeamGifticonData> {
                override fun onResponse(
                    call: Call<ShareRoomGetTeamGifticonData>,
                    response: Response<ShareRoomGetTeamGifticonData>
                ) {
                    if (response.isSuccessful) {
                        println("est gift")

                        shareGiftAllData.clear()

                        for (i in response.body()!!.data.data.indices) {

                            shareGiftAllData.add(ShareRoomGifticon(
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
                        totalCount = response.body()!!.data.totalCount
                        nextPage = response.body()!!.data.nextPage

                        shareGiftAdapter!!.notifyDataSetChanged()

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

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val shareRoomData = it.data?.getSerializableExtra("data") as ShareRoomData

                when(it.data?.getIntExtra("flag", -1)) {
                    //add
                    0 -> {
                        //setRoomData()
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
}