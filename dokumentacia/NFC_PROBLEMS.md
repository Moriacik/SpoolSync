# NFC Problémy a Riešenia

## 📋 Zhrnutie Problému
Android aplikácia sa pokúšala implementovať NFC čítanie a zápis na Mifare Ultralight tagoch, ale narazila na sériu problémov s NDEF formatovaním, dvojitým čítaním a systémovým prístupom.

---

## 🔴 Primárne Problémy

### 1. **"Tag does not support NDEF" Error**

#### Symptóm
Pri pokuse zapísať filament ID na tag:
```
E: Tag does not support NDEF format. Cannot write filament ID to this tag.
```

Hoci tag je preformatovaný ako NDEF v inej aplikácii.

#### Log Output
```
2026-01-14 11:52:22.584 5469-5542 NfcViewModel D  Tag technologies available: android.nfc.tech.NfcA, android.nfc.tech.MifareUltralight
2026-01-14 11:52:22.584 5469-5542 NfcViewModel D  NDEF not directly available, trying NdefFormatable...
2026-01-14 11:52:22.584 5469-5542 NfcViewModel D  NdefFormatable not available
2026-01-14 11:52:22.584 5469-5542 NfcViewModel E  All NDEF write/format methods failed
```

#### Príčina
- Mifare Ultralight tag nie je kompatibilný s Android NDEF API
- Android systém nevie priamo pristupovať NDEF na tomto type tagu
- Tag vyžaduje low-level `MifareUltralight` API na zápis

#### Riešenie
Použite `MifareUltralight` API namiesto `Ndef`:

```kotlin
fun writeMifareUltralight(tag: Tag, filamentId: String) {
    val mifareUltra = MifareUltralight.get(tag)
    try {
        mifareUltra.connect()
        
        // Vytvorte NDEF message ručne
        val ndefRecord = NdefRecord.createTextRecord("en", filamentId)
        val ndefMessage = NdefMessage(arrayOf(ndefRecord))
        val ndefBytes = ndefMessage.toByteArray()
        
        // Zapíšte na stránky 4-13 (Mifare Ultralight má 16 stránok)
        var pageIndex = 4
        for (i in ndefBytes.indices step 4) {
            val pageData = ndefBytes.slice(i until minOf(i + 4, ndefBytes.size))
                .toByteArray()
                .padStart(4, 0)
            mifareUltra.writePage(pageIndex, pageData)
            pageIndex++
        }
        
        Log.d("NFC", "Successfully wrote to MifareUltralight")
    } catch (e: Exception) {
        Log.e("NFC", "Error writing: ${e.message}")
    } finally {
        mifareUltra.close()
    }
}
```

---

### 2. **"Expected MB Flag" / "Trailing Data" Error pri Čítaní**

#### Symptóm
Po úspešnom zápise sa tag číta, ale NDEF parser hádže chyby:

```
W: Failed to parse NDEF message: expected MB flag
android.nfc.FormatException: expected MB flag
```

Alebo:

```
W: Failed to parse NDEF message: trailing data
android.nfc.FormatException: trailing data
```

#### Log Output - Nesprávny Formát
```
2026-01-14 12:11:33.754 13417-13508 NfcViewModel D  Extracted NDEF message from bytes 16 to 78
2026-01-14 12:11:33.754 13417-13508 NfcViewModel D  NDEF payload (62 bytes): E1 10 2B FE D1 01 27...
2026-01-14 12:11:33.756 13417-13508 NfcViewModel W  Failed to parse NDEF message: unexpected ME flag in non-trailing chunk
```

#### Príčina
- **NDEF TLV header** (`E1 10 3E 00`) sa zapisuje do payloadu namiesto spracovaného formátu
- Pri čítaní sa NDEF header nezavádza správne v parsovacích bytoch
- Mifare Ultralight vyžaduje manuálnu správu TLV štruktúry

#### Riešenie - Správny NDEF Zápis

