package com.lis.lis.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.lis.lis.constants.*
import com.lis.lis.enums.Sleepstage

class SleepstageService: Service() {

    private var isInStage = false
    private val broadcastIntent = Intent(SEND_SAVEVALUES_BROADCAST_ACTION)
    private var prevStage = Sleepstage.WAKE
    private var stage = Sleepstage.WAKE
    private var c = 0
    private var pulseArray = ArrayList<Int>()
    private var hfVarArray = ArrayList<Int>()
    private var pulseAvg60 = 60
    private var pulseAvg20 = 60
    private var hfVarAvg60 = 0
    private var hfVarAvg20 = 0

    private val valueReceiver = ValueReceiver()

    private inner class ValueReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val extras = intent!!.extras!!
            val pulse = extras.getString(PULS)!!
            val hfVar = extras.getString(HFVAR)!!
            val isMoving = extras.getInt(ISMOVING)

            if(pulse.toInt() != 0 && hfVar.toInt() != 0) {
                getNewValues(pulse.toInt(), hfVar.toInt())
                detectSleepStage()
                sendValues(pulse.toInt(), pulseAvg60, pulseAvg20, hfVar.toInt(), hfVarAvg60, hfVarAvg20, 0, isMoving)
            }
        }

        fun sendValues(pulse: Int, pulseAvg60: Int, pulseAvg20: Int, hfVar: Int, hfVarAvg60: Int, hfVarAvg20: Int, sleepStage: Int, isMoving: Int) {
            val ext = Bundle()
            ext.putInt("pulse", pulse)
            ext.putInt("pulseAvg60", pulseAvg60)
            ext.putInt("pulseAvg20", pulseAvg20)
            ext.putInt("hfvar", hfVar)
            ext.putInt("hfvarAvg60", hfVarAvg60)
            ext.putInt("hfvarAvg20", hfVarAvg20)
            ext.putInt("sleepStage", sleepStage)
            ext.putInt("isMoving", isMoving)
            broadcastIntent.putExtras(ext)
            sendBroadcast(broadcastIntent)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val filter = IntentFilter(SEND_VALUES_BROADCAST_ACTION)
        registerReceiver(valueReceiver, filter)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(valueReceiver)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun detectSleepStage() {
        c++
        prevStage = stage
        if(c == 10) {
            stage = if(stage == Sleepstage.WAKE) Sleepstage.NREM else Sleepstage.WAKE
            c = 0
        }

        isInStage = stage == Sleepstage.WAKE

        if (prevStage != stage) {
            val intent = Intent(SLEEP_STAGE_BROADCAST_ACTION)
            intent.putExtra(IS_IN_STAGE, isInStage)
            sendBroadcast(intent)
            Log.i("Output", "Broadcast sent")
        }
    }

    private fun getNewValues(pulse: Int, hfVar: Int) {
        pulseArray.add(pulse)
        if(pulseArray.size > 60)
            pulseArray.removeAt(0)

        hfVarArray.add(hfVar)
        if(hfVarArray.size > 60)
            hfVarArray.removeAt(0)

        var pulseSum60 = 0
        var pulseSum20 = 0
        for(i in 0 until pulseArray.size) {
            pulseSum60 += pulseArray[i]
            if(i > pulseArray.size - 20)
                pulseSum20 += pulseArray[i]
        }

        pulseAvg60 = pulseSum60 / pulseArray.size
        pulseAvg20 = if(pulseArray.size >= 20)
            pulseSum20 / 20
        else
            pulseSum20 / pulseArray.size

        var hfvarSum60 = 0
        var hfvarSum20 = 0
        for(i in 0 until hfVarArray.size) {
            hfvarSum60 += hfVarArray[i]
            if(i > hfVarArray.size - 20)
                hfvarSum20 += hfVarArray[i]
        }
        hfVarAvg60 = hfvarSum60 / hfVarArray.size
        hfVarAvg20 = if(hfVarArray.size > 20)
            hfvarSum20 / 20
        else
            hfvarSum20 / hfVarArray.size
    }
}