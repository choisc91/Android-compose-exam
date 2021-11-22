package com.charancin.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.charancin.compose.ui.theme.ComposeTheme
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.pow

class MainActivity : ComponentActivity() {

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        ComposeTheme {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = viewModel<MainViewModel>()
            Scaffold(topBar = {
                TopAppBar(title = { Text(text = "Timer") })
            }) {
                Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(80.dp))
                    // timer text.
                    Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                                text = "${viewModel.second.value}",
                                style = TextStyle(fontSize = 80.sp, fontWeight = FontWeight.Bold),
                        )
                        Text(
                                text = ":",
                                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                                text = String.format("%02d", viewModel.milli.value),
                                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                    // rap time.
                    LazyColumn(
                            modifier = Modifier.weight(1f)
                    ) {
                        // todo, items {} 를 사용하게 되면 값이 여러번 찍히는 문제 점이 있음, 확인 바람.
                        item {
                            viewModel.lapTime.value.forEach {
                                Text(it)
                            }
                        }

                    }

                    // controller.
                    Row(
                            modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        FloatingActionButton(
                                onClick = {
                                    viewModel.reset()
                                },
                                backgroundColor = Color.Red,
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "reset", tint = Color.White)
                        }
                        FloatingActionButton(
                                onClick = {
                                    val isRunning = viewModel.isRunning.value
                                    if (isRunning) viewModel.pause() else viewModel.start()
                                },
                        ) {
                            val isRunning = viewModel.isRunning.value
                            Icon(
                                    imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                                    contentDescription = if (isRunning) "pause" else "start"
                            )
                        }
                        FloatingActionButton(
                                onClick = {
                                    viewModel.record()
                                },
                                backgroundColor = Color.Blue,
                        ) {
                            Icon(imageVector = Icons.Default.Create, contentDescription = "record", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

class MainViewModel : ViewModel() {

    private var time = 0

    private var timeTask: Timer? = null

    private val _lapTimes = mutableStateOf(mutableListOf<String>())

    val lapTime: State<List<String>> = _lapTimes

    private val _second = mutableStateOf(0)
    val second: State<Int> = _second

    private val _milli = mutableStateOf(0)
    val milli: State<Int> = _milli

    private val _isRunning = mutableStateOf(false)
    val isRunning: State<Boolean> = _isRunning

    fun start() {
        _isRunning.value = true
        timeTask = timer(period = 10) {
            time++
            _second.value = time / 100
            _milli.value = time % 100
        }
    }

    fun pause() {
        _isRunning.value = false
        timeTask?.cancel()
    }

    fun reset() {
        timeTask?.cancel()
        time = 0
        _isRunning.value = false
        _second.value = 0
        _milli.value = 0
        _lapTimes.value.clear()
    }

    fun record() {
        _lapTimes.value.add("#${_lapTimes.value.size}RAP ${_second.value}:${_milli.value}")
    }
}