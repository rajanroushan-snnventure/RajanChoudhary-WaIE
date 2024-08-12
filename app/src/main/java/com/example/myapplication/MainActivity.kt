package com.example.myapplication.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.myapplication.NetworkUtils
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.util.Date


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    APODScreen()
                }
            }
        }
    }
}

@Composable
fun APODScreen(viewModelData: APODViewModel = viewModel()) {
    val context  = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }
    val title = viewModelData.title.observeAsState("Loading")
    val imageUrl = viewModelData.imageUrl.observeAsState("loading")
    val explanation = viewModelData.explanation.observeAsState("Loading..")
    val mediaType = viewModelData.mediaType.observeAsState()
    LaunchedEffect(viewModelData) {
        if (NetworkUtils.isNetworkAvailable(context)) {
            viewModelData.fetchAPOD(context)
            isLoading = false
        } else {
            viewModelData.fetchData(context)
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(50.dp)
                    .padding(bottom = 16.dp)
            )
        }
        else {
            if (title.value != "Loading") {
                viewModelData.setLastApiCallTime(Date().time, context)
                viewModelData.setLocalData(
                    context,
                    title.value,
                    imageUrl.value,
                    explanation.value,
                    mediaType.value.toString()
                )
            }
            // Once loading is complete, show the actual content
            Text(
                text = title.value,
                modifier = Modifier.padding(bottom = 16.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            if (imageUrl.value.isNotEmpty()) {
                if(mediaType.value == "video") {
                    ExoplayerVideo(imageUrl.value)
                } else {
                    LoadingImageFromInternet(imageUrl.value)
                }
            }
            Text(
                text = explanation.value,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        APODScreen()
    }
}

@Composable
fun LoadingImageFromInternet(imageUrl : String){
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .build(),
        contentDescription = "",
        modifier = Modifier
            .padding(4.dp)
            .size(150.dp)
            .clip(CircleShape),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun ExoplayerVideo(videoUrl: String) {
    val context = LocalContext.current
    val mediaItem = MediaItem.Builder()
        .setUri(videoUrl)
        .build()
    val exoPlayer = remember(context, mediaItem) {
        ExoPlayer.Builder(context)
            .build()
            .also { exoPlayer ->
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = false
                exoPlayer.repeatMode = Player.REPEAT_MODE_OFF
            }
    }

    Unit
    Unit
    DisposableEffect(
        Unit
    ) {
        onDispose { exoPlayer.release() }
    }


}
