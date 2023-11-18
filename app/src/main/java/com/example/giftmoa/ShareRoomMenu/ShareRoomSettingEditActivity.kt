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
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.BottomMenu.ShareRoomFragment
import com.example.giftmoa.Data.EditRequestShareRoomData
import com.example.giftmoa.Data.ShareRoomResponseData
import com.example.giftmoa.Data.Team
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.ActivityShareRoomSettingEditBinding
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareRoomSettingEditActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_Gallery = 1
    }

    private lateinit var sBinding : ActivityShareRoomSettingEditBinding
    private var afterRoomName : String? = null
    private var beforeShareRoomData : Team? = null
    private var afterImageFile : Uri? = null
    private var afterShareRoomData : Team? = null

    private var isEdit = false

    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomSettingEditBinding.inflate(layoutInflater)
        setContentView(sBinding.root)

        beforeShareRoomData = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("data", Team::class.java)
        } else {
            intent.getParcelableExtra("data")
        }

        initEditSetting()

        sBinding.shareSettingEditIv.setOnClickListener {
            selectGallery()
        }

        sBinding.settingComplete.setOnClickListener {
            if (afterRoomName != null && afterImageFile != null) {
                isEdit = true
                afterShareRoomData = Team(beforeShareRoomData?.id,
                    beforeShareRoomData?.teamCode,
                    afterRoomName,
                    afterImageFile.toString(),
                    beforeShareRoomData?.teamLeaderNickname,
                    beforeShareRoomData?.teamMembers)

                sendEditShareRoomData(EditRequestShareRoomData(
                    afterRoomName,
                    afterImageFile.toString()
                ))
            } else if (afterRoomName != null){
                isEdit = true
                afterShareRoomData = Team(beforeShareRoomData?.id,
                    beforeShareRoomData?.teamCode,
                    afterRoomName,
                    beforeShareRoomData?.teamImage.toString(),
                    beforeShareRoomData?.teamLeaderNickname,
                    beforeShareRoomData?.teamMembers)

                sendEditShareRoomData(EditRequestShareRoomData(
                    afterRoomName,
                    beforeShareRoomData?.teamImage.toString()
                ))
            } else if (afterImageFile != null){
                isEdit = true
                afterShareRoomData = Team(beforeShareRoomData?.id,
                    beforeShareRoomData?.teamCode,
                    beforeShareRoomData?.teamName,
                    afterImageFile.toString(),
                    beforeShareRoomData?.teamLeaderNickname,
                    beforeShareRoomData?.teamMembers)

                sendEditShareRoomData(EditRequestShareRoomData(
                    beforeShareRoomData?.teamName,
                    afterImageFile.toString()
                ))
            } else {
                afterShareRoomData = Team(beforeShareRoomData?.id,
                    beforeShareRoomData?.teamCode,
                    beforeShareRoomData?.teamName,
                    beforeShareRoomData?.teamImage.toString(),
                    beforeShareRoomData?.teamLeaderNickname,
                    beforeShareRoomData?.teamMembers)

                sendEditShareRoomData(EditRequestShareRoomData(
                    beforeShareRoomData?.teamName,
                    beforeShareRoomData?.teamImage.toString()
                ))
            }
        }


        sBinding.backArrow.setOnClickListener {
            this.finish()
        }
    }

    private fun initEditSetting() {
        if (beforeShareRoomData?.teamName != null) {
            sBinding.setRoomNameEt.setText(beforeShareRoomData?.teamName)
        }
        sBinding.setRoomNameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

            }
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {


            }
            override fun afterTextChanged(editable: Editable) {
                afterRoomName = editable.toString()

                sBinding.setRoomNameEt.clearFocus()
                sBinding.setRoomNameEt.movementMethod = null
            }
        })

        val requestOptions = RequestOptions()
            .centerCrop() // 또는 .fitCenter()
            .override(200, 200) // 원하는 크기로 조절

        println("image" + beforeShareRoomData?.teamImage)
        Glide.with(this@ShareRoomSettingEditActivity)
            .load(beforeShareRoomData?.teamImage)
            .error(R.drawable.image)
            .apply(requestOptions)
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
            .into(sBinding.shareSettingEditIv)
    }

    private fun sendEditShareRoomData(editRequestShareRoomData : EditRequestShareRoomData) {
        Retrofit2Generator.create(this@ShareRoomSettingEditActivity).editShareRoom(beforeShareRoomData?.id?.toInt()!!, editRequestShareRoomData).enqueue(object :
            Callback<ShareRoomResponseData> {
            override fun onResponse(
                call: Call<ShareRoomResponseData>,
                response: Response<ShareRoomResponseData>
            ) {
                if (response.isSuccessful) {
                    Log.d("SettingEdit", "Success")

                    val intent = Intent(this@ShareRoomSettingEditActivity, ShareRoomSettingActivity::class.java).apply {
                        putExtra("flag", 4)
                        putExtra("data", afterShareRoomData)
                    }
                    setResult(RESULT_OK, intent)
                    startActivity(intent)
                    finish()
                } else {
                    println("faafa")
                    Log.d("SETTING EDIT", response.errorBody()?.string()!!)
                    Log.d("message", call.request().toString())
                    println(response.code())
                }
            }

            override fun onFailure(call: Call<ShareRoomResponseData>, t: Throwable) {
                Log.e("ERROR Setting Edit", t.message.toString())
            }

        })
    }

    private fun selectGallery() {
        val writePermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val readPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)

        if (writePermission == PackageManager.PERMISSION_DENIED ||
            readPermission == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, arrayOf( android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                ShareRoomSettingEditActivity.REQUEST_Gallery
            )

        } else {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
            )

            imageResult.launch(intent)
        }
    }

    private fun getRealPathFromURI(uri : Uri) :String {
        val buildName = Build.MANUFACTURER
        if (buildName.equals("Xiaomi")) {
            return uri.path!!
        }
        var col = 0
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri, proj, null, null, null)

        if (cursor!!.moveToFirst()) {
            col = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        }
        val result = cursor.getString(col)
        cursor.close()
        return result
    }

    private val imageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            val imageUri = it.data?.data
            afterImageFile = getRealPathFromURI(imageUri!!).toUri()
            uploadImageToFirebase(imageUri, {
                imageUrl = it
                Log.d("imageUrl", it)
            }, {
                Log.d("error", it.toString())
            })
            imageUri.let {
                CoroutineScope(Dispatchers.Main).launch {
                    Glide.with(this@ShareRoomSettingEditActivity)
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
                        .into(sBinding.shareSettingEditIv)
                }
            }
        }
    }

    fun uploadImageToFirebase(uri: Uri, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().reference.child("teams/${System.currentTimeMillis()}_image.jpeg")
        storageReference.putFile(uri)
            .addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { downloadUri ->
                    onSuccess(downloadUri.toString())
                    //Toast.makeText(this, downloadUri.toString(), Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                onFailure(it)
            }
    }
}