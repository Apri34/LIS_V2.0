package com.lis.lis.ui.activities

import android.graphics.Point
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.lis.lis.adapters.HintAdapter
import com.lis.lis.R
import com.lis.lis.database.AppDatabase
import com.lis.lis.database.DatabaseInitializer
import com.lis.lis.database.entities.Card
import com.lis.lis.database.entities.Stack
import com.lis.lis.ui.fragments.ConfirmDeleteCardFragment
import com.lis.lis.ui.fragments.ConfirmDeleteStackFragment
import com.lis.lis.ui.fragments.MakeNewCardFragment
import com.lis.lis.ui.fragments.MakeNewStackFragment

class CardsActivity: AppCompatActivity(), MakeNewStackFragment.IMakeNewStack, MakeNewCardFragment.IMakeNewCard, ConfirmDeleteCardFragment.IDeleteCard, ConfirmDeleteStackFragment.IDeleteStack {
    override fun deleteStack(stackId: Int) {
        dbInitializer.deleteStackByName(db.stackDao(), spinnerStack.selectedItem.toString())
        initSpinnerAdapter()
        getNewTable()
    }

    override fun deleteCard(cardId: Int) {
        dbInitializer.deleteCardById(db.cardDao(), cardId)
        getNewTable()
    }

    override fun onCardMade(stackId: Int, val1: String, val2: String) {
        val card = Card(0, stackId, val1, val2)
        dbInitializer.insertCard(db.cardDao(), card)
        getNewTable()
    }

    override fun onNewStackVoc(stackName: String, lang1: String, lang2: String) {
        val stack = Stack(0, stackName, lang1, lang2)
        dbInitializer.insertStack(db.stackDao(), stack)
        initSpinnerAdapter()
        spinnerStack.setSelection(spinnerStack.adapter.count - 1)
        getNewTable()
    }

    override fun onNewStackNonVoc(stackName: String, stackType: String) {
        if(stackType == getString(R.string.spinner_question)) {
            val stack = Stack(0, stackName, getString(R.string.stack_question), getString(R.string.stack_answer))
            dbInitializer.insertStack(db.stackDao(), stack)
            initSpinnerAdapter()
            spinnerStack.setSelection(spinnerStack.adapter.count - 1)
            getNewTable()
        }
        else if(stackType == getString(R.string.spinner_enumeration)) {
            //TODO implement Enumeration
            Toast.makeText(applicationContext, "Enumeration not implemented", Toast.LENGTH_LONG).show()
        }
    }

    private lateinit var spinnerStack: Spinner
    private lateinit var textViewVal1: TextView
    private lateinit var textViewVal2: TextView
    private lateinit var tableLayout: TableLayout
    private var selectedRow: TableRow? = null

    private lateinit var gd1: GradientDrawable
    private lateinit var gd2: GradientDrawable

