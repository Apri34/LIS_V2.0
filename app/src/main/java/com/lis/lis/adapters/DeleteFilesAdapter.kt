package com.lis.lis.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.lis.lis.R

class DeleteFilesAdapter(private val context: Context, val files: ArrayList<String>): RecyclerView.Adapter<DeleteFilesAdapter.DeleteFilesViewHolder>() {

    private var selectionTracker: SelectionTracker<Long>? = null

    fun setSelectionTracker(selectionTracker: SelectionTracker<Long>?) {
        this.selectionTracker = selectionTracker
    }

    inner class DeleteFilesViewHolder(view: View): RecyclerView.ViewHolder(view) {
        private val tvFile: TextView = view.findViewById(R.id.tvDeleteFiles)
        private val cbFile: CheckBox = view.findViewById(R.id.cbDeleteFiles)
        private val layout: ConstraintLayout = view.findViewById(R.id.clDeleteFiles)

        private lateinit var details: Details
        private var isSelected = false

        fun bind(filename: String, position: Int) {
            tvFile.text = filename
            details = Details(position)
            cbFile.isClickable = false
            cbFile.isActivated = false

            layout.setOnClickListener {
                if (selectionTracker != null && !isSelected) {
                    selectionTracker!!.select(details.selectionKey)
                    isSelected = true
                } else if (selectionTracker != null && isSelected) {
                    selectionTracker!!.deselect(details.selectionKey)
                    isSelected = false
                }
            }
            layout.setOnLongClickListener { false }

            if(selectionTracker != null) {
                cbFile.isChecked = selectionTracker!!.isSelected(details.selectionKey)
            }
        }

        fun getItemDetails(): Details = details
    }

    inner class Details(position: Int): ItemDetailsLookup.ItemDetails<Long>() {
        private val pos: Int = position
        override fun getSelectionKey(): Long {
            return pos.toLong()
        }
        override fun getPosition(): Int {
            return pos
        }
    }

    inner class KeyProvider: ItemKeyProvider<Long>(ItemKeyProvider.SCOPE_MAPPED) {
        override fun getKey(position: Int): Long? {
            return position.toLong()
        }
        override fun getPosition(key: Long): Int {
            return key.toInt()
        }
    }

    inner class Predicate: SelectionTracker.SelectionPredicate<Long>() {
        override fun canSelectMultiple(): Boolean {
            return true
        }

        override fun canSetStateForKey(key: Long, nextState: Boolean): Boolean {
            return true
        }

        override fun canSetStateAtPosition(position: Int, nextState: Boolean): Boolean {
            return true
        }

    }

    inner class DetailsLookup(private val recyclerView: RecyclerView): ItemDetailsLookup<Long>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Long>? {
            val view: View? = recyclerView.findChildViewUnder(e.x, e.y)
            if(view != null) {
                val holder: RecyclerView.ViewHolder = recyclerView.getChildViewHolder(view)
                if(holder is DeleteFilesViewHolder) {
                    return holder.getItemDetails()
                }
            }
            return null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeleteFilesViewHolder {
        return DeleteFilesViewHolder(LayoutInflater.from(context).inflate(R.layout.view_delete_files, parent, false))
    }

    override fun getItemCount() = files.size

    override fun onBindViewHolder(holder: DeleteFilesViewHolder, position: Int) {
        holder.bind(files[position], position)
    }
}