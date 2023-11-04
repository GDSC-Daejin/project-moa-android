package com.example.giftmoa.Adapter

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.ShareRoomDetailItem
import com.example.giftmoa.Data.ShareRoomItem
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ItemGifticonBinding
import com.example.giftmoa.databinding.ItemHomeShareRoomNameBinding
import java.text.SimpleDateFormat
import java.util.Date

class HomeShareRoomNameAdapter(private val onClick: (ShareRoomDetailItem) -> Unit) : ListAdapter<ShareRoomDetailItem, HomeShareRoomNameAdapter.ViewHolder>(diffUtil) {

    private var selectedPosition: Int = -1 // 초기에 선택한 아이템의 위치 설정, -1은 아무것도 선택하지 않은 상태

    inner class ViewHolder(val binding: ItemHomeShareRoomNameBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(shareRoom: ShareRoomDetailItem) {
            binding.tvShareRoomName.text = shareRoom.teamName
            val isSelected = adapterPosition == selectedPosition

            // 클릭된 아이템에만 밑줄 스타일을 적용
            binding.tvShareRoomName.paintFlags = if (isSelected) {
                Paint.UNDERLINE_TEXT_FLAG
            } else {
                0 // 클릭되지 않은 아이템은 밑줄 스타일을 제거
            }

            // 선택된 아이템은 텍스트 색상을 하얀색으로, 그렇지 않은 경우 다시 원래 색상으로 설정
            val textColor = if (isSelected) R.color.moa_gray_white else R.color.moa_carrot_300
            binding.tvShareRoomName.setTextColor(binding.root.context.getColor(textColor))

            binding.root.setOnClickListener {
                if (selectedPosition == adapterPosition) {
                    // 이미 선택된 아이템을 다시 클릭했을 때 아무런 동작을 하지 않도록 수정
                    return@setOnClickListener
                }

                if (selectedPosition != -1) {
                    // 이전 선택된 아이템이 있는 경우, 이전 선택된 아이템의 스타일을 원래대로 돌림
                    val previousSelectedPosition = selectedPosition
                    selectedPosition = -1 // 선택 해제
                    notifyItemChanged(previousSelectedPosition)
                }

                // 클릭된 아이템에 스타일을 적용
                selectedPosition = adapterPosition
                notifyItemChanged(selectedPosition)

                onClick(shareRoom)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHomeShareRoomNameBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        // 첫 번째 아이템을 앱 시작 시 클릭된 상태로 설정
        if (holder.adapterPosition == 0) {
            holder.binding.tvShareRoomName.performClick()
        }
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<ShareRoomDetailItem>() {
            override fun areItemsTheSame(oldItem: ShareRoomDetailItem, newItem: ShareRoomDetailItem): Boolean {
                return oldItem.teamId == newItem.teamId
            }

            override fun areContentsTheSame(oldItem: ShareRoomDetailItem, newItem: ShareRoomDetailItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}