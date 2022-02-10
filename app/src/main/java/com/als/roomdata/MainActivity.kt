package com.als.roomdata

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.als.roomdata.CityMapper.toContentValues
import com.als.roomdata.CityMapper.toEntity
import com.als.roomdata.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val historySource = HistorySource(contentResolver)
        historySource.query()

        binding.insert.setOnClickListener {
            historySource.insert(HistoryEntity(1100, "Багамы", 35))
        }
        binding.get.setOnClickListener {
            historySource.getHistory()
        }
        binding.getByPosition.setOnClickListener {
            historySource.getCityByPosition(1)
        }
        binding.update.setOnClickListener {
            historySource.update(HistoryEntity(1100, "Гонолулу", 53))
        }
        binding.delete.setOnClickListener {
            historySource.delete(HistoryEntity(1100))
        }
    }
}

data class HistoryEntity(
    val id: Long = 0,
    val city: String = "",
    val temperature: Int = 0,
)

object CityMapper {
    private const val ID = "id"
    private const val CITY = "city"
    private const val TEMPERATURE = "temperature"

    @SuppressLint("Range")
    fun toEntity(cursor: Cursor): HistoryEntity {
        return HistoryEntity(
            cursor.getLong(cursor.getColumnIndex(ID)),
            cursor.getString(cursor.getColumnIndex(CITY)),
            cursor.getInt(cursor.getColumnIndex(TEMPERATURE))
        )
    }

    fun toContentValues(history: HistoryEntity): ContentValues {
        return ContentValues().apply {
            put(ID, history.id)
            put(CITY, history.city)
            put(TEMPERATURE, history.temperature)
        }
    }
}

class HistorySource(
    private val contentResolver: ContentResolver // Работаем с Content Provider через этот класс
) {

    private var cursor: Cursor? = null

    // Получаем запрос
    fun query() {
        cursor = contentResolver.query(HISTORY_URI, null, null, null, null)
    }

    fun getHistory() {
        // Отправляем запрос на получение таблицы с историей запросов и получаем ответ в виде Cursor
        cursor?.let { cursor ->
            for (i in 0..cursor.count) {
                // Переходим на позицию в Cursor
                if (cursor.moveToPosition(i)) {
                    // Берём из Cursor строку
                    toEntity(cursor)
                    query()
                }
            }
        }
        cursor?.close()
    }

    // Получаем данные о запросе по позиции
    fun getCityByPosition(position: Int): HistoryEntity {
        return if (cursor == null) {
            HistoryEntity()
        } else {
            cursor?.moveToPosition(position)
            toEntity(cursor!!)
        }
    }

    // Добавляем новый город
    fun insert(entity: HistoryEntity) {
        contentResolver.insert(HISTORY_URI, toContentValues(entity))
        query() // Снова открываем Cursor для повторного чтения данных
    }

    // Редактируем данные
    fun update(entity: HistoryEntity) {
        val uri: Uri = ContentUris.withAppendedId(HISTORY_URI, entity.id)
        contentResolver.update(uri, toContentValues(entity), null, null)
        query() // Снова открываем Cursor для повторного чтения данных
    }

    // Удалить запись в истории запросов
    fun delete(entity: HistoryEntity) {
        val uri: Uri = ContentUris.withAppendedId(HISTORY_URI, entity.id)
        contentResolver.delete(uri, null, null)
        query() // Снова открываем Cursor для повторного чтения данных
    }

    companion object {
        // URI для доступа к Content Provider
        private val HISTORY_URI: Uri =
            Uri.parse("content://com.als.l2/history")
    }
}
