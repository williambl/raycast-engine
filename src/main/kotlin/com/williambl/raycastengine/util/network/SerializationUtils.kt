package com.williambl.raycastengine.util.network

import com.beust.klaxon.JsonObject
import java.lang.reflect.Constructor

/*
 * Creates a new object from the classname and arguments.
 */
fun createObject(className: String, vararg args: Any): Any? {
    return if (className == "") null else getConstructor(Class.forName(className), *args).newInstance(*args)
}

fun getObjectFromJson(jsonObject: JsonObject): Any? {
    return createObject(
            jsonObject.string("class")!!,
            *(
                    jsonObject.array<Any>("args")
                            ?.mapNotNull { if (it is JsonObject) getObjectFromJson(it) else it }
                            ?.toTypedArray()
                            ?: arrayOf()
                    )
    )
}

fun getConstructor(clazz: Class<*>, vararg args: Any): Constructor<*> {
    val argTypes = args.map { it::class.javaObjectType }.toTypedArray()
    return clazz.declaredConstructors.firstOrNull { it.parameterTypes.map { type -> type.kotlin.javaObjectType }.toTypedArray().contentEquals(argTypes) }
            ?: throw NoSuchMethodException("${clazz.name}: ${argTypes.joinToString(" ") { it.name }}")
}

fun writeObjectToJson(klass: Class<*>?, vararg args: Any?): JsonObject {
    val map = mutableMapOf<String, Any?>()
    if (klass == null) {
        map["class"] = ""
        return JsonObject(map)
    }

    map["class"] = klass.canonicalName
    map["args"] = args.map {
        if (it is String || it is Int || it is Long || it is Float || it is Double || it is Char || it is Boolean || it is JsonObject)
            it
        else throw IllegalArgumentException()
    }

    return JsonObject(map)
}