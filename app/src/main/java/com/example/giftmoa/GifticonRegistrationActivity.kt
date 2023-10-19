package com.example.giftmoa

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.giftmoa.BuildConfig.NCP_OCR_URL_PARAMS
import com.example.giftmoa.BuildConfig.X_OCR_SECRET_KEY
import com.example.giftmoa.Data.BoundingBox
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.databinding.ActivityGifticonRegistrationBinding
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import timber.log.Timber
import java.io.IOException

class GifticonRegistrationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGifticonRegistrationBinding
    private val TAG = "GifticonRegistrationActivity"
    private val NCP_OCR_URL = BuildConfig.NCP_OCR_URL

    private val imageLoadLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
        if (uriList.isNotEmpty()) {
            updateImages(uriList)
        } else {
            Toast.makeText(this, "선택된 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var retrofit: Retrofit

    interface NaverClovaOCRService {
        @Multipart
        @POST(NCP_OCR_URL_PARAMS)
        @Headers("X-OCR-SECRET: $X_OCR_SECRET_KEY")
        fun analyzeImage(
            @Part("message") message: RequestBody,
            @Part image: MultipartBody.Part
        ): Call<ResponseBody>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGifticonRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        retrofit = Retrofit.Builder()
            .baseUrl(NCP_OCR_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        binding.btnLoadImage.setOnClickListener { checkPermission() }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> loadImage()
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> showPermissionInfoDialog()
            else -> requestReadExternalStoragePermission()
        }
    }

    private fun showPermissionInfoDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("이미지를 가져오기 위해서, 외부 저장소 읽기 권한이 필요합니다.")
            setPositiveButton("동의") { _, _ -> requestReadExternalStoragePermission() }
            setNegativeButton("취소", null)
        }.show()
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE)
    }

    private fun loadImage() {
        try {
            imageLoadLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load image: ${e.message}")
        }
    }

    private fun updateImages(uriList: List<Uri>) {
        Log.i(TAG, uriList.toString())
        if (uriList.isNotEmpty()) {
            uploadImageDirectlyToNaverClovaOCR(uriList.first())
        } else {
            Log.e(TAG, "Empty URI list")
        }
    }

    private fun uploadImageDirectlyToNaverClovaOCR(uri: Uri) {
        binding.viewLoading.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes() ?: throw IOException("Failed to read bytes from InputStream")
            inputStream.close()

            val requestBody = RequestBody.create("image/png".toMediaTypeOrNull(), bytes)
            val multipartBody = MultipartBody.Part.createFormData("file", "image.png", requestBody)

            val jsonPart = RequestBody.create(
                "application/json".toMediaTypeOrNull(),
                """{
                "images":[{"format":"jpeg","name":"demo"}],
                "requestId":"guide-demo",
                "version":"V2",
                "timestamp":${System.currentTimeMillis()}
            }"""
            )

            val service = retrofit.create(NaverClovaOCRService::class.java)
            service.analyzeImage(jsonPart, multipartBody).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val result = response.body()?.string()
                        Timber.i(result?: "Empty response")
                        Glide.with(this@GifticonRegistrationActivity)
                            .load(uri)
                            .into(binding.ivGifticon)

                        launchAutoRegistrationActivity(result!!, uri.toString())
                    } else {
                        Log.e(TAG, response.errorBody()?.string().toString())
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.e(TAG, t.message.toString())
                }
            })

        } catch (e: Exception) {
            Log.e(TAG, "Failed to process the image: ${e.message}")
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                val resultCode = grantResults.firstOrNull() ?: PackageManager.PERMISSION_DENIED
                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                    loadImage()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_READ_EXTERNAL_STORAGE = 100
    }

    private fun launchAutoRegistrationActivity(result: String, uri: String) {
        val parsedGifticon = extractDataFromJson(result, uri)

        val intent = Intent(this, AutoRegistrationActivity::class.java)
        // parsedGifticon 객체를 intent에 담아서 AutoRegistrationActivity로 전달
        intent.putExtra("PARSED_GIFTICON", parsedGifticon)
        startActivity(intent)
    }

    private fun extractDataFromJson(result: String, uri: String): ParsedGifticon {
        val fields = JSONObject(result)
            .getJSONArray("images")
            .getJSONObject(0)
            .getJSONArray("fields")

        val name = filterInferTextByCoordinates(fields, 0.0, 600.0, 750.0, 1000.0).joinToString(" ")
        val barcodeNumber = filterInferTextByCoordinates(fields, 0.0, 710.0, 1000.0, 1200.0).joinToString(" ")
        val dueDate = filterInferTextByCoordinates(fields, 400.0, 710.0, 1250.0, 1360.0).joinToString(" ")

        var exchangePlace: String? = null
        var orderNumber: String? = null

        var amount: Long? = null
        val regex = Regex("(\\d+([,\\d]*)(천|만|백|십)*)원")
        val matchResult = regex.find(name)

        if (matchResult != null) {
            var amountStr = matchResult.groups[1]?.value?.replace(",", "") ?: ""

            // 수량어 변환
            if (amountStr.endsWith("만")) {
                amountStr = amountStr.replace("만", "0000")
            } else if (amountStr.endsWith("천")) {
                amountStr = amountStr.replace("천", "000")
            } // 백이나 십과 같은 다른 수량어도 필요하다면 추가로 처리할 수 있습니다.

            if (amountStr.isNotEmpty()) {
                amount = amountStr.toLong()
                Log.i(TAG, "amount: $amount")
            }
        }

        for (i in 0 until fields.length()) {
            val field = fields.getJSONObject(i)
            val text = field.getString("inferText")
            when (text) {
                "교환처" -> exchangePlace = fields.getJSONObject(i + 1).getString("inferText")
                "주문번호" -> orderNumber = fields.getJSONObject(i + 1).getString("inferText")
            }
        }

        return ParsedGifticon(name, uri, barcodeNumber, exchangePlace, dueDate, orderNumber, amount)
    }

    private fun filterInferTextByCoordinates(fields: JSONArray, xStart: Double, xEnd: Double, yStart: Double, yEnd: Double): List<String> {
        val inferTexts = mutableListOf<String>()

        for (j in 0 until fields.length()) {
            val field = fields.getJSONObject(j)
            val (minX, maxX, minY, maxY) = getBoundingBox(field.getJSONObject("boundingPoly"))

            if (minX >= xStart && maxX <= xEnd && minY >= yStart && maxY <= yEnd) {
                inferTexts.add(field.getString("inferText"))
            }
        }
        return inferTexts
    }

    private fun getBoundingBox(boundingPoly: JSONObject): BoundingBox {
        val vertices = boundingPoly.getJSONArray("vertices")

        var minX = Double.MAX_VALUE
        var minY = Double.MAX_VALUE
        var maxX = Double.MIN_VALUE
        var maxY = Double.MIN_VALUE
        for (k in 0 until vertices.length()) {
            val vertex = vertices.getJSONObject(k)
            val x = vertex.getDouble("x")
            val y = vertex.getDouble("y")
            if (x < minX) minX = x
            if (x > maxX) maxX = x
            if (y < minY) minY = y
            if (y > maxY) maxY = y
        }

        return BoundingBox(minX, maxX, minY, maxY)
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
        binding.viewLoading.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }
}
