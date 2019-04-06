package com.lis.lis.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.lis.lis.R

class HomeActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    fun openCards(@Suppress("unused_parameter") v: View) {
        val intent = Intent(this, CardsActivity::class.java)
        startActivity(intent)
    }

    fun openSleep(@Suppress("unused_parameter") v: View) {

    }

    fun openStats(@Suppress("unused_parameter") v: View) {

    }

    fun bluetooth(@Suppress("unused_parameter") v: View) {

    }
}