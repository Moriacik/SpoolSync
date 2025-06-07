package com.example.spoolsync.ui.viewModels

import android.app.Application
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.lifecycle.AndroidViewModel

class NfcViewModel(application: Application) : AndroidViewModel(application) {
    var nfcId: String? = null
    var errorMessage: String? = null

    fun readNfcTag(
        tag: Tag?,
        error1: String,
        onNfcRead: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        tag?.let {
            val ndef = Ndef.get(tag)
            try {
                ndef?.connect()
                val message = ndef?.ndefMessage
                val payload = message?.records?.get(0)?.payload
                if (payload != null) {
                    val languageCodeLength = payload[0].toInt() and 0x3F
                    val nfcId = String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charsets.UTF_8)
                    onNfcRead(nfcId)
                }
            } catch (e: Exception) {
                onError(error1 + e.message)
            } finally {
                ndef?.close()
            }
        }
    }

    fun updateNfcTag(
        filamentId: String,
        tag: Tag?,
        errors: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        tag?.let {
            try {
                val ndef = Ndef.get(tag)
                ndef?.connect()
                if (ndef != null) {
                    if (!ndef.isWritable) {
                        onError(errors[0])
                        return
                    }
                    val maxSize = ndef.maxSize
                    if (filamentId.length > maxSize) {
                        onError(errors[1])
                        return
                    }
                    val record = NdefRecord.createTextRecord("en", filamentId)
                    val message = NdefMessage(arrayOf(record))
                    ndef.writeNdefMessage(message)
                    onSuccess()
                } else {
                    onError(errors[2])
                }
            } catch (e: Exception) {
                onError(errors[3] + e.message)
            } finally {
                try {
                    Ndef.get(tag)?.close()
                } catch (_: Exception) { }
            }
        } ?: onError(errors[4])
    }
}