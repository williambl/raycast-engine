package com.williambl.raycastengine.util.network

import com.beust.klaxon.JsonObject
import java.lang.reflect.Constructor

/**
 * Creates a new object from a classname and constructor arguments.
 *
 * @param className the fully-qualified class name of the object to create.
 * @param args the arguments to pass into the constructor. due to a limitation, must be non-null.
 *
 * @return the object, or null
 * @throws [NoSuchMethodException] if there are no matching constructors.
 */
fun createObject(className: String, vararg args: Any): Any? {
    return if (className == "") null else getConstructor(Class.forName(className), *args).newInstance(*args)
}

/**
 * Deserialise an object from JSON.
 *
 * JSON input must be of the form:
 * ```json
 * {
 *      "class": "com.example.ClassName",
 *      "args": [
 *          "arg1",
 *          true,
 *          3.0
 *      ]
 * }
 * ```
 *
 * @see writeObjectToJson
 */
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

/**
 * Find a constructor for a given class which takes given arguments.
 *
 * @param clazz the class to check.
 * @param args the arguments to pass into the constructor. due to a limitation, must be non-null
 * @throws [NoSuchMethodException] if no such constructor exists.
 */
fun getConstructor(clazz: Class<*>, vararg args: Any): Constructor<*> {
    val argTypes = args.map { it::class.javaObjectType }.toTypedArray()
    return clazz.declaredConstructors.firstOrNull { it.parameterTypes.map { type -> type.kotlin.javaObjectType }.toTypedArray().contentEquals(argTypes) }
            ?: throw NoSuchMethodException("${clazz.name}: ${argTypes.joinToString(" ") { it.name }}")
}

/**
 * Serialise an object to JSON.
 *
 * JSON output will be of the form:
 * ```json
 * {
 *      "class": "com.example.ClassName",
 *      "args": [
 *          "arg1",
 *          true,
 *          3.0
 *      ]
 * }
 * ```
 *
 * @see getObjectFromJson
 */
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