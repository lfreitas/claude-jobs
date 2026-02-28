package com.claudejobs.data.prefs

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "api_key_store")

@Singleton
class ApiKeyStore @Inject constructor(@ApplicationContext private val context: Context) {

    companion object {
        private val API_KEY_PREF = stringPreferencesKey("enc_api_key")
        private val MODEL_PREF = stringPreferencesKey("model")
        const val DEFAULT_MODEL = "claude-haiku-4-5-20251001"
        const val MODEL_SONNET = "claude-sonnet-4-6"
    }

    private val aead: Aead by lazy {
        AeadConfig.register()
        val keysetHandle = AndroidKeysetManager.Builder()
            .withSharedPref(context, "claude_jobs_keyset_v1", null)
            .withKeyTemplate(KeyTemplates.get("AES128_GCM"))
            .withMasterKeyUri("android-keystore://claude_jobs_master_key")
            .build()
            .keysetHandle
        keysetHandle.getPrimitive(Aead::class.java)
    }

    suspend fun saveApiKey(apiKey: String) {
        val encrypted = aead.encrypt(apiKey.toByteArray(Charsets.UTF_8), null)
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP)
        context.dataStore.edit { it[API_KEY_PREF] = encoded }
    }

    suspend fun getApiKey(): String {
        return try {
            val encoded = context.dataStore.data.first()[API_KEY_PREF] ?: return ""
            val decrypted = aead.decrypt(Base64.decode(encoded, Base64.NO_WRAP), null)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun saveModel(model: String) {
        context.dataStore.edit { it[MODEL_PREF] = model }
    }

    fun getModelFlow(): Flow<String> =
        context.dataStore.data.map { it[MODEL_PREF] ?: DEFAULT_MODEL }

    suspend fun getModel(): String =
        context.dataStore.data.first()[MODEL_PREF] ?: DEFAULT_MODEL
}
