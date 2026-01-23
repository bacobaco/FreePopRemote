package com.baco.freepopremote

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tananaev.adblib.AdbBase64
import com.tananaev.adblib.AdbConnection
import com.tananaev.adblib.AdbCrypto
import com.tananaev.adblib.AdbStream
import kotlinx.coroutines.*
import java.net.Socket
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

val DeepBlack = Color(0xFF0A0B0D)
val SurfaceGray = Color(0xFF1C1E22)
val AccentRed = Color(0xFFE50914)
val TextGray = Color(0xFF90949C)
val GlowBlue = Color(0xFF4285F4)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Forcer le mode portrait
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = DeepBlack)) {
                Scaffold(
                    containerColor = DeepBlack,
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        FreeboxRemotePro()
                    }
                }
            }
        }
    }
}

@Composable
fun FreeboxRemotePro() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var ipAddress by remember { mutableStateOf("192.168.1.107") }
    var isConnected by remember { mutableStateOf(false) }
    var adbConnection by remember { mutableStateOf<AdbConnection?>(null) }
    var showIpSettings by remember { mutableStateOf(false) }

    fun getPersistentCrypto(): AdbCrypto {
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
        return AdbCrypto.loadAdbKeyPair(AndroidBase64(), keyPair)
    }

    fun connect() {
        scope.launch(Dispatchers.IO) {
            try {
                val socket = Socket(ipAddress, 5555)
                val crypto = getPersistentCrypto()
                val connection = AdbConnection.create(socket, crypto)
                connection.connect()
                adbConnection = connection
                withContext(Dispatchers.Main) {
                    isConnected = true
                    showIpSettings = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isConnected = false
                    showIpSettings = true
                }
            }
        }
    }

    LaunchedEffect(Unit) { connect() }

    fun sendCommand(cmd: String) {
        scope.launch(Dispatchers.IO) {
            try {
                if (adbConnection == null) connect()
                adbConnection?.open("shell:$cmd")?.close()
            } catch (e: Exception) { }
        }
    }

    fun sendKey(code: Int) = sendCommand("input keyevent $code")
    
    fun launchApp(pkg: String) {
        sendCommand("monkey -p $pkg -c android.intent.category.LEANBACK_LAUNCHER 1 || monkey -p $pkg -c android.intent.category.LAUNCHER 1")
    }

    @Composable
    fun RepeatingKey(code: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
        Box(
            modifier = modifier
                .size(56.dp)
                .pointerInput(code) {
                    detectTapGestures(
                        onPress = {
                            val job = scope.launch(Dispatchers.IO) {
                                while (isActive) {
                                    sendKey(code)
                                    delay(400)
                                }
                            }
                            tryAwaitRelease()
                            job.cancel()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                onClick = { showIpSettings = !showIpSettings },
                shape = RoundedCornerShape(24.dp),
                color = SurfaceGray,
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).background(if(isConnected) Color.Green else Color.Red, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if(isConnected) "FREEBOX" else "DECONNECTÉ", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            IconButton(
                onClick = { sendKey(26) },
                modifier = Modifier.size(44.dp).shadow(8.dp, CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFFFF5252), AccentRed)), CircleShape)
            ) {
                Icon(Icons.Default.PowerSettingsNew, null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        if (showIpSettings) {
            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Address", color = TextGray) },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = GlowBlue,
                    unfocusedBorderColor = SurfaceGray
                ),
                trailingIcon = {
                    IconButton(onClick = { connect() }) {
                        Icon(Icons.Default.Refresh, null, tint = Color.White)
                    }
                }
            )
        }

        // --- NUMERIC PAD ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            val numbers = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(null, "0", null)
            )
            numbers.forEach { row ->
                Row(modifier = Modifier.fillMaxWidth(0.95f), horizontalArrangement = Arrangement.SpaceEvenly) {
                    row.forEach { num ->
                        if (num != null) {
                            RemoteButton(text = num) { sendKey(num.single().digitToInt() + 7) }
                        } else {
                            Spacer(modifier = Modifier.size(60.dp))
                        }
                    }
                }
            }
        }

        // --- NAVIGATION & ROCKERS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            ModernRockerRepeatable("VOL", Icons.Default.Add, Icons.Default.Remove, 24, 25, { sendKey(164) }, ::sendKey)

            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxSize().border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape).background(SurfaceGray, CircleShape))
                RepeatingKey(19, Modifier.align(Alignment.TopCenter)) { Icon(Icons.Default.KeyboardArrowUp, null, tint = TextGray, modifier = Modifier.size(32.dp)) }
                RepeatingKey(20, Modifier.align(Alignment.BottomCenter)) { Icon(Icons.Default.KeyboardArrowDown, null, tint = TextGray, modifier = Modifier.size(32.dp)) }
                RepeatingKey(21, Modifier.align(Alignment.CenterStart)) { Icon(Icons.Default.KeyboardArrowLeft, null, tint = TextGray, modifier = Modifier.size(32.dp)) }
                RepeatingKey(22, Modifier.align(Alignment.CenterEnd)) { Icon(Icons.Default.KeyboardArrowRight, null, tint = TextGray, modifier = Modifier.size(32.dp)) }
                Surface(
                    onClick = { sendKey(23) }, shape = CircleShape, color = SurfaceGray,
                    modifier = Modifier.size(60.dp).border(2.dp, Brush.radialGradient(listOf(GlowBlue, Color.Transparent)), CircleShape),
                    tonalElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) { Text("OK", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
                }
            }

            ModernRockerRepeatable("CH", Icons.Default.KeyboardArrowUp, Icons.Default.KeyboardArrowDown, 166, 167, null, ::sendKey)
        }

        // --- ACTIONS & APPS ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                ActionPill(Icons.Default.ArrowBack, "RETOUR") { sendKey(4) }
                ActionPill(Icons.Default.Home, "HOME") { sendKey(3) }
                ActionPill(Icons.Default.VolumeOff, "MUTE") { sendKey(164) }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                // Remplacement du bouton TV par Store (v1.0.8)
                AppButton("STORE") { launchApp("com.android.vending") }
                AppButton("NETFLIX") { launchApp("com.netflix.ninja") }
                AppButton("YOUTUBE") { launchApp("com.google.android.youtube.tv") }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "v1.0.8",
            color = TextGray.copy(alpha = 0.5f),
            fontSize = 9.sp,
            modifier = Modifier.align(Alignment.End).padding(end = 8.dp)
        )
    }
}

