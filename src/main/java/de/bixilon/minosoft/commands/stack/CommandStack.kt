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

package de.bixilon.minosoft.commands.stack

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class CommandStack {
    private val stack: MutableList<StackEntry> = mutableListOf()
    val size: Int get() = stack.size

    var executor: Entity? = null
    lateinit var connection: PlayConnection

    inline operator fun <reified T> get(name: String): T? {
        return getAny(name).nullCast()
    }

    fun getAny(name: String): Any? {
        return stack.find { it.name == name }?.data
    }

    fun reset(size: Int) {
        var index = 0
        stack.removeAll { index++ >= size }
    }

    fun push(name: String, data: Any?) {
        stack.add(StackEntry(name, data))
    }

    private data class StackEntry(
        val name: String,
        val data: Any?,
    )
}
