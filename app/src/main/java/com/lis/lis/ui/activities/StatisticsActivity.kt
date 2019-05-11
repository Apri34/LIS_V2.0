package com.lis.lis.ui.activities

import android.os.AsyncTask
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.forEach
import com.jjoe64.graphview.GraphView
import com.lis.lis.R
import com.lis.lis.adapters.HintAdapter
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.ref.WeakReference
import java.sql.Date
import java.text.DateFormat
import kotlin.collections.ArrayList

class StatisticsActivity: AppCompatActivity() {

    private lateinit var graphView: GraphView
    private lateinit var fileMap: SparseArray<String>

    private lateinit var timeArrayList: ArrayList<Long>
    private lateinit var pulseArrayList: ArrayList<Int>
    private lateinit var pulseDiaArrayList: ArrayList<Int>
    private lateinit var hfvarArrayList: ArrayList<Int>
    private lateinit var hfvarDiaArrayList: ArrayList<Int>
    private lateinit var stageArrayList: ArrayList<Int>
    private lateinit var movingArrayList: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_statistics)

        timeArrayList = ArrayList()
        pulseArrayList = ArrayList()
        pulseDiaArrayList = ArrayList()
        hfvarArrayList = ArrayList()
        hfvarDiaArrayList = ArrayList()
        stageArrayList = ArrayList()
        movingArrayList = ArrayList()

        graphView = findViewById(R.id.graphView)
        fileMap = getFileMap()

        val spinnerDataset = findViewById<Spinner>(R.id.spinnerDataset)
        spinnerDataset.adapter = getDatasetAdapter(fileMap)
        spinnerDataset.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                fetchData(parent!!.selectedItem.toString())
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val spinnerDataType = findViewById<Spinner>(R.id.spinnerDataType)
        spinnerDataType.adapter = getDataTypeAdapter()
        spinnerDataType.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                //TODO make graph
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
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
                val text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date)
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
        filenames.add("Select a file")

        val adapter = HintAdapter(this, android.R.layout.simple_spinner_item, filenames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun getDataTypeAdapter(): SpinnerAdapter {
        val types = ArrayList<String>()
        //TODO add right types
        types.add(getString(R.string.pulse))
        types.add(getString(R.string.pulse))
        types.add(getString(R.string.pulse))
        types.add(getString(R.string.pulse))
        types.add(getString(R.string.pulse))
        types.add("Select a type")

        val adapter = HintAdapter(this, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }

    private fun fetchData(spinnerItem: String): Boolean {
        val id = fileMap.keyAt(fileMap.indexOfValue(spinnerItem))

        val filenameFilter = FilenameFilter (
            fun(_, name): Boolean {
                val lowerCaseName = name.toLowerCase()
                return lowerCaseName.contains(String.format("json_values_%1i", id))
        })
        val files = filesDir.listFiles(filenameFilter)
        if(files.size != 1) return false

        val task = FetchData(this)
        return task.execute(files[0].name).get()
    }

    private fun getNewGraph() {
        TODO("not implemented")
    }

    private class FetchData(context: StatisticsActivity): AsyncTask<String, Void, Boolean>() {

        private val weakReference = WeakReference<StatisticsActivity>(context)

        override fun onPreExecute() {
            super.onPreExecute()
            //TODO show dialog or fetching screen in graph view
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
                while(weakReference.get() != null && !weakReference.get()!!.isFinishing) {
                    try {
                        val values = obj.getJSONObject(count.toString())
                        weakReference.get()!!.timeArrayList.add(values.getLong("time"))
                        weakReference.get()!!.pulseArrayList.add(values.getInt("pulse"))
                        weakReference.get()!!.pulseDiaArrayList.add(values.getInt("pulseDiameter"))
                        weakReference.get()!!.hfvarArrayList.add(values.getInt("hfvar"))
                        weakReference.get()!!.hfvarDiaArrayList.add(values.getInt("hfvarDiameter"))
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
            //TODO return from dialog or fetching screen in graph view
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
    }
}