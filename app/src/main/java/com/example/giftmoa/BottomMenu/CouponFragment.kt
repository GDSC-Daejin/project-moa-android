package com.example.giftmoa.BottomMenu

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.Adapter.HomeTabAdapter
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.CouponTab.AutoRegistrationActivity
import com.example.giftmoa.BottomSheetFragment.BottomSheetFragment
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BottomSheetFragment.SortBottomSheet
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.BoundingBox
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.GifticonRegistrationActivity
import com.example.giftmoa.HomeTab.HomeEntireFragment
import com.example.giftmoa.CouponTab.ManualRegistrationActivity
import com.example.giftmoa.Data.AddCategoryRequest
import com.example.giftmoa.Data.AddCategoryResponse
import com.example.giftmoa.Data.GetCategoryListResponse
import com.example.giftmoa.Data.GetKakaoLoginResponse
import com.example.giftmoa.HomeTab.CouponViewModel
import com.example.giftmoa.MainActivity
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.FragmentCouponBinding
import com.example.giftmoa.utils.FileGalleryPermissionUtil
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
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

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CouponFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

interface CategoryListener {
    fun onCategoryUpdated(category: String)
    fun onCategoryDeleted(category: String)
}

class CouponFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!

    private val TAG = "CouponFragment"

    private lateinit var viewModel: CouponViewModel


    private val tabTextList = listOf("전체", "사용가능", "사용완료")

    private var categoryList = mutableListOf<CategoryItem>()

    // 기프티콘 전체 리스트
    var allGifticonList = mutableListOf<GifticonDetailItem>()
    // 사용가능한 기프티콘 리스트
    var availableGifticonList = mutableListOf<GifticonDetailItem>()
    // 사용완료한 기프티콘 리스트
    var usedGifticonList = mutableListOf<GifticonDetailItem>()

    private var getBottomSheetData = ""

    private val NCP_OCR_URL = BuildConfig.NCP_OCR_URL
    private val imageLoadLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
        if (uriList.isNotEmpty()) {
            updateImages(uriList)
        } else {
            Toast.makeText(requireActivity(), "선택된 이미지가 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    private lateinit var retrofit: Retrofit

    private lateinit var manualAddGifticonResult: ActivityResultLauncher<Intent>

    interface NaverClovaOCRService {
        @Multipart
        @POST(BuildConfig.NCP_OCR_URL_PARAMS)
        @Headers("X-OCR-SECRET: ${BuildConfig.X_OCR_SECRET_KEY}")
        fun analyzeImage(
            @Part("message") message: RequestBody,
            @Part image: MultipartBody.Part
        ): Call<ResponseBody>
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCouponBinding.inflate(inflater, container, false)

        retrofit = Retrofit.Builder()
            .baseUrl(NCP_OCR_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val root: View = binding.root
        val tabLayout = binding.tabLayout
        val viewPager = binding.viewpager

        val homeTabAdapter = HomeTabAdapter(this)
        viewPager.adapter = homeTabAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = tabTextList[pos]
            //val typeface = resources.getFont(com.example.mio.R.font.pretendard_medium)
            //tab.setIcon(tabIconList[pos])
        }.attach()

        return root
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
            val inputStream = requireActivity().contentResolver.openInputStream(uri)
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

            val service = retrofit.create(GifticonRegistrationActivity.NaverClovaOCRService::class.java)
            service.analyzeImage(jsonPart, multipartBody).enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        val result = response.body()?.string()
                        Timber.i(result?: "Empty response")

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
                    FileGalleryPermissionUtil().loadImage(imageLoadLauncher)
                }
            }
        }
    }

    private fun launchAutoRegistrationActivity(result: String, uri: String) {
        val parsedGifticon = extractDataFromJson(result, uri)

        val intent = Intent(requireActivity(), AutoRegistrationActivity::class.java)
        // parsedGifticon 객체를 intent에 담아서 AutoRegistrationActivity로 전달
        intent.putExtra("PARSED_GIFTICON", parsedGifticon)
        startActivity(intent)
    }

    private fun extractDataFromJson(result: String, uri: String): ParsedGifticon {
        val fields = JSONObject(result)
            .getJSONArray("images")
            .getJSONObject(0)
            .getJSONArray("fields")

        val name = filterInferTextByCoordinates(fields, 0.0, 634.0, 780.0, 1050.0).joinToString(" ")
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

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")

        binding.tvAddCoupon.setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        getBottomSheetData = value
                        when (value) {
                            "자동 등록" -> {
                                //checkPermission()
                                FileGalleryPermissionUtil().checkPermission(requireActivity(), imageLoadLauncher)
                            }

                            "수동 등록" -> {
                                val intent = Intent(requireActivity(), ManualRegistrationActivity::class.java)
                                startActivity(intent)
                                /*manualAddGifticonResult.launch(Intent(requireActivity(), ManualRegistrationActivity::class.java).apply {
                                    putExtra("isEdit", false)
                                })*/
                            }
                        }
                    }
                })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
        binding.viewLoading.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CouponFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CouponFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }

        const val REQUEST_READ_EXTERNAL_STORAGE = 100
    }
}