package com.example.giftmoa.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.R
import com.example.giftmoa.databinding.HomeItemBinding
import com.example.giftmoa.databinding.ItemGifticonBinding
import com.kakao.sdk.common.util.SdkLogLevel


class HomeGiftAdapter : RecyclerView.Adapter<HomeGiftAdapter.HomeGiftViewHolder>(){
    private lateinit var binding : ItemGifticonBinding
    var giftItemData = ArrayList<GiftData>()
    private lateinit var context : Context

    init {
        setHasStableIds(true)
    }

    inner class HomeGiftViewHolder(private val binding : ItemGifticonBinding ) : RecyclerView.ViewHolder(binding.root) {
        private var position : Int? = null
        private var giftBrand = binding.tvCategoryName
        private var giftName = binding.tvCouponName
        private var giftImg = binding.ivCouponImage
        private var giftRemainingDay = binding.tvDDay


        fun bind(itemData: GiftData, position : Int) {
            this.position = position

            giftBrand.text = itemData.brand
            giftName.text = itemData.name
            if (itemData.giftImg != null) {
                giftImg.setImageURI(itemData.giftImg)
            } else {
                giftImg.setImageResource(R.drawable.asset_gifticon_coffee)
            }
            giftRemainingDay.text = "D+${itemData.remainingDay}"

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