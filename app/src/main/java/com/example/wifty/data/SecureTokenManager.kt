package com.example.wifty.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.wifty.data.api.UserData
import com.google.gson.Gson

class SecureTokenManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val gson = Gson()

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secret_shared_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthData(token: String, user: UserData?) {
        val userJson = gson.toJson(user)
        sharedPreferences.edit()
            .putString("auth_token", token)
            .putString("user_data", userJson)
            .apply()
    }

    fun getToken(): String? {
        return sharedPreferences.getString("auth_token", null)
    }

    fun getUserData(): UserData? {
        val userJson = sharedPreferences.getString("user_data", null)
        return userJson?.let { gson.fromJson(it, UserData::class.java) }
    }

    fun clearAuthData() {
        sharedPreferences.edit().clear().apply()
    }
}
