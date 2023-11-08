package com.example.giftmoa.Adapter
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.res.ColorStateList
import android.graphics.ColorFilter
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.Data.*
import com.example.giftmoa.R
import com.example.giftmoa.databinding.HomeItemBinding
import com.example.giftmoa.databinding.ItemGifticonBinding
import com.example.giftmoa.databinding.ShareRoomItemBinding
import com.kakao.sdk.common.KakaoSdk.init
import com.kakao.sdk.common.util.SdkLogLevel
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ShareRoomGifticonAdapter : RecyclerView.Adapter<ShareRoomGifticonAdapter.ShareViewHolder>(){
    private lateinit var binding : ItemGifticonBinding
    var shareRoomGifticonItemData = ArrayList<ShareRoomGifticon>()
    private lateinit var context : Context
    private var pos = 0

    init {
        setHasStableIds(true)
    }

    inner class ShareViewHolder(private val binding : ItemGifticonBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private var giftBrand = binding.tvCouponExchangePlace
        private var giftName = binding.tvCouponName
        private var giftImg = binding.ivCouponImage
        private var giftRemainingDay = binding.tvDDay


        fun bind(itemData: ShareRoomGifticon, position : Int) {
            this.position = position

            val s = itemData.dueDate

            giftBrand.text = itemData.exchangePlace
            giftName.text = itemData.name
            if (itemData.gifticonImagePath != null) {
                giftImg.setImageURI(itemData.gifticonImagePath!!.toUri())
            } else {
                giftImg.setImageResource(R.drawable.image)
            }

            if (itemData.status == "AVAILABLE") {
                binding.tvCouponUsedComplete.visibility = View.GONE
                binding.ivCouponImage.setImageURI(itemData.gifticonImagePath!!.toUri())
            } else {
                binding.tvCouponUsedComplete.visibility = View.VISIBLE
                binding.ivCouponImage.setImageURI(itemData.gifticonImagePath!!.toUri())
            }

            val now = System.currentTimeMillis()
            val date = Date(now)
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val currentDate = sdf.format(date)

            val nowFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(currentDate)
            val beforeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(s)

            val diffMilliseconds = beforeFormat?.time?.minus(nowFormat?.time!!)

            val diffSeconds = diffMilliseconds?.div(1000)
            val diffMinutes = diffMilliseconds?.div((60 * 1000))
            val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
            val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
            if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {
                if(diffSeconds > -1){

                }

                if (diffSeconds > 0) {

                }

                if (diffMinutes > 0) {

                }

                if (diffHours > 0) {

                }

                if (diffDays > 0) {
                    giftRemainingDay.text = "D-${diffDays.toString()}"
                }

                if (diffDays < 0) {
                    giftRemainingDay.text = "기간 지남"
                }
            }
            /*binding.root.setOnClickListener {
                val clickedPosition = position
                *//*if (clickedPosition != RecyclerView.NO_POSITION) {
                    selectedItem = clickedPosition // 클릭된 아이템 위치 업데이트
                    notifyDataSetChanged() // 어댑터 갱신 요청
                }*//*

                //중복선택가능
                setMultipleSelection(binding, itemData.gifticonId, clickedPosition)
                //onItemClickListener?.let { it(expense) }            }
            }*/

            binding.root.setOnClickListener {
                itemClickListener.onClick(it, position, shareRoomGifticonItemData[position].gifticonId.toString())
                pos = position
                setMultipleSelection(binding, null, pos)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareRoomGifticonAdapter.ShareViewHolder {
        context = parent.context
        binding = ItemGifticonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShareViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShareViewHolder, position: Int) {
        holder.bind(shareRoomGifticonItemData[holder.adapterPosition], position)
        /*holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, shareRoomGifticonItemData[holder.adapterPosition].gifticonId.toString())
            pos = holder.adapterPosition
            setMultipleSelection(binding, null, pos)
        }*/
    }

    override fun getItemCount(): Int {
        return shareRoomGifticonItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        shareRoomGifticonItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: String)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: ShareRoomGifticonAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: ShareRoomGifticonAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    private fun setMultipleSelection(binding: ItemGifticonBinding, s: Int?, adapterPosition : Int) {
        if(shareRoomGifticonItemData[adapterPosition].isSelected == true){
            shareRoomGifticonItemData[adapterPosition].isSelected = false
            changeBackground(binding, adapterPosition)
        }else{
            shareRoomGifticonItemData[adapterPosition].isSelected = true
            changeBackground(binding, adapterPosition)
        }
        notifyDataSetChanged()
    }

    private fun changeBackground(binding: ItemGifticonBinding, position: Int) {
        if(shareRoomGifticonItemData[position].isSelected == true){
            val colorStateList = ContextCompat.getColor(context, R.color.moa_gray_300)
            binding.ivCouponImage.setColorFilter(colorStateList)
        }else{
            binding.ivCouponImage.clearColorFilter()
        }
    }


}