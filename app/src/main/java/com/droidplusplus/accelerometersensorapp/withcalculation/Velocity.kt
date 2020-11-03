package com.droidplusplus.accelerometersensorapp.withcalculation

import android.util.Log
import kotlin.math.pow
import kotlin.math.sqrt

class Velocity {
    private val TAG = "Velocity"
    private var mSampleCounter = 0
    private val mTotalSamples = 5
    private var time0: Long = 0
    private var aDelT0 = 0.0
    private var v0 = 0.0
    private var v = 0.0
    private val totalVelocityValues = 1000
    private var accel = DoubleArray(mTotalSamples)
    var vlArray = DoubleArray(totalVelocityValues)

    private fun getAvg(a: DoubleArray): Double {
        var total = 0.0
        for (i in a.indices) total += a[i]
        return total / a.size
    }

    private fun getAcceleration(linearAcceleration: FloatArray): Double {
        return sqrt(
            linearAcceleration[0].toDouble().pow(2.0) + linearAcceleration[0].toDouble()
                .pow(2.0) + linearAcceleration[0].toDouble().pow(2.0)
        )
    }

    fun getVelocity(linearAcceleration: FloatArray, time1: Long): Double {
        try {
            if (mSampleCounter < mTotalSamples - 1) {
                if (mSampleCounter == 0) time0 = time1
                accel[mSampleCounter] = getAcceleration(linearAcceleration)
                mSampleCounter++
            } else if (mSampleCounter == mTotalSamples - 1) {
                accel[mSampleCounter] = getAcceleration(linearAcceleration)
                val avgAccel = getAvg(accel)
                val timeDelta = (time1 - time0) / 1000
                val aDelT1 = avgAccel * timeDelta
                Log.d(TAG, "aDelT1 = $avgAccel * $timeDelta = $aDelT1")
                v = calculateVelocity(aDelT1)
                if (itration != totalVelocityValues) {
                    vlArray[itration] = v
                    itration++
                } else {
                    for (j in 0 until totalVelocityValues - 1) vlArray[j] = vlArray[j + 1]
                    vlArray[totalVelocityValues - 1] = v
                }
                mSampleCounter = 0
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return v
    }

    private fun calculateVelocity(aDelT1: Double): Double {
        val v = v0 + (aDelT1 - aDelT0)
        Log.d(TAG, "v = $v0+ ($aDelT1 - $aDelT0) = $v")
        v0 = v
        aDelT0 = aDelT1
        return v
    }

    companion object {
        // Singleton the value
        var itration = 0
    }
}