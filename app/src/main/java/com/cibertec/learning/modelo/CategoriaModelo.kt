package com.cibertec.learning.modelo

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.ui.graphics.vector.ImageVector
import com.cibertec.learning.R

/**
 * Modelo que representa las categorías del cuaderno de aprendizaje.
 */
enum class CategoriaModelo(
    val identificador: String,
    @StringRes val idTexto: Int,
    @StringRes val idDescripcion: Int,
    val icono: ImageVector
) {
    TODOS(
        identificador = "todos",
        idTexto = R.string.categoria_todos,
        idDescripcion = R.string.subtitulo_inicio,
        icono = Icons.Default.List
    ),
    VERBOS(
        identificador = "verbos",
        idTexto = R.string.categoria_verbos,
        idDescripcion = R.string.desc_verbos,
        icono = Icons.Default.Bolt
    ),
    ADJETIVOS(
        identificador = "adjetivos",
        idTexto = R.string.categoria_adjetivos,
        idDescripcion = R.string.desc_adjetivos,
        icono = Icons.Default.AutoAwesome
    ),
    PRONOMBRES(
        identificador = "pronombres",
        idTexto = R.string.categoria_pronombres,
        idDescripcion = R.string.desc_pronombres,
        icono = Icons.Default.Psychology
    ),
    PRONOMBRES_PERSONALES(
        identificador = "pronombres_personales",
        idTexto = R.string.categoria_pronombres_personales,
        idDescripcion = R.string.desc_pronombres_personales,
        icono = Icons.Default.Person
    ),
    OTRA(
        identificador = "otra",
        idTexto = R.string.categoria_otra,
        idDescripcion = R.string.categoria_otra,
        icono = Icons.Default.Category
    );

    companion object {
        fun obtenerPorIdentificador(identificador: String?): CategoriaModelo {
            return entries.firstOrNull { it.identificador.equals(identificador, ignoreCase = true) }
                ?: TODOS
        }

        fun obtenerCategoriasDisponiblesParaRegistro(): List<CategoriaModelo> {
            return listOf(VERBOS, ADJETIVOS, PRONOMBRES, PRONOMBRES_PERSONALES, OTRA)
        }
    }
}
