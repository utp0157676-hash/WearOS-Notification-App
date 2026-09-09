package com.example.menuapp.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.wear.compose.material.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

enum class Screen { MENU, ESTADISTICAS_AGUA, ESTADISTICAS_SUENO, ESTADISTICAS_RUNNING, ESTADISTICAS_CUERPO }

class MainActivity : ComponentActivity() {

    private var heartRateState = mutableStateOf("72 BPM")
    private var stepsState = mutableStateOf("4,480")
    private var spo2State = mutableStateOf("98%")

    private var runTimeState = mutableStateOf("00:00:00")
    private val kmHistory = mutableStateListOf(5.2f, 3.1f, 4.5f, 2.0f, 6.2f)

    private var weightState = mutableStateOf(65.0f)
    private var heightState = mutableStateOf(165)
    private var caloriesState = mutableStateOf(663f)
    private val calorieGoal = 2000f

    private var currentWaterLiters = mutableStateOf(2.7f)
    private val waterHistory = mutableStateListOf(1.5f, 2.0f, 1.2f, 2.5f, 2.7f)
    private val sleepHistory = mutableStateListOf(7.0f, 6.5f, 8.0f, 5.5f, 7.2f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        setContent {
            val context = LocalContext.current
            var currentScreen by remember { mutableStateOf(Screen.MENU) }

            val launcherPermiso = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { _ -> }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        launcherPermiso.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }
                while(true) {
                    delay(3000)
                    heartRateState.value = "${(70..120).random()} BPM"
                    val currentSteps = stepsState.value.replace(",", "").toInt()
                    stepsState.value = String.format(Locale.US, "%,d", currentSteps + (1..4).random())
                    if(caloriesState.value < calorieGoal) {
                        caloriesState.value += 1.2f
                    }
                }
            }

            MaterialTheme {
                when (currentScreen) {
                    Screen.MENU -> MainMenuScreen(
                        heartRate = heartRateState.value, steps = stepsState.value,
                        water = String.format(Locale.US, "%.1fL", currentWaterLiters.value),
                        onOptionClick = { route ->
                            when (route) {
                                "ver_sueno" -> currentScreen = Screen.ESTADISTICAS_SUENO
                                "ver_agua" -> currentScreen = Screen.ESTADISTICAS_AGUA
                                "ver_running" -> currentScreen = Screen.ESTADISTICAS_RUNNING
                                "ver_cuerpo" -> currentScreen = Screen.ESTADISTICAS_CUERPO
                            }
                        }
                    )
                    Screen.ESTADISTICAS_AGUA -> WaterStatsScreen(currentWaterLiters.value, waterHistory, { currentWaterLiters.value += it; waterHistory[4] = currentWaterLiters.value }, { currentScreen = Screen.MENU })
                    Screen.ESTADISTICAS_SUENO -> SleepStatsScreen(sleepHistory, { currentScreen = Screen.MENU })
                    Screen.ESTADISTICAS_RUNNING -> RunningStatsScreen(kmHistory, { currentScreen = Screen.MENU })
                    Screen.ESTADISTICAS_CUERPO -> BodyStatsScreen(weightState.value, heightState.value, caloriesState.value, calorieGoal, { weightState.value += it }, { heightState.value += it }, { currentScreen = Screen.MENU })
                }
            }
        }
    }

    @SuppressLint("NotificationPermission")
    private fun enviarNotificacionWearable(titulo: String, contenido: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = NotificationCompat.Builder(this, "wear_channels")
            .setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(titulo).setContentText(contenido)
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
        notificationManager.notify(1, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("wear_channels", "Alertas", NotificationManager.IMPORTANCE_HIGH)
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
    }
}

// ---------------- COMPONENTES DE PANTALLA ----------------

@Composable
fun MainMenuScreen(heartRate: String, steps: String, water: String, onOptionClick: (String) -> Unit) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        item { ListHeader { Text("FITNESS-OS", color = Color(0xFF2DD4BF), fontWeight = FontWeight.Bold) } }
        item { Chip(modifier = Modifier.fillMaxWidth(), onClick = { onOptionClick("sensor_corazon") }, label = { Text("Pulso") }, secondaryLabel = { Text(heartRate, color = Color.Red) }, icon = { Icon(Icons.Default.Favorite, null, tint = Color.Red) }) }
        item { Chip(modifier = Modifier.fillMaxWidth(), onClick = { onOptionClick("ver_running") }, label = { Text("Running") }, secondaryLabel = { Text("$steps pasos", color = Color.Green) }, icon = { Icon(Icons.Default.DirectionsRun, null, tint = Color.Green) }) }
        item { Chip(modifier = Modifier.fillMaxWidth(), onClick = { onOptionClick("ver_cuerpo") }, label = { Text("Cuerpo/Calorías") }, secondaryLabel = { Text("Peso/Altura >") }, icon = { Icon(Icons.Default.MonitorWeight, null, tint = Color.Yellow) }) }
        item { Chip(modifier = Modifier.fillMaxWidth(), onClick = { onOptionClick("ver_agua") }, label = { Text("Agua") }, secondaryLabel = { Text(water, color = Color.Cyan) }, icon = { Icon(Icons.Default.LocalDrink, null, tint = Color.Cyan) }) }
        item { Chip(modifier = Modifier.fillMaxWidth(), onClick = { onOptionClick("ver_sueno") }, label = { Text("Sueño") }, secondaryLabel = { Text("Ver Historial") }, icon = { Icon(Icons.Default.Bedtime, null, tint = Color.Magenta) }) }
    }
}

