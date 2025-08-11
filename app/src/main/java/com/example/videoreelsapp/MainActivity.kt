package com.example.videoreelsapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.videoreelsapp.ui.VideoCallFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, VideoCallFragment())
            .commit()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_call -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, VideoCallFragment())
                        .commit()
                    true
                }
                R.id.nav_reels -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, ReelsFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}


