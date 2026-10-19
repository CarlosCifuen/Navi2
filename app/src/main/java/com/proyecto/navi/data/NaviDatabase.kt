package com.proyecto.navi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * SQLite solo guarda tipos primitivos, así que el enum viaja como texto.
 * Se guarda `name` y no `ordinal`: si alguien reordena las constantes del enum,
 * con ordinal todas las filas existentes pasarían a significar otra cosa.
 */
class Converters {
    @TypeConverter
    fun tagToString(tag: TaskTag): String = tag.name

    @TypeConverter
    fun stringToTag(value: String): TaskTag = TaskTag.valueOf(value)
}

@Database(
    entities = [Task::class, Profile::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class NaviDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao
    abstract fun profileDao(): ProfileDao

    companion object {
        @Volatile
        private var INSTANCE: NaviDatabase? = null

        /**
         * Singleton: abrir varias conexiones a la misma base es caro y puede dar
         * resultados inconsistentes. El doble chequeo con @Volatile es el patrón
         * estándar para que dos hilos no construyan dos instancias a la vez.
         */
        fun getInstance(context: Context): NaviDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: build(context.applicationContext).also { INSTANCE = it }
            }

        private fun build(context: Context): NaviDatabase =
            Room.databaseBuilder(context, NaviDatabase::class.java, "navi.db")
                .addCallback(object : RoomDatabase.Callback() {
                    /**
                     * onCreate corre una sola vez en la vida de la base, no en
                     * cada arranque. Dispara al primer acceso real (una query),
                     * que siempre ocurre después de que build() retornó, así que
                     * para entonces INSTANCE ya está asignado.
                     *
                     * El scope es propio porque acá no hay lifecycle del cual
                     * colgarse; el seed es corto y de una sola vez.
                     */
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                database.taskDao().insertAll(seedTasks())
                                database.profileDao().upsert(seedProfile())
                            }
                        }
                    }
                })
                .build()
    }
}
