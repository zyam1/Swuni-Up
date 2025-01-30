package com.example.swuni_up

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class ADDLogDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "swuniup.db"
        const val DATABASE_VERSION = 1
        const val TABLE_LOG = "Log"

        const val COLUMN_ID = "log_id"
        const val COLUMN_CHALLENGER_ID = "challenger_id"
        const val COLUMN_DATE = "log_date"
        const val COLUMN_PHOTO = "log_image"
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA foreign_keys=ON;")
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("ADDLogDBHelper", "onCreate called")
        val createTableQuery = """
            CREATE TABLE $TABLE_LOG (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CHALLENGER_ID INTEGER NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_PHOTO BLOB NOT NULL,
                FOREIGN KEY ($COLUMN_CHALLENGER_ID) REFERENCES Challenger(challenger_id)
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_LOG")
        onCreate(db)
    }

    fun insertLog(logEntry: LogEntry): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CHALLENGER_ID, logEntry.challengerId)
            put(COLUMN_DATE, logEntry.logDate)
            put(COLUMN_PHOTO, logEntry.logImage)
        }

        Log.d("ADDLogDBHelper", "Inserting LogEntry: $values")

        return db.insert(TABLE_LOG, null, values)
    }

    // LogEntry 데이터 클래스
    data class LogEntry(
        val challengerId: Long,
        val logDate: String,
        val logImage: ByteArray
    )
}
