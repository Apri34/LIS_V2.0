package com.lis.lis.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.lis.lis.R
import java.lang.ClassCastException

class SearchingDevicesProgressFragment: DialogFragment() {

    private lateinit var listener: ICancelDiscovery

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = activity!!.layoutInflater
        val v = inflater.inflate(R.layout.progressdialog_searching_device, null)
        builder.setView(v)
            .setNegativeButton(android.R.string.cancel) { _, _->
                listener.cancelDiscovery()
            }

        return builder.create()
    }

    interface ICancelDiscovery {
        fun cancelDiscovery()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as ICancelDiscovery
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement BTDisconnectListener")
        }
    }
}