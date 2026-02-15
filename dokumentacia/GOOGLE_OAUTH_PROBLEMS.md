# Google OAuth 2.0 Problémy a Riešenia

## 📋 Zhrnutie Problému
Android aplikácia sa pokúšala implementovať Google Sign-In funkčnosť, ale po výbere Google účtu sa aplikácia zatuhla na načítavacej animácii bez ďalšieho pokroku.

---

## 🔴 Primárny Problém

### Symptóm
Po kliknutí na tlačidlo "Sign In with Google":
1. Otvorí sa výber Google účtu
2. User vyberie svoj účet
3. Aplikácia zobrazí loading animation
4. **Aplikácia sa nezavesí (visí na loading screene)**

### Log Output
```
2026-02-15 12:32:03.014 20335-20335 LoginScreen    D  Google Sign-In button clicked
2026-02-15 12:32:03.015 20335-20335 LoginScreen    D  Creating GoogleSignInOptions...
2026-02-15 12:32:03.016 20335-20335 LoginScreen    D  Getting Google SignIn client...
2026-02-15 12:32:03.022 20335-20335 LoginScreen    D  Launching Google Sign-In intent...
2026-02-15 12:32:05.630 20335-20335 LoginScreen    D  Google Sign-In Launcher result: resultCode=0
2026-02-15 12:32:05.630 20335-20335 LoginScreen    D  Result not OK (probably user cancelled)
```

**Analýza:**
- `resultCode=0` znamená `RESULT_CANCELLED`
- Log hovorí "Result not OK", čo je chybné - user si vybral účet ale kód to interpretuje ako zrušenie

---

## 🔧 Hlavné Problémy

### 1. **JAVA_HOME Environment Variable**

#### Problém
```
ERROR: JAVA_HOME is set to an invalid directory: C:\Program Files\Java\bin
Please set the JAVA_HOME variable in your environment to match the
location of your Java installation.
```

#### Príčina
`JAVA_HOME` by malo ukazovať na adresár JDK, nie na `bin` podadresár.

#### Riešenie
```bash
# Windows PowerShell (správne)
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"  # Alebo aktuálna verzia
$env:JAVA_HOME="C:\Users\<user>\AppData\Local\Android\Sdk\jdk"  # Ak je v Android SDK

# Overenie
java -version
```

---

### 2. **Chýbajúci Android OAuth 2.0 Client ID**

#### Problém
Firebase Console → OAuth 2.0 Client IDs nemá **Android** aplikáciu, len:
- Web client (z dneška)
- Web client (staršia verzia)

#### Príčina
Android aplikácia potrebuje vlastný Client ID pre Google Sign-In. Bez tohto sú všetky pokus o autentifikáciu neúspešné.

#### Riešenie

**Krok 1: Získajte SHA-1 Fingerprint vašej aplikácie**

```bash
# Navigujte do root adresáru projektu
cd C:\Users\marti\Documents\GitHub\SpoolSync\SpoolSync

# Spustite signingReport (nastavte JAVA_HOME najprv!)
.\gradlew.bat signingReport
```

**Výstup bude vyzerať takto:**
```
Task :app:signingReport
Variant: debug
Config: debug
Store: C:\Users\...\debug.keystore
Alias: AndroidDebugKey
MD5: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
SHA1: AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD
SHA-256: ...
```

**Krok 2: Pridajte Android aplikáciu do Firebase Console**

1. Idite do: https://console.firebase.google.com/
2. Vyberte projekt → **Project Settings** → **Your apps**
3. Kliknite na **Android** (ak nie je, kliknite `+`)
4. Vyplňte:
   - **Package name:** `com.example.spoolsync`
   - **SHA-1 certificate fingerprint:** `AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD`
5. Kliknite **Register app**
6. **Stiahnite** `google-services.json` a umiestnite do `app/` adresára

**Krok 3: Aktualizujte `AndroidManifest.xml`**

Ubezpečte sa, že máte:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

### 3. **Nesprávna Implementácia AuthViewModel**

#### Problém
LoginScreen sa pokúša volať `authViewModel.signInWithGoogle(account)`, ale callback sa nikdy nespustí.

#### Príčina
AuthViewModel pravdepodobne:
- Nemá správnú Firebase autentifikáciu
- Callback nie je volaný asynchronne
- IDToken sa neosiela k Firebasu správne

