package com.example.giftmoa.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class FormatUtil {

    fun ParsedDateToString(date: String): String {
        var formattedDate: String? = null
        if (date.contains("년")) {
            val regex = Regex("(\\d+\\s*년)\\s*(\\d+\\s*월)\\s*(\\d+\\s*일)")
            val matchResult = date.let { regex.find(it) }

            formattedDate = if (matchResult != null) {
                val year = matchResult.groups[1]?.value?.trim() ?: ""
                val month = matchResult.groups[2]?.value?.trim() ?: ""
                val day = matchResult.groups[3]?.value?.trim() ?: ""

                "$year $month $day"
            } else {
                date
            }
        } else {
            // 2021.12.31 -> 2021년 12월 31일
            val regex = Regex("(\\d+)\\.(\\d+)\\.(\\d+)")
            val matchResult = date.let { regex.find(it) }

            formattedDate = if (matchResult != null) {
                val year = matchResult.groups[1]?.value?.trim() ?: ""
                val month = matchResult.groups[2]?.value?.trim() ?: ""
                val day = matchResult.groups[3]?.value?.trim() ?: ""

                "${year}년 ${month}월 ${day}일"
            } else {
                date
            }
        }

        return formattedDate
    }

    fun DateToString(date: String): String? {
        var formattedDate: String? = null
        val dateString = date // "2024-01-26T00:00:00.000Z"
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val outputFormat = SimpleDateFormat("yyyy년 MM월 dd일")

        try {
            val date = inputFormat.parse(dateString)
            formattedDate = outputFormat.format(date)
        } catch (e: Exception) {
            // 날짜 파싱에 실패한 경우에 대한 예외 처리
            formattedDate = "날짜 파싱 실패"
        }

        return formattedDate
    }

    fun StringToDate(dateString: String, TAG: String): String? {
        val inputFormat = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREA)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.KOREA)
        val utcTimeZone = TimeZone.getTimeZone("UTC")
        inputFormat.timeZone = utcTimeZone
        outputFormat.timeZone = utcTimeZone

        var formattedDate: String? = null

        try {
            val date = inputFormat.parse(dateString)
            if (date != null) {
                val calendar = Calendar.getInstance(utcTimeZone)
                calendar.time = date
                calendar.add(Calendar.DATE, 1)  // 하루 늘리기

                formattedDate = outputFormat.format(calendar.time)
                // 이제 formattedDate 변수에 변환된 날짜가 저장되어 있음
            } else {
                // 날짜 파싱 실패
                Log.d(TAG, "uploadGifticonToServer: 날짜 파싱 실패")
            }
        } catch (e: Exception) {
            // 날짜 파싱 중 예외가 발생한 경우
            Log.e(TAG, "uploadGifticonToServer: 날짜 파싱 중 예외 발생", e)
        }

        return formattedDate
    }
}