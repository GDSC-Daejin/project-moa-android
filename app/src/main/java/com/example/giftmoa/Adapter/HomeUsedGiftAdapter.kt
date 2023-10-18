package com.example.giftmoa.Adapter
import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.giftmoa.Data.GiftData
import com.example.giftmoa.Data.UsedGiftData
import com.example.giftmoa.R
import com.example.giftmoa.databinding.HomeItemBinding
import com.example.giftmoa.databinding.HomeUsedCouponItemBinding
import com.kakao.sdk.common.util.SdkLogLevel
import kotlinx.coroutines.NonDisposableHandle.parent


class HomeUsedGiftAdapter : RecyclerView.Adapter<HomeUsedGiftAdapter.HomeUsedGiftViewHolder>(){
    private lateinit var binding : HomeUsedCouponItemBinding
    var usedGiftItemData = ArrayList<UsedGiftData>()
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
        private var usedImage = binding.usedCouponIv
        private var usedUser = binding.usedUser


        fun bind(itemData: UsedGiftData, position : Int) {
            this.position = position

            usedBrand.text = itemData.brand
            usedName.text = itemData.name
            usedDays.text = itemData.usedDays.toString() + "분 전"
            usedCost.text = "사용금액 | " + itemData.cost.toString()
            usedUser.text = "사용자 | " + itemData.user

            if (itemData.giftImg != null) {
                usedImage.setImageURI(itemData.giftImg)
            } else {
                usedImage.setImageResource(R.drawable.image)
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

        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, holder.adapterPosition, usedGiftItemData[holder.adapterPosition].name)
        }
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
        fun onClick(view: View, position: Int, itemId: String)
    }

    //약한 참조로 참조하는 객체가 사용되지 않을 경우 가비지 콜렉션에 의해 자동해제
    //private var itemClickListener: WeakReference<ItemClickListener>? = null
    private lateinit var itemClickListener: HomeUsedGiftAdapter.ItemClickListener

    fun setItemClickListener(itemClickListener: HomeUsedGiftAdapter.ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

}