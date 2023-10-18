package com.example.giftmoa.Data

import android.net.Uri

data class UsedGiftData (
    var usedDays : Int,
    var brand : String,
    var name : String,
    var cost : Int,
    var user : String,
    var giftImg : Uri?
) : java.io.Serializable {

}