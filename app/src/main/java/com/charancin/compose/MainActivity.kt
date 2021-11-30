package com.charancin.compose

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Colors
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.charancin.compose.ui.theme.ComposeTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MainActivity : ComponentActivity() {

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        ComposeTheme {

        }
    }

    // 앱 실행시 화면 유지.
    private fun keepScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    // 가로 고정.
    private fun setOrientation(mode: Int) {
//        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
//        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        requestedOrientation = mode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        keepScreen()
//        setOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
        super.onCreate(savedInstanceState)
        setContent {
            var granted by remember {
                mutableStateOf(false)
            }
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { isGranted ->
                    granted = isGranted
                }
            )
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                granted = true
            }
            if (granted) {
                val viewModel = viewModel<MainViewModel>()
                lifecycle.addObserver(viewModel)
                BuildMapView(viewModel)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "권한이 허용되지 않았습니다")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }) {
                        Text(text = "권한 허용")
                    }
                }
            }
        }
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application),
    LifecycleEventObserver {
    private val client =
        FusedLocationProviderClient(application.applicationContext)

    private val locationRequest: LocationRequest

    private val locationCallback: MyLocationCallback

    private val _state = mutableStateOf(
        MapState(null, PolylineOptions().width(5f).color(Color.RED))
    )

    val state: State<MapState> = _state

    init {
        locationCallback = MyLocationCallback()
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10000    // 최대 10초, 위치 변경이 없을 시에는 동작하지 않음.
        locationRequest.fastestInterval = 5000  // 최소 5초마다 갱신 함.
    }

    inner class MyLocationCallback : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val current = locationResult.lastLocation // 마지막으로 수신 받은 위치.

            val polyLine = state.value.polylineOptions
            _state.value = state.value.copy(
                location = current,
                polylineOptions = polyLine.add(LatLng(current.latitude, current.longitude))
            )
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            client.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else if (event == Lifecycle.Event.ON_PAUSE) {
            client.removeLocationUpdates(locationCallback)
        }
    }
}

data class MapState(
    val location: Location?,
    val polylineOptions: PolylineOptions,
)

@Composable
fun BuildMapView(viewModel: MainViewModel) {
    val mapView = rememberMapView()
    val state = viewModel.state.value

    AndroidView(
        factory = { mapView },
        update = {
            mapView.getMapAsync { googleMap ->
                state.location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17f))
                    googleMap.addPolyline(state.polylineOptions)
                }
            }
        }
    )
}

@Composable
fun rememberMapView(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        // 라이프사이클 감지.
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> throw IllegalStateException("error message")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return mapView
}