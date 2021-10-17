package com.example.myreader.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.myreader.databinding.ActivityStartPageBinding
import kotlin.concurrent.thread

class StartPageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartPageBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityStartPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        thread {
            Thread.sleep(1000)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }
}