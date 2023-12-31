package com.example.giftmoa.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.giftmoa.Data.*
import com.example.giftmoa.R
import com.example.giftmoa.databinding.HomeItemBinding
import com.example.giftmoa.databinding.HomeUsedCouponItemBinding
import com.kakao.sdk.common.util.SdkLogLevel
import kotlinx.coroutines.NonDisposableHandle.parent
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class HomeUsedGiftAdapter(private val onClick: (UsedGifticon) -> Unit) : RecyclerView.Adapter<HomeUsedGiftAdapter.HomeUsedGiftViewHolder>(){
    private lateinit var binding : HomeUsedCouponItemBinding
    var usedGiftItemData = ArrayList<UsedGifticon>()
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class HomeUsedGiftViewHolder(private val binding : HomeUsedCouponItemBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private var usedBrand = binding.usedBrand
        private var usedName = binding.usedName
        private var usedDays = binding.usedDays
        private var usedCost = binding.usedCost
        private var usedUser = binding.usedUser

        val sharedPref = context.getSharedPreferences("profile_nickname", Context.MODE_PRIVATE)
        val userName = sharedPref.getString("profileNickname", null) // 기본값은 null


        fun bind(itemData: UsedGifticon, position : Int) {
            this.position = position

            usedBrand.text = itemData.exchangePlace
            if (itemData.name?.length!! >= 16) {
                usedName.textSize = 12F
                usedName.text = itemData.name
            } else {
                usedName.text = itemData.name
            }


            //binding.usedCost.text = "사용금액 | ${itemData.gifticonHistories?.get(0)?.usedPrice.toString()}"

            if (itemData.gifticonImagePath != null) {
                Glide.with(context)
                    .load(itemData.gifticonImagePath)
                    .into(binding.usedCouponIv)
            } else {
                Glide.with(context)
                    .load(R.drawable.icon_gifticon_null)
                    .into(binding.usedCouponIv)
            }

            if (itemData.gifticonType == "MONEY") {
                binding.usedCost.apply {
                    visibility = View.VISIBLE
                    text = "사용금액 | ${itemData.gifticonHistories?.get(0)?.usedPrice.toString()}"
                }
                binding.usedUser.apply {
                    text = "사용자 | ${userName.toString()}"
                }
            } else {
                binding.usedCost.visibility = View.GONE
                binding.usedUser.apply {
                    text = "사용자 | ${userName.toString()}"
                    setPadding(0, 0, 0, 0)
                }
            }

            if (itemData.usedDate != null ) {
                val now = System.currentTimeMillis()
                val date = Date(now)
                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.KOREA)
                val currentDate = sdf.format(date)

                val nowFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.KOREA).parse(currentDate)
                val beforeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.KOREA).parse(itemData.usedDate)

                val diffMilliseconds = nowFormat?.time?.minus(beforeFormat?.time!!)

                val diffSeconds = diffMilliseconds?.div(1000)
                val diffMinutes = diffMilliseconds?.div((60 * 1000))
                val diffHours = diffMilliseconds?.div((60 * 60 * 1000))
                val diffDays = diffMilliseconds?.div((24 * 60 * 60 * 1000))
                if (diffMinutes != null && diffDays != null && diffHours != null && diffSeconds != null) {
                    if(diffSeconds > -1){
                        //usedDays.text = diffSeconds + "일 전"
                    }

                    if (diffSeconds > 0) {
                        usedDays.text =  diffSeconds.toString() + "초 전"

                    }

                    if (diffMinutes > 0) {
                        usedDays.text = diffMinutes.toString() + "분 전"

                    }

                    if (diffHours > 0) {
                        usedDays.text =  diffHours.toString() + "시간 전"

                    }

                    if (diffDays > 0) {
                        usedDays.text = diffDays.toString() + "일 전"
                    }

                    if (diffDays < 0) {
                        //usedDays.text = itemData.usedDate.toString() + "분 전"
                    }
                }
            }

            binding.root.setOnClickListener {
                onClick(itemData)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeUsedGiftViewHolder {
        context = parent.context
        binding = HomeUsedCouponItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HomeUsedGiftViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HomeUsedGiftViewHolder, position: Int) {
        holder.bind(usedGiftItemData[holder.adapterPosition], position)

        /*holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, usedGiftItemData[holder.adapterPosition].name)
        }*/
    }

    override fun getItemCount(): Int {
        return usedGiftItemData.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    //데이터 Handle 함수
    fun removeData(position: Int) {
        usedGiftItemData.removeAt(position)
        //temp = null
        notifyItemRemoved(position)
    }

    interface ItemClickListener {
        fun onClick(view: View, position: Int, itemId: String?)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: HomeUsedGiftAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: HomeUsedGiftAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}