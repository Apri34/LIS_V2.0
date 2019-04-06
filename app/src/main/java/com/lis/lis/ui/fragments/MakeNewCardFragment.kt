package com.lis.lis.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.lis.lis.R

class MakeNewCardFragment: AppCompatDialogFragment() {

    private lateinit var etVal1: EditText
    private lateinit var etVal2: EditText
    private lateinit var tvVal1: TextView
    private lateinit var tvVal2: TextView
    private var onCardMadeListener: IMakeNewCard? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = activity!!.layoutInflater
        val v = inflater.inflate(R.layout.fragment_makenewcard, @Suppress("InflateParameters") null)

        val col1 = arguments!!.getString("col1")
        val col2 = arguments!!.getString("col2")

        builder.setView(v)
            .setTitle(getString(R.string.new_card))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.ok)) {_, _ -> } //Override in onResume
        etVal1 = v.findViewById(R.id.etVal1)
        etVal2 = v.findViewById(R.id.etVal2)
        etVal1.hint = col1
        etVal2.hint = col2
        tvVal1 = v.findViewById(R.id.tvEV1)
        tvVal2 = v.findViewById(R.id.tvEV2)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()

        val d = dialog as AlertDialog
        val posButton = d.getButton(Dialog.BUTTON_POSITIVE)
        posButton.setOnClickListener {
            if(onCardMadeListener != null) {
                var error = false
                if (etVal1.text.toString().isEmpty()) {
                    tvVal1.visibility = View.VISIBLE
                    error = true
                } else {
                    tvVal1.visibility = View.GONE
                }
                if (etVal2.text.toString().isEmpty()) {
                    tvVal2.visibility = View.VISIBLE
                    error = true
                } else {
                    tvVal2.visibility = View.GONE
                }

                if (!error) {
                    val stackId = arguments!!.getInt("stackId")
                    onCardMadeListener!!.onCardMade(stackId, etVal1.text.toString(), etVal2.text.toString())
                    d.dismiss()
                }
            }
            else {
                Log.i(activity.toString(), "must set and implement IMakeNewCardListener")
            }
        }
    }

    interface IMakeNewCard {
        fun onCardMade(stackId: Int, val1: String, val2: String)
    }

    fun setOnCardMadeListener(listener: IMakeNewCard) {
        onCardMadeListener = listener
    }
}