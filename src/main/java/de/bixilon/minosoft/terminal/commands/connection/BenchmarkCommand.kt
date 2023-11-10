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

package de.bixilon.minosoft.terminal.commands.connection

import de.bixilon.kotlinglm.GLM.PIf
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.random.RandomUtil.nextDouble
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.kutil.random.RandomUtil.nextInt
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.commands.nodes.ArgumentNode
import de.bixilon.minosoft.commands.nodes.LiteralNode
import de.bixilon.minosoft.commands.parser.brigadier._int.IntParser
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.startInit
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.*
import kotlin.math.cbrt
import kotlin.math.sin

object BenchmarkCommand : ConnectionCommand {
    override var node = LiteralNode("benchmark").addChild(
        LiteralNode("players", executor = { benchmarkPlayers(it) }).addChild(ArgumentNode("count", parser = IntParser(min = 0, max = 100000), executable = true)),
    )


    private fun benchmark(connection: PlayConnection, count: Int, factory: (position: Vec3d, rotation: EntityRotation, id: Int) -> Entity) {
        val offset = connection.player.physics.position
        val random = Random()

        val entities: MutableList<Entity> = ArrayList(count)
        val spread = maxOf(cbrt(count.toDouble()), 5.0)
        for (id in 0 until count) {
            val position = offset + Vec3d(random.nextDouble(-spread, spread), random.nextDouble(-spread, spread), random.nextDouble(-spread, spread))
            val rotation = EntityRotation(random.nextFloat(-179.0f, 179.0f), random.nextFloat(-89.0f, 89.0f))
            val entity = factory.invoke(position, rotation, id)
            entity.startInit()
            entities += entity
            connection.world.entities.add(random.nextInt(100000, 200000), null, entity)
            // TODO: make them move randomly?
        }

        var progress = 0.0f
        connection.ticker += {
            val time = (millis() % 1000L) / 1000.0f
            progress += time
            if (progress > (PIf * 2)) {
                progress %= (PIf * 2)
            }
            val sin = sin(progress)
            val delta = Vec3d(sin, if (sin > 0) 0.1 else -0.1, -sin)

            for (entity in entities) {
                entity.forceMove(delta)
            }
        }
    }

    private fun benchmarkPlayers(stack: CommandStack) {
        val count = stack.get<Int>("count") ?: 100
        val type = stack.connection.registries.entityType[RemotePlayerEntity] ?: return

        benchmark(stack.connection, count) { position, rotation, id -> RemotePlayerEntity(stack.connection, type, EntityData(stack.connection, Int2ObjectOpenHashMap()), position, rotation, PlayerAdditional("Dummy $id")) }
    }
}