```kotlin
fun writeNdefToMifareUltralight(tag: Tag, filamentId: String) {
    val mifareUltra = MifareUltralight.get(tag)
    try {
        mifareUltra.connect()
        
        // Vytvorte NDEF record
        val ndefRecord = NdefRecord.createTextRecord("en", filamentId)
        val ndefMessage = NdefMessage(arrayOf(ndefRecord))
        val messageBytes = ndefMessage.toByteArray()
        
        // NDEF TLV struktura:
        // Byte 0-1: Capability Container (CC) - [E1][10] = NDEF capable, 16 bytes max
        // Byte 2: NDEF TLV Type = [03] (NDEF message)
        // Byte 3: NDEF Length = messageBytes.size
        // Bytes 4+: NDEF message data
        
        val tlvData = ByteArray(messageBytes.size + 4)
        tlvData[0] = 0xE1.toByte()  // CC - NDEF capable
        tlvData[1] = 0x10.toByte()  // CC - 16 bytes max
        tlvData[2] = 0x03.toByte()  // NDEF message TLV type
        tlvData[3] = messageBytes.size.toByte()  // Length
        System.arraycopy(messageBytes, 0, tlvData, 4, messageBytes.size)
        
        // Zapíšte do stránok 2-13 (36 bytes = 9 stránok po 4 bytoch)
        var pageIndex = 2
        for (i in tlvData.indices step 4) {
            val endIdx = minOf(i + 4, tlvData.size)
            val pageData = tlvData.slice(i until endIdx).toByteArray()
            
            // Pad to 4 bytes if needed
            val paddedData = if (pageData.size < 4) {
                pageData + ByteArray(4 - pageData.size)
            } else {
                pageData
            }
            
            mifareUltra.writePage(pageIndex, paddedData)
            Log.d("NFC", "Wrote page $pageIndex")
            pageIndex++
        }
        
        Log.d("NFC", "Successfully wrote NDEF to MifareUltralight: $filamentId")
    } catch (e: Exception) {
        Log.e("NFC", "Error writing: ${e.message}", e)
    } finally {
        mifareUltra.close()
    }
}
```

---

### 3. **Čítanie Vracia Skrátené Dáta**

#### Symptóm
```
2026-01-14 12:16:05.847 14615-14899 NfcViewModel D  Extracted text: '27326370-7c66-4a2a-b3??????????????????????????????'
```

Čítané UUID je skrátené, chýbajú znaky na konci.

#### Príčina
- Pri čítaní sa čítajú iba prvé 48 bytov (3 stránky × 4 byty na stránku)
- Filament ID UUID (36 znakov) + TLV overhead potrebuje viac miesta
- Payload sa nesprávne parsuje

#### Riešenie - Podrobné Čítanie
```kotlin
fun readNdefFromMifareUltralight(tag: Tag): String? {
    val mifareUltra = MifareUltralight.get(tag)
    return try {
        mifareUltra.connect()
        
        // Čítajte viac stránok pre úplné dáta
        val allData = ByteArray(64) // 16 stránok × 4 byty
        
        // Mifare Ultralight má 16 stránok (0-15)
        // Stránky 0-3 sú read-only (serial, lock bits, OTP)
        // Stránky 4-15 sú dostupné
        
        var currentIndex = 0
        for (page in 0..11) {  // Čítajte strany 0-11 (48 bytov)
            val data = mifareUltra.readPage(page)
            data.copyInto(allData, currentIndex)
            currentIndex += 4
        }
        
        // Hľadajte NDEF TLV header [E1] alebo [03]
        var ndefStartIndex = -1
        for (i in 0 until currentIndex) {
            if (allData[i] == 0x03.toByte() || allData[i] == 0xE1.toByte()) {
                ndefStartIndex = i
                break
            }
        }
        
        if (ndefStartIndex < 0) {
            Log.w("NFC", "No NDEF TLV header found")
            return null
        }
        
        // Preskočte TLV header [03] a length byte
        val ndefLength = allData[ndefStartIndex + 1].toInt() and 0xFF
        val ndefData = allData.slice(
            ndefStartIndex + 2 until ndefStartIndex + 2 + ndefLength
        ).toByteArray()
        
        // Parsujte NDEF message
        val ndefMessage = NdefMessage(ndefData)
        val records = ndefMessage.records
        
        if (records.isNotEmpty()) {
            val firstRecord = records[0]
            val payload = firstRecord.payload
            
            // Text record: payload[0] = status, payload[1..3] = language, payload[4+] = text
            val text = String(payload.slice(3 until payload.size).toByteArray())
            Log.d("NFC", "Read text: $text")
            return text
        }
        
        null
    } catch (e: Exception) {
        Log.e("NFC", "Error reading: ${e.message}", e)
        null
    } finally {
        mifareUltra.close()
    }
}
```

