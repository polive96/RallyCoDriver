package com.oliveracing.rallycodriver

import app.cash.turbine.test
import com.oliveracing.rallycodriver.PaceNote
import com.oliveracing.rallycodriver.PaceNoteParser
import com.oliveracing.rallycodriver.RallyViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description

// Rule to set the Main dispatcher for tests
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

@ExperimentalCoroutinesApi
class RallyViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RallyViewModel
    private lateinit var mockPaceNoteParser: PaceNoteParser

    private val sampleNotes = listOf(
        PaceNote(6, "L", 100),
        PaceNote(4, "R", 50),
        PaceNote(0, "Crest", 200)
    )

    @Before
    fun setUp() {
        mockPaceNoteParser = mockk()
        // Default behavior for parser, can be overridden in specific tests
        every { mockPaceNoteParser.parse(any()) } returns sampleNotes
        viewModel = RallyViewModel(mockPaceNoteParser) // Manually instantiate with mock
    }

    @After
    fun tearDown() {
        // Any cleanup if necessary
    }

    @Test
    fun `initial state loads sample data correctly`() = runTest {
        viewModel.currentPaceNote.test {
            assertEquals(sampleNotes[0], awaitItem()) // Initial note from init block
            // ViewModel init block loads its own sample data, not from mock at this point
            // Let's adjust to test the actual init block behavior
        }
        // Re-initialize for this specific test case to ensure we check ViewModel's internal sample data
        val freshViewModel = RallyViewModel(PaceNoteParser) // Use real parser for default data
        freshViewModel.currentPaceNote.test {
             val expectedInitialNote = PaceNote(6, "L", 100) // From ViewModel's init
             assertEquals(expectedInitialNote, awaitItem())
        }
    }

    @Test
    fun `loadNotesFromString updates pace notes and current note`() = runTest {
        val csvData = "1,N,10\n2,S,20"
        val parsedNotes = listOf(PaceNote(1, "N", 10), PaceNote(2, "S", 20))
        every { mockPaceNoteParser.parse(csvData) } returns parsedNotes

        viewModel.currentPaceNote.test {
            // Initial emission from sample data in init
            awaitItem() // Skip initial value from constructor/init

            viewModel.loadNotesFromString(csvData)

            assertEquals(parsedNotes[0], awaitItem()) // First note from loaded CSV
            verify { mockPaceNoteParser.parse(csvData) }
        }
    }

    @Test
    fun `loadNotesFromString with empty CSV clears notes`() = runTest {
        val csvData = ""
        every { mockPaceNoteParser.parse(csvData) } returns emptyList()

        viewModel.currentPaceNote.test {
            awaitItem() // Skip initial

            viewModel.loadNotesFromString(csvData)
            assertEquals(null, awaitItem()) // No current note
        }
    }
    
    @Test
    fun `loadNotesFromString with only invalid CSV lines results in no notes`() = runTest {
        val csvData = "invalid,line\nanother,invalid"
        every { mockPaceNoteParser.parse(csvData) } returns emptyList()

        viewModel.currentPaceNote.test {
            awaitItem() // Skip initial from ViewModel's sample data

            viewModel.loadNotesFromString(csvData)

            assertEquals(null, awaitItem())
            verify { mockPaceNoteParser.parse(csvData) }
        }
    }

    @Test
    fun `nextNote advances current note, stops at end`() = runTest {
         // Use the default sampleNotes loaded in setUp by the ViewModel's init
        val vmWithInternalSamples = RallyViewModel(PaceNoteParser) // Uses its own sample data

        vmWithInternalSamples.currentPaceNote.test {
            assertEquals(PaceNote(6, "L", 100), awaitItem()) // Initial

            vmWithInternalSamples.nextNote()
            assertEquals(PaceNote(4, "R", 50), awaitItem())

            vmWithInternalSamples.nextNote()
            assertEquals(PaceNote(0, "Crest", 200), awaitItem())

            vmWithInternalSamples.nextNote()
            assertEquals(PaceNote(5, "L", 70), awaitItem()) // Last of internal sample data

            vmWithInternalSamples.nextNote() // Try to go past the end
            // No new item should be emitted, or remains the last one
            // Turbine will timeout if no new item, or we can cancel
            expectNoEvents() // Assert that no new state is emitted
        }
    }

    @Test
    fun `previousNote moves to previous note, stops at start`() = runTest {
        val vmWithInternalSamples = RallyViewModel(PaceNoteParser) // Uses its own sample data

        // Go to the last note first
        vmWithInternalSamples.nextNote() // 6L -> 4R
        vmWithInternalSamples.nextNote() // 4R -> 0Crest
        vmWithInternalSamples.nextNote() // 0Crest -> 5L (last)

        vmWithInternalSamples.currentPaceNote.test {
            assertEquals(PaceNote(5, "L", 70), awaitItem()) // Current is last

            vmWithInternalSamples.previousNote()
            assertEquals(PaceNote(0, "Crest", 200), awaitItem())

            vmWithInternalSamples.previousNote()
            assertEquals(PaceNote(4, "R", 50), awaitItem())

            vmWithInternalSamples.previousNote()
            assertEquals(PaceNote(6, "L", 100), awaitItem()) // First note

            vmWithInternalSamples.previousNote() // Try to go before start
            expectNoEvents() // Assert that no new state is emitted
        }
    }

    @Test
    fun `getCurrentNoteTextForSpeech returns correct distance string`() {
        // ViewModel is initialized with its own sample data
        val vmWithInternalSamples = RallyViewModel(PaceNoteParser)
        assertEquals("100", vmWithInternalSamples.getCurrentNoteTextForSpeech()) // Initial note 6 L 100

        vmWithInternalSamples.nextNote() // 4 R 50
        assertEquals("50", vmWithInternalSamples.getCurrentNoteTextForSpeech())

        // Test with no note
        every { mockPaceNoteParser.parse(any()) } returns emptyList()
        viewModel.loadNotesFromString("") // Clears notes
        assertEquals("", viewModel.getCurrentNoteTextForSpeech())
    }

    @Test
    fun `currentPaceNote emits null when no notes are loaded`() = runTest {
        every { mockPaceNoteParser.parse(any()) } returns emptyList()
        viewModel.loadNotesFromString("") // Load empty list

        viewModel.currentPaceNote.test {
            assertEquals(null, awaitItem())
        }
    }
}
