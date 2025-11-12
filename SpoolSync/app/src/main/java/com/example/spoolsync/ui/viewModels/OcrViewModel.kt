package com.example.spoolsync.ui.viewModels

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
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
     * Rozpozná text z orezaného obrázka.
     *
     * @param bitmap Bitmap obrázka, z ktorého sa má rozpoznať text.
     * @return Rozpoznaný text alebo prázdny reťazec v prípade chyby.
     */
    suspend fun recognizeTextFromCroppedImage(bitmap: Bitmap): String {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        return try {
            val firebaseVisionText = recognizer.process(inputImage).await()
            firebaseVisionText.text.trim()
        } catch (exception: Exception) {
            ""
        }
    }
}