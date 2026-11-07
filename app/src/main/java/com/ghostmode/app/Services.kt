package com.ghostmode.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import android.view.accessibility.AccessibilityEvent

class GhostAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() { Ghost.init(this) }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        if (Ghost.shouldBlock(pkg)) {
            startActivity(
                Intent(this, BlockActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }
    }

    override fun onInterrupt() {}
}

class GhostCallScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        Ghost.init(this)
        val number = details.handle?.schemeSpecificPart
        val block = Ghost.enforcing() && Ghost.calls && number != null &&
            details.callDirection == Call.Details.DIRECTION_INCOMING && isBlockedNumber(this, number)
        respondToCall(
            details,
            CallResponse.Builder()
                .setDisallowCall(block).setRejectCall(block)
                .setSkipCallLog(false).setSkipNotification(block).build()
        )
    }
}
