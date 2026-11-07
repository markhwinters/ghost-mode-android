package com.ghostmode.app

import android.Manifest
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val Obs = Color(0xFF08090D); val Gra = Color(0xFF151820); val Gun = Color(0xFF252B35)
val Vio = Color(0xFF8065FF); val Ice = Color(0xFF83BFFF); val Sil = Color(0xFFC5CBD4)
val Ok = Color(0xFF3DDC84); val Bad = Color(0xFFFF4D5E); val Amb = Color(0xFFFFB02E)
val Cut = CutCornerShape(topEnd = 14.dp, bottomStart = 14.dp)

@Composable
fun GhostTheme(content: @Composable () -> Unit) = MaterialTheme(
    colorScheme = darkColorScheme(primary = Vio, secondary = Ice, background = Obs, surface = Gra, onBackground = Sil, onSurface = Sil),
    content = content
)

@Composable
fun GCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 6.dp)
            .border(1.dp, Brush.linearGradient(listOf(Ice.copy(alpha = .55f), Ice.copy(alpha = .15f))), Cut)
            .background(Gra, Cut).padding(16.dp),
        content = content
    )
}

@Composable
fun GSwitch(on: Boolean, allow: Boolean = false, onChange: (Boolean) -> Unit) = Switch(
    checked = on, onCheckedChange = onChange,
    colors = SwitchDefaults.colors(
        checkedTrackColor = if (allow) Ok else Bad, checkedThumbColor = Color.White, checkedBorderColor = Color.Transparent,
        uncheckedTrackColor = Gun, uncheckedThumbColor = Sil, uncheckedBorderColor = Color.Transparent
    )
)

@Composable fun Title(t: String) = Text(t, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, modifier = Modifier.padding(vertical = 12.dp))

fun fmt(ms: Long): String = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(ms))

fun a11yOn(c: Context): Boolean =
    (Settings.Secure.getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "").contains(c.packageName + "/")

@Composable
fun GhostApp() {
    var tab by remember { mutableIntStateOf(0) }
    val items = listOf("HOME" to Icons.Default.Home, "APPS" to Icons.Default.Menu, "CONTACTS" to Icons.Default.Person,
        "PROGRESS" to Icons.Default.CheckCircle, "SETTINGS" to Icons.Default.Settings)
    Scaffold(containerColor = Obs, bottomBar = {
        NavigationBar(containerColor = Gra) {
            items.forEachIndexed { i, (l, ic) ->
                NavigationBarItem(
                    selected = tab == i, onClick = { tab = i }, icon = { Icon(ic, l) }, label = { Text(l, fontSize = 9.sp) },
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Ice, selectedTextColor = Ice, indicatorColor = Gun,
                        unselectedIconColor = Sil.copy(alpha = .5f), unselectedTextColor = Sil.copy(alpha = .5f))
                )
            }
        }
    }) { pad ->
        Box(Modifier.padding(pad).statusBarsPadding().fillMaxSize()) {
            when (tab) { 0 -> Home(); 1 -> AppsScreen(false); 2 -> ContactsScreen(); 3 -> Progress(); else -> SettingsScreen() }
        }
    }
}

@Composable
fun Confirm(text: String, onYes: () -> Unit, onNo: () -> Unit) = AlertDialog(
    onDismissRequest = onNo, containerColor = Gra, titleContentColor = Color.White, textContentColor = Sil,
    title = { Text("Confirm") }, text = { Text(text) },
    confirmButton = { TextButton(onClick = onYes) { Text("Yes", color = Bad) } },
    dismissButton = { TextButton(onClick = onNo) { Text("Cancel", color = Ice) } }
)

