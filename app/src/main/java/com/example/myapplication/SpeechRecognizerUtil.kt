package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class SpeechRecognizerUtil(private val context: Context) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognitionCallback: ((String) -> Unit)? = null

    companion object {
        private const val TAG = "SpeechRecognizerUtil"
    }

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Log.d(TAG, "Ready for speech")
                }

                override fun onBeginningOfSpeech() {
                    Log.d(TAG, "Beginning of speech")
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Log.v(TAG, "RMS changed: $rmsdB") // Can be very verbose
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    Log.d(TAG, "Buffer received")
                }

                override fun onEndOfSpeech() {
                    Log.d(TAG, "End of speech")
                }

                override fun onError(error: Int) {
                    val errorMessage = getErrorText(error)
                    Log.e(TAG, "Error: $errorMessage (code: $error)")
                    // Optionally, notify callback about error
                    // recognitionCallback?.invoke("ERROR: $errorMessage")
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        val recognizedText = matches[0]
                        Log.d(TAG, "Recognized text: $recognizedText")
                        recognitionCallback?.invoke(recognizedText)
                    } else {
                        Log.d(TAG, "No recognition results")
                        recognitionCallback?.invoke("") // Or an error/empty indicator
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (matches != null && matches.isNotEmpty()) {
                        val partialText = matches[0]
                        Log.d(TAG, "Partial recognized text: $partialText")
                        // Optionally, provide partial results to callback
                        // recognitionCallback?.invoke(partialText)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    Log.d(TAG, "Event: $eventType")
                }
            })
        } else {
            Log.e(TAG, "Speech recognition not available on this device.")
        }
    }

    fun startListening(callback: (String) -> Unit) {
        if (speechRecognizer == null) {
            Log.e(TAG, "Speech recognizer not initialized or not available.")
            callback("ERROR: Speech recognizer not available.")
            return
        }
        this.recognitionCallback = callback
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // Enable partial results
            // Optionally, specify language:
            // putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
        try {
            speechRecognizer?.startListening(intent)
            Log.d(TAG, "Started listening...")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when starting listening: ${e.message}")
            Log.e(TAG, "Ensure RECORD_AUDIO permission is granted at runtime if targeting Android M or higher.")
            callback("ERROR: Permission denied or other security issue.")
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        Log.d(TAG, "Stopped listening.")
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        Log.d(TAG, "Speech recognizer destroyed.")
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            SpeechRecognizer.ERROR_SERVER -> "Error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown speech recognizer error"
        }
    }
}
