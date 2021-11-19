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
                        Text(text = "${viewModel.second.value}")
                        Text(text = ":")
                        Text(text = "${viewModel.milli.value}")
                    }
                    Spacer(modifier = Modifier.height(80.dp))
                    // rap time.
                    // todo 래코드 타임이 중복해서 찍히는 문제가 있음, 해결 바람.
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(count = viewModel.lapTime.value.size) {
                            println(viewModel.lapTime.value.size)
                            viewModel.lapTime.value.forEach {
                                Text(it)
                            }
//                            for (record in viewModel.lapTime.value) Text(record)
                        }
                    }
                    // controller.
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        FloatingActionButton(onClick = {
                            viewModel.reset()
                        }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = "reset")
                        }
                        FloatingActionButton(onClick = {
                            val isRunning = viewModel.isRunning.value
                            if (isRunning) viewModel.pause() else viewModel.start()
                        }) {
                            val isRunning = viewModel.isRunning.value
                            Icon(
                                imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = if (isRunning) "pause" else "start"
                            )
                        }
                        FloatingActionButton(onClick = {
                            viewModel.record()
                        }) {
                            Icon(imageVector = Icons.Default.Create, contentDescription = "record")
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