package com.example.spoolsync.nfc

import android.nfc.Tag
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton pre správu NFC tagov získaných z Intent-u.
 * Slúži ako "event bus" medzi MainActivity (ktorá spracúva Intent)
 * a NFC Screen (ktorá počúva na nový tag).
 * 
 * Umožňuje aj signalizovanie, že aplikácia by mala navigovať na NFC screen.
 */
object NfcTagManager {
    private const val TAG = "NfcTagManager"
    
    private val _nfcTag = MutableStateFlow<Tag?>(null)
    val nfcTag: StateFlow<Tag?> = _nfcTag

    // Signal na navigáciu (keď obdrží tag z Intent-u, signalizuje aplikácii aby otvorila NFC screen)
    private val _shouldNavigateToNfc = MutableStateFlow(false)
    val shouldNavigateToNfc: StateFlow<Boolean> = _shouldNavigateToNfc

    // Režim NFC (READ, UPDATE, OCR) - potrebný na rozhodnutie či navigovať
    private val _nfcMode = MutableStateFlow<String?>(null)
    val nfcMode: StateFlow<String?> = _nfcMode

    fun setTag(tag: Tag?, navigateToNfc: Boolean = false, mode: String? = null) {
        Log.d(TAG, "NFC tag received: ${tag?.id?.contentToString() ?: "null"}, navigateToNfc=$navigateToNfc, mode=$mode")
        _nfcTag.value = tag
        _nfcMode.value = mode
        if (navigateToNfc) {
            _shouldNavigateToNfc.value = true
        }
    }

    fun clearTag() {
        Log.d(TAG, "NFC tag cleared")
        _nfcTag.value = null
        _nfcMode.value = null
    }

    fun getAndClearTag(): Tag? {
        val tag = _nfcTag.value
        clearTag()
        return tag
    }

    fun clearNavigationSignal() {
        Log.d(TAG, "Navigation signal cleared")
        _shouldNavigateToNfc.value = false
    }
}

