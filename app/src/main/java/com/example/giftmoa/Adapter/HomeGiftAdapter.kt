package com.example.giftmoa.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.ShareRoomGifticon
import com.example.giftmoa.R
import com.example.giftmoa.databinding.HomeItemBinding
import com.example.giftmoa.databinding.ItemGifticonBinding
import com.kakao.sdk.common.util.SdkLogLevel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeGiftAdapter : RecyclerView.Adapter<HomeGiftAdapter.HomeGiftViewHolder>(){
    private lateinit var binding : HomeItemBinding
    var giftItemData = ArrayList<ShareRoomGifticon>()

    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class HomeGiftViewHolder(private val binding : ItemGifticonBinding ) : RecyclerView.ViewHolder(binding.root) {
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
                giftImg.setImageResource(R.drawable.asset_gifticon_coffee)
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
                    giftRemainingDay.text = "D+${diffDays.toString()}"
                }

                if (diffDays < 0) {
                    giftRemainingDay.text = "기간 지남"
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeGiftViewHolder {
        context = parent.context
        binding = ItemGifticonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeGiftViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeGiftViewHolder, position: Int) {
        holder.bind(giftItemData[holder.adapterPosition], position)

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, giftItemData[holder.adapterPosition].name)
        }
    }

    override fun getItemCount(): Int {
        return giftItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        giftItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: String)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: HomeGiftAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: HomeGiftAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}