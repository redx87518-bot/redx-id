package com.redx.idcard

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.redx.idcard.databinding.ActivityPreviewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreviewBinding

    companion object {
        var resultBitmap: Bitmap? = null
        var cardType: String = "ID Card"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Preview — $cardType"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val bmp = resultBitmap
        if (bmp != null) {
            binding.ivPreview.setImageBitmap(bmp)
        } else {
            Toast.makeText(this, "Error: no image generated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.btnSave.setOnClickListener { saveToGallery() }
        binding.btnShare.setOnClickListener { shareImage() }
    }

    private fun saveToGallery() {
        val bmp = resultBitmap ?: return
        val filename = "REDX_${cardType.replace(" ", "_")}_${System.currentTimeMillis()}.jpg"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ — use MediaStore, no storage permission needed
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/RedxIDCard")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = contentResolver
                val uri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { stream: OutputStream ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    Toast.makeText(
                        this,
                        "✅ Saved to Gallery → Pictures/RedxIDCard",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(this, "❌ Could not create file", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Android 9 and below — write to file then scan
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "RedxIDCard"
                )
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { stream ->
                    bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                }
                // Trigger gallery scan so the image appears immediately
                MediaScannerConnection.scanFile(
                    this,
                    arrayOf(file.absolutePath),
                    arrayOf("image/jpeg")
                ) { _, _ -> }
                Toast.makeText(
                    this,
                    "✅ Saved to Gallery → Pictures/RedxIDCard",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Save failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareImage() {
        val bmp = resultBitmap ?: return
        try {
            val file = File(cacheDir, "redx_id_share.jpg")
            FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            val uri = androidx.core.content.FileProvider.getUriForFile(
                this, "${packageName}.fileprovider", file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "Share $cardType"))
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        resultBitmap = null
    }
}
