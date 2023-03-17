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

package de.bixilon.minosoft.data.physics.parkour

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.string.WhitespaceUtil.trimWhitespaces
import de.bixilon.minosoft.assets.util.InputStreamUtil.readAsString
import de.bixilon.minosoft.commands.parser.brigadier._double.DoubleParser.Companion.readDouble
import de.bixilon.minosoft.commands.parser.brigadier._float.FloatParser.Companion.readFloat
import de.bixilon.minosoft.commands.util.StringReader
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.assertPosition
import de.bixilon.minosoft.data.physics.PhysicsTestUtil.runTicks
import de.bixilon.minosoft.input.camera.PlayerMovementInput

object ParkourUtil {

    fun read(id: String): List<ParkourTick> {
        val input = ParkourTick::class.java.getResourceAsStream("/parkour/$id.txt")!!.readAsString()

        val ticks: MutableList<ParkourTick> = mutableListOf()

        for (line in input.lines()) {
            if (line.isEmpty() || line.startsWith("#")) continue

            val inputRaw = line.split("-->")
            val movementInput = inputRaw[0].toMovementInput()

            val (positionRaw, rotationRaw) = inputRaw[1].trimWhitespaces().split(" ")
            val position = positionRaw.toPosition()
            val rotation = rotationRaw.toRotation()

            ticks += ParkourTick(
                input = movementInput,
                position = position,
                rotation = rotation,
            )
        }

        return ticks
    }

    private fun StringReader.readMovementType(): String {
        if (peek() == ','.code) read()

        val builder = StringBuilder()

        while (true) {
            val peek = peek() ?: break
            if (peek == '='.code) {
                read()
                break
            }
            builder.append(peek.toChar())
            read()
        }

        return builder.toString()
    }

    fun String.toMovementInput(): PlayerMovementInput {
        val reader = StringReader(this)
        reader.skipWhitespaces()
        var input = PlayerMovementInput()

        while (true) {
            val type = reader.readMovementType()
            val value = reader.readUntil(','.code, ' '.code).toBoolean()
            input = when (type) {
                "f" -> input.copy(forward = value)
                "b" -> input.copy(backward = value)
                "l" -> input.copy(left = value)
                "r" -> input.copy(right = value)
                "j" -> input.copy(jump = value)
                "sn" -> input.copy(sneak = value)
                "sp" -> input.copy(sprint = value)
                else -> break
            }
        }
        return input
    }

    fun String.toPosition(): Vec3d {
        val reader = StringReader(this)
        reader.skipWhitespaces()
        val x = reader.readDouble()!!
        reader.read('|'.code)
        val y = reader.readDouble()!!
        reader.read('|'.code)
        val z = reader.readDouble()!!

        return Vec3d(x, y, z)
    }

    fun String.toRotation(): EntityRotation {
        val reader = StringReader(this)
        reader.skipWhitespaces()
        val yaw = reader.readFloat()!!
        reader.read('|'.code)
        val pitch = reader.readFloat()!!

        return EntityRotation(yaw, pitch)
    }

    fun LocalPlayerEntity.run(ticks: List<ParkourTick>) {
        val first = ticks.first()
        forceTeleport(first.position)
        forceRotate(first.rotation)
        runTicks(10) // clear initial movement, etc
        for ((index, tick) in ticks.withIndex()) {
            if (index == 0) continue
            try {
                forceRotate(tick.rotation)
                this.run(tick)
            } catch (error: Throwable) {
                throw AssertionError("Failed at tick: $index", error)
            }
        }
    }

    fun LocalPlayerEntity.run(tick: ParkourTick) {
        this.input = tick.input
        runTicks(1)
        assertPosition(tick.position.x, tick.position.y, tick.position.z)
    }
}
