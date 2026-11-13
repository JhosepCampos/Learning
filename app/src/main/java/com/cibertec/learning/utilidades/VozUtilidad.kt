package com.cibertec.learning.utilidades

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Utilidad nativa de Android para sintetizar voz en inglés sin APIs externas.
 */
class VozUtilidad(context: Context) {

    private var sintetizadorVoz: TextToSpeech? = null
    private var estaInicializado = false

    init {
        sintetizadorVoz = TextToSpeech(context.applicationContext) { estado ->
            if (estado == TextToSpeech.SUCCESS) {
                val resultado = sintetizadorVoz?.setLanguage(Locale.US)
                estaInicializado = resultado != TextToSpeech.LANG_MISSING_DATA &&
                        resultado != TextToSpeech.LANG_NOT_SUPPORTED
            }
        }
    }

    /**
     * Pronuncia el texto en inglés.
     */
    fun pronunciarEnIngles(texto: String) {
        if (estaInicializado && texto.isNotBlank()) {
            sintetizadorVoz?.speak(texto, TextToSpeech.QUEUE_FLUSH, null, "ID_PRONUNCIACION")
        }
    }

    /**
     * Libera los recursos del sintetizador.
     */
    fun liberarRecursos() {
        sintetizadorVoz?.stop()
        sintetizadorVoz?.shutdown()
        sintetizadorVoz = null
        estaInicializado = false
    }
}
