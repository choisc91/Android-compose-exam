package com.charancin.compose

import android.app.Application
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charancin.compose.ui.theme.ComposeTheme

class MainActivity : ComponentActivity() {

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        ComposeTheme {

        }
    }

    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // 화면 유지.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE    // 가로 고정.
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(viewModel)
        setContent {
//            val viewModel = viewModel<MainViewModel>()
            TiltScreen(
                x = viewModel.x.value, y = viewModel.y.value
            )
        }
    }
}

@Composable
fun TiltScreen(x: Float, y: Float) {
    val yCoord = x * 20
    val xCoord = y * 20
    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        drawCircle(
            color = Color.Black,
            radius = 100f,
            center = Offset(centerX, centerY),
            style = Stroke(),
        )
        drawCircle(
            color = Color.Green,
            radius = 100f,
            center = Offset(xCoord + centerX, yCoord + centerY),
        )
        drawLine(
            color = Color.Black,
            start = Offset(centerX - 20, centerY),
            end = Offset(centerX + 20, centerY)
        )
        drawLine(
            color = Color.Black,
            start = Offset(centerX, centerY - 20),
            end = Offset(centerX, centerY + 20)
        )
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application),
    LifecycleEventObserver,
    SensorEventListener {

    private val _x = mutableStateOf(0f)

    val x: State<Float> = _x

    private val _y = mutableStateOf(0f)

    val y: State<Float> = _y


    // 늦은 초기화 기법 사용.
    private val sensorManager by lazy {
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun registerSensor() {
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL,
        )
    }

    private fun unregisterSensor() {
        sensorManager.unregisterListener(this)
    }


    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            _x.value = event.values[0]
            _y.value = event.values[1]
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            registerSensor()
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            unregisterSensor()
        }
    }
}