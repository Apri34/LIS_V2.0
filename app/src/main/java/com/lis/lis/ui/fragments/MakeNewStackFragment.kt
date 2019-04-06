package com.lis.lis.ui.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatDialogFragment
import com.lis.lis.adapters.HintAdapter
import com.lis.lis.R

class MakeNewStackFragment: AppCompatDialogFragment() {

    private lateinit var etStackName: EditText
    private lateinit var spStackType: Spinner
    private lateinit var spLang1: Spinner
    private lateinit var spLang2: Spinner
    private lateinit var tvLang1: TextView
    private lateinit var tvLang2: TextView
    private lateinit var tvEnterName: TextView
    private lateinit var tvSameLangs: TextView
    private lateinit var tvSelectLang: TextView
    private lateinit var tvSelectType: TextView
    private var onStackMadeListener: IMakeNewStack? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = activity!!.layoutInflater
        val v = inflater.inflate(R.layout.fragment_makenewstack, @Suppress("InflateParams") null)

        builder.setView(v)
            .setTitle(R.string.new_stack)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok) {_, _ -> } //Override in onResume

        etStackName = v.findViewById(R.id.etSN)
        spStackType = v.findViewById(R.id.spinnerStackTypeMakeStack)
        val types = ArrayList<String>()
        types.add(getString(R.string.spinner_vocables))
        types.add(getString(R.string.spinner_enumeration))
        types.add(getString(R.string.spinner_question))
        types.add(getString(R.string.spinner_choose_stacktype))
        val typeAdapter = HintAdapter(context, android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spStackType.adapter = typeAdapter
        spStackType.setSelection(typeAdapter.count)

        spLang1 = v.findViewById(R.id.spinnerLang1)
        spLang2 = v.findViewById(R.id.spinnerLang2)

        val langs = ArrayList<String>()
        langs.add(getString(R.string.spinner_german))
        langs.add(getString(R.string.spinner_english))
        langs.add(getString(R.string.spinner_spanish))
        langs.add(getString(R.string.spinner_choose_language))
        val langAdapter = HintAdapter(context, android.R.layout.simple_spinner_item, langs)
        langAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spLang1.adapter = langAdapter
        spLang2.adapter = langAdapter
        spLang1.setSelection(langAdapter.count)
        spLang2.setSelection(langAdapter.count)

        tvLang1 = v.findViewById(R.id.tvL1)
        tvLang2 = v.findViewById(R.id.tvL2)

        spStackType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                spLang1.isEnabled = false
                spLang2.isEnabled = false
                spLang1.visibility = View.GONE
                spLang2.visibility = View.GONE
                tvLang1.visibility = View.GONE
                tvLang2.visibility = View.GONE
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(parent!!.selectedItem.toString() == getString(R.string.spinner_vocables)) {
                    spLang1.isEnabled = true
                    spLang2.isEnabled = true
                    spLang1.visibility = View.VISIBLE
                    spLang2.visibility = View.VISIBLE
                    tvLang1.visibility = View.VISIBLE
                    tvLang2.visibility = View.VISIBLE
                } else {
                    spLang1.isEnabled = false
                    spLang2.isEnabled = false
                    spLang1.visibility = View.GONE
                    spLang2.visibility = View.GONE
                    tvLang1.visibility = View.GONE
                    tvLang2.visibility = View.GONE
                }
            }
        }

        tvEnterName = v.findViewById(R.id.tvPEN)
        tvSelectType = v.findViewById(R.id.tvPCT)
        tvSameLangs = v.findViewById(R.id.tvSLNA)
        tvSelectLang = v.findViewById(R.id.tvSL)

        return builder.create()
    }

    override fun onResume() {
        super.onResume()
        val d = dialog as AlertDialog
        val posButton = d.getButton(Dialog.BUTTON_POSITIVE)
        posButton.setOnClickListener {
            val stackName = etStackName.text.toString()
            val selectedType = spStackType.selectedItem.toString()
            if(onStackMadeListener != null) {

                var error = false
                if (stackName.isEmpty()) {
                    tvEnterName.visibility = View.VISIBLE
                    error = true
                } else {
                    tvEnterName.visibility = View.GONE
                }
                if (selectedType == getString(R.string.spinner_choose_stacktype)) {
                    tvSelectType.visibility = View.VISIBLE
                    error = true
                } else {
                    tvSelectType.visibility = View.GONE
                }
                if (selectedType == getString(R.string.spinner_vocables)) {
                    if (spLang1.selectedItem.toString() == getString(R.string.spinner_choose_language) || spLang2.selectedItem.toString() == getString(
                            R.string.spinner_choose_language
                        )
                    ) {
                        tvSameLangs.visibility = View.GONE
                        tvSelectLang.visibility = View.VISIBLE
                        error = true
                    } else if (spLang1.selectedItem.toString() == spLang2.selectedItem.toString()) {
                        tvSameLangs.visibility = View.VISIBLE
                        tvSelectLang.visibility = View.GONE
                        error = true
                    } else {
                        tvSameLangs.visibility = View.GONE
                        tvSelectLang.visibility = View.GONE
                    }
                }

                if (!error) {
                    if(selectedType == getString(R.string.spinner_vocables)) {
                        onStackMadeListener!!.onNewStackVoc(stackName, spLang1.selectedItem.toString(), spLang2.selectedItem.toString())
                    } else {
                        onStackMadeListener!!.onNewStackNonVoc(stackName, selectedType)
                    }
                    d.dismiss()
                }
            } else {
                Log.i(activity.toString(), "must set and implement IMakeNewStackListener")
            }
        }
    }

    interface IMakeNewStack {
        fun onNewStackVoc(stackName: String, lang1: String, lang2: String)
        fun onNewStackNonVoc(stackName: String, stackType: String)
    }

    fun setOnStackMadeListener(listener: IMakeNewStack) {
        onStackMadeListener = listener
    }
}