package com.example.giftmoa.ShareRoomMenu

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.giftmoa.BottomMenu.ShareRoomFragment
import com.example.giftmoa.BuildConfig
import com.example.giftmoa.Data.*
import com.example.giftmoa.MoaInterface
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityShareRoomEditBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class ShareRoomEditActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_Gallery = 1
    }

    private lateinit var sBinding : ActivityShareRoomEditBinding
    private var inviteCode = ""
    private var shareRoomData : ShareRoomData? = null
    private var imageFile : Uri? = null
    private var shareRoomName = ""
    private var saveSharedPreference = SaveSharedPreference()
    private var myViewModel = SharedViewModel()
    private var type = ""

    private var tempTeamData : TeamCreateData? = null

    //조건확인
    private var isCheckImage = false
    private var isCheckInviteCode = false
    private var isCheckName = false
    private var isCheckShareRoomName = false
    private var isComplete = false

    private val SERVER_URL = BuildConfig.server_URL
    private val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val service: MoaInterface = retrofit.create(MoaInterface::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomEditBinding.inflate(layoutInflater)
        setContentView(sBinding.root)

        type = intent.getStringExtra("type").toString()


        val charSet = ('0'..'9') + ('a'..'z') + ('A'..'Z')
        val rangeRandom = List(10) {charSet.random()}.joinToString("")
        inviteCode = rangeRandom.toString()
        /*mBinding.editNext.apply {
            setBackgroundResource(R.drawable.round_btn_update_layout)
            setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_3))
        }*/

        if (type == "ADD") {

        }

        sBinding.shareBackgroundImageRegistrationTv.setOnClickListener {
            selectGallery()
        }

        sBinding.shareBackgroundImageRegistrationIv.setOnClickListener {
            selectGallery()
        }

        sBinding.shareNameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                /*if (type == "EDIT") {
                    mBinding.editTitle.setText(eTemp!!.postTitle)
                    editTitle = eTemp!!.postTitle
                }*/
            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun afterTextChanged(editable: Editable) {
                shareRoomName = editable.toString()
                /*if (editable.isEmpty()) {
                    Toast.makeText("")
                }*/
                //깜빡임 제거
                sBinding.shareNameEt.clearFocus()
                sBinding.shareNameEt.movementMethod = null
                isCheckShareRoomName = true
                myViewModel.postCheckComplete(complete = true)
            }
        })


        myViewModel.checkComplete.observe(this) {
            if (it) { //saveSharedPreference.getName(this@ShareRoomEditActivity).toString() 이름 부분 안받아와지는거 수정하기 Todo
                shareRoomData = ShareRoomData(shareRoomName, 0, 0,
                    saveSharedPreference.getName(this@ShareRoomEditActivity).toString(), inviteCode, imageFile.toString())
                println(shareRoomData)
                CoroutineScope(Main).launch {
                    sBinding.editNext.apply {
                        setBackgroundResource(R.drawable.share_room_bottom_update_btn_round_layout)
                        setTextColor(ContextCompat.getColor(this@ShareRoomEditActivity ,R.color.moa_gray_white))
                    }
                }
                sBinding.editNext.setOnClickListener {
                    isComplete = !isComplete
                    myViewModel.postCheckComplete(false)

                    val saveSharedPreference = SaveSharedPreference()
                    val token = saveSharedPreference.getToken(this).toString()
                    val getExpireDate = saveSharedPreference.getExpireDate(this).toString()
                    /*var interceptor = HttpLoggingInterceptor()
                interceptor = interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                val client = OkHttpClient.Builder().addInterceptor(interceptor).build()*/

                    /*val retrofit = Retrofit.Builder().baseUrl("url 주소")
                        .addConverterFactory(GsonConverterFactory.create())
                        //.client(client) 이걸 통해 통신 오류 log찍기 가능
                        .build()
                    val service = retrofit.create(MioInterface::class.java)*/
                    //통신로그

                    /*val loggingInterceptor = HttpLoggingInterceptor()
                    loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                    val clientBuilder = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()*/
                    //통신
                    val SERVER_URL = BuildConfig.server_URL
                    val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                    //.client(clientBuilder)

                    //Authorization jwt토큰 로그인
                    val interceptor = Interceptor { chain ->

                        var newRequest: Request
                        if (token != null && token != "") { // 토큰이 없는 경우
                            // Authorization 헤더에 토큰 추가
                            newRequest =
                                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                            val expireDate: Long = getExpireDate.toLong()
                            if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                                //refresh 들어갈 곳
                                newRequest =
                                    chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                                return@Interceptor chain.proceed(newRequest)
                            }
                        } else newRequest = chain.request()
                        chain.proceed(newRequest)
                    }
                    val builder = OkHttpClient.Builder()
                    builder.interceptors().add(interceptor)
                    val client: OkHttpClient = builder.build()
                    retrofit.client(client)
                    val retrofit2: Retrofit = retrofit.build()
                    val api = retrofit2.create(MoaInterface::class.java)

                    tempTeamData = TeamCreateData(shareRoomName, imageFile.toString())
                    CoroutineScope(Dispatchers.IO).launch {
                        /*"application/json; charset=UTF-8",*/
                        api.createShareRoom(tempTeamData!!).enqueue(object :
                            Callback<ShareRoomResponseData> {
                            override fun onResponse(
                                call: Call<ShareRoomResponseData>,
                                response: Response<ShareRoomResponseData>
                            ) {
                                if (response.isSuccessful) {
                                    println("succcc")
                                    println(response.body()!!.code)
                                } else {
                                    println("faafa")
                                    Log.d("add", response.errorBody()?.string()!!)
                                    Log.d("message", call.request().toString())
                                    println(response.code())
                                }
                            }

                            override fun onFailure(call: Call<ShareRoomResponseData>, t: Throwable) {
                                Log.d("error", t.toString())
                            }

                        })
                    }


                    val intent = Intent(this@ShareRoomEditActivity, ShareRoomFragment::class.java).apply {
                        putExtra("flag", 0)
                        putExtra("data", shareRoomData)
                    }
                    setResult(RESULT_OK, intent)
                    finish()
                }
            } else {
                CoroutineScope(Main).launch {
                    sBinding.editNext.apply {
                        setBackgroundResource(R.drawable.share_room_bottom_btn_round_layout)
                        setTextColor(ContextCompat.getColor(this@ShareRoomEditActivity ,R.color.moa_gray_200))
                    }
                }
                sBinding.editNext.setOnClickListener {

                }
            }
        }
    }

    private fun selectGallery() {
        val writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if (writePermission == PackageManager.PERMISSION_DENIED ||
                readPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf( android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_Gallery)

        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )

            imageResult.launch(intent)
        }
    }

    /*private fun getRealPathFromURI(uri : Uri) :String {
        val buildName = Build.MANUFACTURER
        if (buildName.equals("Xiaomi")) {
            return uri.path!!
        }
        val col = 0
        val proj
    }*/

    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val imageUri = it.data?.data
            imageFile = imageUri
            imageUri?.let {
                CoroutineScope(Main).launch {
                    Glide.with(this@ShareRoomEditActivity)
                        .load(imageUri)
                        .error(R.drawable.image)
                        .centerCrop()
                        .override(200, 200)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                Log.d("Glide", "Image load failed: ${e?.message}")
                                println(e?.message.toString())
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                println("glide")
                                return false
                            }
                        })
                        .into(sBinding.shareBackgroundImageRegistrationIv)
                }
                sBinding.shareBackgroundImageRegistrationTv.visibility = View.GONE
                isCheckImage = true
            }
        }
    }
}