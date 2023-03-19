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

package de.bixilon.minosoft.commands.parser.selector

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.error.InvalidSelectorKeyError
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.commands.util.ReadResult

abstract class SelectorParser<T> : ArgumentParser<AbstractTarget<T>> {
    override val examples: List<Any?> = listOf("1", "@")
    protected abstract val properties: TargetProperties<T>

    override fun parse(reader: CommandReader): AbstractTarget<T> {
        if (!reader.canPeek()) {
            throw ExpectedArgumentError(reader)
        }
        return if (reader.peek() == '@'.code) {
            reader.parseSelector()
        } else {
            parseId(reader)
        }
    }

    fun CommandReader.parseSelector(): AbstractTarget<T> {
        unsafeRead('@'.code)

        val properties: Map<String, TargetProperty<T>> = readMap({ readKey() }, { readValue(it) }) ?: emptyMap()

        return SelectorTarget(properties)
    }

    private fun CommandReader.readKey(): String? {
        if (peek() == '"'.code) {
            return readUnquotedString()
        }
        return readUntil('='.code)
    }

    private fun CommandReader.readValue(key: ReadResult<String>): TargetProperty<T> {
        val target = properties[key.result] ?: throw InvalidSelectorKeyError(this, key)
        return target.read(this)
    }

    abstract fun parseId(reader: CommandReader): AbstractTarget<T>
}
