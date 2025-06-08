package com.example.spoolsync.ui.viewModels

import android.app.Application
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import androidx.lifecycle.AndroidViewModel

/**
 * ViewModel pre prácu s NFC tagmi.
 * Umožňuje čítať a zapisovať údaje na NFC tagy pomocou technológie NDEF.
 *
 * @param application Kontext aplikácie potrebný pre AndroidViewModel.
 */
class NfcViewModel(
    application: Application
) : AndroidViewModel(application) {

    /**
     * Prečíta obsah NFC tagu a vráti ho cez callback.
     *
     * @param tag NFC tag, ktorý sa má prečítať.
     * @param error1 Chybová správa pre prípad zlyhania.
     * @param onNfcRead Callback s prečítaným obsahom tagu.
     * @param onError Callback s chybovou správou pri zlyhaní.
     */
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

    /**
     * Zapíše filamentId na NFC tag.
     *
     * @param filamentId Identifikátor filamentu, ktorý sa má zapísať.
     * @param tag NFC tag, na ktorý sa má zapisovať.
     * @param errors Zoznam chybových správ pre rôzne situácie.
     * @param onSuccess Callback pri úspešnom zápise.
     * @param onError Callback s chybovou správou pri zlyhaní.
     */
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