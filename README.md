# SaveEat

Un'app Android nativa per tenere traccia di quello che hai in frigorifero e non sprecare cibo.

## Il Problema

SaveEat risolve il problema dello spreco in modo semplice: scansioni lo scontrino con il telefono, i prodotti vengono aggiunti automaticamente alla tua dispensa virtuale, e ricevi notifiche prima che scadano. Se stai per ricomprare quello che hai già, l'app te lo ricorda. Se hai ingredienti che stanno per scadere, ti suggerisce ricette veloci per usarli.

## Come Funziona

### Scansione Scontrini
Fotografa lo scontrino e il sistema estrae automaticamente i prodotti.

### Dispensa Virtuale
Tutti i tuoi prodotti in una lista, con data di scadenza e categoria. Puoi filtrarli, cercarli, e segnare cosa hai consumato.

### Ricette Smart
L'app sapendo ciò che sta per scadere, suggerisce ricette che puoi fare adesso con quello che hai in dispensa.

### Gamification
Ogni prodotto consumato prima che scada = Eco-points. Accumuli punti, sali in classifica con altri utenti.

### Offline First
Non hai internet al supermercato? Non importa. L'app funziona completamente offline. Quando torni online, sincronizza automaticamente.

## Tech Stack

- **Linguaggio**: Kotlin
- **UI**: Jetpack Compose (Material Design 3)
- **Database Locale**: Room
- **Backend**: Supabase (PostgreSQL, Auth, Storage)
- **Async**: Kotlin Coroutines & Flow
- **DI**: Koin
- **Architettura**: MVVM + Clean Architecture
- **OCR**: Google Gemini SDK
- **Auth**: Biometric (impronte/riconoscimento facciale)

## Setup

### Prerequisiti
- Android Studio 2024.1+
- JDK 11+
- Android SDK 34+

### Installazione

```bash
git clone https://github.com/Matt2309/SaveEat.git
cd SaveEat
./gradlew build
```

### Eseguire sul Dispositivo/Emulatore

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```