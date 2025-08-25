package com.example.shoe_store.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.shoe_store.models.User
import com.google.gson.Gson

object SessionManager {
    private const val PREFS_NAME = "user_session"
    private const val KEY_USER_JSON = "user_json"

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUser(context: Context, user: User) {
        val userJson = Gson().toJson(user)
        getSharedPreferences(context).edit().apply {
            putString(KEY_USER_JSON, userJson)
            apply()
        }
    }

    fun getUser(context: Context): User? {
        val userJson = getSharedPreferences(context).getString(KEY_USER_JSON, null)
        return try {
            if (userJson != null) {
                Gson().fromJson(userJson, User::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun clearUser(context: Context) {
        getSharedPreferences(context).edit().clear().apply()
    }


    fun getUserId(context: Context): Int {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        return sharedPref.getInt("user_id", 0)
    }

    fun saveUserId(context: Context, userId: Int) {
        val sharedPref = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("user_id", userId)
            apply()
        }
    }

}