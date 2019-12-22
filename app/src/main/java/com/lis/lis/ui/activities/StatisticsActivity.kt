package com.lis.lis.ui.activities

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.util.forEach
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.lis.lis.R
import com.lis.lis.adapters.HintAdapter
import com.lis.lis.ui.fragments.DeleteFilesFragment
import com.lis.lis.ui.fragments.FetchingDataProgressFragment
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.lang.ref.WeakReference
import java.sql.Date
import java.text.DateFormat
import java.util.*

class StatisticsActivity: AppCompatActivity(), FetchingDataProgressFragment.ICancelFetchingData, DeleteFilesFragment.IDeleteSelectedFiles {

    private lateinit var graphView: GraphView
    private lateinit var spinnerDataset: Spinner
    private lateinit var spinnerDataType: Spinner
    private lateinit var fileMap: SparseArray<String>
    private lateinit var task: FetchData

    private lateinit var timeArrayList: ArrayList<Long>
    private lateinit var pulseArrayList: ArrayList<Int>
    private lateinit var pulseAvgArrayList: ArrayList<Int>
    private lateinit var hfvarArrayList: ArrayList<Int>
    private lateinit var hfvarAvgArrayList: ArrayList<Int>
    private lateinit var stageArrayList: ArrayList<Int>
    private lateinit var movingArrayList: ArrayList<Int>

    private val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        timeArrayList = ArrayList()
        pulseArrayList = ArrayList()
        pulseAvgArrayList = ArrayList()
        hfvarArrayList = ArrayList()
        hfvarAvgArrayList = ArrayList()
        stageArrayList = ArrayList()
        movingArrayList = ArrayList()

        spinnerDataType = findViewById(R.id.spinnerDataType)
        spinnerDataset = findViewById(R.id.spinnerDataset)
        graphView = findViewById(R.id.graphView)

        initSpinnerAdapters()
        spinnerDataset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(parent!!.selectedItem.toString() != getString(R.string.select_file))
                    fetchData(parent.selectedItem.toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        spinnerDataType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                getNewGraph()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        initSpinnerAdapters()
    }

    fun deleteFiles(@Suppress("UNUSED_PARAMETER")v: View) {
        val dialog = DeleteFilesFragment()
        dialog.show(supportFragmentManager, "DeleteFilesDialog")
    }

