package com.lis.lis.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.speech.tts.TextToSpeech
import android.util.Log
import com.lis.lis.R
import com.lis.lis.constants.*
import com.lis.lis.database.AppDatabase
import com.lis.lis.database.DatabaseInitializer
import java.util.*

class OutputService: Service() {

    private val sleepstageReceiver = SleepstageReceiver()
    private lateinit var toSpeech: TextToSpeech
    private lateinit var db: AppDatabase
    private lateinit var loc1: Locale
    private lateinit var loc2: Locale
    private lateinit var stackName: String

    private val runnableOutput = Runnable {
        val cards = DatabaseInitializer.getInstance().getCardsByStackName(db.cardDao(), stackName)
        for(i in 0 until cards.size) {
            if (Thread.currentThread().isInterrupted) {
                toSpeech.stop()
                break
            }

            toSpeech.language = loc1
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                toSpeech.speak(cards[i].val1, TextToSpeech.QUEUE_ADD, null, null)
            else {
                @Suppress("DEPRECATION")
                toSpeech.speak(cards[i].val1, TextToSpeech.QUEUE_ADD, null)
            }
            toSpeech.language = loc2
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                toSpeech.speak(cards[i].val2, TextToSpeech.QUEUE_ADD, null, null)
            else {
                @Suppress("DEPRECATION")
                toSpeech.speak(cards[i].val2, TextToSpeech.QUEUE_ADD, null)
            }

            while (toSpeech.isSpeaking) {
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }

            }
        }
    }
    private val threadOutput = Thread(runnableOutput)

    private inner class SleepstageReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            val extras = intent!!.extras
            val isInStage = extras!!.getBoolean(IS_IN_STAGE)

            if(isInStage && !threadOutput.isInterrupted) {
                threadOutput.start()
                Log.i("Output", "Output started")
            } else if(!isInStage) {
                threadOutput.interrupt()
                Log.i("Output", "Output stopped")
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        val intentFilter = IntentFilter(SLEEP_STAGE_BROADCAST_ACTION)
        registerReceiver(sleepstageReceiver, intentFilter)

        toSpeech = TextToSpeech(this, TextToSpeech.OnInitListener {})
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        db = AppDatabase.getInstance(this)
        stackName = intent!!.extras!!.getString(STACKNAME)!!
        setLangs(stackName)
        threadOutput.start()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        threadOutput.interrupt()
        unregisterReceiver(sleepstageReceiver)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    private fun setLangs(stackName: String){
        val stack = DatabaseInitializer.getInstance().getStackByName(db.stackDao(), stackName)
        val col1 = stack.column1
        val col2 = stack.column2

        loc1 = when (col1) {
            getString(R.string.spinner_german) -> Locale.GERMAN
            getString(R.string.spinner_english) -> Locale.ENGLISH
            getString(R.string.spinner_spanish) -> Locale("es", "ES")
            else -> Locale.GERMAN
        }

        loc2 = when (col2) {
            getString(R.string.spinner_german) -> Locale.GERMAN
            getString(R.string.spinner_english) -> Locale.ENGLISH
            getString(R.string.spinner_spanish) -> Locale("es", "ES")
            else -> Locale.GERMAN
        }
    }
}