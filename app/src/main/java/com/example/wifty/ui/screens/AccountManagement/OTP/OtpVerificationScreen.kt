package com.example.wifty.ui.screens.AccountManagement.OTP

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    onBack: () -> Unit,
    onSubmit: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Return to settings", fontSize = 14.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF6750A4))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Color(0xFFF6F3FF), Color.White)))
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(20.dp))
                Text("Input Code", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Please input the code you received on your email",
                    color = Color.Gray,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(32.dp))

                // OTP Boxes Logic
                BasicTextField(
                    value = otpCode,
                    onValueChange = {
                        if (it.length <= 6) otpCode = it
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decorationBox = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(6) { index ->
                                val char = when {
                                    index < otpCode.length -> otpCode[index].toString()
                                    else -> ""
                                }
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(0.8f)
                                        .background(Color.White, RoundedCornerShape(8.dp))
                                        .border(
                                            1.dp,
                                            if (char.isNotEmpty()) Color(0xFF6750A4) else Color.LightGray,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = char,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                )

                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("Submit")
                }

                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    Text("Didn't receive any code? ", color = Color.Gray, fontSize = 14.sp)
                    Text("Resend", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}