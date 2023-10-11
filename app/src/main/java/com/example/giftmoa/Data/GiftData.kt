package com.example.giftmoa.Data

import android.net.Uri

data class GiftData(
    var remainingDay : Int,
    var brand : String,
    var name : String,
    var giftImg : Uri?
) : java.io.Serializable {

}
