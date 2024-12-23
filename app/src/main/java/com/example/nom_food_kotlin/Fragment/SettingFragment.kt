package com.example.nom_food_kotlin.Fragment

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.res.ResourcesCompat
import com.example.nom_food_kotlin.Activity.SignInActivity
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.FragmentSettingBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class SettingFragment : Fragment() {
    private lateinit var binding: FragmentSettingBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var sharedPreferences: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        firebaseAuth = FirebaseAuth.getInstance()
        sharedPreferences = requireContext().getSharedPreferences("loginSave", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getString("userId", "")
        Log.d("checkUserId", userId.toString())

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        binding.btnLogout.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
            val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
            val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
            val alertDialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }
            btnConfirm.setOnClickListener {
                signOutAndStartSignInActivity()
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

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
                Log.d("ProfileFragment", "Sign out failed: ${task.exception?.message}")
            }
        }
    }
}