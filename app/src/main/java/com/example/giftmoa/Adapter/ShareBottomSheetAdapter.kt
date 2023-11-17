package com.example.giftmoa.Adapter

import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.Team
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ItemCategoryBinding
import com.example.giftmoa.databinding.ItemHomeShareRoomNameBinding
import com.example.giftmoa.databinding.ItemShareBottomSheetBinding

class ShareBottomSheetAdapter(private val onClick: (Team) -> Unit) : ListAdapter<Team, ShareBottomSheetAdapter.ViewHolder>(diffUtil) {

    private var selectedPosition: Int = -1

    inner class ViewHolder(val binding: ItemShareBottomSheetBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(team: Team) {
            binding.tvShareRoomName.text = team.teamName
            Glide.with(binding.ivShareRoomImage.context)
                .load(team.teamImage)
                .into(binding.ivShareRoomImage)

            // 체크박스 상태 업데이트
            binding.checkbox.isChecked = selectedPosition == adapterPosition

            // 체크박스 클릭 리스너 설정
            binding.checkbox.setOnClickListener {
                val previousSelectedPosition = selectedPosition
                if (selectedPosition == adapterPosition) {
                    // 같은 아이템을 다시 클릭한 경우, 선택 해제
                    selectedPosition = -1
                } else {
                    // 새로운 아이템 선택
                    selectedPosition = adapterPosition
                }

                // 이전 선택된 아이템과 현재 선택된 아이템의 체크박스 상태 업데이트
                if (previousSelectedPosition != -1) {
                    notifyItemChanged(previousSelectedPosition)
                }
                notifyItemChanged(selectedPosition)

                // 클릭된 아이템에 대한 액션 실행
                onClick(team)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemShareBottomSheetBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun getSelectedTeam(): Team? {
        return if (selectedPosition != -1) getItem(selectedPosition) else null
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Team>() {
            override fun areItemsTheSame(oldItem: Team, newItem: Team): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Team, newItem: Team): Boolean {
                return oldItem == newItem
            }
        }
    }
}
