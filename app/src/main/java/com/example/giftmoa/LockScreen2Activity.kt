package com.example.giftmoa

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.example.giftmoa.databinding.ActivityLockScreen2Binding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockScreen2Activity : AppCompatActivity() { //비밀번호

    private lateinit var mBinding : ActivityLockScreen2Binding
    private var savedPasswords = "" //나중에 비밀번호 설정하고 나서 불러올 때 사용될 변수
    private var type : String? = "" //create 비밀번호 처음 저장 시 사용할 type, use 비밀번호 저장된 거 불러올때 사용할 type
    private var userPassword = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityLockScreen2Binding.inflate(layoutInflater)
        setContentView(mBinding.root)

        type = intent.getStringExtra("type")

        if (type == "create") {
            mBinding.lockTv.text = "비밀번호 설정"
        }

        // pass1에 포커스 주기
        mBinding.pass1.requestFocus()

        // 키보드 나타나게 하기
        mBinding.pass1.postDelayed({
            mBinding.pass1.showSoftInputOnFocus = true
        }, 200)

        mBinding.pass1.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable?.length == 1) {
                    userPassword.plus(editable.toString())
                    // 한 글자가 입력된 경우, 다음 EditText로 포커스 이동
                    mBinding.pass2.requestFocus()

                    CoroutineScope(Dispatchers.Main).launch {
                        mBinding.pass1.text = null
                        mBinding.pass1.setBackgroundResource(R.drawable.moa_character_default)
                    }
                }
            }
        })

        mBinding.pass2.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                /*if (count == 1) {
                    mBinding.pass3.requestFocus()
                }*/
            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable?.length == 1) {
                    userPassword.plus(editable.toString())

                    // 한 글자가 입력된 경우, 다음 EditText로 포커스 이동
                    mBinding.pass3.requestFocus()
                    CoroutineScope(Dispatchers.Main).launch {
                        mBinding.pass2.text = null
                        mBinding.pass2.setBackgroundResource(R.drawable.moa_character_default)
                    }
                }
            }
        })

        mBinding.pass3.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                /*if (count == 1) {
                    mBinding.pass4.requestFocus()
                }*/
            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable?.length == 1) {
                    userPassword.plus(editable.toString())
                    // 한 글자가 입력된 경우, 다음 EditText로 포커스 이동
                    mBinding.pass4.requestFocus()
                    CoroutineScope(Dispatchers.Main).launch {
                        mBinding.pass3.text = null
                        mBinding.pass3.setBackgroundResource(R.drawable.moa_character_default)
                    }
                }
            }
        })

        mBinding.pass4.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
                /*if (count == 1) {
                    mBinding.pass4.requestFocus()
                }*/
            }

            override fun afterTextChanged(editable: Editable?) {
                if (editable?.length == 1) {
                    userPassword.plus(editable.toString())
                    CoroutineScope(Dispatchers.Main).launch {
                        mBinding.pass4.text = null
                        mBinding.pass4.setBackgroundResource(R.drawable.moa_character_default)
                    }
                    handlePasswordComplete(type, userPassword)
                }
            }
        })

        /*mBinding.pass3.setOnEditorActionListener { _, _, _ ->
            mBinding.pass4.requestFocus()
            true
        }*/

        // pass4에 "완료" 버튼 설정
        mBinding.pass4.imeOptions = EditorInfo.IME_ACTION_DONE
        //mBinding.pass4.setRawInputType(InputType.TYPE_CLASS_NUMBER)

        // pass4의 입력이 완료되면 호출되는 리스너 설정
        mBinding.pass4.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER) {
                // 키보드의 "완료" 버튼을 눌렀을 때 원하는 동작 수행
                // 여기서는 입력이 모두 완료되었으므로 다음 작업 수행
                handlePasswordComplete(type, userPassword)
                true
            } else {
                false
            }
        }
    }

    private fun handlePasswordComplete(type : String?, password : String?) {
        if (type == "create") {
            val sharedPref = getSharedPreferences("SavedPassword", Context.MODE_PRIVATE)
            with (sharedPref.edit()) {
                putString("password", password?.toString())
                apply()
            }
            Toast.makeText(this@LockScreen2Activity, "비밀번호가 설정되었습니다", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val sharedPref = getSharedPreferences("SavedPassword", Context.MODE_PRIVATE)
            val getPassword = sharedPref.getString("password", null)
            if (password == getPassword) {
                val intent = Intent(this@LockScreen2Activity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            else {
                mBinding.pass1.text = null
                mBinding.pass1.setBackgroundResource(R.drawable.lock_screen_password_background)

                mBinding.pass2.text = null
                mBinding.pass2.setBackgroundResource(R.drawable.lock_screen_password_background)

                mBinding.pass3.text = null
                mBinding.pass3.setBackgroundResource(R.drawable.lock_screen_password_background)

                mBinding.pass4.text = null
                mBinding.pass4.setBackgroundResource(R.drawable.lock_screen_password_background)

                userPassword = ""

                mBinding.pass1.requestFocus()

                Toast.makeText(this@LockScreen2Activity, "비밀번호를 다시 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }
    }
}