@Composable
fun RunningStatsScreen(history: List<Float>, onBack: () -> Unit) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF062010)), horizontalAlignment = Alignment.CenterHorizontally) {
        item { Icon(Icons.Default.ArrowBack, null, modifier = Modifier.clickable { onBack() }.size(24.dp).padding(bottom = 4.dp), tint = Color.White) }
        item { Text("Km por Día", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        item {
            Row(modifier = Modifier.height(60.dp).fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom) {
                history.forEach { km ->
                    Box(modifier = Modifier.width(12.dp).fillMaxHeight((km / 10f).coerceIn(0.1f, 1f)).background(Color.Green, RoundedCornerShape(2.dp)))
                }
            }
        }
        item { Text("Promedio Semanal: 4.2 km", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp)) }
    }
}

@Composable
fun BodyStatsScreen(weight: Float, height: Int, burned: Float, goal: Float, onWeightChange: (Float) -> Unit, onHeightChange: (Int) -> Unit, onBack: () -> Unit) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A)), horizontalAlignment = Alignment.CenterHorizontally) {
        item { Icon(Icons.Default.ArrowBack, null, modifier = Modifier.clickable { onBack() }.size(24.dp)) }
        item {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(75.dp).padding(vertical = 4.dp)) {
                val progress = (burned / goal).coerceIn(0f, 1f)
                Canvas(modifier = Modifier.size(70.dp)) {
                    drawArc(Color(0xFF333333), 0f, 360f, false, style = Stroke(6.dp.toPx()))
                    drawArc(Color(0xFFFFB300), -90f, progress * 360f, false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${burned.toInt()}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("kcal", fontSize = 8.sp, color = Color.Gray)
                }
            }
        }
        item { Text("Registro Corporal", fontSize = 11.sp, color = Color(0xFFFFB300), fontWeight = FontWeight.Bold) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                CompactButton(onClick = { onWeightChange(-0.5f) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333))) { Text("-", color = Color.White) }
                Text(String.format(Locale.US, " %.1f kg ", weight), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                CompactButton(onClick = { onWeightChange(0.5f) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333))) { Text("+", color = Color.White) }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(top = 2.dp)) {
                CompactButton(onClick = { onHeightChange(-1) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333))) { Text("-", color = Color.White) }
                Text(" $height cm ", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                CompactButton(onClick = { onHeightChange(1) }, colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF333333))) { Text("+", color = Color.White) }
            }
        }
    }
}

@Composable
fun WaterStatsScreen(current: Float, history: List<Float>, onAdd: (Float) -> Unit, onBack: () -> Unit) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF001520)), horizontalAlignment = Alignment.CenterHorizontally) {
        item { Icon(Icons.Default.ArrowBack, null, modifier = Modifier.clickable { onBack() }.size(24.dp)) }
        item { Text(String.format(Locale.US, "%.1fL Hoy", current), fontWeight = FontWeight.Bold, fontSize = 14.sp) }
        item {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(vertical = 4.dp)) {
                CompactButton(onClick = { onAdd(0.25f) }) { Text("+¼L", fontSize = 9.sp) }
                Spacer(Modifier.width(8.dp))
                CompactButton(onClick = { onAdd(0.5f) }) { Text("+½L", fontSize = 9.sp) }
            }
        }
        item {
            Row(modifier = Modifier.height(45.dp).fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom) {
                history.forEach { Box(modifier = Modifier.width(10.dp).fillMaxHeight((it / 3f).coerceIn(0.1f, 1f)).background(Color.Cyan, RoundedCornerShape(1.dp))) }
            }
        }
    }
}

@Composable
fun SleepStatsScreen(history: List<Float>, onBack: () -> Unit) {
    ScalingLazyColumn(modifier = Modifier.fillMaxSize().background(Color(0xFF100020)), horizontalAlignment = Alignment.CenterHorizontally) {
        item { Icon(Icons.Default.ArrowBack, null, modifier = Modifier.clickable { onBack() }.size(24.dp)) }
        item { Text("Rendimiento Sueño", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFA78BFA)) }
        item { Text("Promedio: 6.8 hrs", fontSize = 11.sp, color = Color.White, modifier = Modifier.padding(vertical = 2.dp)) }
        item {
            Row(modifier = Modifier.height(55.dp).fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceAround, verticalAlignment = Alignment.Bottom) {
                history.forEach { Box(modifier = Modifier.width(11.dp).fillMaxHeight((it / 10f).coerceIn(0.1f, 1f)).background(Color(0xFFA78BFA), RoundedCornerShape(2.dp))) }
            }
        }
    }
}