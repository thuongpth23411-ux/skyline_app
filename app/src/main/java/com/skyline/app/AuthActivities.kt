package com.skyline.app

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.skyline.app.network.LoginRequest
import com.skyline.app.network.RetrofitClient
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.regex.Pattern

private const val TAG = "AuthDebug"

open class BaseAuthActivity : AppCompatActivity() {
    protected fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }

    protected fun setupHomeButton() {
        findViewById<View?>(R.id.btnHome)?.setOnClickListener { goHome() }
    }

    protected fun setupPasswordVisibility(editText: EditText) {
        var isPasswordVisible = false
        editText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = editText.compoundDrawables[2]
                if (drawableEnd != null && event.x >= (editText.width - editText.paddingEnd - drawableEnd.bounds.width())) {
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                        editText.setCompoundDrawablesWithIntrinsicBounds(editText.compoundDrawables[0], null, AppCompatResources.getDrawable(this, R.drawable.ic_eye_off_auth), null)
                    } else {
                        editText.transformationMethod = PasswordTransformationMethod.getInstance()
                        editText.setCompoundDrawablesWithIntrinsicBounds(editText.compoundDrawables[0], null, AppCompatResources.getDrawable(this, R.drawable.ic_eye_auth), null)
                    }
                    editText.setSelection(editText.text.length)
                    v.performClick()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    protected fun setupOtpInputs(inputs: List<EditText>) {
        for (i in inputs.indices) {
            inputs[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < inputs.size - 1) {
                        inputs[i + 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: Editable?) {}
            })

            inputs[i].setOnKeyListener { _, keyCode, event ->
                if (event.action == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                    if (inputs[i].text.isEmpty() && i > 0) {
                        inputs[i - 1].requestFocus()
                        inputs[i - 1].setText("")
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    protected fun updateRequirementStatus(textView: TextView, isValid: Boolean) {
        val color = if (isValid) getColor(R.color.auth_success) else getColor(R.color.auth_hint)
        textView.setTextColor(color)
        textView.compoundDrawablesRelative[0]?.setTint(color)
    }
}

class LoginActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupHomeButton()
        
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        setupPasswordVisibility(edtPassword)
        
        findViewById<View>(R.id.tvForgot).setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
        findViewById<View>(R.id.tvRegister).setOnClickListener { startActivity(Intent(this, RegisterEmailActivity::class.java)) }
        
        findViewById<View>(R.id.btnLogin).setOnClickListener {
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.login(LoginRequest(email, password))
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@LoginActivity, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                        goHome()
                    } else {
                        Toast.makeText(this@LoginActivity, response.body()?.message ?: "Đăng nhập thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@LoginActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

class RegisterEmailActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_email)
        setupHomeButton()
        
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        
        findViewById<View>(R.id.btnContinue).setOnClickListener {
            val email = edtEmail.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val btn = it
            btn.isEnabled = false
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.sendOtpReg(com.skyline.app.network.ForgotPasswordRequest(email))
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@RegisterEmailActivity, "Mã OTP đã được gửi đến email", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@RegisterEmailActivity, PhoneOtpActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        intent.putExtra("IS_REGISTER", true)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@RegisterEmailActivity, response.body()?.message ?: "Gửi OTP thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@RegisterEmailActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btn.isEnabled = true
                }
            }
        }
    }
}

class SetPasswordActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)
        setupHomeButton()
        
        val email = intent.getStringExtra("EMAIL")
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val edtConfirm = findViewById<EditText>(R.id.edtConfirm)
        val btnContinue = findViewById<android.view.View>(R.id.btnContinue)
        
        val tvReqLength = findViewById<TextView>(R.id.tvReqLength)
        val tvReqComplex = findViewById<TextView>(R.id.tvReqComplex)
        val tvReqSpace = findViewById<TextView>(R.id.tvReqSpace)

        setupPasswordVisibility(edtPassword)
        setupPasswordVisibility(edtConfirm)

        var isLengthValid = false
        var isComplexValid = false
        var isSpaceValid = false

        edtPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s?.toString() ?: ""
                
                isLengthValid = password.length >= 8
                isComplexValid = password.any { it.isUpperCase() } && 
                                 password.any { it.isLowerCase() } && 
                                 password.any { it.isDigit() } && 
                                 password.any { !it.isLetterOrDigit() }
                isSpaceValid = password.isNotEmpty() && !password.contains(" ")

                updateRequirementStatus(tvReqLength, isLengthValid)
                updateRequirementStatus(tvReqComplex, isComplexValid)
                updateRequirementStatus(tvReqSpace, isSpaceValid)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        btnContinue.setOnClickListener {
            val password = edtPassword.text.toString()
            val confirm = edtConfirm.text.toString()
            
            if (!isLengthValid || !isComplexValid || !isSpaceValid) {
                Toast.makeText(this, "Mật khẩu chưa đáp ứng đủ yêu cầu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirm) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val intent = Intent(this, CompleteInfoActivity::class.java)
            intent.putExtra("EMAIL", email)
            intent.putExtra("PASSWORD", password)
            startActivity(intent)
        }
    }
}

class CompleteInfoActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_info)
        setupHomeButton()
        
        val email = intent.getStringExtra("EMAIL") ?: ""
        val password = intent.getStringExtra("PASSWORD") ?: ""
        
        val tvCountryCode = findViewById<TextView>(R.id.tvCountryCode)
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val edtFirstName = findViewById<EditText>(R.id.edtFirstName)
        val edtLastName = findViewById<EditText>(R.id.edtLastName)
        val edtDob = findViewById<EditText>(R.id.edtDob)
        val tvCountry = findViewById<TextView>(R.id.tvCountry)
        val cbAgree = findViewById<android.widget.CheckBox>(R.id.cbAgree)

        tvCountryCode.setOnClickListener {
            showListSelector("Chọn mã vùng", arrayOf("+84 (Việt Nam)", "+66 (Thái Lan)", "+65 (Singapore)", "+60 (Malaysia)")) { selected ->
                tvCountryCode.text = selected.split(" ")[0]
            }
        }

        tvTitle.setOnClickListener {
            showListSelector("Chọn danh xưng", arrayOf("Ông", "Bà", "Anh", "Chị")) { selected ->
                tvTitle.text = selected
            }
        }

        tvCountry.setOnClickListener {
            showListSelector("Chọn quốc gia", arrayOf("Việt Nam", "Thái Lan", "Singapore", "Malaysia", "Hàn Quốc", "Nhật Bản")) { selected ->
                tvCountry.text = selected
            }
        }

        edtDob.setOnClickListener { showDatePicker(edtDob) }
        
        findViewById<View>(R.id.btnContinue).setOnClickListener {
            val phone = edtPhone.text.toString().trim()
            val firstName = edtFirstName.text.toString().trim()
            val lastName = edtLastName.text.toString().trim()
            val dob = edtDob.text.toString().trim()
            
            if (dob.isNotEmpty() && !isValidDate(dob)) {
                Toast.makeText(this, "Ngày sinh không đúng định dạng DD/MM/YYYY", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.isNotEmpty() && !isValidPhone(phone)) {
                Toast.makeText(this, "Số điện thoại không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val name = "$firstName $lastName".trim().let { if (it.isEmpty()) "User" else it }
            val btn = it
            btn.isEnabled = false
            
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.registerFinalize(com.skyline.app.network.RegisterRequest(
                        email = email,
                        password = password,
                        name = name,
                        phone = if (phone.isEmpty()) null else phone
                    ))
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@CompleteInfoActivity, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@CompleteInfoActivity, AccountSuccessActivity::class.java))
                        finishAffinity() // Clear all auth activities
                    } else {
                        Toast.makeText(this@CompleteInfoActivity, response.body()?.message ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CompleteInfoActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    btn.isEnabled = true
                }
            }
        }
        findViewById<View>(R.id.tvSkip).setOnClickListener { 
             val skipView = it
             skipView.isEnabled = false
             lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.registerFinalize(com.skyline.app.network.RegisterRequest(
                        email = email,
                        password = password
                    ))
                    if (response.isSuccessful && response.body()?.success == true) {
                         startActivity(Intent(this@CompleteInfoActivity, AccountSuccessActivity::class.java))
                         finishAffinity()
                    } else {
                        Toast.makeText(this@CompleteInfoActivity, response.body()?.message ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CompleteInfoActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    skipView.isEnabled = true
                }
            }
        }
    }

    private fun showListSelector(title: String, items: Array<String>, onSelected: (String) -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setItems(items) { _, which -> onSelected(items[which]) }
            .show()
    }

    private fun showDatePicker(editText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR) - 18 // Default to 18 years ago
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val date = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
            editText.setText(date)
        }, year, month, day).show()
    }

    private fun isValidDate(date: String): Boolean {
        val regex = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[012])/(19|20)\\d\\d$"
        return Pattern.compile(regex).matcher(date).matches()
    }

    private fun isValidPhone(phone: String): Boolean {
        return phone.length in 9..11 && phone.all { it.isDigit() }
    }
}

class PhoneOtpActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        setupHomeButton()
        
        val email = intent.getStringExtra("EMAIL") ?: ""
        val isRegister = intent.getBooleanExtra("IS_REGISTER", false)
        
        val otpInputs = listOf<EditText>(
            findViewById(R.id.edtOtp1), findViewById(R.id.edtOtp2),
            findViewById(R.id.edtOtp3), findViewById(R.id.edtOtp4),
            findViewById(R.id.edtOtp5), findViewById(R.id.edtOtp6)
        )
        setupOtpInputs(otpInputs)
        
        findViewById<View>(R.id.btnVerify).setOnClickListener {
            val otp = otpInputs.joinToString("") { it.text.toString() }
            
            if (otp.length < 6) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.verifyOtp(com.skyline.app.network.VerifyOtpRequest(email, otp))
                    if (response.isSuccessful && response.body()?.success == true) {
                        if (isRegister) {
                            // Go to Set Password for Registration
                            val intent = Intent(this@PhoneOtpActivity, SetPasswordActivity::class.java)
                            intent.putExtra("EMAIL", email)
                            intent.putExtra("OTP", otp)
                            startActivity(intent)
                        } else {
                            // Existing success behavior (for other flows)
                            startActivity(Intent(this@PhoneOtpActivity, AccountSuccessActivity::class.java))
                        }
                    } else {
                        Toast.makeText(this@PhoneOtpActivity, response.body()?.message ?: "Xác thực thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@PhoneOtpActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<View>(R.id.tvBack).setOnClickListener { finish() }
    }
}

class ForgotPasswordActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        setupHomeButton()
        
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        
        findViewById<View>(R.id.btnOtp).setOnClickListener {
            val email = edtEmail.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.forgotPassword(com.skyline.app.network.ForgotPasswordRequest(email))
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ForgotPasswordActivity, "Mã OTP đã được gửi", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@ForgotPasswordActivity, ForgotOtpActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@ForgotPasswordActivity, response.body()?.message ?: "Gửi OTP thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ForgotPasswordActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<android.view.View>(R.id.tvBackLogin).setOnClickListener { finish() }
    }
}

class ForgotOtpActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        setupHomeButton()
        
        val email = intent.getStringExtra("EMAIL") ?: ""
        val otpInputs = listOf<EditText>(
            findViewById(R.id.edtOtp1), findViewById(R.id.edtOtp2),
            findViewById(R.id.edtOtp3), findViewById(R.id.edtOtp4),
            findViewById(R.id.edtOtp5), findViewById(R.id.edtOtp6)
        )
        setupOtpInputs(otpInputs)
        
        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnVerify).setOnClickListener {
            val otp = otpInputs.joinToString("") { it.text.toString() }
            
            if (otp.length < 6) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.verifyOtp(com.skyline.app.network.VerifyOtpRequest(email, otp))
                    if (response.isSuccessful && response.body()?.success == true) {
                        val intent = Intent(this@ForgotOtpActivity, ResetPasswordActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        intent.putExtra("OTP", otp)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@ForgotOtpActivity, response.body()?.message ?: "Xác thực thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ForgotOtpActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

class ResetPasswordActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        setupHomeButton()
        
        val email = intent.getStringExtra("EMAIL") ?: ""
        val otp = intent.getStringExtra("OTP") ?: ""
        
        val edtNewPass = findViewById<EditText>(R.id.edtNewPass)
        val edtConfirm = findViewById<EditText>(R.id.edtConfirm)
        val btnDone = findViewById<android.view.View>(R.id.btnDone)

        val tvReqLength = findViewById<TextView>(R.id.tvReqLength)
        val tvReqComplex = findViewById<TextView>(R.id.tvReqComplex)
        val tvReqSpace = findViewById<TextView>(R.id.tvReqSpace)

        setupPasswordVisibility(edtNewPass)
        setupPasswordVisibility(edtConfirm)

        var isLengthValid = false
        var isComplexValid = false
        var isSpaceValid = false

        edtNewPass.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s?.toString() ?: ""
                isLengthValid = password.length >= 8
                isComplexValid = password.any { it.isUpperCase() } && 
                                 password.any { it.isLowerCase() } && 
                                 password.any { it.isDigit() } && 
                                 password.any { !it.isLetterOrDigit() }
                isSpaceValid = password.isNotEmpty() && !password.contains(" ")

                updateRequirementStatus(tvReqLength, isLengthValid)
                updateRequirementStatus(tvReqComplex, isComplexValid)
                updateRequirementStatus(tvReqSpace, isSpaceValid)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        
        btnDone.setOnClickListener {
            val newPass = edtNewPass.text.toString()
            val confirm = edtConfirm.text.toString()
            
            if (!isLengthValid || !isComplexValid || !isSpaceValid) {
                Toast.makeText(this, "Mật khẩu chưa đáp ứng đủ yêu cầu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass != confirm) {
                Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.resetPassword(com.skyline.app.network.ResetPasswordRequest(email, otp, newPass))
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ResetPasswordActivity, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@ResetPasswordActivity, ResetSuccessActivity::class.java))
                    } else {
                        Toast.makeText(this@ResetPasswordActivity, response.body()?.message ?: "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@ResetPasswordActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<View>(R.id.tvBack).setOnClickListener { finish() }
    }
}

class ResetSuccessActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
        setupHomeButton()
        findViewById<View>(R.id.btnPrimary).setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
        findViewById<View>(R.id.tvHome).setOnClickListener { goHome() }
    }
}

class AccountSuccessActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_success)
        setupHomeButton()
        findViewById<View>(R.id.btnExplore).setOnClickListener { goHome() }
    }
}
