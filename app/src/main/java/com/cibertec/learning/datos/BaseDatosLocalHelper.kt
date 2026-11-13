package com.cibertec.learning.datos

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Manejador de base de datos SQLite local para almacenamiento sin conexión.
 * Administra las tablas 'palabras' y 'oraciones', ambas aisladas por 'usuario_id'.
 */
class BaseDatosLocalHelper(context: Context) : SQLiteOpenHelper(
    context,
    NOMBRE_BASE_DATOS,
    null,
    VERSION_BASE_DATOS
) {
    companion object {
        const val NOMBRE_BASE_DATOS = "cuaderno_ingles.db"
        const val VERSION_BASE_DATOS = 3

        // Tabla Palabras
        const val TABLA_PALABRAS = "palabras"
        const val COLUMNA_ID = "id"
        const val COLUMNA_USUARIO_ID = "usuario_id"
        const val COLUMNA_TEXTO_INGLES = "texto_ingles"
        const val COLUMNA_PRONUNCIACION_FONETICA = "pronunciacion_fonetica"
        const val COLUMNA_TRADUCCION = "traduccion"
        const val COLUMNA_CATEGORIA = "categoria"
        const val COLUMNA_EJEMPLO = "ejemplo"
        const val COLUMNA_FECHA_CREACION = "fecha_creacion"
        const val COLUMNA_SINCRONIZADO = "sincronizado"
        const val COLUMNA_ELIMINADO = "eliminado"

        // Tabla Oraciones
        const val TABLA_ORACIONES = "oraciones"
        const val COLUMNA_CONTEXTO = "contexto"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val consultaCrearTablaPalabras = """
            CREATE TABLE IF NOT EXISTS $TABLA_PALABRAS (
                $COLUMNA_ID TEXT PRIMARY KEY,
                $COLUMNA_USUARIO_ID TEXT NOT NULL,
                $COLUMNA_TEXTO_INGLES TEXT NOT NULL,
                $COLUMNA_PRONUNCIACION_FONETICA TEXT,
                $COLUMNA_TRADUCCION TEXT NOT NULL,
                $COLUMNA_CATEGORIA TEXT NOT NULL,
                $COLUMNA_EJEMPLO TEXT,
                $COLUMNA_FECHA_CREACION INTEGER NOT NULL,
                $COLUMNA_SINCRONIZADO INTEGER NOT NULL DEFAULT 0,
                $COLUMNA_ELIMINADO INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(consultaCrearTablaPalabras)

        val consultaCrearTablaOraciones = """
            CREATE TABLE IF NOT EXISTS $TABLA_ORACIONES (
                $COLUMNA_ID TEXT PRIMARY KEY,
                $COLUMNA_USUARIO_ID TEXT NOT NULL,
                $COLUMNA_TEXTO_INGLES TEXT NOT NULL,
                $COLUMNA_PRONUNCIACION_FONETICA TEXT,
                $COLUMNA_TRADUCCION TEXT NOT NULL,
                $COLUMNA_CONTEXTO TEXT NOT NULL,
                $COLUMNA_FECHA_CREACION INTEGER NOT NULL,
                $COLUMNA_SINCRONIZADO INTEGER NOT NULL DEFAULT 0,
                $COLUMNA_ELIMINADO INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(consultaCrearTablaOraciones)
    }

    override fun onUpgrade(db: SQLiteDatabase, versionAntigua: Int, versionNueva: Int) {
        if (versionAntigua < 3) {
            val consultaCrearTablaOraciones = """
                CREATE TABLE IF NOT EXISTS $TABLA_ORACIONES (
                    $COLUMNA_ID TEXT PRIMARY KEY,
                    $COLUMNA_USUARIO_ID TEXT NOT NULL,
                    $COLUMNA_TEXTO_INGLES TEXT NOT NULL,
                    $COLUMNA_PRONUNCIACION_FONETICA TEXT,
                    $COLUMNA_TRADUCCION TEXT NOT NULL,
                    $COLUMNA_CONTEXTO TEXT NOT NULL,
                    $COLUMNA_FECHA_CREACION INTEGER NOT NULL,
                    $COLUMNA_SINCRONIZADO INTEGER NOT NULL DEFAULT 0,
                    $COLUMNA_ELIMINADO INTEGER NOT NULL DEFAULT 0
                )
            """.trimIndent()
            db.execSQL(consultaCrearTablaOraciones)
        }
    }
}
