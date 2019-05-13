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
import com.lis.lis.ui.fragments.FetchingDataFragment
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.sql.Date
import java.text.DateFormat
import java.util.*

class StatisticsActivity: AppCompatActivity(), FetchingDataFragment.ICancelFetchingData, DeleteFilesFragment.IDeleteSelectedFiles {

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
        //TODO add right types after implementation of SleepstageService
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

    private fun fetchData(spinnerItem: String): Boolean {
        val id = fileMap.keyAt(fileMap.indexOfValue(spinnerItem))

        val filenameFilter = FilenameFilter (
            fun(_, name): Boolean {
                val lowerCaseName = name.toLowerCase()
                return lowerCaseName.contains("json_values_$id")
        })
        val files = filesDir.listFiles(filenameFilter)
        if(files.size != 1) return false

        task = FetchData(this)
        return task.execute(files[0].name).get()
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
            graphView.viewport.isYAxisBoundsManual = true

            when(spinnerDataType.selectedItem.toString()) {
                getString(R.string.pulse) -> {
                    graphView.viewport.setMinY(30.0)
                    graphView.viewport.setMaxY(120.0)
                }
                getString(R.string.pulse_avg) -> {
                    graphView.viewport.setMinY(30.0)
                    graphView.viewport.setMaxY(120.0)
                }
                getString(R.string.hfvar) -> {
                    graphView.viewport.setMinY(0.0)
                    graphView.viewport.setMaxY(20.0)
                }
                getString(R.string.hfvar_avg) -> {
                    graphView.viewport.setMinY(0.0)
                    graphView.viewport.setMaxY(25.0)
                }
                getString(R.string.sleepstage) -> {
                    graphView.viewport.setMinY(0.0)
                    graphView.viewport.setMaxY(4.0)
                }
                getString(R.string.movement) -> {
                    graphView.viewport.setMinY(0.0)
                    graphView.viewport.setMaxY(25.0)
                }
            }

            graphView.viewport.isXAxisBoundsManual = true
            graphView.viewport.isScalable = true
            graphView.viewport.isScrollable = true

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
        private val dialog = FetchingDataFragment()

        override fun onPreExecute() {
            super.onPreExecute()
            dialog.show(weakReference.get()!!.supportFragmentManager, "FetchingDataDialog")
        }

        override fun doInBackground(vararg fileName: String?): Boolean {
            var inputStream: FileInputStream? = null
            var jsonValues = ""
            try {
                inputStream = weakReference.get()!!.openFileInput(fileName[0])
                val reader = InputStreamReader(inputStream)
                val bufferedReader = BufferedReader(reader)
                bufferedReader.readLine()
                jsonValues = bufferedReader.readLine()
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

            if(jsonValues != "") {
                val obj = JSONObject(jsonValues)
                var count = 1
                while(weakReference.get() != null && !weakReference.get()!!.isFinishing && !isCancelled) {
                    try {
                        val values = obj.getJSONObject(count.toString())
                        weakReference.get()!!.timeArrayList.add(values.getLong("time"))
                        weakReference.get()!!.pulseArrayList.add(values.getInt("pulse"))
                        weakReference.get()!!.pulseAvgArrayList.add(values.getInt("pulseAvg"))
                        weakReference.get()!!.hfvarArrayList.add(values.getInt("hfvar"))
                        weakReference.get()!!.hfvarAvgArrayList.add(values.getInt("hfvarAvg"))
                        weakReference.get()!!.stageArrayList.add(values.getInt("sleepStage"))
                        weakReference.get()!!.movingArrayList.add(values.getInt("isMovingCount"))
                    } catch(e: JSONException) {
                        e.printStackTrace()
                        break
                    }
                    count++
                }
                return true
            }

            else {
                return false
            }
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