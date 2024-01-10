package com.example.giftmoa

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.giftmoa.Data.Data1
import com.example.giftmoa.Data.GetKakaoLoginResponse
import com.example.giftmoa.Data.RefreshTokenRequest
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Call

class AuthInterceptor(context: Context) : Interceptor {
    private val parentContext = context
    private val sharedPreferences = context.getSharedPreferences("TokenData", Context.MODE_PRIVATE)


    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = sharedPreferences.getString("accessToken", "") ?: ""
        val expiredException = sharedPreferences.getLong("accessTokenExpiresIn", 0L)
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        val response = chain.proceed(request)
        //Error opening kernel wakelock stats for: wakeup34: Permission denied 오류 수정
        if (expiredException <= System.currentTimeMillis() && expiredException != 0L) { //나중에 401처리나 그런거 나오면 다시 수정 Todo
            /*val intent = Intent(parentContext, Login2Activity::class.java)
            parentContext.startActivity(intent)
            Toast.makeText(parentContext, "사용자 정보가 유효하지 않습니다. 다시 로그인해주세요.", Toast.LENGTH_SHORT).show()*/
            /*do {
                refreshToken(parentContext)
            } while (false)*/
            refreshToken(parentContext)

            // 이후에 새로 갱신된 accessToken을 사용하여 요청을 재시도할 수 있도록
            // request에 새로운 accessToken을 설정하고 다시 호출하면 됩니다.
            val newAccessToken = sharedPreferences.getString("accessToken", "") ?: ""
            val newRequest = request.newBuilder()
                .header("Authorization", "Bearer $newAccessToken")
                .build()

            // 재시도
            return chain.proceed(newRequest)
        }
        return response
    }

    private fun refreshToken(context : Context) {
        val sharedPreferences = context.getSharedPreferences("TokenData", Context.MODE_PRIVATE)
        val refreshToken = sharedPreferences.getString("refreshToken", "") ?: ""
        val temp = RefreshTokenRequest(refreshToken)
        Retrofit2Generator.create(context).refreshToken(temp).enqueue(object :
            retrofit2.Callback<GetKakaoLoginResponse> {
            override fun onResponse(call: Call<GetKakaoLoginResponse>, response: retrofit2.Response<GetKakaoLoginResponse>) {
                if (response.isSuccessful) {
                    // 로그인 데이터 저장
                    response.body()?.data?.let { data ->
                        saveLoginData(data, context)
                    }
                } else if (response.code() == 404) {
                    val intent = Intent(context, Login2Activity::class.java)
                    context.startActivity(intent)
                } else if (response.code() == 401) {
                    val intent = Intent(context, Login2Activity::class.java)
                    context.startActivity(intent)
                }
            }

            override fun onFailure(call: Call<GetKakaoLoginResponse>, t: Throwable) {
                Log.e("Intercept", "Retrofit onFailure: ", t)
            }
        })
    }

    private fun saveLoginData(data: Data1, context : Context) {
        val sharedPref = context.getSharedPreferences("TokenData", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("grantType", data.grantType)
            putString("accessToken", data.accessToken)
            putString("refreshToken", data.refreshToken)
            putLong("accessTokenExpiresIn", data.accessTokenExpiresIn)
            apply() // 비동기적으로 데이터를 저장
        }
    }
}

