package com.redx.idcard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.redx.idcard.databinding.ActivityPakistanCardBinding

class PakistanCardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPakistanCardBinding
    private var selectedPhotoBitmap: Bitmap? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bmp = MediaStore.Images.Media.getBitmap(contentResolver, it)
            selectedPhotoBitmap = bmp
            binding.ivSelectedPhoto.setImageBitmap(bmp)
            binding.tvPhotoHint.text = "Photo selected ✓"
        }
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) pickImageLauncher.launch("image/*")
        else Toast.makeText(this, "Permission required to select photo", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPakistanCardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Pakistan CNIC Generator"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnSelectPhoto.setOnClickListener {
            requestPhotoPermission()
        }

        binding.btnGenerate.setOnClickListener {
            generateCard()
        }
    }

    private fun requestPhotoPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            pickImageLauncher.launch("image/*")
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun generateCard() {
        val name = binding.etName.text.toString().trim()
        val dob = binding.etDob.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Please enter your name"
            return
        }
        if (dob.isEmpty()) {
            binding.etDob.error = "Please enter date of birth (DD.MM.YYYY)"
            return
        }
        if (selectedPhotoBitmap == null) {
            Toast.makeText(this, "Please select a photo", Toast.LENGTH_SHORT).show()
            return
        }

        val result = CardGeneratorUtils.generatePakistanCard(
            context = this,
            name = name,
            dob = dob,
            photo = selectedPhotoBitmap!!
        )

        val intent = Intent(this, PreviewActivity::class.java)
        PreviewActivity.resultBitmap = result
        PreviewActivity.cardType = "Pakistan CNIC"
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
