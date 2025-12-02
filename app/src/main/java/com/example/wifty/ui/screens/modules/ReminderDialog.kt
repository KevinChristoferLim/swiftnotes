package com.example.wifty.ui.screens.modules

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.util.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Card

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderDialog(
    onDismiss: () -> Unit,
    onSave: (ReminderData) -> Unit
) {
    val ctx = LocalContext.current

    var tab by remember { mutableStateOf(0) }

    var step by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf<Long?>(null) }

    val timeState = rememberTimePickerState()

    var repeatChoices by remember { mutableStateOf<List<String>>(emptyList()) }

    var placeText by remember { mutableStateOf("") }

    fun showDatePicker(onPicked: (Int, Int, Int) -> Unit) {
        val c = Calendar.getInstance()
        DatePickerDialog(
            ctx,
            { _, y, m, d -> onPicked(y, m, d) },
            c.get(Calendar.YEAR),
            c.get(Calendar.MONTH),
            c.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .width(340.dp)
            ) {

                Text("Add reminder", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                TabRow(selectedTabIndex = tab) {
                    Tab(tab == 0, { tab = 0 }) { Text("Time") }
                    Tab(tab == 1, { tab = 1 }) { Text("Place") }
                }

                Spacer(Modifier.height(16.dp))

                // TIME TAB (3 STEPS)
                if (tab == 0) {

                    Text("Step ${step + 1} of 3", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    when (step) {

                        // STEP 1 — PICK DATE
                        0 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.padding(16.dp)) {

                                    Text("Choose date", style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        selectedDate?.let { Date(it).toString() } ?: "No date selected"
                                    )

                                    Spacer(Modifier.height(12.dp))

                                    Button(onClick = {
                                        showDatePicker { y, m, d ->
                                            val cal = Calendar.getInstance()
                                            cal.set(y, m, d, 0, 0)
                                            selectedDate = cal.timeInMillis
                                        }
                                    }) {
                                        Text("Pick date")
                                    }

                                    if (selectedDate != null) {
                                        TextButton(onClick = { selectedDate = null }) {
                                            Text("Clear")
                                        }
                                    }
                                }
                            }
                        }

                        // STEP 2 — PICK TIME
                        1 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("Choose time", style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(8.dp))

                                    TimePicker(state = timeState)
                                }
                            }
                        }

                        // STEP 3 — REPEAT OPTIONS
                        2 -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.padding(16.dp)) {

                                    Text("Repeat options", style = MaterialTheme.typography.titleSmall)
                                    Spacer(Modifier.height(12.dp))

                                    var selected by remember { mutableStateOf(repeatChoices.toMutableSet()) }

                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {

                                        // Everyday
                                        FilterChip(
                                            selected = "Everyday" in selected,
                                            onClick = {
                                                selected.clear()
                                                selected.add("Everyday")
                                                repeatChoices = selected.toList()
                                            },
                                            label = { Text("Everyday") }
                                        )

                                        val days = listOf(
                                            "Monday", "Tuesday", "Wednesday",
                                            "Thursday", "Friday", "Saturday", "Sunday"
                                        )

                                        days.forEach { day ->
                                            FilterChip(
                                                selected = day in selected,
                                                onClick = {
                                                    if (day in selected) selected.remove(day)
                                                    else selected.add(day)

                                                    selected.remove("Everyday")
                                                    repeatChoices = selected.toList()
                                                },
                                                label = { Text(day.take(3)) }
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))
                                    Text("Selected: ${repeatChoices.ifEmpty { listOf("None") }.joinToString()}")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        if (step > 0) {
                            OutlinedButton(onClick = { step-- }) { Text("Back") }
                        } else Spacer(modifier = Modifier.width(1.dp))

                        if (step < 2) {
                            Button(onClick = { step++ }) { Text("Next") }
                        }
                    }
                }

                // PLACE TAB
                if (tab == 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Place", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))

                            OutlinedTextField(
                                value = placeText,
                                onValueChange = { placeText = it },
                                label = { Text("Location name or address") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // SAVE
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))

                    Button(onClick = {
                        val cal = Calendar.getInstance()

                        if (selectedDate != null) cal.timeInMillis = selectedDate!!

                        cal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                        cal.set(Calendar.MINUTE, timeState.minute)
                        cal.set(Calendar.SECOND, 0)

                        onSave(
                            ReminderData(
                                dateMillis = selectedDate,
                                timeMillis = cal.timeInMillis,
                                repeat = repeatChoices,
                                location = placeText.ifBlank { null }
                            )
                        )
                    }) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
