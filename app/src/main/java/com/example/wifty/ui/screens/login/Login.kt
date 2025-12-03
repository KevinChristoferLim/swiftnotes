package com.example.wifty.ui.screens.login

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

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit // 👈 NEW CALLBACK ADDED
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

        if (isLogin)
            LoginUI(onLoginSuccess, onNavigateToForgotPassword) // 👈 Pass it here
        else
            SignUpUI()
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
    onLoginSuccess: () -> Unit,
    onNavigateToForgotPassword: () -> Unit // 👈 NEW PARAMETER
) {
    Column(modifier = Modifier.fillMaxWidth()) {

        Text("Let's Login", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("And notes your idea", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(25.dp))

        LabeledInput(label = "Email Address")
        LabeledInput(label = "Password", isPassword = true)

        Text(
            text = "Forgot Password?",
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clickable { onNavigateToForgotPassword() }, // 👈 CONNECTED HERE
            color = Color(0xFF6B4EFF),
            fontSize = 13.sp
        )

        GradientButton(text = "Login") {
            onLoginSuccess()
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("Or", fontSize = 14.sp, color = Color.Gray)
        }

        Spacer(modifier = Modifier.height(15.dp))

        GoogleButton {}
    }
}

@Composable
fun SignUpUI() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Let's Sign Up", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("And notes your idea", fontSize = 14.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(25.dp))

        LabeledInput(label = "Full Name")
        LabeledInput(label = "Email Address")
        LabeledInput(label = "Password", isPassword = true)
        LabeledInput(label = "Confirm Password", isPassword = true)

        Spacer(modifier = Modifier.height(15.dp))

        GradientButton(text = "Sign Up") {}
    }
}

@Composable
fun LabeledInput(label: String, isPassword: Boolean = false) {
    var value by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(5.dp))
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
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

@Composable
fun GoogleButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .shadow(4.dp, RoundedCornerShape(10.dp))
            .background(Color.White, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text("Sign in with Google", fontSize = 16.sp, color = Color.Black)
    }
}