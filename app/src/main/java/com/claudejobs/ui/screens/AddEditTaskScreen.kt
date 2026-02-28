package com.claudejobs.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.claudejobs.ui.viewmodel.AddEditTaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTaskScreen(
    taskId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: AddEditTaskViewModel = hiltViewModel()
) {
    LaunchedEffect(taskId) { viewModel.loadTask(taskId) }

    val isEditing = taskId > 0L
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Task" else "New Task") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Task name
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it; viewModel.nameError = false },
                label = { Text("Task name") },
                isError = viewModel.nameError,
                supportingText = if (viewModel.nameError) {{ Text("Required") }} else null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Prompt
            OutlinedTextField(
                value = viewModel.prompt,
                onValueChange = { viewModel.prompt = it; viewModel.promptError = false },
                label = { Text("Prompt for Claude") },
                isError = viewModel.promptError,
                supportingText = if (viewModel.promptError) {{ Text("Required") }} else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                maxLines = 8
            )

            // Schedule type selector
            Text("Schedule", style = MaterialTheme.typography.labelMedium)
            val scheduleTypes = listOf("DAILY", "WEEKLY", "INTERVAL", "ONE_TIME")
            val scheduleLabels = listOf("Daily", "Weekly", "Every N hours", "One time")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                scheduleTypes.take(2).forEachIndexed { i, type ->
                    FilterChip(
                        selected = viewModel.scheduleType == type,
                        onClick = { viewModel.scheduleType = type },
                        label = { Text(scheduleLabels[i]) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                scheduleTypes.drop(2).forEachIndexed { i, type ->
                    FilterChip(
                        selected = viewModel.scheduleType == type,
                        onClick = { viewModel.scheduleType = type },
                        label = { Text(scheduleLabels[i + 2]) }
                    )
                }
            }

            // Conditional schedule fields
            when (viewModel.scheduleType) {
                "DAILY" -> {
                    TimeRow(
                        hour = viewModel.hourOfDay,
                        minute = viewModel.minuteOfHour,
                        onShowPicker = { showTimePicker = true }
                    )
                }
                "WEEKLY" -> {
                    DayOfWeekRow(
                        selected = viewModel.dayOfWeek,
                        onSelect = { viewModel.dayOfWeek = it }
                    )
                    TimeRow(
                        hour = viewModel.hourOfDay,
                        minute = viewModel.minuteOfHour,
                        onShowPicker = { showTimePicker = true }
                    )
                }
                "INTERVAL" -> {
                    OutlinedTextField(
                        value = viewModel.intervalHours.toString(),
                        onValueChange = { viewModel.intervalHours = it.toIntOrNull()?.coerceAtLeast(1) ?: 1 },
                        label = { Text("Repeat every (hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                "ONE_TIME" -> {
                    val formatter = remember { SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()) }
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Run at: ${formatter.format(Date(viewModel.runAtEpochMillis))}")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Buttons
            Button(
                onClick = { viewModel.saveTask(onSaved) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEditing) "Update Task" else "Create Task")
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = viewModel.hourOfDay,
            initialMinute = viewModel.minuteOfHour,
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.hourOfDay = timePickerState.hour
                    viewModel.minuteOfHour = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    // Date picker dialog (for ONE_TIME)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.runAtEpochMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Keep existing time, update date
                        val cal = Calendar.getInstance().apply { timeInMillis = viewModel.runAtEpochMillis }
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = millis
                            set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, cal.get(Calendar.MINUTE))
                        }
                        viewModel.runAtEpochMillis = newCal.timeInMillis
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next: pick time") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TimeRow(hour: Int, minute: Int, onShowPicker: () -> Unit) {
    OutlinedButton(
        onClick = onShowPicker,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Time: %02d:%02d".format(hour, minute))
    }
}

@Composable
private fun DayOfWeekRow(selected: Int, onSelect: (Int) -> Unit) {
    val days = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        days.forEachIndexed { idx, label ->
            FilterChip(
                selected = selected == idx + 1,
                onClick = { onSelect(idx + 1) },
                label = { Text(label) }
            )
        }
    }
}
