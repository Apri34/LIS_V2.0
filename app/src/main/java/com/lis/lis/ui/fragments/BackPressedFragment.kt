package com.lis.lis.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.lis.lis.R
import java.lang.ClassCastException

class BackPressedFragment: DialogFragment() {

    private lateinit var listener: ILeave

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(getString(R.string.leave))
            .setNegativeButton(android.R.string.cancel) {_,_->

            }
            .setPositiveButton(R.string.yes) {_,_->
                listener.leave()
            }
        return builder.create()
    }

    interface ILeave {
        fun leave()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as ILeave
        } catch (e: ClassCastException) {
            e.printStackTrace()
        }
    }
}