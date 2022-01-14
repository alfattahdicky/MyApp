package com.example.myapp

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.myapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    // View Binding
    private lateinit var binding: ActivitySignUpBinding
    //Action Bar
    private lateinit var actionBar: ActionBar
    // progressDialog
    private lateinit var progressDialog: ProgressDialog
    // Firebase auth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private var email = ""
    private var password = ""
    private var username = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure Action Bar
        actionBar = supportActionBar!!
        actionBar.title = "Sign Up"
        // Enable back button
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowHomeEnabled(true)

        // config progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Creating Account In..")
        progressDialog.setCanceledOnTouchOutside(false)

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        // handle click, begin sign up
        binding.btnSignUp.setOnClickListener {
            // validate data
            validateData()
        }
    }

    private fun validateData() {
        // get data
        username = binding.usernameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        // validate data
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEt.error = "Invalid email format"
        } else if (TextUtils.isEmpty(password)) {
            binding.passwordEt.error = "Please enter password"
        } else if (password.length < 6 ) {
            binding.passwordEt.error = "Password must at least 6 character"
        } else  {
            // continue sign up
            firebaseSignUp()
        }
    }

    private fun firebaseSignUp() {
        // Show progress
        progressDialog.show()

        // create account
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                // sign up success
                progressDialog.dismiss()

                val firebaseUser = firebaseAuth.currentUser
                val email = firebaseUser!!.email
                Toast.makeText(this, "Login with $email", Toast.LENGTH_SHORT).show()

                binding.emailEt.setText("")
                binding.passwordEt.setText("")
                binding.usernameEt.setText("")
                storeDatabase()
            }
            .addOnFailureListener { e->
                // sign up failed
                progressDialog.dismiss()
                Toast.makeText(this, "Sign Up failed due to ${e.message} ", Toast.LENGTH_SHORT).show()
            }
    }

    private fun storeDatabase() {
        val uid = firebaseAuth.currentUser?.uid
        val user = User(username, email)
        if(uid != null) {
            databaseReference.child(uid).setValue(user).addOnCompleteListener {
                if(it.isSuccessful) {
                    Toast.makeText(this, "Registrasi Berhasil", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed registrasi", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed() // previous activity
        return super.onSupportNavigateUp()
    }
}