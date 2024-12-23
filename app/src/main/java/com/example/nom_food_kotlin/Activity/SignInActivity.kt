package com.example.nom_food_kotlin.Activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nom_food_kotlin.Activity.Admin.MainActivityAdmin
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.User
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivitySignInBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class SignInActivity : AppCompatActivity() {
    companion object {
        private const val RC_SIGN_IN = 9001
    }
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var firebaseAuth: FirebaseAuth
    private var sharedPreferences: SharedPreferences? = null

    private var listUserId: List<String> = listOf()

    private lateinit var binding: ActivitySignInBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivitySignInBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseHelper = FirebaseHelper()

        firebaseHelper.getAllUserId { userIds ->
            listUserId = userIds
        }

        val currentUser = firebaseAuth.currentUser

        sharedPreferences = getSharedPreferences("loginSave", MODE_PRIVATE)

        if (currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnLogin.setOnClickListener {
            val phoneNumber = binding.etPhoneNumber.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (phoneNumber.isNotEmpty() && password.isNotEmpty()) {
                loginUser(phoneNumber, password)
            } else {
                Toast.makeText(
                    this, "Please enter both phone number and password", Toast.LENGTH_SHORT
                ).show()
            }
        }

//        binding.btnGoogle.setOnClickListener {
//            googleSignIn()
//        }
    }

    private fun loginUser(phoneNumber: String, password: String) {
        firebaseHelper.dbUsers.child("Users").orderByChild("phoneNumber").equalTo(phoneNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (userSnapshot in snapshot.children) {
                            val user = userSnapshot.getValue(User::class.java)
                            if (user != null && user.password == password) {
                                saveUserId(user.userId, user.role)
                                val intent = if (user.role == "customer") {
                                    Intent(this@SignInActivity, MainActivity::class.java)
                                } else {
                                    Intent(this@SignInActivity, MainActivityAdmin::class.java)
                                }.apply {
                                    putExtra("userId", user.userId)
                                    putExtra("phoneNumber", phoneNumber)
                                    putExtra("email", user.email)
                                }
                                startActivity(intent)
                                finish()
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                                return
                            }
                        }
                        Toast.makeText(this@SignInActivity, "Invalid password", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@SignInActivity, "No account found with this phone number", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@SignInActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun saveUserId(userId: String?, role: String?) {
        val editor: SharedPreferences.Editor = sharedPreferences?.edit()!!
        editor.putString("userId", userId)
        editor.putBoolean("isLogin", true)
        editor.putString("role", role)
        editor.apply()
    }


    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val currentUser = firebaseAuth.currentUser
                    currentUser?.let {
                        val name = it.displayName
                        val email = it.email.toString()
                        val phoneNumber = it.phoneNumber.toString()
                        val userId = firebaseHelper.currentUserId()
                        val password = firebaseHelper.randomPassword()
                        val user = User(userId, name, phoneNumber = "", email, password)
                        firebaseHelper.getAllUserId { userIds ->
                            if (userId != null && !userIds.contains(userId)) {
                                firebaseHelper.addUser(user)
                            }
                            val intent = Intent(this, MainActivity::class.java).apply {
                                putExtra("userId", userId)
                                putExtra("email", email)
                                putExtra("phoneNumber", phoneNumber)
                            }
                            startActivity(intent)
                            finish()

                            Toast.makeText(this, "Login with email: $email", Toast.LENGTH_SHORT).show()
                            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                        }
                    }
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showCoordinates(view: View) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val x = location[0]
        val y = location[1]

        Toast.makeText(this, "Tọa độ: X = $x, Y = $y", Toast.LENGTH_LONG).show()
    }
}