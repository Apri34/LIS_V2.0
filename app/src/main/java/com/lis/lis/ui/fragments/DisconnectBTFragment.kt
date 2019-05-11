package com.lis.lis.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.lis.lis.R
import java.lang.ClassCastException

class DisconnectBTFragment: DialogFragment() {

    private lateinit var listener: BTDisconnectListener

    interface BTDisconnectListener {
        fun disconnectBTDevice()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(activity)
            builder.setMessage(R.string.bt_device_already_connected_disconnect)
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    listener.disconnectBTDevice()
                }
                .setNegativeButton(android.R.string.cancel) { _, _ ->

                }
        return builder.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as BTDisconnectListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement BTDisconnectListener")
        }
    }
}