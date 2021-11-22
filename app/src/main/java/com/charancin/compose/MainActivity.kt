package com.charancin.compose

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.charancin.compose.ui.theme.ComposeTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
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
            BuildScaffold()
        }
    }

    @Composable
    fun BuildScaffold() {
        val viewModel = viewModel<MainViewModel>()
        val focusManager = LocalFocusManager.current
        val scaffoldState = rememberScaffoldState()
        val (url, setUrl) = rememberSaveable {
            mutableStateOf("https://m.naver.com")
        }
        val context = LocalContext.current
        val webView = remember {
            WebView(context).apply {
                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()
                loadUrl("https://m.naver.com")
            }
        }

        // Composable 안에서 코루틴 스코프 사용 시 주의.
//        val scope = rememberCoroutineScope()
        LaunchedEffect(Unit) {
            viewModel.undoFlow.collectLatest {
                if (webView.canGoBack()) {
                    webView.goBack()
                } else {
                    scaffoldState.snackbarHostState.showSnackbar("마지막")
                }
            }
        }

        LaunchedEffect(Unit) {
            viewModel.redoFlow.collectLatest {
                if (webView.canGoForward()) {
                    webView.canGoForward()
                } else {
                    scaffoldState.snackbarHostState.showSnackbar("첫번째")
                }
            }
        }

        Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                            title = { Text("Web app") },
                            actions = {
                                IconButton(
                                        onClick = {
                                            viewModel.undo()
                                            // todo set back arrow action.
                                        }
                                ) {
                                    Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "back",
                                            tint = Color.White,
                                    )
                                }
                                IconButton(
                                        onClick = {
                                            viewModel.redo()
                                            // todo set forward arrow action.
                                        }
                                ) {
                                    Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = "forward",
                                            tint = Color.White,
                                    )
                                }
                            }
                    )
                }
        ) {
            //  todo set webview
            Column(modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = url,
                        onValueChange = setUrl,
                        label = { Text(text = "https://") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            focusManager.clearFocus()
                            viewModel.url.value = url
                        })
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Composable view 가 아닌 일반 android 에서 제공하는 View 를 사용하는 방법.
                AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = {
                            // context 가 들고오고, Android view 에 context 를 제공 해줘야한다.
                            // Android web view.
                            webView
                        },
                        // 코루틴 스코프을 제공하지 않아 별도 값 처리.
                        update = { webView ->
                            webView.loadUrl(viewModel.url.value)
                            // 화면 갱신 부분. composition 이 다시 실행 될 때는 update 발생 함.
                            // 비워 두게 되면 factory 따라 감.
                            // 반복적으로 실행되는 오류가 있음.
//                            scope.launch {
//                                viewModel.undoFlow.collectLatest {
//                                    if (webView.canGoBack()) {
//                                        webView.goBack()
//                                    } else {
//                                        scaffoldState.snackbarHostState.showSnackbar("마지막")
//                                    }
//                                }
//                            }
//                            scope.launch {
//                                viewModel.redoFlow.collectLatest {
//                                    if (webView.canGoForward()) {
//                                        webView.canGoForward()
//                                    } else {
//                                        scaffoldState.snackbarHostState.showSnackbar("첫번째")
//                                    }
//                                }
//                            }
                        }
                )
            }
        }
    }
}

class MainViewModel : ViewModel() {
    val url = mutableStateOf("https://m.naver.com")

    private val _undoFlow = MutableSharedFlow<Boolean>()

    val undoFlow = _undoFlow.asSharedFlow()

    private val _redoFlow = MutableSharedFlow<Boolean>()

    val redoFlow = _redoFlow.asSharedFlow()


    fun undo() {
        viewModelScope.launch {
            _undoFlow.emit(true)
        }
    }

    fun redo() {
        viewModelScope.launch {
            _redoFlow.emit(true)
        }
    }
}