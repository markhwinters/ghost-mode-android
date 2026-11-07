# GHOST MODE (Android, Kotlin + Jetpack Compose)

Build: push to GitHub, open Actions > Build APK > download the `ghost-mode-debug-apk` artifact.
Local: `gradle assembleDebug` (JDK 17, Android SDK 34).

Setup on the phone: install APK, Home shows what is missing.
1. Settings > Open accessibility settings > enable GHOST MODE (app blocking).
2. Settings > Set GHOST MODE as call screener (call blocking).
3. Contacts tab > allow contacts access.

Enforced: app blocking (accessibility service shows the block screen), incoming call rejection for blocked contacts, commitment countdown that survives restarts (stored dates, no background timer), confirmation before lifting a restriction, emergency override, essential apps.
Not enforced yet: SMS blocking, messaging notification suppression (saved settings only), PIN, cooldown.
