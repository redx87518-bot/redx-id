package com.redx.idcard

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.redx.idcard.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPakistan.setOnClickListener {
            startActivity(Intent(this, PakistanCardActivity::class.java))
        }

        binding.btnNigeria.setOnClickListener {
            startActivity(Intent(this, NigeriaCardActivity::class.java))
        }
    }
}