    private lateinit var db: AppDatabase
    private lateinit var dbInitializer: DatabaseInitializer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cards)

        db = AppDatabase.getInstance(this)
        dbInitializer = DatabaseInitializer.getInstance()

        spinnerStack = findViewById(R.id.spinnerStackCards)
        initSpinnerAdapter()
        spinnerStack.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?){}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                getNewTable()
            }
        }

        textViewVal1 = findViewById(R.id.textViewValue1)
        textViewVal2 = findViewById(R.id.textViewValue2)
        tableLayout = findViewById(R.id.tableLayout)

        gd1 = GradientDrawable()
        gd1.setStroke(1, ContextCompat.getColor(this, R.color.black))
        gd1.setColor(ContextCompat.getColor(this, R.color.white))

        gd2 = GradientDrawable()
        gd2.setColor(ContextCompat.getColor(this, R.color.colorBlueSelected))
        gd2.setStroke(3, ContextCompat.getColor(this, R.color.black))

        getNewTable()
    }

    override fun onResume() {
        super.onResume()
        initSpinnerAdapter()
    }

    private fun getNewTable() {
        tableLayout.removeAllViews()
        if(spinnerStack.selectedItem == null || spinnerStack.selectedItem.toString() == getString(R.string.spinner_cards_choose_stack)) {
            textViewVal1.text = null
            textViewVal2.text = null
            return
        }
        val stackName = spinnerStack.selectedItem.toString()
        val stack = dbInitializer.getStackByName(db.stackDao(), stackName)
        if(stack != null) {
            textViewVal1.text = stack.column1
            textViewVal2.text = stack.column2
        } else {
            Toast.makeText(this, R.string.error_no_such_stack, Toast.LENGTH_LONG).show()
            return
        }
        val cards = dbInitializer.getCardsByStackName(db.cardDao(), stackName)
        cards.forEach { card ->
            tableLayout.addView(createTableRow(card))
        }
    }

    private fun createTableRow(card: Card): TableRow {
        val tableRow = TableRow(this)
        tableRow.addView(createTextView(card.cardId.toString(), 0))
        tableRow.addView(createTextView(card.val1, 1))
        tableRow.addView(createTextView(card.val2, 1))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            tableRow.background = gd1
        }

        tableRow.isClickable = true
        tableRow.setOnClickListener {
            if(selectedRow == it) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                    selectedRow!!.background = gd1
                selectedRow = null
            } else {
                if (selectedRow != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    selectedRow!!.background = gd1
                }

                selectedRow = it as TableRow
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    selectedRow!!.background = gd2
                } else {
                    Toast.makeText(
                        this,
                        String.format(getString(R.string.table_row_chose), card.val1, card.val2),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        return tableRow
    }

    private fun createTextView(text: String, weight: Int): TextView {
        val tv = TextView(this)
        tv.text = text
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        tv.width = (size.x / 2 - 12) * weight
        tv.gravity = Gravity.CENTER
        tv.textSize = 18f
        tv.setTextColor(ContextCompat.getColor(this, R.color.black))

        return tv
    }

    fun newCard(@Suppress("unused_parameter") v: View) {
        val makeNewCardFragment = MakeNewCardFragment()
        if(spinnerStack.selectedItem.toString() == getString(R.string.spinner_cards_choose_stack) || spinnerStack.selectedItem == null) {
            Toast.makeText(this, getString(R.string.please_choose_type), Toast.LENGTH_LONG).show()
            return
        }

        val stack = dbInitializer.getStackByName(db.stackDao(), spinnerStack.selectedItem.toString())
        if(stack != null) {
            val bundle = Bundle()
            bundle.putInt("stackId", stack.stackId)
            bundle.putString("col1", stack.column1)
            bundle.putString("col2", stack.column2)
            makeNewCardFragment.arguments = bundle
            makeNewCardFragment.setOnCardMadeListener(this)
            makeNewCardFragment.show(supportFragmentManager, "MakeNewCardDialog")
        } else
            Toast.makeText(this, getString(R.string.error_no_such_stack), Toast.LENGTH_LONG).show()
    }

    fun newStack(@Suppress("unused_parameter") v: View) {
        val newStackFragment = MakeNewStackFragment()
        newStackFragment.setOnStackMadeListener(this)
        newStackFragment.show(supportFragmentManager, "new stack")
    }

    fun deleteCard(@Suppress("unused_parameter") v: View) {
        if(selectedRow == null) {
            Toast.makeText(this, getString(R.string.choose_row), Toast.LENGTH_LONG).show()
        } else {
            val confirmDialog = ConfirmDeleteCardFragment()
            val bundle = Bundle()
            val cardId = (selectedRow!!.getChildAt(0) as TextView).text.toString().toInt()
            bundle.putInt("cardId", cardId)
            confirmDialog.arguments = bundle
            confirmDialog.setOnDeleteCardListener(this)
            confirmDialog.show(supportFragmentManager, "ConfirmDeleteCardDialog")
        }
    }

    fun deleteStack(@Suppress("unused_parameter") v: View) {
        val confirmDialog = ConfirmDeleteStackFragment()
        val bundle = Bundle()
        val stackName = spinnerStack.selectedItem.toString()
        if(stackName == getString(R.string.spinner_cards_choose_stack))
        {
            Toast.makeText(this, R.string.spinner_cards_choose_stack, Toast.LENGTH_LONG).show()
        } else {
            val stack = dbInitializer.getStackByName(db.stackDao(), stackName)
            if(stack != null) {
                val stackId = stack.stackId
                bundle.putInt("stackId", stackId)
                confirmDialog.arguments = bundle
                confirmDialog.setOnStackDeleteListener(this)
                confirmDialog.show(supportFragmentManager, "ConfirmDeleteStackDialog")
            } else {
                Toast.makeText(this, R.string.error_no_such_stack, Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun initSpinnerAdapter() {
        val stacks = dbInitializer.getAllStacks(db.stackDao())
        stacks.add(getString(R.string.spinner_cards_choose_stack))
        val adapter = HintAdapter(this, android.R.layout.simple_spinner_item, stacks)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerStack.adapter = adapter
        spinnerStack.setSelection(adapter.count)
    }
}