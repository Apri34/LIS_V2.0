package com.lis.lis.services

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import com.lis.lis.constants.SEND_SAVEVALUES_BROADCAST_ACTION
import org.json.JSONObject
import java.io.*
import java.util.*

class SaveValuesService: Service() {

    private val valueReceiver = ValueReceiver()
    private val intentFilter = IntentFilter(SEND_SAVEVALUES_BROADCAST_ACTION)
    private var outputStream: OutputStream? = null
    private var count = 1

    private inner class ValueReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {

            //TODO Nach der Implementierung von SleepStageService schauen, ob die Werte noch passen (ob es die richtigen sind und die richtigen Datentypen)
            val extras = intent!!.extras!!
            val pulse = extras.getInt("pulse")
            val time = Calendar.getInstance().timeInMillis
            val pulseAvg = extras.getInt("pulseAvg")
            val hfvar = extras.getInt("hfvar")
            val hfvarAvg = extras.getInt("hfvarAvg")
            val sleepStage = extras.getInt("sleepStage")
            val isMovingCount = extras.getInt("isMovingCount")

            val jsonString = "\"$count\":{" +
                    "\"time\":$time," +
                    "\"pulse\":$pulse," +
                    "\"pulseAvg\":$pulseAvg," +
                    "\"hfvar\":$hfvar," +
                    "\"hfvarAvg\":$hfvarAvg," +
                    "\"sleepStage\":$sleepStage," +
                    "\"isMovingCount\":$isMovingCount" +
                    "}"

            if(outputStream != null) {
                try {
                    if(count != 1) {
                        outputStream!!.write(",".toByteArray())
                    }
                    outputStream!!.write(jsonString.toByteArray())
                    count++
                    Log.i(this.toString(), "Values saved: Pulse = $pulse")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        this.registerReceiver(valueReceiver, intentFilter)

        val fileNameFilter = FilenameFilter(
            fun (_, name): Boolean {
                val lowerCaseName = name.toLowerCase()
                return lowerCaseName.contains("json_values")
            }
        )
        val files = filesDir.listFiles(fileNameFilter)
        var id = 0

        for(i in 0 until files.size) {
            var inputStream: FileInputStream? = null
            var jsonParameters = ""
            try {
                inputStream = openFileInput(files[i].name)
                val reader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(reader)
                jsonParameters = bufferedReader.readLine()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            if(jsonParameters != "") {
                val obj = JSONObject(jsonParameters)
                val objId = obj.getInt("id")
                id = if(id > objId) id else objId + 1
            }
        }

        val filename = "json_values_$id"
        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE)
            outputStream!!.write(("{\"startTime\":" + Calendar.getInstance().timeInMillis.toString() + "," +
                    "\"id\":$id"+
                    "}\n").toByteArray())
            outputStream!!.write("{".toByteArray())
            Log.i(this.toString(), "File created: id = $id")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(valueReceiver)

        if(outputStream != null) {
            try {
                outputStream!!.write("}".toByteArray())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            try {
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        count = 1
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }
}