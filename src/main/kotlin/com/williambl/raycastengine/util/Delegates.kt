package com.williambl.raycastengine.util

import com.williambl.raycastengine.Main
import com.williambl.raycastengine.network.ServerNetworkManager
import com.williambl.raycastengine.util.network.writeString
import com.williambl.raycastengine.util.network.writeUUID
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import java.util.*
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

fun <T> synced(initialValue: T, ownerIdProp: KProperty<UUID>, toBytes: (ByteBuf, T) -> ByteBuf, fromBytes: (ByteBuf) -> T): ReadWriteProperty<Any?, T> =
        SyncedProperty(initialValue, ownerIdProp, toBytes, fromBytes)

class SyncedProperty<T>(private val initialValue: T, private val ownerIdProp: KProperty<UUID>, private val toBytes: (ByteBuf, T) -> ByteBuf, private val fromBytes: (ByteBuf) -> T) : ObservableProperty<T>(initialValue) {
    override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) {
        if (!Main.world.isClient) {
            val buf = Unpooled.buffer()
            buf.writeUUID(ownerIdProp.call())
            buf.writeString(property.name)
            toBytes(buf, newValue)
            ServerNetworkManager.sendPacketToAll("sync", buf)
        }
    }

    fun setFromBytes(property: KMutableProperty<*>, receiver: Any?, buf: ByteBuf) {
        property.setter.call(receiver, fromBytes(buf))
    }
}

