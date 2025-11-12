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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.components.DecorativeCornerDividers
import com.example.spoolsync.ui.theme.SpoolSyncTheme
import com.example.spoolsync.ui.viewModels.OcrViewModel
import kotlinx.coroutines.launch
import java.io.File
import kotlin.collections.remove

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    navController: NavController,
    ocrViewModel: OcrViewModel
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var scannedText by remember { mutableStateOf<String?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var previewWidth by remember { mutableStateOf(0) }
    var previewHeight by remember { mutableStateOf(0) }

    fun Bitmap.rotateRight90(): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(90f)
        return android.graphics.Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
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
            BottomAppBar(
                containerColor = SpoolSyncTheme.colors.lighterGrayDarkerGray
            ) {
                NavigationBar(
                    containerColor = Color.Transparent
                ) {
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("filaments") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_filament),
                                contentDescription = stringResource(R.string.filaments),
                                tint = colorResource(R.color.gray),
                                modifier = Modifier.size(48.dp),
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { navController.navigate("filamentNfcRead") },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = stringResource(R.string.info),
                                tint = colorResource(R.color.gray),
                                modifier = Modifier.size(32.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = {  },
                        icon = {
                            Icon(
                                painter = painterResource(R.drawable.ic_printer),
                                contentDescription = stringResource(R.string.print),
                                tint = SpoolSyncTheme.colors.blackWhite,
                                modifier = Modifier.size(48.dp)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .clipToBounds()
                        .border(2.dp, SpoolSyncTheme.colors.lightGrayWhite)
                        .onSizeChanged { size ->
                            previewWidth = size.width
                            previewHeight = size.height
                        }
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx).apply {
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                            }
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build()
                                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                                imageCapture = ImageCapture.Builder().build()
                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                cameraProvider.unbindAll()
                                cameraProvider.bindToLifecycle(
                                    ctx as LifecycleOwner, cameraSelector, preview, imageCapture
                                )
                            }, ContextCompat.getMainExecutor(ctx))
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(modifier = Modifier.size(100.dp, 60.dp).background(Color.Transparent).align(Alignment.Center)) {
                        DecorativeCornerDividers()
                    }
                }
            }

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

                                capturedBitmap = rotatedBitmap
                                coroutineScope.launch {
                                    val croppedBitmap = Bitmap.createBitmap(
                                        rotatedBitmap,
                                        startX,
                                        startY,
                                        cropWidth,
                                        cropHeight
                                    )

                                    val result = ocrViewModel.recognizeTextFromCroppedImage(croppedBitmap)
                                    scannedText = result

                                    navController.currentBackStackEntry
                                        ?.savedStateHandle
                                        ?.set("scannedWeight", result)
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
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Capture")
            }
        }
    }
}