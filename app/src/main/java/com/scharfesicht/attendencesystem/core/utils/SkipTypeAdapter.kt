package com.scharfesicht.attendencesystem.core.utils

import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

/**
 * TypeAdapter that safely handles any JSON value (string, array, object, null)
 * by skipping it during deserialization
 */
class SkipTypeAdapter : TypeAdapter<Any?>() {
    override fun write(out: JsonWriter, value: Any?) {
        out.nullValue()
    }

    override fun read(`in`: JsonReader): Any? {
        // Skip any value - string, array, object, null
        `in`.skipValue()
        return null
    }
}

/**
 * Create Gson instance that safely handles flexible API fields
 * Fields with type Any? will be skipped during parsing
 */
fun createSafeGson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(Any::class.java, SkipTypeAdapter())
        .setLenient() // Allow lenient parsing
        .create()
}