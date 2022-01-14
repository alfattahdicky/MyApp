package com.example.myapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.myapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var actionBar: ActionBar

    // Progress Dialog
    private lateinit var progressDialog: ProgressDialog

    // FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    private var email = ""
    private var password = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Config Action Bar
        actionBar = supportActionBar!!
        actionBar.title = "Login"

        // Configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Logging In")
        progressDialog.setCanceledOnTouchOutside(false)

        // Init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        //  open register activity
        binding.noAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        //begin login
        binding.btnLogin.setOnClickListener {
            // Check validate data
            validateData()
        }


    }

    private fun validateData() {
        // get data
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // invalid email
            binding.emailEt.error = "Invalid Email"

        } else if(TextUtils.isEmpty(password)) {
            // password empty
            binding.passwordEt.error = "Please enter password"
        } else {
             // data valid, begin login
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        // show progress
        progressDialog.show()
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // login success
                progressDialog.dismiss()
                // get user info
                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(this, "Logged as $email", Toast.LENGTH_SHORT).show()

                // Open Profile
                startActivity(Intent(this, ProfileActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                // login failed
                progressDialog.dismiss()
                Toast.makeText(this, "Login failed due to ${e.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser != null) {
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }
    }
}