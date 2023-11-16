package com.example.giftmoa.Adapter
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.*
import com.example.giftmoa.R
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
        shareRoomGifticonItemData.forEach { it ->
            it.isSelected = false
        }
    }

    inner class ShareViewHolder(private val binding : ItemGifticonBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private var giftBrand = binding.tvCouponExchangePlace
        private var giftName = binding.tvCouponName
        private var giftImg = binding.ivCouponImage
        var viewAlpha = binding.viewAlpha
        private var giftRemainingDay = binding.tvDDay


        fun bind(itemData: ShareRoomGifticon, position : Int) {
            this.position = position

            val s = itemData.dueDate

            giftBrand.text = itemData.exchangePlace
            giftName.text = itemData.name
            if (itemData.gifticonImagePath != null) {
                Glide.with(binding.ivCouponImage.context)
                    .load(itemData.gifticonImagePath)
                    .into(binding.ivCouponImage)
            } else {
                giftImg.setImageResource(R.drawable.image)
            }

            if (itemData.status == "AVAILABLE") {
                binding.tvCouponUsedComplete.visibility = View.GONE
                binding.viewAlpha.visibility = View.GONE
                Glide.with(binding.ivCouponImage.context)
                    .load(itemData.gifticonImagePath)
                    .into(binding.ivCouponImage)
            } else {
                binding.tvCouponUsedComplete.visibility = View.VISIBLE
                binding.viewAlpha.visibility = View.VISIBLE
                Glide.with(binding.ivCouponImage.context)
                    .load(itemData.gifticonImagePath)
                    .into(binding.ivCouponImage)
            }

            try {
                //2024-01-26T00:00:00.000+00:00 -> inputFormat
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                val dueDate = inputFormat.parse(itemData.dueDate)

                val currentDate = Date()
                val timeDifference = dueDate.time - currentDate.time
                val daysDifference = timeDifference / (1000 * 60 * 60 * 24)

                if (daysDifference >= 0) {
                    val dDayText = "D-${daysDifference}"
                    if (daysDifference == 0L)
                        binding.tvDDay.text = "D-Day"
                    else
                        binding.tvDDay.text = dDayText

                } else {
                    binding.tvDDay.text = "만료"
                    binding.viewAlpha.visibility = android.view.View.VISIBLE
                }
            } catch (e: Exception) {
                // 날짜 파싱에 실패한 경우나 예외 처리
                binding.tvDDay.text = "날짜 형식 오류"
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
                itemClickListener.onClick(it, position, shareRoomGifticonItemData[position].gifticonId)
                pos = position
                if (shareRoomGifticonItemData[position].isSelected == true) {
                    shareRoomGifticonItemData[pos].isSelected = false
                    binding.viewAlpha.visibility = View.GONE
                    binding.ivCouponImage.alpha = 1.0.toFloat()
                    //setMultipleSelection(binding, pos)
                } else {
                    shareRoomGifticonItemData[pos].isSelected = true
                    binding.viewAlpha.visibility = View.VISIBLE
                    binding.ivCouponImage.alpha = 0.7.toFloat()
                    //setMultipleSelection(binding, pos)
                }
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
            shareRoomGifticonItemData[holder.adapterPosition].isSelected = true
            println(shareRoomGifticonItemData[holder.adapterPosition])
            setMultipleSelection(binding, pos)
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
        fun onClick(view: View, position: Int, itemId: Int)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: ShareRoomGifticonAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: ShareRoomGifticonAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

   /*private fun setMultipleSelection(binding: ItemGifticonBinding, adapterPosition : Int) {
        if(shareRoomGifticonItemData[adapterPosition].isSelected == true){
            Log.i("SETMULTIPLE", "TRUE")
            changeBackground(binding, adapterPosition)

            //shareRoomGifticonItemData[adapterPosition].isSelected = false
        }else{
            Log.i("SETMULTIPLE", "FALSE")
            changeBackground(binding, adapterPosition)

            //shareRoomGifticonItemData[adapterPosition].isSelected = true
        }
        notifyDataSetChanged()
    }

    private fun changeBackground(binding: ItemGifticonBinding, position: Int) {
        if(shareRoomGifticonItemData[position].isSelected == true){
            Log.i("SETMULTIPLE", "VIEW CHANGE")
            //binding.ivCouponImage.backgroundTintList =
            //binding.ivCouponImage.alpha = 0.7.toFloat()
            binding.viewAlpha.visibility = View.VISIBLE
        }else{
            Log.i("SETMULTIPLE", "VIEW CHANGE2")
            //binding.ivCouponImage.alpha = 1.0.toFloat()
            binding.viewAlpha.visibility = View.GONE
        }
    }*/


}