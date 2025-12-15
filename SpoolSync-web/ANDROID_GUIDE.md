# Android Implementation Guide

## QR Scanner Integration

Použití pre skenovanie QR kódu z webovej aplikácie.

### Dependencies (build.gradle)

```gradle
dependencies {
    // QR Code scanning
    implementation 'com.google.mlkit:vision-common:17.3.0'
    implementation 'com.google.mlkit:barcode-scanning:17.1.0'
    implementation 'com.google.android.gms:play-services-mlkit-barcode-scanning:18.3.0'
    
    // Firebase
    implementation 'com.google.firebase:firebase-auth:23.0.0'
    implementation 'com.google.firebase:firebase-firestore:25.0.0'
}
```

## QR Code Scanning Activity

```kotlin
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import android.graphics.Bitmap
import org.json.JSONObject

class QRScannerActivity : AppCompatActivity() {
    
    private val scanner: BarcodeScanner by lazy {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        com.google.mlkit.vision.barcode.BarcodeScanning.getClient(options)
    }
    
    fun scanQRFromBitmap(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    if (rawValue != null) {
                        handleQRData(rawValue)
                    }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
    
    private fun handleQRData(qrContent: String) {
        try {
            val qrData = JSONObject(qrContent)
            val sessionId = qrData.getString("sessionId")
            val requestToken = qrData.getString("requestToken")
            val expiresAt = qrData.getLong("expiresAt")
            
            // Overenie expirácii
            if (System.currentTimeMillis() > expiresAt) {
                showError("QR kód vypršal")
                return
            }
            
            // Potvrdenie v Firestore
            confirmQRSession(sessionId, requestToken)
        } catch (e: Exception) {
            showError("Chyba pri spracovaní QR kódu: ${e.message}")
        }
    }
    
    private fun confirmQRSession(sessionId: String, requestToken: String) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        
        if (currentUser == null) {
            showError("User nie je prihlásený")
            return
        }
        
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val sessionRef = db.collection("qrSessions").document(sessionId)
        
        sessionRef.update(
            "confirmed" to true,
            "confirmedBy" to currentUser.uid,
            "confirmedAt" to System.currentTimeMillis()
        )
            .addOnSuccessListener {
                showSuccess("Prihlásenie úspešné!")
                // Navigácia späť na AccountScreen
                navigateToAccountScreen()
            }
            .addOnFailureListener { e ->
                showError("Chyba pri potvrdení: ${e.message}")
            }
    }
    
    private fun navigateToAccountScreen() {
        // Navigácia späť na AccountScreen
        val intent = Intent(this, AccountActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    private fun showSuccess(message: String) {
        // Zobrazenie úspešnej správy (Toast alebo Snackbar)
    }
    
    private fun showError(message: String) {
        // Zobrazenie chyby
    }
}
```

## Data Flow

```
┌─────────────────────────────────────────────────────────┐
│ Android aplikácia (User IS prihlásený)                  │
├─────────────────────────────────────────────────────────┤
│ 1. Klikne "Export Statistics"                            │
│ 2. Otvori QR Scanner                                     │
│ 3. Skane QR kód z webovej stránky                        │
│ 4. Dekóduje: {sessionId, requestToken, expiresAt}       │
│ 5. Overí expiráciu (musí byť < 5 minút stará)           │
│ 6. Pošle potvrdenie do Firestore:                        │
│    - sessionId (doc ID)                                  │
│    - confirmed: true                                     │
│    - confirmedBy: userId (z Firebase Auth)              │
│    - confirmedAt: timestamp                              │
│ 7. Vráti sa na AccountScreen                             │
└─────────────────────────────────────────────────────────┘
                          ↕ Firestore
┌─────────────────────────────────────────────────────────┐
│ Web stránka (bez prihlásenia)                            │
├─────────────────────────────────────────────────────────┤
│ 1. Generuje sessionId + requestToken                     │
│ 2. Uloží do Firestore s TTL 5 minút                      │
│ 3. Generuje QR kód s týmito údajmi                       │
│ 4. Čaká na zmenu v Firestore (onSnapshot listener)       │
│ 5. Keď confirmed: true:                                  │
│    - Uloží userId do localStorage                        │
│    - Presmeruje na dashboard so štatistikami             │
└─────────────────────────────────────────────────────────┘
```

## Firestore Collection Structure

```
qrSessions/
├── {sessionId}
│   ├── sessionId: string
│   ├── requestToken: string
│   ├── expiresAt: timestamp (5 minutes from now)
│   ├── createdAt: timestamp
│   ├── confirmed: boolean (false by default)
│   ├── confirmedBy: string (userId of the confirmer)
│   └── confirmedAt: timestamp (when confirmed)
```

## Security Rules

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /qrSessions/{sessionId} {
      // Any anonymous user can create QR sessions
      allow create;
      
      // Any user can read (to verify it exists)
      allow read;
      
      // Only authenticated users can update
      allow update: if request.auth != null && 
                       request.data.confirmed == true &&
                       request.data.confirmedBy == request.auth.uid;
    }
  }
}
```

## Error Handling

```kotlin
// Možné chyby a ich riešenie:

// 1. QR kód vypršal
if (System.currentTimeMillis() > expiresAt) {
    showError("QR kód vypršal. Prosím, vygeneruj nový.")
    return
}

// 2. User nie je prihlásený
if (currentUser == null) {
    // Presmeruj na login
    startActivity(Intent(this, LoginActivity::class.java))
    return
}

// 3. Firestore chyba
.addOnFailureListener { e ->
    when (e) {
        is FirebaseFirestoreException -> {
            when (e.code) {
                FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                    showError("Nemáš povolenie na túto akciu")
                FirebaseFirestoreException.Code.NOT_FOUND ->
                    showError("Session nenájdený")
                else ->
                    showError("Chyba Firestore: ${e.message}")
            }
        }
        else -> showError("Neznáma chyba: ${e.message}")
    }
}
```

## Testing

```kotlin
@Test
fun testQRDecoding() {
    val qrContent = """{"sessionId":"123","requestToken":"456","expiresAt":${System.currentTimeMillis() + 300000}}"""
    val qrData = JSONObject(qrContent)
    
    assertEquals("123", qrData.getString("sessionId"))
    assertEquals("456", qrData.getString("requestToken"))
    assertTrue(qrData.getLong("expiresAt") > System.currentTimeMillis())
}
```
