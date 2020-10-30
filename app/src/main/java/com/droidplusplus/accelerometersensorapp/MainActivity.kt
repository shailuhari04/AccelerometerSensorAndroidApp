package com.droidplusplus.accelerometersensorapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.droidplusplus.accelerometersensorapp.simple.SimpleActivity
import com.droidplusplus.accelerometersensorapp.withgraph.WithGraphActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        handleClickEvent()
    }

    private fun handleClickEvent() {
        //simple btn click action handle
        simpleBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    SimpleActivity::class.java
                )
            )
        }

        //with Graph btn click action handle
        withGraphBtn.setOnClickListener {
            startActivity(
                Intent(
                    this@MainActivity,
                    WithGraphActivity::class.java
                )
            )
        }
    }

}