package com.example.spoolsync.ui.viewModels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * ViewModel pre rozpoznávanie textu z obrázkov pomocou ML Kit OCR.
 *
 * @param application Kontext aplikácie potrebný pre AndroidViewModel.
 */
class OcrViewModel(
    application: Application
) : AndroidViewModel(application) {

    /**
     * Rozpozná text z orezaného stredu obrázka načítaného z Uri.
     * Orezáva stred obrázka na veľkosť 100x60 (alebo menej podľa veľkosti obrázka) a použije ML Kit na rozpoznanie textu.
     *
     * @param context Kontext na prístup k content resolveru.
     * @param imageUri Uri obrázka, z ktorého sa má rozpoznať text.
     * @return Rozpoznaný text alebo null, ak sa obrázok nepodarilo načítať.
     */
    suspend fun recognizeTextFromCroppedImage(
        context: Context,
        imageUri: Uri
    ): String? {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val originalBitmap = inputStream?.use { BitmapFactory.decodeStream(it) } ?: return null
        val cropWidth = minOf(100, originalBitmap.width)
        val cropHeight = minOf(60, originalBitmap.height)
        val startX = (originalBitmap.width - cropWidth) / 2
        val startY = (originalBitmap.height - cropHeight) / 2

        val croppedBitmap = Bitmap.createBitmap(
            originalBitmap,
            startX,
            startY,
            cropWidth,
            cropHeight
        )

        val image = InputImage.fromBitmap(croppedBitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val result = recognizer.process(image).await()
        return result.text
    }
}