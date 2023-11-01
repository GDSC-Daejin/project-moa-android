package com.example.giftmoa.Data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {

    //모든 페이지 내용 작성 만족 시 다음 페이지 버튼 활성화
    private val _checkComplete : MutableLiveData<Boolean> = MutableLiveData()
    val checkComplete : LiveData<Boolean> = _checkComplete
    fun postCheckComplete(complete : Boolean) {
        _checkComplete.value = complete
    }
}