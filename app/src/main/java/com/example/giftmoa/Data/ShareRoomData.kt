package com.example.giftmoa.Data

import android.net.Uri

data class ShareRoomData(
    var title : String,
    var numberOfPeople : Int,
    var shareCouponCount : Int,
    var master : String,
    var roomCode : String,
    var roomBackground : String?
) : java.io.Serializable {

}
