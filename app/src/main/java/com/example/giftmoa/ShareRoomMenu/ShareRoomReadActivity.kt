package com.example.giftmoa.ShareRoomMenu

import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.*
import com.example.giftmoa.Adapter.HomeGiftAdapter
import com.example.giftmoa.Adapter.HomeUsedGiftAdapter
import com.example.giftmoa.Adapter.ShareRoomGifticonAdapter
import com.example.giftmoa.BottomMenu.ShareRoomFragment
import com.example.giftmoa.Data.*
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
    private var gridManager = GridLayoutManager(this@ShareRoomReadActivity, 2, GridLayoutManager.VERTICAL, false)
    private var shareGiftAdapter : HomeGiftAdapter? = null
    private var shareUsedGiftAdapter : HomeUsedGiftAdapter? = null

    private var saveSharedPreference = SaveSharedPreference()

    private var shareRoomData : Team? = null
    private var shareUsedGiftAllData = ArrayList<Gifticon>()
    private var shareGiftAllData = ArrayList<TeamGifticon>()
    private var type = ""

    private var totalCount : Int? = null
    private var nextPage : Int? = null

    private var isEdit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomReadBinding.inflate(layoutInflater)

        type = intent.getStringExtra("type") as String

        if (type == "READ") {
            shareRoomData = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra("data", Team::class.java)
            } else {
                intent.getParcelableExtra("data")
            }
            println("shareroomdata " + shareRoomData)

            initUsedRecyclerView()
            initSharedGifticonRecyclerView()
            sBinding.shareTitle.text = shareRoomData!!.teamName
            val requestOptions = RequestOptions()
                .centerCrop() // 또는 .fitCenter()
                .override(1800, 1300) // 원하는 크기로 조절

            Glide.with(this@ShareRoomReadActivity)
                .load(shareRoomData?.teamImage)
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

            if (shareRoomData?.teamLeaderNickname == saveSharedPreference.getName(this@ShareRoomReadActivity).toString()) {
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
                putExtra("teamId", shareRoomData?.id)
            }
            requestActivity.launch(intent)
        }

        sBinding.shareMoveIv.setOnClickListener {
            val intent = Intent(this@ShareRoomReadActivity, SharedLockerActivity::class.java).apply {
                putExtra("teamId", shareRoomData?.id)
            }
            startActivity(intent)
        }

        shareGiftAdapter!!.setItemClickListener(object : HomeGiftAdapter.ItemClickListener{
            override fun onClick(view: View, position: Int, itemId: Long?) {
                val intent = Intent(this@ShareRoomReadActivity, GifticonDetailActivity::class.java).apply {
                    putExtra("gifticonId", itemId)
                }
                requestActivity.launch(intent)
            }
        })

        sBinding.backArrow.setOnClickListener {
            if (isEdit) {
                val intent = Intent(this@ShareRoomReadActivity, ShareRoomFragment::class.java).apply {
                    putExtra("flag", 4)
                }
                setResult(RESULT_OK, intent)
            }
            this@ShareRoomReadActivity.finish()
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
        sBinding.shareGiftRv.addItemDecoration(
            GridSpacingItemDecoration(spanCount = 2, spacing = 10f.fromDpToPx())
        )
    }

    private fun Float.fromDpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun setUsedGiftData() {
        Retrofit2Generator.create(this@ShareRoomReadActivity).getShareRoomGifticonFilterData("All_USED_NAME_DESC", shareRoomData?.id?.toInt()!!, 0, 10).enqueue(object : Callback<GetGifticonListResponse> {
            override fun onResponse(
                call: Call<GetGifticonListResponse>,
                response: Response<GetGifticonListResponse>
            ) {
                if (response.isSuccessful) {
                    shareUsedGiftAllData.clear()

                    response.body()?.data?.dataList?.forEach {
                        shareUsedGiftAllData.add(
                            Gifticon(
                            it.id,
                            it.name,
                            it.gifticonImagePath,
                            it.exchangePlace,
                            it.dueDate,
                            it.gifticonType,
                            it.status,
                            it.usedDate,
                            it.author,
                            it.category
                        ))
                    }
                    shareUsedGiftAdapter?.notifyDataSetChanged()
                } else {
                    println("faafa")
                    Log.d("test", response.errorBody()?.string()!!)
                    Log.d("message", call.request().toString())
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<GetGifticonListResponse>, t: Throwable) {
                Log.e("ERROR get Used Data", t.message.toString())
            }

        })

    }

    private fun setSharedGiftData() {
        Retrofit2Generator.create(this@ShareRoomReadActivity).getTeamGifticonList(shareRoomData?.id!!, 0, 10).enqueue(object : Callback<GetTeamGifticonListResponse> {
            override fun onResponse(
                call: Call<GetTeamGifticonListResponse>,
                response: Response<GetTeamGifticonListResponse>
            ) {
                if (response.isSuccessful) {
                    println("est gift")

                    shareGiftAllData.clear()

                    val responseBody = response.body()
                    responseBody?.data?.dataList?.let { newList ->

                        Log.d("TAG", "read: newList = $newList")
                        // 새로운 데이터를 리스트에 추가합니다.
                        val currentPosition = shareGiftAllData.size
                        shareGiftAllData.addAll(newList)

                        // 어댑터에 데이터가 변경되었음을 알립니다.
                        // DiffUtil.Callback 사용을 위한 submitList는 비동기 처리를 하므로 리스트의 사본을 넘깁니다.
                        //sAdapter.submitList(shareRoomDetailList.toList())
                        // 또는
                        // DiffUtil을 사용하지 않는 경우
                    }
                    shareGiftAdapter?.notifyDataSetChanged()

                    totalCount = response.body()!!.data?.totalCount?.toInt()
                    nextPage = response.body()!!.data?.nextPage?.toInt()

                } else {
                    println("faafa")
                    Log.d("add", response.errorBody()?.string()!!)
                    Log.d("message", call.request().toString())
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<GetTeamGifticonListResponse>, t: Throwable) {

            }

        })
    }

    private val requestActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        when (it.resultCode) {
            AppCompatActivity.RESULT_OK -> {
                val afterShareRoomData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.data?.getParcelableExtra("data", Team::class.java)
                } else {
                    it.data?.getParcelableExtra("data")
                }

                when(it.data?.getIntExtra("flag", -1)) {
                    //delete shareRoom
                    0 -> {
                        val intent = Intent(this@ShareRoomReadActivity, ShareRoomFragment::class.java).apply {
                            putExtra("flag", 0)
                        }
                        setResult(RESULT_OK, intent)
                        this@ShareRoomReadActivity.finish()
                    }

                    1 -> {
                        setSharedGiftData()
                    }

                    //use Gifticon
                    3 -> {
                        val updatedGifticonWithStatus = if (Build.VERSION.SDK_INT >= 33) {
                            it.data?.getParcelableExtra(
                                "updatedGifticonWithStatus",
                                Gifticon::class.java
                            )
                        } else {
                            it.data?.getParcelableExtra<Gifticon>("updatedGifticonWithStatus")
                        }
                        Log.d("SHARE_READ", "updatedGifticonWithStatus: $updatedGifticonWithStatus")
                        setSharedGiftData()
                    }

                    4 -> {
                        shareRoomData = afterShareRoomData
                        CoroutineScope(Dispatchers.Main).launch {
                            sBinding.shareTitle.text = afterShareRoomData?.teamName
                            Glide.with(this@ShareRoomReadActivity)
                                .load(afterShareRoomData?.teamImage?.toUri())
                                .error(R.drawable.image)
                                //.apply(requestOptions)
                                .centerCrop()
                                .override(200, 200)
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

                            isEdit = true
                        }
                    }
                }
            }
        }
    }
    override fun onBackPressed() {
        if (isEdit) {
            val intent = Intent(this@ShareRoomReadActivity, ShareRoomFragment::class.java).apply {
                putExtra("flag", 4)
                putExtra("data", shareRoomData)
            }
            setResult(RESULT_OK, intent)
        }
        this@ShareRoomReadActivity.finish()
    }
}