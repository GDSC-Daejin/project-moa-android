package com.example.giftmoa.BottomMenu

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.Adapter.HomeTabAdapter
import com.example.giftmoa.AssetLoader
import com.example.giftmoa.AutoRegistrationActivity
import com.example.giftmoa.BottomSheetFragment.BottomSheetFragment
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.BoundingBox
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.Data.StorageData
import com.example.giftmoa.GifticonDetailActivity
import com.example.giftmoa.GifticonInfoListener
import com.example.giftmoa.GifticonRegistrationActivity
import com.example.giftmoa.R
import com.example.giftmoa.databinding.FragmentCouponBinding
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

class CouponFragment : Fragment(), CategoryListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentCouponBinding? = null
    private val binding get() = _binding!!

    private val TAG = "CouponFragment"
    private val tabTextList = listOf("전체", "사용가능", "사용완료")

    private var categoryList = mutableListOf<CategoryItem>()
    private var gifticonList = mutableListOf<GifticonDetailItem>()

    private lateinit var gifticonListAdapter: GifticonListAdapter

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

    interface NaverClovaOCRService {
        @Multipart
        @POST(BuildConfig.NCP_OCR_URL_PARAMS)
        @Headers("X-OCR-SECRET: ${BuildConfig.X_OCR_SECRET_KEY}")
        fun analyzeImage(
            @Part("message") message: RequestBody,
            @Part image: MultipartBody.Part
        ): Call<ResponseBody>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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

        /*gifticonListAdapter = GifticonListAdapter { gifticon ->
            val intent = Intent(requireActivity(), GifticonDetailActivity::class.java)
            intent.putExtra("gifticonId", gifticon.id)
            startActivity(intent)
        }*/

        getJsonData()

        TabLayoutMediator(tabLayout, viewPager) { tab, pos ->
            tab.text = tabTextList[pos]
            //val typeface = resources.getFont(com.example.mio.R.font.pretendard_medium)
            //tab.setIcon(tabIconList[pos])
        }.attach()

        binding.tvAddCoupon.setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : BottomSheetFragment.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        getBottomSheetData = value
                        //myViewModel.postCheckSearchFilter(getBottomSheetData)
                        when (value) {
                            "자동 등록" -> {
                                //val intent = Intent(requireActivity(), CouponAutoAddActivity::class.java)
                                /*val intent = Intent(requireActivity(), GifticonRegistrationActivity::class.java)
                                startActivity(intent)*/
                                checkPermission()
                            }

                            "수동 등록" -> {

                            }
                        }
                    }
                })
            }
        }

        binding.ivAddCategory.setOnClickListener {
            showCategoryBottomSheet(categoryList)
        }

        return root
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> loadImage()
            shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> showPermissionInfoDialog()
            else -> requestReadExternalStoragePermission()
        }
    }

    private fun showPermissionInfoDialog() {
        AlertDialog.Builder(requireActivity()).apply {
            setMessage("이미지를 가져오기 위해서, 외부 저장소 읽기 권한이 필요합니다.")
            setPositiveButton("동의") { _, _ -> requestReadExternalStoragePermission() }
            setNegativeButton("취소", null)
        }.show()
    }

    private fun requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            REQUEST_READ_EXTERNAL_STORAGE
        )
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
//                        Glide.with(this@GifticonRegistrationActivity)
//                            .load(uri)
//                            .into(binding.ivGifticon)

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

    private fun showCategoryBottomSheet(cateogoryList: List<CategoryItem>) {
        val categoryBottomSheet = CategoryBottomSheet(categoryList, this)
        categoryBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        categoryBottomSheet.show(requireActivity().supportFragmentManager, categoryBottomSheet.tag)
    }

    private fun getJsonData() {
        val assetLoader = AssetLoader()
        val storageJsonString = assetLoader.getJsonString(requireActivity(), "storage.json")

        if (!storageJsonString.isNullOrEmpty()) {
            val gson = Gson()
            val storageData = gson.fromJson(storageJsonString, StorageData::class.java)

            for (category in storageData.categories) {
                category.categoryName?.let { createNewChip(it.trim()) }
                categoryList.add(category)
                val chip = category.categoryName?.let { createNewChip(it) }

                // 마지막 Chip 뷰의 인덱스를 계산
                val lastChildIndex = binding.chipGroupCategory.childCount - 1

                // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                // 현재 Chip을 바로 그 앞에 추가
                if (lastChildIndex >= 0) {
                    binding.chipGroupCategory.addView(chip, lastChildIndex)
                } else {
                    // ChipGroup에 자식이 없는 경우, 그냥 추가
                    binding.chipGroupCategory.addView(chip)
                }
            }
        }
    }

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        chip.setOnCloseIconClickListener {
            // 닫기 아이콘 클릭 시 Chip 제거
            (it.parent as? ViewGroup)?.removeView(it)
        }
        return chip
    }

    private fun deleteChip(text: String) {
        for (i in 0 until binding.chipGroupCategory.childCount) {
            val childView = binding.chipGroupCategory.getChildAt(i)
            if (childView is Chip) {
                val chip = childView as Chip
                if (chip.text == text) {
                    binding.chipGroupCategory.removeView(chip)
                    break
                }
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

        private const val REQUEST_READ_EXTERNAL_STORAGE = 100
    }

    override fun onCategoryUpdated(categoryName: String) {
        Log.d(TAG, "onCategoryUpdated: $categoryName")
        val chip = createNewChip(categoryName)
        val positionToInsert = binding.chipGroupCategory.childCount - 1
        binding.chipGroupCategory.addView(chip, positionToInsert)
        // categoryList에 추가
        categoryList.add(CategoryItem(0, categoryName))
    }

    override fun onCategoryDeleted(categoryName: String) {
        var categoryId: Long? = null
        Log.d(TAG, "onCategoryDeleted: $categoryName")
        for (category in categoryList) {
            if (categoryName == category.categoryName) {
                // categoryList에서 해당 카테고리의 id 값 가져오기
                categoryId = category.id
                // categoryList에서 해당 카테고리 삭제
                categoryList.remove(category)
                deleteChip(categoryName)
                Log.d(TAG, "onCategoryDeleted: $categoryId")
                Log.d(TAG, "onCategoryDeleted: $categoryList")
                break
            }
        }
    }

}