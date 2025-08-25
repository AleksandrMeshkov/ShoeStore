@file:Suppress("DEPRECATION")

package com.example.shoe_store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.shoe_store.models.UserAuth
import com.example.shoe_store.service.RetrofitClient
import com.example.shoe_store.ui.theme.Shoe_storeTheme
import com.example.shoe_store.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Shoe_storeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Логин", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Пароль", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (username.isEmpty() || password.isEmpty()) {
                    errorMessage = "Заполните все поля"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.instance.login(
                            UserAuth(Login = username, Password = password)
                        )

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                response.body()?.let { user ->
                                    SessionManager.saveUser(context, user)
                                    context.startActivity(Intent(context, Promotka::class.java))
                                    (context as? Activity)?.finish()
                                } ?: run {
                                    errorMessage = "Неверный логин или пароль"
                                    isLoading = false
                                }
                            } else {
                                errorMessage = try {
                                    response.errorBody()?.string() ?: "Ошибка авторизации"
                                } catch (e: Exception) {
                                    "Ошибка авторизации"
                                }
                                isLoading = false
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Ошибка сети: ${e.message}"
                            isLoading = false
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1976D2)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text(text = "Войти", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = {
                context.startActivity(Intent(context, RegWindow::class.java))
            }
        ) {
            Text("Нет аккаунта? Зарегистрироваться", color = Color(0xFF1976D2))
        }
        TextButton(
            onClick = {
                context.startActivity(Intent(context, ForgotPassword::class.java))
            }
        ) {
            Text("Забыли пароль?", color = Color(0xFF1976D2))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Shoe_storeTheme {
        LoginScreen()
    }
}