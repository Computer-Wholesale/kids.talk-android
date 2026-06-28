/*
 * Kids.Talk: RingerModeGuard
 *
 * Monitors AudioManager.ACTION_RINGER_MODE_CHANGED during an active call and
 * immediately restores the ringer mode to what it was before the call started.
 *
 * This is necessary because the Linphone SDK's native audio layer (and on some OEM
 * devices, Android itself) changes the ringer mode from SILENT/VIBRATE to NORMAL
 * when a call starts. This class prevents that from happening without user consent.
 */
package org.linphone.core

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import org.linphone.core.tools.Log

class RingerModeGuard(private val context: Context) {

    companion object {
        private const val TAG = "[RingerModeGuard]"
        // AudioManager.ACTION_RINGER_MODE_CHANGED = "android.media.RINGER_MODE_CHANGED"
        private const val ACTION_RINGER_MODE_CHANGED = "android.media.RINGER_MODE_CHANGED"
    }

    private var savedRingerMode: Int = -1
    private var isGuarding: Boolean = false

    private val ringerModeReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action != ACTION_RINGER_MODE_CHANGED) return
            if (!isGuarding || savedRingerMode < 0) return

            val audioManager = ctx.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val newMode = audioManager.ringerMode

            if (newMode != savedRingerMode) {
                Log.w(
                    "$TAG Ringer mode changed from $savedRingerMode to $newMode during a call - restoring to $savedRingerMode"
                )
                try {
                    audioManager.ringerMode = savedRingerMode
                    Log.i("$TAG Ringer mode restored to $savedRingerMode")
                } catch (e: Exception) {
                    Log.e("$TAG Failed to restore ringer mode: $e")
                }
            }
        }
    }

    /**
     * Call this when a call starts (IncomingReceived or OutgoingInit).
     * Saves the current ringer mode and starts monitoring for changes.
     */
    fun startGuarding() {
        if (isGuarding) return

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        savedRingerMode = audioManager.ringerMode
        isGuarding = true

        val filter = IntentFilter(ACTION_RINGER_MODE_CHANGED)
        context.registerReceiver(ringerModeReceiver, filter)

        Log.i("$TAG Started guarding ringer mode (saved: $savedRingerMode)")
    }

    /**
     * Call this when the last call ends (onLastCallEnded).
     * Restores the ringer mode and stops monitoring.
     */
    fun stopGuarding() {
        if (!isGuarding) return

        isGuarding = false

        try {
            context.unregisterReceiver(ringerModeReceiver)
        } catch (e: Exception) {
            Log.w("$TAG Could not unregister receiver: $e")
        }

        if (savedRingerMode >= 0) {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val currentMode = audioManager.ringerMode
                if (currentMode != savedRingerMode) {
                    Log.w(
                        "$TAG Ringer mode is $currentMode at call end, restoring to $savedRingerMode"
                    )
                    audioManager.ringerMode = savedRingerMode
                } else {
                    Log.i("$TAG Ringer mode unchanged ($savedRingerMode), no restore needed")
                }
            } catch (e: Exception) {
                Log.e("$TAG Failed to restore ringer mode on stop: $e")
            }
            savedRingerMode = -1
        }

        Log.i("$TAG Stopped guarding ringer mode")
    }
}
