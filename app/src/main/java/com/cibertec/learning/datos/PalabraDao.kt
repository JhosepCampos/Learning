package com.cibertec.learning.datos

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.cibertec.learning.modelo.CategoriaModelo
import com.cibertec.learning.modelo.PalabraModelo

/**
 * Objeto de acceso a datos (DAO) para interactuar con la tabla de palabras en SQLite.
 * Todos los métodos filtran estrictamente por usuarioId para aislamiento de datos.
 */
class PalabraDao(private val baseDatosHelper: BaseDatosLocalHelper) {

    /**
     * Inserta una nueva palabra o actualiza una existente en SQLite asignada al usuario.
     */
    fun guardarOActualizar(palabra: PalabraModelo): Boolean {
        val db = baseDatosHelper.writableDatabase
        val valores = ContentValues().apply {
            put(BaseDatosLocalHelper.COLUMNA_ID, palabra.id)
            put(BaseDatosLocalHelper.COLUMNA_USUARIO_ID, palabra.usuarioId)
            put(BaseDatosLocalHelper.COLUMNA_TEXTO_INGLES, palabra.textoIngles.trim())
            put(BaseDatosLocalHelper.COLUMNA_PRONUNCIACION_FONETICA, palabra.pronunciacionFonetica.trim())
            put(BaseDatosLocalHelper.COLUMNA_TRADUCCION, palabra.traduccion.trim())
            put(BaseDatosLocalHelper.COLUMNA_CATEGORIA, palabra.categoria)
            put(BaseDatosLocalHelper.COLUMNA_EJEMPLO, palabra.ejemplo.trim())
            put(BaseDatosLocalHelper.COLUMNA_FECHA_CREACION, palabra.fechaCreacion)
            put(BaseDatosLocalHelper.COLUMNA_SINCRONIZADO, if (palabra.sincronizado) 1 else 0)
            put(BaseDatosLocalHelper.COLUMNA_ELIMINADO, if (palabra.eliminado) 1 else 0)
        }

        val resultado = db.insertWithOnConflict(
            BaseDatosLocalHelper.TABLA_PALABRAS,
            null,
            valores,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        return resultado != -1L
    }

    /**
     * Obtiene todas las palabras no eliminadas del cuaderno pertenecientes al usuario.
     */
    fun obtenerTodas(usuarioId: String): List<PalabraModelo> {
        if (usuarioId.isBlank()) return emptyList()
        val lista = mutableListOf<PalabraModelo>()
        val db = baseDatosHelper.readableDatabase
        val cursor: Cursor = db.query(
            BaseDatosLocalHelper.TABLA_PALABRAS,
            null,
            "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = ?",
            arrayOf(usuarioId, "0"),
            null,
            null,
            "${BaseDatosLocalHelper.COLUMNA_FECHA_CREACION} DESC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                lista.add(mapearCursorAPalabra(c))
            }
        }
        return lista
    }

