package com.lis.lis.ui.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.lis.lis.R
import com.lis.lis.constants.*
import com.lis.lis.database.AppDatabase
import com.lis.lis.database.DatabaseInitializer
import com.lis.lis.services.OutputService
import com.lis.lis.services.SaveValuesService
import com.lis.lis.services.SleepstageService

class SleepActivity: AppCompatActivity() {

    private val valueReceiver = ValueReceiver()
    private lateinit var tvP: TextView
    private lateinit var tvPulse: TextView
    private lateinit var tvH: TextView
    private lateinit var tvHFV: TextView
    private lateinit var tvM: TextView
    private lateinit var tvMoving: TextView
    private lateinit var btnSleep: Button
    private lateinit var btnGetUp: Button
    private lateinit var spStacks: Spinner

    private inner class ValueReceiver: BroadcastReceiver() {
        override fun onReceive(p0: Context?, intent: Intent?) {
            val extras = intent!!.extras
            if(extras != null) {
                tvPulse.text = extras.getInt("pulseAvg").toString()
                tvHFV.text = extras.getInt("hfvarAvg").toString()
                tvMoving.text = extras.getInt("isMovingCount").toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleep)

        tvP = findViewById(R.id.tvP)
        tvPulse = findViewById(R.id.tvPulse)
        tvH = findViewById(R.id.tvH)
        tvHFV = findViewById(R.id.tvHFV)
        tvM = findViewById(R.id.tvM)
        tvMoving = findViewById(R.id.tvMoving)
        btnGetUp = findViewById(R.id.buttonGetUp)
        btnSleep = findViewById(R.id.buttonSleepSleep)
        spStacks = findViewById(R.id.spinnerStackSleep)
        val db = AppDatabase.getInstance(this)
        val stacks = DatabaseInitializer.getInstance().getAllStacks(db.stackDao())
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stacks)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStacks.adapter = spinnerAdapter

        val filter = IntentFilter(SEND_SAVEVALUES_BROADCAST_ACTION)
        registerReceiver(valueReceiver, filter)

        startSleepStageService()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(valueReceiver)
        stopSleepStageService()
    }

    fun sleep(@Suppress("UNUSED_PARAMETER") v: View) {
        tvP.visibility = View.INVISIBLE
        tvPulse.visibility = View.INVISIBLE
        tvH.visibility = View.INVISIBLE
        tvHFV.visibility = View.INVISIBLE
        tvM.visibility = View.INVISIBLE
        tvMoving.visibility = View.INVISIBLE
        btnSleep.visibility = View.INVISIBLE
        btnSleep.isEnabled = false
        btnGetUp.visibility = View.VISIBLE
        btnGetUp.isEnabled = true

        if(spStacks.selectedItem != null) {
            startOutputService(spStacks.selectedItem.toString())
        }
        startSaveValuesService()
    }

    fun getUp(@Suppress("UNUSED_PARAMETER") v: View) {
        tvP.visibility = View.VISIBLE
        tvPulse.visibility = View.VISIBLE
        tvH.visibility = View.VISIBLE
        tvHFV.visibility = View.VISIBLE
        tvM.visibility = View.VISIBLE
        tvMoving.visibility = View.VISIBLE
        btnSleep.visibility = View.VISIBLE
        btnSleep.isEnabled = true
        btnGetUp.visibility = View.INVISIBLE
        btnGetUp.isEnabled = false

        stopOutputService()
        stopSaveValuesService()
    }

    private fun startSleepStageService() {
        val intent = Intent(this, SleepstageService::class.java)
        startService(intent)
    }
    private fun startSaveValuesService() {
        val intent = Intent(this, SaveValuesService::class.java)
        startService(intent)
    }
    private fun startOutputService(stackName: String) {
        val intent = Intent(this, OutputService::class.java)
        intent.putExtra(STACKNAME, stackName)
        startService(intent)
    }

    private fun stopSleepStageService() {
        val intent = Intent(this, SleepstageService::class.java)
        stopService(intent)
    }
    private fun stopSaveValuesService() {
        val intent = Intent(this, SaveValuesService::class.java)
        stopService(intent)
    }
    private fun stopOutputService() {
        val intent = Intent(this, OutputService::class.java)
        stopService(intent)
    }
}