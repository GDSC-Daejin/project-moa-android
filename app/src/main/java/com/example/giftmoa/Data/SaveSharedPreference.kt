package com.example.giftmoa.Data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

class SaveSharedPreference {
    private val acctoken = "token"
    private val expireDate = "expireDate"
    private val userName = "name"

    fun getSharedPreferences(ctx: Context?): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(ctx!!)
    }

    fun setName(ctx: Context?, name: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(userName, name)
        editor.apply()
    }
    fun getName(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(userName, "")
    }


    fun setToken(ctx: Context?, token: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(acctoken, token)
        editor.apply()
    }
    fun getToken(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(acctoken, "")
    }

    fun setExpireDate(ctx: Context?, expire: String?) {
        val editor = getSharedPreferences(ctx).edit()
        editor.putString(expireDate, expire!!)
        editor.apply()
    }
    fun getExpireDate(ctx: Context?): String? {
        return getSharedPreferences(ctx).getString(expireDate, "")
    }

}