# 🚗 Gde mi je auto

Jednostavna Android aplikacija koja pamti gde si parkirao auto i navigira te nazad do njega.
Sve radi **offline, na tvom telefonu** — bez naloga, bez interneta, bez deljenja podataka.

## Šta radi

- **Zapamti gde sam parkirao** — jednim dugmetom sačuva trenutnu GPS lokaciju + vreme + tvoju belešku (npr. „nivo 2, kod stuba B").
- **Vodi me do auta** — otvara navigaciju (Google Maps) do sačuvane lokacije, pešačenje.
- Poslednja lokacija ostaje sačuvana i kad ugasiš i ponovo otvoriš aplikaciju.

## Kako da napraviš aplikaciju (APK)

Treba ti **Android Studio** (besplatan): https://developer.android.com/studio

1. Otvori Android Studio → **Open** → izaberi ovaj folder projekta.
2. Sačekaj da Android Studio preuzme Gradle i biblioteke (prvi put traje par minuta, potreban je internet). Ako te pita da instalira „Android SDK" ili „Build Tools", prihvati.
3. Priključi svoj telefon USB kablom i uključi **USB debugging** (Podešavanja → O telefonu → 7 puta klikni na „Broj verzije/Build number" → nazad → Opcije za programere → USB debugging).
4. Pritisni zeleno dugme **Run ▶**. Aplikacija se instalira i pokrene na telefonu.

Ako želiš samostalan APK fajl:
- Meni **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
- APK se pravi u `app/build/outputs/apk/debug/app-debug.apk` — taj fajl možeš da prebaciš i instaliraš na telefon.

## Kako se koristi

1. Prvi put kad pritisneš „Zapamti gde sam parkirao", telefon će pitati za **dozvolu za lokaciju** — dozvoli.
2. Za najbolju tačnost, sačuvaj lokaciju **napolju / na otvorenom** kad izađeš iz auta.
3. Kad se vraćaš, pritisni „Vodi me do auta".

## Tehnički detalji

- Jezik: **Kotlin**
- Minimalni Android: **7.0 (API 24)**
- Lokacija: Google `FusedLocationProvider` (`play-services-location`)
- Čuvanje: `SharedPreferences` (lokalno na telefonu)
- Nema internet dozvole — aplikacija ne šalje nikakve podatke nikuda.

## Privatnost

Aplikacija koristi lokaciju **samo** kada ti pritisneš dugme, čuva je **samo** na tvom telefonu i **ne** šalje je nigde.
