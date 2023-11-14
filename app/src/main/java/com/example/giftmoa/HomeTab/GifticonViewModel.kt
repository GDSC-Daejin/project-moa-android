package com.example.giftmoa.HomeTab

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.GifticonData

class GifticonViewModel: ViewModel() {

    val couponList = MediatorLiveData<List<Gifticon>>()
    private var datas = arrayListOf<Gifticon>()
    val allCouponList = MutableLiveData<List<Gifticon>>()
    val availableCouponList = MutableLiveData<List<Gifticon>>()
    val usedCouponList = MutableLiveData<List<Gifticon>>()

    /*init {
        couponList.addSource(allCouponList) {
            datas.addAll(it)
            couponList.value = datas
        }
        couponList.addSource(availableCouponList) {
            datas.addAll(it)
            couponList.value = datas
        }
        couponList.addSource(usedCouponList) {
            datas.addAll(it)
            couponList.value = datas
        }
    }*/
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

    fun addCoupon(coupon: Gifticon) {
        datas.add(coupon)
        setData(datas)
    }

    fun deleteCoupon(coupon: Gifticon) {
        datas.remove(coupon)
        setData(datas)
    }

    fun updateCouponStatus(coupon: Gifticon) {
        if (coupon.status == "AVAILABLE") {
            coupon.status = "UNAVAILABLE"
        } else {
            coupon.status = "AVAILABLE"
        }
        setData(datas)
    }

    fun updateCoupon(coupon: Gifticon) {
        val index = datas.indexOfFirst { x -> x.id == coupon.id }
        datas[index] = coupon
        setData(datas)
    }

    // 정렬 기준
    fun sortCouponList(sort: String) {
        when (sort) {
            "최신순" -> {
                datas.sortByDescending { x -> x.id }
                setData(datas)
            }
            "이름순" -> {
                datas.sortBy { x -> x.name }
                setData(datas)
            }
            "마감임박순" -> {
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
                setData(datas.filter { x -> x.category?.id == categoryId }.toList() as ArrayList<Gifticon>)
            }
        }
    }

    fun clearCouponList() {
        datas.clear()
        setData(datas)
    }

    private fun setData(data: ArrayList<Gifticon>) {
        allCouponList.value = data.toList()
        availableCouponList.value = data.filter { x -> x.status == "AVAILABLE" }.toList()
        usedCouponList.value = data.filter {  x -> x.status == "UNAVAILABLE" }.toList()
        couponList.value = data
    }
}