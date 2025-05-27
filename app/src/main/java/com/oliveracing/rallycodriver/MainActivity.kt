package com.oliveracing.rallycodriver

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.koin.androidx.viewmodel.ext.android.viewModel // Correct import for by viewModel()
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {

    private val rallyViewModel: RallyViewModel by viewModel() // Koin ViewModel injection

    // File picker launcher
    private val filePickerLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                try {
                    val contentResolver = applicationContext.contentResolver
                    contentResolver.openInputStream(it)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            val fileContent = reader.readText()
                            rallyViewModel.loadNotesFromString(fileContent)
                            Log.d("MainActivity", "Successfully loaded file: $uri")
                        }
                    }
                } catch (e: IOException) {
                    Log.e("MainActivity", "Error reading file: $uri", e)
                    // Optionally, show a Toast or some UI feedback about the error
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Using MaterialTheme (ensure you have the Material3 dependency)
            MaterialTheme {
                RallyAppScreen(
                    rallyViewModel = rallyViewModel,
                    onLoadFileClicked = {
                        // Launch the file picker to select a CSV file
                        filePickerLauncher.launch(arrayOf("text/csv"))
                    }
                )
            }
        }
    }
}

@Composable
fun RallyAppScreen(rallyViewModel: RallyViewModel, onLoadFileClicked: () -> Unit) {
    val currentNote by rallyViewModel.currentPaceNote.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top // Align content to the top
        ) {
            Button(onClick = onLoadFileClicked) {
                Text("Load CSV File")
            }

            Spacer(modifier = Modifier.height(16.dp)) // Space between button and PaceNoteDisplay

            // The PaceNoteDisplay will take the rest of the space
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                 PaceNoteDisplay(
                    paceNote = currentNote,
                    onNext = { rallyViewModel.nextNote() },
                    onPrevious = { rallyViewModel.previousNote() }
                )
            }
        }
    }
}
