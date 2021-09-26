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

/**
 * A property synced from server->client.
 *
 * Properties using the `synced` delegate are automatically synced when changed on the server. They act transparently,
 * looking just like normal properties; however, do not change them too often (i.e. multiple times a tick), as every
 * change sends a packet to all clients!
 *
 * @param initialValue the initial value of the property.
 * @param ownerIdProp the property holding the UUID of the [GameObject][com.williambl.raycastengine.gameobject.GameObject] which owns this property.
 * @param toBytes a function used to serialise the property, used when syncing.
 * @param fromBytes a function used to deserialise the property, used when syncing.
 *
 * @see SyncedProperty
 */
fun <T> synced(initialValue: T, ownerIdProp: KProperty<UUID>, toBytes: (ByteBuf, T) -> ByteBuf, fromBytes: (ByteBuf) -> T): ReadWriteProperty<Any?, T> =
        SyncedProperty(initialValue, ownerIdProp, toBytes, fromBytes)

/**
 * A property synced from server to client.
 *
 * @param T the type of the property.
 * @param initialValue the initial value of the property.
 * @property ownerIdProp the property holding the UUID of the [GameObject][com.williambl.raycastengine.gameobject.GameObject] which owns this property.
 * @property toBytes a function used to serialise the property, used when syncing.
 * @property fromBytes a function used to deserialise the property, used when syncing.
 *
 * @see synced
 */
class SyncedProperty<T>(initialValue: T, private val ownerIdProp: KProperty<UUID>, private val toBytes: (ByteBuf, T) -> ByteBuf, private val fromBytes: (ByteBuf) -> T) : ObservableProperty<T>(initialValue) {
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

