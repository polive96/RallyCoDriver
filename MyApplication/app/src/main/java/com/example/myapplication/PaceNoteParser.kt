package com.example.myapplication

object PaceNoteParser {

    fun parse(csvData: String): List<PaceNote> {
        val paceNotes = mutableListOf<PaceNote>()
        val lines = csvData.trim().split('\n')

        for (line in lines) {
            if (line.isBlank()) {
                continue // Skip empty lines
            }
            val parts = line.split(',')
            if (parts.size == 3) {
                try {
                    val radius = parts[0].trim().toInt()
                    val direction = parts[1].trim()
                    val distance = parts[2].trim().toInt()
                    paceNotes.add(PaceNote(radius, direction, distance))
                } catch (e: NumberFormatException) {
                    // Log or handle lines with non-integer radius/distance if needed
                    // For now, we just skip them as per requirements
                    println("Warning: Skipping malformed line (number format issue): $line")
                }
            } else {
                // Log or handle lines with incorrect number of columns if needed
                println("Warning: Skipping malformed line (column count issue): $line")
            }
        }
        return paceNotes
    }
}
