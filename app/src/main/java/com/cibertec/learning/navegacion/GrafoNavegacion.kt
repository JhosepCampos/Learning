package com.cibertec.learning.navegacion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cibertec.learning.modelo.CategoriaModelo
import com.cibertec.learning.vistamodelo.AutenticacionViewModel
import com.cibertec.learning.vistamodelo.OracionesViewModel
import com.cibertec.learning.vistamodelo.VocabularioViewModel
import com.cibertec.learning.vistas.PantallaFormularioOracion
import com.cibertec.learning.vistas.PantallaFormularioPalabra
import com.cibertec.learning.vistas.PantallaInicio
import com.cibertec.learning.vistas.PantallaListaPalabras
import com.cibertec.learning.vistas.PantallaLogin
import com.cibertec.learning.vistas.PantallaOraciones
import com.cibertec.learning.vistas.PantallaRegistro

/**
 * Grafo principal de navegación.
 * Si el usuario no ha iniciado sesión, arranca directamente en la pantalla de Login.
 */
@Composable
fun GrafoNavegacion(
    controladorNavegacion: NavHostController,
    vocabularioViewModel: VocabularioViewModel,
    autenticacionViewModel: AutenticacionViewModel,
    oracionesViewModel: OracionesViewModel,
    modifier: Modifier = Modifier
) {
    val usuarioActual by autenticacionViewModel.usuarioActual.collectAsState()

    val destinoInicial = if (usuarioActual != null) {
        RutasNavegacion.Inicio.ruta
    } else {
        RutasNavegacion.Login.ruta
    }

    NavHost(
        navController = controladorNavegacion,
        startDestination = destinoInicial,
        modifier = modifier
    ) {
        // 1. Pantalla de Inicio
        composable(route = RutasNavegacion.Inicio.ruta) {
            PantallaInicio(
                vocabularioViewModel = vocabularioViewModel,
                autenticacionViewModel = autenticacionViewModel,
                alNavegarACategoria = { categoria ->
                    controladorNavegacion.navigate(RutasNavegacion.ListaVocabulario.crearRuta(categoria))
                },
                alNavegarAFormulario = {
                    controladorNavegacion.navigate(
                        RutasNavegacion.FormularioPalabra.crearRuta(
                            categoriaPredeterminada = CategoriaModelo.VERBOS.identificador
                        )
                    )
                },
                alNavegarAOraciones = {
                    controladorNavegacion.navigate(RutasNavegacion.Oraciones.ruta)
                },
                alNavegarALogin = {
                    controladorNavegacion.navigate(RutasNavegacion.Login.ruta)
                }
            )
        }

        // 2. Pantalla de Lista de Vocabulario
        composable(
            route = RutasNavegacion.ListaVocabulario.ruta,
            arguments = listOf(
                navArgument("categoria") {
                    type = NavType.StringType
                    defaultValue = CategoriaModelo.TODOS.identificador
                }
            )
        ) { entradaNavegacion ->
            val categoria = entradaNavegacion.arguments?.getString("categoria")
                ?: CategoriaModelo.TODOS.identificador

            PantallaListaPalabras(
                categoriaInicial = categoria,
                vocabularioViewModel = vocabularioViewModel,
                alVolver = { controladorNavegacion.popBackStack() },
                alNavegarAFormularioConCategoria = { cat ->
                    controladorNavegacion.navigate(
                        RutasNavegacion.FormularioPalabra.crearRuta(categoriaPredeterminada = cat)
                    )
                },
                alEditarPalabra = { palabra ->
                    controladorNavegacion.navigate(
                        RutasNavegacion.FormularioPalabra.crearRuta(
                            id = palabra.id,
                            categoriaPredeterminada = palabra.categoria
                        )
                    )
                }
            )
        }

        // 3. Formulario de Palabra (Vocabulario)
        composable(
            route = RutasNavegacion.FormularioPalabra.ruta,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("categoriaPredeterminada") {
                    type = NavType.StringType
                    defaultValue = CategoriaModelo.VERBOS.identificador
                }
            )
        ) { entradaNavegacion ->
            val idPalabra = entradaNavegacion.arguments?.getString("id")
            val categoriaPredeterminada = entradaNavegacion.arguments?.getString("categoriaPredeterminada")

            PantallaFormularioPalabra(
                idPalabra = if (idPalabra.isNullOrBlank()) null else idPalabra,
                categoriaPredeterminada = categoriaPredeterminada,
                vocabularioViewModel = vocabularioViewModel,
                alVolver = { controladorNavegacion.popBackStack() }
            )
        }

        // 4. Pantalla de Oraciones de Comunicación Rápida
        composable(route = RutasNavegacion.Oraciones.ruta) {
            PantallaOraciones(
                oracionesViewModel = oracionesViewModel,
                alVolver = { controladorNavegacion.popBackStack() },
                alNavegarAFormularioOracion = { idOracion ->
                    controladorNavegacion.navigate(
                        RutasNavegacion.FormularioOracion.crearRuta(id = idOracion)
                    )
                }
            )
        }

        // 5. Formulario de Oración
        composable(
            route = RutasNavegacion.FormularioOracion.ruta,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { entradaNavegacion ->
            val idOracion = entradaNavegacion.arguments?.getString("id")

            PantallaFormularioOracion(
                idOracion = if (idOracion.isNullOrBlank()) null else idOracion,
                oracionesViewModel = oracionesViewModel,
                vocabularioViewModel = vocabularioViewModel,
                alVolver = { controladorNavegacion.popBackStack() }
            )
        }

        // 6. Pantalla de Inicio de Sesión / Perfil
        composable(route = RutasNavegacion.Login.ruta) {
            PantallaLogin(
                autenticacionViewModel = autenticacionViewModel,
                alIniciarSesionExitoso = {
                    vocabularioViewModel.refrescarDatosUsuario()
                    oracionesViewModel.refrescarDatosUsuario()
                    controladorNavegacion.navigate(RutasNavegacion.Inicio.ruta) {
                        popUpTo(RutasNavegacion.Login.ruta) { inclusive = true }
                    }
                },
                alCerrarSesion = {
                    vocabularioViewModel.refrescarDatosUsuario()
                    oracionesViewModel.refrescarDatosUsuario()
                    controladorNavegacion.navigate(RutasNavegacion.Login.ruta) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                alVolver = if (usuarioActual != null) {
                    { controladorNavegacion.popBackStack() }
                } else null,
                alNavegarARegistro = {
                    controladorNavegacion.navigate(RutasNavegacion.Registro.ruta)
                }
            )
        }

        // 7. Pantalla de Registro
        composable(route = RutasNavegacion.Registro.ruta) {
            PantallaRegistro(
                autenticacionViewModel = autenticacionViewModel,
                alVolver = { controladorNavegacion.popBackStack() },
                alCompletarRegistro = {
                    vocabularioViewModel.refrescarDatosUsuario()
                    oracionesViewModel.refrescarDatosUsuario()
                    controladorNavegacion.navigate(RutasNavegacion.Inicio.ruta) {
                        popUpTo(RutasNavegacion.Login.ruta) { inclusive = true }
                    }
                }
            )
        }
    }
}
