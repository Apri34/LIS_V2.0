package com.lis.lis.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.lis.lis.R
import java.lang.ClassCastException

class FetchingDataFragment: DialogFragment() {

    private lateinit var listener: ICancelFetchingData

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = activity!!.layoutInflater
        val v = inflater.inflate(R.layout.fragment_fetching_data, null)
        builder.setView(v)
        builder.setNegativeButton(android.R.string.cancel) { _, _->
            listener.cancelFetchingData()
        }
        return builder.create()
    }

    interface ICancelFetchingData {
        fun cancelFetchingData()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as ICancelFetchingData
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement BTDisconnectListener")
        }
    }
}