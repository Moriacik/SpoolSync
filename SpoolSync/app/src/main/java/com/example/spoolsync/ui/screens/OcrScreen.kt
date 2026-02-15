package com.example.spoolsync.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.components.BottomNavigationBar
import com.example.spoolsync.ui.components.CameraFrameOverlay
import com.example.spoolsync.ui.components.DecorativeCornerDividers
import com.example.spoolsync.ui.components.NavigationItem
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.OcrViewModel
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    navController: NavController,
    ocrViewModel: OcrViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Cleanup camera when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            try {
                cameraProvider?.unbindAll()
                cameraProvider = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun Bitmap.rotateRight90(): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(90f)
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }

    LaunchedEffect(Unit) {
        val filamentId = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("filamentId")

        val weight = navController.currentBackStackEntry
            ?.savedStateHandle
            ?.get<String>("scannedWeight")

        if (filamentId != null && weight != null) {
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("filamentId")
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("scannedWeight")
            navController.navigate("print/$filamentId/$weight") {
                popUpTo("filamentNfcRead") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.ic_printer),
                            contentDescription = stringResource(R.string.print),
                            Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(text = stringResource(R.string.print), fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                selectedItem = NavigationItem.PRINT
            )
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            CameraFrameOverlay(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                content = {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val provider = cameraProviderFuture.get()
                                cameraProvider = provider
                                val preview = Preview.Builder().build()
                                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                                imageCapture = ImageCapture.Builder().build()
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    lifecycleOwner, cameraSelector, preview, imageCapture
                                )
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val picturesDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
                    val file = File(picturesDir ?: context.cacheDir, "temp.jpg")
                    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()
                    imageCapture?.takePicture(
                        outputOptions,
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                val rotatedBitmap = bitmap.rotateRight90()

                                val cropWidthRatio = 0.27f
                                val cropHeightRatio = 0.10f

                                val cropWidth = (rotatedBitmap.width * cropWidthRatio).toInt()
                                val cropHeight = (rotatedBitmap.height * cropHeightRatio).toInt()
                                val startX = maxOf(0, (rotatedBitmap.width - cropWidth) / 2)
                                val startY = maxOf(0, (rotatedBitmap.height - cropHeight) / 2)

                                coroutineScope.launch {
                                    val croppedBitmap = Bitmap.createBitmap(
                                        rotatedBitmap,
                                        startX,
                                        startY,
                                        cropWidth,
                                        cropHeight
                                    )

                                    val result = ocrViewModel.recognizeTextFromCroppedImage(croppedBitmap)

                                    // Extract only numbers from OCR result (remove text like "g", "gramov", etc.)
                                    val extractedNumber = result.filter { it.isDigit() }.takeIf { it.isNotEmpty() } ?: "0"

                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("scannedWeight", extractedNumber)
                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("capturedBitmap", rotatedBitmap)

                                    navController.navigate("filamentNfcReadOcr") {
                                        popUpTo("filamentNfcReadOcr") { inclusive = true }
                                    }
                                }
                            }
                            override fun onError(exc: ImageCaptureException) {}
                        }
                    )
                },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SpoolSyncTheme.colors.lightGrayGray)
            ) {
                Text(stringResource(R.string.capture), color = colorResource(R.color.white))
            }
        }
    }
}