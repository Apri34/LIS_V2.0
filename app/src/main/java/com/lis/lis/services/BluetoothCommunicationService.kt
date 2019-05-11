package com.lis.lis.services

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.lis.lis.R
import com.lis.lis.constants.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.NullPointerException
import java.util.*

class BluetoothCommunicationService() {
    private val myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var context: Context
    private lateinit var resources: Resources
    private lateinit var device: BluetoothDevice
    private lateinit var deviceUUID: UUID
    private var connectThread: ConnectThread? = null
    private var acceptThread: AcceptThread? = null
    private var connectedThread: ConnectedThread? = null
    private val mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    @Suppress("deprecation") private lateinit var progressDialog: ProgressDialog

    constructor(context: Context, resources: Resources) : this() {
        this.context = context
        this.resources = resources
        start()
    }

    @Synchronized private fun start() {
        if(connectThread != null) {
            connectThread!!.cancel()
            connectThread = null
        }

        if(acceptThread == null) {
            acceptThread = AcceptThread()
            acceptThread!!.start()
        }
    }

    private fun connected(mySocket: BluetoothSocket) {
        connectedThread = ConnectedThread(mySocket)
        connectedThread!!.start()
    }

    @Suppress("deprecation")
    fun startClient(device: BluetoothDevice, deviceUUID: UUID) {
        progressDialog = ProgressDialog.show(context, resources.getString(R.string.connecting_bt), resources.getString(R.string.please_wait))
        this.device = device
        this.deviceUUID = deviceUUID
        connectThread = ConnectThread()
        connectThread!!.start()
    }

    inner class ConnectThread : Thread() {

        private var mySocket: BluetoothSocket? = null
        override fun run() {
            var tmp: BluetoothSocket? = null
            try {
                tmp = device.createRfcommSocketToServiceRecord(deviceUUID)
            } catch(e: IOException) {
                e.printStackTrace()
            }

            mySocket = tmp
            myBluetoothAdapter.cancelDiscovery()

            try {
                mySocket!!.connect()
            } catch (e: IOException) {
                e.printStackTrace()
                try {
                    mySocket!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            connected(mySocket!!)
        }

        fun cancel() {
            try {
                mySocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    inner class AcceptThread : Thread() {

        private var myServerSocket: BluetoothServerSocket? = null
        init {
            var tmp: BluetoothServerSocket? = null
            try {
                tmp = myBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(resources.getString(R.string.app_name), mUUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            myServerSocket = tmp
        }

        override fun run() {
            var socket: BluetoothSocket? = null

            try {
                socket = myServerSocket!!.accept()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if(socket != null) {
                connected(socket)
            }
        }
    }

    inner class ConnectedThread(socket: BluetoothSocket): Thread() {
        private var inStream: InputStream?
        private var outStream: OutputStream?
        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                progressDialog.dismiss()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }

            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }

            inStream = tmpIn
            outStream = tmpOut
        }

        override fun run() {
            val inReader = InputStreamReader(inStream)
            val lineNumReader = LineNumberReader(inReader)

            val sendValuesIntent = Intent(SEND_VALUES_BROADCAST_ACTION)
            val extras = Bundle()

            while(true) {
                try {
                    val obj = lineNumReader.readLine()

                    val jsonObject = JSONObject(obj)
                    val pulse = jsonObject.getString("puls")
                    val hfVar = jsonObject.getString("hfvar")
                    val isMoving = jsonObject.getInt("Bewegung")
                    extras.putString(PULS, pulse)
                    extras.putString(HFVAR, hfVar)
                    extras.putInt(ISMOVING, isMoving)
                    sendValuesIntent.putExtras(extras)
                    context.sendBroadcast(sendValuesIntent)
                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }
}