    private fun getFileMap(): SparseArray<String> {
        val fileNameFilter = FilenameFilter(
            fun (_, name): Boolean {
                val lowerCaseName = name.toLowerCase()
                return lowerCaseName.contains("json_values")
            }
        )
        val files = filesDir.listFiles(fileNameFilter)

        val filenames = SparseArray<String>()
        for(i in 0 until files.size) {
            var inputStream: FileInputStream? = null
            var jsonParameters = ""
            try {
                inputStream = openFileInput(files[i].name)
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

    private fun getDatasetAdapter(fileMap: SparseArray<String>): ArrayAdapter<String> {
        val filenames = ArrayList<String>()
        fileMap.forEach(
            fun(_, name) {
                filenames.add(name)
            }
        )
        filenames.add(getString(R.string.select_file))

        val adapter = HintAdapter(this, android.R.layout.simple_spinner_item, filenames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun getDataTypeAdapter(): SpinnerAdapter {
        val types = ArrayList<String>()
        types.add(getString(R.string.pulse))
        types.add(getString(R.string.pulse_avg))
        types.add(getString(R.string.hfvar))
        types.add(getString(R.string.hfvar_avg))
        types.add(getString(R.string.sleepstage))
        types.add(getString(R.string.movement))
        types.add(getString(R.string.select_type))

        val adapter = HintAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun fetchData(spinnerItem: String) {
        val id = fileMap.keyAt(fileMap.indexOfValue(spinnerItem))

        val filenameFilter = FilenameFilter (
            fun(_, name): Boolean {
                val lowerCaseName = name.toLowerCase()
                return lowerCaseName.contains("json_values_$id")
        })
        val files = filesDir.listFiles(filenameFilter)
        if(files.size != 1) return

        task = FetchData(this)
        task.execute(files[0].name)
    }

    private fun getNewGraph() {
        if(spinnerDataType.selectedItem.toString() == getString(R.string.select_type) ||
            spinnerDataset.selectedItem.toString() == getString(R.string.select_file)) {
            return
        } else {
            graphView.removeAllSeries()

            val defaultLabelFormatter = object : DefaultLabelFormatter() {
                override fun formatLabel(value: Double, isValueX: Boolean): String {
                    return if (isValueX) {
                        timeFormat.format(java.util.Date(value.toLong()))
                    } else {
                        super.formatLabel(value, isValueX)
                    }
                }
            }
            graphView.gridLabelRenderer.labelFormatter = defaultLabelFormatter
            graphView.gridLabelRenderer.horizontalAxisTitle = getString(R.string.time)

            graphView.viewport.apply {

                isYAxisBoundsManual = true

                when(spinnerDataType.selectedItem.toString()) {
                    getString(R.string.pulse) -> {
                        setMinY(30.0)
                        setMaxY(120.0)
                    }
                    getString(R.string.pulse_avg) -> {
                        setMinY(30.0)
                        setMaxY(120.0)
                    }
                    getString(R.string.hfvar) -> {
                        setMinY(0.0)
                        setMaxY(20.0)
                    }
                    getString(R.string.hfvar_avg) -> {
                        setMinY(0.0)
                        setMaxY(25.0)
                    }
                    getString(R.string.sleepstage) -> {
                        setMinY(0.0)
                        setMaxY(4.0)
                    }
                    getString(R.string.movement) -> {
                        setMinY(0.0)
                        setMaxY(25.0)
                    }
                }

                isXAxisBoundsManual = true
                isScalable = true
                isScrollable = true
                setMinX(timeArrayList[0].toDouble())
                setMaxX(timeArrayList[timeArrayList.size - 1].toDouble())
            }

            val arrayListValues = when (spinnerDataType.selectedItem.toString()) {
                getString(R.string.pulse) -> pulseArrayList
                getString(R.string.pulse_avg) -> pulseAvgArrayList
                getString(R.string.hfvar) -> hfvarArrayList
                getString(R.string.hfvar_avg) -> hfvarAvgArrayList
                getString(R.string.sleepstage) -> stageArrayList
                getString(R.string.movement) -> movingArrayList
                else -> null
            } ?: return

            val dataPoints = Array(timeArrayList.size) {
                DataPoint(Date(timeArrayList[it]), arrayListValues[it].toDouble())
            }

            val series = LineGraphSeries<DataPoint>(dataPoints)
            series.color = ContextCompat.getColor(this, R.color.colorPrimaryDark)
            series.title = spinnerDataType.selectedItem.toString()
            graphView.addSeries(series)
        }
    }

    private class FetchData(context: StatisticsActivity): AsyncTask<String, Void, Boolean>() {

        private val weakReference = WeakReference<StatisticsActivity>(context)
        private val dialog = FetchingDataProgressFragment()

        override fun onPreExecute() {
            weakReference.get()!!.pulseArrayList.clear()
            weakReference.get()!!.hfvarArrayList.clear()
            weakReference.get()!!.hfvarAvgArrayList.clear()
            weakReference.get()!!.movingArrayList.clear()
            weakReference.get()!!.pulseAvgArrayList.clear()
            weakReference.get()!!.stageArrayList.clear()
            weakReference.get()!!.timeArrayList.clear()
            dialog.show(weakReference.get()!!.supportFragmentManager, "FetchingDataDialog")
            super.onPreExecute()
        }

        override fun doInBackground(vararg fileName: String?): Boolean {
            var inputStream: FileInputStream? = null
            var jsonValues: String? = null
            try {
                inputStream = weakReference.get()!!.openFileInput(fileName[0])
                val reader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(reader)
                bufferedReader.readLine()
                while(!isCancelled) {
                    try {
                        jsonValues = bufferedReader.readLine()
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                        break
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if(jsonValues != null) {
                        try {
                            val obj = JSONObject(jsonValues)
                            weakReference.get()!!.timeArrayList.add(obj.getLong("time"))
                            weakReference.get()!!.pulseArrayList.add(obj.getInt("pulse"))
                            weakReference.get()!!.pulseAvgArrayList.add(obj.getInt("pulseAvg60"))
                            weakReference.get()!!.hfvarArrayList.add(obj.getInt("hfvar"))
                            weakReference.get()!!.hfvarAvgArrayList.add(obj.getInt("hfvarAvg60"))
                            weakReference.get()!!.stageArrayList.add(obj.getInt("sleepStage"))
                            weakReference.get()!!.movingArrayList.add(obj.getInt("isMoving"))
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    } else {
                        break
                    }
                }
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

            return weakReference.get()!!.timeArrayList.size != 0
        }

        override fun onPostExecute(result: Boolean?) {
            super.onPostExecute(result)
            dialog.dismiss()
            if(weakReference.get() != null && !weakReference.get()!!.isFinishing) {
                if (result!!) {
                    weakReference.get()!!.getNewGraph()
                } else {
                    Log.i(weakReference.get().toString(), "Error fething data")
                    Toast.makeText(
                        weakReference.get(),
                        weakReference.get()!!.getString(R.string.error_fetching_data),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else return
        }

        override fun onCancelled() {
            super.onCancelled()
            weakReference.get()!!.pulseArrayList.clear()
            weakReference.get()!!.hfvarArrayList.clear()
            weakReference.get()!!.hfvarAvgArrayList.clear()
            weakReference.get()!!.movingArrayList.clear()
            weakReference.get()!!.pulseAvgArrayList.clear()
            weakReference.get()!!.stageArrayList.clear()
            weakReference.get()!!.timeArrayList.clear()
            weakReference.get()!!.initSpinnerAdapters()
        }
    }

    override fun cancelFetchingData() {
        task.cancel(true)
    }

    private fun initSpinnerAdapters() {
        fileMap = getFileMap()
        spinnerDataset.adapter = getDatasetAdapter(fileMap)
        spinnerDataType.adapter = getDataTypeAdapter()
        spinnerDataset.setSelection(spinnerDataset.count)
        spinnerDataType.setSelection(spinnerDataType.count)
    }

    override fun deleteSelectedFiles(selectedFiles: List<String>) {
        val ids = ArrayList<Int>()
        fileMap.forEach{key, name->
            for (i in 0 until  selectedFiles.size) {
                if(selectedFiles[i] == name)
                    ids.add(key)
            }
        }

        var deletedEverything = true
        ids.forEach {
            val filename = "$filesDir/json_values_$it"
            val file = File(filename)
            val isDeleted = file.delete()
            if(!isDeleted)
                deletedEverything = false
        }

        if(!deletedEverything)
            Toast.makeText(this, getString(R.string.error_deleting_files), Toast.LENGTH_LONG).show()

        initSpinnerAdapters()
    }
}