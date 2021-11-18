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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.charancin.compose.ui.theme.ComposeTheme

private const val FIRST = "first"
private const val SECOND = "second"
private const val THIRD = "third"

class MainActivity : ComponentActivity() {

//    private val viewModel by viewModels<MainViewModel>()

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
    fun BuildScaffold() {
        val navigationCtrl = rememberNavController()
        Scaffold {
            NavHost(navController = navigationCtrl, startDestination = FIRST) {
                composable(FIRST) {
                    BuildFirstScreen(navigationCtrl)
                }
                composable(SECOND) {
                    BuildSecondScreen(navigationCtrl)
                }
                composable("$THIRD/{value}") {
                    BuildThirdScreen(navigationCtrl, it.arguments?.getString("value") ?: "")
                }
            }
        }
    }

    @Composable
    fun BuildFirstScreen(ctrl: NavController) {
        // 코틀린 구조 분해.
        val (value, setValue) = remember {
            mutableStateOf("")
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "This is first screen")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = {
                ctrl.navigate(SECOND)
            }) {
                Text(text = "Move to second screen")
            }
            Spacer(modifier = Modifier.height(24.dp))
            TextField(
                value = value,
                onValueChange = setValue,
                placeholder = { Text(text = "input password") })
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (value == "8878") ctrl.navigate("$THIRD/$value")
                // todo add snack bar.
            }) {
                Text(text = "Move to third screen")
            }


        }
    }

    @Composable
    fun BuildSecondScreen(ctrl: NavController) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "This is second screen")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                ctrl.popBackStack()
            }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "arrow_back")
            }
        }
    }

    @Composable
    fun BuildThirdScreen(ctrl: NavController, password: String) {
        val viewModel = viewModel<MainViewModel>()
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "This is third screen")
            Text(text = viewModel.text.value)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                ctrl.navigateUp()
            }) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "arrow_back")
            }
            Button(onClick = {
                viewModel.changeText()
            }) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "change_value")
            }
        }
    }
}

//
class MainViewModel : ViewModel() {
    private val _text = mutableStateOf("check it!")
    val text: State<String> = _text

    fun changeText() {
        _text.value = if (_text.value == "check it!") "good!" else "check it!"
    }
}