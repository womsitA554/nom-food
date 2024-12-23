package com.example.nom_food_kotlin.Activity.Admin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.nom_food_kotlin.Helper.FirebaseHelper
import com.example.nom_food_kotlin.Model.Category
import com.example.nom_food_kotlin.Model.Menu
import com.example.nom_food_kotlin.R
import com.example.nom_food_kotlin.databinding.ActivityAddOrEditFoodBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class AddOrEditFoodActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddOrEditFoodBinding
    private lateinit var firebaseHelper: FirebaseHelper
    private val list: MutableList<Category> = mutableListOf()

    private lateinit var storageReference: StorageReference
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddOrEditFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseHelper = FirebaseHelper()
        storageReference = FirebaseStorage.getInstance().reference

        binding.btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1000)
        }

        val itemId = intent.getStringExtra("itemId")
        val title = intent.getStringExtra("title")
        val picUrl = intent.getStringExtra("picUrl")
        val price = intent.getFloatExtra("price", 0.0f)
        Log.d("checkPrice", price.toString())
        val description = intent.getStringExtra("description")

        binding.etTitle.setText(title)
        if (price != 0.0f) {
            binding.etPrice.setText(price.toString())
        } else {
            binding.etPrice.setText("")
        }
        binding.etDescription.setText(description)

        val options = RequestOptions().transform(CenterCrop())
        Glide.with(this).load(picUrl).apply(options).into(binding.picUrl)

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivityAdmin::class.java)
            intent.putExtra("navigateToo", "SecondFragment")
            finish()
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }

        binding.btnSave.setOnClickListener {
//            if (imageUri != null) {
//                val fileRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")
//                fileRef.putFile(imageUri!!)
//                    .addOnSuccessListener {
//                        fileRef.downloadUrl.addOnSuccessListener { downloadUrl ->
//                            saveMenu(downloadUrl.toString())
//                            MotionToast.createColorToast(
//                                this,
//                                null,
//                                "New product added successfully",
//                                MotionToastStyle.SUCCESS,
//                                Gravity.TOP,
//                                MotionToast.LONG_DURATION,
//                                ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
//                            )
//                        }
//                    }
//                    .addOnFailureListener { exception ->
//                        Log.e("ImageUpload", "Failed to upload image", exception)
//                        MotionToast.createColorToast(
//                            this,
//                            null,
//                            "New product added failed",
//                            MotionToastStyle.ERROR,
//                            Gravity.TOP,
//                            MotionToast.LONG_DURATION,
//                            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
//                        )
//                    }
//            } else {
            val existingPicUrl = intent.getStringExtra("picUrl")
            saveMenu(existingPicUrl)
//            }
        }

        firebaseHelper.getAllCategory { listCategory ->
            if (listCategory.isNotEmpty()) {
                val categoryTitles = listCategory.map { it.title }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_item,
                    categoryTitles
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spCategory.adapter = adapter
            } else {
                Toast.makeText(this, "No categories found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imageUri?.let {
                Glide.with(this)
                    .load(it)
                    .into(binding.picUrl)
            }
        }
    }

    fun generateItemId(): String {
        val uniqueNumber = System.currentTimeMillis().toString().takeLast(9)
        return "SP$uniqueNumber"
    }

    private fun saveMenu(picUrl: String?) {
        val itemId = intent.getStringExtra("itemId") ?: generateItemId()
        val title = binding.etTitle.text.toString()
        val price = binding.etPrice.text.toString().toFloat()
        val description = binding.etDescription.text.toString()
        val category = binding.spCategory.selectedItem.toString()
        val menu = Menu(
            itemId = itemId,
            title = title,
            price = price,
            description = description,
            picUrl = "https://firebasestorage.googleapis.com/v0/b/orderfood-69ed5.appspot.com/o/Nobita.png?alt=media&token=3227b893-1367-440b-aeab-2333e82da55b",
            category = category,
            rating = 0.0f
        )

        if (intent.getStringExtra("itemId") != null) {
            firebaseHelper.updateMenu(category, menu)
        } else {
            firebaseHelper.addMenu(category, menu)
        }

        val intent = Intent(this, MainActivityAdmin::class.java)
        intent.putExtra("navigateToo", "SecondFragment")
        startActivity(intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        MotionToast.createColorToast(
            this,
            null,
            "New product added successfully",
            MotionToastStyle.SUCCESS,
            Gravity.TOP,
            MotionToast.LONG_DURATION,
            ResourcesCompat.getFont(this, www.sanju.motiontoast.R.font.helvetica_regular)
        )

        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}