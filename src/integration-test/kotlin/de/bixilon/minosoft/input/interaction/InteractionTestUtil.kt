/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
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
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertPacket
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil
import de.bixilon.minosoft.protocol.packets.c2s.play.item.UseItemC2SP
import glm_.vec3.Vec3d
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.Assert.assertEquals

object InteractionTestUtil {
    private val PRESS = KeyHandler::class.java.getDeclaredMethod("onPress").apply { setUnsafeAccessible() }
    private val TICK = KeyHandler::class.java.getDeclaredMethod("onTick").apply { setUnsafeAccessible() }
    private val RELEASE = KeyHandler::class.java.getDeclaredMethod("onRelease").apply { setUnsafeAccessible() }

    private val pig = EntityType(Pig.identifier, minecraft(""), 1.0f, 1.0f, mapOf(), Pig, null)

    fun createSession(): PlaySession {
        val session = SessionTestUtil.createSession(0)
        createPlayer(session)

        return session
    }

    fun PlaySession.lookAtPig(distance: Double = 1.0): Entity {
        return this.lookAt(pig, distance)
    }

    fun PlaySession.lookAt(type: EntityType, distance: Double = 1.0): Entity {
        return this.lookAt(type.factory.build(this, this@InteractionTestUtil.pig, EntityData(this, Int2ObjectOpenHashMap()), Vec3d.EMPTY, EntityRotation.EMPTY)!!, distance)
    }

    fun PlaySession.lookAt(entity: Entity, distance: Double = 1.0): Entity {
        world.entities.add(10, null, entity)
        camera.target::target.forceSet(DataObserver(EntityTarget(Vec3d.EMPTY, distance, Directions.DOWN, entity)))

        return entity
    }

    fun PlaySession.lookAtPlayer(distance: Double = 1.0): RemotePlayerEntity {
        val player = RemotePlayerEntity(this, registries.entityType["player"]!!, EntityData(this, Int2ObjectOpenHashMap()), additional = PlayerAdditional("dummy"))

        return this.lookAt(player).unsafeCast()
    }

    fun KeyHandler.tick(count: Int = 1) {
        for (i in 0 until count) {
            TICK.invoke(this)
        }
    }


    fun KeyHandler.unsafePress() {
        PRESS.invoke(this)
    }

    fun KeyHandler.unsafeRelease() {
        RELEASE.invoke(this)
    }

    fun PlaySession.assertUseItem(hand: Hands) {
        assertEquals(assertPacket(UseItemC2SP::class.java).hand, hand)
    }
}
