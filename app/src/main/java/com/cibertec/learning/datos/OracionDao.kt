package com.cibertec.learning.datos

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.cibertec.learning.modelo.OracionModelo

/**
 * Objeto de acceso a datos (DAO) para interactuar con la tabla de oraciones en SQLite.
 * Cada consulta filtra por usuarioId para aislamiento absoluto de datos.
 */
class OracionDao(private val baseDatosHelper: BaseDatosLocalHelper) {

    /**
     * Guarda o actualiza una oración en SQLite vinculada al usuario.
     */
    fun guardarOActualizar(oracion: OracionModelo): Boolean {
        val db = baseDatosHelper.writableDatabase
        val valores = ContentValues().apply {
            put(BaseDatosLocalHelper.COLUMNA_ID, oracion.id)
            put(BaseDatosLocalHelper.COLUMNA_USUARIO_ID, oracion.usuarioId)
            put(BaseDatosLocalHelper.COLUMNA_TEXTO_INGLES, oracion.textoIngles.trim())
            put(BaseDatosLocalHelper.COLUMNA_PRONUNCIACION_FONETICA, oracion.pronunciacionFonetica.trim())
            put(BaseDatosLocalHelper.COLUMNA_TRADUCCION, oracion.traduccion.trim())
            put(BaseDatosLocalHelper.COLUMNA_CONTEXTO, oracion.contexto.trim())
            put(BaseDatosLocalHelper.COLUMNA_FECHA_CREACION, oracion.fechaCreacion)
            put(BaseDatosLocalHelper.COLUMNA_SINCRONIZADO, if (oracion.sincronizado) 1 else 0)
            put(BaseDatosLocalHelper.COLUMNA_ELIMINADO, if (oracion.eliminado) 1 else 0)
        }

        val resultado = db.insertWithOnConflict(
            BaseDatosLocalHelper.TABLA_ORACIONES,
            null,
            valores,
            SQLiteDatabase.CONFLICT_REPLACE
        )
        return resultado != -1L
    }

