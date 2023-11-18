package com.example.giftmoa.ShareRoomMenu

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.TeamGifticon

class TeamGifticonViewModel: ViewModel() {

    val couponList = MediatorLiveData<List<TeamGifticon>>()
    private var datas = arrayListOf<TeamGifticon>()
    val allCouponList = MutableLiveData<List<TeamGifticon>>()
    val availableCouponList = MutableLiveData<List<TeamGifticon>>()
    val usedCouponList = MutableLiveData<List<TeamGifticon>>()

    init {
        couponList.addSource(allCouponList) {
                value -> couponList.value = value
        }
        couponList.addSource(availableCouponList) {
                value -> couponList.value = value
        }
        couponList.addSource(usedCouponList) {
                value -> couponList.value = value
        }
    }

    fun addCoupon(coupon: TeamGifticon) {
        // 가장 앞에 추가
        datas.add(0, coupon)
        setData(datas)
    }

    fun deleteCoupon(coupon: TeamGifticon) {
        datas.remove(coupon)
        setData(datas)
    }

    fun deleteCouponById(id: Long) {
        val index = datas.indexOfFirst { x -> x.gifticonId == id }
        datas.removeAt(index)
        setData(datas)
    }

    fun updateCouponStatus(coupon: TeamGifticon) {
        if (coupon.status == "AVAILABLE") {
            coupon.status = "UNAVAILABLE"
        } else {
            coupon.status = "AVAILABLE"
        }
        setData(datas)
    }

    fun updateCoupon(coupon: TeamGifticon) {
        val index = datas.indexOfFirst { x -> x.gifticonId == coupon.gifticonId }
        datas[index] = coupon
        setData(datas)
    }

    // 정렬 기준
    fun sortCouponList(sort: String) {
        when (sort) {
            "최신 등록순" -> {
                datas.sortByDescending { x -> x.gifticonId }
                setData(datas)
            }
            "상품명순" -> {
                datas.sortBy { x -> x.name }
                setData(datas)
            }
            "마감 임박순" -> {
                datas.sortBy { x -> x.dueDate }
                setData(datas)
            }
        }
    }

    // 필터 기준
    /*fun filterCouponList(filter: String) {
        when (filter) {
            "전체" -> {
                setData(datas)
            }
            "사용가능" -> {
                setData(datas.filter { x -> x.status == "AVAILABLE" }.toList() as ArrayList<Gifticon>)
            }
            "사용완료" -> {
                setData(datas.filter { x -> x.status == "UNAVAILABLE" }.toList() as ArrayList<Gifticon>)
            }
        }
    }*/

    // 카테고리 기준
    fun filterCouponListByCategoryId(categoryId: Long) {
        when (categoryId) {
            0L -> {
                setData(datas)
            }
            else -> {
                setData(datas.filter { x -> x.category?.id == categoryId }.toList() as ArrayList<TeamGifticon>)
            }
        }
    }

    fun clearCouponList() {
        datas.clear()
        setData(datas)
    }

    private fun setData(data: ArrayList<TeamGifticon>) {
        allCouponList.value = data.toList()
        availableCouponList.value = data.filter { x -> x.status == "AVAILABLE" }.toList()
        usedCouponList.value = data.filter {  x -> x.status == "UNAVAILABLE" }.toList()
        couponList.value = data
    }
}