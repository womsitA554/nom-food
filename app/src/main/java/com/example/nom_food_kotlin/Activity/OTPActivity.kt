package com.example.nom_food_kotlin.Activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityOtpactivityBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpactivityBinding
    private var phoneNumber: String = ""
    private lateinit var verificationCode: String
    private lateinit var firebaseAuth: FirebaseAuth
    var resendingToken: ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        phoneNumber = intent.getStringExtra("phoneNumber").toString()
        if (phoneNumber.isNotEmpty()) {
            binding.tvPhoneNumber.text = phoneNumber
            Log.d("OTPActivity", "Phone number: $phoneNumber")
        } else {
            Log.d("OTPActivity", "Phone number is null or empty")
            Toast.makeText(this, "Phone number is not provided", Toast.LENGTH_SHORT).show()
            return
        }

        sendOtp(phoneNumber, false)

        binding.btnNext.setOnClickListener {
            val otp: String = binding.etCode.text.toString()
            if (otp.isEmpty()) {
                Toast.makeText(this@OTPActivity, "OTP cannot be empty", Toast.LENGTH_SHORT).show()
            } else {
                showProgressBar(true)
                verifyOtp(otp)
            }
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private fun verifyOtp(otp: String) {
        val phoneAuthCredential = PhoneAuthProvider.getCredential(verificationCode, otp)
        signUpPassword(phoneAuthCredential)
    }

    private fun sendOtp(phoneNumber: String, isResend: Boolean) {
        val builder = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    Log.d("OTPActivity", "Verification completed")
                    signUpPassword(phoneAuthCredential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Log.e("OTPActivity", "Verification failed: ${e.message}")
                    Toast.makeText(
                        this@OTPActivity,
                        "OTP send failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    showProgressBar(false)
                }

                override fun onCodeSent(
                    s: String,
                    forceResendingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationCode = s
                    resendingToken = forceResendingToken
                    Log.d("OTPActivity", "OTP code sent successfully")
                    Toast.makeText(this@OTPActivity, "OTP sent successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        if (isResend) {
            resendingToken?.let {
                builder.setForceResendingToken(it)
            }
        }

        PhoneAuthProvider.verifyPhoneNumber(builder.build())
    }

    private fun signUpPassword(phoneAuthCredential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
            showProgressBar(false)
            if (task.isSuccessful) {
                val intent = Intent(this, SetUpNameActivity::class.java)
                intent.putExtra("phoneNumber", phoneNumber)
                startActivity(intent)
                finish()
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                Toast.makeText(this, "OTP verification successful", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("OTPActivity", "OTP verification failed: ${task.exception?.message}")
                Toast.makeText(this, "OTP verification failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProgressBar(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnNext.isEnabled = !show
    }
}
