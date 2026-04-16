package com.example.matelecommandefree

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Base64
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import kotlinx.coroutines.*
import java.net.Socket
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class AdbManager(private val context: Context) {
    private val TAG = "AdbManager"
    private var adbConnection: AdbConnection? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    var isConnected by mutableStateOf(false)
    var statusMessage by mutableStateOf("Recherche de la Freebox...")
    var ipAddress by mutableStateOf("")

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            Log.d(TAG, "Dcouverte lance")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            Log.d(TAG, "Service trouv: ${service.serviceName}")
            if (service.serviceType.contains("_adb")) {
                nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.e(TAG, "Resolve failed: $errorCode")
                    }

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        val host = serviceInfo.host.hostAddress
                        Log.d(TAG, "Resolved IP: $host")
                        if (host != null) {
                            ipAddress = host
                            if (!isConnected) connect()
                        }
                    }
                })
            }
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            Log.d(TAG, "Service perdu")
        }

        override fun onDiscoveryStopped(regType: String) {}
        override fun onStartDiscoveryFailed(regType: String, errorCode: Int) {}
        override fun onStopDiscoveryFailed(regType: String, errorCode: Int) {}
    }

    init {
        // Restaurer la dernire IP connue
        ipAddress = context.getSharedPreferences("adb_prefs", Context.MODE_PRIVATE).getString("last_ip", "") ?: ""
        startDiscovery()
        if (ipAddress.isNotEmpty()) connect()
    }

    private fun startDiscovery() {
        try {
            nsdManager.discoverServices("_adb._tcp.", NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Discovery error", e)
        }
    }

    fun connect() {
        if (ipAddress.isEmpty()) return
        scope.launch {
            try {
                statusMessage = "Connexion  $ipAddress..."
                val socket = Socket(ipAddress, 5555)
                val crypto = getPersistentCrypto()
                val connection = AdbConnection.create(socket, crypto)
                connection.connect()
                adbConnection = connection
                isConnected = true
                statusMessage = "Connect en Wi-Fi"
                // Sauvegarder l'IP
                context.getSharedPreferences("adb_prefs", Context.MODE_PRIVATE).edit().putString("last_ip", ipAddress).apply()
            } catch (e: Exception) {
                Log.e(TAG, "Connect error", e)
                isConnected = false
                statusMessage = "Erreur de connexion (Freebox dbranche ?)"
            }
        }
    }

    fun sendKey(code: Int, count: Int = 1) {
        scope.launch {
            try {
                if (adbConnection == null) connect()
                // Générer une commande du type "input keyevent 21 21 21..."
                val keys = List(count) { code.toString() }.joinToString(" ")
                val stream = adbConnection?.open("shell:input keyevent $keys")
                stream?.close()
            } catch (e: Exception) {
                isConnected = false
                statusMessage = "Déconnecté"
                connect() // Auto-reconnect
            }
        }
    }

    fun launchApp(pkg: String) {
        scope.launch {
            try {
                if (adbConnection == null) connect()
                adbConnection?.open("shell:monkey -p $pkg -c android.intent.category.LEANBACK_LAUNCHER 1")?.close()
            } catch (e: Exception) {}
        }
    }

    private fun getPersistentCrypto(): AdbCrypto {
        val prefs = context.getSharedPreferences("adb_keys", Context.MODE_PRIVATE)
        val pubKeyStr = prefs.getString("public_key", null)
        val privKeyStr = prefs.getString("private_key", null)

        val keyPair: KeyPair
        if (pubKeyStr != null && privKeyStr != null) {
            val keyFactory = KeyFactory.getInstance("RSA")
            val pubKeySpec = X509EncodedKeySpec(Base64.decode(pubKeyStr, Base64.NO_WRAP))
            val privKeySpec = PKCS8EncodedKeySpec(Base64.decode(privKeyStr, Base64.NO_WRAP))
            keyPair = KeyPair(keyFactory.generatePublic(pubKeySpec), keyFactory.generatePrivate(privKeySpec))
        } else {
            val generator = KeyPairGenerator.getInstance("RSA")
            generator.initialize(2048)
            keyPair = generator.generateKeyPair()
            prefs.edit()
                .putString("public_key", Base64.encodeToString(keyPair.public.encoded, Base64.NO_WRAP))
                .putString("private_key", Base64.encodeToString(keyPair.private.encoded, Base64.NO_WRAP))
                .apply()
        }
        return AdbCrypto.loadAdbKeyPair(object : AdbBase64 {
            override fun encodeToString(b: ByteArray): String {
                return Base64.encodeToString(b, Base64.NO_WRAP)
            }
        }, keyPair)
    }
}
