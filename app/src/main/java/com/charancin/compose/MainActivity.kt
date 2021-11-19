package com.charancin.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlin.math.pow

class MainViewModel : ViewModel() {

    // 기존, 예제를 참고하며 만들때는 뷰모델에 height 와 weight 를 뷰모델이 가지게 구조를
    // 마음대로 만들었지만
    // 생각해보니 height 와 weight 는 한번만 쓰고 증발해버리는 단발성 value 라
    // 굳이 뷰모델이 가지고 있을까 의문이 들어 예제와 똑같이 만들고
    // composable 에서 콜백 형태로 넘겨주는 것 처럼 변경.

    private val _bmi = mutableStateOf(.0)

    val bmi = _bmi.value

    fun setBmi(height: Double, weight: Double) {
        _bmi.value = height / (weight / 100.0).pow(2.0)
    }
}

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
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "home", builder = {
                composable(route = "home") {
                    BuildHome { height, weight ->
                        viewModel.setBmi(height, weight)
                        // 여기서, 네비게이터 value 로 보내준다면 뷰모델 또한 필요가 없어짐.
                        // 계산식도 이쪽으로 넘기고.
                        navController.navigate("result")
                    }
                }
                composable(route = "result") {
                    // 여기서 bmi 를 바라보고 있지 않아도, 단발성 value 로 고칠 수있을 것 같음.
                    BuildResult(navController, viewModel.bmi)
                }
            })
        }
    }

    @Composable
    fun BuildHome(onResult: (Double, Double) -> Unit) {
        val (height, setHeight) = rememberSaveable {
            mutableStateOf("")
        }
        val (weight, setWeight) = rememberSaveable {
            mutableStateOf("")
        }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "비만도 계산기") }
                )
            }
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                OutlinedTextField(
                    value = height,
                    onValueChange = setHeight,
                    label = { Text(text = "키") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = weight,
                    singleLine = true,
                    onValueChange = setWeight,
                    label = { Text(text = "몸무게") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        if (height.isNotEmpty() && weight.isNotEmpty()) {
                            onResult(height.toDouble(), weight.toDouble())
                        }
                    },
                ) {
                    Text(text = "계산")
                }
            }
        }
    }

    @Composable
    fun BuildResult(navController: NavController, value: Double) {
        // 뭔가 계산식이 안맞는 것 같지만, 예제니까 패스.
        val text = when {
            value >= 35 -> "초고도 비만"
            value >= 30 -> "고도 비만"
            value >= 25 -> "중고도 비만"
            value >= 23 -> "비만"
            value >= 18.5 -> "정상"
            else -> "저체중"
        }

        val imageRes = when {
            value >= 23 -> R.drawable.ic_baseline_sentiment_very_dissatisfied_24
            value >= 18.5 -> R.drawable.ic_baseline_sentiment_very_satisfied_24
            else -> R.drawable.ic_baseline_sentiment_satisfied_alt_24
        }

        Scaffold(topBar = {
            TopAppBar(
                title = { Text(text = "결과") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigateUp()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "back",
                        )
                    }
                },
            )
        }) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = text, fontSize = 32.sp)
                Spacer(modifier = Modifier.height(48.dp))
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = "normal",
                    modifier = Modifier.size(80.dp),
                    colorFilter = ColorFilter.tint(color = Color.Black)
                )

            }
        }
    }
}