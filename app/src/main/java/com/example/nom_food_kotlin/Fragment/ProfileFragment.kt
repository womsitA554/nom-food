package com.example.nom_food_kotlin.Fragment

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.nom_food_kotlin.Activity.SignInActivity
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseHelper: FirebaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        var selected = 0
        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(BillFragment())
            binding.btnBill.setImageResource(R.drawable.bill_black)
            binding.btnBill.setBackgroundResource(R.drawable.btninprofile_bg)
            selected = 1
        }

        binding.btnBill.setOnClickListener {
            if (selected != 1) {
                replaceFragment(BillFragment())
                updateIconSelected(1)
                updateBackgroundSelected(1)
                selected = 1
            }
        }

        binding.btnSetting.setOnClickListener {
            if (selected != 2) {
                replaceFragment(SettingFragment())
                updateIconSelected(2)
                updateBackgroundSelected(2)
                selected = 2
            }
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseHelper = FirebaseHelper()

        sharedPreferences = requireContext().getSharedPreferences("loginSave", MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "")
        Log.d("checkUserId", userId.toString())

        if (userId != null) {
            firebaseHelper.getPhoneNumberAndEmail(userId).addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val dataSnapshot = task.result
                    if (dataSnapshot !=null && dataSnapshot.exists()){
                        val email = dataSnapshot.child("email").getValue(String::class.java)
                        val phoneNumber = dataSnapshot.child("phoneNumber").getValue(String::class.java)
                        val name = dataSnapshot.child("name").getValue(String::class.java)

                        binding.tvName.text = name
                        binding.tvPhoneNumber.text = phoneNumber
                    }else {
                        Log.d("UserInfo", "No data available")
                    }
                }else {
                    Log.e("UserInfo", "Error getting data", task.exception)
                }
            }
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

//        binding.btnLogOut.setOnClickListener {
//            signOutAndStartSignInActivity()
//        }


        return binding.root
    }

    private fun signOutAndStartSignInActivity() {
        firebaseAuth.signOut()
        googleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val editor = sharedPreferences.edit()
                editor.clear()
                editor.apply()

                val intent = Intent(requireContext(), SignInActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
                requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
            } else {
                // Handle failure (optional)
                Log.d("ProfileFragment", "Sign out failed: ${task.exception?.message}")
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container_profile, fragment)
        fragmentTransaction.commit()
    }

    fun updateIconSelected(selected: Int) {
        binding.btnBill.setImageResource(if (selected == 1) R.drawable.bill_black else R.drawable.bill_grey)
        binding.btnSetting.setImageResource(if (selected == 2) R.drawable.setting_black else R.drawable.setting_grey)
    }
    fun updateBackgroundSelected(selected: Int) {
        binding.btnBill.setBackgroundResource(if (selected == 1) R.drawable.btninprofile_bg else R.drawable.linearprofile_bg)
        binding.btnSetting.setBackgroundResource(if (selected == 2) R.drawable.btninprofile_bg else R.drawable.linearprofile_bg)
    }
}
