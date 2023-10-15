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

package de.bixilon.minosoft.data.physics

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.primitive.DoubleUtil
import de.bixilon.kutil.primitive.DoubleUtil.matches
import de.bixilon.kutil.primitive.FloatUtil
import de.bixilon.kutil.primitive.FloatUtil.matches
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.camera.ConnectionCamera
import de.bixilon.minosoft.data.entities.StatusEffectInstance
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.local.SignatureKeyManagement
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeOperations
import de.bixilon.minosoft.data.registries.effects.attributes.AttributeType
import de.bixilon.minosoft.data.registries.effects.attributes.EntityAttributes
import de.bixilon.minosoft.data.registries.effects.attributes.MinecraftAttributes
import de.bixilon.minosoft.data.registries.effects.attributes.container.AttributeModifier
import de.bixilon.minosoft.data.registries.effects.movement.MovementEffect
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.matches
import de.bixilon.minosoft.util.KUtil.startInit
import org.testng.Assert
import java.util.*
import kotlin.reflect.jvm.javaField

object PhysicsTestUtil {
    const val MATCH_EXACTLY = true
    val VALUE_MARGIN = if (MATCH_EXACTLY) 0.0 else DoubleUtil.DEFAULT_MARGIN

    private val PLAYER_ATTRIBUTES = LocalPlayerEntity::attributes.javaField!!
    private val CONNECTION_PLAYER = PlayConnection::player.javaField!!
    private val CONNECTION_CAMERA = PlayConnection::camera.javaField!!


    fun createPlayer(connection: PlayConnection = createConnection(light = false)): LocalPlayerEntity {
        val player = LocalPlayerEntity(connection.account, connection, SignatureKeyManagement(connection, connection.account))
        PLAYER_ATTRIBUTES.forceSet(player, EntityAttributes(player.type.attributes))
        player.startInit()
        CONNECTION_PLAYER.forceSet(connection, player)
        CONNECTION_CAMERA.forceSet(connection, ConnectionCamera(connection))
        connection.camera.init()
        connection.world.entities.remove(0)
        connection.world.entities.add(0, null, player)
        return player
    }

    fun LocalPlayerEntity.addModifier(type: AttributeType, amount: Double, amplifier: Int, operation: AttributeOperations = AttributeOperations.MULTIPLY_TOTAL) {
        this.attributes.add(type, AttributeModifier(null, UUID.randomUUID(), amount * (amplifier + 1), operation))
    }

    fun LocalPlayerEntity.applySpeed(level: Int, duration: Int = 10000) {
        this.effects += StatusEffectInstance(MovementEffect.Speed, level, duration)
        addModifier(MinecraftAttributes.MOVEMENT_SPEED, 0.20000000298023224, level)
    }

    fun LocalPlayerEntity.applySlowness(level: Int) {
        this.effects += StatusEffectInstance(MovementEffect.Slowness, level, 10000)
        addModifier(MinecraftAttributes.MOVEMENT_SPEED, -0.15000000596046448, level)
    }

    fun assertEquals(actual: Double, expected: Double, margin: Double = DoubleUtil.DEFAULT_MARGIN) {
        if (actual.matches(expected, margin)) {
            return
        }
        Assert.assertEquals(actual, expected)
    }

    fun assertEquals(actual: Float, expected: Float, margin: Float = FloatUtil.DEFAULT_MARGIN) {
        if (actual.matches(expected, margin)) {
            return
        }
        Assert.assertEquals(actual, expected)
    }

    private fun Double.formatted(): String {
        return String.format("%.18f", this)
    }

    private fun Vec3d.formatted(): String {
        return "(${x.formatted()}, ${y.formatted()}, ${z.formatted()})"
    }

    fun Entity.assertPosition(x: Double, y: Double, z: Double, ticks: Int = 1) {
        val position = physics.position
        val expected = Vec3d(x, y, z)
        if (position.matches(expected, VALUE_MARGIN * ticks)) {
            return
        }
        val delta = (position - expected)
        Assert.assertEquals(position, expected, "Position mismatch:\nC${position.formatted()}\nE${expected.formatted()}\nD:${delta.formatted()}\n\n")
    }

    fun Entity.assertVelocity(x: Double, y: Double, z: Double, ticks: Int = 1) {
        val velocity = physics.velocity
        val expected = Vec3d(x, y, z)
        if (velocity.matches(expected, VALUE_MARGIN * ticks)) {
            return
        }
        val delta = (velocity - expected)
        Assert.assertEquals(velocity, expected, "Velocity mismatch:\nC${physics.velocity.formatted()}\nE${expected.formatted()}\nD:${delta.formatted()}\n\n")
    }

    fun Entity.assertGround(onGround: Boolean = true) {
        Assert.assertEquals(this.physics.onGround, onGround)
    }

    fun Entity.runTicks(count: Int) {
        for (tick in 0 until count) {
            this.forceTick(0L)
        }
    }

    fun Entity.kill() {
        if (this is LocalPlayerEntity) {
            this.healthCondition = this.healthCondition.copy(hp = 0.0f)
            return
        }
        TODO("Can not kill $this")
    }

    fun Entity.damage() {
        if (this is LocalPlayerEntity) {
            this.healthCondition = this.healthCondition.copy(hp = this.healthCondition.hp - 1.0f)
            return
        }
        TODO("Can not damage $this")
    }
}
