package com.lis.lis.ui.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.SparseArray
import androidx.core.util.forEach
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lis.lis.R
import com.lis.lis.adapters.DeleteFilesAdapter
import org.json.JSONObject
import java.io.*
import java.lang.ClassCastException
import java.sql.Date
import java.text.DateFormat
import java.util.*

class DeleteFilesFragment: DialogFragment() {

    private lateinit var listener: IDeleteSelectedFiles
    private lateinit var rv: RecyclerView
    private lateinit var selectionTracker: SelectionTracker<Long>
    private lateinit var viewAdapter: DeleteFilesAdapter

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context)
        val inflater = activity!!.layoutInflater
        val v = inflater.inflate(R.layout.fragment_delete_files, null)
        builder.setView(v)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                val selectedFiles = selectionTracker.selection.map {
                    viewAdapter.files[it.toInt()]
                }.toList()

                listener.deleteSelectedFiles(selectedFiles)
            }
            .setNegativeButton(android.R.string.cancel) { _, _ ->

            }
            .setMessage(getString(R.string.delete_files_question))

        rv = v.findViewById(R.id.rvDeleteFiles)
        rv.setHasFixedSize(true)
        val viewManager = LinearLayoutManager(context)
        rv.layoutManager = viewManager
        val fileMap = getFileMap()
        val filenames = ArrayList<String>()
        fileMap.forEach(
            fun(_, name) {
                filenames.add(name)
            }
        )
        viewAdapter = DeleteFilesAdapter(context!!, filenames)
        rv.adapter = viewAdapter

        selectionTracker = SelectionTracker.Builder<Long>(
            "files_selection",
            rv,
            viewAdapter.KeyProvider(),
            viewAdapter.DetailsLookup(rv),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(viewAdapter.Predicate())
            .build()
        viewAdapter.setSelectionTracker(selectionTracker)

        return builder.create()
    }

    interface IDeleteSelectedFiles {
        fun deleteSelectedFiles(selectedFiles: List<String>)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        try {
            listener = context as IDeleteSelectedFiles
        } catch (e: ClassCastException){
            e.printStackTrace()
        }
    }

    private fun getFileMap(): SparseArray<String> {
        val fileNameFilter = FilenameFilter(
            fun (_, name): Boolean {
                val lowerCaseName = name.toLowerCase()
                return lowerCaseName.contains("json_values")
            }
        )
        val files = context!!.filesDir.listFiles(fileNameFilter)

        val filenames = SparseArray<String>()
        for(i in 0 until files.size) {
            var inputStream: FileInputStream? = null
            var jsonParameters = ""
            try {
                inputStream = context!!.openFileInput(files[i].name)
                val reader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(reader)
                jsonParameters = bufferedReader.readLine()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                if(inputStream != null) {
                    try {
                        inputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }

            if(jsonParameters != "") {
                val obj = JSONObject(jsonParameters)
                val timeInMillis = obj.getLong("startTime")
                val date = Date(timeInMillis)
                val text = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.getDefault()).format(date)
                val id = obj.getInt("id")
                filenames.append(id, text)
            }
        }

        return filenames
    }
}