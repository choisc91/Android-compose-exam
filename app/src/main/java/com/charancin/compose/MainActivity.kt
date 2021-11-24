package com.charancin.compose

import android.app.Application
import android.content.pm.ActivityInfo
import android.media.SoundPool
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // 화면 유지.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE    // 가로 고정.
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = viewModel<MainViewModel>()
            val keys = listOf(
                Pair("도", Color.Red),
                Pair("레", Color.Red),
                Pair("미", Color.Red),
                Pair("파", Color.Red),
                Pair("솔", Color.Red),
                Pair("라", Color.Red),
                Pair("시", Color.Red),
                Pair("도#", Color.Red),
            )
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,

                ) {
                keys.forEachIndexed { index, key ->
                    val padding = (index + 2) * 8
                    val modifier = Modifier
                        .padding(top = padding.dp, bottom = padding.dp)
                        .clickable {
                            viewModel.playSound(index)
                        }
                    Keyboard(modifier = modifier, color = key.second, text = key.first)
                }
            }
        }
    }
}

@Composable
fun Keyboard(modifier: Modifier, color: Color, text: String) {
    Box(
        modifier = modifier
            .width(48.dp)
            .fillMaxHeight()
            .background(color = color)
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            style = TextStyle(
                color = Color.White,
                fontSize = 24.sp,
            )
        )
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val soundPool = SoundPool.Builder().setMaxStreams(8).build()

    private val sounds = listOf(
        soundPool.load(application.applicationContext, R.raw.do1, 1),
        soundPool.load(application.applicationContext, R.raw.re, 1),
        soundPool.load(application.applicationContext, R.raw.mi, 1),
        soundPool.load(application.applicationContext, R.raw.fa, 1),
        soundPool.load(application.applicationContext, R.raw.sol, 1),
        soundPool.load(application.applicationContext, R.raw.la, 1),
        soundPool.load(application.applicationContext, R.raw.si, 1),
        soundPool.load(application.applicationContext, R.raw.do2, 1),
    )

    fun playSound(index: Int) {
        soundPool.play(sounds[index], 1f, 1f, 0, 0, 1f)
    }

    override fun onCleared() {
        soundPool.release()
        super.onCleared()
    }


}