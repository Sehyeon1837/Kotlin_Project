package com.example.kotiln_project

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startMap = findViewById<Button>(R.id.btn_startmap)

        startMap.setOnClickListener {
            //setContentView(R.layout.activity_maps)
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }
}