---

### 4. **Dvojité Čítanie - NFC Intent Trigger**

#### Symptóm
Po naskenovaní tagu aplikáciou sa tag načíta:
1. Aplikácia ho číta (správne ✅)
2. Android NFC systém pokúša otvoriť inú aplikáciu ("No supported application for this NFC tag")

#### Log
```
2026-01-14 12:11:33.728 13417-13508 NfcViewModel D  Starting readNfcTag
```
(po chvíli)
```
Toast: "No supported application for this NFC tag"
```

#### Príčina
- Android zabuduje NFC intent po prvom čítaní
- NFC systém pokúša otvoriť intent pre TECH_DISCOVERED
- Ak aplikácia nie je zaregistrovaná ako NFC handler v AndroidManifest.xml, systém vyhľadá inú

#### Riešenie - AndroidManifest.xml
```xml
<activity android:name=".MainActivity">
    <!-- ... -->
    
    <!-- NFC Intent Filter -->
    <intent-filter>
        <action android:name="android.nfc.action.TECH_DISCOVERED" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    
    <intent-filter>
        <action android:name="android.nfc.action.TAG_DISCOVERED" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
    
    <meta-data
        android:name="android.nfc.action.TECH_DISCOVERED"
        android:resource="@xml/nfc_tech_filter" />
</activity>
```

**Vytvorte `res/xml/nfc_tech_filter.xml`:**
```xml
<resources xmlns:xliff="urn:oasis:names:tc:xliff:document:1.2">
    <tech-list>
        <tech>android.nfc.tech.NfcA</tech>
        <tech>android.nfc.tech.MifareUltralight</tech>
        <tech>android.nfc.tech.Ndef</tech>
    </tech-list>
</resources>
```

**Spracujte Intent v MainActivity:**
```kotlin
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    Log.d("MainActivity", "onNewIntent called")
    
    if (NfcAdapter.ACTION_TECH_DISCOVERED == intent.action) {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        if (tag != null) {
            handleNfcTag(tag)
        }
    }
}

private fun handleNfcTag(tag: Tag) {
    // Skontrolujte, či ste v NfcScreen
    // Ak áno, delegujte na NfcViewModel
    // Ak nie, ignorujte (nevoľajte ďalšie aplikácie)
    Log.d("MainActivity", "Handling NFC tag in current screen")
}
```

---

### 5. **"Filament not found" pri Čítaní Prázdneho Tagu**

#### Symptóm
```
D: Filament not found - tag was loaded and read (even if empty)
```

#### Príčina
- Aplikácia pokúša nájsť filament s daným UUID
- Tag je prázdny (alebo UUID sa nezhoduje)
- Žiadna chyba, iba informácia

#### Riešenie
Toto je očakávané správanie. Prázdny tag by mal byť:
1. Automaticky formatovaný ako NDEF
2. Ponúka sa formulár na zadanie nového filamentu

```kotlin
val filament = viewModel.getFilamentById(readUuid)
if (filament == null) {
    Log.d("NFC", "Filament not found - tag may be empty or new")
    // Ponúknite zápis nového filamentu
    showNewFilamentDialog()
}
```

---

## ✅ Finálna Implementácia - NFC ViewModel

