package com.huawei.hms.sample2

import android.Manifest.permission.READ_PHONE_NUMBERS
import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.agconnect.auth.*
import com.huawei.hmf.tasks.Task
import kotlinx.android.synthetic.main.activity_phone_login.*
import kotlinx.android.synthetic.main.bottom_info.*
import java.util.*

//author Ivantsov Alexey
class PhoneActivity : BaseActivity() {
    private val TAG = PhoneActivity::class.simpleName

    var verifyCode: String? = ""
    var hasPhonePermission: Boolean = false
    var phoneNumber: String? = ""

    /**
     * Переменная для внутренней логики - внедрено для демо
     * Если True - AGConnectAuthCredential будет сформирован с паролем
     * Если False - AGConnectAuthCredential будет сформирован с кодом верификации, пароль не требуется
     */
    val credentialType: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    val permissions = arrayOf(
        READ_PHONE_STATE,
        READ_PHONE_NUMBERS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_login)

        checkPermissions()

        btnPhoneCode.setOnClickListener {
            phoneNumber = editTextPhone.text.toString()

            if (phoneNumber.isNullOrEmpty()) {
                Toast.makeText(
                    this@PhoneActivity,
                    "Please put the phone number",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            requestVerificationCode()
        }

        btnPhoneCodeOk.setOnClickListener {
            verifyCode = editTextVerificationCode.text.toString()

            if (verifyCode.isNullOrEmpty()) {
                Toast.makeText(
                    this@PhoneActivity,
                    "Please put the verification code",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            /**
             *  После создания учетной записи пользователь входит в систему со сформированным credential.
             */
            signInToAppGalleryConnect()
        }

        btnCreateUserInAg.setOnClickListener {
            createUserInAppGallery()
        }

        btnPhoneLogout.setOnClickListener {
            logout()
        }
    }

    private fun requestVerificationCode() {
        editTextPhone.isEnabled = false
        btnPhoneCode.isEnabled = false

        val settings = VerifyCodeSettings.newBuilder()
            .action(VerifyCodeSettings.ACTION_REGISTER_LOGIN) //ACTION_REGISTER_LOGIN o ACTION_RESET_PASSWORD
            /**
             *  Минимальный интервал отправки, значения от 30 с до 120 с.
             */
            .sendInterval(30)
            /**
             *  Необязательный параметр. Указывает язык для отправки кода подтверждения.
             *  Значение должно содержать информацию о языке и стране / регионе.
             *  Значением по умолчанию является Locale.getDefault.
             */
            .locale(
                Locale(
                    "ru",
                    "RU"
                )
            ).build()

        /**
         * Запрос на проверку кода для регистрации мобильного номера.
         * Код подтверждения будет отправлен на ваш номер мобильного телефона,
         * поэтому вам нужно убедиться, что номер мобильного телефона правильный.
         */
        val task: Task<VerifyCodeResult> =
            PhoneAuthProvider.requestVerifyCode(
                phoneNumber!!.substring(0, 2),
                phoneNumber!!.substring(2),
                settings
            )
        task.addOnSuccessListener {
            /**
             * Запрос на код подтверждения отправлен успешно.
             */
                verifyResults: VerifyCodeResult ->
            llCodeInput.visibility = View.VISIBLE
            Toast.makeText(
                this@PhoneActivity,
                "Please wait verification code, and then type it and press OK",
                Toast.LENGTH_LONG
            ).show()
        }.addOnFailureListener { e: Exception ->
            Log.e(TAG, e.message.toString())
            val message = checkError(e)
            Toast.makeText(
                this@PhoneActivity,
                message,
                Toast.LENGTH_LONG
            ).show()
            tvResults.text = message
            editTextPhone.isEnabled = true
            btnPhoneCode.isEnabled = true
        }
    }

    private fun createUserInAppGallery() {
        /**
         * Зарегистрирация аккаунта в AppGallery Connect, используя номер мобильного телефона.
         */
        val phoneUser = if (credentialType) {
            PhoneUser.Builder()
                /**Код страны (международный), для России это 7, вводится без знака +*/
                .setCountryCode(phoneNumber!!.substring(1, 2))
                /** Номер телефона без кода страны т.е. 9876543210, вводится без разделителей и доп. символов*/
                .setPhoneNumber(phoneNumber!!.substring(2))
                .setVerifyCode(verifyCode)
                /**
                 * Обязательно.
                 * Если этот параметр установлен, по умолчанию для текущего пользователя должен быть создан пароль,
                 * и в дальнейшем пользователь может войти в систему с помощью пароля.
                 * В противном случае пользователь может войти в систему только с помощью кода подтверждения.
                 */
                .setPassword("password")//TODO() we need request password from user...
                .build()
        } else {
            PhoneUser.Builder()
                /**Код страны (международный), для России это 7, вводится без знака +*/
                .setCountryCode(phoneNumber!!.substring(1, 2))
                /** Номер телефона без кода страны т.е. 9876543210, вводится без разделителей и доп. символов*/
                .setPhoneNumber(phoneNumber!!.substring(2))
                .setVerifyCode(verifyCode)
                .build()
        }
        AGConnectAuth.getInstance().createUser(phoneUser)
            .addOnSuccessListener { result: SignInResult ->
                /**
                 *  После создания учетной записи пользователь входит в систему
                 */
                signInToAppGalleryConnect()
            }
            .addOnFailureListener { e: Exception ->
                Log.e(TAG, e.message.toString())
                val message = checkError(e)
                Toast.makeText(
                    this@PhoneActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                tvResults.text = message
            }
    }

    private fun signInToAppGalleryConnect() {
        /** Формируем AGConnectAuthCredential */
        val credential: AGConnectAuthCredential = if (credentialType) {
            /** С паролем */
            PhoneAuthProvider.credentialWithPassword(
                /**Код страны (международный), для России это 7, вводится без знака +*/
                phoneNumber!!.substring(1, 2),
                /** Номер телефона без кода страны т.е. 9876543210, вводится без разделителей и доп. символов*/
                phoneNumber!!.substring(2),
                "password"//TODO() we need request password from user...
            )
        } else {
            /** с кодом верификации, пароль опционален*/
            PhoneAuthProvider.credentialWithVerifyCode(
                /**Код страны (международный), для России это 7, вводится без знака +*/
                phoneNumber!!.substring(1, 2),
                /** Номер телефона без кода страны т.е. 9876543210, вводится без разделителей и доп. символов*/
                phoneNumber!!.substring(2),
                /** пароль опционален*/
                "",
                verifyCode
            )
        }
        /**Осуществляем вход.*/
        AGConnectAuth.getInstance().signIn(credential)
            .addOnSuccessListener { signInResult: SignInResult ->
                llCodeInput.visibility = View.GONE
                val user = signInResult.user
                Toast.makeText(this@PhoneActivity, user.uid, Toast.LENGTH_LONG).show()
                tvResults.text = getUserInfo(user, ivProfile)
                btnPhoneCode.visibility = View.GONE
            }
            .addOnFailureListener { e: Exception ->
                Log.e(TAG, e.message.toString())
                val message = checkError(e)
                Toast.makeText(
                    this@PhoneActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
                tvResults.text = message
                /** Если получаем ошибку AGCAuthException.USER_NOT_REGISTERED,
                 * то начинаем регистрацию пользователя в AGC
                 */
                if (e is AGCAuthException && e.code == AGCAuthException.USER_NOT_REGISTERED) {
                    btnCreateUserInAg.visibility = View.VISIBLE
                }
            }
    }

    override fun onResume() {
        super.onResume()
        /** Проверяем наличие текущего уже авторизированного пользователя*/
        if (AGConnectAuth.getInstance().currentUser != null) {
            tvResults.text = getUserInfo(AGConnectAuth.getInstance().currentUser, ivProfile)
            btnPhoneCode.visibility = View.VISIBLE
            llCodeInput.visibility = View.GONE
            btnCreateUserInAg.visibility = View.GONE
            btnPhoneLogout.visibility = View.GONE
        }

        /** Делаем попытку достать номер телефона и подставить его в поле ввода*/
        if (hasPhonePermission) {
            phoneNumber = getPhoneNumberFromTelephony(this)
            editTextPhone.setText(phoneNumber)
        }
    }

    override fun logout() {
        super.logout()
        editTextPhone.apply {
            isEnabled = true
            visibility = View.VISIBLE
        }
        btnPhoneCode.visibility = View.VISIBLE
        llCodeInput.visibility = View.GONE
        btnCreateUserInAg.visibility = View.GONE
        btnPhoneLogout.visibility = View.GONE
    }

    /** Делаем попытку достать номер телефона*/
    @SuppressLint("MissingPermission", "HardwareIds")
    fun getPhoneNumberFromTelephony(context: Context): String? {
        try {
            val telephonyManager =
                context.applicationContext
                    .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            return telephonyManager.line1Number
        } catch (securityException: SecurityException) {
        }
        return null
    }

    /** Проверяем наличие разрешений READ_PHONE_STATE & READ_PHONE_NUMBERS для получения номера*/
    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(this, READ_PHONE_NUMBERS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            /** Проверяем, должны ли мы показать дополнительное уведомление*/
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_PHONE_STATE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, READ_PHONE_NUMBERS)
            ) {
                showPhonePermissionRationale()
            } else {
                requestStoragePermissions()
            }
        } else {
            hasPhonePermission = true
        }
    }

    /** Запрпшиваем разрешения READ_PHONE_STATE & READ_PHONE_NUMBERS для получения номера*/
    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(this, permissions, 1)
    }

    /** Показываем уведомление о том что нам нужны разрешения READ_PHONE_STATE & READ_PHONE_NUMBERS для получения номера*/
    private fun showPhonePermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_request_phone_title)
            .setMessage(R.string.permission_request_phone_description)
            .setPositiveButton(android.R.string.ok) { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .setOnDismissListener { requestStoragePermissions() }
            .show()
    }

    @SuppressLint("SetTextI18n")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 1 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                hasPhonePermission = true
                phoneNumber = getPhoneNumberFromTelephony(this)
                editTextPhone.setText(phoneNumber)
            } else {
                Log.i(
                    TAG,
                    "onRequestPermissionsResult: apply READ_PHONE_STATE & READ_PHONE_NUMBERS PERMISSION failed"
                )
                tvResults.text =
                    "onRequestPermissionsResult: apply READ_PHONE_STATE PERMISSION &READ_PHONE_NUMBERS failed"
            }
        }
    }
}