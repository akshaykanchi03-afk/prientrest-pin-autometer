package com.example.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecureStorage(context: Context) {

    private val sharedPrefs = context.getSharedPreferences("secure_tokens_prefs", Context.MODE_PRIVATE)
    private val keyStoreAlias = "PinterestTokenCryptKey"

    init {
        initKeyStoreKey()
    }

    private fun initKeyStoreKey() {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (!keyStore.containsAlias(keyStoreAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            val spec = KeyGenParameterSpec.Builder(
                keyStoreAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return (keyStore.getEntry(keyStoreAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    fun encryptAndSave(key: String, value: String) {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
            val ivBase64 = Base64.encodeToString(iv, Base64.DEFAULT)

            sharedPrefs.edit()
                .putString("${key}_encrypted", encryptedBase64)
                .putString("${key}_iv", ivBase64)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun decryptAndGet(key: String): String? {
        try {
            val encryptedBase64 = sharedPrefs.getString("${key}_encrypted", null) ?: return null
            val ivBase64 = sharedPrefs.getString("${key}_iv", null) ?: return null

            val encryptedBytes = Base64.decode(encryptedBase64, Base64.DEFAULT)
            val iv = Base64.decode(ivBase64, Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun clear(key: String) {
        sharedPrefs.edit()
            .remove("${key}_encrypted")
            .remove("${key}_iv")
            .apply()
    }
}
