package com.intellauncher.app

import android.content.Intent
import android.os.BatteryManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val bg = Color(0xFF080B16)
private val panel = Color(0xFF171B2C)
private val accent = Color(0xFFAE8BFF)
private val muted = Color(0xFFB4B5C9)
private val ink = Color(0xFFF5F3FF)
private val blue = Color(0xFF6F91FF)
private val cardBrush = Brush.linearGradient(listOf(Color(0xFF20233A), Color(0xFF141827)))

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   MaterialTheme(colorScheme = darkColorScheme(primary = accent, onPrimary = bg, background = bg, onBackground = ink, surface = panel, onSurface = ink, surfaceVariant = panel, onSurfaceVariant = muted)) {
    Screen()
   }
  }
 }
 @Composable private fun Screen() {
  var page by remember { mutableIntStateOf(1) }
  var drawer by remember { mutableStateOf(false) }
  var query by remember { mutableStateOf("") }
  var drag by remember { mutableFloatStateOf(0f) }
  val apps = remember {
   packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
    .filter { it.activityInfo.packageName != packageName }
    .map { it.loadLabel(packageManager).toString() to it.activityInfo.packageName }
    .distinctBy { it.second }.sortedBy { it.first.lowercase() }
  }
  Column(Modifier.fillMaxSize().background(bg).padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 34.dp)) {
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
    Text("INTEL / LAUNCHER", color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    Text(listOf("WORKSPACE", "HOME", "INTELLIGENCE")[page], color = muted, fontSize = 11.sp)
   }
   Spacer(Modifier.height(16.dp))
   Box(Modifier.weight(1f).fillMaxWidth().pointerInput(page, drawer) {
    detectHorizontalDragGestures(onDragStart = { drag = 0f }, onHorizontalDrag = { _, dx -> drag += dx },
     onDragEnd = {
      if (!drawer) {
       if (drag < -65) page = (page + 1).coerceAtMost(2)
       if (drag > 65) page = (page - 1).coerceAtLeast(0)
      }
      drag = 0f
     })
   }) {
    if (drawer) Column {
     Text("Aplicativos", color = ink, fontSize = 26.sp, fontWeight = FontWeight.Bold)
     OutlinedTextField(query, { query = it }, label = { Text("Buscar aplicativo") }, modifier = Modifier.fillMaxWidth())
     LazyColumn {
      items(apps.filter { it.first.contains(query, ignoreCase = true) }) { app ->
       Row(Modifier.fillMaxWidth().clickable {
        packageManager.getLaunchIntentForPackage(app.second)?.let { startActivity(it) }
       }.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.Apps, null, tint = accent)
        Spacer(Modifier.width(12.dp))
        Text(app.first, color = ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
       }
      }
     }
    } else when(page) {
     0 -> Workspace()
     1 -> Home { drawer = true }
     else -> Intelligence()
    }
   }
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
    repeat(3) { i ->
     Box(Modifier.padding(4.dp).size(if (page == i) 9.dp else 6.dp)
      .background(if (page == i) accent else muted, RoundedCornerShape(50)))
    }
   }
   Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    Nav(Icons.Outlined.CalendarMonth, "Agenda") { page = 0; drawer = false }
    Nav(Icons.Outlined.Home, "Home") { page = 1; drawer = false }
    Nav(Icons.Outlined.Apps, "Apps") { drawer = !drawer }
    Nav(Icons.Outlined.Security, "Intel") { page = 2; drawer = false }
   }
  }
 }
 @Composable private fun Nav(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, action: () -> Unit) {
  Column(Modifier.clickable(onClick = action).padding(5.dp), horizontalAlignment = Alignment.CenterHorizontally) {
   Box(Modifier.size(49.dp).background(cardBrush, RoundedCornerShape(16.dp)).border(1.dp, Color(0xFF343955), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
    Icon(icon, title, tint = accent)
   }
   Text(title, color = muted, fontSize = 11.sp)
  }
 }
 @Composable private fun Tile(title: String, body: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
  Column(Modifier.fillMaxWidth().padding(vertical = 7.dp).background(cardBrush, RoundedCornerShape(20.dp))
   .border(1.dp, Color(0xFF34344F), RoundedCornerShape(20.dp)).padding(17.dp)) {
   Row(verticalAlignment = Alignment.CenterVertically) {
    Icon(icon, null, tint = accent)
    Spacer(Modifier.width(10.dp))
    Text(title, color = ink, fontWeight = FontWeight.SemiBold)
   }
   Spacer(Modifier.height(10.dp))
   Text(body, color = muted, lineHeight = 21.sp)
  }
 }
 @Composable private fun Home(openApps: () -> Unit) {
  Column(Modifier.verticalScroll(rememberScrollState())) {
   Spacer(Modifier.height(35.dp))
   Text(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()), color = ink, fontSize = 58.sp, fontWeight = FontWeight.Light)
   Text(SimpleDateFormat("EEEE, dd MMMM", Locale.getDefault()).format(Date()), color = muted)
   Spacer(Modifier.height(30.dp))
   Tile("Seu espaço", "Bem-vindo ao IntelLauncher • Design Edition", Icons.Outlined.AutoAwesome)
   Spacer(Modifier.height(14.dp))
   Row(Modifier.fillMaxWidth().clickable { openApps() }.background(panel, RoundedCornerShape(25.dp)).padding(18.dp)) {
    Icon(Icons.Outlined.Search, null, tint = accent)
    Spacer(Modifier.width(12.dp))
    Text("Buscar aplicativos", color = muted)
   }
   Tile("Navegação inteligente", "Workspace  •  Home  •  Intelligence", Icons.Outlined.DashboardCustomize)
  }
 }
 @Composable private fun Workspace() {
  Column(Modifier.verticalScroll(rememberScrollState())) {
   Text("Workspace", color = ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
   Text("Agenda • Tarefas • Projetos", color = muted)
   Spacer(Modifier.height(16.dp))
   Tile("Sua agenda", "Compromissos e calendário em breve.", Icons.Outlined.CalendarMonth)
   Tile("Prioridades", "Organize tarefas e lembretes neste espaço.", Icons.Outlined.CheckCircleOutline)
   Tile("Compromissos", "Seus eventos aparecerão aqui futuramente.", Icons.Outlined.EventNote)
  }
 }
 @Composable private fun Intelligence() {
  val battery = remember {
   (getSystemService(BATTERY_SERVICE) as BatteryManager).getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
  }
  Column(Modifier.verticalScroll(rememberScrollState())) {
   Text("Intelligence", color = ink, fontSize = 29.sp, fontWeight = FontWeight.Bold)
   Text("Threat Intel • Riscos • Sistema", color = muted)
   Spacer(Modifier.height(12.dp))
   Tile("Visão de ameaças", "Módulo em preparação • sem dados ao vivo", Icons.Outlined.Shield)
   Tile("Vulnerabilidades", "CISA KEV  •  NVD  •  EPSS\nIntegrações futuras", Icons.Outlined.BugReport)
   Tile("Saúde do dispositivo", "Bateria: " + (if (battery in 0..100) "$battery%" else "indisponível"), Icons.Outlined.BatteryFull)
   Tile("Fontes", "Os dados de CTI ainda não estão conectados.", Icons.Outlined.Info)
  }
 }
}