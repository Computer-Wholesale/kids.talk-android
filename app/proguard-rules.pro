# ──────────────────────────────────────────────────────────────────────────────
# Kids.Talk — R8/ProGuard keep rules (KID-393)
#
# The Linphone SDK AAR ships its own comprehensive consumer rules (proguard.txt,
# ~23 KB) that keep all org.linphone.core.* interfaces and implementations.
# This file covers the APP-LEVEL classes that R8 cannot infer from code alone.
# ──────────────────────────────────────────────────────────────────────────────

# ── Preserve line numbers for crash symbolication ─────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Android Manifest components (instantiated by the system via reflection) ───
# Services
-keep class org.linphone.core.CoreInCallService { *; }
-keep class org.linphone.core.CorePushService { *; }
-keep class org.linphone.core.CoreFileTransferService { *; }
-keep class org.linphone.core.CoreKeepAliveThirdPartyAccountsService { *; }
-keep class org.linphone.telecom.auto.AndroidAutoService { *; }
-keep class org.linphone.telecom.TelecomConnectionService { *; }
-keep class org.linphone.telecom.TelecomRedirectionService { *; }

# BroadcastReceivers
-keep class org.linphone.core.CorePushReceiver { *; }
-keep class org.linphone.notifications.NotificationBroadcastReceiver { *; }
-keep class org.linphone.core.BootReceiver { *; }

# ── Firebase Cloud Messaging ──────────────────────────────────────────────────
-keep class com.google.firebase.messaging.FirebaseMessagingService { *; }
-keep class * extends com.google.firebase.messaging.FirebaseMessagingService { *; }

# ── Data Binding generated classes ────────────────────────────────────────────
-keep class org.linphone.databinding.** { *; }

# ── Navigation Safe Args (generated Directions classes) ───────────────────────
-keep class * extends androidx.navigation.NavArgs { *; }
-keep class **Directions { *; }
-keep class **Directions$* { *; }

# ── Kotlin serialization / reflection ─────────────────────────────────────────
-keepclassmembers class * {
    @kotlin.Metadata *;
}
-keep class kotlin.Metadata { *; }

# ── Enums (used by Linphone SDK callbacks) ────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ── Parcelable (used by TelecomManager) ───────────────────────────────────────
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ── Suppress warnings for optional dependencies ──────────────────────────────
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
