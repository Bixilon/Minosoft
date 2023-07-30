/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.input.interaction

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.target.targets.EntityTarget
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.animal.Pig
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.createPlayer
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.Assert.assertEquals

object InteractionTestUtil {
    private val PRESS = KeyHandler::class.java.getDeclaredMethod("onPress").apply { isAccessible = true }
    private val TICK = KeyHandler::class.java.getDeclaredMethod("onTick").apply { isAccessible = true }
    private val RELEASE = KeyHandler::class.java.getDeclaredMethod("onRelease").apply { isAccessible = true }

    private val pig = EntityType(Pig.identifier, minecraft(""), 1.0f, 1.0f, true, false, mapOf(), Pig, null)

    fun createConnection(): PlayConnection {
        val connection = ConnectionTestUtil.createConnection(0)
        val player = createPlayer(connection)

        return connection
    }

    fun PlayConnection.lookAtPig(distance: Double = 1.0): Entity {
        return this.lookAt(pig, distance)
    }

    fun PlayConnection.lookAt(type: EntityType, distance: Double = 1.0): Entity {
        return this.lookAt(type.factory.build(this, this@InteractionTestUtil.pig, EntityData(this, Int2ObjectOpenHashMap()), Vec3d.EMPTY, EntityRotation.EMPTY)!!, distance)
    }

    fun PlayConnection.lookAt(entity: Entity, distance: Double = 1.0): Entity {
        world.entities.add(10, null, entity)
        camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, distance, Directions.DOWN, entity)))

        return entity
    }

    fun PlayConnection.lookAtPlayer(distance: Double = 1.0): RemotePlayerEntity {
        val player = RemotePlayerEntity(this, registries.entityType["player"]!!, EntityData(this, Int2ObjectOpenHashMap()), additional = PlayerAdditional("dummy"))

        return this.lookAt(player).unsafeCast()
    }

    fun KeyHandler.tick(count: Int = 1) {
        for (i in 0 until count) {
            TICK.invoke(this)
        }
    }


    fun KeyHandler.unsafePress() {
        this::isPressed.forceSet(true)
        PRESS.invoke(this)
    }

    fun KeyHandler.unsafeRelease() {
        this::isPressed.forceSet(false)
        RELEASE.invoke(this)
    }

    fun PlayConnection.assertUseItem(hand: Hands) {
        assertEquals(assertPacket(UseItemC2SP::class.java).hand, hand)
    }
}
