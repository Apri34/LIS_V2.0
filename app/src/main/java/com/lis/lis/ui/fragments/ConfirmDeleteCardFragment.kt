package com.lis.lis.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDialogFragment
import com.lis.lis.R

class ConfirmDeleteCardFragment: AppCompatDialogFragment() {

    private var onDeleteCardListener: IDeleteCard? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val cardId = arguments!!.getInt("cardId")

        builder.setTitle(getString(R.string.confirm_delete_card))
            .setNegativeButton(getString(R.string.no)) {_,_->

            }
            .setPositiveButton(getString(R.string.yes)) {_,_->
                if(onDeleteCardListener != null)
                    onDeleteCardListener!!.deleteCard(cardId)
                else
                    Log.i(activity.toString(), "must set and implement IDeleteCard")
            }

        return builder.create()
    }

    interface IDeleteCard {
        fun deleteCard(cardId: Int)
    }

    fun setOnDeleteCardListener(listener: IDeleteCard) {
        onDeleteCardListener = listener
    }
}