package com.example.quiz

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.Toast
import com.example.quiz.databinding.ActivitySignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.btnSignup.setOnClickListener{
            val email = binding.edtSignupEmail.text.toString().trim()
            val password = binding.edtSignupPassword.text.toString().trim()
            val confirmPassword = binding.edtPasswordConfirm.text.toString().trim()
            val selectedRoleId = binding.roleRadioGroup.checkedRadioButtonId

            if (selectedRoleId == -1 || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields and select a role.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val role = if (selectedRoleId == R.id.btnStudentRadio) "student" else "admin"
            Log.d("SignupActivity", "Selected role: $role")
            registerUser(email, password, role)
        }

        binding.loginRedirectText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun registerUser(email: String, password: String, role: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    saveUserData(role)
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserData(role: String) {
        val user = firebaseAuth.currentUser
        val db = FirebaseDatabase.getInstance().reference  // Get reference to the root of your Realtime Database

        user?.uid?.let { uid ->
            val userData = mapOf("uid" to uid, "role" to role)
            db.child("users").child(uid).setValue(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                    redirectUserBasedOnRole(role, true)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to register user details: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun redirectUserBasedOnRole(role: String, isSignup: Boolean) {
        if (isSignup) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            val intent = when (role) {
                "student" -> Intent(this, MainActivity::class.java)
                "admin" -> Intent(this, AdminPanelActivity::class.java)
                else -> return
            }
            startActivity(intent)
        }
        finish()
    }
}