@Composable
fun ModernRockerRepeatable(label: String, iconUp: ImageVector, iconDown: ImageVector, keyUp: Int, keyDown: Int, onMiddle: (() -> Unit)?, onKey: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Column(modifier = Modifier.width(56.dp).height(160.dp).clip(RoundedCornerShape(28.dp)).background(SurfaceGray), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f).pointerInput(keyUp) {
                detectTapGestures(onPress = {
                    val job = scope.launch(Dispatchers.IO) { while(isActive) { onKey(keyUp); delay(400) } }
                    tryAwaitRelease(); job.cancel()
                })
            }, contentAlignment = Alignment.Center) { Icon(iconUp, null, tint = Color.White) }

            if (onMiddle != null) {
                Box(modifier = Modifier.size(42.dp).clip(CircleShape).clickable { onMiddle() }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.VolumeOff, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            } else {
                HorizontalDivider(color = DeepBlack, thickness = 2.dp, modifier = Modifier.padding(horizontal = 8.dp))
            }

            Box(modifier = Modifier.fillMaxWidth().weight(1f).pointerInput(keyDown) {
                detectTapGestures(onPress = {
                    val job = scope.launch(Dispatchers.IO) { while(isActive) { onKey(keyDown); delay(400) } }
                    tryAwaitRelease(); job.cancel()
                })
            }, contentAlignment = Alignment.Center) { Icon(iconDown, null, tint = Color.White) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, color = TextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun AppButton(label: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(8.dp), color = SurfaceGray, modifier = Modifier.width(90.dp).height(36.dp)) {
        Box(contentAlignment = Alignment.Center) { Text(label, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun RemoteButton(text: String, onClick: () -> Unit) {
    Box(modifier = Modifier.padding(vertical = 2.dp).size(60.dp).clip(CircleShape).background(SurfaceGray).clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ActionPill(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onClick() }) {
        Surface(shape = RoundedCornerShape(16.dp), color = SurfaceGray, modifier = Modifier.size(width = 80.dp, height = 48.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(24.dp)) }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = TextGray, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}

class AndroidBase64 : AdbBase64 {
    override fun encodeToString(b: ByteArray): String { return Base64.encodeToString(b, Base64.NO_WRAP) }
}
