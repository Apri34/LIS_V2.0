package com.lis.lis.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.lis.lis.R

class ConnectProgressFragment: DialogFragment() {

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = activity!!.layoutInflater
        val v = inflater.inflate(R.layout.progressdialog_connecting_bluetooth, null)
        builder.setView(v)

        return builder.create()
    }
}