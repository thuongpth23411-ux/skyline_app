package com.skyline.app

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.skyline.app.databinding.ActivityConfirmPaymentBinding
import com.skyline.app.databinding.DialogPriceDetailBinding
import java.util.Calendar
import java.util.Locale

class ConfirmPaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfirmPaymentBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupHeader()
        setupPaymentMethods()
        setupCardTypeSpinner()
        setupExpiryDatePicker()
        setupTermsAndPay()
    }

    private fun setupHeader() {
        binding.btnBack.setOnClickListener { finish() }
        
        // Gạch chân Chi tiết giá
        binding.tvPriceDetail.text = Html.fromHtml("<u>${getString(R.string.price_detail)}</u>", Html.FROM_HTML_MODE_COMPACT)
        
        binding.tvPriceDetail.setOnClickListener {
            showPriceDetailDialog()
        }
    }

    private fun showPriceDetailDialog() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val dialogBinding = DialogPriceDetailBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.btnBack.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnClose.setOnClickListener { dialog.dismiss() }
        dialogBinding.btnConfirm.setOnClickListener { dialog.dismiss() }

        dialog.show()
    }

    private fun setupPaymentMethods() {
        binding.cardPaymentMethod.setOnClickListener { selectPaymentMethod(0) }
        binding.vnpayMethod.setOnClickListener { selectPaymentMethod(1) }
        binding.vietqrMethod.setOnClickListener { selectPaymentMethod(2) }
        binding.momoMethod.setOnClickListener { selectPaymentMethod(3) }

        binding.rbPaymentCard.setOnClickListener { selectPaymentMethod(0) }
        binding.rbVNPay.setOnClickListener { selectPaymentMethod(1) }
        binding.rbVietQR.setOnClickListener { selectPaymentMethod(2) }
        binding.rbMomo.setOnClickListener { selectPaymentMethod(3) }

        // Mặc định không chọn phương thức nào
        resetPaymentSelection()
    }

    private fun resetPaymentSelection() {
        binding.rbPaymentCard.isChecked = false
        binding.rbVNPay.isChecked = false
        binding.rbVietQR.isChecked = false
        binding.rbMomo.isChecked = false

        binding.cardDetails.visibility = View.GONE
        binding.tvVNPayRedirect.visibility = View.GONE
        binding.tvVietQRRedirect.visibility = View.GONE
        binding.tvMomoRedirect.visibility = View.GONE

        val inactiveColor = Color.parseColor("#D1D5DB")
        binding.cardPaymentMethod.strokeColor = inactiveColor
        binding.vnpayMethod.strokeColor = inactiveColor
        binding.vietqrMethod.strokeColor = inactiveColor
        binding.momoMethod.strokeColor = inactiveColor
    }

    private fun selectPaymentMethod(index: Int) {
        binding.rbPaymentCard.isChecked = index == 0
        binding.rbVNPay.isChecked = index == 1
        binding.rbVietQR.isChecked = index == 2
        binding.rbMomo.isChecked = index == 3

        binding.cardDetails.visibility = if (index == 0) View.VISIBLE else View.GONE
        binding.tvVNPayRedirect.visibility = if (index == 1) View.VISIBLE else View.GONE
        binding.tvVietQRRedirect.visibility = if (index == 2) View.VISIBLE else View.GONE
        binding.tvMomoRedirect.visibility = if (index == 3) View.VISIBLE else View.GONE

        // Đổi màu viền cho phương thức được chọn
        val activeColor = Color.BLACK
        val inactiveColor = Color.parseColor("#D1D5DB")

        binding.cardPaymentMethod.strokeColor = if (index == 0) activeColor else inactiveColor
        binding.vnpayMethod.strokeColor = if (index == 1) activeColor else inactiveColor
        binding.vietqrMethod.strokeColor = if (index == 2) activeColor else inactiveColor
        binding.momoMethod.strokeColor = if (index == 3) activeColor else inactiveColor
    }

    private fun setupCardTypeSpinner() {
        val cardTypes = arrayOf(
            getString(R.string.visa),
            getString(R.string.mastercard),
            getString(R.string.jcb),
            getString(R.string.amex)
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, cardTypes)
        binding.spinnerCardType.setAdapter(adapter)
    }

    private fun setupExpiryDatePicker() {
        binding.etExpiry.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, _ ->
                // Định dạng MM/YY
                val formattedMonth = String.format(Locale.getDefault(), "%02d", selectedMonth + 1)
                val formattedYear = selectedYear.toString().substring(2)
                binding.etExpiry.setText("$formattedMonth/$formattedYear")
            }, year, month, day)

            // Chỉ chọn được các ngày tương lai
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun setupTermsAndPay() {
        // Hiển thị text với link màu xanh và gạch chân, phần còn lại màu đen
        val termsText = "<font color='#000000'>Tôi hiểu và đồng ý với </font>" +
                "<u><font color='#0B4DA2'>Điều lệ vận chuyển</font></u><font color='#000000'>, </font>" +
                "<u><font color='#0B4DA2'>Điều kiện điều khoản</font></u><font color='#000000'>, </font>" +
                "<u><font color='#0B4DA2'>Chính sách bảo mật</font></u><font color='#000000'> và </font>" +
                "<u><font color='#0B4DA2'>Điều kiện giá vé</font></u><font color='#000000'> của Skyline.</font>"

        binding.tvTerms.text = Html.fromHtml(termsText, Html.FROM_HTML_MODE_COMPACT)

        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            binding.btnPay.isEnabled = isChecked
            binding.btnPay.alpha = if (isChecked) 1.0f else 0.5f
        }

        binding.btnPay.setOnClickListener {
            Toast.makeText(this, getString(R.string.processing_payment), Toast.LENGTH_LONG).show()
        }
    }
}