@Composable
fun Home() {
    val ctx = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = System.currentTimeMillis() } }
    var pick by remember { mutableIntStateOf(30) }
    var custom by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    val started = Ghost.active && Ghost.startMs > 0
    val left = if (started) (Ghost.endMs - now).coerceAtLeast(0L) else 0L
    val state = when { !started -> "STANDBY"; now < Ghost.startMs -> "ARMED"; now >= Ghost.endMs -> "COMPLETE"; else -> "ACTIVE" }
    val shown = if (started) ((left + DAY - 1) / DAY).toInt() else (custom.toIntOrNull() ?: pick)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("GHOST MODE", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
        Text("Disappear from the noise. Reappear for yourself.", color = Sil.copy(alpha = .6f), fontSize = 11.sp)
        Spacer(Modifier.height(24.dp))
        Text("SYSTEM STATUS: $state", color = if (state == "ACTIVE") Ok else Amb, fontSize = 12.sp, letterSpacing = 2.sp)
        Text("$shown", color = Color.White, fontSize = 88.sp, fontWeight = FontWeight.Bold)
        Text("DAYS", color = Ice, letterSpacing = 8.sp)
        if (started) {
            Spacer(Modifier.height(8.dp))
            Text("${left / DAY}d ${left % DAY / 3_600_000}h ${left % 3_600_000 / 60_000}m ${left % 60_000 / 1000}s", color = Sil)
        }
        Text("Your time. Your attention. Your life.", color = Sil.copy(alpha = .6f), fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp))
        Spacer(Modifier.height(16.dp))
        GCard {
            if (started) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Start: ${fmt(Ghost.startMs)}"); Text("End: ${fmt(Ghost.endMs)}")
                }
                Spacer(Modifier.height(8.dp))
                Text("Completed: ${((now - Ghost.startMs) / DAY).coerceIn(0L, Ghost.days.toLong())} days")
            } else {
                Text("Commitment length", color = Color.White)
                Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(7, 14, 30, 60, 90).forEach { d ->
                        FilterChip(selected = custom.isEmpty() && pick == d, onClick = { pick = d; custom = "" }, label = { Text("$d days") })
                    }
                }
                OutlinedTextField(custom, { custom = it.filter(Char::isDigit).take(3) }, label = { Text("Custom days") }, singleLine = true)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GCard(Modifier.weight(1f)) { Text("${Ghost.apps.size}", color = Bad, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("apps blocked", fontSize = 12.sp) }
            GCard(Modifier.weight(1f)) { Text("${Ghost.contacts.size}", color = Bad, fontSize = 28.sp, fontWeight = FontWeight.Bold); Text("contacts blocked", fontSize = 12.sp) }
        }
        if (!a11yOn(ctx)) {
            Text("Blocking service is off. Enable it in Settings so apps can be restricted.", color = Amb, fontSize = 12.sp, modifier = Modifier.padding(vertical = 8.dp))
        }
        if (started) {
            Button(onClick = { confirm = true }, colors = ButtonDefaults.buttonColors(containerColor = Amb, contentColor = Obs), shape = Cut) { Text("EMERGENCY OVERRIDE") }
        } else {
            Button(onClick = { Ghost.activate((custom.toIntOrNull() ?: pick).coerceIn(1, 365)) }, shape = Cut) { Text("ACTIVATE GHOST MODE") }
        }
    }
    if (confirm) Confirm("Pause all restrictions now?", { Ghost.stop(); confirm = false }, { confirm = false })
}

@Composable
fun AppsScreen(essentialMode: Boolean) {
    val ctx = LocalContext.current
    val all = remember { loadApps(ctx) }
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<String?>(null) }
    val list = if (essentialMode) Ghost.essentials else Ghost.apps
    val shown = all.filter { it.label.contains(q, true) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Title(if (essentialMode) "ESSENTIAL APPS" else "APPS")
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search apps") }, singleLine = true)
        Row {
            TextButton(onClick = { Ghost.setAll(list, shown.map { it.pkg }, true) }) { Text(if (essentialMode) "Allow all shown" else "Block all shown", color = Ice) }
            TextButton(onClick = { if (Ghost.enforcing() && !essentialMode) pending = "*" else Ghost.setAll(list, shown.map { it.pkg }, false) }) { Text("Clear shown", color = Ice) }
        }
        LazyColumn {
            items(shown, key = { it.pkg }) { a ->
                val icon = remember(a.pkg) { ctx.packageManager.getApplicationIcon(a.pkg).toBitmap(80, 80).asImageBitmap() }
                val on = a.pkg in list
                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(icon, null, Modifier.size(36.dp))
                    Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                        Text(a.label, color = Color.White)
                        val ess = a.pkg in Ghost.essentials && !essentialMode
                        Text(if (essentialMode) (if (on) "Always allowed" else "Not essential") else if (ess) "Essential, always allowed" else if (on) "Blocked" else "Allowed",
                            fontSize = 11.sp, color = if (on && !essentialMode) Bad else Sil.copy(alpha = .5f))
                    }
                    GSwitch(on, allow = essentialMode) { want ->
                        if (!want && Ghost.enforcing() && !essentialMode) pending = a.pkg else Ghost.toggle(list, a.pkg)
                    }
                }
            }
        }
    }
    pending?.let { p ->
        Confirm("GHOST MODE is active. Remove this restriction?", {
            if (p == "*") Ghost.setAll(list, shown.map { it.pkg }, false) else Ghost.toggle(list, p)
            pending = null
        }, { pending = null })
    }
}

