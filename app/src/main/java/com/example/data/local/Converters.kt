package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.MessageStatus
import com.example.data.model.MessageType
import com.example.data.model.SenderType

class Converters {
    @TypeConverter
    fun fromSenderType(value: SenderType): String = value.name

    @TypeConverter
    fun toSenderType(value: String): SenderType = try {
        SenderType.valueOf(value)
    } catch (e: Exception) {
        SenderType.OTHER
    }

    @TypeConverter
    fun fromMessageStatus(value: MessageStatus): String = value.name

    @TypeConverter
    fun toMessageStatus(value: String): MessageStatus = try {
        MessageStatus.valueOf(value)
    } catch (e: Exception) {
        MessageStatus.SENT
    }

    @TypeConverter
    fun fromMessageType(value: MessageType): String = value.name

    @TypeConverter
    fun toMessageType(value: String): MessageType = try {
        MessageType.valueOf(value)
    } catch (e: Exception) {
        MessageType.TEXT
    }
}
