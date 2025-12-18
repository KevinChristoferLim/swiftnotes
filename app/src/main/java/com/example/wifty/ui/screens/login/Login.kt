package com.example.wifty.ui.screens.login

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.wifty.data.api.ApiService

class AuthViewModelFactory(private val apiService: ApiService, private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(apiService, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    var isLogin by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Toggle Buttons
        Row(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(50))
                .background(Color.White, RoundedCornerShape(50))
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToggleTab(text = "Login", selected = isLogin) { isLogin = true }
            ToggleTab(text = "Sign Up", selected = !isLogin) { isLogin = false }
        }

        Spacer(modifier = Modifier.height(40.dp))

        if (isLogin) {
            LoginUI(authViewModel, onLoginSuccess)
        } else {
            SignUpUI(authViewModel, onLoginSuccess)
        }
    }
}

@Composable
fun ToggleTab(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) Color(0xFF9C6BFF) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 30.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (selected) Color.White else Color.Black)
    }
}

@Composable
fun LoginUI(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    if (uiState.isLoggedIn) {
        onLoginSuccess()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Let's Login", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("And notes your idea", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(25.dp))

        LabeledInput(label = "Email Address", value = email, onValueChange = { email = it })
        LabeledInput(label = "Password", value = password, onValueChange = { password = it }, isPassword = true)

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            GradientButton(text = "Login") {
                authViewModel.login(email, password)
            }
        }

        uiState.error?.let {
            Text(it, color = Color.Red)
        }
    }
}

@Composable
fun SignUpUI(authViewModel: AuthViewModel, onSignUpSuccess: () -> Unit) {
    val uiState by authViewModel.uiState.collectAsState()
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    if (uiState.isLoggedIn) {
        onSignUpSuccess()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Let's Sign Up", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("And notes your idea", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(25.dp))

        LabeledInput(label = "Full Name", value = fullName, onValueChange = { fullName = it })
        LabeledInput(label = "Email Address", value = email, onValueChange = { email = it })
        LabeledInput(label = "Password", isPassword = true, value = password, onValueChange = { password = it })
        LabeledInput(label = "Confirm Password", isPassword = true, value = confirmPassword, onValueChange = { confirmPassword = it })

        Spacer(modifier = Modifier.height(15.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            GradientButton(text = "Sign Up") {
                if (password != confirmPassword) {
                    error = "Passwords do not match"
                } else {
                    error = null
                    authViewModel.signUp(fullName, email, password, confirmPassword)
                }
            }
        }

        val displayError = error ?: uiState.error
        displayError?.let {
            Text(it, color = Color.Red)
        }
    }
}

@Composable
fun LabeledInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
        )
        Spacer(modifier = Modifier.height(15.dp))
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF9C6BFF), Color(0xFF77C7FF))
                )
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}
