package com.charancin.compose

import android.Manifest
import android.app.Application
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.charancin.compose.ui.theme.ComposeTheme
import com.google.accompanist.pager.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.Appendable
import java.text.DecimalFormat
import java.util.*
import kotlin.concurrent.timer
import kotlin.math.absoluteValue
import kotlin.math.pow

class MainActivity : ComponentActivity() {

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        ComposeTheme {

        }
    }

    @ExperimentalPagerApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = viewModel<MainViewModel>()
            var isGranted by remember {
                mutableStateOf(false)
            }
            val launcher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                    isGranted = it
                }
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isGranted = true
            }

            if (isGranted) {
                viewModel.getPictures()
                PictureScreen(viewModel.pictureUris.value)
            } else {
                PermissionScreen {
                    launcher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }
}

// content provider, 사용 시 context 가 필요한데, viewModel 에서는 얻을 수가 없어
// Android view model 을 사용해 context 제공.
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _pictureUris = mutableStateOf(emptyList<Uri>())

    val pictureUris: State<List<Uri>> = _pictureUris

    fun getPictures() {
        val uris = mutableListOf<Uri>()
        getApplication<Application>().contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            "${MediaStore.Images.ImageColumns.DATE_ADDED} DESC",
        )?.use { cursor ->
            // 컬럼의 아이디의 인덱스.
            val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)

            // cursor 안에 있는 객체 순환.
            while (cursor.moveToNext()) {
                // 아이디.
                val id = cursor.getLong(idIndex)
                val contentUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                uris.add(contentUri)
            }
        }
        _pictureUris.value = uris
    }
}

@ExperimentalPagerApi
@Composable
fun PictureScreen(pictures: List<Uri>) {
    // 시범 기능이라 오류 남.
    val pagerState = rememberPagerState()
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            count = pictures.size,
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .fillMaxSize()
        ) { index ->
            Card(
                modifier = Modifier
                    .graphicsLayer {
                        // 화면이 그려질때의 애니메이션을 구현할 수 있음.
                        val pageOffset = calculateCurrentOffsetForPage(index).absoluteValue
                        lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        ).also {
                            scaleX = it
                            scaleY = it
                        }
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
            ) {
                Image(
                    painter = rememberImagePainter(data = pictures[index]),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
        )
    }
}

@Composable
fun PermissionScreen(onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "사진첩에 접근하기 위한 권한이 필요합니다")
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick
        ) {
            Text(text = "권한 요청")
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    (1 - fraction) * start + fraction * stop