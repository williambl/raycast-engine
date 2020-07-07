package com.williambl.raycastengine

import com.beust.klaxon.JsonObject

/*
 * Creates a new object from the classname and arguments.
 */
fun createObject(className: String, constructor: Int, vararg args: Any): Any? {
    println(className)
    return if (className == "") null else Class.forName(className).constructors[constructor]?.newInstance(*args)
}

fun getObjectFromJson(jsonObject: JsonObject): Any? {
    val args = jsonObject.array<Any>("args")
    return createObject(
            jsonObject.string("class")!!,
            jsonObject.int("constructor") ?: 0,
            *(args?.map { if (it is JsonObject) getObjectFromJson(it) else it }?.filterNotNull()?.toTypedArray()
                    ?: arrayOf())
    )
}

fun writeObjectToJson(klass: Class<*>?, constructor: Int, vararg args: Any?): JsonObject {
    val map = mutableMapOf<String, Any?>()
    if (klass == null) {
        map["class"] = ""
        return JsonObject(map)
    }

    map["class"] = klass.canonicalName
    map["constructor"] = constructor

    map["args"] = args.map {
        if (it is String || it is Int || it is Long || it is Float || it is Double || it is Char || it is Boolean || it is JsonObject)
            it
        else throw IllegalArgumentException()
    }

    return JsonObject(map)
}