#### Riešenie - AuthViewModel.kt
```kotlin
fun signInWithGoogle(account: GoogleSignInAccount, callback: (Boolean, String?) -> Unit) {
    Log.d("AuthViewModel", "signInWithGoogle called with account: ${account.email}")
    
    // Získajte ID Token
    val idToken = account.idToken
    if (idToken == null) {
        Log.e("AuthViewModel", "ID Token je null")
        callback(false, "Nie je možné získať ID Token")
        return
    }

    // Vytvorte Firebase credential
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    
    // Prihláste sa do Firebasu
    auth.signInWithCredential(credential)
        .addOnSuccessListener { result ->
            Log.d("AuthViewModel", "Firebase signIn success: ${result.user?.uid}")
            
            // Uložte user info do Firebasu
            val userId = result.user?.uid ?: return@addOnSuccessListener
            val userRef = firestore.collection("users").document(userId)
            
            userRef.set(mapOf(
                "email" to account.email,
                "displayName" to account.displayName,
                "photoURL" to account.photoUrl?.toString(),
                "createdAt" to System.currentTimeMillis()
            ), SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("AuthViewModel", "User data saved")
                    callback(true, null)
                }
                .addOnFailureListener { e ->
                    Log.e("AuthViewModel", "Failed to save user data: ${e.message}")
                    callback(false, e.message)
                }
        }
        .addOnFailureListener { e ->
            Log.e("AuthViewModel", "Firebase signIn failed: ${e.message}")
            callback(false, e.message)
        }
}
```

---

### 4. **Možné Riešenie: Nesprávny Client ID v strings.xml**

#### Problém
`google_web_client_id` v `strings.xml` (alebo `values/strings.xml`) môže byť:
- Web Client ID namiesto Android Client ID
- Zablokovaný Client ID
- Nesprávny formát

#### Riešenie
```xml
<!-- values/strings.xml -->
<string name="google_web_client_id">725419336343-d0te5osajslp966jks8dd4m0mo0ipqp2.apps.googleusercontent.com</string>

<!-- Vygenerovaný z Firebase Console pre ANDROID aplikáciu -->
<!-- Stiahnite z google-services.json ako: -->
<!-- "client_id" z "oauth_client" kde "client_type" == 1 (Android) -->
```

---

## ✅ Implementácia - Krok za Krokom

### 1. Opravte JAVA_HOME
```powershell
# PowerShell (admin)
$env:JAVA_HOME="C:\Users\marti\AppData\Local\Android\Sdk\jdk\17.0.1"
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Users\marti\AppData\Local\Android\Sdk\jdk\17.0.1", "User")
```

### 2. Získajte SHA-1
```bash
cd C:\Users\marti\Documents\GitHub\SpoolSync\SpoolSync
.\gradlew.bat signingReport
```

### 3. Zaregistrujte Android App v Firebase
- Package name: `com.example.spoolsync`
- SHA-1: (z kroku 2)
- Stiahnite `google-services.json`

### 4. Aktualizujte AuthViewModel
```kotlin
// Pridajte Google Provider
val credential = GoogleAuthProvider.getCredential(idToken, null)
auth.signInWithCredential(credential)
    .addOnSuccessListener { ... }
    .addOnFailureListener { ... }
```

### 5. Testujte
```bash
./gradlew.bat clean build
```

---

## 🔍 Debug Tipy

**Zistite správny Client ID:**
```bash
# Pozrite google-services.json
# Hľadajte: "client_type": 1 (Android), "client_id": "..."
```

**Overenie Konfigurácií:**
- ✅ `JAVA_HOME` nastavený správne
- ✅ `google-services.json` v `app/`
- ✅ Android Client ID v Firebase Console
- ✅ SHA-1 fingerprint sa zhoduje
- ✅ Internet permission v AndroidManifest.xml

---

## 📌 Súhrn

| Problém | Príčina | Riešenie |
|---------|--------|---------|
| Loading visí | Callback nie je volaný | Implementujte `GoogleAuthProvider.getCredential()` |
| JAVA_HOME chyba | Nesprávna cesta | Nastavte na JDK adresár, nie `bin` |
| Chýbajúci Android Client ID | Aplikácia nie je zaregistrovaná | Zaregistrujte v Firebase s SHA-1 |
| "Result not OK" log | Nesprávna interpretácia kódu | Skontrolujte `resultCode` správne |

