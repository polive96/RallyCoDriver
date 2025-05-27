package com.oliveracing.rallycodriver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class RallyViewModel(private val paceNoteParser: PaceNoteParser) : ViewModel() {

    private val _paceNotes = mutableListOf<PaceNote>()

    private val _currentNoteIndex = MutableStateFlow(-1) // Start with -1, indicating no note selected or list empty

    // Expose currentPaceNote as a StateFlow
    // It derives its value from _currentNoteIndex and _paceNotes
    val currentPaceNote: StateFlow<PaceNote?> = _currentNoteIndex.map { index ->
        if (index >= 0 && index < _paceNotes.size) {
            _paceNotes[index]
        } else {
            null
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after last subscriber
        initialValue = null
    )

    init {
        // Sample data for initial testing
        _paceNotes.addAll(listOf(
            PaceNote(6, "L", 100),
            PaceNote(4, "R", 50),
            PaceNote(0, "Crest", 200), // Using 0 for 'Crest' as an example
            PaceNote(5, "L", 70)
        ))
        if (_paceNotes.isNotEmpty()) {
            _currentNoteIndex.value = 0
        }
    }

    fun loadNotesFromString(csvData: String) {
        val parsedNotes = PaceNoteParser.parse(csvData)
        _paceNotes.clear()
        _paceNotes.addAll(parsedNotes)
        if (_paceNotes.isNotEmpty()) {
            _currentNoteIndex.value = 0
        } else {
            _currentNoteIndex.value = -1
        }
    }

    fun nextNote() {
        if (_currentNoteIndex.value < _paceNotes.size - 1) {
            _currentNoteIndex.value++
        }
    }

    fun previousNote() {
        if (_currentNoteIndex.value > 0) {
            _currentNoteIndex.value--
        }
    }

    fun getCurrentNoteTextForSpeech(): String {
        return currentPaceNote.value?.distance?.toString() ?: ""
    }
}
