package com.scharfesicht.attendencesystem.core.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.scharfesicht.attendencesystem.features.attendance.domain.model.*
import java.lang.reflect.Type

/**
 * Custom deserializer for flexible API fields that can be string, array, or object
 */
class FlexibleDataDeserializer : JsonDeserializer<Any?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): Any? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> json.asString
            json.isJsonArray -> json.asJsonArray.toString()
            json.isJsonObject -> json.asJsonObject.toString()
            else -> null
        }
    }
}

/**
 * Custom deserializer for QR data (can be string or array)
 */
class QrDataDeserializer : JsonDeserializer<QrData?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): QrData? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> QrData(json.asString)
            json.isJsonArray -> QrData(json.asJsonArray.toString())
            json.isJsonObject -> QrData(json.asJsonObject.toString())
            else -> null
        }
    }
}

/**
 * Custom deserializer for NFC data (can be string or array)
 */
class NfcDataDeserializer : JsonDeserializer<NfcData?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): NfcData? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> NfcData(json.asString)
            json.isJsonArray -> NfcData(json.asJsonArray.toString())
            json.isJsonObject -> NfcData(json.asJsonObject.toString())
            else -> null
        }
    }
}

/**
 * Custom deserializer for Permissions data (can be string or object)
 */
class PermissionsDataDeserializer : JsonDeserializer<PermissionsData?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PermissionsData? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> PermissionsData(json.asString)
            json.isJsonObject -> PermissionsData(json.asJsonObject.toString())
            json.isJsonArray -> PermissionsData(json.asJsonArray.toString())
            else -> null
        }
    }
}

/**
 * Custom deserializer for Active Short Leave (can be anything)
 */
class ActiveShortLeaveDeserializer : JsonDeserializer<ActiveShortLeave?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ActiveShortLeave? {
        return when {
            json == null || json.isJsonNull -> null
            json.isJsonPrimitive -> ActiveShortLeave(json.asString)
            json.isJsonObject -> ActiveShortLeave(json.asJsonObject.toString())
            json.isJsonArray -> ActiveShortLeave(json.asJsonArray.toString())
            else -> null
        }
    }
}

/**
 * Custom deserializer for Managers list (can be empty array or array of objects)
 */
class ManagerListDeserializer : JsonDeserializer<List<Manager>> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): List<Manager> {
        return try {
            when {
                json == null || json.isJsonNull -> emptyList()
                json.isJsonArray -> {
                    val array = json.asJsonArray
                    if (array.size() == 0) {
                        emptyList()
                    } else {
                        // Try to parse as Manager objects
                        try {
                            array.map { element ->
                                if (element.isJsonObject) {
                                    val obj = element.asJsonObject
                                    Manager(
                                        id = obj.get("id")?.asInt ?: 0,
                                        name = obj.get("name")?.asString ?: "",
                                        uuid = obj.get("uuid")?.asString ?: ""
                                    )
                                } else {
                                    Manager() // Empty manager for non-object elements
                                }
                            }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                }
                else -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * Custom deserializer for Active Leave
 */
class ActiveLeaveDeserializer : JsonDeserializer<ActiveLeave?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ActiveLeave? {
        return try {
            when {
                json == null || json.isJsonNull -> null
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    val leave = obj.get("leave")
                    val leaveData = if (leave != null && leave.isJsonObject) {
                        val leaveObj = leave.asJsonObject
                        Leave(
                            id = leaveObj.get("id")?.asInt ?: 0,
                            type = leaveObj.get("type")?.asString ?: "",
                            start_date = leaveObj.get("start_date")?.asString ?: "",
                            end_date = leaveObj.get("end_date")?.asString ?: ""
                        )
                    } else {
                        null
                    }

                    ActiveLeave(
                        is_disable = obj.get("is_disable")?.asInt ?: 0,
                        message = obj.get("message")?.asString,
                        leave = leaveData
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Custom deserializer for Auth Device (can be null or object)
 */
class AuthDeviceDeserializer : JsonDeserializer<AuthDevice?> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): AuthDevice? {
        return try {
            when {
                json == null || json.isJsonNull -> null
                json.isJsonObject -> {
                    val obj = json.asJsonObject
                    AuthDevice(
                        id = obj.get("id")?.asInt ?: 0,
                        name = obj.get("name")?.asString ?: "",
                        type = obj.get("type")?.asString ?: ""
                    )
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Create Gson instance with custom deserializers
 */
fun createFlexibleGson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(QrData::class.java, QrDataDeserializer())
        .registerTypeAdapter(NfcData::class.java, NfcDataDeserializer())
        .registerTypeAdapter(PermissionsData::class.java, PermissionsDataDeserializer())
        .registerTypeAdapter(ActiveShortLeave::class.java, ActiveShortLeaveDeserializer())
        .registerTypeAdapter(object : TypeToken<List<Manager>>() {}.type, ManagerListDeserializer())
        .registerTypeAdapter(ActiveLeave::class.java, ActiveLeaveDeserializer())
        .registerTypeAdapter(AuthDevice::class.java, AuthDeviceDeserializer())
        .setLenient() // Allow lenient parsing
        .create()
}