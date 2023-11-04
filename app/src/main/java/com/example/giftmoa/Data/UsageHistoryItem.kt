package com.example.giftmoa.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UsageHistoryItem(
    val id: Long? = null,
    val nickname: String? = null,
    val profileImageUrl: String? = null,
    val usedDate: String? = null,
    val usedAmount: Long? = null,
): Parcelable
