package com.example.shoe_store

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shoe_store.models.User
import com.example.shoe_store.models.UserRegistration
import com.example.shoe_store.service.RetrofitClient
import com.example.shoe_store.ui.theme.Shoe_storeTheme
import com.example.shoe_store.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegWindow : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Shoe_storeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    RegScreen()
                }
            }
        }
    }
}

@Composable
fun RegScreen() {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var patronymic by remember { mutableStateOf("") }
    var login by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var checked by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

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
            Text(text = "Имя", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Фамилия", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Отчество (необязательно)", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = patronymic,
                onValueChange = { patronymic = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Логин", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = login,
                onValueChange = { login = it },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Пароль", style = MaterialTheme.typography.bodySmall)
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = when {
                        it.isEmpty() -> null
                        it.length < 6 -> "Пароль должен содержать минимум 6 символов"
                        else -> null
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = {
                    passwordError?.let {
                        Text(text = it, color = Color.Red)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = checked,
                onCheckedChange = { checked = it }
            )
            Text("Я согласен с условиями")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!checked) {
                    errorMessage = "Примите условия соглашения"
                    return@Button
                }

                if (login.isEmpty() || password.isEmpty() || name.isEmpty() || surname.isEmpty()) {
                    errorMessage = "Заполните все обязательные поля"
                    return@Button
                }

                isLoading = true
                errorMessage = null

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.instance.register(
                            UserRegistration(
                                Login = login,
                                Password = password,
                                Name = name,
                                Surname = surname,
                                Patronymic = patronymic.ifEmpty { null }
                            )
                        )

                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                response.body()?.let { registrationResponse ->
                                    val user = User(
                                        UsersID = registrationResponse.user_id,
                                        Login = login,
                                        Password = password,
                                        Name = name,
                                        Surname = surname,
                                        Patronymic = patronymic.ifEmpty { null },
                                        Photo = null
                                    )
                                    SessionManager.saveUser(context, user)
                                    context.startActivity(Intent(context, Promotka::class.java))
                                    (context as? Activity)?.finish()
                                } ?: run {
                                    errorMessage = "Ошибка регистрации"
                                    isLoading = false
                                }
                            } else {
                                errorMessage = try {
                                    response.errorBody()?.string() ?: "Ошибка регистрации"
                                } catch (e: Exception) {
                                    "Ошибка регистрации"
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
                Text(text = "Зарегистрироваться", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = {
                    context.startActivity(Intent(context, MainActivity::class.java))
                }
            ) {
                Text("Уже есть аккаунт? Войти", color = Color(0xFF1976D2))
            }


        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegScreenPreview() {
    Shoe_storeTheme {
        RegScreen()
    }
}