```kotlin
@HiltViewModel
class NfcViewModel @Inject constructor(
    private val filamentRepository: FilamentRepository
) : ViewModel() {
    
    // Čítanie s omnoho bytmi
    fun readNfcTag(tag: Tag) {
        viewModelScope.launch {
            try {
                val filamentId = readMifareUltralight(tag)
                
                if (filamentId != null) {
                    Log.d("NFC", "Successfully read: $filamentId")
                    _readData.value = filamentId
                    
                    // Hľadajte filament
                    val filament = filamentRepository.getFilament(filamentId)
                    if (filament != null) {
                        _filament.value = filament
                    } else {
                        Log.w("NFC", "Filament not found: $filamentId")
                    }
                } else {
                    _error.value = "Nie je možné čítať z tagu"
                }
            } catch (e: Exception) {
                Log.e("NFC", "Error reading NFC tag: ${e.message}", e)
                _error.value = "Error reading NFC tag: ${e.message}"
            }
        }
    }
    
    // Zápis na tag
    fun writeNfcTag(tag: Tag, filamentId: String) {
        viewModelScope.launch {
            try {
                writeToMifareUltralight(tag, filamentId)
                Log.d("NFC", "Successfully wrote to tag: $filamentId")
                _writeSuccess.value = true
            } catch (e: Exception) {
                Log.e("NFC", "Error writing to NFC tag: ${e.message}", e)
                _error.value = "Error writing to NFC tag: ${e.message}"
            }
        }
    }
    
    private suspend fun readMifareUltralight(tag: Tag): String? {
        val mifareUltra = MifareUltralight.get(tag)
        return try {
            mifareUltra.connect()
            
            val allData = ByteArray(64)
            var currentIndex = 0
            
            for (page in 0..11) {
                val data = mifareUltra.readPage(page)
                data.copyInto(allData, currentIndex)
                currentIndex += 4
            }
            
            // Nájdite NDEF TLV header
            var ndefStartIndex = -1
            for (i in 0 until currentIndex) {
                if ((allData[i] == 0x03.toByte() || allData[i] == 0xE1.toByte())
                    && i + 1 < currentIndex) {
                    ndefStartIndex = i
                    break
                }
            }
            
            if (ndefStartIndex < 0) return null
            
            val ndefLength = allData[ndefStartIndex + 1].toInt() and 0xFF
            if (ndefLength <= 0 || ndefStartIndex + 2 + ndefLength > currentIndex) return null
            
            val ndefData = allData.slice(
                ndefStartIndex + 2 until ndefStartIndex + 2 + ndefLength
            ).toByteArray()
            
            val ndefMessage = NdefMessage(ndefData)
            val records = ndefMessage.records
            
            if (records.isNotEmpty()) {
                val payload = records[0].payload
                return String(payload.slice(3 until payload.size).toByteArray()).trim()
            }
            
            null
        } catch (e: Exception) {
            Log.e("NFC", "Read error: ${e.message}", e)
            null
        } finally {
            try { mifareUltra.close() } catch (e: Exception) { }
        }
    }
    
    private suspend fun writeToMifareUltralight(tag: Tag, filamentId: String) {
        val mifareUltra = MifareUltralight.get(tag)
        try {
            mifareUltra.connect()
            
            val ndefRecord = NdefRecord.createTextRecord("en", filamentId)
            val ndefMessage = NdefMessage(arrayOf(ndefRecord))
            val messageBytes = ndefMessage.toByteArray()
            
            val tlvData = ByteArray(messageBytes.size + 4)
            tlvData[0] = 0xE1.toByte()
            tlvData[1] = 0x10.toByte()
            tlvData[2] = 0x03.toByte()
            tlvData[3] = messageBytes.size.toByte()
            System.arraycopy(messageBytes, 0, tlvData, 4, messageBytes.size)
            
            var pageIndex = 2
            for (i in tlvData.indices step 4) {
                val endIdx = minOf(i + 4, tlvData.size)
                val pageData = tlvData.slice(i until endIdx).toByteArray()
                    .let { if (it.size < 4) it + ByteArray(4 - it.size) else it }
                
                mifareUltra.writePage(pageIndex, pageData)
                pageIndex++
            }
            
            Log.d("NFC", "Write successful")
        } finally {
            try { mifareUltra.close() } catch (e: Exception) { }
        }
    }
}
```

---

## 📌 Súhrn - Kontrolný List

| Problém | Riešenie |
|---------|----------|
| NDEF chyba | Používajte `MifareUltralight` API |
| Čítanie vracia skrátené dáta | Čítajte všetky potrebné stránky (12+) |
| Doubles NFC reads | Registrujte v AndroidManifest.xml |
| Parsing chyby | Správne spracujte TLV header |
| Prázdny tag | Ponúknite nový filament |

---

## 🔍 Debug Tipy

```kotlin
// Zobraziť všetky dostupné technológie
val technologies = tag.techList
Log.d("NFC", "Available technologies: ${technologies.joinToString()}")

// Zobraziť raw byty
val bytes = byteArrayOf(0xE1, 0x10, 0x03, ...)
Log.d("NFC", "Raw data: ${bytes.joinToString(" ") { "%02X".format(it) }}")
```

