package com.example.parkiranauto

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.parkiranauto.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    // binding = lak pristup elementima sa ekrana (dugmad, tekst...)
    private lateinit var binding: ActivityMainBinding

    // Google-ov "klijent" koji nam daje trenutnu lokaciju telefona
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // Ovde trajno čuvamo poslednju sačuvanu lokaciju (ostaje i kad ugasiš app)
    private lateinit var prefs: SharedPreferences

    // Ovo je "pitanje za dozvolu" za lokaciju. Kad korisnik odgovori,
    // pozove se blok koda ispod.
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchAndSaveLocation()
        } else {
            toast(getString(R.string.permission_needed))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        prefs = getSharedPreferences("parking", Context.MODE_PRIVATE)

        // Klik na "Zapamti gde sam parkirao"
        binding.saveButton.setOnClickListener {
            if (hasLocationPermission()) {
                fetchAndSaveLocation()
            } else {
                // Ako nemamo dozvolu, pitamo korisnika
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }

        // Klik na "Vodi me do auta"
        binding.navigateButton.setOnClickListener {
            openNavigation()
        }

        // Kad se app otvori, prikaži poslednju sačuvanu lokaciju (ako postoji)
        showSavedLocation()
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this, Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // Uzmi trenutnu lokaciju sa GPS-a i sačuvaj je
    private fun fetchAndSaveLocation() {
        if (!hasLocationPermission()) return

        binding.savedInfo.text = getString(R.string.getting_location)
        val cancellationToken = CancellationTokenSource()

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    val note = binding.noteInput.text?.toString()?.trim() ?: ""
                    prefs.edit()
                        .putFloat("lat", location.latitude.toFloat())
                        .putFloat("lng", location.longitude.toFloat())
                        .putLong("time", System.currentTimeMillis())
                        .putString("note", note)
                        .apply()
                    toast(getString(R.string.saved_ok))
                    showSavedLocation()
                } else {
                    binding.savedInfo.text = getString(R.string.location_failed)
                    toast(getString(R.string.location_failed))
                }
            }.addOnFailureListener {
                binding.savedInfo.text = getString(R.string.location_failed)
                toast(getString(R.string.location_failed))
            }
        } catch (e: SecurityException) {
            toast(getString(R.string.permission_needed))
        }
    }

    // Prikaži sačuvanu lokaciju na kartici i uključi/isključi dugme za navigaciju
    private fun showSavedLocation() {
        if (!prefs.contains("lat")) {
            binding.savedInfo.text = getString(R.string.no_saved_location)
            binding.navigateButton.isEnabled = false
            return
        }

        val lat = prefs.getFloat("lat", 0f)
        val lng = prefs.getFloat("lng", 0f)
        val time = prefs.getLong("time", 0L)
        val note = prefs.getString("note", "") ?: ""

        val dateStr = SimpleDateFormat("dd.MM.yyyy. HH:mm", Locale.getDefault())
            .format(Date(time))

        val sb = StringBuilder()
        sb.append("📍 ").append(String.format(Locale.US, "%.5f, %.5f", lat, lng)).append("\n")
        sb.append("🕒 ").append(dateStr)
        if (note.isNotEmpty()) {
            sb.append("\n📝 ").append(note)
        }

        binding.savedInfo.text = sb.toString()
        binding.navigateButton.isEnabled = true

        // Ako je beleška prazna, popuni polje sačuvanom beleškom (radi lakšeg pregleda)
        if (binding.noteInput.text.isNullOrEmpty() && note.isNotEmpty()) {
            binding.noteInput.setText(note)
        }
    }

    // Otvori navigaciju do sačuvane lokacije (Google Maps ili druga mapa)
    private fun openNavigation() {
        if (!prefs.contains("lat")) return
        val lat = prefs.getFloat("lat", 0f)
        val lng = prefs.getFloat("lng", 0f)

        // Prvo pokušaj da pokreneš navigaciju direktno u Google Maps
        val navUri = Uri.parse("google.navigation:q=$lat,$lng&mode=w") // mode=w = pešačenje
        val navIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            startActivity(navIntent)
            return
        } catch (e: android.content.ActivityNotFoundException) {
            // Google Maps nije dostupan — probaćemo rezervni način ispod
        }

        // Rezervno: otvori bilo koju mapu preko standardnog geo: linka
        val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(Moj auto)")
        val geoIntent = Intent(Intent.ACTION_VIEW, geoUri)
        try {
            startActivity(geoIntent)
        } catch (e: android.content.ActivityNotFoundException) {
            toast(getString(R.string.no_maps))
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
