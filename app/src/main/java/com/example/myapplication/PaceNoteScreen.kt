package com.example.myapplication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PaceNoteDisplay(paceNote: PaceNote?, onNext: () -> Unit, onPrevious: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Pushes content to top and buttons to bottom
    ) {
        if (paceNote != null) {
            // Content Column: Takes up available vertical space, pushing buttons down
            Column(
                modifier = Modifier.weight(1f), // Occupy available space, pushing buttons to the bottom
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // Center the text vertically in this column
            ) {
                Text(
                    text = "${paceNote.radius}",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp), // Large text
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // Each text takes equal portion of this column
                        .wrapContentHeight(Alignment.CenterVertically) // Center text vertically within its space
                )
                Text(
                    text = paceNote.direction,
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
                Text(
                    text = "${paceNote.distance}m", // Added 'm' for clarity
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        } else {
            // Message Column when no note is loaded
            Column(
                modifier = Modifier.weight(1f), // Occupy available space
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "No pace note loaded.",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Buttons Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp), // Add some padding above the buttons
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onPrevious) {
                Text("Previous")
            }
            Button(onClick = onNext) {
                Text("Next")
            }
        }
    }
}

@Preview(showBackground = true, name = "Pace Note Loaded")
@Composable
fun PaceNoteDisplayPreview() {
    MaterialTheme { // Wrap with MaterialTheme for preview
        PaceNoteDisplay(
            paceNote = PaceNote(radius = 6, direction = "L", distance = 100),
            onNext = {},
            onPrevious = {}
        )
    }
}

@Preview(showBackground = true, name = "No Pace Note")
@Composable
fun PaceNoteDisplayNullPreview() {
    MaterialTheme { // Wrap with MaterialTheme for preview
        PaceNoteDisplay(
            paceNote = null,
            onNext = {},
            onPrevious = {}
        )
    }
}
