/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.physics.pipeline

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class PhysisPipeline<E : Entity>(private val connection: PlayConnection) {
    val parts: MutableList<PipelinePart<out E>> = mutableListOf()

    private fun getIndex(name: String): Int {
        val index = parts.indexOfFirst { it.name == name }
        if (index < 0) {
            throw IllegalArgumentException("$name is not part of the pipeline")
        }
        return index
    }

    fun addAfter(after: String, part: PipelinePart<out E>) {
        checkContains(after)
        parts.add(getIndex(after) + 1, part)
    }

    fun addBefore(before: String, part: PipelinePart<out E>) {
        checkContains(before)
        parts.add(getIndex(before), part)
    }

    fun addLast(part: PipelinePart<out E>) {
        checkContains(part.name)
        parts.add(part)
    }

    fun buildLast(builder: PipelineBuilder<out E, out PipelinePart<E>>) {
        checkContains(builder.name)
        parts.add(builder.build(connection))
    }

    fun addFirst(part: PipelinePart<out E>) {
        checkContains(part.name)
        parts.add(0, part)
    }

    fun buildFirst(builder: PipelineBuilder<out E, out PipelinePart<E>>) {
        checkContains(builder.name)
        parts.add(0, builder.build(connection))
    }

    operator fun <V : PipelinePart<*>> get(name: String): V? {
        return parts.find { it.name == name }?.unsafeCast()
    }

    operator fun contains(name: String): Boolean {
        return this.get<PipelinePart<*>>(name) != null
    }

    private fun checkContains(name: String) {
        if (name in this) {
            throw IllegalArgumentException("$name is already part of the pipeline!")
        }
    }

    fun run(entity: E) {
        val context = PipelineContext()
        for (part in parts) {
            part.unsafeCast<PipelinePart<E>>().handle(context, entity)
            if (context.cancelRest) {
                break
            }
        }
    }
}
