package com.droidplusplus.accelerometersensorapp.withgraph

import android.content.Context
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.droidplusplus.accelerometersensorapp.R
import com.droidplusplus.accelerometersensorapp.utils.AppExecutors
import com.jjoe64.graphview.LegendRenderer
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_with_graph.*
import java.io.File
import java.io.FileOutputStream
import kotlin.math.sqrt


class WithGraphActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "WithGraph"

    private val maxDataPoints = 80

    private val sensorManager: SensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    private var sampleCount: Double = 0.0

    private var xMean: Double = 0.0
    private var yMean: Double = 0.0
    private var zMean: Double = 0.0

    private var filesWriteable: Boolean = false

    private val filePath: String = "DataStorage"
    private val fileName: String = "DataLog.csv"

    private var logger: File? = null
    private var fos: FileOutputStream? = null


    private var seriesX: LineGraphSeries<DataPoint>? = null
    private var seriesY: LineGraphSeries<DataPoint>? = null
    private var seriesZ: LineGraphSeries<DataPoint>? = null


    private var seriesClamped: LineGraphSeries<DataPoint>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_with_graph)

        initializeGraph()

        sensorManagerSetUp()

        fileOperationSetUp()
    }

    private fun initializeGraph() {
        Log.d(TAG, "Start collection")

        sampleCount = 0.0
        graph.removeAllSeries()
        seriesX = LineGraphSeries()
        seriesY = LineGraphSeries()
        seriesZ = LineGraphSeries()

        seriesX?.color = Color.RED
        seriesY?.color = Color.GREEN
        seriesZ?.color = Color.BLUE

        seriesX?.thickness = 4
        seriesY?.thickness = 4
        seriesZ?.thickness = 4

        seriesClamped = LineGraphSeries()
        seriesClamped?.color = Color.BLACK
        seriesClamped?.thickness = 8

        // add the raw sensor values to the graph
        graph.addSeries(seriesX)
        graph.addSeries(seriesY)
        graph.addSeries(seriesZ)

        // Binary shake indicator
        graph.addSeries(seriesClamped)

        seriesX?.title = "x_sample"
        seriesY?.title = "y_sample"
        seriesZ?.title = "z_sample"
        seriesClamped?.title = "shake_detect"
        graph.legendRenderer.isVisible = true
        graph.legendRenderer.align = LegendRenderer.LegendAlign.BOTTOM


        // enables  horizontal scaling and scrolling
        graph.viewport.isScalable = true

        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(0.0)
        graph.viewport.setMaxX(maxDataPoints.toDouble())

        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMinY(-40.0)
        graph.viewport.setMaxY(40.0)

    }

    private fun sensorManagerSetUp() {
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun fileOperationSetUp() {
        filesWriteable = isExternalStorageAvailable() && !isExternalStorageReadOnly()

        if (filesWriteable) {
            logger = File(getExternalFilesDir(filePath), fileName)

            try {
                fos = FileOutputStream(logger)
                val header: String = "Time1,X,Y,Z,Normalized" + System.getProperty("line.separator")
                AppExecutors.instance?.diskIO()
                    ?.execute { fos?.write(header.toByteArray()) }


            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event)
        }
    }

    private fun getAccelerometer(event: SensorEvent) {

        // obtain the raw accelerometer values
        val values = event.values
        val x = values[0]
        val y = values[1]
        val z = values[2]

        Log.d(TAG, "New Update")
        Log.d(TAG, "x = $x ")
        Log.d(TAG, "y = $y ")
        Log.d(TAG, "z = $z ")


        sampleCount += 1.0

        // plot the raw sensor data
        seriesX?.appendData(
            DataPoint(sampleCount, x.toDouble()),
            true, maxDataPoints
        )
        seriesY?.appendData(
            DataPoint(sampleCount, y.toDouble()),
            true, maxDataPoints
        )
        seriesZ?.appendData(
            DataPoint(sampleCount, z.toDouble()),
            true, maxDataPoints
        )


        val xPair = highPassFilterApproximation(sampleCount, x, xMean)
        val yPair = highPassFilterApproximation(sampleCount, y, yMean)
        val zPair = highPassFilterApproximation(sampleCount, z, zMean)

        val xFiltered = xPair.first
        xMean = xPair.second

        val yFiltered = yPair.first
        yMean = yPair.second

        val zFiltered = zPair.first
        zMean = zPair.second

        val filteredAccelerationMagnitude =
            sqrt(xFiltered * xFiltered + yFiltered * yFiltered + zFiltered * zFiltered)

        var clamped = 0.0
        if (filteredAccelerationMagnitude > 2.0) {
            clamped = 20.0
        }


        seriesClamped?.appendData(
            DataPoint(sampleCount, clamped),
            true, maxDataPoints
        )

        val actualTime = event.timestamp

        // save sample data to csv file
        AppExecutors.instance?.diskIO()?.execute {
            val dataString = "$actualTime, $x, $y, $z, $filteredAccelerationMagnitude" +
                    System.getProperty("line.separator")

            fos?.write(dataString.toByteArray())
        }

    }

    private fun highPassFilterApproximation(
        count: Double,
        sample: Float,
        mean: Double
    ): Pair<Double, Double> {
        var gValue = sample.toDouble() / SensorManager.GRAVITY_EARTH
        val meanUpdate = (mean * (count - 1) + gValue) / count
        gValue -= meanUpdate

        return Pair(gValue, meanUpdate)
    }

    private fun isExternalStorageAvailable(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED == extStorageState
    }

    private fun isExternalStorageReadOnly(): Boolean {
        val extStorageState = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED_READ_ONLY == extStorageState

    }

    override fun onPause() {
        super.onPause()
        // unregister listener
        Log.d(TAG, "Stop collection")
        sensorManager.unregisterListener(this)
        AppExecutors.instance?.diskIO()?.execute {
            val dataString =
                """Stopping Collection${System.getProperty("line.separator")}"""

            fos?.write(dataString.toByteArray())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // close the File IO operation
        fos?.close()
    }

}