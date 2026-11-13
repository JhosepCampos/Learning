package com.cibertec.learning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cibertec.learning.navegacion.GrafoNavegacion
import com.cibertec.learning.ui.theme.LearningTheme
import com.cibertec.learning.vistamodelo.AutenticacionViewModel
import com.cibertec.learning.vistamodelo.OracionesViewModel
import com.cibertec.learning.vistamodelo.VocabularioViewModel

class MainActivity : ComponentActivity() {

    private val vocabularioViewModel: VocabularioViewModel by viewModels {
        VocabularioViewModel.crearFabrica(application)
    }

    private val autenticacionViewModel: AutenticacionViewModel by viewModels()

    private val oracionesViewModel: OracionesViewModel by viewModels {
        OracionesViewModel.crearFabrica(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LearningTheme {
                val controladorNavegacion = rememberNavController()

                Surface(modifier = Modifier.fillMaxSize()) {
                    GrafoNavegacion(
                        controladorNavegacion = controladorNavegacion,
                        vocabularioViewModel = vocabularioViewModel,
                        autenticacionViewModel = autenticacionViewModel,
                        oracionesViewModel = oracionesViewModel
                    )
                }
            }
        }
    }
}