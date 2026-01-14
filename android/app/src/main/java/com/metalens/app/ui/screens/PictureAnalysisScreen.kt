package com.metalens.app.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meta.wearable.dat.core.Wearables
import com.meta.wearable.dat.core.types.Permission
import com.meta.wearable.dat.core.types.PermissionStatus
import com.metalens.app.R
import com.metalens.app.wearables.LocalWearablesPermissionRequester
import com.metalens.app.wearables.WearablesViewModel
import kotlinx.coroutines.delay

@Composable
fun PictureAnalysisScreen(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
) {
    val activity = LocalContext.current as ComponentActivity
    val wearablesViewModel: WearablesViewModel = viewModel(activity)
    val permissionRequester = LocalWearablesPermissionRequester.current
    val uiState by wearablesViewModel.uiState.collectAsStateWithLifecycle()

    var countdown by remember { mutableIntStateOf(0) }
    var isCountingDown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Prepare camera session first (permission + STREAMING), then countdown will trigger capture.
        if (uiState.capturedPhoto == null && !uiState.isCapturingPhoto && !uiState.isPreparingPhotoSession && !uiState.isPhotoSessionReady) {
            val permission = Permission.CAMERA
            val statusResult = Wearables.checkPermissionStatus(permission)
            statusResult.onFailure { error, _ ->
                wearablesViewModel.setRecentError("Permission check error: ${error.description}")
            }
            val status = statusResult.getOrNull()
            val granted =
                when (status) {
                    PermissionStatus.Granted -> true
                    PermissionStatus.Denied -> permissionRequester.request(permission) == PermissionStatus.Granted
                    null -> false
                }

            if (granted) {
                wearablesViewModel.preparePhotoCaptureSession()
            } else {
                wearablesViewModel.setRecentError("Camera permission denied")
            }
        }
    }

    LaunchedEffect(uiState.isPhotoSessionReady, uiState.capturedPhoto) {
        // Start 3..2..1 countdown only once the session is ready and we don't have a photo yet.
        if (uiState.capturedPhoto != null) return@LaunchedEffect
        if (!uiState.isPhotoSessionReady) return@LaunchedEffect
        if (isCountingDown) return@LaunchedEffect

        isCountingDown = true
        for (i in 3 downTo 1) {
            countdown = i
            delay(1_000)
        }
        countdown = 0
        wearablesViewModel.capturePreparedPhoto()
        isCountingDown = false
    }

    Surface(modifier = modifier.fillMaxSize(), color = Color.Black) {
        Box(modifier = Modifier.fillMaxSize()) {
            val photo = uiState.capturedPhoto
            when {
                photo != null -> {
                    Image(
                        bitmap = photo.asImageBitmap(),
                        contentDescription = stringResource(R.string.picture_analysis),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                }
                uiState.isPreparingPhotoSession -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.picture_analysis_preparing),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                countdown > 0 -> {
                    // Big countdown overlay (3..2..1)
                    Text(
                        text = countdown.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
                uiState.isCapturingPhoto -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.picture_analysis_capturing),
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                else -> {
                    // Error / empty state
                    val msg = uiState.recentError ?: "No photo"
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier =
                            Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                    )
                }
            }

            // Bottom actions
            Row(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    enabled = !uiState.isPreparingPhotoSession && !uiState.isCapturingPhoto && countdown == 0,
                    onClick = {
                        // Reset and start again
                        countdown = 0
                        isCountingDown = false
                        wearablesViewModel.resetPictureAnalysis()
                        wearablesViewModel.preparePhotoCaptureSession()
                    },
                ) {
                    Text("Retake", color = Color.White)
                }

                TextButton(
                    onClick = {
                        countdown = 0
                        isCountingDown = false
                        wearablesViewModel.resetPictureAnalysis()
                        onClose()
                    },
                ) {
                    Text(stringResource(R.string.common_close), color = Color.White)
                }
            }
        }
    }
}

