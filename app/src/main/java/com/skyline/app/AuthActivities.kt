package com.skyline.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

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
        findViewById<android.view.View>(R.id.tvForgot).setOnClickListener { startActivity(Intent(this, ForgotPasswordActivity::class.java)) }
        findViewById<android.view.View>(R.id.tvRegister).setOnClickListener { startActivity(Intent(this, RegisterEmailActivity::class.java)) }
        findViewById<android.view.View>(R.id.btnLogin).setOnClickListener { goHome() }
    }
}

class RegisterEmailActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_email)
        setupHomeButton()
        findViewById<android.view.View>(R.id.btnContinue).setOnClickListener { startActivity(Intent(this, SetPasswordActivity::class.java)) }
    }
}

class SetPasswordActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_password)
        setupHomeButton()
        findViewById<android.view.View>(R.id.btnContinue).setOnClickListener { startActivity(Intent(this, CompleteInfoActivity::class.java)) }
    }
}

class CompleteInfoActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complete_info)
        setupHomeButton()
        findViewById<android.view.View>(R.id.btnContinue).setOnClickListener { startActivity(Intent(this, PhoneOtpActivity::class.java)) }
        findViewById<android.view.View>(R.id.tvSkip).setOnClickListener { startActivity(Intent(this, AccountSuccessActivity::class.java)) }
    }
}

class PhoneOtpActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        setupHomeButton()
        findViewById<android.view.View>(R.id.btnVerify).setOnClickListener { startActivity(Intent(this, AccountSuccessActivity::class.java)) }
        findViewById<android.view.View>(R.id.tvBack).setOnClickListener { finish() }
    }
}

class ForgotPasswordActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        setupHomeButton()
        findViewById<android.view.View>(R.id.btnOtp).setOnClickListener { startActivity(Intent(this, ForgotOtpActivity::class.java)) }
        findViewById<android.view.View>(R.id.tvBackLogin).setOnClickListener { finish() }
    }
}

class ForgotOtpActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        setupHomeButton()
        findViewById<TextView>(R.id.tvBack).setOnClickListener { finish() }
        findViewById<android.view.View>(R.id.btnVerify).setOnClickListener { startActivity(Intent(this, ResetPasswordActivity::class.java)) }
    }
}

class ResetPasswordActivity : BaseAuthActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)
        setupHomeButton()
        findViewById<android.view.View>(R.id.btnDone).setOnClickListener { startActivity(Intent(this, ResetSuccessActivity::class.java)) }
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
