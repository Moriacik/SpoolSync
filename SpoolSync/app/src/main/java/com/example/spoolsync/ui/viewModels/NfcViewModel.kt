package com.example.spoolsync.ui.viewModels

import android.app.Application
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.MifareUltralight
import android.util.Log
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
            try {
                Log.d("NfcViewModel", "Starting readNfcTag")

                // First attempt: Try standard NDEF if available
                val ndef = Ndef.get(tag)
                if (ndef != null) {
                    Log.d("NfcViewModel", "NDEF detected on tag")
                    try {
                        ndef.connect()
                        val ndefMessage = ndef.ndefMessage

                        if (ndefMessage != null && ndefMessage.records.isNotEmpty()) {
                            val record = ndefMessage.records[0]
                            val payload = record.payload

                            Log.d("NfcViewModel", "Record found - TNF: ${record.tnf}, Type: ${String(record.type)}, Payload size: ${payload?.size}")

                            if (payload != null && payload.size > 0) {
                                // Text record format: [status_byte, language_code, text...]
                                val statusByte = payload[0].toInt() and 0xFF
                                val languageLength = statusByte and 0x3F
                                val textStartIndex = 1 + languageLength

                                Log.d("NfcViewModel", "Status: 0x${statusByte.toString(16)}, Language length: $languageLength")

                                if (textStartIndex < payload.size) {
                                    val nfcId = String(payload, textStartIndex, payload.size - textStartIndex, Charsets.UTF_8)
                                    val trimmedId = nfcId.trimEnd('\u0000')
                                    Log.d("NfcViewModel", "Successfully read: $trimmedId")
                                    onNfcRead(trimmedId)
                                    return
                                }
                            }
                        }

                        Log.w("NfcViewModel", "Tag is empty or invalid format")
                        onError("$error1 - Tag is empty or has invalid data")
                        return

                    } catch (e: Exception) {
                        Log.e("NfcViewModel", "NDEF read error: ${e.message}", e)
                    } finally {
                        try {
                            ndef.close()
                        } catch (e: Exception) {
                            Log.w("NfcViewModel", "Failed to close NDEF")
                        }
                    }
                }

                // Second attempt: Try MifareUltralight direct read
                Log.d("NfcViewModel", "Attempting MifareUltralight direct read...")
                val mifare = MifareUltralight.get(tag)
                if (mifare != null) {
                    Log.d("NfcViewModel", "MifareUltralight detected, attempting direct read...")
                    try {
                        mifare.connect()

                        // Read pages 0-12 to get all data (4 bytes per page = 52 bytes total)
                        val allData = ByteArray(52)
                        var totalRead = 0

                        // Read in chunks (4 pages at a time due to max transceive length)
                        for (pageStart in 0..8 step 4) {
                            val pageCount = minOf(4, 13 - pageStart)
                            try {
                                val chunkData = mifare.readPages(pageStart)
                                if (chunkData != null && chunkData.size >= pageCount * 4) {
                                    System.arraycopy(chunkData, 0, allData, pageStart * 4, pageCount * 4)
                                    totalRead += pageCount * 4
                                    Log.d("NfcViewModel", "Read pages $pageStart-${pageStart + pageCount - 1}")
                                }
                            } catch (e: Exception) {
                                Log.w("NfcViewModel", "Failed to read pages $pageStart: ${e.message}")
                            }
                        }

                        Log.d("NfcViewModel", "Total data read: $totalRead bytes")
                        Log.d("NfcViewModel", "Raw data: ${allData.take(40).map { "%02X".format(it) }.joinToString(" ")}")

                        // Parse NDEF from raw data
                        // Look for NDEF TLV header (0xE1)
                        var ndefStartIndex = -1
                        for (i in allData.indices) {
                            if (allData[i] == 0xE1.toByte()) {
                                ndefStartIndex = i
                                Log.d("NfcViewModel", "Found NDEF TLV marker (0xE1) at index $i")
                                break
                            }
                        }

                        if (ndefStartIndex < 0) {
                            Log.e("NfcViewModel", "No NDEF TLV marker found")
                            onError("$error1 - Tag is not NDEF-formatted")
                            return
                        }

                        // Check format
                        if (ndefStartIndex + 2 >= allData.size) {
                            Log.e("NfcViewModel", "Not enough data after TLV marker")
                            onError("$error1 - Invalid NDEF structure")
                            return
                        }

                        // Next byte should be 0x10 (capability container) or the length
                        val nextByte = allData[ndefStartIndex + 1].toInt() and 0xFF
                        Log.d("NfcViewModel", "Byte after 0xE1: 0x${nextByte.toString(16)}")

                        val ndefLength: Int
                        val ndefDataStart: Int

                        if (nextByte == 0x10) {
                            // New format: [0xE1, 0x10, length, data...]
                            if (ndefStartIndex + 3 >= allData.size) {
                                Log.e("NfcViewModel", "Not enough data for new format")
                                onError("$error1 - Invalid NDEF structure")
                                return
                            }
                            ndefLength = allData[ndefStartIndex + 2].toInt() and 0xFF
                            ndefDataStart = ndefStartIndex + 3
                            Log.d("NfcViewModel", "Using new format: length=$ndefLength")
                        } else {
                            // Old format: [0xE1, length, data...]
                            ndefLength = nextByte
                            ndefDataStart = ndefStartIndex + 2
                            Log.d("NfcViewModel", "Using old format: length=$ndefLength")
                        }

                        if (ndefDataStart + ndefLength > allData.size) {
                            Log.e("NfcViewModel", "NDEF data extends beyond buffer")
                            onError("$error1 - Invalid NDEF structure")
                            return
                        }

                        // Extract NDEF message bytes
                        val ndefMessageBytes = ByteArray(ndefLength)
                        System.arraycopy(allData, ndefDataStart, ndefMessageBytes, 0, ndefLength)

                        Log.d("NfcViewModel", "NDEF message bytes: ${ndefMessageBytes.map { "%02X".format(it) }.joinToString(" ")}")

                        try {
                            // Parse NDEF message
                            val ndefMessage = NdefMessage(ndefMessageBytes)
                            if (ndefMessage.records.isNotEmpty()) {
                                val record = ndefMessage.records[0]
                                val payload = record.payload

                                if (payload != null && payload.size > 0) {
                                    // Text record format: [status_byte, language_code, text...]
                                    val statusByte = payload[0].toInt() and 0xFF
                                    val languageLength = statusByte and 0x3F
                                    val textStartIndex = 1 + languageLength

                                    if (textStartIndex < payload.size) {
                                        val nfcId = String(payload, textStartIndex, payload.size - textStartIndex, Charsets.UTF_8)
                                        val trimmedId = nfcId.trimEnd('\u0000')
                                        Log.d("NfcViewModel", "Successfully read: $trimmedId")
                                        onNfcRead(trimmedId)
                                        return
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("NfcViewModel", "Failed to parse NDEF message: ${e.message}", e)
                            onError("$error1 - Failed to parse tag data")
                            return
                        }

                        Log.w("NfcViewModel", "No valid data found in NDEF message")
                        onError("$error1 - Tag is empty")
                        return

                    } catch (e: Exception) {
                        Log.e("NfcViewModel", "MifareUltralight read error: ${e.message}", e)
                    } finally {
                        try {
                            mifare.close()
                        } catch (e: Exception) {
                            Log.w("NfcViewModel", "Failed to close MifareUltralight")
                        }
                    }
                }

                Log.w("NfcViewModel", "NDEF not available - tag may not be formatted")
                onError("$error1 - Tag is not NDEF-formatted. Please format it with NFC Tag Writer app first.")

            } catch (e: Exception) {
                Log.e("NfcViewModel", "Error during readNfcTag: ${e.message}", e)
                onError("$error1 - ${e.message}")
            }
        } ?: onError(error1)
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
                Log.d("NfcViewModel", "Starting updateNfcTag for filamentId: $filamentId")
                Log.d("NfcViewModel", "Tag technologies available: ${tag.techList?.joinToString(", ") ?: "none"}")

                // First attempt: Try NDEF if already formatted
                val ndef = Ndef.get(tag)
                if (ndef != null) {
                    Log.d("NfcViewModel", "NDEF detected on tag")
                    try {
                        ndef.connect()
                        if (ndef.isWritable) {
                            Log.d("NfcViewModel", "NDEF tag is writable, attempting write...")
                            val maxSize = ndef.maxSize
                            if (filamentId.length > maxSize) {
                                Log.e("NfcViewModel", "FilamentId ($filamentId) exceeds max size ($maxSize)")
                                onError(errors.getOrElse(1) { "FilamentId is too long for this tag" })
                                return
                            }
                            val record = NdefRecord.createTextRecord("en", filamentId)
                            val message = NdefMessage(arrayOf(record))
                            ndef.writeNdefMessage(message)
                            Log.d("NfcViewModel", "Successfully wrote to NDEF tag: $filamentId")
                            onSuccess()
                            return
                        } else {
                            Log.w("NfcViewModel", "NDEF tag is not writable, will try NdefFormatable...")
                        }
                    } catch (e: Exception) {
                        Log.w("NfcViewModel", "NDEF write failed: ${e.message}", e)
                    } finally {
                        try {
                            ndef.close()
                        } catch (e: Exception) {
                            Log.w("NfcViewModel", "Failed to close NDEF: ${e.message}")
                        }
                    }
                } else {
                    Log.d("NfcViewModel", "NDEF not directly available, trying NdefFormatable...")
                }

                // Second attempt: Try NdefFormatable to format/reformat the tag (PRIORITY for Ultralight C)
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    Log.d("NfcViewModel", "NdefFormatable detected, attempting to format tag...")
                    try {
                        ndefFormatable.connect()
                        Log.d("NfcViewModel", "Connected to NdefFormatable, formatting tag as NDEF...")
                        val record = NdefRecord.createTextRecord("en", filamentId)
                        val message = NdefMessage(arrayOf(record))
                        ndefFormatable.format(message)
                        Log.d("NfcViewModel", "Successfully formatted and wrote to tag: $filamentId")
                        onSuccess()
                        return
                    } catch (e: Exception) {
                        Log.w("NfcViewModel", "NdefFormatable format failed: ${e.message}", e)
                    } finally {
                        try {
                            ndefFormatable.close()
                        } catch (e: Exception) {
                            Log.w("NfcViewModel", "Failed to close NdefFormatable: ${e.message}")
                        }
                    }
                } else {
                    Log.d("NfcViewModel", "NdefFormatable not available")
                }

                // Third attempt: For Mifare Ultralight, try direct write via MifareUltralight class
                Log.d("NfcViewModel", "Attempting Mifare Ultralight direct write via MifareUltralight...")
                val mifare = MifareUltralight.get(tag)
                if (mifare != null) {
                    Log.d("NfcViewModel", "MifareUltralight detected, attempting write...")
                    try {
                        mifare.connect()

                        // Create NDEF formatted message
                        val record = NdefRecord.createTextRecord("en", filamentId)
                        val message = NdefMessage(arrayOf(record))
                        val ndefBytes = message.toByteArray()

                        Log.d("NfcViewModel", "NDEF message created: ${ndefBytes.size} bytes")

                        // Mifare Ultralight C: Build NDEF structure with CC and TLV
                        // CC (Capability Container) pages: 3 pages starting at page 2
                        // Pages 0-3: UID and config info (read-only)
                        // Pages 4+: User data area
                        
                        // Write NDEF CC to page 2 (Mifare Ultralight format)
                        // CC format: [0xE1, 0x10, max_ndef_size, 0x00]
                        // 0xE1 = NDEF message indicator
                        // 0x10 = Size = 16 bytes
                        // 0x3F = Max NDEF size (63 bytes)
                        // 0x00 = Terminator TLV
                        
                        val ccPage = ByteArray(4)
                        ccPage[0] = 0xE1.toByte()  // NDEF indicator
                        ccPage[1] = 0x10.toByte()  // NDEF CC
                        ccPage[2] = 0x3F.toByte()  // Max size (63 bytes for Ultralight C)
                        ccPage[3] = 0x00.toByte()  // Terminator

                        try {
                            Log.d("NfcViewModel", "Writing CC page 2: ${ccPage.map { "%02X".format(it) }.joinToString(" ")}")
                            mifare.writePage(2, ccPage)
                            Log.d("NfcViewModel", "Successfully wrote CC page 2")
                        } catch (e: Exception) {
                            Log.w("NfcViewModel", "Failed to write CC page: ${e.message}")
                            throw e
                        }

                        // Now write NDEF message starting at page 4
                        // Format: [0xE1, length_byte, NDEF_DATA...]
                        
                        if (ndefBytes.size > 62) {
                            Log.e("NfcViewModel", "NDEF message too large: ${ndefBytes.size} > 62 bytes")
                            onError("Filament ID too long for this tag (max ~45 characters)")
                            return
                        }

                        // Create buffer: [0xE1, size, NDEF_data...]
                        val ndefBuffer = ByteArray(ndefBytes.size + 2)
                        ndefBuffer[0] = 0xE1.toByte()  // NDEF TLV type
                        ndefBuffer[1] = ndefBytes.size.toByte()  // Length
                        System.arraycopy(ndefBytes, 0, ndefBuffer, 2, ndefBytes.size)

                        Log.d("NfcViewModel", "Total NDEF data to write: ${ndefBuffer.size} bytes")

                        // Write NDEF data to pages 4 and onward (4 bytes per page)
                        var pageIndex = 4
                        var byteIndex = 0

                        while (byteIndex < ndefBuffer.size && pageIndex <= 12) {
                            val pageData = ByteArray(4)
                            
                            // Fill page
                            for (i in 0 until 4) {
                                if (byteIndex + i < ndefBuffer.size) {
                                    pageData[i] = ndefBuffer[byteIndex + i]
                                } else {
                                    pageData[i] = 0x00.toByte()
                                }
                            }

                            try {
                                Log.d("NfcViewModel", "Writing page $pageIndex: ${pageData.map { "%02X".format(it) }.joinToString(" ")}")
                                mifare.writePage(pageIndex, pageData)
                                Log.d("NfcViewModel", "Successfully wrote page $pageIndex")
                            } catch (e: Exception) {
                                Log.w("NfcViewModel", "Failed to write page $pageIndex: ${e.message}")
                                throw e
                            }

                            pageIndex++
                            byteIndex += 4
                        }

                        Log.d("NfcViewModel", "Successfully wrote NDEF to MifareUltralight: $filamentId")
                        onSuccess()
                        return
                    } catch (e: Exception) {
                        Log.w("NfcViewModel", "MifareUltralight write failed: ${e.message}", e)
                    } finally {
                        try {
                            mifare.close()
                        } catch (e: Exception) {
                            Log.w("NfcViewModel", "Failed to close MifareUltralight: ${e.message}")
                        }
                    }
                } else {
                    Log.d("NfcViewModel", "MifareUltralight not available")
                }

                // Fourth attempt: Try to re-open NDEF after format attempt
                // Sometimes the tag needs to be re-queried after formatting attempt
                Log.d("NfcViewModel", "Attempting to re-open NDEF after format attempt...")
                val ndefRetry = Ndef.get(tag)
                if (ndefRetry != null) {
                    Log.d("NfcViewModel", "NDEF available on retry")
                    try {
                        ndefRetry.connect()
                        if (ndefRetry.isWritable) {
                            Log.d("NfcViewModel", "Retrying NDEF write...")
                            val maxSize = ndefRetry.maxSize
                            if (filamentId.length > maxSize) {
                                Log.e("NfcViewModel", "FilamentId exceeds max size on retry")
                                onError(errors.getOrElse(1) { "FilamentId is too long for this tag" })
                                return
                            }
                            val record = NdefRecord.createTextRecord("en", filamentId)
                            val message = NdefMessage(arrayOf(record))
                            ndefRetry.writeNdefMessage(message)
                            Log.d("NfcViewModel", "Successfully wrote to NDEF tag on retry: $filamentId")
                            onSuccess()
                            return
                        }
                    } catch (e: Exception) {
                        Log.w("NfcViewModel", "NDEF retry write failed: ${e.message}", e)
                    } finally {
                        try {
                            ndefRetry.close()
                        } catch (e: Exception) {
                            Log.w("NfcViewModel", "Failed to close NDEF retry: ${e.message}")
                        }
                    }
                }

                // All methods failed
                Log.e("NfcViewModel", "All NDEF write/format methods failed")

                // Provide better error message based on tag type
                val errorMessage = when {
                    tag.techList?.contains("android.nfc.tech.MifareUltralight") == true ->
                        "Mifare Ultralight tag is not NDEF-formatted. Please use NFC Tag Writer app to format this tag first."
                    else -> "Unable to write to tag. Tag may be read-only or corrupted. Try reformatting with NFC tools."
                }
                onError(errorMessage)


            } catch (e: Exception) {
                Log.e("NfcViewModel", "Unexpected error during updateNfcTag: ${e.message}", e)
                onError(errors.getOrElse(3) { "Write failed: ${e.message}" })
            }
        } ?: run {
            Log.e("NfcViewModel", "Tag is null")
            onError(errors.getOrElse(4) { "Tag is null" })
        }
    }
}