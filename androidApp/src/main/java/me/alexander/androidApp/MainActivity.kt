package me.alexander.androidApp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.juul.kable.Scanner
import me.alexander.shared.Greeting
import android.widget.TextView
import kotlinx.coroutines.flow.onEach

fun greet(): String {
    return Greeting().greeting()
}

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tv: TextView = findViewById(R.id.text_view)
        tv.text = greet()

            Scanner()
                .advertisements
                .onEach { Log.i("QQ", it.address) }
    }
}
