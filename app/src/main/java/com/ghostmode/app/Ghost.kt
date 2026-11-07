package com.ghostmode.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.runtime.*

const val DAY = 86_400_000L

data class AppInfo(val pkg: String, val label: String)
data class Contact(val id: String, val name: String)

object Ghost {
    private var sp: SharedPreferences? = null
    var active by mutableStateOf(false)
    var startMs by mutableLongStateOf(0L)
    var days by mutableIntStateOf(90)
    var calls by mutableStateOf(true)
    var sms by mutableStateOf(true)
    var notifs by mutableStateOf(true)
    val apps = mutableStateListOf<String>()
    val contacts = mutableStateListOf<String>()
    val essentials = mutableStateListOf<String>()

    // Never blocked, whatever the user selects.
    private val always = setOf(
        "com.ghostmode.app", "com.android.systemui", "com.android.phone",
        "com.android.dialer", "com.google.android.dialer", "com.android.emergency"
    )

    val endMs get() = startMs + days * DAY

    fun init(c: Context) {
        if (sp != null) return
        val p = c.applicationContext.getSharedPreferences("ghost", Context.MODE_PRIVATE)
        sp = p
        active = p.getBoolean("active", false)
        startMs = p.getLong("start", 0L)
        days = p.getInt("days", 90)
        calls = p.getBoolean("calls", true)
        sms = p.getBoolean("sms", true)
        notifs = p.getBoolean("notifs", true)
        apps.addAll(p.getStringSet("apps", emptySet()) ?: emptySet())
        contacts.addAll(p.getStringSet("contacts", emptySet()) ?: emptySet())
        essentials.addAll(p.getStringSet("ess", emptySet()) ?: emptySet())
    }

    fun save() {
        sp?.edit()
            ?.putBoolean("active", active)?.putLong("start", startMs)?.putInt("days", days)
            ?.putBoolean("calls", calls)?.putBoolean("sms", sms)?.putBoolean("notifs", notifs)
            ?.putStringSet("apps", apps.toSet())?.putStringSet("contacts", contacts.toSet())
            ?.putStringSet("ess", essentials.toSet())?.apply()
    }

    fun enforcing(now: Long = System.currentTimeMillis()) = active && startMs > 0 && now in startMs until endMs
    fun shouldBlock(pkg: String) = enforcing() && pkg in apps && pkg !in essentials && pkg !in always

    fun toggle(list: MutableList<String>, v: String) { if (!list.remove(v)) list.add(v); save() }
    fun setAll(list: MutableList<String>, items: List<String>, on: Boolean) {
        items.forEach { list.remove(it); if (on) list.add(it) }
        save()
    }
    fun activate(d: Int) { days = d; startMs = System.currentTimeMillis(); active = true; save() }
    fun stop() { active = false; save() }
    fun extend() { days += 30; save() }
    fun setFlags(c: Boolean = calls, s: Boolean = sms, n: Boolean = notifs) { calls = c; sms = s; notifs = n; save() }
    fun reset() {
        active = false; startMs = 0L; days = 90; calls = true; sms = true; notifs = true
        apps.clear(); contacts.clear(); essentials.clear()
        sp?.edit()?.clear()?.apply()
    }
}

fun loadApps(c: Context): List<AppInfo> {
    val pm = c.packageManager
    val i = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(i, 0)
        .map { AppInfo(it.activityInfo.packageName, it.loadLabel(pm).toString()) }
        .filter { it.pkg != c.packageName }
        .distinctBy { it.pkg }
        .sortedBy { it.label.lowercase() }
}

fun loadContacts(c: Context): List<Contact> {
    val out = mutableListOf<Contact>()
    c.contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
        null, null, ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
    )?.use { cur ->
        while (cur.moveToNext()) out.add(Contact(cur.getString(0), cur.getString(1) ?: ""))
    }
    return out
}

fun isBlockedNumber(c: Context, number: String): Boolean {
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number))
    c.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup._ID), null, null, null)?.use { cur ->
        while (cur.moveToNext()) if (cur.getString(0) in Ghost.contacts) return true
    }
    return false
}
