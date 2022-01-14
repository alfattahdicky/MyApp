package com.example.myapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import com.example.myapp.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    // View binding
    private lateinit var binding: ActivityProfileBinding

    // Action bar
    private lateinit var actionBar: ActionBar

    // Progress dialog
    private lateinit var progressDialog: ProgressDialog

    // firebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var  databaseReference: DatabaseReference
    private lateinit var storageReference: DatabaseReference

    // Picture
    private var pickImage = 100
    private lateinit var imageUri: Uri


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding  = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //config action bar
        actionBar = supportActionBar!!
        actionBar.title = "Profile"

        // Progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading ....")
        progressDialog.setCanceledOnTouchOutside(false)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        checkAndUpdateUser()

        // handle click, update username
        val idUsername = binding.resultUsernameEt
        val inputUsername = binding.editUsernameEt
        val btnChangeEdit = binding.btnChangeEdit
        val btnChangeUsername = binding.btnChangeUsername
        btnChangeEdit.setOnClickListener {
            idUsername.visibility = View.GONE
            inputUsername.setText(idUsername.text)
            inputUsername.visibility = View.VISIBLE

            btnChangeEdit.visibility = View.GONE
            btnChangeUsername.visibility = View.VISIBLE
        }

        btnChangeUsername.setOnClickListener {
            val updateUsername =  inputUsername.text.toString()

            updateData(updateUsername)
        }

        binding.changeImageTv.setOnClickListener {
            selectAndUploadImage()
        }


        // handle click , logout
        binding.btnLogout.setOnClickListener {
            firebaseAuth.signOut()
            checkAndUpdateUser()
        }

    }

    private fun selectAndUploadImage() {
        // Select Image
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(intent, pickImage)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == pickImage && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            uploadImage(imageUri)
            updateImage(imageUri)
        }
    }

    private fun updateImage(imageUri: Uri) {
        val imageFirebaseUser = "img/${FirebaseAuth.getInstance().currentUser?.uid}"

        val storageReference = FirebaseStorage.getInstance().reference.child(imageFirebaseUser)

        storageReference.downloadUrl
            .addOnSuccessListener {
                binding.imageTv.setImageURI(this.imageUri)
                Toast.makeText(this, "Change Download Profile Success", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                progressDialog.dismiss();
                Toast.makeText(this, "Change Download Profile Cancel to $it", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImage(imageUri: Uri) {
        progressDialog.show();
        val imageFirebaseUser = "img/${FirebaseAuth.getInstance().currentUser?.uid}"

        val storageReference = FirebaseStorage.getInstance().reference.child(imageFirebaseUser)

            storageReference.putFile(this.imageUri)
            .addOnSuccessListener {
                progressDialog.dismiss();
                Toast.makeText(this, "Change Update to db", Toast.LENGTH_SHORT).show()

            } .addOnFailureListener {
                progressDialog.dismiss();
                Toast.makeText(this, "Change Update Profile Cancel to $it", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateData(updateUsername: String) {
        val firebaseUser = firebaseAuth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        val user = mapOf<String, String>(
            "username" to updateUsername
        )
        if (firebaseUser != null) {
            databaseReference.child(firebaseUser).updateChildren(user)
                .addOnSuccessListener {
                    binding.resultUsernameEt.text = updateUsername
                    binding.resultUsernameEt.visibility = View.VISIBLE
                    binding.editUsernameEt.visibility = View.GONE
                    binding.btnChangeUsername.visibility = View.GONE
                    binding.btnChangeEdit.visibility = View.VISIBLE

                    Toast.makeText(this, "Username complete change", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed is $it", Toast.LENGTH_SHORT).show()
                }
        }

    }

    private fun checkAndUpdateUser() {
        // check user logged
        val firebaseUser = firebaseAuth.currentUser?.uid
        databaseReference = FirebaseDatabase.getInstance().getReference("Users")

        if(firebaseUser != null) {
            databaseReference.child(firebaseUser).get()
                .addOnSuccessListener {
                    if(it.exists()) {
                        val username = it.child("username").value
                        val email = it.child("email").value

                        binding.resultUsernameEt.text = ""
                        binding.resultEmailEt.text = ""
                        binding.resultUsernameEt.text = username.toString()
                        binding.resultEmailEt.text = email.toString()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed is $it", Toast.LENGTH_SHORT).show()
                }
        }else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}