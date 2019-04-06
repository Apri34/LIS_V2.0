package com.lis.lis.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import com.lis.lis.R

class ConfirmDeleteStackFragment: AppCompatDialogFragment() {

    private var onDeleteStackListener: IDeleteStack? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val stackId = arguments!!.getInt("stackId")

        builder.setTitle(getString(R.string.confirm_delete_stack))
            .setNegativeButton(R.string.no) {_,_->

            }
            .setPositiveButton(R.string.yes) {_,_->
                if(onDeleteStackListener != null)
                    onDeleteStackListener!!.deleteStack(stackId)
                else
                    Log.i(activity.toString(), "must set and implement IDeleteStack")
            }

        return builder.create()
    }

    interface IDeleteStack {
        fun deleteStack(stackId: Int)
    }

    fun setOnStackDeleteListener(listener: IDeleteStack) {
        onDeleteStackListener = listener
    }
}