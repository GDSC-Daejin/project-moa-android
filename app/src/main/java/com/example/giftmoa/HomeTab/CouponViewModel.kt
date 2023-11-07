package com.example.giftmoa.HomeTab

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.GifticonDetailItem

class CouponViewModel : ViewModel() {

    val gifticonList = MediatorLiveData<List<Gifticon>>()
    private var datas = arrayListOf<Gifticon>()
    val allGifticonList = MutableLiveData<List<Gifticon>>()
    val availableGifticonList = MutableLiveData<List<Gifticon>>()
    val usedGifticonList = MutableLiveData<List<Gifticon>>()

    init {
        gifticonList.addSource(allGifticonList) {
            value -> gifticonList.value = value
        }
        gifticonList.addSource(availableGifticonList) {
            value -> gifticonList.value = value
        }
        gifticonList.addSource(usedGifticonList) {
            value -> gifticonList.value = value
        }
    }

    private fun setData(data: ArrayList<Gifticon>) {

        allGifticonList.value = data.filter { x -> x.name != null }
        availableGifticonList.value = data.filter { x -> x.status == "AVAILABLE" }
        usedGifticonList.value = data.filter { x -> x.status != "AVAILABLE" }
        gifticonList.value = data
    }

    fun addData(gifticon: Gifticon) {
        datas.add(gifticon)
        setData(datas)
    }


    /*private val _allGifticonList = MutableLiveData<List<GifticonDetailItem>>()
    val allGifticonList: LiveData<List<GifticonDetailItem>> = _allGifticonList

    private val _availableGifticonList = MutableLiveData<List<GifticonDetailItem>>()
    val availableGifticonList: LiveData<List<GifticonDetailItem>> = _availableGifticonList

    private val _usedGifticonList = MutableLiveData<List<GifticonDetailItem>>()
    val usedGifticonList: LiveData<List<GifticonDetailItem>> = _usedGifticonList

    // 데이터를 로드하는 메서드
    fun loadData() {
        val exampleGifticon = GifticonDetailItem(
            id = 0,
            name = "디지털상품권 5천원권",
            barcodeNumber = "8591 2658 7534 3357",
            gifticonImagePath = null,
            exchangePlace = "메가MGC커피",
            dueDate = "2024-01-26T00:00:00.000Z",
            gifticonType = "GENERAL",
            orderNumber = "1969302141",
            status = "AVAILABLE",
            usedDate = null, // 아직 사용되지 않았다고 가정
            category = Category(id = 0, categoryName = "스타벅스"),
            amount = 5000
        )

        // 예제 데이터를 리스트에 넣어주기
        val exampleAllGifticons = listOf(exampleGifticon)

        // 필터링된 목록을 생성합니다 (사용 가능한 항목들만)
        val exampleAvailableGifticons = exampleAllGifticons.filter { it.status == "AVAILABLE" }
        val exampleUsedGifticons = exampleAllGifticons.filter { it.status != "AVAILABLE" }

        // LiveData 업데이트
        _allGifticonList.value = exampleAllGifticons
        _availableGifticonList.value = exampleAvailableGifticons
        _usedGifticonList.value = exampleUsedGifticons
    }*/
}