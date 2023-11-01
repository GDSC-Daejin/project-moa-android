package com.example.giftmoa.ShareRoomMenu

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup.LayoutParams
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
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.ShareRoomData
import com.example.giftmoa.Data.UsedGiftData
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityShareRoomReadBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch

class ShareRoomReadActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivityShareRoomReadBinding
    private var gridManager = GridLayoutManager(this@ShareRoomReadActivity, 2)
    private var shareGiftAdapter : HomeGiftAdapter? = null
    private var shareUsedGiftAdapter : HomeUsedGiftAdapter? = null

    private var shareRoomData : ShareRoomData? = null
    private var shareUsedGiftAllData = ArrayList<UsedGiftData>()
    private var shareGiftAllData = ArrayList<GiftData>()
    private var type = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomReadBinding.inflate(layoutInflater)


        initUsedRecyclerView()
        initHomeRecyclerView()

        type = intent.getStringExtra("type") as String

        if (type == "READ") {
            shareRoomData = intent.getSerializableExtra("data") as ShareRoomData
            sBinding.shareTitle.text = shareRoomData!!.title
        }

        if (shareRoomData!!.roomBackground != null) {
            sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_update_icon)
            sBinding.shareTitle.text = shareRoomData!!.title
            CoroutineScope(Main).launch {
                sBinding.shareTitle.apply {
                    sBinding.shareTitle.setTextColor(ContextCompat.getColor(this@ShareRoomReadActivity ,R.color.moa_gray_white))
                }
                sBinding.shareSettingIcon.apply {
                    sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_update_icon)
                }
            }
        } else {
            sBinding.shareTitle.text = shareRoomData!!.title
            sBinding.shareSettingIcon.setBackgroundResource(R.drawable.share_setting_icon)

        }

        val requestOptions = RequestOptions()
            .centerCrop() // 또는 .fitCenter()
            .override(1800, 1300) // 원하는 크기로 조절

        Glide.with(this@ShareRoomReadActivity)
            .load(shareRoomData!!.roomBackground!!.toUri())
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

        setContentView(sBinding.root)
        
    }

    private fun initUsedRecyclerView() {
        setUsedGiftData()
        shareUsedGiftAdapter = HomeUsedGiftAdapter()
        shareUsedGiftAdapter!!.usedGiftItemData = shareUsedGiftAllData
        sBinding.shareUsedRv.adapter = shareUsedGiftAdapter
        sBinding.shareUsedRv.setHasFixedSize(true)
    }

    private fun initHomeRecyclerView() {
        setGiftData()

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
        shareUsedGiftAllData.add(UsedGiftData(12, "스타벅스", "아이스 아메리카노", 0, "그지1", null))
        shareUsedGiftAllData.add(UsedGiftData(13, "스타벅스", "아이스 아메리카노", 0, "그지1", null))
        shareUsedGiftAllData.add(UsedGiftData(14, "스타벅스", "아이스 아메리카노", 0, "그지1", null))
    }

    private fun setGiftData() {
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))
        shareGiftAllData.add(GiftData(200, "스타벅스", "아이스 아메리카노", null))

    }
}