    /**
     * Obtiene todas las oraciones activas de un usuario.
     */
    fun obtenerTodas(usuarioId: String): List<OracionModelo> {
        if (usuarioId.isBlank()) return emptyList()
        val lista = mutableListOf<OracionModelo>()
        val db = baseDatosHelper.readableDatabase
        val cursor: Cursor = db.query(
            BaseDatosLocalHelper.TABLA_ORACIONES,
            null,
            "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = ?",
            arrayOf(usuarioId, "0"),
            null,
            null,
            "${BaseDatosLocalHelper.COLUMNA_FECHA_CREACION} DESC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                lista.add(mapearCursorAOracion(c))
            }
        }
        return lista
    }

    /**
     * Obtiene oraciones filtradas por contexto comunicativo (viajes, cotidiano, trabajo, etc.).
     */
    fun obtenerPorContexto(usuarioId: String, contexto: String): List<OracionModelo> {
        if (usuarioId.isBlank()) return emptyList()
        if (contexto.isBlank() || contexto.equals("todos", ignoreCase = true)) {
            return obtenerTodas(usuarioId)
        }

        val lista = mutableListOf<OracionModelo>()
        val db = baseDatosHelper.readableDatabase
        val cursor = db.query(
            BaseDatosLocalHelper.TABLA_ORACIONES,
            null,
            "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_CONTEXTO} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = ?",
            arrayOf(usuarioId, contexto, "0"),
            null,
            null,
            "${BaseDatosLocalHelper.COLUMNA_FECHA_CREACION} DESC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                lista.add(mapearCursorAOracion(c))
            }
        }
        return lista
    }

    /**
     * Busca oraciones en inglés o traducción en tiempo real para comunicación rápida.
     */
    fun buscarOraciones(usuarioId: String, termino: String, contexto: String?): List<OracionModelo> {
        if (usuarioId.isBlank()) return emptyList()
        val lista = mutableListOf<OracionModelo>()
        val db = baseDatosHelper.readableDatabase
        val busqueda = "%${termino.trim()}%"

        val seleccion: String
        val argumentos: Array<String>

        val filtraContexto = !contexto.isNullOrBlank() && !contexto.equals("todos", ignoreCase = true)

        if (filtraContexto) {
            seleccion = "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND (${BaseDatosLocalHelper.COLUMNA_TEXTO_INGLES} LIKE ? OR ${BaseDatosLocalHelper.COLUMNA_TRADUCCION} LIKE ?) AND ${BaseDatosLocalHelper.COLUMNA_CONTEXTO} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = ?"
            argumentos = arrayOf(usuarioId, busqueda, busqueda, contexto!!, "0")
        } else {
            seleccion = "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND (${BaseDatosLocalHelper.COLUMNA_TEXTO_INGLES} LIKE ? OR ${BaseDatosLocalHelper.COLUMNA_TRADUCCION} LIKE ?) AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = ?"
            argumentos = arrayOf(usuarioId, busqueda, busqueda, "0")
        }

        val cursor = db.query(
            BaseDatosLocalHelper.TABLA_ORACIONES,
            null,
            seleccion,
            argumentos,
            null,
            null,
            "${BaseDatosLocalHelper.COLUMNA_FECHA_CREACION} DESC"
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                lista.add(mapearCursorAOracion(c))
            }
        }
        return lista
    }

    /**
     * Obtiene una oración por su ID.
     */
    fun obtenerPorId(id: String, usuarioId: String): OracionModelo? {
        val db = baseDatosHelper.readableDatabase
        val cursor = db.query(
            BaseDatosLocalHelper.TABLA_ORACIONES,
            null,
            "${BaseDatosLocalHelper.COLUMNA_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ?",
            arrayOf(id, usuarioId),
            null,
            null,
            null
        )

        return cursor.use { c ->
            if (c.moveToFirst()) mapearCursorAOracion(c) else null
        }
    }

    /**
     * Marca una oración como eliminada o la borra directamente si nunca se subió a Firebase.
     */
    fun marcarComoEliminada(id: String, usuarioId: String): Boolean {
        val db = baseDatosHelper.writableDatabase
        val oracion = obtenerPorId(id, usuarioId) ?: return false

        return if (oracion.sincronizado) {
            val valores = ContentValues().apply {
                put(BaseDatosLocalHelper.COLUMNA_ELIMINADO, 1)
                put(BaseDatosLocalHelper.COLUMNA_SINCRONIZADO, 0)
            }
            db.update(
                BaseDatosLocalHelper.TABLA_ORACIONES,
                valores,
                "${BaseDatosLocalHelper.COLUMNA_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ?",
                arrayOf(id, usuarioId)
            ) > 0
        } else {
            eliminarDefinitivo(id, usuarioId)
        }
    }

    /**
     * Elimina físicamente el registro de SQLite.
     */
    fun eliminarDefinitivo(id: String, usuarioId: String): Boolean {
        val db = baseDatosHelper.writableDatabase
        return db.delete(
            BaseDatosLocalHelper.TABLA_ORACIONES,
            "${BaseDatosLocalHelper.COLUMNA_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ?",
            arrayOf(id, usuarioId)
        ) > 0
    }

    /**
     * Obtiene oraciones pendientes de sincronizar con Firebase.
     */
    fun obtenerPendientesDeSincronizar(usuarioId: String): List<OracionModelo> {
        if (usuarioId.isBlank()) return emptyList()
        val lista = mutableListOf<OracionModelo>()
        val db = baseDatosHelper.readableDatabase
        val cursor = db.query(
            BaseDatosLocalHelper.TABLA_ORACIONES,
            null,
            "${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_SINCRONIZADO} = ?",
            arrayOf(usuarioId, "0"),
            null,
            null,
            null
        )

        cursor.use { c ->
            while (c.moveToNext()) {
                lista.add(mapearCursorAOracion(c))
            }
        }
        return lista
    }

    /**
     * Marca una oración como sincronizada en la nube.
     */
    fun marcarComoSincronizado(id: String, usuarioId: String) {
        val db = baseDatosHelper.writableDatabase
        val valores = ContentValues().apply {
            put(BaseDatosLocalHelper.COLUMNA_SINCRONIZADO, 1)
        }
        db.update(
            BaseDatosLocalHelper.TABLA_ORACIONES,
            valores,
            "${BaseDatosLocalHelper.COLUMNA_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ?",
            arrayOf(id, usuarioId)
        )
    }

    /**
     * Cuenta el número total de oraciones activas de un usuario.
     */
    fun contarOraciones(usuarioId: String): Int {
        if (usuarioId.isBlank()) return 0
        val db = baseDatosHelper.readableDatabase
        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM ${BaseDatosLocalHelper.TABLA_ORACIONES} WHERE ${BaseDatosLocalHelper.COLUMNA_USUARIO_ID} = ? AND ${BaseDatosLocalHelper.COLUMNA_ELIMINADO} = 0",
            arrayOf(usuarioId)
        )
        return cursor.use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    private fun mapearCursorAOracion(cursor: Cursor): OracionModelo {
        val id = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_ID))
        val usuarioId = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_USUARIO_ID))
        val textoIngles = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_TEXTO_INGLES))
        val pronunciacionFonetica = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_PRONUNCIACION_FONETICA)) ?: ""
        val traduccion = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_TRADUCCION))
        val contexto = cursor.getString(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_CONTEXTO))
        val fechaCreacion = cursor.getLong(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_FECHA_CREACION))
        val sincronizado = cursor.getInt(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_SINCRONIZADO)) == 1
        val eliminado = cursor.getInt(cursor.getColumnIndexOrThrow(BaseDatosLocalHelper.COLUMNA_ELIMINADO)) == 1

        return OracionModelo(
            id = id,
            usuarioId = usuarioId,
            textoIngles = textoIngles,
            pronunciacionFonetica = pronunciacionFonetica,
            traduccion = traduccion,
            contexto = contexto,
            fechaCreacion = fechaCreacion,
            sincronizado = sincronizado,
            eliminado = eliminado
        )
    }
}
