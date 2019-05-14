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

    private val valueReceiver = ValueReceiver()

    private inner class ValueReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val extras = intent!!.extras!!
            val pulse = extras.getString(PULS)!!
            val hfVar = extras.getString(HFVAR)!!
            val isMoving = extras.getInt(ISMOVING)

            //TODO something

            detectSleepStage()


            //TODO put real values
            sendValues(pulse.toInt(), 0, hfVar.toInt(), 0, 0, 0)
        }

        fun sendValues(pulse: Int, pulseAvg: Int, hfVar: Int, hfVarAvg: Int, sleepStage: Int, isMovingCount: Int) {
            val ext = Bundle()
            ext.putInt("pulse", pulse)
            ext.putInt("pulseAvg", pulseAvg)
            ext.putInt("hfvar", hfVar)
            ext.putInt("hfvarAvg", hfVarAvg)
            ext.putInt("sleepStage", sleepStage)
            ext.putInt("isMovingCount", isMovingCount)
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
        //TODO something
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
}