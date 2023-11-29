package com.example.giftmoa.ShareRoomMenu

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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
import com.example.giftmoa.utils.LeftMarginItemDecoration
import com.example.giftmoa.utils.RightMarginItemDecoration
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShareRoomReadActivity : AppCompatActivity() {
    private lateinit var sBinding: ActivityShareRoomReadBinding
    private var gridManager =
        GridLayoutManager(this@ShareRoomReadActivity, 2, GridLayoutManager.VERTICAL, false)
    private var shareGiftAdapter: HomeGiftAdapter? = null
    private var shareUsedGiftAdapter: HomeUsedGiftAdapter? = null

    private var saveSharedPreference = SaveSharedPreference()

    private var shareRoomReadData: Team? = null
    private var shareUsedGiftAllData = ArrayList<UsedGifticon>()
    private var shareGiftAllData = ArrayList<TeamGifticon>()
    private var type = ""

    private var totalCount: Int? = null
    private var nextPage: Int? = null

    private var isEdit = false

    private var identification: String? = ""

    private val TAG = "ShareRoomReadActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomReadBinding.inflate(layoutInflater)

        type = intent.getStringExtra("type") as String

        if (type == "READ") {
            shareRoomReadData = if (Build.VERSION.SDK_INT >= 33) {
                intent.getParcelableExtra("data", Team::class.java)
            } else {
                intent.getParcelableExtra("data")
            }

            println("shareroomdata read" + shareRoomReadData)

            val sharedPref = this.getSharedPreferences("profile_nickname", Context.MODE_PRIVATE)
            identification = sharedPref.getString("profileNickname", null) // 기본값은 null

            initUsedRecyclerView()
            initSharedGifticonRecyclerView()
            sBinding.shareTitle.text = shareRoomReadData!!.teamName
            val requestOptions = RequestOptions()
                .centerCrop() // 또는 .fitCenter()
                .override(1800, 1300) // 원하는 크기로 조절

            /*Glide.with(this@ShareRoomReadActivity)
                .load(shareRoomReadData?.teamImage)
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
                .into(sBinding.shareBackgroundImageview)*/

            Glide.with(sBinding.root.context)
                .load(shareRoomReadData?.teamImage?.toUri())
                .centerCrop()
                .error(R.drawable.image)
                .into(sBinding.shareBackgroundImageview)

            if (shareRoomReadData?.teamLeaderNickname == identification) {
                sBinding.shareSettingIcon.setOnClickListener {
                    val intent = Intent(
                        this@ShareRoomReadActivity,
                        ShareRoomSettingActivity::class.java
                    ).apply {
                        putExtra("type", "SETTING_LEADER")
                        putExtra("data", shareRoomReadData)
                    }
                    requestActivity.launch(intent)
                }
            } else {
                sBinding.shareSettingIcon.setOnClickListener {
                    val intent = Intent(
                        this@ShareRoomReadActivity,
                        ShareRoomSettingActivity::class.java
                    ).apply {
                        putExtra("type", "SETTING_MEMBER")
                        putExtra("data", shareRoomReadData)
                    }
                    requestActivity.launch(intent)
                }
            }
        }

        /*if (shareRoomReadData!!.teamImage != null) {
            sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_update_icon)
            sBinding.shareTitle.text = shareRoomReadData!!.teamName
            CoroutineScope(Main).launch {
                sBinding.shareTitle.apply {
                    sBinding.shareTitle.setTextColor(ContextCompat.getColor(this@ShareRoomReadActivity ,R.color.moa_gray_white))
                }
                sBinding.shareSettingIcon.apply {
                    sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_update_icon)
                }
            }
        } else {
            sBinding.shareTitle.text = shareRoomReadData!!.teamName
            sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_icon)
        }*/


        sBinding.shareGifticonBtn.setOnClickListener {
            val intent =
                Intent(this@ShareRoomReadActivity, ShareGifticonActivity::class.java).apply {
                    putExtra("teamId", shareRoomReadData?.id)
                }
            requestActivity.launch(intent)
        }

        sBinding.shareMoveIv.setOnClickListener {
            val intent =
                Intent(this@ShareRoomReadActivity, SharedLockerActivity::class.java).apply {
                    putExtra("teamId", shareRoomReadData?.id)
                }
            startActivity(intent)
        }

        shareGiftAdapter!!.setItemClickListener(object : HomeGiftAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: Long?) {
                val intent =
                    Intent(this@ShareRoomReadActivity, GifticonDetailActivity::class.java).apply {
                        putExtra("gifticonId", itemId)
                    }
                requestActivity.launch(intent)
            }
        })

        sBinding.backArrow.setOnClickListener {
            if (isEdit) {
                val intent =
                    Intent(this@ShareRoomReadActivity, ShareRoomFragment::class.java).apply {
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
        shareUsedGiftAdapter = HomeUsedGiftAdapter { usedGifticon ->
            val intent = Intent(this, GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", usedGifticon.id)
            startActivity(intent)
        }
        shareUsedGiftAdapter!!.usedGiftItemData = shareUsedGiftAllData
        sBinding.shareUsedRv.adapter = shareUsedGiftAdapter
        sBinding.shareUsedRv.setHasFixedSize(true)

        val dp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, resources.displayMetrics).toInt()
        sBinding.shareUsedRv.addItemDecoration(LeftMarginItemDecoration(dp16))
        sBinding.shareUsedRv.addItemDecoration(RightMarginItemDecoration(dp16))
    }

    //여긴 공유된거
    private fun initSharedGifticonRecyclerView() {
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
        Retrofit2Generator.create(this@ShareRoomReadActivity)
            .getRecentUsedTeamGifticonData(shareRoomReadData?.id?.toInt()!!, 0, 10)
            .enqueue(object : Callback<GetUsedTeamGifticonResponseData> {
                override fun onResponse(
                    call: Call<GetUsedTeamGifticonResponseData>,
                    response: Response<GetUsedTeamGifticonResponseData>
                ) {
                    if (response.isSuccessful) {
                        shareUsedGiftAllData.clear()

                        response.body()?.data?.dataList?.forEach { gifticon ->
                            if (gifticon.gifticonType == "GENERAL") {
                                shareUsedGiftAllData.add(gifticon)
                            } else if (gifticon.gifticonType == "MONEY") {
                                gifticon.gifticonHistories?.sortedByDescending { it.usedDate }?.forEach { history ->
                                    shareUsedGiftAllData.add(
                                        UsedGifticon(
                                            id = gifticon.id,
                                            name = gifticon.name,
                                            gifticonImagePath = gifticon.gifticonImagePath,
                                            exchangePlace = gifticon.exchangePlace,
                                            dueDate = gifticon.dueDate,
                                            gifticonType = gifticon.gifticonType,
                                            status = gifticon.status,
                                            usedDate = gifticon.usedDate,
                                            author = gifticon.author,
                                            category = gifticon.category,
                                            gifticonHistories = listOf(history)
                                        )
                                    )
                                }
                            }
                        }

                        Log.d(TAG, "shareUsedGiftAllData: $shareUsedGiftAllData")
                        shareUsedGiftAdapter?.notifyDataSetChanged()

                    } else {
                        println("faafa")
                        Log.d("test", response.errorBody()?.string()!!)
                        Log.d("message", call.request().toString())
                        println(response.code())
                    }
                }

                override fun onFailure(call: Call<GetUsedTeamGifticonResponseData>, t: Throwable) {
                    Log.e("ERROR get Used Data", t.message.toString())
                }

            })

    }

    private fun setSharedGiftData() {
        Retrofit2Generator.create(this@ShareRoomReadActivity)
            .getTeamGifticonList(shareRoomReadData?.id!!, 0, 20)
            .enqueue(object : Callback<GetTeamGifticonListResponse> {
                override fun onResponse(
                    call: Call<GetTeamGifticonListResponse>,
                    response: Response<GetTeamGifticonListResponse>
                ) {
                    if (response.isSuccessful) {
                        println("est gift")

                        shareGiftAllData.clear()

                        val responseBody = response.body()
                        responseBody?.data?.dataList?.let { newList ->
                            // SimpleDateFormat을 사용하여 날짜를 파싱합니다.
                            val dateFormat =
                                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.KOREA)
                            val currentDate = Date()

                            Log.d(TAG, "currentDate = $currentDate")

                            val filteredList = newList
                                .filter { it.dueDate != null }
                                .mapNotNull { gifticon ->
                                    gifticon.dueDate?.let { dueDateString ->
                                        try {
                                            val dueDate = dateFormat.parse(dueDateString)
                                            if (dueDate != null && dueDate.after(currentDate)) gifticon else null
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                }
                                .sortedBy { it.dueDate }
                                .take(6 - shareGiftAllData.size)

                            shareGiftAllData.addAll(filteredList)
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

    private val requestActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
            when (it.resultCode) {
                AppCompatActivity.RESULT_OK -> {
                    val afterShareRoomData =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            it.data?.getParcelableExtra("data", Team::class.java)
                        } else {
                            it.data?.getParcelableExtra("data")
                        }

                    when (it.data?.getIntExtra("flag", -1)) {
                        //delete shareRoom
                        0 -> {
                            val intent = Intent(
                                this@ShareRoomReadActivity,
                                ShareRoomFragment::class.java
                            ).apply {
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
                            setUsedGiftData()
                        }

                        4 -> {
                            shareRoomReadData = afterShareRoomData
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

    override fun onStart() {
        super.onStart()

        if (type == "READ") {
            setUsedGiftData()
            setSharedGiftData()
        }
    }

    override fun onBackPressed() {
        if (isEdit) {
            val intent = Intent(this@ShareRoomReadActivity, ShareRoomFragment::class.java).apply {
                putExtra("flag", 4)
                putExtra("data", shareRoomReadData)
            }
            setResult(RESULT_OK, intent)
        }
        this@ShareRoomReadActivity.finish()
    }
}