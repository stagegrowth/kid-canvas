package com.stagegrowth.kidcanvas.data.local

import androidx.room.TypeConverter
import com.stagegrowth.kidcanvas.domain.model.Stroke
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Room TypeConverter: List<Stroke> ↔ JSON String 양방향 변환.
 * kotlinx.serialization 사용. Database 클래스에 @TypeConverters 로 등록.
 */
class Converters {

    @TypeConverter
    fun fromStrokeList(strokes: List<Stroke>): String =
        json.encodeToString(strokeListSerializer, strokes)

    @TypeConverter
    fun toStrokeList(value: String): List<Stroke> =
        if (value.isBlank()) emptyList()
        else json.decodeFromString(strokeListSerializer, value)

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        private val strokeListSerializer = ListSerializer(Stroke.serializer())
    }
}
