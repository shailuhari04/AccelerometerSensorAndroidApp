package com.droidplusplus.accelerometersensorapp.simple

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.droidplusplus.accelerometersensorapp.R
import kotlinx.android.synthetic.main.activity_simple.*

class SimpleActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)
        init()
    }

    private fun init() {
        //SensorManager initialization
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if (p0!!.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event = p0)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getAccelerometer(event: SensorEvent) {
        // Movement
        val xVal = event.values[0]
        val yVal = event.values[1]
        val zVal = event.values[2]
        valueOfXTV.text = "Value Of X Axis: $xVal"
        valueOfYTV.text = "Value Of Y Axis: $yVal"
        valueOfZTV.text = "Value Of Z Axis: $zVal"

        //Calculation
        val accelerationSquareRoot =
            (xVal * xVal + yVal * yVal + zVal * zVal) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH)

        if (accelerationSquareRoot >= 3) {
            Toast.makeText(this@SimpleActivity, "Device was shuffled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager!!.registerListener(
            this@SimpleActivity,
            sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager!!.unregisterListener(this@SimpleActivity)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.i("Simple", "onAccuracyChanged : $p0 -- $p1")
    }
}