package com.example.giftmoa.BottomMenu

import android.app.Activity.RESULT_OK
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
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.giftmoa.Adapter.HomeTabAdapter
import com.example.giftmoa.CouponTab.AutoRegistrationActivity
import com.example.giftmoa.BottomSheetFragment.BottomSheetFragment
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BottomSheetFragment.SortBottomSheet
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.BoundingBox
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.GifticonRegistrationActivity
import com.example.giftmoa.CouponTab.ManualRegistrationActivity
import com.example.giftmoa.Data.AddCategoryRequest
import com.example.giftmoa.Data.AddCategoryResponse
import com.example.giftmoa.Data.Author
import com.example.giftmoa.Data.Category
import com.example.giftmoa.Data.GetCategoryListResponse
import com.example.giftmoa.Data.GetGifticonListResponse
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.LogoutUserResponse
import com.example.giftmoa.Data.UpdateGifticonRequest
import com.example.giftmoa.HomeTab.GifticonViewModel
import com.example.giftmoa.HomeTab.HomeEntireFragment
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.FragmentCouponBinding
import com.example.giftmoa.utils.FileGalleryPermissionUtil
import com.google.android.material.chip.Chip
import com.google.android.material.tabs.TabLayoutMediator
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
import java.util.Locale

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
    val binding get() = _binding!!

    private val TAG = "CouponFragment"

    //private lateinit var viewModel: CouponViewModel


    private val tabTextList = listOf("전체", "사용가능", "사용완료")

    private var categoryList = mutableListOf<CategoryItem>()

    // 기프티콘 전체 리스트
    var allCouponList = mutableListOf<GifticonDetailItem>()
    // 사용가능한 기프티콘 리스트
    var availableCouponList = mutableListOf<GifticonDetailItem>()
    // 사용완료한 기프티콘 리스트
    var usedCouponList = mutableListOf<GifticonDetailItem>()

    private var getBottomSheetData = ""

    private lateinit var gifticonViewModel: GifticonViewModel

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
    private lateinit var autoAddGifticonResult: ActivityResultLauncher<Intent>

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
    ): View {
        gifticonViewModel = ViewModelProvider(requireActivity(), ViewModelProvider.NewInstanceFactory())[GifticonViewModel::class.java]

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

        getAllGifticonListFromServer(0)

        getCategoryListFromServer()

        manualAddGifticonResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val updatedGifticon = if (Build.VERSION.SDK_INT >= 33) {
                        it.data?.getParcelableExtra(
                            "updatedGifticon",
                            Gifticon::class.java
                        )
                    } else {
                        it.data?.getParcelableExtra<Gifticon>("updatedGifticon")
                    }
                    val isEdit = it.data?.getBooleanExtra("isEdit", false)
                    Log.d(TAG, "updatedGifticon: $updatedGifticon")
                    Log.d(TAG, "isEdit: $isEdit")
                    if (isEdit == true) {
                        // 기프티콘 수정
                        updatedGifticon?.let { it1 -> gifticonViewModel.updateCoupon(it1) }
                    } else {
                        // 기프티콘 수동 등록
                        binding.tvSort.text = "최신 등록순"
                        gifticonViewModel.sortCouponList("최신 등록순")
                        updatedGifticon?.let { it1 -> gifticonViewModel.addCoupon(it1) }
                    }
                }
            }

        autoAddGifticonResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    val uploadedGifticon = if (Build.VERSION.SDK_INT >= 33) {
                        it.data?.getParcelableExtra(
                            "uploadedGifticon",
                            Gifticon::class.java
                        )
                    } else {
                        it.data?.getParcelableExtra<Gifticon>("uploadedGifticon")
                    }
                    Log.d(TAG, "uploadedGifticon: $uploadedGifticon")
                    binding.tvSort.text = "최신순"
                    gifticonViewModel.sortCouponList("최신순")
                    uploadedGifticon?.let { it1 -> gifticonViewModel.addCoupon(it1) }
                }
            }

        binding.tvSort.setOnClickListener {
            val bottomSheet = SortBottomSheet()
            bottomSheet.show(requireActivity().supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : SortBottomSheet.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        //getBottomSheetData = value
                        /*when (value) {
                            "최신순" -> {
                                binding.tvSort.text = "최신 순"
                                gifticonList.sortByDescending { it.id }
                                giftAdapter.submitList(gifticonList.toList())
                            }

                            "마감임박순" -> {
                                binding.tvSort.text = "마감임박 순"
                                gifticonList.sortBy { it.dueDate }
                                giftAdapter.submitList(gifticonList.toList())

                            }
                        }*/
                        binding.tvSort.text = value
                        gifticonViewModel.sortCouponList(value)
                    }
                })
            }
        }

        binding.ivAddCategory.setOnClickListener {
            showCategoryBottomSheet(categoryList)
        }

        binding.chipGroupCategory.setOnCheckedStateChangeListener { group, checkedId ->
            val selectedCategory = binding.chipGroupCategory.findViewById<Chip>(binding.chipGroupCategory.checkedChipId)
            val categoryName = selectedCategory?.text

            var categoryId: Long? = null

            // categoryList에 있는 카테고리 이름과 같은 카테고리를 찾아서 categoryId를 가져옴
            for (category in categoryList) {
                if (categoryName == category.categoryName) {
                    categoryId = category.id
                }
            }
            if (categoryName == "전체") {
                // 전체 카테고리를 선택한 경우
                // 전체 기프티콘 리스트를 보여줍니다.
                //gifticonViewModel.filterCouponListByCategoryId(0L)
                gifticonViewModel.clearCouponList()
                getAllGifticonListFromServer(0)
                gifticonViewModel.setCategory(CategoryItem(0L, "전체"))
            } else {
                // 전체 카테고리가 아닌 경우
                // 해당 카테고리에 속한 기프티콘 리스트를 보여줍니다.
                //categoryId?.let { gifticonViewModel.filterCouponListByCategoryId(it) }
                gifticonViewModel.clearCouponList()
                categoryId?.let { getCategoryGifticonListFromServer(it, 0) }
                gifticonViewModel.setCategory(CategoryItem(categoryId, categoryName.toString()))
            }

            val currentCategory = gifticonViewModel.selectedCategory.value
            Log.d(TAG, "currentCategory: $currentCategory")
        }

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
        binding.ivProgressBar.visibility = View.VISIBLE
        Glide.with(this)
            .asGif()
            .load(R.drawable.icon_progress_bar)
            .into(binding.ivProgressBar)
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
        val parsedGifticon = extractPlatformFromJson(result, uri)

        /*val intent = Intent(requireActivity(), AutoRegistrationActivity::class.java)
        // parsedGifticon 객체를 intent에 담아서 AutoRegistrationActivity로 전달
        intent.putExtra("PARSED_GIFTICON", parsedGifticon)
        startActivity(intent)*/
        autoAddGifticonResult.launch(Intent(requireActivity(), AutoRegistrationActivity::class.java).apply {
            putExtra("PARSED_GIFTICON", parsedGifticon)
        })
    }

    private fun extractPlatformFromJson(result: String, uri: String): ParsedGifticon {
        val fields = JSONObject(result)
            .getJSONArray("images")
            .getJSONObject(0)
            .getJSONArray("fields")

        val lastField = fields.getJSONObject(fields.length() - 1)
        // 소문자로 변환
        val lastInferText = lastField.getString("inferText").lowercase(Locale.getDefault())

        Log.i(TAG, "lastInferText: $lastInferText")
        if (lastInferText == "toss") {
            // toss 기프티콘
            return extractDataFromToss(fields, uri)
        } else {
            // 카카오톡 기프티콘
            return extractDataFromKakao(fields, uri)
        }
    }

    private fun extractDataFromToss(fields: JSONArray, uri: String): ParsedGifticon {
        val exchangePlace = filterInferTextByCoordinates(fields, 0.0, 980.0, 565.0, 630.0).joinToString(" ")
        val name = filterInferTextByCoordinates(fields, 0.0, 980.0, 632.0, 700.0).joinToString(" ")

        var barcodeNumber: String? = null
        var dueDate: String? = null
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
            if (text == "유효기간") {
                dueDate = fields.getJSONObject(i + 1).getString("inferText")
                barcodeNumber = fields.getJSONObject(i + 2).getString("inferText")
            }
        }
        // 바코드 번호 형식 변환 (111122223333 -> 1111 2222 3333)
        if (barcodeNumber != null) {
            barcodeNumber = if (barcodeNumber.length <= 12) {
                barcodeNumber.replace(Regex("(\\d{4})(\\d{4})(\\d{4})"), "$1 $2 $3")
            } else {
                // 11112222333344 -> 1111 2222 3333 44
                barcodeNumber?.replace(Regex("(\\d{4})(\\d{4})(\\d{4})(\\d{2,4})"), "$1 $2 $3 $4")
            }
        }

        Log.i(TAG, "name: $name")
        Log.i(TAG, "barcodeNumber: $barcodeNumber")
        Log.i(TAG, "exchangePlace: $exchangePlace")
        Log.i(TAG, "dueDate: $dueDate")
        Log.i(TAG, "orderNumber: $orderNumber")
        Log.i(TAG, "amount: $amount")

        return ParsedGifticon(name, uri, barcodeNumber, exchangePlace, dueDate, orderNumber, amount, "toss")
    }


    private fun extractDataFromKakao(fields: JSONArray, uri: String): ParsedGifticon {
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

        return ParsedGifticon(name, uri, barcodeNumber, exchangePlace, dueDate, orderNumber, amount, "kakao")
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
                                /*val intent = Intent(requireActivity(), ManualRegistrationActivity::class.java)
                                startActivity(intent)*/
                                manualAddGifticonResult.launch(Intent(requireActivity(), ManualRegistrationActivity::class.java).apply {
                                    putExtra("isEdit", false)
                                })
                            }
                        }
                    }
                })
            }
        }
    }

    private fun getAllGifticonListFromServer(page: Int) {
        Retrofit2Generator.create(requireActivity()).getAllGifticonList(size = 30, page = page).enqueue(object :
            Callback<GetGifticonListResponse> {
            override fun onResponse(call: Call<GetGifticonListResponse>, response: Response<GetGifticonListResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.dataList?.let { newList ->
                        // newList를 gifticonViewModel의 addCoupon함수를 이용해서 넣어준다.
                        // id가 높은 것 부터 넣어줘야 한다.
                        newList.forEach { gifticon ->
                            gifticonViewModel.addCoupon(gifticon)
                        }
                        gifticonViewModel.sortCouponList(binding.tvSort.text.toString())

                        val allCouponList = gifticonViewModel.allCouponList.value
                        val availableCouponList = gifticonViewModel.availableCouponList.value
                        val usedCouponList = gifticonViewModel.usedCouponList.value

                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun getCategoryListFromServer() {
        Retrofit2Generator.create(requireActivity()).getCategoryList().enqueue(object : Callback<GetCategoryListResponse> {
            override fun onResponse(call: Call<GetCategoryListResponse>, response: Response<GetCategoryListResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val resposeBody = responseBody.data

                        if (resposeBody != null) {
                            for (category in resposeBody) {
                                if (category.categoryName == null) {
                                    continue
                                }
                                if (category.categoryName == "미분류") {
                                    // 가장 마지막에 미분류 카테고리가 추가되게 한다.
                                    categoryList.add(category)
                                    binding.chipGroupCategory.addView(createNewChip(category.categoryName!!))
                                    continue
                                }
                                val chip = category.categoryName!!.let { createNewChip(it) }

                                // 마지막 Chip 뷰의 인덱스를 계산
                                val lastChildIndex = binding.chipGroupCategory.childCount - 1

                                // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                                // 현재 Chip을 바로 그 앞에 추가
                                if (lastChildIndex >= 0) {
                                    binding.chipGroupCategory.addView(chip)
                                } else {
                                    // ChipGroup에 자식이 없는 경우, 그냥 추가
                                    binding.chipGroupCategory.addView(chip)
                                }

                                categoryList.add(category)
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetCategoryListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun deleteGifticon( gifticon: Gifticon) {
        Log.d(TAG, "deleteGifticon: ${gifticon.id}")
        gifticon.id?.let {
            Retrofit2Generator.create(requireActivity()).deleteGifticon(it).enqueue(object : Callback<LogoutUserResponse> {
                override fun onResponse(call: Call<LogoutUserResponse>, response: Response<LogoutUserResponse>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                        val responseBody = response.body()
                        if (responseBody != null) {
                            gifticonViewModel.deleteCoupon(gifticon)
                        }

                    } else {
                        Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<LogoutUserResponse>, t: Throwable) {
                    Log.e(TAG, "Retrofit onFailure: ", t)
                }
            })
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

    override fun onCategoryUpdated(categoryName: String) {
        val categoryRequest = AddCategoryRequest(categoryName)
        // Retrofit을 이용해서 서버에 카테고리 추가 요청
        // 서버에서 카테고리 추가가 완료되면 아래 코드를 실행
        Retrofit2Generator.create(requireActivity()).addCategory(categoryRequest).enqueue(object : Callback<AddCategoryResponse> {
            override fun onResponse(call: Call<AddCategoryResponse>, response: Response<AddCategoryResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    Log.d(TAG, "responseBody category: $responseBody")
                    if (responseBody != null) {
                        val category = responseBody.data
                        // categoryList에 추가
                        if (category != null) {
                            val chip = category.categoryName?.let { createNewChip(it) }
                            val positionToInsert = binding.chipGroupCategory.childCount - 1
                            binding.chipGroupCategory.addView(chip, positionToInsert)
                            categoryList.add(category)
                        }
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<AddCategoryResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
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

    private fun showCategoryBottomSheet(categoryList: List<CategoryItem>) {
        val categoryBottomSheet = CategoryBottomSheet(categoryList, this)
        categoryBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        categoryBottomSheet.show(requireActivity().supportFragmentManager, categoryBottomSheet.tag)
    }

    private fun getCategoryGifticonListFromServer(categoryId: Long, page: Int) {
        Retrofit2Generator.create(requireActivity()).getCategoryGifticonList(categoryId, 30, page).enqueue(object : Callback<GetGifticonListResponse> {
            override fun onResponse(call: Call<GetGifticonListResponse>, response: Response<GetGifticonListResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()
                    responseBody?.data?.dataList?.let { newList ->
                        // newList를 gifticonViewModel의 addCoupon함수를 이용해서 넣어준다.
                        // id가 높은 것 부터 넣어줘야 한다.
                        /*// 기존에 있던 기프티콘 리스트를 모두 지운다.
                        gifticonViewModel.clearCouponList()*/
                        newList.sortedByDescending { it.id }.forEach { gifticon ->
                            gifticonViewModel.addCoupon(gifticon)
                        }
                        gifticonViewModel.sortCouponList(binding.tvSort.text.toString())
                        /*newList.forEach { gifticon ->
                            gifticonViewModel.addCoupon(gifticon)
                        }*/
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
        binding.viewLoading.visibility = View.GONE
        binding.ivProgressBar.visibility = View.GONE
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