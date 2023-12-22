package com.example.giftmoa

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.core.content.ContextCompat
import com.example.giftmoa.databinding.ActivityLockScreenBinding
import java.util.concurrent.Executor

//지문 인증 사용 시 계정에서 인증 사용 여부 동의여부를 표시하도록함 Todo
class LockScreenActivity : AppCompatActivity() {
    companion object {
        const val TAG : String = "LockScreenActivity"
    }

    private lateinit var lBinding : ActivityLockScreenBinding

    private var executor : Executor? = null
    private var biometricPrompt : BiometricPrompt? = null
    private var promptInfo : BiometricPrompt.PromptInfo? = null

    private val loginLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "registerForActivityResult - result : $result")

            if (result.resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "registerForActivityResult - RESULT_OK")
                authenticateToEncrypt()  //생체 인증 가능 여부확인 다시 호출
            } else {
                Log.d(TAG, "registerForActivityResult - NOT RESULT_OK")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(lBinding.root)
        //권한 확인
        /*val biometricPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC)
        if (biometricPermission == PackageManager.PERMISSION_GRANTED) {
            // 이미 권한이 허용된 상태
            // 지문인식 관련 작업 수행
        } else {
            // 권한이 없는 경우, 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.USE_BIOMETRIC), REQUEST_BIOMETRIC_PERMISSION)
        }*/

        //권한 요청 처리
        /*override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)

            when (requestCode) {
                REQUEST_BIOMETRIC_PERMISSION -> {
                    if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // 권한이 허용된 경우
                        // 지문인식 관련 작업 수행
                    } else {
                        // 권한이 거부된 경우
                        // 사용자에게 권한이 필요하다는 메시지를 표시하거나 다른 처리 수행
                    }
                }
            }
        }


        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // 권한이 허용된 경우
        showBiometricPrompt()
        }


        companion object {
            private const val REQUEST_BIOMETRIC_PERMISSION = 123
        }
        */


        biometricPrompt = setBiometricPrompt()
        promptInfo = setPromptInfo()

        //지문 인증 호출 버튼 클릭 시
        lBinding.bioAuthenticate.setOnClickListener {
            authenticateToEncrypt()  //생체 인증 가능 여부확인
        }
    }

    //인증 화면 dialog 내용 세팅
    private fun setPromptInfo(): BiometricPrompt.PromptInfo {
        val promptBuilder: BiometricPrompt.PromptInfo.Builder = BiometricPrompt.PromptInfo.Builder()

        promptBuilder.setTitle("Biometric login for my app")
        promptBuilder.setSubtitle("Log in using your biometric credential")
        promptBuilder.setNegativeButtonText("Use account password")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { //  안면인식 ap사용 android 11부터 지원
            promptBuilder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        }

        promptInfo = promptBuilder.build()
        return promptInfo as BiometricPrompt.PromptInfo
    }

    //생체인증 준비완료 표시
    private fun setBiometricPrompt(): BiometricPrompt {
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this@LockScreenActivity, executor!!, object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(this@LockScreenActivity, """"지문 인식 ERROR [ errorCode: $errorCode, errString: $errString ]""".trimIndent(), Toast.LENGTH_SHORT).show()
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Toast.makeText(this@LockScreenActivity, "지문 인식 성공", Toast.LENGTH_SHORT).show()
                //성공했을 때 값 설정해놓기 바로 main으로 이동?
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Toast.makeText(this@LockScreenActivity, "지문 인식 실패", Toast.LENGTH_SHORT).show()
            }

        } )
        return biometricPrompt as BiometricPrompt
    }

    //생체 인식 인증을 사용할 수 있는지 확인 (인증 암호화)
    private fun authenticateToEncrypt() = with(lBinding) {

        Log.d(TAG, "authenticateToEncrypt() ")

        var textStatus = ""
        val biometricManager = BiometricManager.from(this@LockScreenActivity)
//        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)) {

        //이 부분은 현재 기기에서 생체 인증이 가능한지 여부를 확인
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {

            //생체 인증 가능
            BiometricManager.BIOMETRIC_SUCCESS -> textStatus = "App can authenticate using biometrics."

            //기기에서 생체 인증을 지원하지 않는 경우
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> textStatus = "No biometric features available on this device."

            //현재 생체 인증을 사용할 수 없는 경우
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> textStatus = "Biometric features are currently unavailable."

            //생체 인식 정보가 등록되어 있지 않은 경우
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                textStatus = "Prompts the user to create credentials that your app accepts."

                val dialogBuilder = AlertDialog.Builder(this@LockScreenActivity)
                dialogBuilder
                    .setTitle("Moa")
                    .setMessage("지문 등록이 필요합니다. 지문 등록 설정 화면으로 이동하시겠습니까?")
                    .setPositiveButton("확인") { dialog, which -> moveBiometricSettings() }
                    .setNegativeButton("취소") {dialog, which -> dialog.cancel() }
                dialogBuilder.show()
            }

            //기타 실패
            else ->  textStatus = "Fail Biometric facility"

        }
        //lBinding.tvStatus.text = textStatus

        //인증 실행하기
        startAuthenticate()
    }

    //생체 인증 실행
    private fun startAuthenticate() {
        Log.d(TAG, "goAuthenticate - promptInfo : $promptInfo")
        promptInfo?.let {
            biometricPrompt?.authenticate(it);  //인증 실행
        }
    }

    //설정의 지문 등록 화면으로 이동
    private fun moveBiometricSettings() {
        val enrollIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(
                    Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            }
        } else {
            TODO("VERSION.SDK_INT < R")
        }
        loginLauncher.launch(enrollIntent)
    }

}