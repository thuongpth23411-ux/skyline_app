package com.skyline.app

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.skyline.app.network.LoginRequest
import com.skyline.app.network.RetrofitClient
import kotlinx.coroutines.launch

open class BaseAuthActivity : AppCompatActivity() {
    protected fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }
    protected fun setupHomeButton() {
        findViewById<android.view.View?>(R.id.btnHome)?.setOnClickListener { goHome() }
    }
}

class LoginActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setupHomeButton()
        
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        
        findViewById<android.view.View>(R.id.tvForgot).setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
        findViewById<android.view.View>(R.id.tvRegister).setOnClickListener { startActivity(Intent(this, RegisterEmailActivity::class.java)) }
        
        findViewById<android.view.View>(R.id.btnLogin).setOnClickListener {
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
        
        findViewById<android.view.View>(R.id.btnContinue).setOnClickListener {
            val email = edtEmail.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, SetPasswordActivity::class.java)
            intent.putExtra("EMAIL", email)
            startActivity(intent)
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
        
        findViewById<android.view.View>(R.id.btnContinue).setOnClickListener {
            val password = edtPassword.text.toString()
            val confirm = edtConfirm.text.toString()
            
            if (password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show()
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
        
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val edtFirstName = findViewById<EditText>(R.id.edtFirstName)
        val edtLastName = findViewById<EditText>(R.id.edtLastName)
        
        findViewById<android.view.View>(R.id.btnContinue).setOnClickListener {
            val phone = edtPhone.text.toString()
            val name = "${edtFirstName.text} ${edtLastName.text}".trim()
            
            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.register(com.skyline.app.network.RegisterRequest(
                        email = email,
                        password = password,
                        name = name,
                        phone = phone
                    ))
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@CompleteInfoActivity, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@CompleteInfoActivity, PhoneOtpActivity::class.java).putExtra("EMAIL", email))
                    } else {
                        Toast.makeText(this@CompleteInfoActivity, response.body()?.message ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CompleteInfoActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<android.view.View>(R.id.tvSkip).setOnClickListener { 
             lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.register(com.skyline.app.network.RegisterRequest(
                        email = email,
                        password = password
                    ))
                    if (response.isSuccessful && response.body()?.success == true) {
                         startActivity(Intent(this@CompleteInfoActivity, AccountSuccessActivity::class.java))
                    } else {
                        Toast.makeText(this@CompleteInfoActivity, response.body()?.message ?: "Đăng ký thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@CompleteInfoActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

class PhoneOtpActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        setupHomeButton()
        
        val email = intent.getStringExtra("EMAIL") ?: ""
        
        findViewById<android.view.View>(R.id.btnVerify).setOnClickListener {
            val otp = "${findViewById<EditText>(R.id.edtOtp1).text}${findViewById<EditText>(R.id.edtOtp2).text}${findViewById<EditText>(R.id.edtOtp3).text}${findViewById<EditText>(R.id.edtOtp4).text}${findViewById<EditText>(R.id.edtOtp5).text}${findViewById<EditText>(R.id.edtOtp6).text}"
            
            if (otp.length < 6) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ mã OTP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val response = RetrofitClient.instance.verifyOtp(com.skyline.app.network.VerifyOtpRequest(email, otp))
                    if (response.isSuccessful && response.body()?.success == true) {
                        startActivity(Intent(this@PhoneOtpActivity, AccountSuccessActivity::class.java))
                    } else {
                        Toast.makeText(this@PhoneOtpActivity, response.body()?.message ?: "Xác thực thất bại", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@PhoneOtpActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        findViewById<android.view.View>(R.id.tvBack).setOnClickListener { finish() }
    }
}

class ForgotPasswordActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        setupHomeButton()
        
        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        
        findViewById<android.view.View>(R.id.btnOtp).setOnClickListener {
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
        
        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.btnVerify).setOnClickListener {
            val otp = "${findViewById<EditText>(R.id.edtOtp1).text}${findViewById<EditText>(R.id.edtOtp2).text}${findViewById<EditText>(R.id.edtOtp3).text}${findViewById<EditText>(R.id.edtOtp4).text}${findViewById<EditText>(R.id.edtOtp5).text}${findViewById<EditText>(R.id.edtOtp6).text}"
            
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
        
        findViewById<android.view.View>(R.id.btnDone).setOnClickListener {
            val newPass = edtNewPass.text.toString()
            val confirm = edtConfirm.text.toString()
            
            if (newPass.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show()
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
        findViewById<android.view.View>(R.id.tvBack).setOnClickListener { finish() }
    }
}

class ResetSuccessActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_success)
        setupHomeButton()
        findViewById<android.view.View>(R.id.btnPrimary).setOnClickListener { startActivity(Intent(this, LoginActivity::class.java)) }
        findViewById<android.view.View>(R.id.tvHome).setOnClickListener { goHome() }
    }
}

class AccountSuccessActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_success)
        setupHomeButton()
        findViewById<android.view.View>(R.id.btnExplore).setOnClickListener { goHome() }
    }
}
