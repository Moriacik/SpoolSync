package com.example.spoolsync.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spoolsync.R
import com.example.spoolsync.ui.components.QRScannerFrame
import com.example.spoolsync.ui.viewModels.StatisticsViewModel
import org.json.JSONObject
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    viewModel: StatisticsViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    var cameraPermissionGranted by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }

    val onQRScanned: (String) -> Unit = { qrData ->
        if (isScanning) {
            isScanning = false

            // Parse QR data a povolaj viewModel
            try {
                val jsonData = JSONObject(qrData)
                viewModel.confirmQRSession(
                    sessionId = jsonData.optString("sessionId", ""),
                    requestToken = jsonData.optString("requestToken", "")
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        cameraPermissionGranted = isGranted
        if (!isGranted) {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(Unit) {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        cameraPermissionGranted = permission == PackageManager.PERMISSION_GRANTED
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            // Po úspešnom potvrdení sa naviguje na AccountScreen
            navController.navigate("account") {
                popUpTo("statistics") { inclusive = false }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.export_statistics), fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Späť"
                        )
                    }
                },
                actions = {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (cameraPermissionGranted) {
                // Live camera preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .aspectRatio(1f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        QRScannerFrame(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            CameraPreview(
                                modifier = Modifier.fillMaxSize(),
                                onQRScanned = onQRScanned,
                                isScanning = isScanning
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.scan_qr_code),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                Text(
                    text = "Naskenujte QR kód zo webovej stránky\nna povolenie prístupu k štatistikám",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = colorResource(R.color.gray),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "📷 Povolenie kamery",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Text(
                            "Aplikácia potrebuje prístup k kamere na skenovanie QR kódov",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        ) {
                            Text("Udeliť povolenie")
                        }
                    }
                }
            }

            // Error message
            errorMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.red).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "❌ $it",
                        color = colorResource(R.color.red),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Success message
            successMessage?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = colorResource(R.color.green).copy(alpha = 0.1f)
                    )
                ) {
                    Text(
                        text = "✅ $it",
                        color = colorResource(R.color.green),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Povolenie kamery") },
            text = { Text("Aplikácia potrebuje prístup k kamere. Prosím, povolte prístup v nastaveniach.") },
            confirmButton = {
                Button(onClick = { showPermissionDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onQRScanned: (String) -> Unit = {},
    isScanning: Boolean = true
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var lastScannedCode by remember { mutableStateOf<String?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider

                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(surfaceProvider)
                    }

                    // ML Kit Barcode Scanner setup
                    val scanner = try {
                        val options = com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
                            .setBarcodeFormats(256) // QR_CODE format
                            .build()
                        com.google.mlkit.vision.barcode.BarcodeScanning.getClient(options)
                    } catch (exception: Exception) {
                        null
                    }

                    if (scanner != null) {
                        // ImageAnalysis for QR code detection
                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        setupQRAnalyzer(imageAnalyzer, scanner, cameraExecutor, isScanning, lastScannedCode, onQRScanned)

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalyzer
                            )
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        }
                    } else {
                        // Fallback na regular preview bez ML Kit
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (exception: Exception) {
                            exception.printStackTrace()
                        }
                    }
                }, ContextCompat.getMainExecutor(ctx))
            }
        },
        modifier = modifier
    )

    // Proper cleanup when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            try {
                // Unbind all camera use cases
                cameraProvider?.unbindAll()
            } catch (exception: Exception) {
                exception.printStackTrace()
            }

            // Shutdown executor
            cameraExecutor.shutdown()
        }
    }
}

@Suppress("UNCHECKED_CAST")
@OptIn(markerClass = [androidx.camera.core.ExperimentalGetImage::class])
private fun setupQRAnalyzer(
    imageAnalyzer: ImageAnalysis,
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    cameraExecutor: java.util.concurrent.Executor,
    isScanning: Boolean,
    lastScannedCode: String?,
    onQRScanned: (String) -> Unit
) {
    imageAnalyzer.setAnalyzer(cameraExecutor) { imageProxy ->
        if (isScanning) {
            try {
                val mediaImage = getImageFromProxy(imageProxy)
                if (mediaImage != null) {
                    val image = com.google.mlkit.vision.common.InputImage.fromMediaImage(
                        mediaImage,
                        imageProxy.imageInfo.rotationDegrees
                    )
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            if (barcodes.isNotEmpty()) {
                                for (barcode in barcodes) {
                                    val rawValue = barcode.rawValue
                                    if (!rawValue.isNullOrEmpty()) {
                                        if (rawValue != lastScannedCode) {
                                            onQRScanned(rawValue)
                                        }
                                        break
                                    }
                                }
                            }
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            } catch (exception: Exception) {
                imageProxy.close()
            }
        } else {
            imageProxy.close()
        }
    }
}

@OptIn(markerClass = [androidx.camera.core.ExperimentalGetImage::class])
private fun getImageFromProxy(imageProxy: androidx.camera.core.ImageProxy): android.media.Image? {
    return imageProxy.image
}


