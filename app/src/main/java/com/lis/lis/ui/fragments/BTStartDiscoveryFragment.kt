package com.lis.lis.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.lis.lis.R
import java.lang.ClassCastException

class BTStartDiscoveryFragment: DialogFragment() {

    private lateinit var listener: IStartDiscovery

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(getString(R.string.bluetooth_enabled_start_discovery))
            .setNegativeButton(android.R.string.cancel) { _, _ ->

            }
            .setPositiveButton(android.R.string.yes) { _, _ ->
                listener.startDiscovery()
            }

        return builder.create()
    }

    interface IStartDiscovery {
        fun startDiscovery()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as IStartDiscovery
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }
}