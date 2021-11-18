package com.charancin.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.charancin.compose.ui.theme.ComposeTheme

class MainActivity : ComponentActivity() {

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        ComposeTheme {
            BuildScaffold()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BuildScaffold()
        }
    }

    @Composable
    fun BuildScaffold(viewModel: MainViewModel = viewModel()) {
        // 기본 용법.
        // mutableState 객체 이기 때문에, 직접적으로 접근이 불가능.
        val text1: MutableState<String> = remember {
            mutableStateOf("Hello world!")
        }

        // 간단하게.
        // delegate property kotlin getter setter.
        // by 구문을 적음 으로서 자체적으로 getter 와 setter 를 정의 해줌.
        var text2: String by remember {
            mutableStateOf("Hello world!")
        }

        // 세밀한 구조.
        // operator component1, component2 를 정의.
        val (getValue: String, setValue: (String) -> Unit) = remember {
            mutableStateOf("Hello world!")
        }

        // livedata  를 compose state 로 사용하기.
        val text3: State<String?> = viewModel.liveData.observeAsState("Hello world!")

        Scaffold {
            Column {
                Text(text = "Hello world!")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /*TODO*/ }) {
                    Text(text = "click me")
                }

                TextField(value = getValue, onValueChange = setValue)
            }
        }
    }
}

class MainViewModel : ViewModel() {
    private val _value: MutableState<String> = mutableStateOf("Hello world!")

    // state 객체, 읽기 전용.
    val value: State<String> = _value

    private val _liveData = MutableLiveData<String>()

    val liveData: LiveData<String> = _liveData

    // change value
    fun changeValue(x: String) {
        _value.value = x
    }
}