    /**
     * Obtiene palabras filtradas por categoría pertenecientes al usuario.
     */
    fun obtenerPorCategoria(usuarioId: String, categoria: String): List<PalabraModelo> {
        if (usuarioId.isBlank()) return emptyList()
        if (categoria.equals(CategoriaModelo.TODOS.identificador, ignoreCase = true)) {
            return obtenerTodas(usuarioId)
        }

        val lista = mutableListOf<PalabraModelo>()
        val db = baseDatosHelper.readableDatabase
        val cursor: Cursor = db.query(
            BaseDatosLocalHelper.TABLA_PALABRAS,
            null,
            "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_CATEGORIA} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = ?",
            arrayOf(usuarioId, categoria, "0"),
            null,
            null,
            "${BaseDatosLocalHelper.COLUMNA_FECHA_CREACION} DESC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                lista.add(mapearCursorAPalabra(c))
            }
        }
        return lista
    }

    /**
     * Busca palabras por texto en inglés o traducción pertenecientes exclusivamente al usuario.
     */
    fun buscarPalabras(usuarioId: String, termino: String, categoria: String): List<PalabraModelo> {
        if (usuarioId.isBlank()) return emptyList()
        val lista = mutableListOf<PalabraModelo>()
        val db = baseDatosHelper.readableDatabase
        val busqueda = "%${termino.trim()}%"

        val seleccion: String
        val argumentos: Array<String>

        if (categoria.equals(CategoriaModelo.TODOS.identificador, ignoreCase = true)) {
            seleccion = "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND (${BaseDatosLocalHelper.COLUMNA_TEXTO_INGLES} LIKE ? OR ${BaseDatosLocalHelper.COLUMNA_TRADUCCION} LIKE ?) AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = ?"
            argumentos = arrayOf(usuarioId, busqueda, busqueda, "0")
        } else {
            seleccion = "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND (${BaseDatosLocalHelper.COLUMNA_TEXTO_INGLES} LIKE ? OR ${BaseDatosLocalHelper.COLUMNA_TRADUCCION} LIKE ?) AND ${BaseDatosLocalHelper.COLUMNA_CATEGORIA} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = ?"
            argumentos = arrayOf(usuarioId, busqueda, busqueda, categoria, "0")
        }

        val cursor = db.query(
            BaseDatosLocalHelper.TABLA_PALABRAS,
            null,
            seleccion,
            argumentos,
            null,
            null,
            "${BaseDatosLocalHelper.COLUMNA_FECHA_CREACION} DESC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                lista.add(mapearCursorAPalabra(c))
            }
        }
        return lista
    }

    /**
     * Obtiene una palabra por su ID único y usuario.
     */
    fun obtenerPorId(id: String, usuarioId: String): PalabraModelo? {
        val db = baseDatosHelper.readableDatabase
        val cursor = db.query(
            BaseDatosLocalHelper.TABLA_PALABRAS,
            null,
            "${BaseDatosLocalHelper.COLUMNA_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ?",
            arrayOf(id, usuarioId),
            null,
            null,
            null
        )

        return cursor.use { c ->
            if (c.moveToFirst()) {
                mapearCursorAPalabra(c)
            } else {
                null
            }
        }
    }

    /**
     * Marca una palabra como eliminada o la borra directamente.
     */
    fun marcarComoEliminada(id: String, usuarioId: String): Boolean {
        val db = baseDatosHelper.writableDatabase
        val palabra = obtenerPorId(id, usuarioId) ?: return false

        return if (palabra.sincronizado) {
            val valores = ContentValues().apply {
                put(BaseDatosLocalHelper.COLUMNA_ELIMINADO, 1)
                put(BaseDatosLocalHelper.COLUMNA_SINCRONIZADO, 0)
            }
            db.update(
                BaseDatosLocalHelper.TABLA_PALABRAS,
                valores,
                "${BaseDatosLocalHelper.COLUMNA_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ?",
                arrayOf(id, usuarioId)
            ) > 0
        } else {
            eliminarDefinitivo(id, usuarioId)
        }
    }

    /**
     * Elimina físicamente el registro de la base de datos SQLite para ese usuario.
     */
    fun eliminarDefinitivo(id: String, usuarioId: String): Boolean {
        val db = baseDatosHelper.writableDatabase
        return db.delete(
            BaseDatosLocalHelper.TABLA_PALABRAS,
            "${BaseDatosLocalHelper.COLUMNA_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ?",
            arrayOf(id, usuarioId)
        ) > 0
    }

    /**
     * Obtiene registros que aún no se han sincronizado con Firebase para ese usuario.
     */
    fun obtenerPendientesDeSincronizar(usuarioId: String): List<PalabraModelo> {
        if (usuarioId.isBlank()) return emptyList()
        val lista = mutableListOf<PalabraModelo>()
        val db = baseDatosHelper.readableDatabase
        val cursor = db.query(
            BaseDatosLocalHelper.TABLA_PALABRAS,
            null,
            "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_SINCRONIZADO} = ?",
            arrayOf(usuarioId, "0"),
            null,
            null,
            null
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                lista.add(mapearCursorAPalabra(c))
            }
        }
        return lista
    }

    /**
     * Marca un registro como sincronizado con la nube.
     */
    fun marcarComoSincronizado(id: String, usuarioId: String) {
        val db = baseDatosHelper.writableDatabase
        val valores = ContentValues().apply {
            put(BaseDatosLocalHelper.COLUMNA_SINCRONIZADO, 1)
        }
        db.update(
            BaseDatosLocalHelper.TABLA_PALABRAS,
            valores,
            "${BaseDatosLocalHelper.COLUMNA_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ?",
            arrayOf(id, usuarioId)
        )
    }

    /**
     * Cuenta la cantidad de palabras activas por categoría para los contadores del inicio para ese usuario.
     */
    fun contarPorCategoria(usuarioId: String, categoria: String): Int {
        if (usuarioId.isBlank()) return 0
        val db = baseDatosHelper.readableDatabase
        val cursor = if (categoria.equals(CategoriaModelo.TODOS.identificador, ignoreCase = true)) {
            db.rawQuery(
                "SELECT COUNT(*) FROM ${BaseDatosLocalHelper.TABLA_PALABRAS} WHERE ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = 0",
                arrayOf(usuarioId)
            )
        } else {
            db.rawQuery(
                "SELECT COUNT(*) FROM ${BaseDatosLocalHelper.TABLA_PALABRAS} WHERE ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_CATEGORIA} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = 0",
                arrayOf(usuarioId, categoria)
            )
        }

        return cursor.use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    private fun mapearCursorAPalabra(cursor: Cursor): PalabraModelo {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_ID))
        val usuarioId = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_USUARIO_ID))
        val textoIngles = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_TEXTO_INGLES))
        val pronunciacionFonetica = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_PRONUNCIACION_FONETICA)) ?: ""
        val traduccion = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_TRADUCCION))
        val categoria = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_CATEGORIA))
        val ejemplo = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_EJEMPLO)) ?: ""
        val fechaCreacion = cursor.getLong(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_FECHA_CREACION))
        val sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_SINCRONIZADO)) == 1
        val eliminado = cursor.getInt(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_ELIMINADO)) == 1

        return PalabraModelo(
            id = id,
            usuarioId = usuarioId,
            textoIngles = textoIngles,
            pronunciacionFonetica = pronunciacionFonetica,
            traduccion = traduccion,
            categoria = categoria,
            ejemplo = ejemplo,
            fechaCreacion = fechaCreacion,
            sincronizado = sincronizado,
            eliminado = eliminado
        )
    }
}
