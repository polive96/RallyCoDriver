package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Test

class PaceNoteParserTest {

    @Test
    fun `parse valid CSV data`() {
        val csvData = "6,L,100\n4,R,50\n0,Crest,200"
        val expected = listOf(
            PaceNote(6, "L", 100),
            PaceNote(4, "R", 50),
            PaceNote(0, "Crest", 200)
        )
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }

    @Test
    fun `parse CSV with empty lines`() {
        val csvData = "6,L,100\n\n4,R,50\n\n\n0,Crest,200"
        val expected = listOf(
            PaceNote(6, "L", 100),
            PaceNote(4, "R", 50),
            PaceNote(0, "Crest", 200)
        )
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }

    @Test
    fun `parse CSV with malformed lines - wrong column count`() {
        val csvData = "6,L,100\n4,R\n0,Crest,200,Extra"
        val expected = listOf(PaceNote(6, "L", 100)) // Only the valid line
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }

    @Test
    fun `parse CSV with malformed lines - non-integer radius`() {
        val csvData = "6,L,100\nFour,R,50\n0,Crest,200"
        val expected = listOf(
            PaceNote(6, "L", 100),
            PaceNote(0, "Crest", 200)
        )
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }

    @Test
    fun `parse CSV with malformed lines - non-integer distance`() {
        val csvData = "6,L,100\n4,R,Fifty\n0,Crest,200"
        val expected = listOf(
            PaceNote(6, "L", 100),
            PaceNote(0, "Crest", 200)
        )
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }

    @Test
    fun `parse empty input string`() {
        val csvData = ""
        val expected = emptyList<PaceNote>()
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }

    @Test
    fun `parse input string with only newlines and spaces`() {
        val csvData = "\n   \n  \n"
        val expected = emptyList<PaceNote>()
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }


    @Test
    fun `parse CSV with leading trailing whitespace in values and lines`() {
        val csvData = "  6  , L  , 100  \n  4,R,50 \n0, Crest ,200\n   "
        val expected = listOf(
            PaceNote(6, "L", 100),
            PaceNote(4, "R", 50),
            PaceNote(0, "Crest", 200)
        )
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }

    @Test
    fun `parse CSV with mixed valid and invalid lines`() {
        val csvData = "1,L,10\nINVALID,R,20\n3,C,30\n4,X,Y,Z\n5,G,50"
        val expected = listOf(
            PaceNote(1, "L", 10),
            PaceNote(3, "C", 30),
            PaceNote(5, "G", 50)
        )
        val result = PaceNoteParser.parse(csvData)
        assertEquals(expected, result)
    }
}
