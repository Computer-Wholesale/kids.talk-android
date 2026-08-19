package org.linphone.ui.main.kidstalk

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.linphone.core.ManagedConfiguration
import org.linphone.core.tools.Log

/**
 * Resolves the one callable household contact without allowing locally saved data to override
 * centrally managed configuration. The local store is encrypted and excluded from backup/transfer.
 */
class KidsTalkContactRepository(private val context: Context) : KidsTalkContactStore {
    companion object {
        private const val TAG = "[KidsTalk Contact Repository]"
        const val LOCAL_STORE_FILE = "kidstalk_contact.pref"
        private const val KEY_LOCAL_NAME = "local_name"
        private const val KEY_LOCAL_EXTENSION = "local_extension"
    }

    override fun resolve(): ResolvedKidsTalkContact? = KidsTalkContactResolver.resolve(
        managed = loadManagedContact(),
        local = loadLocalContact()
    )

    override fun saveLocal(contact: KidsTalkContact): Boolean {
        if (!KidsTalkContactPolicy.isValidExtension(contact.extension) || contact.name.isBlank()) return false
        return encryptedPreferences()?.edit()?.putString(KEY_LOCAL_NAME, contact.name)
            ?.putString(KEY_LOCAL_EXTENSION, contact.extension)?.commit() == true
    }

    override fun loadLocalContact(): KidsTalkContact? = encryptedPreferences()?.let { preferences ->
        contactFrom(preferences.getString(KEY_LOCAL_NAME, null), preferences.getString(KEY_LOCAL_EXTENSION, null))
    }

    private fun loadManagedContact(): KidsTalkContact? {
        val restrictions = ManagedConfiguration.getRestrictions(context) ?: return null
        return contactFrom(
            restrictions.getString(ManagedConfiguration.KEY_CONTACT_NAME),
            restrictions.getString(ManagedConfiguration.KEY_CONTACT_EXTENSION)
        )
    }

    private fun contactFrom(name: String?, extension: String?): KidsTalkContact? {
        val normalizedName = name?.trim().orEmpty()
        val normalizedExtension = extension?.trim().orEmpty()
        return if (
            normalizedName.isNotBlank() &&
            KidsTalkContactPolicy.isValidExtension(normalizedExtension)
        ) {
            KidsTalkContact(normalizedName, normalizedExtension)
        } else {
            null
        }
    }

    private fun encryptedPreferences(): SharedPreferences? = try {
        val masterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            LOCAL_STORE_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (exception: Exception) {
        Log.e("$TAG Unable to open encrypted household-contact storage: $exception")
        null
    }
}
