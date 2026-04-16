package com.example.matelecommandefree

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
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
import kotlinx.coroutines.*

val DeepBlack = Color(0xFF0A0B0D)
val SurfaceGray = Color(0xFF1C1E22)
val AccentRed = Color(0xFFE50914)
val TextGray = Color(0xFF90949C)
val GlowBlue = Color(0xFF4285F4)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    val adbManager = remember { AdbManager(context) }

    fun sendKey(code: Int) = adbManager.sendKey(code)
    fun launchApp(pkg: String) = adbManager.launchApp(pkg)

    @Composable
    fun RepeatingKey(code: Int, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
        Box(
            modifier = modifier
                .size(56.dp)
                .pointerInput(code) {
                    detectTapGestures(
                        onPress = {
                            val job = scope.launch(Dispatchers.IO) {
                                var startTime = System.currentTimeMillis()
                                while (isActive) {
                                    val elapsed = System.currentTimeMillis() - startTime
                                    val count = when {
                                        elapsed > 6000 -> 12 // Vitesse turbo après 6s
                                        elapsed > 3000 -> 5  // Vitesse rapide après 3s
                                        else -> 1            // Vitesse normale
                                    }
                                    adbManager.sendKey(code, count)
                                    // On garde un délai stable pour ne pas saturer l'ADB
                                    delay(if (count > 1) 250 else 300)
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
                onClick = { adbManager.connect() },
                shape = RoundedCornerShape(24.dp),
                color = SurfaceGray,
                modifier = Modifier.height(40.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).background(if(adbManager.isConnected) Color.Green else Color.Red, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(if(adbManager.isConnected) "FREEBOX" else "DECONNECTE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(adbManager.statusMessage, color = TextGray, fontSize = 8.sp)
                    }
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

            Box(modifier = Modifier.size(200.dp), contentAlignment = Alignment.Center) {
                Box(modifier = Modifier.fillMaxSize().border(1.dp, Color.White.copy(alpha = 0.05f), CircleShape).background(SurfaceGray, CircleShape))
                RepeatingKey(19, Modifier.align(Alignment.TopCenter).padding(top = 10.dp)) { Icon(Icons.Default.KeyboardArrowUp, null, tint = TextGray, modifier = Modifier.size(36.dp)) }
                RepeatingKey(20, Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp)) { Icon(Icons.Default.KeyboardArrowDown, null, tint = TextGray, modifier = Modifier.size(36.dp)) }
                RepeatingKey(21, Modifier.align(Alignment.CenterStart).padding(start = 10.dp)) { Icon(Icons.Default.KeyboardArrowLeft, null, tint = TextGray, modifier = Modifier.size(36.dp)) }
                RepeatingKey(22, Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)) { Icon(Icons.Default.KeyboardArrowRight, null, tint = TextGray, modifier = Modifier.size(36.dp)) }
                Surface(
                    onClick = { sendKey(23) }, shape = CircleShape, color = SurfaceGray,
                    modifier = Modifier.size(70.dp).border(2.dp, Brush.radialGradient(listOf(GlowBlue, Color.Transparent)), CircleShape),
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
                AppButton("STORE") { launchApp("com.android.vending") }
                AppButton("NETFLIX") { launchApp("com.netflix.ninja") }
                AppButton("YOUTUBE") { launchApp("com.google.android.youtube.tv") }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "v2.0.0 (Wi-Fi Auto)",
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
                    val job = scope.launch(Dispatchers.IO) {
                        var currentDelay = 400L
                        while(isActive) {
                            onKey(keyUp)
                            delay(currentDelay)
                            if (currentDelay > 100) currentDelay -= 50
                        }
                    }
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
                    val job = scope.launch(Dispatchers.IO) {
                        var currentDelay = 400L
                        while(isActive) {
                            onKey(keyDown)
                            delay(currentDelay)
                            if (currentDelay > 100) currentDelay -= 50
                        }
                    }
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
