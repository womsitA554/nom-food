package com.example.nom_food_kotlin.Activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivitySignUpBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthProvider

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var verificationCode: String
    private lateinit var firebaseAuth: FirebaseAuth
    private var phoneNumber: String = ""
    var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSignIn.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnNext.setOnClickListener {
            val internationalCode = binding.etInternational.text.toString().trim()
            val localPhoneNumber = binding.etPhoneNumber.text.toString().trim()
            phoneNumber = localPhoneNumber + internationalCode

            if (internationalCode.isNotEmpty() && localPhoneNumber.isNotEmpty()) {
                val intent = Intent(this, OTPActivity::class.java).apply {
                    putExtra("phoneNumber", phoneNumber)
                }
                startActivity(intent)
                finish()
                binding.etInternational.text.clear()
                binding.etPhoneNumber.text.clear()
                binding.etInternational.requestFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.etInternational, InputMethodManager.SHOW_IMPLICIT)

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            } else if (internationalCode.isEmpty()) {
                // Handle empty phone number case
                // For example, show a Toast or set an error message on the EditText
                binding.etInternational.error = "International number cannot be empty"
                return@setOnClickListener
            } else if (localPhoneNumber.isEmpty()){
                binding.etPhoneNumber.error = "Phone number cannot be empty"
                return@setOnClickListener
            }
        }

//        binding.btnGoogle.setOnClickListener {
//            googleSignIn()
//        }
    }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, SignUpActivity.RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SignUpActivity.RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    Toast.makeText(this, "Signed in as ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}