package com.lis.lis.ui.activities

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lis.lis.R
import com.lis.lis.services.BluetoothCommunicationService
import com.lis.lis.ui.fragments.DisconnectBTFragment
import java.lang.Exception
import java.util.*


private const val ENABLE_BT = 1
private const val BLUETOOTH_MODULE = "98:D3:32:31:91:8F"

class HomeActivity: AppCompatActivity(), DisconnectBTFragment.BTDisconnectListener {

    private val mUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private var myBluetoothAdapter: BluetoothAdapter? = null
    private val brDeviceFound = DeviceFoundReceiver()
    private val brConnChanged = BTConnectionChangedReceiver()
    private val brBTStateChanged = BTStateChangedReceiver()
    private lateinit var bluetoothCommunicationService: BluetoothCommunicationService
    private var deviceConnected = false
    private var device: BluetoothDevice? = null

    inner class DeviceFoundReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action

            if(action != null && action == BluetoothDevice.ACTION_FOUND) {
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                if(device!!.address == BLUETOOTH_MODULE) {
                    pickDevice()
                }
            }
        }
    }

    inner class BTConnectionChangedReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action

            if(action != null && action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val myDevice: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                if (myDevice.bondState == BluetoothDevice.BOND_BONDED)
                    device = myDevice

                if(myDevice.bondState == BluetoothDevice.BOND_NONE)
                    device = null
            }
        }
    }

    inner class BTStateChangedReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent!!.action
            if(action == BluetoothAdapter.ACTION_STATE_CHANGED && myBluetoothAdapter!!.state== BluetoothAdapter.STATE_OFF)
                device = null
        }
    }

    private val handler = Handler {
        if(!deviceConnected) {
            Toast.makeText(this, getString(R.string.device_not_found), Toast.LENGTH_LONG).show()
            myBluetoothAdapter!!.cancelDiscovery()
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        myBluetoothAdapter = requestBluetoothAdapter()

        val filterBRConnChanged = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        val filterBRStateChanged = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(brConnChanged, filterBRConnChanged)
        registerReceiver(brBTStateChanged, filterBRStateChanged)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(brDeviceFound)
        } catch (e: Exception) {}
        try {
            unregisterReceiver(brBTStateChanged)
        } catch (e: Exception) {}
        try {
            unregisterReceiver(brConnChanged)
        } catch (e: Exception) {}
    }

    fun openCards(@Suppress("unused_parameter") v: View) {
        val intent = Intent(this, CardsActivity::class.java)
        startActivity(intent)
    }

    fun openSleep(@Suppress("unused_parameter") v: View) {
        if(device != null) {
            val intent = Intent(this, SleepActivity::class.java)
            startActivity(intent)
        }
        else {
            Toast.makeText(this, getString(R.string.device_must_be_connected), Toast.LENGTH_LONG).show()
        }
    }

    fun openStats(@Suppress("unused_parameter") v: View) {
        val intent = Intent(this, StatisticsActivity::class.java)
        startActivity(intent)
    }

    fun bluetooth(@Suppress("unused_parameter") v: View) {
        if(!deviceConnected) {
            if (myBluetoothAdapter == null) {
                Toast.makeText(this, getString(R.string.BT_conn_not_possible), Toast.LENGTH_LONG).show()
                return
            }

            if (!myBluetoothAdapter!!.isEnabled) {
                val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBTIntent, ENABLE_BT)
            }

            //TODO make dialog: Searching for devices with cancel button

            val timer = Runnable {
                try {
                    Thread.sleep(7000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                handler.sendEmptyMessage(0)
            }
            val threadTmr = Thread(timer)
            threadTmr.start()

            if (myBluetoothAdapter!!.isDiscovering) {
                Toast.makeText(this, "Sucht nach Geräten", Toast.LENGTH_LONG).show()
            } else {
                checkBTPermissions()
                myBluetoothAdapter!!.startDiscovery()

                val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
                registerReceiver(brDeviceFound, discoverDevicesIntent)
            }
        } else {
            val dialog = DisconnectBTFragment()
            dialog.show(supportFragmentManager, "DisconnectBTFragment")
        }
    }

    private fun requestBluetoothAdapter(): BluetoothAdapter? {
        return BluetoothAdapter.getDefaultAdapter() ?: return null
    }

    private fun checkBTPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            var permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")

            if (permissionCheck != 0) {
                this.requestPermissions(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), 1001
                )
            }
        }
    }

    private fun pickDevice() {

        myBluetoothAdapter!!.cancelDiscovery()

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2) {
            device!!.createBond()
            bluetoothCommunicationService = BluetoothCommunicationService(this, resources)
            bluetoothCommunicationService.startClient(device!!, mUUID)
        }

        deviceConnected = true
        Toast.makeText(this, getString(R.string.device_found), Toast.LENGTH_LONG).show()
    }

    override fun disconnectBTDevice() {
        if(deviceConnected) {
            device = null
            myBluetoothAdapter!!.disable()
            deviceConnected = false
        }
    }
}