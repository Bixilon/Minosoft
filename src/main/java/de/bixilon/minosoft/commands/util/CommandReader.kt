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

package de.bixilon.minosoft.commands.util

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.commands.errors.reader.map.DuplicatedKeyMapError
import de.bixilon.minosoft.commands.errors.reader.map.ExpectedKeyMapError
import de.bixilon.minosoft.commands.errors.reader.map.InvalidAssignCharMapError
import de.bixilon.minosoft.commands.errors.reader.map.InvalidMapSeparatorError

open class CommandReader(string: String) : StringReader(string) {

    fun <T> readNegateable(reader: CommandReader.() -> T): Pair<T, Boolean>? {
        if (!canPeek()) {
            return null
        }
        var negated = false
        if (peek() == '!'.code) {
            read()
            negated = true
        }
        val it = reader(this) ?: return null

        return Pair(it, negated)
    }

    fun <K, V> readMap(keyReader: CommandReader.() -> K?, valueReader: CommandReader.(key: ReadResult<K>) -> V): Map<K, V>? {
        if (!canPeekNext()) {
            return null
        }
        if (peekNext() != '['.code) {
            return null
        }
        readNext() // [
        val map: MutableMap<K, V> = mutableMapOf()
        while (true) {
            if (peek() == ']'.code) {
                break
            }
            skipWhitespaces()
            val key = readResult { keyReader(this@CommandReader) }
            if (key.result == null) {
                throw ExpectedKeyMapError(this, key)
            }
            val existing: V? = map[key.result]
            if (existing != null) {
                throw DuplicatedKeyMapError(this, key, existing)
            }
            val assign = read()
            if (assign != '='.code) {
                throw InvalidAssignCharMapError(this, pointer - 1, assign)
            }
            skipWhitespaces()
            val value = valueReader(this, key.unsafeCast())
            map[key.result] = value
            val end = read()
            if (end == ']'.code) {
                break
            }
            if (end != ','.code) {
                throw InvalidMapSeparatorError(this, pointer - 1, end)
            }
        }

        return map
    }
}
