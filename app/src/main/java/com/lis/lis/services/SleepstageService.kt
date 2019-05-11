package com.lis.lis.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.audiofx.DynamicsProcessing
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
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
            val ext = Bundle()
            ext.putInt("pulse", pulse.toInt())
            ext.putInt("pulseAvg", 0)
            ext.putInt("hfvar", hfVar.toInt())
            ext.putInt("hfvarAvg", 0)
            ext.putInt("sleepStage", 0)
            ext.putInt("isMovingCount", 0)
            broadcastIntent.putExtras(ext)
            sendBroadcast(broadcastIntent)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val intentFilter = IntentFilter(SEND_VALUES_BROADCAST_ACTION)
        registerReceiver(valueReceiver, intentFilter)
        
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