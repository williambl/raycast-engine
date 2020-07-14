package com.williambl.raycastengine

import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

inline fun <T> synced(initialValue: T, dirtyProperty: KMutableProperty<Boolean>/*, crossinline toBytes: (T, ByteBuf) -> Unit, crossinline fromBytes: (T, ByteBuf) -> Unit*/):
        ReadWriteProperty<Any?, T> =
        object : ObservableProperty<T>(initialValue) {
            override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = dirtyProperty.setter.call(true)
        }
