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
import com.example.giftmoa.Data.SaveSharedPreference
import com.example.giftmoa.Data.ShareRoomData
import com.example.giftmoa.Data.SharedViewModel
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityShareRoomEditBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
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

    //조건확인
    private var isCheckImage = false
    private var isCheckInviteCode = false
    private var isCheckName = false
    private var isCheckShareRoomName = false
    private var isComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareRoomEditBinding.inflate(layoutInflater)
        setContentView(sBinding.root)

        val charSet = ('0'..'9') + ('a'..'z') + ('A'..'Z')
        val rangeRandom = List(10) {charSet.random()}.joinToString("")
        inviteCode = rangeRandom.toString()
        /*mBinding.editNext.apply {
            setBackgroundResource(R.drawable.round_btn_update_layout)
            setTextColor(ContextCompat.getColor(this@NoticeBoardEditActivity ,R.color.mio_gray_3))
        }*/

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
                shareRoomData = ShareRoomData(shareRoomName, 0, 0, "손민성", inviteCode, imageFile.toString())
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