@Composable
fun ContactsScreen() {
    val ctx = LocalContext.current
    var granted by remember { mutableStateOf(ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) }
    val ask = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted = it }
    var q by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<String?>(null) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        Title("CONTACTS")
        if (!granted) {
            Text("GHOST MODE reads your contacts only to let you pick who to block. Nothing leaves your device.", color = Sil)
            Button(onClick = { ask.launch(Manifest.permission.READ_CONTACTS) }, Modifier.padding(top = 12.dp), shape = Cut) { Text("Allow contacts access") }
            return@Column
        }
        val all = remember(granted) { loadContacts(ctx) }
        val shown = all.filter { it.name.contains(q, true) }
        OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search contacts") }, singleLine = true)
        Row {
            TextButton(onClick = { Ghost.setAll(Ghost.contacts, shown.map { it.id }, true) }) { Text("Block all shown", color = Ice) }
            TextButton(onClick = { if (Ghost.enforcing()) pending = "*" else Ghost.setAll(Ghost.contacts, shown.map { it.id }, false) }) { Text("Clear shown", color = Ice) }
        }
        LazyColumn {
            items(shown, key = { it.id }) { c ->
                val on = c.id in Ghost.contacts
                Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(c.name, color = Color.White)
                        Text(if (on) "Blocked" else "Allowed", fontSize = 11.sp, color = if (on) Bad else Sil.copy(alpha = .5f))
                    }
                    GSwitch(on) { want -> if (!want && Ghost.enforcing()) pending = c.id else Ghost.toggle(Ghost.contacts, c.id) }
                }
            }
        }
        pending?.let { p ->
            Confirm("GHOST MODE is active. Remove this restriction?", {
                if (p == "*") Ghost.setAll(Ghost.contacts, shown.map { it.id }, false) else Ghost.toggle(Ghost.contacts, p)
                pending = null
            }, { pending = null })
        }
    }
}

@Composable
fun Progress() {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (true) { delay(1000); now = System.currentTimeMillis() } }
    val started = Ghost.active && Ghost.startMs > 0
    val span = (Ghost.endMs - Ghost.startMs).coerceAtLeast(1L)
    val pct = if (started) ((now - Ghost.startMs).toFloat() / span).coerceIn(0f, 1f) else 0f
    val done = if (started) ((now - Ghost.startMs) / DAY).coerceIn(0L, Ghost.days.toLong()).toInt() else 0
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Title("PROGRESS")
        Box(Modifier.size(180.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize().padding(10.dp)) {
                drawArc(Gun, -90f, 360f, false, style = Stroke(16f))
                drawArc(Vio, -90f, 360f * pct, false, style = Stroke(16f, cap = StrokeCap.Butt))
            }
            Text("${(pct * 100).toInt()}%", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(16.dp))
        GCard {
            Text("Days completed: $done"); Text("Days remaining: ${if (started) Ghost.days - done else Ghost.days}")
            Text("App restrictions: ${Ghost.apps.size}"); Text("Contact restrictions: ${Ghost.contacts.size}")
        }
        if (started && now >= Ghost.endMs) Button(onClick = { Ghost.extend() }, shape = Cut) { Text("EXTEND 30 DAYS") }
    }
}

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    var sub by remember { mutableStateOf("") }
    var wipe by remember { mutableStateOf(false) }
    if (sub == "ess") {
        Column {
            TextButton(onClick = { sub = "" }, Modifier.padding(start = 12.dp)) { Text("< Settings", color = Ice) }
            AppsScreen(true)
        }
        return
    }
    val rm = ctx.getSystemService(RoleManager::class.java)
    val role = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Title("SETTINGS")
        GCard {
            Text("Permissions", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Blocking service: ${if (a11yOn(ctx)) "ON" else "OFF"}", color = if (a11yOn(ctx)) Ok else Amb)
            TextButton(onClick = { ctx.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("Open accessibility settings", color = Ice) }
            Text("Call screening: ${if (rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) "ON" else "OFF"}", color = if (rm.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) Ok else Amb)
            TextButton(onClick = {
                if (rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) role.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
            }) { Text("Set GHOST MODE as call screener", color = Ice) }
        }
        GCard {
            Text("Communication controls", color = Color.White, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Incoming calls"); Text("Rejects calls from blocked contacts", fontSize = 11.sp, color = Sil.copy(alpha = .5f)) }; GSwitch(Ghost.calls) { Ghost.setFlags(c = it) } }
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("SMS messages"); Text("Setting saved. Not enforced yet", fontSize = 11.sp, color = Amb) }; GSwitch(Ghost.sms) { Ghost.setFlags(s = it) } }
            Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Messaging notifications"); Text("Setting saved. Not enforced yet", fontSize = 11.sp, color = Amb) }; GSwitch(Ghost.notifs) { Ghost.setFlags(n = it) } }
        }
        GCard {
            Text("Essential apps", color = Color.White, fontWeight = FontWeight.Bold)
            Text("Apps that are never blocked. Phone and emergency calling are always allowed.", fontSize = 12.sp)
            TextButton(onClick = { sub = "ess" }) { Text("Manage essential apps", color = Ice) }
        }
        GCard {
            Text("Privacy", color = Color.White, fontWeight = FontWeight.Bold)
            Text("All data stays on this device. No account, no analytics, no ads.", fontSize = 12.sp)
            TextButton(onClick = { wipe = true }) { Text("Delete all data", color = Bad) }
        }
    }
    if (wipe) Confirm("Delete all GHOST MODE data and restrictions?", { Ghost.reset(); wipe = false }, { wipe = false })
}
